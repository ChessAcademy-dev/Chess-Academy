package com.chessacademy.feature.play

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chessacademy.chess.board.ChessBoard
import com.chessacademy.chess.rules.Color
import com.chessacademy.chess.rules.GameStateStatus
import com.chessacademy.chess.rules.Square
import com.chessacademy.core.designsystem.components.OneUiScaffold

@Composable
fun PlayScreen(
    viewModel: PlayViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isConfiguring) {
        GameConfigDialog(
            onStartGame = { color, difficulty ->
                viewModel.startNewGame(color, difficulty)
            }
        )
    }

    OneUiScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partie gegen Computer", fontWeight = FontWeight.Bold) },
                actions = {
                    if (!state.isGameOver) {
                        IconButton(onClick = { viewModel.resignGame() }) {
                            Icon(Icons.Default.Flag, contentDescription = "Aufgeben")
                        }
                    }
                    IconButton(onClick = { viewModel.showConfigDialog() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Neues Spiel")
                    }
                }
            )
        }
    ) { innerModifier ->
        Surface(
            modifier = innerModifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Gegner-Info Card (Schwarz)
                val opponentName = if (state.playerColor == Color.WHITE) "Stockfish (${state.engineDifficulty.name})" else "Du (Schwarz)"
                val opponentElo = if (state.playerColor == Color.WHITE) state.engineDifficulty.elo else 1200
                PlayerHeaderCard(
                    playerName = opponentName,
                    rating = opponentElo,
                    isThinking = state.isEngineThinking || (state.boardPosition.activeColor != state.playerColor && !state.isGameOver),
                    evaluation = state.evaluationCp
                )

                // Schachbrett (Gesperrt während KI denkt)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChessBoard(
                        boardState = state.boardState,
                        onSquareClick = { index ->
                            if (!state.isEngineThinking && state.boardPosition.activeColor == state.playerColor) {
                                val sq = Square.fromIndex(index)
                                viewModel.onSquareSelected(sq)
                            }
                        }
                    )
                }

                // Spieler (Weiß) & Zugliste
                val playerName = if (state.playerColor == Color.WHITE) "Du (Weiß)" else "Stockfish (${state.engineDifficulty.name})"
                val playerElo = if (state.playerColor == Color.WHITE) 1200 else state.engineDifficulty.elo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerHeaderCard(
                        playerName = playerName,
                        rating = playerElo,
                        isThinking = state.boardPosition.activeColor == state.playerColor && !state.isGameOver && state.playerColor != Color.WHITE,
                        evaluation = null
                    )

                    MoveHistoryView(moves = state.moveHistory)
                }

                // Status & Spielende
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isGameOver) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.resultMessage ?: "Spiel beendet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.showConfigDialog() }) {
                                    Text("Neues Spiel starten")
                                }
                            }
                        }
                    } else {
                        val statusText = when (state.gameStatus) {
                            GameStateStatus.CHECK -> "Schach!"
                            else -> if (state.boardPosition.activeColor == state.playerColor) "Du bist am Zug" else "Stockfish berechnet Zug..."
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.gameStatus == GameStateStatus.CHECK) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerHeaderCard(
    playerName: String,
    rating: Int,
    isThinking: Boolean,
    evaluation: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subText = if (evaluation != null) {
                    val evalVal = evaluation / 100.0
                    "Elo: $rating | Eval: ${if (evalVal > 0) "+$evalVal" else "$evalVal"}"
                } else {
                    "Elo: $rating"
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isThinking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
