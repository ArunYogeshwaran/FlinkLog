package com.ayogeshwaran.workoutlogger.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.usecase.AddWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetDatesWithWorkoutsUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetWorkoutsForDateUseCase
import com.ayogeshwaran.workoutlogger.presentation.home.todayMidnight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class HistoryViewMode {
    WEEKLY, MONTHLY
}

data class HistoryUiState(
    val displayedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val displayedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedDate: Long = todayMidnight(),
    val viewMode: HistoryViewMode = HistoryViewMode.MONTHLY
)

sealed class HistoryEvent {
    data class WorkoutDeleted(val entry: WorkoutEntry) : HistoryEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getWorkoutsForDateUseCase: GetWorkoutsForDateUseCase,
    getDatesWithWorkoutsUseCase: GetDatesWithWorkoutsUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val addWorkoutUseCase: AddWorkoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HistoryEvent>()
    val events = _events.asSharedFlow()

    val datesWithWorkouts: StateFlow<Set<Long>> = getDatesWithWorkoutsUseCase()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val workoutsForSelectedDate: StateFlow<List<WorkoutEntry>> = _uiState
        .flatMapLatest { state ->
            val endOfDay = state.selectedDate + 24 * 60 * 60 * 1000L
            val startOfDay = if (state.viewMode == HistoryViewMode.WEEKLY) {
                state.selectedDate - 6 * 24 * 60 * 60 * 1000L
            } else {
                state.selectedDate
            }
            getWorkoutsForDateUseCase(startOfDay, endOfDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setViewMode(viewMode: HistoryViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = viewMode)
    }

    fun adjustSelectedDate(days: Int) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = _uiState.value.selectedDate
        cal.add(Calendar.DAY_OF_YEAR, days)
        _uiState.value = _uiState.value.copy(
            selectedDate = cal.timeInMillis,
            displayedYear = cal.get(Calendar.YEAR),
            displayedMonth = cal.get(Calendar.MONTH)
        )
    }

    fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        _uiState.value = _uiState.value.copy(
            selectedDate = cal.timeInMillis,
            displayedYear = year,
            displayedMonth = month
        )
    }

    fun previousMonth() {
        val state = _uiState.value
        val cal = Calendar.getInstance()
        cal.set(state.displayedYear, state.displayedMonth, 1)
        cal.add(Calendar.MONTH, -1)
        _uiState.value = _uiState.value.copy(
            displayedYear = cal.get(Calendar.YEAR),
            displayedMonth = cal.get(Calendar.MONTH)
        )
    }

    fun nextMonth() {
        val state = _uiState.value
        val cal = Calendar.getInstance()
        cal.set(state.displayedYear, state.displayedMonth, 1)
        cal.add(Calendar.MONTH, 1)
        _uiState.value = _uiState.value.copy(
            displayedYear = cal.get(Calendar.YEAR),
            displayedMonth = cal.get(Calendar.MONTH)
        )
    }

    fun deleteWorkout(entry: WorkoutEntry) {
        viewModelScope.launch {
            deleteWorkoutUseCase(entry)
            _events.emit(HistoryEvent.WorkoutDeleted(entry))
        }
    }

    fun undoDelete(entry: WorkoutEntry) {
        viewModelScope.launch {
            addWorkoutUseCase(entry)
        }
    }

    fun updateWorkoutNote(entry: WorkoutEntry, newNote: String) {
        viewModelScope.launch {
            addWorkoutUseCase(entry.copy(notes = newNote))
        }
    }

    class Factory(
        private val getWorkoutsForDateUseCase: GetWorkoutsForDateUseCase,
        private val getDatesWithWorkoutsUseCase: GetDatesWithWorkoutsUseCase,
        private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
        private val addWorkoutUseCase: AddWorkoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(
                getWorkoutsForDateUseCase,
                getDatesWithWorkoutsUseCase,
                deleteWorkoutUseCase,
                addWorkoutUseCase
            ) as T
        }
    }
}

