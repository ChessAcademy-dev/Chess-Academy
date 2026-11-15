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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val timeControl: TimeControl = TimeControl.BLITZ_3_2,
    val whiteTimeMillis: Long = 3 * 60 * 1000L,
    val blackTimeMillis: Long = 3 * 60 * 1000L,
    val evaluationCp: Int? = null,
    val moveHistory: List<Move> = emptyList(),
    val isGameOver: Boolean = false,
    val resultMessage: String? = null,
    val isConfiguring: Boolean = true
)

@HiltViewModel
class PlayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val uciEngine: UciEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayUiState())
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val gameId: String = UUID.randomUUID().toString()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uciEngine.prepareEngineBinary(context)
                uciEngine.startEngine()
                uciEngine.isReady()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun startNewGame(playerColor: Color, difficulty: EngineDifficulty, timeControl: TimeControl) {
        timerJob?.cancel()
        val startPos = BoardPosition.startingPosition()
        
        _uiState.update {
            it.copy(
                playerColor = playerColor,
                engineDifficulty = difficulty,
                timeControl = timeControl,
                whiteTimeMillis = timeControl.initialMillis,
                blackTimeMillis = timeControl.initialMillis,
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
        startClock()

        if (playerColor == Color.BLACK) {
            triggerEngineMove(startPos, emptyList())
        }
    }

    private fun startClock() {
        if (_uiState.value.timeControl == TimeControl.UNLIMITED) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_uiState.value.isGameOver) {
                delay(100L)
                if (_uiState.value.isConfiguring) continue

                val activeColor = _uiState.value.boardPosition.activeColor
                _uiState.update { state ->
                    if (activeColor == Color.WHITE) {
                        val newTime = state.whiteTimeMillis - 100L
                        if (newTime <= 0) {
                            handleTimeOut(Color.WHITE)
                        }
                        state.copy(whiteTimeMillis = newTime.coerceAtLeast(0L))
                    } else {
                        val newTime = state.blackTimeMillis - 100L
                        if (newTime <= 0) {
                            handleTimeOut(Color.BLACK)
                        }
                        state.copy(blackTimeMillis = newTime.coerceAtLeast(0L))
                    }
                }
            }
        }
    }

    private fun handleTimeOut(timedOutColor: Color) {
        timerJob?.cancel()
        val winner = if (timedOutColor == Color.WHITE) "Schwarz (Computer / Zeit)" else "Weiß (Zeit)"
        _uiState.update {
            it.copy(
                isGameOver = true,
                resultMessage = "Zeitablauf! $winner gewinnt."
            )
        }
        saveGameToDatabase(GameStateStatus.CHECKMATE)
    }

    fun onSquareSelected(square: Square) {
        val state = _uiState.value
        if (state.isGameOver || state.isEngineThinking || state.isConfiguring) return
        if (state.boardPosition.activeColor != state.playerColor) return

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
        // Inkrement auf Uhr addieren
        applyIncrement(currentPos.activeColor)

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

    private fun applyIncrement(color: Color) {
        val increment = _uiState.value.timeControl.incrementMillis
        if (increment <= 0) return
        _uiState.update {
            if (color == Color.WHITE) {
                it.copy(whiteTimeMillis = it.whiteTimeMillis + increment)
            } else {
                it.copy(blackTimeMillis = it.blackTimeMillis + increment)
            }
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
                                applyIncrement(position.activeColor)
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
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun resignGame() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isGameOver = true,
                resultMessage = "Aufgabe! Computer gewinnt."
            )
        }
        saveGameToDatabase(GameStateStatus.CHECKMATE)
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
        timerJob?.cancel()
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

        saveGameToDatabase(status)
    }

    private fun saveGameToDatabase(status: GameStateStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultStr = when (status) {
                GameStateStatus.CHECKMATE -> if (_uiState.value.boardPosition.activeColor != _uiState.value.playerColor) "1-0" else "0-1"
                else -> "1/2-1/2"
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            
            // PGN generieren
            val pgnBuilder = StringBuilder()
            pgnBuilder.append("[Event \"Chess Academy Match\"]\n")
            pgnBuilder.append("[Date \"${dateFormat.format(Date())}\"]\n")
            pgnBuilder.append("[White \"${if (_uiState.value.playerColor == Color.WHITE) "Spieler" else "Stockfish"}\"]\n")
            pgnBuilder.append("[Black \"${if (_uiState.value.playerColor == Color.BLACK) "Spieler" else "Stockfish"}\"]\n")
            pgnBuilder.append("[Result \"$resultStr\"]\n\n")
            
            _uiState.value.moveHistory.forEachIndexed { index, move ->
                if (index % 2 == 0) pgnBuilder.append("${(index / 2) + 1}. ")
                pgnBuilder.append("${move.from.toAlgebraic()}${move.to.toAlgebraic()} ")
            }
            pgnBuilder.append(resultStr)

            val gameEntity = GameEntity(
                id = gameId,
                whitePlayerName = if (_uiState.value.playerColor == Color.WHITE) "Spieler" else "Stockfish",
                blackPlayerName = if (_uiState.value.playerColor == Color.BLACK) "Spieler" else "Stockfish",
                result = resultStr,
                date = dateFormat.format(Date()),
                pgn = pgnBuilder.toString(),
                timeControl = _uiState.value.timeControl.title,
                isCompleted = _uiState.value.isGameOver,
                playerColor = _uiState.value.playerColor.name
            )
            gameDao.insertGame(gameEntity)
        }
    }

    fun showConfigDialog() {
        timerJob?.cancel()
        _uiState.update { it.getInitialConfigState() }
    }

    private fun PlayUiState.getInitialConfigState(): PlayUiState = this.copy(isConfiguring = true)

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uciEngine.quit()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
