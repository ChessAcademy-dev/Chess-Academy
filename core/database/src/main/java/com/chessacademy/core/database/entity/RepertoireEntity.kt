package com.chessacademy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repertoires")
data class RepertoireEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val profileId: Long,
    val color: String, // "white" oder "black"
    val openingName: String,
    val movesPgn: String,
    val notes: String = ""
)
