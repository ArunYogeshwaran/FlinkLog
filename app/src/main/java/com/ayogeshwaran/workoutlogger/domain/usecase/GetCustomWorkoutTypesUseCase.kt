package com.ayogeshwaran.workoutlogger.domain.usecase

import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class GetCustomWorkoutTypesUseCase(private val repository: WorkoutRepository) {

    operator fun invoke(): Flow<List<WorkoutType>> {
        return repository.getCustomWorkoutTypes()
    }
}
