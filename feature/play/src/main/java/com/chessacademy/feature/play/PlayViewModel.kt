package com.chessacademy.feature.play

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessacademy.chess.board.BoardState
import com.chessacademy.chess.engine.UciEngine
import com.chessacademy.chess.rules.BoardPosition
import com.chessacademy.chess.rules.ChessRules
import com.chessacademy.chess.rules.Color
import com.chessacademy.chess.rules.GameStateStatus
import com.chessacademy.chess.rules.Move
import com.chessacademy.chess.rules.Square
import com.chessacademy.core.database.dao.GameDao
import com.chessacademy.core.database.entity.GameEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class PlayUiState(
    val boardPosition: BoardPosition = BoardPosition.startingPosition(),
    val boardState: BoardState = BoardState(),
    val gameStatus: GameStateStatus = GameStateStatus.ACTIVE,
    val isEngineThinking: Boolean = false,
    val playerColor: Color = Color.WHITE,
    val engineDifficulty: EngineDifficulty = EngineDifficulty.MEDIUM,
    val evaluationCp: Int? = null,
    val moveHistory: List<Move> = emptyList(),
    val isGameOver: Boolean = false,
    val resultMessage: String? = null,
    val isConfiguring: Boolean = true // Zeigt Konfigurationsdialog vor Spielbeginn
)

enum class EngineDifficulty(val elo: Int, val movetimeMs: Long, val depth: Int) {
    EASY(800, 500, 4),
    MEDIUM(1500, 1000, 8),
    HARD(2200, 2000, 14)
}

