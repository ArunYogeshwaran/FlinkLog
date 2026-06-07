package com.ayogeshwaran.workoutlogger.presentation.home

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.presentation.components.EditNotesDialog
import com.ayogeshwaran.workoutlogger.presentation.components.SwipeToDeleteWorkoutCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val workouts by viewModel.workoutsForDate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("onboarding", Context.MODE_PRIVATE) }
    var showSwipeHint by remember { mutableStateOf(!prefs.getBoolean("swipe_hint_dismissed", false)) }
    var editingWorkout by remember { mutableStateOf<WorkoutEntry?>(null) }

    val sortedSelectedTypes = remember(uiState.selectedWorkoutTypes) {
        uiState.selectedWorkoutTypes.sortedWith(
            compareBy<WorkoutType> { it.category.ordinal }.thenBy { it.name }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.WorkoutsLogged -> {
                    val count = event.entries.size
                    val msg = if (count == 1) "Workout logged!" else "$count workouts logged!"
                    snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            event.entries.forEach { viewModel.deleteWorkout(it) }
                        }
                    }
                }
                is HomeEvent.WorkoutDeleted -> {
                    snackbarHostState.showSnackbar(
                        message = "Workout deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDelete(event.entry)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date & Time Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DateTimeHeader(
                    selectedDate = uiState.selectedDate,
                    selectedHour = uiState.selectedHour,
                    selectedMinute = uiState.selectedMinute,
                    onDateClick = { viewModel.onShowDatePicker(true) },
                    onTimeClick = { viewModel.onShowTimePicker(true) }
                )
            }

            // Cardio & General chips
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cardio & General",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    viewModel.cardioTypes.forEach { type ->
                        WorkoutChip(
                            workoutType = type,
                            isSelected = uiState.selectedWorkoutTypes.contains(type),
                            onSelected = {
                                viewModel.onWorkoutTypeToggled(type)
                            }
                        )
                    }
                }
            }

            // Gym / Muscle Groups chips
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gym / Muscle Groups",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    viewModel.gymTypes.forEach { type ->
                        WorkoutChip(
                            workoutType = type,
                            isSelected = uiState.selectedWorkoutTypes.contains(type),
                            onSelected = {
                                viewModel.onWorkoutTypeToggled(type)
                            }
                        )
                    }
                }
            }

            // Notes fields (visible only when one or more workout types are selected)
            if (sortedSelectedTypes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Workout Notes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(
                    items = sortedSelectedTypes,
                    key = { "note_${it.category.name}_${it.name}" }
                ) { workoutType ->
                    val noteValue = uiState.workoutNotesMap[workoutType] ?: ""
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = noteValue,
                        onValueChange = { viewModel.onWorkoutNotesChanged(workoutType, it) },
                        label = { Text("Notes for ${workoutType.emoji} ${workoutType.name}") },
                        placeholder = { Text("e.g., details, weight, sets") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        trailingIcon = {
                            if (noteValue.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onWorkoutNotesChanged(workoutType, "") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear notes"
                                    )
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // Log Workout button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.logWorkout() },
                    enabled = uiState.selectedWorkoutTypes.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Workout")
                }
            }

            // Section header for today's workouts
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Logged Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Swipe-to-delete onboarding tooltip
            if (showSwipeHint && workouts.isNotEmpty()) {
                item {
                    SwipeToDeleteTooltip(
                        onDismiss = {
                            showSwipeHint = false
                            prefs.edit { putBoolean("swipe_hint_dismissed", true) }
                        }
                    )
                }
            }

            // Workout list or empty state
            if (workouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No workouts logged yet.\nStart your first session.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    items = workouts,
                    key = { it.id }
                ) { workout ->
                    SwipeToDeleteWorkoutCard(
                        workout = workout,
                        onDelete = { viewModel.deleteWorkout(workout) },
                        onEditNotes = { editingWorkout = it }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (uiState.showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.selectedHour,
            initialMinute = uiState.selectedMinute
        )
        TimePickerDialog(
            onDismiss = { viewModel.onShowTimePicker(false) },
            onConfirm = {
                viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute)
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    editingWorkout?.let { workout ->
        EditNotesDialog(
            workout = workout,
            onDismiss = { editingWorkout = null },
            onConfirm = { newNote ->
                viewModel.updateWorkoutNote(workout, newNote)
            }
        )
    }
}

@Composable
private fun DateTimeHeader(
    selectedDate: Long,
    selectedHour: Int,
    selectedMinute: Int,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()) }
    val timeString = remember(selectedHour, selectedMinute) {
        String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(selectedDate)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = onDateClick) {
                    Text("Change Date")
                }
                TextButton(onClick = onTimeClick) {
                    Text("Change Time")
                }
            }
        }
    }
}

@Composable
private fun WorkoutChip(
    workoutType: WorkoutType,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(workoutType.name) }
    )
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = { content() }
    )
}

@Composable
private fun SwipeToDeleteTooltip(
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡 Tip: Swipe left on a workout to delete it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Got it",
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

