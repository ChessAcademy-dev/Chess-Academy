package com.chessacademy.chess.rules

enum class Color { WHITE, BLACK } {
    fun opposite(): Color = if (this == WHITE) BLACK else WHITE
}

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }

data class Piece(val type: PieceType, val color: Color)

data class Square(val file: Int, val rank: Int) { // file 0..7 (a..h), rank 0..7 (1..8)
    init {
        require(file in 0..7 && rank in 0..7) { "Ungültiges Feld: file=$file, rank=$rank" }
    }

    val index: Int get() = rank * 8 + file

    companion object {
        fun fromIndex(index: Int): Square {
            require(index in 0..63) { "Ungültiger Brettindex: $index" }
            return Square(index % 8, index / 8)
        }

        fun fromAlgebraic(notation: String): Square? {
            if (notation.length != 2) return null
            val file = notation[0] - 'a'
            val rank = notation[1] - '1'
            if (file !in 0..7 || rank !in 0..7) return null
            return Square(file, rank)
        }
    }

    fun toAlgebraic(): String {
        val fChar = ('a'.code + file).toChar()
        val rChar = ('1'.code + rank).toChar()
        return "$fChar$rChar"
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null
)

enum class GameStateStatus {
    ACTIVE,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_BY_REPETITION,
    DRAW_BY_MATERIAL
}
