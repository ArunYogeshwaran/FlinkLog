package com.ayogeshwaran.workoutlogger.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.presentation.home.FakeWorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutAppFunctionsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeWorkoutRepository
    private lateinit var appFunctions: WorkoutAppFunctions
    private val fakeContext = org.mockito.Mockito.mock(AppFunctionContext::class.java)

    @Before
    fun setUp() {
        repository = FakeWorkoutRepository()
        appFunctions = WorkoutAppFunctions(repository)
    }

    @Test
    fun logWorkout_insertsWorkoutSuccessfully() = runTest(testDispatcher) {
        val timestamp = System.currentTimeMillis()
        val success = appFunctions.logWorkout(
            appFunctionContext = fakeContext,
            workoutType = "Running",
            notes = "Running 5km",
            timestamp = timestamp
        )

        assertTrue(success)

        val inserted = repository.workouts.first()
        assertEquals(1, inserted.size)
        assertEquals("Running", inserted[0].workoutType)
        assertEquals("Running 5km", inserted[0].notes)
        assertEquals(timestamp, inserted[0].timestamp)
    }

    @Test
    fun getWorkoutsForRange_returnsWorkoutsInRange() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600 * 1000
        val twoHoursAgo = now - 7200 * 1000

        repository.insertWorkout(
            WorkoutEntry(
                id = 1,
                workoutCategory = WorkoutCategory.CARDIO,
                workoutType = "Running",
                date = oneHourAgo,
                timestamp = oneHourAgo,
                createdAt = oneHourAgo,
                notes = "Running"
            )
        )
        repository.insertWorkout(
            WorkoutEntry(
                id = 2,
                workoutCategory = WorkoutCategory.GYM,
                workoutType = "Chest",
                date = twoHoursAgo,
                timestamp = twoHoursAgo,
                createdAt = twoHoursAgo,
                notes = "Chest press"
            )
        )

        val results = appFunctions.getWorkoutsForRange(
            appFunctionContext = fakeContext,
            startTimeMillis = twoHoursAgo - 1000,
            endTimeMillis = now + 1000
        )

        assertEquals(2, results.size)
        // sorted by descending timestamp
        assertEquals("Running", results[0].type)
        assertEquals("Chest", results[1].type)
    }

    @Test
    fun getCustomWorkoutTypes_returnsCustomTypes() = runTest(testDispatcher) {
        repository.insertCustomWorkoutType("Yoga", WorkoutCategory.CARDIO)
        repository.insertCustomWorkoutType("Crossfit", WorkoutCategory.GYM)

        val results = appFunctions.getCustomWorkoutTypes(fakeContext)

        assertEquals(2, results.size)
        assertTrue(results.contains("Yoga"))
        assertTrue(results.contains("Crossfit"))
    }

    @Test
    fun suggestWorkout_leastRecent_returnsNeglectedWorkout() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()

        // Log "Chest" recently
        repository.insertWorkout(
            WorkoutEntry(
                id = 1,
                workoutCategory = WorkoutCategory.GYM,
                workoutType = "Chest",
                date = now - 24 * 3600 * 1000,
                timestamp = now - 24 * 3600 * 1000,
                createdAt = now,
                notes = ""
            )
        )

        // Suggest using LEAST_RECENT
        val suggestion = appFunctions.suggestWorkout(fakeContext, "LEAST_RECENT")

        // "Chest" was done recently, so something else should be suggested
        assertTrue(suggestion.workoutType != "Chest")
        assertTrue(
            suggestion.rationale.contains("You haven't logged") || suggestion.rationale.contains(
                "You haven't trained"
            )
        )
    }

    @Test
    fun suggestWorkout_weekdayPattern_suggestsFrequentWeekdayWorkout() = runTest(testDispatcher) {
        val today = Calendar.getInstance()
        val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        // Log "Arms" on the same weekday 7 days ago, 14 days ago, and 21 days ago
        for (i in 1..3) {
            val logTime = today.timeInMillis - (7L * i * 24 * 3600 * 1000)
            repository.insertWorkout(
                WorkoutEntry(
                    id = i,
                    workoutCategory = WorkoutCategory.GYM,
                    workoutType = "Arms",
                    date = logTime,
                    timestamp = logTime,
                    createdAt = logTime,
                    notes = ""
                )
            )
        }

        // Suggest using WEEKDAY_PATTERN
        val suggestion = appFunctions.suggestWorkout(fakeContext, "WEEKDAY_PATTERN")

        assertEquals("Arms", suggestion.workoutType)
        assertTrue(suggestion.rationale.contains("You typically train Arms on this day of the week"))
    }
}
