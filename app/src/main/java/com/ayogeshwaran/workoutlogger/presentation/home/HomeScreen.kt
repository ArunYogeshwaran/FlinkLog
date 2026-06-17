package com.ayogeshwaran.workoutlogger.presentation.home

import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayogeshwaran.workoutlogger.R
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutType
import com.ayogeshwaran.workoutlogger.domain.model.localizedName
import com.ayogeshwaran.workoutlogger.presentation.components.EditNotesDialog
import com.ayogeshwaran.workoutlogger.presentation.components.SwipeToDeleteWorkoutCard
import kotlinx.coroutines.launch
import java.util.Calendar

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
    val scope = rememberCoroutineScope()

    val cardioTypes by viewModel.cardioTypes.collectAsStateWithLifecycle()
    val gymTypes by viewModel.gymTypes.collectAsStateWithLifecycle()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var customWorkoutCategory by remember { mutableStateOf<WorkoutCategory?>(null) }
    var deletingCustomWorkoutType by remember { mutableStateOf<WorkoutType?>(null) }
    val prefs = remember { context.getSharedPreferences("onboarding", Context.MODE_PRIVATE) }
    var showSwipeHint by remember {
        mutableStateOf(
            !prefs.getBoolean(
                "swipe_hint_dismissed",
                false
            )
        )
    }
    var editingWorkout by remember { mutableStateOf<WorkoutEntry?>(null) }
    var showLogBottomSheet by remember { mutableStateOf(false) }

    val customTimestamp = uiState.customTimestamp
    val calendar = remember(customTimestamp) {
        Calendar.getInstance().apply {
            timeInMillis = customTimestamp
        }
    }

    val datePickerDialog = remember(context, customTimestamp) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.updateCustomDate(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember(context, customTimestamp) {
        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.updateCustomTime(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            is24Hour
        )
    }

    val formattedDate = remember(context, customTimestamp) {
        android.text.format.DateUtils.formatDateTime(
            context,
            customTimestamp,
            android.text.format.DateUtils.FORMAT_SHOW_DATE or
                    android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY or
                    android.text.format.DateUtils.FORMAT_SHOW_YEAR
        )
    }

    val timeFormat = remember(context) { android.text.format.DateFormat.getTimeFormat(context) }
    val formattedTime = remember(timeFormat, customTimestamp) {
        timeFormat.format(java.util.Date(customTimestamp))
    }

    LaunchedEffect(showLogBottomSheet) {
        if (showLogBottomSheet) {
            viewModel.onLogSheetOpened()
        }
    }

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
                    val msg =
                        context.resources.getQuantityString(R.plurals.workouts_logged, count, count)
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
                    cardioTypes.forEach { type ->
                        WorkoutChip(
                            workoutType = type,
                            isSelected = uiState.selectedWorkoutTypes.contains(type),
                            onSelected = {
                                viewModel.onWorkoutTypeToggled(type)
                            },
                            onDeleteCustom = {
                                deletingCustomWorkoutType = type
                            }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = {
                            customWorkoutCategory = WorkoutCategory.CARDIO
                            showAddCustomDialog = true
                        },
                        label = { Text("+") }
                    )
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
                    gymTypes.forEach { type ->
                        WorkoutChip(
                            workoutType = type,
                            isSelected = uiState.selectedWorkoutTypes.contains(type),
                            onSelected = {
                                viewModel.onWorkoutTypeToggled(type)
                            },
                            onDeleteCustom = {
                                deletingCustomWorkoutType = type
                            }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = {
                            customWorkoutCategory = WorkoutCategory.GYM
                            showAddCustomDialog = true
                        },
                        label = { Text("+") }
                    )
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
                    text = stringResource(R.string.logged_workouts_today),
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

    deletingCustomWorkoutType?.let { type ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deletingCustomWorkoutType = null },
            title = { Text(stringResource(R.string.delete_custom_workout_title)) },
            text = { Text(stringResource(R.string.delete_custom_workout_confirm, type.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomWorkoutType(type)
                        deletingCustomWorkoutType = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.delete_custom_workout_success)
                            )
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCustomWorkoutType = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAddCustomDialog && customWorkoutCategory != null) {
        AddCustomWorkoutDialog(
            category = customWorkoutCategory!!,
            onDismiss = {
                showAddCustomDialog = false
                customWorkoutCategory = null
            },
            onConfirm = {
                showAddCustomDialog = false
                customWorkoutCategory = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.add_custom_workout_success)
                    )
                }
            },
            viewModel = viewModel
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.SuggestionChip(
                        onClick = { datePickerDialog.show() },
                        label = { Text(formattedDate) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.change_date)
                            )
                        }
                    )
                    androidx.compose.material3.SuggestionChip(
                        onClick = { timePickerDialog.show() },
                        label = { Text(formattedTime) }
                    )
                }

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
                                    onValueChange = {
                                        viewModel.onWorkoutNotesChanged(
                                            workoutType,
                                            it
                                        )
                                    },
                                    label = {
                                        Text(
                                            stringResource(
                                                R.string.notes_label,
                                                workoutType.localizedName()
                                            )
                                        )
                                    },
                                    placeholder = {
                                        val placeholderRes = if (workoutType.category == com.ayogeshwaran.workoutlogger.domain.model.WorkoutCategory.CARDIO) {
                                            R.string.notes_placeholder_cardio
                                        } else {
                                            R.string.notes_placeholder_weights
                                        }
                                        Text(stringResource(placeholderRes))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 3,
                                    trailingIcon = {
                                        if (noteValue.isNotEmpty()) {
                                            IconButton(onClick = {
                                                viewModel.onWorkoutNotesChanged(
                                                    workoutType,
                                                    ""
                                                )
                                            }) {
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
    onSelected: () -> Unit,
    onDeleteCustom: (() -> Unit)? = null
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(workoutType.localizedName()) },
        trailingIcon = {
            if (workoutType.nameRes == 0 && onDeleteCustom != null) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.delete_action),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDeleteCustom() },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
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


@Composable
private fun AddCustomWorkoutDialog(
    category: WorkoutCategory,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: HomeViewModel
) {
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_custom_workout_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.workout_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().isEmpty()) {
                        errorMessage = context.getString(R.string.add_custom_workout_error_empty)
                    } else {
                        viewModel.addCustomWorkoutType(
                            name = name,
                            category = category,
                            onSuccess = {
                                onConfirm()
                            },
                            onError = { errorType ->
                                errorMessage = when (errorType) {
                                    "empty" -> context.getString(R.string.add_custom_workout_error_empty)
                                    "exists" -> context.getString(R.string.add_custom_workout_error_exists)
                                    else -> "Unknown error"
                                }
                            }
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

