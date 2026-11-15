package com.chessacademy.chess.rules

import org.junit.Assert.*
import org.junit.Test

class ChessRulesTest {

    @Test
    fun startingPosition_hasCorrectLegalMovesCount() {
        val pos = BoardPosition.startingPosition()
        val whiteMoves = ChessRules.getAllLegalMoves(pos, Color.WHITE)
        assertEquals(20, whiteMoves.size) // 16 Bauerntritte + 4 Springerzüge
    }

    @Test
    fun checkmateDetection_scholarsMate() {
        // Scholar's Mate Position (Schachmatt in 4 Zügen)
        var pos = BoardPosition.startingPosition()
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("e2")!!, Square.fromAlgebraic("e4")!!))
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("e7")!!, Square.fromAlgebraic("e5")!!))
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("d1")!!, Square.fromAlgebraic("h5")!!))
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("b8")!!, Square.fromAlgebraic("c6")!!))
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("f1")!!, Square.fromAlgebraic("c4")!!))
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("g8")!!, Square.fromAlgebraic("f6")!!))
        pos = ChessRules.applyMove(pos, Move(Square.fromAlgebraic("h5")!!, Square.fromAlgebraic("f7")!!))

        val status = ChessRules.getGameStatus(pos)
        assertEquals(GameStateStatus.CHECKMATE, status)
    }

    @Test
    fun castling_isRecognizedAndExecuted() {
        val fen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
        val pos = BoardPosition.fromFen(fen)!!
        val legalMoves = ChessRules.getLegalMoves(pos, Square.fromAlgebraic("e1")!!)
        
        // Weißer König auf e1 sollte kurze Rochade (g1) als legalen Zug haben
        val castlingMove = legalMoves.find { it.to == Square.fromAlgebraic("g1") }
        assertNotNull(castlingMove)

        val newPos = ChessRules.applyMove(pos, castlingMove!!)
        assertEquals(Piece(PieceType.KING, Color.WHITE), newPos.pieceAt(Square.fromAlgebraic("g1")!!))
        assertEquals(Piece(PieceType.ROOK, Color.WHITE), newPos.pieceAt(Square.fromAlgebraic("f1")!!))
    }

    @Test
    fun enPassant_isSupported() {
        val fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2"
        var pos = BoardPosition.fromFen(fen)!!
        // Weißer Bauer e4 schlägt e6 en passant
        val move = Move(Square.fromAlgebraic("e4")!!, Square.fromAlgebraic("e6")!!)
        val legalMoves = ChessRules.getLegalMoves(pos, Square.fromAlgebraic("e4")!!)
        assertTrue(legalMoves.contains(move))

        val newPos = ChessRules.applyMove(pos, move)
        assertNull(newPos.pieceAt(Square.fromAlgebraic("e5")!!))
        assertEquals(Piece(PieceType.PAWN, Color.WHITE), newPos.pieceAt(Square.fromAlgebraic("e6")!!))
    }

    @Test
    fun insufficientMaterial_isRecognized() {
        val fen = "8/8/8/8/8/8/8/k6K w - - 0 1" // Nur Könige
        val pos = BoardPosition.fromFen(fen)!!
        val status = ChessRules.getGameStatus(pos)
        assertEquals(GameStateStatus.DRAW_BY_MATERIAL, status)
    }
}
