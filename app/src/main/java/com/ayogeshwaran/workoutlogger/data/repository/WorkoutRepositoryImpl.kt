package com.ayogeshwaran.workoutlogger.data.repository

import com.ayogeshwaran.workoutlogger.data.local.WorkoutDao
import com.ayogeshwaran.workoutlogger.data.local.entity.CustomWorkoutTypeEntity
import com.ayogeshwaran.workoutlogger.data.local.entity.WorkoutEntryEntity
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override suspend fun insertWorkout(entry: WorkoutEntry) {
        dao.insertWorkout(entry.toEntity())
    }

    override suspend fun deleteWorkout(entry: WorkoutEntry) {
        dao.deleteWorkout(entry.toEntity())
    }

    override fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntry>> {
        return dao.getWorkoutsForDate(startOfDay, endOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDatesWithWorkouts(): Flow<List<Long>> {
        return dao.getDatesWithWorkouts()
    }

    override fun getWorkoutsInRange(start: Long, end: Long): Flow<List<WorkoutEntry>> {
        return dao.getWorkoutsInRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCustomWorkoutType(name: String, category: WorkoutCategory) {
        dao.insertCustomWorkoutType(
            CustomWorkoutTypeEntity(
                name = name,
                category = category.name.lowercase()
            )
        )
    }

    override fun getCustomWorkoutTypes(): Flow<List<WorkoutType>> {
        return dao.getCustomWorkoutTypes().map { entities ->
            entities.map { entity ->
                WorkoutType(
                    name = entity.name,
                    emoji = "",
                    category = WorkoutCategory.fromString(entity.category),
                    nameRes = 0
                )
            }
        }
    }

    override suspend fun deleteCustomWorkoutType(name: String, category: WorkoutCategory) {
        dao.deleteCustomWorkoutTypeByName(name, category.name.lowercase())
    }

    private fun WorkoutEntry.toEntity(): WorkoutEntryEntity {
        return WorkoutEntryEntity(
            id = id,
            workoutCategory = workoutCategory.name.lowercase(),
            workoutType = workoutType,
            date = date,
            timestamp = timestamp,
            createdAt = createdAt,
            notes = notes
        )
    }

    private fun WorkoutEntryEntity.toDomain(): WorkoutEntry {
        return WorkoutEntry(
            id = id,
            workoutCategory = WorkoutCategory.fromString(workoutCategory),
            workoutType = workoutType,
            date = date,
            timestamp = timestamp,
            createdAt = createdAt,
            notes = notes
        )
    }
}

