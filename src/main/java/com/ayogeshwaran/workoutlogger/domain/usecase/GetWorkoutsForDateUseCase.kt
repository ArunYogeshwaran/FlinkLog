package com.ayogeshwaran.workoutlogger.domain.usecase

import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class GetWorkoutsForDateUseCase(private val repository: WorkoutRepository) {

    operator fun invoke(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntry>> {
        return repository.getWorkoutsForDate(startOfDay, endOfDay)
    }
}

