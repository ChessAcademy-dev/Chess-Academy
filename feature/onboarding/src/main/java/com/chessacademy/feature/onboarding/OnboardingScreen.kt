package com.chessacademy.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chessacademy.core.designsystem.components.OneUiScaffold

/**
 * Moderner Onboarding-Bildschirm für CHESS ACADEMY.
 * Führt den Nutzer schrittweise durch Sprache, Profil, Niveau und Lernziele.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    OneUiScaffold(
        topBar = {
            // Einfache Header-Anzeige für Schritte
            LinearProgressIndicator(
                progress = { (state.currentStep + 1) / 4f },
                modifier = Modifier.fillMaxWidth(),
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Inhaltsbereich je nach Schritt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (state.currentStep) {
                        0 -> WelcomeStep(
                            selectedLanguage = state.selectedLanguage,
                            onLanguageSelected = { viewModel.setLanguage(it) }
                        )
                        1 -> ProfileStep(
                            playerName = state.playerName,
                            onNameChanged = { viewModel.setPlayerName(it) }
                        )
                        2 -> SkillLevelStep(
                            selectedElo = state.selectedSkillLevel,
                            onEloSelected = { viewModel.setSkillLevel(it) }
                        )
                        3 -> GoalsStep(
                            selectedGoals = state.selectedGoals,
                            onGoalToggled = { viewModel.toggleGoal(it) }
                        )
                    }
                }

                // Navigationsbuttons (Zurück / Weiter / Starten)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (state.currentStep > 0) {
                        OutlinedButton(onClick = { viewModel.previousStep() }) {
                            Text("Zurück")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (state.currentStep < 3) {
                        Button(onClick = { viewModel.nextStep() }) {
                            Text("Weiter")
                        }
                    } else {
                        Button(onClick = { viewModel.completeOnboarding(onFinished) }) {
                            Text("Loslegen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(selectedLanguage: String, onLanguageSelected: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Willkommen bei Chess Academy",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Deine umfassende Schach-Plattform für Training, Analyse und Spiel.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Sprache wählen / Select Language", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onLanguageSelected("de") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguage == "de") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Deutsch")
            }
            Button(
                onClick = { onLanguageSelected("en") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("English")
            }
        }
    }
}

@Composable
fun ProfileStep(playerName: String, onNameChanged: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Dein Spielerprofil",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Deine Daten werden zu 100% lokal auf deinem Gerät gespeichert (Offline-First).",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChanged,
            label = { Text("Spielername") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SkillLevelStep(selectedElo: Int, onEloSelected: (Int) -> Unit) {
    val levels = listOf(
        Pair(800, "Anfänger (Einsteiger)"),
        Pair(1200, "Fortgeschritten (Vereinsspieler)"),
        Pair(1600, "Erfahren (Starker Spieler)"),
        Pair(2000, "Experte / Meister")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Spielstärke (ca. Elo)",
            style = MaterialTheme.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        for ((elo, label) in levels) {
            ElevatedButton(
                onClick = { onEloSelected(elo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (selectedElo == elo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Text(text = "$label ($elo Elo)")
            }
        }
    }
}

@Composable
fun GoalsStep(selectedGoals: Set<String>, onGoalToggled: (String) -> Unit) {
    val goals = listOf(
        "Taktik & Mattaufgaben",
        "Eröffnungen verbessern",
        "Partien analysieren",
        "Endspiel-Training"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Deine Lernziele",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        for (goal in goals) {
            val isSelected = selectedGoals.contains(goal)
            ElevatedButton(
                onClick = { onGoalToggled(goal) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Text(text = goal)
            }
        }
    }
}
