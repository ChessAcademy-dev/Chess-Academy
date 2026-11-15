package com.chessacademy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val id: String,
    val whitePlayerName: String,
    val blackPlayerName: String,
    val result: String, // z.B. "1-0", "0-1", "1/2-1/2", "*"
    val date: String,
    val pgn: String = "",
    val timeControl: String = "Blitz (3+2)",
    val isCompleted: Boolean = true,
    val playerColor: String = "WHITE"
)
