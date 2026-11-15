package com.chessacademy.chess.rules

object SanResolver {

    /**
     * Löst einen SAN-Zug (Standard Algebraic Notation, z.B. "Nf3", "O-O", "exd8=Q+")
     * anhand der aktuellen Brettstellung in einen präzisen [Move] auf.
     */
    fun resolveSan(position: BoardPosition, san: String): Move? {
        val cleanSan = san.replace("+", "").replace("#", "").trim()
        if (cleanSan.isEmpty()) return null

        val legalMoves = ChessRules.getAllLegalMoves(position, position.activeColor)

        // 1. Rochade prüfen
        if (cleanSan == "O-O" || cleanSan == "0-0") {
            val rank = if (position.activeColor == Color.WHITE) 0 else 7
            return legalMoves.find { it.from == Square(4, rank) && it.to == Square(6, rank) }
        }
        if (cleanSan == "O-O-O" || cleanSan == "0-0-0") {
            val rank = if (position.activeColor == Color.WHITE) 0 else 7
            return legalMoves.find { it.from == Square(4, rank) && it.to == Square(2, rank) }
        }

        // 2. Umwandlung extrahieren (z.B. "=Q")
        var promotionType: PieceType? = null
        var workingSan = cleanSan
        val promoIndex = workingSan.indexOf('=')
        if (promoIndex != -1 && promoIndex + 1 < workingSan.length) {
            promotionType = when (workingSan[promoIndex + 1].uppercaseChar()) {
                'Q' -> PieceType.QUEEN
                'R' -> PieceType.ROOK
                'B' -> PieceType.BISHOP
                'N' -> PieceType.KNIGHT
                else -> null
            }
            workingSan = workingSan.substring(0, promoIndex)
        }

        // 3. Ziel-Square ermitteln (die letzten beiden Zeichen, z. B. "e4")
        if (workingSan.length < 2) return null
        val targetSquareStr = workingSan.takeLast(2)
        val targetSquare = Square.fromAlgebraic(targetSquareStr) ?: return null
        val prefix = workingSan.dropLast(2) // z. B. "Nf" bei Nf3 oder "ex" bei exd5 oder "Nbd7"

        // 4. Figutyp bestimmen
        val pieceType = when {
            prefix.isNotEmpty() && prefix[0].isUpperCase() -> {
                when (prefix[0]) {
                    'N' -> PieceType.KNIGHT
                    'B' -> PieceType.BISHOP
                    'R' -> PieceType.ROOK
                    'Q' -> PieceType.QUEEN
                    'K' -> PieceType.KING
                    else -> PieceType.PAWN
                }
            }
            else -> PieceType.PAWN
        }

        // 5. Filter auf legale Züge anwenden
        val candidateMoves = legalMoves.filter { move ->
            move.to == targetSquare &&
            position.pieceAt(move.from)?.type == pieceType &&
            (promotionType == null || move.promotion == promotionType)
        }

        if (candidateMoves.isEmpty()) return null
        if (candidateMoves.size == 1) return candidateMoves[0]

        // 6. Disambiguierung auflösen (z.B. Date oder Rank angegeben wie in "Nbd7" oder "R1a3")
        val disambigChar = prefix.filter { !it.isUpperCase() && it != 'x' }.firstOrNull()
        if (disambigChar != null) {
            val fileVal = disambigChar - 'a'
            val rankVal = disambigChar - '1'
            return candidateMoves.find { move ->
                if (fileVal in 0..7) move.from.file == fileVal
                else if (rankVal in 0..7) move.from.rank == rankVal
                else false
            }
        }

        return candidateMoves.firstOrNull()
    }
}
