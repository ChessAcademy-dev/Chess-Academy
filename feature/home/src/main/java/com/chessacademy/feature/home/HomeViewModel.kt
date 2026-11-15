package com.chessacademy.feature.home

import com.chessacademy.core.database.dao.GameDao
import com.chessacademy.core.database.dao.ProfileDao
import com.chessacademy.core.database.entity.GameEntity
import com.chessacademy.core.database.entity.ProfileEntity
import com.chessacademy.core.datastore.PreferencesRepository
import com.chessacademy.core.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var profileDao: ProfileDao
    private lateinit var gameDao: GameDao
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        profileDao = mock(ProfileDao::class.java)
        gameDao = mock(GameDao::class.java)
        preferencesRepository = mock(PreferencesRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDataSuccessfully() = runTest {
        // Arrange
        val profile = ProfileEntity(id = 1, name = "TestPlayer", eloTactics = 1400)
        val games = listOf(
            GameEntity(
                id = "game_1",
                whitePlayerName = "TestPlayer",
                blackPlayerName = "Stockfish",
                result = "1-0",
                date = "2026-11-15"
            )
        )
        val prefs = UserPreferences(onboardingCompleted = true, language = "de", learningGoals = setOf("Endgame"))

        `when`(profileDao.getAllProfiles()).thenReturn(flowOf(listOf(profile)))
        `when`(gameDao.getAllGames()).thenReturn(flowOf(games))
        `when`(preferencesRepository.userPreferencesFlow).thenReturn(flowOf(prefs))

        // Act
        val viewModel = HomeViewModel(profileDao, gameDao, preferencesRepository)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("TestPlayer", state.profile?.name)
        assertEquals(1400, state.profile?.eloTactics)
        assertEquals(1, state.recentGames.size)
        assertEquals("de", state.language)
        assertTrue(state.learningGoals.contains("Endgame"))
        assertNull(state.errorMessage)
    }

    @Test
    fun errorHandling_capturesExceptionGracefully() = runTest {
        // Arrange
        `when`(profileDao.getAllProfiles()).thenThrow(RuntimeException("Database connection failed"))
        `when`(gameDao.getAllGames()).thenReturn(flowOf(emptyList()))
        `when`(preferencesRepository.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))

        // Act
        val viewModel = HomeViewModel(profileDao, gameDao, preferencesRepository)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage?.contains("Database connection failed") == true)
    }
}
