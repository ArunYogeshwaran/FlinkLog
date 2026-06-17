package com.ayogeshwaran.workoutlogger.presentation.home

import com.ayogeshwaran.workoutlogger.domain.model.PresetWorkoutTypes
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.domain.repository.WorkoutRepository
import com.ayogeshwaran.workoutlogger.domain.usecase.AddCustomWorkoutTypeUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.AddWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteCustomWorkoutTypeUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetCustomWorkoutTypesUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetWorkoutsForDateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class FakeWorkoutRepository : WorkoutRepository {
    val workouts = MutableStateFlow<List<WorkoutEntry>>(emptyList())

    override suspend fun insertWorkout(entry: WorkoutEntry) {
        val currentList = workouts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == entry.id && entry.id != 0 }
        if (index != -1) {
            currentList[index] = entry
        } else {
            val id = if (entry.id == 0) (currentList.maxOfOrNull { it.id } ?: 0) + 1 else entry.id
            currentList.add(entry.copy(id = id))
        }
        workouts.value = currentList
    }

    override suspend fun deleteWorkout(entry: WorkoutEntry) {
        workouts.value = workouts.value.filterNot { it.id == entry.id }
    }

    override fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntry>> {
        return workouts.map { list ->
            list.filter { it.date >= startOfDay && it.date < endOfDay }
                .sortedByDescending { it.timestamp }
        }
    }

    override fun getDatesWithWorkouts(): Flow<List<Long>> {
        return workouts.map { list ->
            list.map { it.date }.distinct().sortedDescending()
        }
    }

    override fun getWorkoutsInRange(start: Long, end: Long): Flow<List<WorkoutEntry>> {
        return workouts.map { list ->
            list.filter { it.date >= start && it.date < end }
                .sortedByDescending { it.timestamp }
        }
    }

    val customWorkoutTypes = MutableStateFlow<List<WorkoutType>>(emptyList())

    override suspend fun insertCustomWorkoutType(name: String, category: WorkoutCategory) {
        val current = customWorkoutTypes.value.toMutableList()
        current.add(
            WorkoutType(
                name = name,
                emoji = "",
                category = category,
                nameRes = 0
            )
        )
        customWorkoutTypes.value = current
    }

    override fun getCustomWorkoutTypes(): Flow<List<WorkoutType>> {
        return customWorkoutTypes
    }

    override suspend fun deleteCustomWorkoutType(name: String, category: WorkoutCategory) {
        customWorkoutTypes.value =
            customWorkoutTypes.value.filterNot { it.name == name && it.category == category }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeWorkoutRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeWorkoutRepository()
        viewModel = HomeViewModel(
            addWorkoutUseCase = AddWorkoutUseCase(repository),
            deleteWorkoutUseCase = DeleteWorkoutUseCase(repository),
            getWorkoutsForDateUseCase = GetWorkoutsForDateUseCase(repository),
            addCustomWorkoutTypeUseCase = AddCustomWorkoutTypeUseCase(repository),
            getCustomWorkoutTypesUseCase = GetCustomWorkoutTypesUseCase(repository),
            deleteCustomWorkoutTypeUseCase = DeleteCustomWorkoutTypeUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun togglingWorkoutType_updatesSelectedWorkoutTypes() = runTest(testDispatcher) {
        val running = PresetWorkoutTypes.first { it.name == "Running" }

        viewModel.onWorkoutTypeToggled(running)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedWorkoutTypes.contains(running))

        viewModel.onWorkoutTypeToggled(running)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.selectedWorkoutTypes.contains(running))
    }

    @Test
    fun updatingCustomDate_updatesCustomTimestampAndSetsIsCustomDateTime() =
        runTest(testDispatcher) {
            viewModel.updateCustomDate(2026, Calendar.JUNE, 20)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isCustomDateTime)

            val cal = Calendar.getInstance().apply {
                timeInMillis = state.customTimestamp
            }
            assertEquals(2026, cal.get(Calendar.YEAR))
            assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH))
            assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
        }

    @Test
    fun updatingCustomTime_updatesCustomTimestampAndSetsIsCustomDateTime() =
        runTest(testDispatcher) {
            viewModel.updateCustomTime(15, 30)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isCustomDateTime)

            val cal = Calendar.getInstance().apply {
                timeInMillis = state.customTimestamp
            }
            assertEquals(15, cal.get(Calendar.HOUR_OF_DAY))
            assertEquals(30, cal.get(Calendar.MINUTE))
        }

    @Test
    fun logWorkoutWithCustomDateTime_savesWithCorrectMidnightAndTimestamp() =
        runTest(testDispatcher) {
            val running = PresetWorkoutTypes.first { it.name == "Running" }
            viewModel.onWorkoutTypeToggled(running)

            viewModel.updateCustomDate(2026, Calendar.JUNE, 20)
            viewModel.updateCustomTime(15, 30)
            testDispatcher.scheduler.advanceUntilIdle()

            val customTime = viewModel.uiState.value.customTimestamp

            viewModel.logWorkout()
            testDispatcher.scheduler.advanceUntilIdle()

            val savedWorkouts = repository.workouts.first()
            assertEquals(1, savedWorkouts.size)
            val saved = savedWorkouts[0]

            assertEquals("Running", saved.workoutType)
            assertEquals(customTime, saved.timestamp)

            val cal = Calendar.getInstance().apply {
                timeInMillis = customTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            assertEquals(cal.timeInMillis, saved.date)

            val state = viewModel.uiState.value
            assertTrue(state.selectedWorkoutTypes.isEmpty())
            assertFalse(state.isCustomDateTime)
        }

    @Test
    fun addCustomWorkoutType_successfullySavesAndPresentsCustomWorkout() = runTest(testDispatcher) {
        val collectJob = launch {
            viewModel.cardioTypes.collect {}
        }

        var successCalled = false
        viewModel.addCustomWorkoutType(
            name = "Zumba",
            category = WorkoutCategory.CARDIO,
            onSuccess = { successCalled = true },
            onError = {}
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(successCalled)

        val cardioList = viewModel.cardioTypes.value
        val hasZumba =
            cardioList.any { it.name == "Zumba" && it.category == WorkoutCategory.CARDIO }
        assertTrue(hasZumba)

        collectJob.cancel()
    }

    @Test
    fun addCustomWorkoutType_uniquenessValidation() = runTest(testDispatcher) {
        // Preset conflict
        var errorType: String? = null
        viewModel.addCustomWorkoutType(
            name = "Running", // running already exists in PresetWorkoutTypes
            category = WorkoutCategory.CARDIO,
            onSuccess = {},
            onError = { errorType = it }
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("exists", errorType)

        // Custom duplicate conflict
        viewModel.addCustomWorkoutType(
            name = "Kettlebell",
            category = WorkoutCategory.GYM,
            onSuccess = {},
            onError = {}
        )
        testDispatcher.scheduler.advanceUntilIdle()

        var dupError: String? = null
        viewModel.addCustomWorkoutType(
            name = "Kettlebell",
            category = WorkoutCategory.GYM,
            onSuccess = {},
            onError = { dupError = it }
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("exists", dupError)
    }

    @Test
    fun deleteCustomWorkoutType_successfullyRemovesWorkoutAndClearsUiSelection() =
        runTest(testDispatcher) {
            val collectJob = launch {
                viewModel.cardioTypes.collect {}
            }

            viewModel.addCustomWorkoutType(
                name = "Zumba",
                category = WorkoutCategory.CARDIO,
                onSuccess = {},
                onError = {}
            )
            testDispatcher.scheduler.advanceUntilIdle()

            val runningList = viewModel.cardioTypes.value
            val zumbaType = runningList.first { it.name == "Zumba" }

            viewModel.onWorkoutTypeToggled(zumbaType)
            assertTrue(viewModel.uiState.value.selectedWorkoutTypes.contains(zumbaType))

            viewModel.deleteCustomWorkoutType(zumbaType)
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedList = viewModel.cardioTypes.value
            assertFalse(updatedList.contains(zumbaType))
            assertFalse(viewModel.uiState.value.selectedWorkoutTypes.contains(zumbaType))

            collectJob.cancel()
        }
}
