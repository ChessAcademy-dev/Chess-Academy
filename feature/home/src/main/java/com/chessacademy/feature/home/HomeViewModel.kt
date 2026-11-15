package com.chessacademy.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessacademy.core.database.dao.GameDao
import com.chessacademy.core.database.dao.ProfileDao
import com.chessacademy.core.database.entity.GameEntity
import com.chessacademy.core.database.entity.ProfileEntity
import com.chessacademy.core.datastore.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val profile: ProfileEntity? = null,
    val recentGames: List<GameEntity> = emptyList(),
    val learningGoals: Set<String> = emptySet(),
    val language: String = "de",
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileDao: ProfileDao,
    private val gameDao: GameDao,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                // Kombiniere Profile, Partien und Benutzereinstellungen reaktiv
                combine(
                    profileDao.getAllProfiles(),
                    gameDao.getAllGames(),
                    preferencesRepository.userPreferencesFlow
                ) { profiles, games, prefs ->
                    val activeProfile = profiles.firstOrNull()
                    HomeUiState(
                        isLoading = false,
                        profile = activeProfile,
                        recentGames = games.take(5), // Die 5 letzten Partien
                        learningGoals = prefs.learningGoals,
                        language = prefs.language,
                        errorMessage = null
                    )
                }.catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = e.localizedMessage ?: "Fehler beim Laden der Daten"
                        ) 
                    }
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.localizedMessage ?: UnidentifiedError()
                    ) 
                }
            }
        }
    }

    private fun UnidentifiedError(): String = "Unbekannter Fehler aufgetreten"

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
