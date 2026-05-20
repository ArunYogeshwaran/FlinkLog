package com.ayogeshwaran.workoutlogger.domain.model

data class WorkoutEntry(
    val id: Int = 0,
    val workoutCategory: WorkoutCategory,
    val workoutType: String,
    val date: Long,
    val timestamp: Long,
    val createdAt: Long
)