@HiltViewModel
class PlayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val uciEngine: UciEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayUiState())
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uciEngine.prepareEngineBinary(context)
                uciEngine.startEngine()
                uciEngine.isReady()
            } catch (e: Exception) {
                // Engine-Fehler protokollieren
            }
        }
    }

    fun startNewGame(playerColor: Color, difficulty: EngineDifficulty) {
        val startPos = BoardPosition.startingPosition()
        _uiState.update {
            it.copy(
                playerColor = playerColor,
                engineDifficulty = difficulty,
                isConfiguring = false,
                boardPosition = startPos,
                gameStatus = GameStateStatus.ACTIVE,
                moveHistory = emptyList(),
                isGameOver = false,
                resultMessage = null,
                isEngineThinking = false,
                evaluationCp = null
            )
        }
        updateBoardState(startPos)

        // Wenn der Computer Weiß hat, beginnt Stockfish direkt
        if (playerColor == Color.BLACK) {
            triggerEngineMove(startPos, emptyList())
        }
    }

    fun onSquareSelected(square: Square) {
        val state = _uiState.value
        if (state.isGameOver || state.isEngineThinking || state.isConfiguring) return

        val currentPos = state.boardPosition
        // Nur ziehen, wenn der Spieler am Zug ist
        if (currentPos.activeColor != state.playerColor) return

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
                executeMove(intendedMove)
            } else {
                val piece = currentPos.pieceAt(square)
                if (piece != null && piece.color == currentPos.activeColor) {
                    val newLegalMoves = ChessRules.getLegalMoves(currentPos, square)
                    _uiState.update {
                        it.copy(
                            boardState = it.boardState.copy(
                                selectedSquare = square,
                                legalMoves = newLegalMoves.map { m -> m.to }
                            )
                        )
                    }
                } else {
                    clearSelection()
                }
            }
        }
    }

    private fun executeMove(move: Move) {
        val currentPos = _uiState.value.boardPosition
        val nextPos = ChessRules.applyMove(currentPos, move)
        val history = _uiState.value.moveHistory + move
        val status = ChessRules.getGameStatus(nextPos, emptyList())

        updateBoardState(nextPos, status, history)

        if (status == GameStateStatus.ACTIVE || status == GameStateStatus.CHECK) {
            if (nextPos.activeColor != _uiState.value.playerColor) {
                triggerEngineMove(nextPos, history)
            }
        } else {
            handleGameOver(status)
        }
    }

    private fun triggerEngineMove(position: BoardPosition, history: List<Move>) {
        _uiState.update { it.copy(isEngineThinking = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fen = position.toFen()
                val difficulty = _uiState.value.engineDifficulty
                val bestMoveResult = uciEngine.analyzePosition(
                    fen = fen,
                    movetimeMs = difficulty.movetimeMs
                )

                if (bestMoveResult.bestMove != null) {
                    val fromStr = bestMoveResult.bestMove!!.take(2)
                    val toStr = bestMoveResult.bestMove!!.takeLast(2)
                    val from = Square.fromAlgebraic(fromStr)
                    val to = Square.fromAlgebraic(toStr)

                    if (from != null && to != null) {
                        val legalMoves = ChessRules.getLegalMoves(position, from)
                        val engineMove = legalMoves.find { it.to == to }

                        if (engineMove != null) {
                            viewModelScope.launch(Dispatchers.Main) {
                                val nextPos = ChessRules.applyMove(position, engineMove)
                                val newHistory = history + engineMove
                                val status = ChessRules.getGameStatus(nextPos, emptyList())

                                _uiState.update { 
                                    it.copy(
                                        isEngineThinking = false, 
                                        evaluationCp = bestMoveResult.scoreCp 
                                    ) 
                                }
                                updateBoardState(nextPos, status, newHistory)

                                if (status != GameStateStatus.ACTIVE && status != GameStateStatus.CHECK) {
                                    handleGameOver(status)
                                }
                            }
                            return@launch
                        }
                    }
                }
                // Fallback bei ungültigem Engine-Zug
                handleEngineError("Stockfish konnte keinen gültigen Zug berechnen.")
            } catch (e: Exception) {
                handleEngineError("Engine-Fehler: ${e.localizedMessage}")
            }
        }
    }

    private fun handleEngineError(errorMessage: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.update {
                it.copy(
                    isEngineThinking = false,
                    isGameOver = true,
                    resultMessage = errorMessage
                )
            }
        }
    }

    fun resignGame() {
        val playerColor = _uiState.value.playerColor
        val winner = if (playerColor == Color.WHITE) "Schwarz (Computer)" else "Weiß (Computer)"
        _uiState.update {
            it.copy(
                isGameOver = true,
                resultMessage = "Aufgabe! $winner gewinnt."
            )
        }
        saveFinishedGame(GameStateStatus.CHECKMATE)
    }

    private fun updateBoardState(
        position: BoardPosition,
        status: GameStateStatus = GameStateStatus.ACTIVE,
        history: List<Move> = emptyList()
    ) {
        val piecesMap = position.squares.mapKeys { it.key.index }
        _uiState.update {
            it.copy(
                boardPosition = position,
                boardState = BoardState(
                    pieces = piecesMap,
                    selectedSquare = null,
                    legalMoves = emptyList()
                ),
                gameStatus = status,
                moveHistory = history
            )
        }
    }

    private fun clearSelection() {
        _uiState.update {
            it.copy(
                boardState = it.boardState.copy(
                    selectedSquare = null,
                    legalMoves = emptyList()
                )
            )
        }
    }

    private fun handleGameOver(status: GameStateStatus) {
        val message = when (status) {
            GameStateStatus.CHECKMATE -> "Schachmatt! Partie beendet."
            GameStateStatus.STALEMATE -> "Patt! Unentschieden."
            GameStateStatus.DRAW_BY_MATERIAL -> "Remis durch ungenügendes Material."
            GameStateStatus.DRAW_BY_REPETITION -> "Remis durch Stellungswiederholung."
            else -> "Partie beendet."
        }

        _uiState.update {
            it.copy(
                isGameOver = true,
                resultMessage = message
            )
        }

        saveFinishedGame(status)
    }

    private fun saveFinishedGame(status: GameStateStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultStr = when (status) {
                GameStateStatus.CHECKMATE -> if (_uiState.value.boardPosition.activeColor != _uiState.value.playerColor) "1-0" else "0-1"
                else -> "1/2-1/2"
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val gameEntity = GameEntity(
                id = UUID.randomUUID().toString(),
                whitePlayerName = if (_uiState.value.playerColor == Color.WHITE) "Spieler" else "Stockfish",
                blackPlayerName = if (_uiState.value.playerColor == Color.BLACK) "Spieler" else "Stockfish",
                result = resultStr,
                date = dateFormat.format(Date())
            )
            gameDao.insertGame(gameEntity)
        }
    }

    fun showConfigDialog() {
        _uiState.update { it.copy(isConfiguring = true) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uciEngine.quit()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
