package com.chessacademy.chess.pgn

/**
 * Exporter zur Umwandlung von [PgnGame]-Objekten in standardkonformen PGN-Text.
 * Berücksichtigt Header-Reihenfolge, Zeilenumbruchbegrenzung und Zugnotationen.
 */
class PgnExporter {

    fun exportGame(game: PgnGame): String {
        val sb = StringBuilder()

        // Standard-Header in bevorzugter Reihenfolge ausgeben
        val standardKeys = listOf("Event", "Site", "Date", "Round", "White", "Black", "Result", "ECO")
        val writtenKeys = mutableSetOf<String>()

        for (key in standardKeys) {
            game.headers[key]?.let { value ->
                sb.append("[$key \"$value\"]\n")
                writtenKeys.add(key)
            }
        }

        // Alle weiteren benutzerdefinierten Header
        for ((key, value) in game.headers) {
            if (!writtenKeys.contains(key)) {
                sb.append("[$key \"$value\"]\n")
            }
        }

        // Falls kein Result-Header existiert, hinzufügen
        if (!game.headers.containsKey("Result")) {
            sb.append("[Result \"${game.result}\"]\n")
        }

        sb.append("\n")

        // Zugfolge formatieren
        val moveString = buildMoveString(game.moves, game.result)
        sb.append(wrapLine(moveString, 80))
        sb.append("\n")

        return sb.toString()
    }

    private fun buildMoveString(moves: List<PgnMoveNode>, result: String): String {
        val sb = StringBuilder()
        var lastMoveNumber = -1

        for (node in moves) {
            if (node.moveNumber != lastMoveNumber || node.isWhiteMove) {
                if (node.isWhiteMove) {
                    sb.append("${node.moveNumber}. ")
                } else if (lastMoveNumber != node.moveNumber) {
                    sb.append("${node.moveNumber}... ")
                }
                lastMoveNumber = node.moveNumber
            }
            sb.append("${node.sanMove} ")
        }
        sb.append(result)
        return sb.toString().trim()
    }

    private fun wrapLine(text: String, maxLineLength: Int): String {
        val words = text.split(" ")
        val sb = StringBuilder()
        var currentLineLength = 0

        for (word in words) {
            if (currentLineLength + word.length + 1 > maxLineLength) {
                sb.append("\n")
                currentLineLength = 0
            } else if (currentLineLength > 0) {
                sb.append(" ")
                currentLineLength++
            }
            sb.append(word)
            currentLineLength += word.length
        }
        return sb.toString()
    }
}
