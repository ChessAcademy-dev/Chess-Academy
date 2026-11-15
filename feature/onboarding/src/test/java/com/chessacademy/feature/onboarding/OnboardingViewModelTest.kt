package com.chessacademy.feature.onboarding

import com.chessacademy.core.database.dao.ProfileDao
import com.chessacademy.core.database.entity.ProfileEntity
import com.chessacademy.core.datastore.PreferencesRepository
import com.chessacademy.core.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Fake-Implementierung von ProfileDao für isolierte Unit-Tests.
 */
class FakeProfileDao : ProfileDao {
    private val profiles = mutableListOf<ProfileEntity>()
    private val profilesFlow = MutableStateFlow<List<ProfileEntity>>(emptyList())

    override fun getAllProfiles(): Flow<List<ProfileEntity>> = profilesFlow

    override suspend fun getProfileById(profileId: Long): ProfileEntity? {
        return profiles.find { it.id == profileId }
    }

    override suspend fun insertProfile(profile: ProfileEntity): Long {
        val newId = (profiles.maxOfOrNull { it.id } ?: 0L) + 1L
        val newProfile = profile.copy(id = newId)
        profiles.add(newProfile)
        profilesFlow.value = profiles.toList()
        return newId
    }

    override suspend fun updateProfile(profile: ProfileEntity) {
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profile
            profilesFlow.value = profiles.toList()
        }
    }

    override suspend fun deleteProfile(profile: ProfileEntity) {
        profiles.removeIf { it.id == profile.id }
        profilesFlow.value = profiles.toList()
    }
}

/**
 * Fake-Implementierung von PreferencesRepository für isolierte Unit-Tests.
 */
class FakePreferencesRepository : PreferencesRepository(
    context = mockContext() // Hilfsfunktion für Testkontext
) {
    private val preferences = MutableStateFlow(UserPreferences())
    override val userPreferencesFlow: Flow<UserPreferences> = preferences

    var throwErrorOnUpdate: Boolean = false

    override suspend fun updateLanguage(language: String) {
        if (throwErrorOnUpdate) throw RuntimeException("Speicherfehler im DataStore")
        preferences.update { it.copy(language = language) }
    }

    override suspend fun updateLearningGoals(goals: Set<String>) {
        if (throwErrorOnUpdate) throw RuntimeException("Speicherfehler im DataStore")
        preferences.update { it.copy(learningGoals = goals) }
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        if (throwErrorOnUpdate) throw RuntimeException("Speicherfehler im DataStore")
        preferences.update { it.copy(onboardingCompleted = completed) }
    }
}

// Dummy-Kontext für Fake-Repository (wird von Room/DataStore in Fake-Tests nicht real ausgeführt)
private fun mockContext(): android.content.Context {
    return org.mockito.Mockito.mock(android.content.Context::class.java)
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeProfileDao: FakeProfileDao
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeProfileDao = FakeProfileDao()
        fakePreferencesRepository = FakePreferencesRepository()
        viewModel = OnboardingViewModel(fakePreferencesRepository, fakeProfileDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isCorrect() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(0, state.currentStep)
        assertEquals("de", state.selectedLanguage)
        assertEquals(1200, state.estimatedStartingElo)
        assertTrue(state.selectedGoals.isEmpty())
        assertFalse(state.isCompleted)
        assertFalse(state.isSaving)
        assertNull(state.errorMessage)
    }

    @Test
    fun languageSelection_updatesStateAndPreferences() = runTest {
        viewModel.setLanguage("en")
        advanceUntilIdle()

        assertEquals("en", viewModel.uiState.value.selectedLanguage)
        assertEquals("en", fakePreferencesRepository.userPreferencesFlow.first().language)
    }

    @Test
    fun playerName_updatesState() = runTest {
        viewModel.setPlayerName("Grandmaster Alex")
        assertEquals("Grandmaster Alex", viewModel.uiState.value.playerName)
    }

    @Test
    fun skillLevel_updatesState() = runTest {
        viewModel.setSelfAssessedSkillLevel(1600)
        assertEquals(1600, viewModel.uiState.value.estimatedStartingElo)
        assertTrue(viewModel.uiState.value.isSelfAssessment)
    }

    @Test
    fun toggleGoal_addsAndRemovesGoals() = runTest {
        val goal = "Taktik & Mattaufgaben"
        
        viewModel.toggleGoal(goal)
        assertTrue(viewModel.uiState.value.selectedGoals.contains(goal))

        viewModel.toggleGoal(goal)
        assertFalse(viewModel.uiState.value.selectedGoals.contains(goal))
    }

    @Test
    fun stepNavigation_movesForwardAndBackward() = runTest {
        assertEquals(0, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.currentStep)

        viewModel.previousStep()
        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    @Test
    fun completeOnboarding_createsProfileAndFinishes_whenNoProfileExists() = runTest {
        viewModel.setPlayerName("Magnus")
        viewModel.setSelfAssessedSkillLevel(2000)
        viewModel.toggleGoal("Partieanalyse")

        var finishedCalled = false
        viewModel.completeOnboarding {
            finishedCalled = true
        }
        advanceUntilIdle()

        assertTrue(finishedCalled)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertTrue(fakePreferencesRepository.userPreferencesFlow.first().onboardingCompleted)

        val profiles = fakeProfileDao.getAllProfiles().first()
        assertEquals(1, profiles.size)
        assertEquals("Magnus", profiles[0].name)
        assertEquals(2000, profiles[0].eloTactics)
    }

    @Test
    fun completeOnboarding_preventsDuplicateProfile_whenProfileAlreadyExists() = runTest {
        // Vorab ein Profil anlegen
        fakeProfileDao.insertProfile(ProfileEntity(name = "Existing User"))
        
        // ViewModel neu initialisieren, um den Zustand mit vorhandenem Profil zu laden
        viewModel = OnboardingViewModel(fakePreferencesRepository, fakeProfileDao)
        advanceUntilIdle()

        var finishedCalled = false
        viewModel.completeOnboarding {
            finishedCalled = true
        }
        advanceUntilIdle()

        assertTrue(finishedCalled)
        // Es darf kein zweites Profil erstellt
