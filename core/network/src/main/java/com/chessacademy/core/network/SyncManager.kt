package com.chessacademy.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.chessacademy.core.common.Resource
import com.chessacademy.core.datastore.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repräsentiert den aktuellen Status der Cloud-Synchronisierung.
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Manager zur Steuerung der optionalen Cloud-Synchronisierung.
 * 
 * OFFLINE-FIRST ARCHITEKTUR:
 * - Die App arbeitet vollständig lokal über die Room-Datenbank und DataStore. 
 * - Ein Internetzugang ist für den Kernbetrieb (Spielen, Training, Academy, Analyse) nicht erforderlich.
 * - Da aktuell kein Backend (Cloud-Server) angebunden ist, bricht der Sync sauber ab und teilt 
 *   dem Nutzer transparent mit, dass der Offline-Modus aktiv ist.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: Flow<SyncState> = _syncState.asStateFlow()

    /**
     * Prüft, ob das Gerät aktuell mit dem Internet verbunden ist.
     * Benötigt die Berechtigung ACCESS_NETWORK_STATE im AndroidManifest.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Führt eine optionale Synchronisierung aus, falls der Benutzer dies in den Einstellungen
     * aktiviert hat. Da kein Backend konfiguriert ist, wird der Status entsprechend gesetzt.
     */
    suspend fun triggerSync(): Resource<Unit> {
        val preferences = preferencesRepository.userPreferencesFlow.first()
        
        // 1. Prüfung, ob Cloud-Sync vom Benutzer gewünscht ist
        if (!preferences.cloudSyncEnabled) {
            _syncState.value = SyncState.Idle
            return Resource.Success(Unit)
        }

        // 2. Prüfung der Internetverbindung
        if (!isNetworkAvailable()) {
            val errorMsg = "Keine Internetverbindung. Lokaler Offline-Modus aktiv."
            _syncState.value = SyncState.Error(errorMsg)
            return Resource.Error(errorMsg)
        }

        return try {
            _syncState.value = SyncState.Syncing

            // Da kein Backend konfiguriert ist, melden wir transparent den Zustand.
            // Die lokale App-Funktionalität bleibt davon völlig unberührt (Offline-First).
            val errorMsg = "Cloud-Synchronisierung derzeit nicht verfügbar (kein Backend konfiguriert)."
            _syncState.value = SyncState.Error(errorMsg)
            Resource.Error(errorMsg)

        } catch (e: CancellationException) {
            // Wichtig: CancellationException niemals verschlucken, damit Coroutines normal abbrechen können
            throw e
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Unbekannter Synchronisierungsfehler"
            _syncState.value = SyncState.Error(errorMsg)
            Resource.Error(errorMsg)
        }
    }
}
