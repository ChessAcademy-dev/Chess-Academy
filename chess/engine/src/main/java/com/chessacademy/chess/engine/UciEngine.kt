package com.chessacademy.chess.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * UCI-Engine-Client zur Steuerung von Stockfish auf Android.
 * Kommuniziert über Standard-Eingabe/Ausgabe (Stdin/Stdout) mit dem nativen Prozess.
 * 
 * WICHTIGER HINWEIS ZU BINÄRDATEIEN & LIZENZ:
 * - Stockfish steht unter der GNU General Public License v3.0 (GPLv3).
 * - Die entsprechende Stockfish-Binärdatei (z. B. stockfish-android-arm64) ist im Repository 
 *   nicht enthalten und muss vor dem Release kompiliert und im assets/-Ordner abgelegt werden.
 */
class UciEngine(private val context: Context) {

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val isRunning = AtomicBoolean(false)

    /**
     * Startet den Stockfish-Prozess aus den App-Assets.
     */
    suspend fun startEngine(): Boolean = withContext(Dispatchers.IO) {
        if (isRunning.get()) return@withContext true

        try {
            // Binärdatei aus assets/ in den internen App-Speicher kopieren (falls noch nicht geschehen)
            val binaryFile = prepareEngineBinary() ?: return@withContext false

            val processBuilder = ProcessBuilder(binaryFile.absolutePath)
            processBuilder.redirectErrorStream(true)
            process = processBuilder.start()

            writer = BufferedWriter(OutputStreamWriter(process!!.outputStream))
            reader = BufferedReader(InputStreamReader(process!!.inputStream))

            // UCI-Initialisierung
            sendCommand("uci")
            var line: String?
            while (reader!!.readLine().also { line = it } != null) {
                if (line == "uciok") {
                    break
                }
            }

            sendCommand("isready")
            while (reader!!.readLine().also { line = it } != null) {
                if (line == "readyok") {
                    break
                }
            }

            isRunning.set(true)
            true
        } catch (e: Exception) {
            stopEngine()
            false
        }
    }

    /**
     * Sendet einen Befehl an die Engine.
     */
    suspend fun sendCommand(command: String) = withContext(Dispatchers.IO) {
        try {
            writer?.let {
                it.write("$command\n")
                it.flush()
            }
        } catch (e: Exception) {
            // Fehlerbehandlung bei abgebrochenem Prozess
        }
    }

    /**
     * Analysiert eine Stellung (FEN) bis zu einer bestimmten Tiefe oder Zeit.
     */
    suspend fun analyzePosition(
        fen: String,
        depth: Int = 15,
        onInfo: (AnalysisInfo) -> Unit
    ): EngineResult? = withContext(Dispatchers.IO) {
        if (!isRunning.get()) {
            if (!startEngine()) return@withContext null
        }

        sendCommand("position fen $fen")
        sendCommand("go depth $depth")

        var bestMove = ""
        var ponderMove: String? = null

        try {
            while (true) {
                val line = reader?.readLine() ?: break
                if (line.startsWith("info")) {
                    val info = parseInfoLine(line)
                    onInfo(info)
                } else if (line.startsWith("bestmove")) {
                    val parts = line.split(" ")
                    if (parts.size >= 2) {
                        bestMove = parts[1]
                    }
                    if (parts.size >= 4 && parts[2] == "ponder") {
                        ponderMove = parts[3]
                    }
                    break
                }
            }
        } catch (e: Exception) {
            // Abbruch oder Lesefehler
        }

        if (bestMove.isBlank()) null else EngineResult(bestMove, ponderMove)
    }

    /**
     * Stoppt laufende Berechnungen sofort.
     */
    suspend fun stopAnalysis() {
        sendCommand("stop")
    }

    /**
     * Beendet den Engine-Prozess und gibt Ressourcen frei.
     */
    suspend fun stopEngine() = withContext(Dispatchers.IO) {
        try {
            sendCommand("quit")
            writer?.close()
            reader?.close()
            process?.destroy()
        } catch (e: Exception) {
            // Ignorieren
        } finally {
            writer = null
            reader = null
            process = null
            isRunning.set(false)
        }
    }

    private fun prepareEngineBinary(): File? {
        return try {
            val file = File(context.filesDir, "stockfish")
            if (!file.exists()) {
                context.assets.open("stockfish").use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            file.setExecutable(true)
            file
        } catch (e: Exception) {
            null // Binärdatei fehlt in assets/ oder konnte nicht kopiert werden
        }
    }

    private fun parseInfoLine(line: String): AnalysisInfo {
        var depth = 0
        var scoreCp: Int? = null
        var scoreMate: Int? = null
        val pv = mutableListOf<String>()
        var nodes = 0L
        var timeMs = 0L

        val tokens = line.split(" ")
        var i = 0
        while (i < tokens.size) {
            when (tokens[i]) {
                "depth" -> if (i + 1 < tokens.size) depth = tokens[i + 1].toIntOrNull() ?: 0
                "nodes" -> if (i + 1 < tokens.size) nodes = tokens[i + 1].toLongOrNull() ?: 0L
                "time" -> if (i + 1 < tokens.size) timeMs = tokens[i + 1].toLongOrNull() ?: 0L
                "score" -> {
                    if (i + 2 < tokens.size) {
                        val type = tokens[i + 1]
                        val value = tokens[i + 2].toIntOrNull() ?: 0
                        if (type == "cp") scoreCp = value
                        if (type == "mate") scoreMate = value
                    }
                }
                "pv" -> {
                    i++
                    while (i < tokens.size) {
                        pv.add(tokens[i])
                        i++
                    }
                }
            }
            i++
        }

        return AnalysisInfo(depth, scoreCp, scoreMate, pv, nodes, timeMs)
    }
}
