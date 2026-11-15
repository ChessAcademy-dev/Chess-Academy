package com.chessacademy.chess.pgn

/**
 * Repräsentiert eine vollständige PGN-Partie inklusive Metadaten (Header),
 * Zugabfolge, Kommentaren, Untervarianten und Ergebnis.
 */
data class PgnGame(
    val headers: Map<String, String> = emptyMap(),
    val moves: List<PgnMoveNode> = emptyList(),
    val result: String = "*"
) {
    val event: String get() = headers["Event"] ?: "?"
    val site: String get() = headers["Site"] ?: "?"
    val date: String get() = headers["Date"] ?: "????.??.??"
    val white: String get() = headers["White"] ?: "?"
    val black: String get() = headers["Black"] ?: "?"
    val eco: String get() = headers["ECO"] ?: ""
}

/**
 * Repräsentiert einen einzelnen Zug im Pgn-Baum inklusive Kommentaren und Untervarianten.
 */
data class PgnMoveNode(
    val moveNumber: Int,
    val isWhiteMove: Boolean,
    val sanMove: String,
    val commentBefore: String? = null,
    val commentAfter: String? = null,
    val variations: List<List<PgnMoveNode>> = emptyList(),
    val nags: List<String> = emptyList() // Z. B. "$1" für !, "$2" für ?
)
