package com.chessacademy.chess.rules

import com.chessacademy.chess.pgn.PgnExporter
import com.chessacademy.chess.pgn.PgnGame
import com.chessacademy.chess.pgn.PgnMoveNode
import com.chessacademy.chess.pgn.PgnParser
import com.chessacademy.chess.board.BoardUtils
import org.junit.Assert.*
import org.junit.Test

class ChessIntegrationTest {

    @Test
    fun pgnImport_rulesValidation_andBoardSynchronization() {
        // 1. PGN-Text vorbereiten (Scholar's Mate)
        val pgnText = """
            [Event "Test Game"]
            [Site "Local"]
            [Date "2026.11.15"]
            [Round "1"]
            [White "Player 1"]
            [Black "Player 2"]
            [Result "1-0"]

            1. e4 e5 2. Bc4 Nc6 3. Qh5 Nf6 4. Qxf7# 1-0
        """.trimIndent()

        // 2. PGN einlesen über :chess:pgn Parser
        val parser = PgnParser()
        val parseResult = parser.parsePgn(pgnText)
        assertTrue(parseResult is com.chessacademy.core.common.Resource.Success)
        
        val games = (parseResult as com.chessacademy.core.common.Resource.Success).data
        assertEquals(1, games.size)
        val game = games[0]

        // 3. Partie über :chess:rules und SanResolver schrittweise abspielen und validieren
        var position = BoardPosition.startingPosition()
        val executedMoves = mutableListOf<PgnMoveNode>()

        for (node in game.moves) {
            val move = SanResolver.resolveSan(position, node.sanMove)
            assertNotNull("Zug ${node.sanMove} konnte nicht aufgelöst werden", move)

            val legalMoves = ChessRules.getLegalMoves(position, move!!.from)
            assertTrue("Zug ${node.sanMove} ist in dieser Stellung nicht legal", legalMoves.contains(move))

            // Zug ausführen
            position = ChessRules.applyMove(position, move)
            executedMoves.add(node)
        }

        // 4. Endstatus prüfen (Muss Schachmatt sein)
        val gameStatus = ChessRules.getGameStatus(position)
        assertEquals(GameStateStatus.CHECKMATE, gameStatus)

        // 5. Synchronisation mit :chess:board prüfen (FEN zu Pieces Konvertierung)
        val finalFen = position.toFen()
        val boardPieces = BoardUtils.fenToPieces(finalFen)
        assertFalse("Das Brett enthält Figuren nach dem Matt", boardPieces.isEmpty())

        // 6. Export zurück in PGN über :chess:pgn Exporter prüfen
        val exportedPgn = PgnExporter().exportGame(game)
        assertTrue(exportedPgn.contains("1. e4"))
        assertTrue(exportedPgn.contains("Qxf7"))
    }

    @Test
    fun illegalMove_isRejected() {
        val position = BoardPosition.startingPosition()
        // Springer von b1 nach e4 ist illegal in der Grundstellung
        val illegalMove = Move(Square.fromAlgebraic("b1")!!, Square.fromAlgebraic("e4")!!)
        val legalMoves = ChessRules.getLegalMoves(position, Square.fromAlgebraic("b1")!!)
        
        assertFalse(legalMoves.contains(illegalMove))
    }
}
