package com.chessacademy.chess.engine

/**
 * Repräsentiert die aktuellen Analyseergebnisse der Schachengine.
 */
data class AnalysisInfo(
    val depth: Int = 0,
    val scoreCp: Int? = null,      // Centipawns (positiv = Vorteil Weiß)
    val scoreMate: Int? = null,    // Matt in X Zügen
    val pv: List<String> = emptyList(), // Principal Variation (bestวยer Zugpfad)
    val nodes: Long = 0L,
    val timeMs: Long = 0L
)

/**
 * Ergebnis einer abgeschlossenen Engine-Berechnung.
 */
data class EngineResult(
    val bestMove: String,
    val ponderMove: String? = null
)
