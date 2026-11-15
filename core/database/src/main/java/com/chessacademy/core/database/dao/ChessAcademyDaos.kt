package com.chessacademy.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chessacademy.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: Long): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)
}

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getGamesForProfile(profileId: Long): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Delete
    suspend fun deleteGame(game: GameEntity)
}

@Dao
interface TrainingProgressDao {
    @Query("SELECT * FROM training_progress WHERE profileId = :profileId")
    fun getTrainingProgressForProfile(profileId: Long): Flow<List<TrainingProgressEntity>>

    @Query("SELECT * FROM training_progress WHERE profileId = :profileId AND nextReview <= :currentTime")
    suspend fun getItemsDueForReview(profileId: Long, currentTime: Long): List<TrainingProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: TrainingProgressEntity)
}

@Dao
interface CustomThemeDao {
    @Query("SELECT * FROM custom_themes WHERE profileId = :profileId")
    fun getThemesForProfile(profileId: Long): Flow<List<CustomThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: CustomThemeEntity): Long

    @Delete
    suspend fun deleteTheme(theme: CustomThemeEntity)
}

@Dao
interface RepertoireDao {
    @Query("SELECT * FROM repertoires WHERE profileId = :profileId AND color = :color")
    fun getRepertoireForColor(profileId: Long, color: String): Flow<List<RepertoireEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepertoire(repertoire: RepertoireEntity): Long

    @Delete
    suspend fun deleteRepertoire(repertoire: RepertoireEntity)
}
