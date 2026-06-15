package com.ayogeshwaran.workoutlogger.domain.usecase

import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository

class AddCustomWorkoutTypeUseCase(private val repository: WorkoutRepository) {

    suspend operator fun invoke(name: String, category: WorkoutCategory) {
        repository.insertCustomWorkoutType(name, category)
    }
}
