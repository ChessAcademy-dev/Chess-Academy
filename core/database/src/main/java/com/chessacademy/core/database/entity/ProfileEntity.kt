package com.chessacademy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val isGuest: Boolean = false,
    val isKidMode: Boolean = false,
    val eloTactics: Int = 1200,
    val eloStrategy: Int = 1200,
    val eloOpenings: Int = 1200,
    val eloEndgame: Int = 1200,
    val eloCalculation: Int = 1200,
    val eloBlitz: Int = 1200,
    val createdAt: Long = System.currentTimeMillis()
)
