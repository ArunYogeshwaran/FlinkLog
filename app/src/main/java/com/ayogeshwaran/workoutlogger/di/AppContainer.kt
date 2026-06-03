package com.ayogeshwaran.workoutlogger.di

import android.content.Context
import com.ayogeshwaran.workoutlogger.data.local.WorkoutDatabase
import com.ayogeshwaran.workoutlogger.data.repository.WorkoutRepositoryImpl
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import com.ayogeshwaran.workoutlogger.domain.usecase.AddWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetDatesWithWorkoutsUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetWorkoutsForDateUseCase

class AppContainer(context: Context) {

    private val database = WorkoutDatabase.getInstance(context)
    private val dao = database.workoutDao()

    val repository: WorkoutRepository = WorkoutRepositoryImpl(dao)

    val addWorkoutUseCase = AddWorkoutUseCase(repository)
    val deleteWorkoutUseCase = DeleteWorkoutUseCase(repository)
    val getWorkoutsForDateUseCase = GetWorkoutsForDateUseCase(repository)
    val getDatesWithWorkoutsUseCase = GetDatesWithWorkoutsUseCase(repository)
}

