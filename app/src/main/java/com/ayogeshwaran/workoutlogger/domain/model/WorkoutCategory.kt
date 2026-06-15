package com.ayogeshwaran.workoutlogger.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ayogeshwaran.workoutlogger.R

enum class WorkoutCategory(@param:StringRes val displayNameRes: Int) {
    CARDIO(R.string.category_cardio),
    GYM(R.string.category_gym);

    companion object {
        fun fromString(value: String): WorkoutCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CARDIO
        }
    }
}

data class WorkoutType(
    val name: String,
    val emoji: String,
    val category: WorkoutCategory,
    @param:StringRes val nameRes: Int
)

@Composable
fun WorkoutType.localizedName(): String {
    return if (nameRes != 0) {
        stringResource(nameRes)
    } else {
        name
    }
}

@Composable
fun WorkoutEntry.localizedType(): String {
    val preset = PresetWorkoutTypes.find { it.name.equals(workoutType, ignoreCase = true) }
    return preset?.localizedName() ?: workoutType
}

val PresetWorkoutTypes = listOf(
    // Cardio & General
    WorkoutType("Running", "🏃", WorkoutCategory.CARDIO, R.string.workout_type_running),
    WorkoutType("Cycling", "🚴", WorkoutCategory.CARDIO, R.string.workout_type_cycling),
    WorkoutType("Swimming", "🏊", WorkoutCategory.CARDIO, R.string.workout_type_swimming),
    WorkoutType("Walking", "🚶", WorkoutCategory.CARDIO, R.string.workout_type_walking),
    WorkoutType("HIIT", "⚡", WorkoutCategory.CARDIO, R.string.workout_type_hiit),
    WorkoutType("Yoga", "🧘", WorkoutCategory.CARDIO, R.string.workout_type_yoga),
    WorkoutType("Stretching", "🤸", WorkoutCategory.CARDIO, R.string.workout_type_stretching),

    // Gym / Muscle Groups
    WorkoutType("Chest", "🫁", WorkoutCategory.GYM, R.string.workout_type_chest),
    WorkoutType("Back", "🔙", WorkoutCategory.GYM, R.string.workout_type_back),
    WorkoutType("Shoulders", "💪", WorkoutCategory.GYM, R.string.workout_type_shoulders),
    WorkoutType("Biceps", "💪", WorkoutCategory.GYM, R.string.workout_type_biceps),
    WorkoutType("Triceps", "💪", WorkoutCategory.GYM, R.string.workout_type_triceps),
    WorkoutType("Legs", "🦵", WorkoutCategory.GYM, R.string.workout_type_legs),
    WorkoutType("Abs", "🧱", WorkoutCategory.GYM, R.string.workout_type_abs),
    WorkoutType("Full Body", "🏋️", WorkoutCategory.GYM, R.string.workout_type_full_body)
)
