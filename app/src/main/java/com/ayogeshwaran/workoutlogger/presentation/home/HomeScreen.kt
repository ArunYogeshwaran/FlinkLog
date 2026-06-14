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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.ayogeshwaran.workoutlogger.domain.model.localizedName
import com.ayogeshwaran.workoutlogger.presentation.components.EditNotesDialog
import com.ayogeshwaran.workoutlogger.presentation.components.SwipeToDeleteWorkoutCard
import androidx.compose.ui.res.stringResource
import com.ayogeshwaran.workoutlogger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
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
    var showLogBottomSheet by remember { mutableStateOf(false) }

    val sortedSelectedTypes = remember(uiState.selectedWorkoutTypes) {
        uiState.selectedWorkoutTypes.sortedWith(
            compareBy<WorkoutType> { it.category.ordinal }.thenBy { it.name }
        )
    }

    LaunchedEffect(sortedSelectedTypes) {
        if (sortedSelectedTypes.isEmpty()) {
            showLogBottomSheet = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.WorkoutsLogged -> {
                    val count = event.entries.size
                    val msg = context.resources.getQuantityString(R.plurals.workouts_logged, count, count)
                    snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Short
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            event.entries.forEach { viewModel.deleteWorkout(it) }
                        }
                    }
                }
                is HomeEvent.WorkoutDeleted -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.workout_deleted_msg),
                        actionLabel = context.getString(R.string.undo),
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
            // About button Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.about_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cardio & General chips
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.category_cardio),
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
                    text = stringResource(R.string.category_gym),
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

            // Log Workout button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showLogBottomSheet = true },
                    enabled = uiState.selectedWorkoutTypes.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.log_workout_btn))
                }
            }

            // Section header for today's workouts
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.logged_workouts_title),
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
                            text = stringResource(R.string.empty_workouts_home),
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



    editingWorkout?.let { workout ->
        EditNotesDialog(
            workout = workout,
            onDismiss = { editingWorkout = null },
            onConfirm = { newNote ->
                viewModel.updateWorkoutNote(workout, newNote)
            }
        )
    }

    if (showLogBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLogBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.workout_notes_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sortedSelectedTypes,
                        key = { "sheet_note_${it.category.name}_${it.name}" }
                    ) { workoutType ->
                        val noteValue = uiState.workoutNotesMap[workoutType] ?: ""
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = noteValue,
                                    onValueChange = { viewModel.onWorkoutNotesChanged(workoutType, it) },
                                    label = { Text(stringResource(R.string.notes_label, workoutType.emoji, workoutType.localizedName())) },
                                    placeholder = { Text(stringResource(R.string.notes_placeholder)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 3,
                                    trailingIcon = {
                                        if (noteValue.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.onWorkoutNotesChanged(workoutType, "") }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = stringResource(R.string.clear_notes_desc)
                                                )
                                            }
                                        }
                                    },
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.onWorkoutTypeToggled(workoutType) },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_action),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.logWorkout()
                        showLogBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.log_workout_btn))
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
        label = { Text(workoutType.localizedName()) }
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
                text = stringResource(R.string.swipe_hint_text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.swipe_hint_dismiss),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

