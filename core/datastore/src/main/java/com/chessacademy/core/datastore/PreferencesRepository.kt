package com.chessacademy.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Singleton DataStore Instanz für das gesamte Projekt
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chess_academy_preferences")

/**
 * Repository zur reaktiven Verwaltung und Speicherung aller Benutzereinstellungen über DataStore.
 */
class PreferencesRepository(private val context: Context) {

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
            UserPreferences(
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "system",
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                language = preferences[PreferencesKeys.LANGUAGE] ?: "de",
                boardTheme = preferences[PreferencesKeys.BOARD_THEME] ?: "classic",
                pieceStyle = preferences[PreferencesKeys.PIECE_STYLE] ?: "staunton",
                pieceContrast = preferences[PreferencesKeys.PIECE_CONTRAST] ?: "auto",
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                soundVolume = preferences[PreferencesKeys.SOUND_VOLUME] ?: 1.0f,
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true,
                transparencyLevel = preferences[PreferencesKeys.TRANSPARENCY_LEVEL] ?: 0.8f,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                dailyReminderHour = preferences[PreferencesKeys.DAILY_REMINDER_HOUR] ?: 19,
                dailyReminderMinute = preferences[PreferencesKeys.DAILY_REMINDER_MINUTE] ?: 0,
                kidsModeEnabled = preferences[PreferencesKeys.KIDS_MODE_ENABLED] ?: false,
                cloudSyncEnabled = preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] ?: false
            )
        }

    suspend fun updateThemeMode(themeMode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = themeMode }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    suspend fun updateBoardTheme(boardTheme: String) {
        context.dataStore.edit { it[PreferencesKeys.BOARD_THEME] = boardTheme }
    }

    suspend fun updatePieceStyle(pieceStyle: String) {
        context.dataStore.edit { it[PreferencesKeys.PIECE_STYLE] = pieceStyle }
    }

    suspend fun updatePieceContrast(contrast: String) {
        context.dataStore.edit { it[PreferencesKeys.PIECE_CONTRAST] = contrast }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateSoundVolume(volume: Float) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_VOLUME] = volume }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun updateAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun updateTransparencyLevel(level: Float) {
        context.dataStore.edit { it[PreferencesKeys.TRANSPARENCY_LEVEL] = level }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateDailyReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.DAILY_REMINDER_HOUR] = hour
            it[PreferencesKeys.DAILY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateKidsModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KIDS_MODE_ENABLED] = enabled }
    }

    suspend fun updateCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CLOUD_SYNC_ENABLED] = enabled }
    }
}
