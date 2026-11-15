package com.chessacademy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val profileId: Long,
    val pgn: String,
    val whitePlayer: String,
    val blackPlayer: String,
    val result: String, // "1-0", "0-1", "1/2-1/2", "*"
    val timestamp: Long = System.currentTimeMillis(),
    val openingName: String = "Unbekannte Eröffnung",
    val isFavorite: Boolean = false,
    val analysisDataJson: String? = null
)
