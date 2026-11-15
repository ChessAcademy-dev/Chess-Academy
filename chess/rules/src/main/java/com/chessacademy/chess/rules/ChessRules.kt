package com.chessacademy.chess.rules

object ChessRules {

    fun getLegalMoves(position: BoardPosition, square: Square): List<Move> {
        val piece = position.pieceAt(square) ?: return emptyList()
        if (piece.color != position.activeColor) return emptyList()

        val pseudoMoves = getPseudoLegalMoves(position, square)
        return pseudoMoves.filter { move ->
            val nextPos = applyMoveUnsafe(position, move)
            !isKingUnderAttack(nextPos, position.activeColor)
        }
    }

    fun getAllLegalMoves(position: BoardPosition, color: Color): List<Move> {
        val moves = mutableListOf<Move>()
        for ((sq, piece) in position.squares) {
            if (piece.color == color) {
                moves.addAll(getLegalMoves(position, sq))
            }
        }
        return moves
    }

    fun isKingUnderAttack(position: BoardPosition, color: Color): Boolean {
        val kingSquare = position.findKing(color) ?: return false
        val enemyColor = color.opposite()

        for ((sq, piece) in position.squares) {
            if (piece.color == enemyColor) {
                if (canAttack(position, sq, kingSquare, piece)) {
                    return true
                }
            }
        }
        return false
    }

    fun getGameStatus(position: BoardPosition, moveHistory: List<BoardPosition> = emptyList()): GameStateStatus {
        val legalMoves = getAllLegalMoves(position, position.activeColor)
        val inCheck = isKingUnderAttack(position, position.activeColor)

        if (legalMoves.isEmpty()) {
            return if (inCheck) GameStateStatus.CHECKMATE else GameStateStatus.STALEMATE
        }

        if (inCheck) {
            return GameStateStatus.CHECK
        }

        // Remisprüfung durch ungenügendes Material
        if (hasInsufficientMaterial(position)) {
            return GameStateStatus.DRAW_BY_MATERIAL
        }

        // Stellungswiederholung (Dreifache Wiederholung)
        if (isThreefoldRepetition(position, moveHistory)) {
            return GameStateStatus.DRAW_BY_REPETITION
        }

        return GameStateStatus.ACTIVE
    }

    private fun getPseudoLegalMoves(position: BoardPosition, square: Square): List<Move> {
        val piece = position.pieceAt(square) ?: return emptyList()
        val moves = mutableListOf<Move>()

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == Color.WHITE) 1 else -1
                val startRank = if (piece.color == Color.WHITE) 1 else 6
                val forward = Square(square.file, square.rank + dir)

                if (forward.rank in 0..7 && position.pieceAt(forward) == null) {
                    addPawnMoves(square, forward, piece.color, moves)
                    val doubleForward = Square(square.file, square.rank + 2 * dir)
                    if (square.rank == startRank && position.pieceAt(doubleForward) == null) {
                        moves.add(Move(square, doubleForward))
                    }
                }

