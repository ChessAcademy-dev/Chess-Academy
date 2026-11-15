package com.chessacademy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_progress")
data class TrainingProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val profileId: Long,
    val puzzleId: String,
    val category: String, // "tactic", "endgame", "defense", etc.
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val lastReviewed: Long = System.currentTimeMillis(),
    val nextReview: Long = System.currentTimeMillis(),
    val averageTimeMs: Long = 0L
)
