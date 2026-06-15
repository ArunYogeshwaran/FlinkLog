package com.ayogeshwaran.workoutlogger.domain.repository

import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    suspend fun insertWorkout(entry: WorkoutEntry)

    suspend fun deleteWorkout(entry: WorkoutEntry)

    fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntry>>

    fun getDatesWithWorkouts(): Flow<List<Long>>

    fun getWorkoutsInRange(start: Long, end: Long): Flow<List<WorkoutEntry>>

    suspend fun insertCustomWorkoutType(name: String, category: WorkoutCategory)

    fun getCustomWorkoutTypes(): Flow<List<WorkoutType>>

    suspend fun deleteCustomWorkoutType(name: String, category: WorkoutCategory)
}

