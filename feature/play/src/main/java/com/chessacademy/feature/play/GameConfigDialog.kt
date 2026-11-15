package com.chessacademy.feature.play

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chessacademy.chess.rules.Color

@Composable
fun GameConfigDialog(
    onStartGame: (Color, EngineDifficulty) -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.WHITE) }
    var selectedDifficulty by remember { mutableStateOf(EngineDifficulty.MEDIUM) }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Partie konfigurieren", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Spielerfarbe wählen
                Text(text = "Deine Farbe", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedColor == Color.WHITE,
                        onClick = { selectedColor = Color.WHITE },
                        label = { Text("Weiß") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedColor == Color.BLACK,
                        onClick = { selectedColor = Color.BLACK },
                        label = { Text("Schwarz") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Schwierigkeit wählen
                Text(text = "KI-Schwierigkeit", style = MaterialTheme.typography.titleSmall)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EngineDifficulty.values().forEach { diff ->
                        val label = when (diff) {
                            EngineDifficulty.EASY -> "Leicht (~800 Elo)"
                            EngineDifficulty.MEDIUM -> "Mittel (~1500 Elo)"
                            EngineDifficulty.HARD -> "Schwer (~2200 Elo)"
                        }
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartGame(selectedColor, selectedDifficulty) }
            ) {
                Text("Spiel starten")
            }
        }
    )
}
