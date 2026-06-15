package com.ayogeshwaran.workoutlogger.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayogeshwaran.workoutlogger.domain.model.PresetWorkoutTypes
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.domain.usecase.AddCustomWorkoutTypeUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.AddWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteCustomWorkoutTypeUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.DeleteWorkoutUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetCustomWorkoutTypesUseCase
import com.ayogeshwaran.workoutlogger.domain.usecase.GetWorkoutsForDateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val selectedWorkoutTypes: Set<WorkoutType> = emptySet(),
    val workoutNotesMap: Map<WorkoutType, String> = emptyMap(),
    val isCustomDateTime: Boolean = false,
    val customTimestamp: Long = System.currentTimeMillis()
)

sealed class HomeEvent {
    data class WorkoutsLogged(val entries: List<WorkoutEntry>) : HomeEvent()
    data class WorkoutDeleted(val entry: WorkoutEntry) : HomeEvent()
}

private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

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
    private val getWorkoutsForDateUseCase: GetWorkoutsForDateUseCase,
    private val addCustomWorkoutTypeUseCase: AddCustomWorkoutTypeUseCase,
    private val getCustomWorkoutTypesUseCase: GetCustomWorkoutTypesUseCase,
    private val deleteCustomWorkoutTypeUseCase: DeleteCustomWorkoutTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    val workoutsForDate: StateFlow<List<WorkoutEntry>> = _uiState
        .flatMapLatest {
            val startOfDay = todayMidnight()
            val endOfDay = startOfDay + DAY_IN_MILLIS
            getWorkoutsForDateUseCase(startOfDay, endOfDay)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cardioTypes: StateFlow<List<WorkoutType>> = combine(
        flowOf(PresetWorkoutTypes.filter { it.category == WorkoutCategory.CARDIO }),
        getCustomWorkoutTypesUseCase()
    ) { presets, customs ->
        presets + customs.filter { it.category == WorkoutCategory.CARDIO }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gymTypes: StateFlow<List<WorkoutType>> = combine(
        flowOf(PresetWorkoutTypes.filter { it.category == WorkoutCategory.GYM }),
        getCustomWorkoutTypesUseCase()
    ) { presets, customs ->
        presets + customs.filter { it.category == WorkoutCategory.GYM }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onWorkoutTypeToggled(type: WorkoutType) {
        val current = _uiState.value.selectedWorkoutTypes
        val isSelected = current.contains(type)
        val updated = if (isSelected) {
            current - type
        } else {
            current + type
        }

        val updatedMap = _uiState.value.workoutNotesMap.toMutableMap()
        if (isSelected) {
            updatedMap.remove(type)
        }

        _uiState.value = _uiState.value.copy(
            selectedWorkoutTypes = updated,
            workoutNotesMap = updatedMap
        )
    }

    fun onWorkoutNotesChanged(workoutType: WorkoutType, notes: String) {
        val updatedMap = _uiState.value.workoutNotesMap.toMutableMap()
        if (notes.isEmpty()) {
            updatedMap.remove(workoutType)
        } else {
            updatedMap[workoutType] = notes
        }
        _uiState.value = _uiState.value.copy(workoutNotesMap = updatedMap)
    }

    fun logWorkout() {
        val state = _uiState.value
        val workoutTypes = state.selectedWorkoutTypes
        if (workoutTypes.isEmpty()) return

        val now = System.currentTimeMillis()
        val timestampToUse = if (state.isCustomDateTime) state.customTimestamp else now

        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampToUse
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateMidnight = cal.timeInMillis

        val entries = workoutTypes.map { workoutType ->
            WorkoutEntry(
                workoutCategory = workoutType.category,
                workoutType = workoutType.name,
                date = dateMidnight,
                timestamp = timestampToUse,
                createdAt = now,
                notes = state.workoutNotesMap[workoutType] ?: ""
            )
        }

        viewModelScope.launch {
            entries.forEach { addWorkoutUseCase(it) }
            _events.emit(HomeEvent.WorkoutsLogged(entries))
        }

        _uiState.value = _uiState.value.copy(
            selectedWorkoutTypes = emptySet(),
            workoutNotesMap = emptyMap(),
            isCustomDateTime = false,
            customTimestamp = System.currentTimeMillis()
        )
    }

    fun updateCustomDate(year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.customTimestamp
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        _uiState.value = _uiState.value.copy(
            isCustomDateTime = true,
            customTimestamp = cal.timeInMillis
        )
    }

    fun updateCustomTime(hourOfDay: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.customTimestamp
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        _uiState.value = _uiState.value.copy(
            isCustomDateTime = true,
            customTimestamp = cal.timeInMillis
        )
    }

    fun onLogSheetOpened() {
        if (!_uiState.value.isCustomDateTime) {
            _uiState.value = _uiState.value.copy(
                customTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun updateWorkoutNote(entry: WorkoutEntry, newNote: String) {
        viewModelScope.launch {
            addWorkoutUseCase(entry.copy(notes = newNote))
        }
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

    fun addCustomWorkoutType(
        name: String,
        category: WorkoutCategory,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            onError("empty")
            return
        }

        viewModelScope.launch {
            val isPreset = PresetWorkoutTypes.any { it.name.equals(trimmedName, ignoreCase = true) }
            val existingCustoms = getCustomWorkoutTypesUseCase().first()
            val isCustom = existingCustoms.any { it.name.equals(trimmedName, ignoreCase = true) }

            if (isPreset || isCustom) {
                onError("exists")
            } else {
                addCustomWorkoutTypeUseCase(trimmedName, category)
                onSuccess()
            }
        }
    }

    fun deleteCustomWorkoutType(type: WorkoutType) {
        viewModelScope.launch {
            deleteCustomWorkoutTypeUseCase(type.name, type.category)
            val currentSelected = _uiState.value.selectedWorkoutTypes
            if (currentSelected.contains(type)) {
                val updatedSelected = currentSelected - type
                val updatedNotesMap = _uiState.value.workoutNotesMap.toMutableMap()
                updatedNotesMap.remove(type)
                _uiState.value = _uiState.value.copy(
                    selectedWorkoutTypes = updatedSelected,
                    workoutNotesMap = updatedNotesMap
                )
            }
        }
    }

    class Factory(
        private val addWorkoutUseCase: AddWorkoutUseCase,
        private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
        private val getWorkoutsForDateUseCase: GetWorkoutsForDateUseCase,
        private val addCustomWorkoutTypeUseCase: AddCustomWorkoutTypeUseCase,
        private val getCustomWorkoutTypesUseCase: GetCustomWorkoutTypesUseCase,
        private val deleteCustomWorkoutTypeUseCase: DeleteCustomWorkoutTypeUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                addWorkoutUseCase,
                deleteWorkoutUseCase,
                getWorkoutsForDateUseCase,
                addCustomWorkoutTypeUseCase,
                getCustomWorkoutTypesUseCase,
                deleteCustomWorkoutTypeUseCase
            ) as T
        }
    }
}

