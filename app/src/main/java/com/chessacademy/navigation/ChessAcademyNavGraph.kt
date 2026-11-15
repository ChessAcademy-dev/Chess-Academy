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
import com.chessacademy.feature.home.HomeScreen
import com.chessacademy.feature.onboarding.OnboardingScreen
import com.chessacademy.feature.onboarding.OnboardingViewModel

/**
 * Definition sämtlicher Navigationsrouten in CHESS ACADEMY.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Play : Screen("play")
    object Academy : Screen("academy")
    object Tactics : Screen("tactics")
    object Analysis : Screen("analysis")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}

/**
 * Zentraler Navigationsgraph der Anwendung.
 */
@Composable
fun ChessAcademyNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    preferencesRepository: PreferencesRepository
) {
    val userPrefsState by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val prefs = userPrefsState

        if (prefs == null) {
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
                // Onboarding
                composable(route = Screen.Onboarding.route) {
                    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Home
                composable(route = Screen.Home.route) {
                    HomeScreen(
                        onNavigateToPlay = { navController.navigate(Screen.Play.route) },
                        onNavigateToAcademy = { navController.navigate(Screen.Academy.route) },
                        onNavigateToTactics = { navController.navigate(Screen.Tactics.route) },
                        onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                // Platzhalter-Routen für kommende Feature-Module
                composable(route = Screen.Play.route) { FeaturePlaceholder("Spielmodus gegen Computer") }
                composable(route = Screen.Academy.route) { FeaturePlaceholder("Academy Lektionen") }
                composable(route = Screen.Tactics.route) { FeaturePlaceholder("Taktiktraining") }
                composable(route = Screen.Analysis.route) { FeaturePlaceholder("Partie-Analyse & KI-Coach") }
                composable(route = Screen.Profile.route) { FeaturePlaceholder("Profil & Statistik") }
                composable(route = Screen.Settings.route) { FeaturePlaceholder("Einstellungen") }
            }
        }
    }
}

@Composable
fun FeaturePlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title – In Entwicklung",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
