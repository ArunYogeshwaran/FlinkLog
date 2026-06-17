package com.ayogeshwaran.workoutlogger.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.PresetWorkoutTypes
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

@AppFunctionSerializable(isDescribedByKDoc = true)
data class WorkoutEntrySerializable(
    /** The database unique identifier of the workout log entry. */
    val id: Long,
    /** The name of the workout type (e.g. "Running", "Chest"). */
    val type: String,
    /** The custom notes or details associated with the workout log. */
    val notes: String,
    /** The epoch millisecond timestamp representing when the workout was logged. */
    val timestamp: Long
)

@AppFunctionSerializable(isDescribedByKDoc = true)
data class WorkoutSuggestionSerializable(
    /** The recommended name/type of the workout (e.g., "Legs" or "Chest"). */
    val workoutType: String,
    /** The reason/explanation for this specific recommendation. */
    val rationale: String
)

/**
 * Exposes core workout tracking functionalities to system-level AI agents.
 */
class WorkoutAppFunctions(
    private val repository: WorkoutRepository
) {

    /**
     * Log a new workout activity with optional notes and custom timestamp.
     *
     * @param appFunctionContext The execution context.
     * @param workoutType The name of the workout type (e.g. "Running", "Cycling", "Chest").
     * @param notes Optional descriptive notes or weight/set details. If null, defaults to empty.
     * @param timestamp Optional custom logging time in epoch milliseconds. If null, defaults to current time.
     * @return True if the workout was logged successfully.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun logWorkout(
        appFunctionContext: AppFunctionContext,
        workoutType: String,
        notes: String?,
        timestamp: Long?
    ): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val timestampToUse = timestamp ?: now

        // Find or map the category
        val category = findCategoryForType(workoutType)

        // Find date midnight for this timestamp to group correctly in calendar
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampToUse
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateMidnight = cal.timeInMillis

        val entry = WorkoutEntry(
            workoutCategory = category,
            workoutType = workoutType,
            date = dateMidnight,
            timestamp = timestampToUse,
            createdAt = now,
            notes = notes ?: ""
        )
        repository.insertWorkout(entry)
        true
    }

    /**
     * Retrieve logged workouts for a specific date range.
     * Required workflow: Call this before attempting to view or analyze a user's recent activity logs.
     *
     * @param appFunctionContext The execution context.
     * @param startTimeMillis The start time of the range in epoch milliseconds.
     * @param endTimeMillis The end time of the range in epoch milliseconds.
     * @return A list of [WorkoutEntrySerializable] objects containing workout logs in the range.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getWorkoutsForRange(
        appFunctionContext: AppFunctionContext,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<WorkoutEntrySerializable> = withContext(Dispatchers.IO) {
        val logs = repository.getWorkoutsInRange(startTimeMillis, endTimeMillis).first()
        logs.map {
            WorkoutEntrySerializable(
                id = it.id.toLong(),
                type = it.workoutType,
                notes = it.notes,
                timestamp = it.timestamp
            )
        }
    }

    /**
     * Retrieve all custom workout types created by the user.
     *
     * @param appFunctionContext The execution context.
     * @return A list of names of custom workout types.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getCustomWorkoutTypes(
        appFunctionContext: AppFunctionContext
    ): List<String> = withContext(Dispatchers.IO) {
        val customTypes = repository.getCustomWorkoutTypes().first()
        customTypes.map { it.name }
    }

    /**
     * Suggest a workout to perform today based on history and user preferences.
     * Required workflow: Call [getWorkoutsForRange] to obtain recent history before invoking suggestion logic if custom context is needed.
     *
     * @param appFunctionContext The execution context.
     * @param preferenceType The suggestion preference: "LEAST_RECENT" for long-neglected workouts, or "WEEKDAY_PATTERN" for typical weekly habits.
     * @return A [WorkoutSuggestionSerializable] recommendation.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun suggestWorkout(
        appFunctionContext: AppFunctionContext,
        preferenceType: String
    ): WorkoutSuggestionSerializable = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000

        // Fetch recent workouts (last 30 days)
        val recentWorkouts = repository.getWorkoutsInRange(thirtyDaysAgo, now).first()
        
        // Get all possible workout types (preset + custom)
        val customTypes = repository.getCustomWorkoutTypes().first().map { it.name }
        val allTypes = (PresetWorkoutTypes.map { it.name } + customTypes).distinct()

        if (preferenceType.equals("WEEKDAY_PATTERN", ignoreCase = true)) {
            // Find current weekday (e.g. Wednesday)
            val todayCal = Calendar.getInstance()
            val currentDayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK)

            // Find what workouts user logged on the same weekday in past 30 days
            val weekdayWorkouts = recentWorkouts.filter { log ->
                val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                logCal.get(Calendar.DAY_OF_WEEK) == currentDayOfWeek
            }

            if (weekdayWorkouts.isNotEmpty()) {
                val mostFrequentType = weekdayWorkouts
                    .groupBy { it.workoutType }
                    .maxByOrNull { it.value.size }?.key

                if (mostFrequentType != null) {
                    return@withContext WorkoutSuggestionSerializable(
                        workoutType = mostFrequentType,
                        rationale = "You typically train $mostFrequentType on this day of the week."
                    )
                }
            }
        }

        // Default or "LEAST_RECENT": find the workout type not logged for the longest time
        val lastLoggedMap = allTypes.associateWith { type ->
            recentWorkouts
                .filter { it.workoutType.equals(type, ignoreCase = true) }
                .maxOfOrNull { it.timestamp } ?: 0L
        }

        val leastRecentEntry = lastLoggedMap.minByOrNull { it.value }
        val suggestedType = leastRecentEntry?.key ?: "Running"
        val lastLoggedTime = leastRecentEntry?.value ?: 0L

        val rationale = if (lastLoggedTime == 0L) {
            "You haven't logged $suggestedType in the last 30 days."
        } else {
            val daysAgo = ((now - lastLoggedTime) / (24 * 60 * 60 * 1000)).toInt()
            "You haven't trained $suggestedType in $daysAgo days."
        }

        WorkoutSuggestionSerializable(
            workoutType = suggestedType,
            rationale = rationale
        )
    }


    private fun findCategoryForType(typeName: String): WorkoutCategory {
        val preset = PresetWorkoutTypes.find { it.name.equals(typeName, ignoreCase = true) }
        return preset?.category ?: WorkoutCategory.GYM
    }
}
