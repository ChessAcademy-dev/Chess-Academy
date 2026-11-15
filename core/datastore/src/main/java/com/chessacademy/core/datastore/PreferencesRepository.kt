package com.chessacademy.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Singleton DataStore Instanz für das gesamte Projekt
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chess_academy_preferences")

/**
 * Repository zur reaktiven Verwaltung und Speicherung aller Benutzereinstellungen über DataStore.
 * Vollständig abgesichert gegen ungültige Werte und vorbereitet für Hilt Dependency Injection.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val LANGUAGE = stringPreferencesKey("language")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val PIECE_STYLE = stringPreferencesKey("piece_style")
        val PIECE_CONTRAST = stringPreferencesKey("piece_contrast")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val TRANSPARENCY_LEVEL = floatPreferencesKey("transparency_level")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val KIDS_MODE_ENABLED = booleanPreferencesKey("kids_mode_enabled")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val LEARNING_GOALS = stringSetPreferencesKey("learning_goals")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val rawVolume = preferences[PreferencesKeys.SOUND_VOLUME] ?: 1.0f
            val rawTransparency = preferences[PreferencesKeys.TRANSPARENCY_LEVEL] ?: 0.8f
            val rawHour = preferences[PreferencesKeys.DAILY_REMINDER_HOUR] ?: 19
            val rawMinute = preferences[PreferencesKeys.DAILY_REMINDER_MINUTE] ?: 0

            UserPreferences(
                themeMode = preferences[PreferencesKeys.THEME_MODE]?.takeIf { it in listOf("light", "dark", "system") } ?: "system",
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                language = preferences[PreferencesKeys.LANGUAGE]?.takeIf { it in listOf("de", "en") } ?: "de",
                boardTheme = preferences[PreferencesKeys.BOARD_THEME] ?: "classic",
                pieceStyle = preferences[PreferencesKeys.PIECE_STYLE] ?: "staunton",
                pieceContrast = preferences[PreferencesKeys.PIECE_CONTRAST]?.takeIf { it in listOf("none", "thin", "medium", "strong", "auto") } ?: "auto",
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                soundVolume = rawVolume.coerceIn(0.0f, 1.0f),
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true,
                transparencyLevel = rawTransparency.coerceIn(0.0f, 1.0f),
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                dailyReminderHour = rawHour.coerceIn(0, 23),
                dailyReminderMinute = rawMinute.coerceIn(0, 59),
                kidsModeEnabled = preferences[PreferencesKeys.KIDS_MODE_ENABLED] ?: false,
                cloudSyncEnabled = preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] ?: false,
                learningGoals = preferences[PreferencesKeys.LEARNING_GOALS] ?: emptySet(),
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            )
        }

    suspend fun updateThemeMode(themeMode: String) {
        val validMode = if (themeMode in listOf("light", "dark", "system")) themeMode else "system"
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = validMode }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateLanguage(language: String) {
        val validLang = if (language in listOf("de", "en")) language else "de"
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = validLang }
    }

    suspend fun updateBoardTheme(boardTheme: String) {
        context.dataStore.edit { it[PreferencesKeys.BOARD_THEME] = boardTheme }
    }

    suspend fun updatePieceStyle(pieceStyle: String) {
        context.dataStore.edit { it[PreferencesKeys.PIECE_STYLE] = pieceStyle }
    }

    suspend fun updatePieceContrast(contrast: String) {
        val validContrast = if (contrast in listOf("none", "thin", "medium", "strong", "auto")) contrast else "auto"
        context.dataStore.edit { it[PreferencesKeys.PIECE_CONTRAST] = validContrast }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateSoundVolume(volume: Float) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_VOLUME] = volume.coerceIn(0.0f, 1.0f) }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun updateAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun updateTransparencyLevel(level: Float) {
        context.dataStore.edit { it[PreferencesKeys.TRANSPARENCY_LEVEL] = level.coerceIn(0.0f, 1.0f) }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateDailyReminderTime(hour: Int, minute: Int) {
        val validHour = hour.coerceIn(0, 23)
        val validMinute = minute.coerceIn(0, 59)
        context.dataStore.edit {
            it[PreferencesKeys.DAILY_REMINDER_HOUR] = validHour
            it[PreferencesKeys.DAILY_REMINDER_MINUTE] = validMinute
        }
    }

    suspend fun updateKidsModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KIDS_MODE_ENABLED] = enabled }
    }

    suspend fun updateCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CLOUD_SYNC_ENABLED] = enabled }
    }

    suspend fun updateLearningGoals(goals: Set<String>) {
        val sanitizedGoals = goals.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        context.dataStore.edit { it[PreferencesKeys.LEARNING_GOALS] = sanitizedGoals }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }
}
