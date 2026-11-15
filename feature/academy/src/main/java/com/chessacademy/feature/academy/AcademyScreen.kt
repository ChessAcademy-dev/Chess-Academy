package com.chessacademy.feature.academy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun AcademyScreen(
    viewModel: AcademyViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.value.let { it to viewModel.uiState.collectAsState().value }.second

    if (state.selectedLesson == null) {
        AcademyOverviewScreen(
            lessons = state.lessons,
            completedIds = state.completedLessonIds,
            onLessonClick = { lesson -> viewModel.selectLesson(lesson) }
        )
    } else {
        AcademyLessonScreen(
            state = state,
            onSquareClick = { index -> viewModel.onSquareSelected(Square.fromIndex(index)) },
            onNextStep = { viewModel.nextStep() },
            onBack = { viewModel.exitLesson() }
        )
    }
}

@Composable
fun AcademyOverviewScreen(
    lessons: List<AcademyLesson>,
    completedIds: Set<String>,
    onLessonClick: (AcademyLesson) -> Unit
) {
    OneUiScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chess Academy Lektionen", fontWeight = FontWeight.Bold) }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Wähle eine Lektion",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                lessons.forEach { lesson ->
                    val isCompleted = completedIds.contains(lesson.id)
                    Card(
                        onClick = { onLessonClick(lesson) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lesson.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Abgeschlossen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademyLessonScreen(
    state: AcademyUiState,
    onSquareClick: (Int) -> Unit,
    onNextStep: () -> Unit,
    onBack: () -> Unit
) {
    val lesson = state.selectedLesson!!
    val step = lesson.steps[state.currentStepIndex]

    OneUiScaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
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
                // Aufgabenstellung
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Schritt ${state.currentStepIndex + 1} von ${lesson.steps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.instruction,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
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
                        onSquareClick = onSquareClick
                    )
                }

                // Feedback & Weiter-Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.feedbackMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.isStepCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.feedbackMessage!!,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (state.isStepCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    if (state.isStepCompleted && !state.isLessonFinished) {
                        Button(
                            onClick = onNextStep,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Nächster Schritt")
                        }
                    } else if (state.isLessonFinished) {
                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lektion abschließen")
                        }
                    }
                }
            }
        }
    }
}
