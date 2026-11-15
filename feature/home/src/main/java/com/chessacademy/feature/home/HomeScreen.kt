package com.chessacademy.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chessacademy.core.designsystem.components.OneUiScaffold

/**
 * Hauptbildschirm (Home) der CHESS ACADEMY im One-UI-Design.
 * Bietet übersichtliche Einstiegspunkte in alle Trainings- und Spielbereiche.
 */
@Composable
fun HomeScreen(
    onNavigateToPlay: () -> Unit,
    onNavigateToAcademy: () -> Unit,
    onNavigateToTactics: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    OneUiScaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Chess Academy", fontWeight = FontWeight.Bold) }
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Willkommens- / Schnellstart-Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) Bereit@{
                        Text(
                            text = "Bereit für deine nächste Partie?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Starte ein Spiel gegen die Engine oder trainiere deine Taktikfähigkeiten.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToPlay,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Gegen Computer spielen")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Training & Analyse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Feature-Karten Grid / Liste
                HomeFeatureCard(
                    title = "Academy Lektionen",
                    description = "Eröffnungen, Strategie und Endspiele strukturiert lernen",
                    onClick = onNavigateToAcademy
                )

                HomeFeatureCard(
                    title = "Taktiktraining (Puzzles)",
                    description = "Löse Mattaufgaben und verbessere dein taktisches Sehen",
                    onClick = onNavigateToTactics
                )

                HomeFeatureCard(
                    title = "Partie-Analyse & KI-Coach",
                    description = "Analysiere eigene PGN-Partien mit Stockfish",
                    onClick = onNavigateToAnalysis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Konto & Anpassung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Profil & Statistik")
                    }
                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Einstellungen")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeFeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
