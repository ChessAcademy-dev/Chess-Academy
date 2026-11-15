package com.chessacademy.core.database.di

import android.content.Context
import androidx.room.Room
import com.chessacademy.core.database.ChessAcademyDatabase
import com.chessacademy.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-Modul zur Bereitstellung der Room-Datenbank und aller DAOs 
 * im globalen Singleton-Scope der Chess Academy.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChessAcademyDatabase(
        @ApplicationContext context: Context
    ): ChessAcademyDatabase {
        return Room.databaseBuilder(
            context,
            ChessAcademyDatabase::class.java,
            ChessAcademyDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Sicherer Fallback bei Schema-Änderungen in der Entwicklungsphase
            .build()
    }

    @Provides
    fun provideProfileDao(database: ChessAcademyDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    fun provideGameDao(database: ChessAcademyDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    fun provideTrainingProgressDao(database: ChessAcademyDatabase): TrainingProgressDao {
        return database.trainingProgressDao()
    }

    @Provides
    fun provideCustomThemeDao(database: ChessAcademyDatabase): CustomThemeDao {
        return database.customThemeDao()
    }

    @Provides
    fun provideRepertoireDao(database: ChessAcademyDatabase): RepertoireDao {
        return database.repertoireDao()
    }
}
