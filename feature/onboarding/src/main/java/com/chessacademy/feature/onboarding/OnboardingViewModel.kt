package com.chessacademy.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessacademy.core.database.dao.ProfileDao
import com.chessacademy.core.database.entity.ProfileEntity
import com.chessacademy.core.datastore.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val selectedLanguage: String = "de",
    val playerName: String = "Schachspieler",
    val selectedSkillLevel: Int = 1200, // Elo
    val selectedGoals: Set<String> = emptySet(),
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
        viewModelScope.launch {
            preferencesRepository.updateLanguage(language)
        }
    }

    fun setPlayerName(name: String) {
        _uiState.update { it.copy(playerName = name.ifBlank { "Schachspieler" }) }
    }

    fun setSkillLevel(elo: Int) {
        _uiState.update { it.copy(selectedSkillLevel = elo) }
    }

    fun toggleGoal(goal: String) {
        _uiState.update { state ->
            val newGoals = if (state.selectedGoals.contains(goal)) {
                state.selectedGoals - goal
            } else {
                state.selectedGoals + goal
            }
            state.copy(selectedGoals = newGoals)
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            // Lokales Profil in Room anlegen (Offline-First)
            val profile = ProfileEntity(
                name = state.playerName,
                isGuest = false,
                eloTactics = state.selectedSkillLevel,
                eloStrategy = state.selectedSkillLevel,
                eloOpenings = state.selectedSkillLevel,
                eloEndgame = state.selectedSkillLevel,
                eloCalculation = state.selectedSkillLevel,
                eloBlitz = state.selectedSkillLevel
            )
            profileDao.insertProfile(profile)

            _uiState.update { it.copy(isCompleted = true) }
            onFinished()
        }
    }
}
