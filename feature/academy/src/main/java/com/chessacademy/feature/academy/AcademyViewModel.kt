package com.chessacademy.feature.academy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessacademy.chess.board.BoardState
import com.chessacademy.chess.rules.BoardPosition
import com.chessacademy.chess.rules.ChessRules
import com.chessacademy.chess.rules.Square
import com.chessacademy.core.database.dao.TrainingProgressDao
import com.chessacademy.core.database.entity.TrainingProgressEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AcademyUiState(
    val lessons: List<AcademyLesson> = emptyList(),
    val selectedLesson: AcademyLesson? = null,
    val currentStepIndex: Int = 0,
    val boardPosition: BoardPosition = BoardPosition.startingPosition(),
    val boardState: BoardState = BoardState(),
    val feedbackMessage: String? = null,
    val isStepCompleted: Boolean = false,
    val isLessonFinished: Boolean = false,
    val completedLessonIds: Set<String> = emptySet()
)

@HiltViewModel
class AcademyViewModel @Inject constructor(
    private val academyRepository: AcademyRepository,
    private val trainingProgressDao: TrainingProgressDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademyUiState())
    val uiState: StateFlow<AcademyUiState> = _uiState.asStateFlow()

    init {
        loadAcademyData()
    }

    private fun loadAcademyData() {
        val lessons = academyRepository.getLessons()
        viewModelScope.launch(Dispatchers.IO) {
            trainingProgressDao.getAllProgress().collect { progressList ->
                val completedIds = progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
                _uiState.update {
                    it.copy(
                        lessons = lessons,
                        completedLessonIds = completedIds
                    )
                }
            }
        }
    }

    fun selectLesson(lesson: AcademyLesson) {
        val firstStep = lesson.steps.firstOrNull() ?: return
        val pos = BoardPosition.fromFen(firstStep.fen) ?: BoardPosition.startingPosition()
        
        _uiState.update {
            it.copy(
                selectedLesson = lesson,
                currentStepIndex = 0,
                boardPosition = pos,
                boardState = BoardState(pieces = pos.squares.mapKeys { entry -> entry.key.index }),
                feedbackMessage = null,
                isStepCompleted = false,
                isLessonFinished = false
            )
        }
    }

    fun exitLesson() {
        _uiState.update { it.copy(selectedLesson = null, feedbackMessage = null) }
    }

    fun onSquareSelected(square: Square) {
        val state = _uiState.value
        val lesson = state.selectedLesson ?: return
        if (state.isStepCompleted || state.isLessonFinished) return

        val currentPos = state.boardPosition
        val currentBoardState = state.boardState

        if (currentBoardState.selectedSquare == null) {
            val piece = currentPos.pieceAt(square)
            if (piece != null && piece.color == currentPos.activeColor) {
                val legalMoves = ChessRules.getLegalMoves(currentPos, square)
                _uiState.update {
                    it.copy(
                        boardState = it.boardState.copy(
                            selectedSquare = square,
                            legalMoves = legalMoves.map { m -> m.to }
                        )
                    )
                }
            }
        } else {
            val selected = currentBoardState.selectedSquare
            if (selected == square) {
                clearSelection()
                return
            }

            val legalMoves = ChessRules.getLegalMoves(currentPos, selected)
            val intendedMove = legalMoves.find { it.to == square }

            if (intendedMove != null) {
                val step = lesson.steps[state.currentStepIndex]
                val fromAlg = intendedMove.from.toAlgebraic()
                val toAlg = intendedMove.to.toAlgebraic()

                if (fromAlg == step.expectedMoveFrom && toAlg == step.expectedMoveTo) {
                    // Richtiger Zug!
                    val nextPos = ChessRules.applyMove(currentPos, intendedMove)
                    val piecesMap = nextPos.squares.mapKeys { it.key.index }

                    val isLastStep = state.currentStepIndex >= lesson.steps.size - 1
                    _uiState.update {
                        it.copy(
                            boardPosition = nextPos,
                            boardState = BoardState(pieces = piecesMap),
                            feedbackMessage = step.explanationOnSuccess,
                            isStepCompleted = true,
                            isLessonFinished = isLastStep
                        )
                    }

                    if (isLastStep) {
                        saveProgress(lesson.id)
                    }
                } else {
                    // Falscher Zug für diese Lektion
                    _uiState.update {
                        it.copy(
                            feedbackMessage = "Nicht ganz richtig. Versuche es noch einmal!",
                            boardState = it.boardState.copy(selectedSquare = null, legalMoves = emptyList())
                        )
                    }
                }
            } else {
                clearSelection()
            }
        }
    }

    private fun clearSelection() {
        _uiState.update {
            it.copy(
                boardState = it.boardState.copy(selectedSquare = null, legalMoves = emptyList())
            )
        }
    }

    fun nextStep() {
        val state = _uiState.value
        val lesson = state.selectedLesson ?: return
        val nextIndex = state.currentStepIndex + 1

        if (nextIndex < lesson.steps.size) {
            val step = lesson.steps[nextIndex]
            val pos = BoardPosition.fromFen(step.fen) ?: state.boardPosition
            _uiState.update {
                it.copy(
                    currentStepIndex = nextIndex,
                    boardPosition = pos,
                    boardState = BoardState(pieces = pos.squares.mapKeys { entry -> entry.key.index }),
                    feedbackMessage = null,
                    isStepCompleted = false
                )
            }
        }
    }

    private fun saveProgress(lessonId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = TrainingProgressEntity(
                lessonId = lessonId,
                isCompleted = true,
                score = 100
            )
            trainingProgressDao.insertProgress(progress)
        }
    }
}
