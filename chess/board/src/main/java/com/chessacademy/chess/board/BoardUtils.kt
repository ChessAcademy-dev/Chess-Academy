package com.chessacademy.chess.board

object BoardUtils {

    /**
     * Wandelt eine FEN-Notation in ein Figuren-Map-Modell für das Brett um.
     */
    fun fenToPieces(fen: String): Map<Int, ChessPieceModel> {
        val pieces = mutableMapOf<Int, ChessPieceModel>()
        val fenBoard = fen.split(" ").firstOrNull() ?: return pieces

        var squareIndex = 0
        for (char in fenBoard) {
            when {
                char == '/' -> continue
                char.isDigit() -> {
                    squareIndex += char.toString().toInt()
                }
                else -> {
                    val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    val type = when (char.lowercaseChar()) {
                        'p' -> PieceType.PAWN
                        'n' -> PieceType.KNIGHT
                        'b' -> PieceType.BISHOP
                        'r' -> PieceType.ROOK
                        'q' -> PieceType.QUEEN
                        'k' -> PieceType.KING
                        else -> null
                    }
                    if (type != null && squareIndex < 64) {
                        pieces[squareIndex] = ChessPieceModel(type, color)
                    }
                    squareIndex++
                }
            }
        }
        return pieces
    }

    /**
     * Standard-Startstellung nach FEN.
     */
    const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
}
