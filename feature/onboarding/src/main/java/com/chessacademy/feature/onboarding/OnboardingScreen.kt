package com.chessacademy.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * Moderner, barrierefreier Onboarding-Bildschirm für CHESS ACADEMY.
 * Unterstützt Deutsch und Englisch, zeigt Lade- und Fehlerzustände an und 
 * kommuniziert transparent die Offline-First-Ausrichtung.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isEnglish = state.selectedLanguage == "en"

    OneUiScaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (state.currentStep + 1) / 4f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
                // Hauptinhalt mit Scroll-Unterstützung für kleine Bildschirme / Tablets
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (state.currentStep) {
                            0 -> WelcomeStep(
                                selectedLanguage = state.selectedLanguage,
                                onLanguageSelected = { viewModel.setLanguage(it) }
                            )
                            1 -> ProfileStep(
                                playerName = state.playerName,
                                onNameChanged = { viewModel.setPlayerName(it) },
                                isEnglish = isEnglish
                            )
                            2 -> SkillLevelStep(
                                selectedElo = state.estimatedStartingElo,
                                onEloSelected = { viewModel.setSelfAssessedSkillLevel(it) },
                                isEnglish = isEnglish
                            )
                            3 -> GoalsStep(
                                selectedGoals = state.selectedGoals,
                                onGoalToggled = { viewModel.toggleGoal(it) },
                                isEnglish = isEnglish
                            )
                        }

                        // Fehleranzeige, falls beim Speichern etwas schiefging
                        if (state.errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.errorMessage ?: "",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.clearError() }) {
                                        Text(if (isEnglish) "Dismiss" else "Ausblenden")
                                    }
                                }
                            }
                        }
                    }
                }

                // Navigations- und Aktionsbereich
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (state.isSaving) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
