package com.chessacademy.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessacademy.core.database.dao.ProfileDao
import com.chessacademy.core.database.entity.ProfileEntity
import com.chessacademy.core.datastore.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI-Zustand für das Onboarding.
 * Hinweis: [estimatedStartingElo] dient ausschließlich als vorläufige Selbsteinschätzung
 * für die initiale Schwierigkeitsanpassung im Training und stellt keine gewertete Turnier-Elo dar.
 */
data class OnboardingUiState(
    val currentStep: Int = 0,
    val selectedLanguage: String = "de",
    val playerName: String = "",
    val estimatedStartingElo: Int = 1200,
    val isSelfAssessment: Boolean = true,
    val selectedGoals: Set<String> = emptySet(),
    val hasExistingProfile: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    /**
     * Lädt bereits gespeicherte Einstellungen und prüft, ob schon ein lokales Profil existiert,
     * um versehentliches Überschreiben oder Duplikate zu vermeiden.
     */
    private fun loadInitialState() {
        viewModelScope.launch {
            try {
                val prefs = preferencesRepository.userPreferencesFlow.first()
                val existingProfiles = profileDao.getAllProfiles().first()
                val primaryProfile = existingProfiles.firstOrNull()

                _uiState.update { state ->
                    state.copy(
                        selectedLanguage = prefs.language,
                        selectedGoals = prefs.learningGoals,
                        hasExistingProfile = primaryProfile != null,
                        playerName = primaryProfile?.name ?: state.playerName
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Vorhandene Einstellungen konnten nicht geladen werden: ${e.localizedMessage ?: "Unbekannter Fehler"}")
                }
            }
        }
    }

    fun setLanguage(language: String) {
        val validLanguage = if (language in listOf("de", "en")) language else "de"
        _uiState.update { it.copy(selectedLanguage = validLanguage, errorMessage = null) }
        viewModelScope.launch {
            try {
                preferencesRepository.updateLanguage(validLanguage)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Sprache konnte nicht gespeichert werden.")
                }
            }
        }
    }

    fun setPlayerName(name: String) {
        _uiState.update { it.copy(playerName = name, errorMessage = null) }
    }

    /**
     * Setzt den vorläufigen Startwert basierend auf der Selbsteinschätzung des Nutzers.
     */
    fun setSelfAssessedSkillLevel(estimatedElo: Int) {
        val clampedElo = estimatedElo.coerceIn(400, 2800)
        _uiState.update {
            it.copy(
                estimatedStartingElo = clampedElo,
                isSelfAssessment = true,
                errorMessage = null
            )
        }
    }

    fun toggleGoal(goal: String) {
        _uiState.update { state ->
            val newGoals = if (state.selectedGoals.contains(goal)) {
                state.selectedGoals - goal
            } else {
                state.selectedGoals + goal
            }
            state.copy(selectedGoals = newGoals, errorMessage = null)
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3), errorMessage = null) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0), errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Schließt das Onboarding vollständig offline ab:
     * 1. Verhindert Mehrfachausführung durch [isSaving]- und [isCompleted]-Prüfung.
     * 2. Speichert Sprache, Lernziele und Abschlussstatus im [PreferencesRepository].
     * 3. Legt nur dann ein neues lokales [ProfileEntity] in Room an, wenn noch kein Profil existiert.
     */
    fun completeOnboarding(onFinished: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.isCompleted) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // 1. DataStore-Einstellungen dauerhaft sichern
                preferencesRepository.updateLanguage(currentState.selectedLanguage)
                preferencesRepository.updateLearningGoals(currentState.selectedGoals)

                // 2. Prüfen, ob bereits ein lokales Profil existiert (Schutz vor Duplikaten & Überschreiben)
                val existingProfiles = profileDao.getAllProfiles().first()
                if (existingProfiles.isEmpty()) {
                    val sanitizedName = currentState.playerName.trim().ifBlank {
                        if (currentState.selectedLanguage == "en") "Chess Player" else "Schachspieler"
                    }
                    val startingElo = currentState.estimatedStartingElo.coerceIn(400, 2800)

                    val newProfile = ProfileEntity(
                        name = sanitizedName,
                        isGuest = false,
                        eloTactics = startingElo,
                        eloStrategy = startingElo,
                        eloOpenings = startingElo,
                        eloEndgame = startingElo,
                        eloCalculation = startingElo,
                        eloBlitz = startingElo
                    )
                    profileDao.insertProfile(newProfile)
                }

                // 3. Onboarding als abgeschlossen markieren
                preferencesRepository.updateOnboardingCompleted(true)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        hasExistingProfile = true,
                        isCompleted = true
                    )
                }
                onFinished()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Fehler beim Speichern des Profils: ${e.localizedMessage ?: "Unbekannter Fehler"}"
                    )
                }
            }
        }
    }
}
