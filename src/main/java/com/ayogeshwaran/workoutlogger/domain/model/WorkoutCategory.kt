package com.ayogeshwaran.workoutlogger.domain.model

enum class WorkoutCategory(val displayName: String) {
    CARDIO("Cardio & General"),
    GYM("Gym / Muscle Groups");

    companion object {
        fun fromString(value: String): WorkoutCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CARDIO
        }
    }
}

data class WorkoutType(
    val name: String,
    val emoji: String,
    val category: WorkoutCategory
)

val PresetWorkoutTypes = listOf(
    // Cardio & General
    WorkoutType("Running", "🏃", WorkoutCategory.CARDIO),
    WorkoutType("Cycling", "🚴", WorkoutCategory.CARDIO),
    WorkoutType("Swimming", "🏊", WorkoutCategory.CARDIO),
    WorkoutType("Walking", "🚶", WorkoutCategory.CARDIO),
    WorkoutType("HIIT", "⚡", WorkoutCategory.CARDIO),
    WorkoutType("Yoga", "🧘", WorkoutCategory.CARDIO),
    WorkoutType("Stretching", "🤸", WorkoutCategory.CARDIO),

    // Gym / Muscle Groups
    WorkoutType("Chest", "🫁", WorkoutCategory.GYM),
    WorkoutType("Back", "🔙", WorkoutCategory.GYM),
    WorkoutType("Shoulders", "💪", WorkoutCategory.GYM),
    WorkoutType("Biceps", "💪", WorkoutCategory.GYM),
    WorkoutType("Triceps", "💪", WorkoutCategory.GYM),
    WorkoutType("Legs", "🦵", WorkoutCategory.GYM),
    WorkoutType("Abs", "🧱", WorkoutCategory.GYM),
    WorkoutType("Full Body", "🏋️", WorkoutCategory.GYM)
)

