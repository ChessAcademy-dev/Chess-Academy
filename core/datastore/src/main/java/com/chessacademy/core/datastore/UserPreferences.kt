package com.chessacademy.core.datastore

/**
 * Datenmodell für sämtliche Benutzereinstellungen der Chess Academy.
 * Enthält sinnvolle Standardwerte für alle Konfigurationsbereiche.
 */
data class UserPreferences(
    val themeMode: String = "system",              // "light", "dark", "system"
    val dynamicColor: Boolean = true,               // Material You (API 31+)
    val language: String = "de",                    // "de", "en"
    val boardTheme: String = "classic",             // Brettstil
    val pieceStyle: String = "staunton",            // Figurenfamilie
    val pieceContrast: String = "auto",             // "none", "thin", "medium", "strong", "auto"
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val vibrationEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val transparencyLevel: Float = 0.8f,            // Für selektives Glass-Design
    val notificationsEnabled: Boolean = true,
    val dailyReminderHour: Int = 19,
    val dailyReminderMinute: Int = 0,
    val kidsModeEnabled: Boolean = false,           // Familien- / Kindermodus
    val cloudSyncEnabled: Boolean = false,          // Optionale Cloud-Synchronisierung
    val learningGoals: Set<String> = emptySet(),    // Ausgewählte Lernziele aus dem Onboarding
    val onboardingCompleted: Boolean = false        // Status des Onboardings
)