                // Schlagen diagonal & En passant
                for (df in listOf(-1, 1)) {
                    val targetFile = square.file + df
                    if (targetFile in 0..7) {
                        val target = Square(targetFile, square.rank + dir)
                        if (target.rank in 0..7) {
                            val targetPiece = position.pieceAt(target)
                            if (targetPiece != null && targetPiece.color != piece.color) {
                                addPawnMoves(square, target, piece.color, moves)
                            } else if (target == position.enPassantSquare) {
                                moves.add(Move(square, target))
                            }
                        }
                    }
                }
            }
            PieceType.KNIGHT -> {
                val offsets = listOf(
                    Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
                    Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
                )
                for ((df, dr) in offsets) {
                    val target = Square(square.file + df, square.rank + dr)
                    if (target.file in 0..7 && target.rank in 0..7) {
                        val p = position.pieceAt(target)
                        if (p == null || p.color != piece.color) {
                            moves.add(Move(square, target))
                        }
                    }
                }
            }
            PieceType.KING -> {
                val offsets = listOf(
                    Pair(1, 0), Pair(1, 1), Pair(0, 1), Pair(-1, 1),
                    Pair(-1, 0), Pair(-1, -1), Pair(0, -1), Pair(1, -1)
                )
                for ((df, dr) in offsets) {
                    val target = Square(square.file + df, square.rank + dr)
                    if (target.file in 0..7 && target.rank in 0..7) {
                        val p = position.pieceAt(target)
                        if (p == null || p.color != piece.color) {
                            moves.add(Move(square, target))
                        }
                    }
                }

                // Rochade
                if (piece.color == Color.WHITE) {
                    if (position.castlingRights.whiteKingside && position.pieceAt(Square(5, 0)) == null && position.pieceAt(Square(6, 0)) == null) {
                        if (!isKingUnderAttack(position, Color.WHITE) && !isSquareAttacked(position, Square(5, 0), Color.BLACK) && !isSquareAttacked(position, Square(6, 0), Color.BLACK)) {
                            moves.add(Move(Square(4, 0), Square(6, 0)))
                        }
                    }
                    if (position.castlingRights.whiteQueenside && position.pieceAt(Square(1, 0)) == null && position.pieceAt(Square(2, 0)) == null && position.pieceAt(Square(3, 0)) == null) {
                        if (!isKingUnderAttack(position, Color.WHITE) && !isSquareAttacked(position, Square(3, 0), Color.BLACK) && !isSquareAttacked(position, Square(2, 0), Color.BLACK)) {
                            moves.add(Move(Square(4, 0), Square(2, 0)))
                        }
                    }
                } else {
                    if (position.castlingRights.blackKingside && position.pieceAt(Square(5, 7)) == null && position.pieceAt(Square(6, 7)) == null) {
                        if (!isKingUnderAttack(position, Color.BLACK) && !isSquareAttacked(position, Square(5, 7), Color.WHITE) && !isSquareAttacked(position, Square(6, 7), Color.WHITE)) {
                            moves.add(Move(Square(4, 7), Square(6, 7)))
                        }
                    }
                    if (position.castlingRights.blackQueenside && position.pieceAt(Square(1, 7)) == null && position.pieceAt(Square(2, 7)) == null && position.pieceAt(Square(3, 7)) == null) {
                        if (!isKingUnderAttack(position, Color.BLACK) && !isSquareAttacked(position, Square(3, 7), Color.WHITE) && !isSquareAttacked(position, Square(2, 7), Color.WHITE)) {
                            moves.add(Move(Square(4, 7), Square(2, 7)))
                        }
                    }
                }
            }
            PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN -> {
                val directions = mutableListOf<Pair<Int, Int>>()
                if (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN) {
                    directions.addAll(listOf(Pair(1, 1), Pair(1, -1), Pair((-1), 1), Pair((-1), -1)))
                }
                if (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN) {
                    directions.addAll(listOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1)))
                }

                for ((df, dr) in directions) {
                    var current = Square(square.file + df, square.rank + dr)
                    while (current.file in 0..7 && current.rank in 0..7) {
                        val p = position.pieceAt(current)
                        if (p == null) {
                            moves.add(Move(square, current))
                        } else {
                            if (p.color != piece.color) {
                                moves.add(Move(square, current))
                            }
                            break
                        }
                        current = Square(current.file + df, current.rank + dr)
                    }
                }
            }
        }
        return moves
    }

    private fun addPawnMoves(from: Square, to: Square, color: Color, moves: MutableList<Move>) {
        val promotionRank = if (color == Color.WHITE) 7 else 0
        if (to.rank == promotionRank) {
            listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { promo ->
                moves.add(Move(from, to, promotion = promo))
            }
        } else {
            moves.add(Move(from, to))
        }
    }

    private fun canAttack(position: BoardPosition, from: Square, to: Square, piece: Piece): Boolean {
        val df = to.file - from.file
        val dr = to.rank - from.rank

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == Color.WHITE) 1 else -1
                return dr == dir && kotlin.math.abs(df) == 1
            }
            PieceType.KNIGHT -> {
                return (kotlin.math.abs(df) == 2 && kotlin.math.abs(dr) == 1) || (kotlin.math.abs(df) == 1 && kotlin.math.abs(dr) == 2)
            }
            PieceType.KING -> {
                return kotlin.math.abs(df) <= 1 && kotlin.math.abs(dr) <= 1
            }
            PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN -> {
                if (piece.type == PieceType.BISHOP && kotlin.math.abs(df) != kotlin.math.abs(dr)) return false
                if (piece.type == PieceType.ROOK && df != 0 && dr != 0) return false
                if (piece.type == PieceType.QUEEN && kotlin.math.abs(df) != kotlin.math.abs(dr) && df != 0 && dr != 0) return false

                val stepF = if (df == 0) 0 else df / kotlin.math.abs(df)
                val stepR = if (dr == 0) 0 else dr / kotlin.math.abs(dr)

                var curF = from.file + stepF
                var curR = from.rank + stepR
                while (curF != to.file || curR != to.rank) {
                    if (position.pieceAt(Square(curF, curR)) != null) return false
                    curF += stepF
                    curR += stepR
                }
                return true
            }
        }
    }

    private fun isSquareAttacked(position: BoardPosition, square: Square, attackerColor: Color): Boolean {
        for ((sq, piece) in position.squares) {
            if (piece.color == attackerColor) {
                if (canAttack(position, sq, square, piece)) return true
            }
        }
        return false
    }

    fun applyMove(position: BoardPosition, move: Move): BoardPosition {
        return applyMoveUnsafe(position, move)
    }

    private fun applyMoveUnsafe(position: BoardPosition, move: Move): BoardPosition {
        val piece = position.pieceAt(move.from) ?: return position
        val targetPiece = position.pieceAt(move.to)
        var newSquares = position.squares.toMutableMap()

        newSquares.remove(move.from)

        var newEpSquare: Square? = null
        var halfmove = position.halfmoveClock + 1

        if (piece.type == PieceType.PAWN) {
            halfmove = 0
            // En passant schlagen
            if (move.to == position.enPassantSquare && targetPiece == null) {
                val capturedPawnSquare = Square(move.to.file, move.from.rank)
                newSquares.remove(capturedPawnSquare)
            }
            // Doppelter Bauerntritt -> Ep-Feld setzen
            if (kotlin.math.abs(move.to.rank - move.from.rank) == 2) {
                val midRank = (move.from.rank + move.to.rank) / 2
                newEpSquare = Square(move.from.file, midRank)
            }
            // Umwandlung
            if (move.promotion != null) {
                newSquares[move.to] = Piece(move.promotion, piece.color)
            } else {
                newSquares[move.to] = piece
            }
        } else {
            if (targetPiece != null) halfmove = 0
            newSquares[move.to] = piece
        }

        // Rochade Handling
        var newCastling = position.castlingRights
        if (piece.type == PieceType.KING) {
            if (piece.color == Color.WHITE) {
                newCastling = newCastling.copy(whiteKingside = false, whiteQueenside = false)
                if (move.from == Square(4, 0) && move.to == Square(6, 0)) {
                    // Weiß kurze Rochade
                    val rook = newSquares.remove(Square(7, 0))
                    if (rook != null) newSquares[Square(5, 0)] = rook
                } else if (move.from == Square(4, 0) && move.to == Square(2, 0)) {
                    // Weiß lange Rochade
                    val rook = newSquares.remove(Square(0, 0))
                    if (rook != null) newSquares[Square(3, 0)] = rook
                }
            } else {
                newCastling = newCastling.copy(blackKingside = false, blackQueenside = false)
                if (move.from == Square(4, 7) && move.to == Square(6, 7)) {
                    // Schwarz kurze Rochade
                    val rook = newSquares.remove(Square(7, 7))
                    if (rook != null) newSquares[Square(5, 7)] = rook
                } else if (move.from == Square(4, 7) && move.to == Square(2, 7)) {
                    // Schwarz lange Rochade
                    val rook = newSquares.remove(Square(0, 7))
                    if (rook != null) newSquares[Square(3, 7)] = rook
                }
            }
        }

        // Turm-Bewegung / Schlagen annulliert Rochaderecht
        if (move.from == Square(0, 0) || move.to == Square(0, 0)) newCastling = newCastling.copy(whiteQueenside = false)
        if (move.from == Square(7, 0) || move.to == Square(7, 0)) newCastling = newCastling.copy(whiteKingside = false)
        if (move.from == Square(0, 7) || move.to == Square(0, 7)) newCastling = newCastling.copy(blackQueenside = false)
        if (move.from == Square(7, 7) || move.to == Square(7, 7)) newCastling = newCastling.copy(blackKingside = false)

        val nextColor = position.activeColor.opposite()
        val fullmove = if (position.activeColor == Color.BLACK) position.fullmoveNumber + 1 else position.fullmoveNumber

        return BoardPosition(
            squares = newSquares,
            activeColor = nextColor,
            castlingRights = newCastling,
            enPassantSquare = newEpSquare,
            halfmoveClock = halfmove,
            fullmoveNumber = fullmove
        )
    }

    private fun hasInsufficientMaterial(position: BoardPosition): Boolean {
        val pieces = position.squares.values
        if (pieces.size > 4) return false

        val nonKings = pieces.filter { it.type != PieceType.KING }
        if (nonKings.isEmpty()) return true // Nur Könige

        if (nonKings.size == 1) {
            val p = nonKings[0]
            if (p.type == PieceType.BISHOP || p.type == PieceType.KNIGHT) return true
        }

        if (nonKings.size == 2) {
            // Zwei Läufer auf gleicher Feld-Farbe oder König + Läufer gegen König + Läufer
            if (nonKings.all { it.type == PieceType.BISHOP }) {
                val bishops = position.squares.entries.filter { it.value.type == PieceType.BISHOP }
                val sq1 = bishops[0].key
                val sq2 = bishops[1].key
                val color1 = (sq1.file + sq1.rank) % 2
                val color2 = (sq2.file + sq2.rank) % 2
                if (color1 == color2) return true
            }
        }

        return false
    }

    private fun isThreefoldRepetition(current: BoardPosition, history: List<BoardPosition>): Boolean {
        if (history.isEmpty()) return false
        val currentFen = current.toFen().substringBeforeLast(" ") // Ohne Halbzug/Volllzug-Zähler
        var count = 0
        for (pos in history) {
            if (pos.toFen().substringBeforeLast(" ") == currentFen) {
                count++
            }
        }
        return count >= 2 // Aktuelle Stellung eingerechnet
    }
}
