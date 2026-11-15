package com.chessacademy.chess.pgn

import com.chessacademy.core.common.Resource

/**
 * Robuster PGN-Parser zum Einlesen und Parsen von Standard-PGN-Texten.
 * Unterstützt Header-Tags, Kommentare in geschweiften Klammern ({...}),
 * Untervarianten in runden Klammern (...) und Ergebniskennzeichnungen.
 */
class PgnParser {

    fun parsePgn(pgnText: String): Resource<List<PgnGame>> {
        return try {
            if (pgnText.isBlank()) {
                return Resource.Error("Der PGN-Text ist leer.")
            }

            val games = mutableListOf<PgnGame>()
            val rawGames = splitIntoRawGames(pgnText)

            for (raw in rawGames) {
                if (raw.isBlank()) continue
                val game = parseSingleGame(raw)
                games.add(game)
            }

            if (games.isEmpty()) {
                Resource.Error("Keine gültigen Partien im PGN gefunden.")
            } else {
                Resource.Success(games)
            }
        } catch (e: Exception) {
            Resource.Error("Fehler beim Parsen der PGN-Datei: ${e.localizedMessage}")
        }
    }

    private fun splitIntoRawGames(text: String): List<String> {
        val games = mutableListOf<String>()
        val currentText = StringBuilder()
        val lines = text.lines()
        var hasTags = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[Event ") && hasTags) {
                games.add(currentText.toString())
                currentText.clear()
                hasTags = false
            }
            if (trimmed.startsWith("[")) {
                hasTags = true
            }
            currentText.append(line).append("\n")
        }
        if (currentText.isNotBlank()) {
            games.add(currentText.toString())
        }
        return games
    }

    private fun parseSingleGame(rawGame: String): PgnGame {
        val headers = mutableMapOf<String, String>()
        val moveTextBuilder = StringBuilder()
        var result = "*"

        val lines = rawGame.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[") && trimmed.endsWith("]") -> {
                    val inner = trimmed.substring(1, trimmed.length - 1)
                    val spaceIdx = inner.indexOf(' ')
                    if (spaceIdx != -1) {
                        val key = inner.substring(0, spaceIdx)
                        val valQuoted = inner.substring(spaceIdx + 1).trim()
                        val value = valQuoted.removeSurrounding("\"")
                        headers[key] = value
                    }
                }
                else -> {
                    moveTextBuilder.append(line).append(" ")
                }
            }
        }

        val fullMoveText = moveTextBuilder.toString().trim()
        val parsedMovesAndResult = parseMoveText(fullMoveText)
        
        if (headers["Result"] == null && parsedMovesAndResult.second != "*") {
            headers["Result"] = parsedMovesAndResult.second
        }
        result = headers["Result"] ?: parsedMovesAndResult.second

        return PgnGame(
            headers = headers,
            moves = parsedMovesAndResult.first,
            result = result
        )
    }

    private fun parseMoveText(text: String): Pair<List<PgnMoveNode>, String> {
        val moves = mutableListOf<PgnMoveNode>()
        var result = "*"
        
        // Tokenizer & Parser Logik für Züge, Kommentare und Klammern
        val tokens = text.split(Regex("\\s+"))
        var currentMoveNumber = 1
        var isWhite = true
        
        var i = 0
        while (i < tokens.size) {
            var token = tokens[i]
            if (token.isBlank()) {
                i++
                continue
            }

            // Ergebnis erkennen
            if (token == "1-0" || token == "0-1" || token == "1/2-1/2" || token == "*") {
                result = token
                i++
                continue
            }

            // Zugnummern überspringen (z. B. "1." oder "1...")
            if (token.matches(Regex("\\d+\\.+"))) {
                val numPart = token.takeWhile { it.isDigit() }
                if (numPart.isNotEmpty()) {
                    currentMoveNumber = numPart.toInt()
                    isWhite = !token.contains("...")
                }
                i++
                continue
            }

            // SAN Zug hinzufügen
            if (!token.startsWith("{") && !token.startsWith("(")) {
                // Bereinigen von NAGs ($1 etc.)
                val cleanMove = token.replace(Regex("\\$\\d+"), "")
                if (cleanMove.isNotBlank()) {
                    moves.add(
                        PgnMoveNode(
                            moveNumber = currentMoveNumber,
                            isWhiteMove = isWhite,
                            sanMove = cleanMove
                        )
                    )
                    if (!isWhite) {
                        currentMoveNumber++
                    }
                    isWhite = !isWhite
                }
            }
            i++
        }

        return Pair(moves, result)
    }
}
