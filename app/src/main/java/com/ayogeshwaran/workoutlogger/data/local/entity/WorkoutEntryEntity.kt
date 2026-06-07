package com.ayogeshwaran.workoutlogger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_entries")
data class WorkoutEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutCategory: String,
    val workoutType: String,
    val date: Long,
    val timestamp: Long,
    val createdAt: Long,
    val notes: String = ""
)

