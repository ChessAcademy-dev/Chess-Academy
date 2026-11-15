package com.chessacademy.feature.play

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chessacademy.chess.rules.Color

@Composable
fun GameConfigDialog(
    onStartGame: (Color, EngineDifficulty, TimeControl) -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.WHITE) }
    var selectedDifficulty by remember { mutableStateOf(EngineDifficulty.MEDIUM) }
    var selectedTimeControl by remember { mutableStateOf(TimeControl.BLITZ_3_2) }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Partie konfigurieren", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Spielerfarbe
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

                // Schwierigkeit
                Text(text = "KI-Schwierigkeit", style = MaterialTheme.typography.titleSmall)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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

                // Bedenkzeit
                Text(text = "Bedenkzeit", style = MaterialTheme.typography.titleSmall)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeControl.values().forEach { tc ->
                        FilterChip(
                            selected = selectedTimeControl == tc,
                            onClick = { selectedTimeControl = tc },
                            label = { Text(tc.title) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartGame(selectedColor, selectedDifficulty, selectedTimeControl) }
            ) {
                Text("Spiel starten")
            }
        }
    )
}
