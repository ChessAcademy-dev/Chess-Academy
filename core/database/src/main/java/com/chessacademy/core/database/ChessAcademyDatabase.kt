package com.chessacademy.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chessacademy.core.database.dao.*
import com.chessacademy.core.database.entity.*

@Database(
    entities = [
        ProfileEntity::class,
        GameEntity::class,
        TrainingProgressEntity::class,
        CustomThemeEntity::class,
        RepertoireEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ChessAcademyDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun gameDao(): GameDao
    abstract fun trainingProgressDao(): TrainingProgressDao
    abstract fun customThemeDao(): CustomThemeDao
    abstract fun repertoireDao(): RepertoireDao

    companion object {
        const val DATABASE_NAME = "chess_academy.db"
    }
}
