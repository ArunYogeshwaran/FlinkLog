package com.ayogeshwaran.workoutlogger.domain.usecase

import androidx.paging.PagingData
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class GetPagedWorkoutsUseCase(private val repository: WorkoutRepository) {
    operator fun invoke(): Flow<PagingData<WorkoutEntry>> {
        return repository.getPagedWorkouts()
    }
}
