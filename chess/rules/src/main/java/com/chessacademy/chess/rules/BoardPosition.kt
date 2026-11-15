package com.chessacademy.chess.rules

data class CastlingRights(
    val whiteKingside: Boolean = true,
    val whiteQueenside: Boolean = true,
    val blackKingside: Boolean = true,
    val blackQueenside: Boolean = true
)

data class BoardPosition(
    val squares: Map<Square, Piece> = emptyMap(),
    val activeColor: Color = Color.WHITE,
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantSquare: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1
) {
    fun pieceAt(square: Square): Piece? = squares[square]

    fun setPiece(square: Square, piece: Piece?): BoardPosition {
        val newSquares = squares.toMutableMap()
        if (piece == null) {
            newSquares.remove(square)
        } else {
            newSquares[square] = piece
        }
        return copy(squares = newSquares)
    }

    fun findKing(color: Color): Square? {
        return squares.entries.find { it.value == Piece(PieceType.KING, color) }?.key
    }

    companion object {
        fun startingPosition(): BoardPosition {
            return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") ?: BoardPosition()
        }

        fun fromFen(fen: String): BoardPosition? {
            val parts = fen.split(" ")
            if (parts.size < 6) return null

            val boardPart = parts[0]
            val activePart = parts[1]
            val castlingPart = parts[2]
            val epPart = parts[3]
            val halfmovePart = parts[4].toIntOrNull() ?: 0
            val fullmovePart = parts[5].toIntOrNull() ?: 1

            val squares = mutableMapOf<Square, Piece>()
            val rankRows = boardPart.split("/")
            if (rankRows.size != 8) return null

            for (r in 7 downTo 0) {
                val rankIndex = 7 - r
                val rowStr = rankRows[rankIndex]
                val fileIndex = 0
                var f = 0
                for (char in rowStr) {
                    if (char.isDigit()) {
                        f += char.toString().toInt()
                    } else {
                        val color = if (char.isUpperCase()) Color.WHITE else Color.BLACK
                        val type = when (char.lowercaseChar()) {
                            'p' -> PieceType.PAWN
                            'n' -> PieceType.KNIGHT
                            'b' -> PieceType.BISHOP
                            'r' -> PieceType.ROOK
                            'q' -> PieceType.QUEEN
                            'k' -> PieceType.KING
                            else -> return null
                        }
                        if (f > 7) return null
                        squares[Square(f, r)] = Piece(type, color)
                        f++
                    }
                }
            }

            val activeColor = if (activePart == "b") Color.BLACK else Color.WHITE
            val wK = castlingPart.contains('K')
            val wQ = castlingPart.contains('Q')
            val bK = castlingPart.contains('k')
            val bQ = castlingPart.contains('q')

            val epSquare = Square.fromAlgebraic(epPart)

            return BoardPosition(
                squares = squares,
                activeColor = activeColor,
                castlingRights = CastlingRights(wK, wQ, bK, bQ),
                enPassantSquare = epSquare,
                halfmoveClock = halfmovePart,
                fullmoveNumber = fullmovePart
            )
        }
    }

    fun toFen(): String {
        val sb = StringBuilder()
        for (r in 7 downTo 0) {
            var emptyCount = 0
            for (f in 0..7) {
                val piece = squares[Square(f, r)]
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    val char = when (piece.type) {
                        PieceType.PAWN -> 'p'
                        PieceType.KNIGHT -> 'n'
                        PieceType.BISHOP -> 'b'
                        PieceType.ROOK -> 'r'
                        PieceType.QUEEN -> 'q'
                        PieceType.KING -> 'k'
                    }
                    sb.append(if (piece.color == Color.WHITE) char.uppercaseChar() else char)
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount)
            }
            if (r > 0) sb.append('/')
        }

        sb.append(if (activeColor == Color.WHITE) " w " else " b ")

        val castling = buildString {
            if (castlingRights.whiteKingside) append('K')
            if (castlingRights.whiteQueenside) append('Q')
            if (castlingRights.blackKingside) append('k')
            if (castlingRights.blackQueenside) append('q')
        }
        sb.append(if (castling.isEmpty()) "-" else castling)

        sb.append(" ")
        sb.append(enPassantSquare?.toAlgebraic() ?: "-")
        sb.append(" $halfmoveClock $fullmoveNumber")

        return sb.toString()
    }
}
