package com.chessacademy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chessacademy.core.datastore.PreferencesRepository
import com.chessacademy.feature.home.HomeScreen
import com.chessacademy.feature.home.HomeViewModel
import com.chessacademy.feature.onboarding.OnboardingScreen
import com.chessacademy.feature.onboarding.OnboardingViewModel
import com.chessacademy.feature.play.PlayScreen
import com.chessacademy.feature.play.PlayViewModel

sealed class Screen(val route: String, val label: String) {
    object Onboarding : Screen("onboarding", "Onboarding")
    object Home : Screen("home", "Home")
    object Play : Screen("play", "Spielen")
    object Academy : Screen("academy", "Academy")
    object Tactics : Screen("tactics", "Training")
    object Analysis : Screen("analysis", "Analyse")
    object Profile : Screen("profile", "Profil")
    object Settings : Screen("settings", "Einstellungen")
}

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

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val bottomNavItems = listOf(
                Triple(Screen.Home, Icons.Default.Home, "Home"),
                Triple(Screen.Play, Icons.Default.SportsEsports, "Spielen"),
                Triple(Screen.Academy, Icons.Default.School, "Academy"),
                Triple(Screen.Tactics, Icons.Default.Psychology, "Training"),
                Triple(Screen.Analysis, Icons.Default.Analytics, "Analyse")
            )

            Scaffold(
                bottomBar = {
                    if (currentRoute != null && currentRoute != Screen.Onboarding.route) {
                        NavigationBar {
                            bottomNavItems.forEach { (screen, icon, label) ->
                                NavigationBarItem(
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding)
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
                        val homeViewModel: HomeViewModel = hiltViewModel()
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToPlay = { navController.navigate(Screen.Play.route) },
                            onNavigateToAcademy = { navController.navigate(Screen.Academy.route) },
                            onNavigateToTactics = { navController.navigate(Screen.Tactics.route) },
                            onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                        )
                    }

                    // Echter Spielmodus gegen Computer (:feature:play)
                    composable(route = Screen.Play.route) {
                        val playViewModel: PlayViewModel = hiltViewModel()
                        PlayScreen(
                            viewModel = playViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    // Academy-Modul (:feature:academy)
                    composable(route = Screen.Academy.route) {
                        val academyViewModel: AcademyViewModel = hiltViewModel()
                        AcademyScreen(
                               viewModel = academyViewModel,
                               onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Noch offene Feature-Module als Platzhalter behalten
                    composable(route = Screen.Academy.route) { FeaturePlaceholder("Academy Lektionen") }
                    composable(route = Screen.Tactics.route) { FeaturePlaceholder("Taktiktraining & Puzzles") }
                    composable(route = Screen.Analysis.route) { FeaturePlaceholder("Partie-Analyse & KI-Coach") }
                    composable(route = Screen.Profile.route) { FeaturePlaceholder("Profil & Statistiken") }
                    composable(route = Screen.Settings.route) { FeaturePlaceholder("App-Einstellungen") }
                }
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
            text = "$title\n(Modul in Entwicklung)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
