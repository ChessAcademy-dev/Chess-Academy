package com.chessacademy.chess.board

/**
 * Repräsentiert eine Schachfigur auf dem Brett (Farbe + Typ).
 */
data class ChessPieceModel(
    val type: PieceType,
    val color: PieceColor
) {
    val symbol: String get() = when (color) {
        PieceColor.WHITE -> when (type) {
            PieceType.PAWN -> "P"
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }
        PieceColor.BLACK -> when (type) {
            PieceType.PAWN -> "p"
            PieceType.KNIGHT -> "n"
            PieceType.BISHOP -> "b"
            PieceType.ROOK -> "r"
            PieceType.QUEEN -> "q"
            PieceType.KING -> "k"
        }
    }
}

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }
enum class PieceColor { WHITE, BLACK }

/**
 * Datenmodell zur Steuerung der Brettansicht und Markierungen.
 */
data class BoardViewState(
    val pieces: Map<Int, ChessPieceModel> = emptyMap(), // Mapping von Feldindex 0-63 auf Figur
    val selectedSquare: Int? = null,                    // Ausgewähltes Feld (0-63)
    val legalMoves: Set<Int> = emptySet(),              // Erlaubte Zielfelder für die ausgewählte Figur
    val lastMoveFrom: Int? = null,                      // Startfeld des letzten Zuges
    val lastMoveTo: Int? = null,                        // Zielfeld des letzten Zuges
    val isFlipped: Boolean = false                      // true = Schwarz unten, false = Weiß unten
)
