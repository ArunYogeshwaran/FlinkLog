package com.ayogeshwaran.workoutlogger.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayogeshwaran.workoutlogger.domain.model.PresetWorkoutTypes
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.domain.usecase.AddWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetWorkoutsForDateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val selectedDate: Long = todayMidnight(),
    val selectedHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val selectedMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    val selectedWorkoutType: WorkoutType? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
)

sealed class HomeEvent {
    data class WorkoutLogged(val entry: WorkoutEntry) : HomeEvent()
    data class WorkoutDeleted(val entry: WorkoutEntry) : HomeEvent()
}

fun todayMidnight(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val addWorkoutUseCase: AddWorkoutUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val getWorkoutsForDateUseCase: GetWorkoutsForDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    val workoutsForDate: StateFlow<List<WorkoutEntry>> = _uiState
        .flatMapLatest { state ->
            val startOfDay = state.selectedDate
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
            getWorkoutsForDateUseCase(startOfDay, endOfDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cardioTypes = PresetWorkoutTypes.filter { it.category == WorkoutCategory.CARDIO }
    val gymTypes = PresetWorkoutTypes.filter { it.category == WorkoutCategory.GYM }

    fun onDateSelected(millis: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        _uiState.value = _uiState.value.copy(selectedDate = cal.timeInMillis, showDatePicker = false)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(selectedHour = hour, selectedMinute = minute, showTimePicker = false)
    }

    fun onWorkoutTypeSelected(type: WorkoutType?) {
        _uiState.value = _uiState.value.copy(selectedWorkoutType = type)
    }

    fun onShowDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    fun onShowTimePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTimePicker = show)
    }

    fun logWorkout() {
        val state = _uiState.value
        val workoutType = state.selectedWorkoutType ?: return

        val cal = Calendar.getInstance()
        cal.timeInMillis = state.selectedDate
        cal.set(Calendar.HOUR_OF_DAY, state.selectedHour)
        cal.set(Calendar.MINUTE, state.selectedMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val entry = WorkoutEntry(
            workoutCategory = workoutType.category,
            workoutType = workoutType.name,
            date = state.selectedDate,
            timestamp = cal.timeInMillis,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            addWorkoutUseCase(entry)
            _events.emit(HomeEvent.WorkoutLogged(entry))
        }

        _uiState.value = _uiState.value.copy(selectedWorkoutType = null)
    }

    fun deleteWorkout(entry: WorkoutEntry) {
        viewModelScope.launch {
            deleteWorkoutUseCase(entry)
            _events.emit(HomeEvent.WorkoutDeleted(entry))
        }
    }

    fun undoDelete(entry: WorkoutEntry) {
        viewModelScope.launch {
            addWorkoutUseCase(entry)
        }
    }

    class Factory(
        private val addWorkoutUseCase: AddWorkoutUseCase,
        private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
        private val getWorkoutsForDateUseCase: GetWorkoutsForDateUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(addWorkoutUseCase, deleteWorkoutUseCase, getWorkoutsForDateUseCase) as T
        }
    }
}

