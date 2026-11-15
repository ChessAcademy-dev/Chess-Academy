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
import javax.inject.Inject

data class PlayUiState(
    val boardPosition: BoardPosition = BoardPosition.startingPosition(),
    val boardState: BoardState = BoardState(),
    val gameStatus: GameStateStatus = GameStateStatus.ACTIVE,
    val isEngineThinking: Boolean = false,
    val engineElo: Int = 1200,
    val evaluationCp: Int? = null,
    val moveHistory: List<Move> = emptyList(),
    val isGameOver: Boolean = false,
    val resultMessage: String? = null
)

@HiltViewModel
class PlayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val uciEngine: UciEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayUiState())
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    init {
        initializeGame()
    }

    private fun initializeGame() {
        viewModelScope.launch(Dispatchers.IO) {
            uciEngine.prepareEngineBinary(context)
            uciEngine.startEngine()
            uciEngine.isReady()
        }
        updateBoardState(BoardPosition.startingPosition())
    }

    fun onSquareSelected(square: Square) {
        if (_uiState.value.isGameOver || _uiState.value.isEngineThinking) return

        val currentPos = _uiState.value.boardPosition
        val currentBoardState = _uiState.value.boardState

        if (currentBoardState.selectedSquare == null) {
            // Wähle Stück aus, wenn es dem aktiven Spieler gehört
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
                // Auswahl aufheben
                clearSelection()
                return
            }

            // Prüfe, ob ein legaler Zug zur Zielposition existiert
            val legalMoves = ChessRules.getLegalMoves(currentPos, selected)
            val intendedMove = legalMoves.find { it.to == square }

            if (intendedMove != null) {
                executeMove(intendedMove)
            } else {
                // Neues Stück anwählen falls eigenes
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
            // Wenn Schwarz am Zug ist und gegen KI gespielt wird, Engine triggern
            if (nextPos.activeColor == Color.BLACK) {
                triggerEngineMove(nextPos, history)
            }
        } else {
            handleGameOver(status)
        }
    }

    private fun triggerEngineMove(position: BoardPosition, history: List<Move>) {
        _uiState.update { it.copy(isEngineThinking = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val fen = position.toFen()
            val bestMoveResult = uciEngine.analyzePosition(
                fen = fen,
                movetimeMs = 1000 // 1 Sekunde Bedenkzeit für Engine
            )

            if (bestMoveResult.bestMove != null) {
                // Konvertiere UCI-String (z.B. "e7e5") in Move-Objekt
                val from = Square.fromAlgebraic(bestMoveResult.bestMove!!.take(2))
                val to = Square.fromAlgebraic(bestMoveResult.bestMove!!.takeLast(2))
                if (from != null && to != null) {
                    val legalMoves = ChessRules.getLegalMoves(position, from)
                    val engineMove = legalMoves.find { it.to == to }

                    if (engineMove != null) {
                        viewModelScope.launch(Dispatchers.Main) {
                            val nextPos = ChessRules.applyMove(position, engineMove)
                            val newHistory = history + engineMove
                            val status = ChessRules.getGameStatus(nextPos, emptyList())

                            _uiState.update { it.copy(isEngineThinking = false, evaluationCp = bestMoveResult.scoreCp) }
                            updateBoardState(nextPos, status, newHistory)

                            if (status != GameStateStatus.ACTIVE && status != GameStateStatus.CHECK) {
                                handleGameOver(status)
                            }
                        }
                        return@launch
                    }
                }
            }
            _uiState.update { it.copy(isEngineThinking = false) }
        }
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

        // Partie in Room-Datenbank speichern
        saveFinishedGame(status)
    }

    private fun saveFinishedGame(status: GameStateStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultStr = when (status) {
                GameStateStatus.CHECKMATE -> if (_uiState.value.boardPosition.activeColor == Color.WHITE) "0-1" else "1-0"
                else -> "1/2-1/2"
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val gameEntity = GameEntity(
                id = java.util.UUID.randomUUID().toString(),
                whitePlayerName = "Spieler",
                blackPlayerName = "Stockfish (Elo ${_uiState.value.engineElo})",
                result = resultStr,
                date = dateFormat.format(Date())
            )
            gameDao.insertGame(gameEntity)
        }
    }

    fun restartGame() {
        val startPos = BoardPosition.startingPosition()
        updateBoardState(startPos, GameStateStatus.ACTIVE, emptyList())
        _uiState.update {
            it.copy(
                isGameOver = false,
                resultMessage = null,
                isEngineThinking = false,
                evaluationCp = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            uciEngine.quit()
        }
    }
}
