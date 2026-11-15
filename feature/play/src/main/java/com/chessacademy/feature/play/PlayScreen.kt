package com.chessacademy.feature.play

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.chessacademy.chess.rules.Square
import com.chessacademy.core.designsystem.components.OneUiScaffold

/**
 * Spielbildschirm für den Modus "Gegen Computer" (Stockfish).
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
                    IconButton(onClick = { viewModel.restartGame() }) {
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
                // Gegner-Info Card (Stockfish)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
                                text = "Gegner: Stockfish (KI)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val evalText = if (state.evaluationCp != null) {
                                val eval = state.evaluationCp!! / 100.0
                                "Bewertung: ${if (eval > 0) "+$eval" else "$eval"}"
                            } else {
                                "Bereitschaft: Aktiv"
                            }
                            Text(
                                text = evalText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.isEngineThinking) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }

                // Schachbrett
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

                // Status & Spielende-Dialog/Info
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
                        val turnText = if (state.boardPosition.activeColor == com.chessacademy.chess.rules.Color.WHITE) {
                            "Du bist am Zug (Weiß)"
                        } else {
                            "Stockfish denkt nach..."
                        }
                        Text(
                            text = turnText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
