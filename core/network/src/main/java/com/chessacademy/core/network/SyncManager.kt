package com.chessacademy.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.chessacademy.core.common.Resource
import com.chessacademy.core.datastore.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
 * ARCHITEKTURHINWEIS & OFFENE BACKEND-ENTSCHEIDUNGEN:
 * - Die App arbeitet strikt nach dem **Offline-First-Prinzip**. Alle Daten (Partien, Profile, Training) 
 *   werden primär lokal in der Room-Datenbank gespeichert.
 * - Dieser SyncManager fungiert als abstrakte Schnittstelle. Eine konkrete Backend-Anbindung 
 *   (z. B. Firebase Firestore, Supabase oder ein eigener REST-Server) ist noch offen und kann 
 *   hier später flexibel per Dependency Injection angebunden werden.
 * - Es sind absolut keine API-Schlüssel oder Zugangsdaten im Quellcode hinterlegt.
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
     * aktiviert hat und eine Internetverbindung besteht.
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

        try {
            _syncState.value = SyncState.Syncing

            // TODO: Hier wird später die tatsächliche Synchronisierung mit dem Backend implementiert
            // (z. B. Upload von Partien, Abgleich von Trainingsfortschritten).
            // Da das Backend noch nicht gewählt ist, simulieren wir hier einen erfolgreichen Durchlauf.
            kotlinx.coroutines.delay(1000L)

            _syncState.value = SyncState.Success
            return Resource.Success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: Uncaught sync error
            _syncState.value = SyncState.Error(errorMsg)
            return Resource.Error(errorMsg)
        }
    }
}
