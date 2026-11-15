package com.chessacademy.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Responsiver Scaffold im Samsung-One-UI-Stil, optimiert für Smartphones, Tablets und Foldables.
 * Bietet ergonomische Abstände, Einhandbedienungsorientierung und nahtlose Material-3-Integration
 * ohne proprietäre Hersteller-SDK-Abhängigkeiten.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (modifier: Modifier) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}
