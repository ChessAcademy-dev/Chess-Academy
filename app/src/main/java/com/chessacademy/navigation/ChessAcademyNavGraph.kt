package com.chessacademy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chessacademy.core.datastore.PreferencesRepository
import com.chessacademy.feature.onboarding.OnboardingScreen
import com.chessacademy.feature.onboarding.OnboardingViewModel

/**
 * Definition der Navigationsrouten in CHESS ACADEMY.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
}

/**
 * Zentraler Navigationsgraph der Anwendung.
 * Steuert den Startbildschirm abhängig vom Onboarding-Status im DataStore
 * und verhindert doppelte Navigationsziele durch sauberes Back-Stack-Management.
 */
@Composable
fun ChessAcademyNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    preferencesRepository: PreferencesRepository
) {
    // Reaktiver Strom der Benutzereinstellungen zur Ermittlung des Onboarding-Status
    val userPrefsState by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val prefs = userPrefsState

        if (prefs == null) {
            // Ladezustand, während DataStore initialisiert wird (Splash / Loader)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val startDestination = if (prefs.onboardingCompleted) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                // Onboarding-Route
                composable(route = Screen.Onboarding.route) {
                    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onFinished = {
                            // Navigiere zu Home und entferne Onboarding aus dem Back-Stack (kein Zurückkehren möglich)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Home-Route (Hauptbildschirm nach erfolgreichem Onboarding)
                composable(route = Screen.Home.route) {
                    HomeScreenPlaceholder()
                }
            }
        }
    }
}

/**
 * Platzhalter für den Home-Bildschirm, bis das :feature:home-Modul integriert wird.
 */
@Composable
fun HomeScreenPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Willkommen zurück bei Chess Academy! (Home-Bildschirm)",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
