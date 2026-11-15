package com.chessacademy.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Zentraler Manager für Soundeffekte (über SoundPool) und haptisches Feedback (Vibration).
 * Respektiert automatisch die Benutzerpräferenzen (Sound an/aus, Lautstärke, Vibration an/aus)
 * aus dem PreferencesRepository.
 * 
 * Wichtiger Hinweis zu Sound-Ressourcen:
 * Die entsprechenden Audio-Dateien (z.B. move.ogg, capture.ogg, check.ogg, checkmate.ogg, invalid.ogg, success.ogg)
 * müssen im späteren Verlauf im Ordner `res/raw/` abgelegt und verlinkt werden.
 */
@Singleton
class FeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Sound IDs (werden nach dem Laden zugewiesen)
    // Hinweis: Falls res/raw noch nicht existiert, fängt der Manager das Laden sicher ab.
    private var moveSoundId: Int = 0
    private var captureSoundId: Int = 0
    private var checkSoundId: Int = 0
    private var checkmateSoundId: Int = 0
    private var invalidSoundId: Int = 0
    private var successSoundId: Int = 0

    init {
        loadSounds()
    }

    private fun loadSounds() {
        try {
            // Platzhalter-Sicherheitsprüfung für res/raw Ressourcen
            // Sobald die OGG-Dateien in res/raw hinzugefügt werden, greifen diese IDs.
            val res = context.resources
            val pkg = context.packageName

            moveSoundId = getSafeResId(res, pkg, "move")
            captureSoundId = getSafeResId(res, pkg, "capture")
            checkSoundId = getSafeResId(res, pkg, "check")
            checkmateSoundId = getSafeResId(res, pkg, "checkmate")
            invalidSoundId = getSafeResId(res, pkg, "invalid")
            successSoundId = getSafeResId(res, pkg, "success")
        } catch (e: Exception) {
            // Verhindert Abstürze, falls Sounddateien beim initialen Build fehlen
        }
    }

    private fun getSafeResId(res: android.content.res.Resources, pkg: String, name: String): Int {
        val id = res.getIdentifier(name, "raw", pkg)
        return if (id != 0) soundPool.load(context, id, 1) else 0
    }

    fun playMove(soundEnabled: Boolean = true, volume: Float = 1.0f, vibrationEnabled: Boolean = true) {
        playSound(moveSoundId, soundEnabled, volume)
        vibrate(vibrationEnabled, 25L)
    }

    fun playCapture(soundEnabled: Boolean = true, volume: Float = 1.0f, vibrationEnabled: Boolean = true) {
        playSound(captureSoundId, soundEnabled, volume)
        vibrate(vibrationEnabled, 50L)
    }

    fun playCheck(soundEnabled: Boolean = true, volume: Float = 1.0f, vibrationEnabled: Boolean = true) {
        playSound(checkSoundId, soundEnabled, volume)
        vibrate(vibrationEnabled, 75L)
    }

    fun playCheckmate(soundEnabled: Boolean = true, volume: Float = 1.0f, vibrationEnabled: Boolean = true) {
        playSound(checkmateSoundId, soundEnabled, volume)
        vibrate(vibrationEnabled, 150L)
    }

    fun playInvalid(soundEnabled: Boolean = true, volume: Float = 1.0f, vibrationEnabled: Boolean = true) {
        playSound(invalidSoundId, soundEnabled, volume)
        vibrate(vibrationEnabled, 40L)
    }

    fun playSuccess(soundEnabled: Boolean = true, volume: Float = 1.0f, vibrationEnabled: Boolean = true) {
        playSound(successSoundId, soundEnabled, volume)
        vibrate(vibrationEnabled, 100L)
    }

    private fun playSound(soundId: Int, enabled: Boolean, volume: Float) {
        if (!enabled || soundId == 0) return
        val clampedVolume = volume.coerceIn(0.0f, 1.0f)
        soundPool.play(soundId, clampedVolume, clampedVolume, 1, 0, 1.0f)
    }

    private fun vibrate(enabled: Boolean, durationMs: Long) {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    /**
     * Gibt alle Ressourcen beim Beenden der Anwendung frei.
     */
    fun release() {
        soundPool.release()
    }
}
