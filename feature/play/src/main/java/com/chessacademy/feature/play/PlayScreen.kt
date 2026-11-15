package com.chessacademy.feature.play

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chessacademy.chess.board.ChessBoard
import com.chessacademy.chess.rules.GameStateStatus
import com.chessacademy.chess.rules.Square
import com.chessacademy.core.designsystem.components.OneUiScaffold

/**
 * Vollständiger Spielbildschirm für CHESS ACADEMY.
 * Bietet interaktives Schachbrett, Spieler-Metadaten, Zugliste,
 * Statusanzeige (Schach, Matt, Patt) sowie Reset- und Steuerungsoptionen.
 */
@Composable
fun PlayScreen(
    viewModel: PlayViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    OneUiScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partie gegen Computer", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { viewModel.restartGame() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Neues Spiel"
                        )
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
                // 1. Gegner- & Spielerinfo (Schwarz oben)
                PlayerHeaderCard(
                    playerName = "Stockfish (KI)",
                    rating = state.engineElo,
                    isThinking = state.isEngineThinking,
                    evaluation = state.evaluationCp
                )

                // 2. Interaktives Schachbrett
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChessBoard(
                        boardState = state.boardState,
                        onSquareClick = { index ->
                            val sq = Square.fromIndex(index)
                            viewModel.onSquareSelected(sq)
                        }
                    )
                }

                // 3. Eigener Spieler (Weiß unten) + Zugliste
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerHeaderCard(
                        playerName = "Du (Weiß)",
                        rating = 1200,
                        isThinking = state.boardPosition.activeColor == com.chessacademy.chess.rules.Color.WHITE && !state.isGameOver,
                        evaluation = null
                    )

                    // Zugverlauf
                    MoveHistoryView(moves = state.moveHistory)
                }

                // 4. Statusanzeige & Spielende-Dialog
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
                                Button(onClick = { viewModel.restartGame() }) {
                                    Text("Revanche starten")
                                }
                            }
                        }
                    } else {
                        val statusText = when (state.gameStatus) {
                            GameStateStatus.CHECK -> "Schach!"
                            GameStateStatus.CHECKMATE -> "Schachmatt!"
                            GameStateStatus.STALEMATE -> "Patt - Unentschieden"
                            else -> if (state.boardPosition.activeColor == com.chessacademy.chess.rules.Color.WHITE) "Du bist am Zug" else "Stockfish berechnet..."
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
