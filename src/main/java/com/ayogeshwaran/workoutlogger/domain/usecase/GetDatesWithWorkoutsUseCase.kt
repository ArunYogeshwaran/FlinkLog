package com.ayogeshwaran.workoutlogger.domain.usecase

import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class GetDatesWithWorkoutsUseCase(private val repository: WorkoutRepository) {

    operator fun invoke(): Flow<List<Long>> {
        return repository.getDatesWithWorkouts()
    }
}

