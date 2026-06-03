package com.ayogeshwaran.workoutlogger.domain.usecase

import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository

class DeleteWorkoutUseCase(private val repository: WorkoutRepository) {

    suspend operator fun invoke(entry: WorkoutEntry) {
        repository.deleteWorkout(entry)
    }
}

