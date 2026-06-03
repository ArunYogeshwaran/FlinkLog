package com.ayogeshwaran.workoutlogger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ayogeshwaran.workoutlogger.data.local.entity.WorkoutEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(entry: WorkoutEntryEntity)

    @Delete
    suspend fun deleteWorkout(entry: WorkoutEntryEntity)

    @Query("SELECT * FROM workout_entries WHERE date >= :startOfDay AND date < :endOfDay ORDER BY timestamp DESC")
    fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntryEntity>>

    @Query("SELECT DISTINCT date FROM workout_entries ORDER BY date DESC")
    fun getDatesWithWorkouts(): Flow<List<Long>>

    @Query("SELECT * FROM workout_entries WHERE date >= :start AND date < :end ORDER BY timestamp DESC")
    fun getWorkoutsInRange(start: Long, end: Long): Flow<List<WorkoutEntryEntity>>
}

