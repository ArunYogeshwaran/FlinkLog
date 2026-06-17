package com.ayogeshwaran.workoutlogger.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayogeshwaran.workoutlogger.R
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.presentation.components.EditNotesDialog
import com.ayogeshwaran.workoutlogger.presentation.components.SwipeToDeleteWorkoutCard
import com.ayogeshwaran.workoutlogger.presentation.home.todayMidnight
import com.ayogeshwaran.workoutlogger.presentation.theme.OnWorkoutDoneDark
import com.ayogeshwaran.workoutlogger.presentation.theme.OnWorkoutDoneLight
import com.ayogeshwaran.workoutlogger.presentation.theme.OnWorkoutMissedDark
import com.ayogeshwaran.workoutlogger.presentation.theme.OnWorkoutMissedLight
import com.ayogeshwaran.workoutlogger.presentation.theme.WorkoutDoneDark
import com.ayogeshwaran.workoutlogger.presentation.theme.WorkoutDoneLight
import com.ayogeshwaran.workoutlogger.presentation.theme.WorkoutMissedDark
import com.ayogeshwaran.workoutlogger.presentation.theme.WorkoutMissedLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val datesWithWorkouts by viewModel.datesWithWorkouts.collectAsStateWithLifecycle()
    val workouts by viewModel.workoutsForSelectedDate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingWorkout by remember { mutableStateOf<WorkoutEntry?>(null) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.WorkoutDeleted -> {
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
            // Calendar
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CalendarView(
                    year = uiState.displayedYear,
                    month = uiState.displayedMonth,
                    selectedDate = uiState.selectedDate,
                    datesWithWorkouts = datesWithWorkouts,
                    onDateSelected = { year, month, day ->
                        viewModel.onDateSelected(year, month, day)
                    },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            // Selected date header
            item {
                val dateString = remember(context, uiState.selectedDate) {
                    android.text.format.DateUtils.formatDateTime(
                        context,
                        uiState.selectedDate,
                        android.text.format.DateUtils.FORMAT_SHOW_DATE or
                                android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY or
                                android.text.format.DateUtils.FORMAT_SHOW_YEAR
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Workout list or empty
            if (workouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_workouts_history),
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
}

@Composable
private fun CalendarView(
    year: Int,
    month: Int,
    selectedDate: Long,
    datesWithWorkouts: Set<Long>,
    onDateSelected: (Int, Int, Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val cal = remember(year, month) {
        Calendar.getInstance().apply { set(year, month, 1) }
    }
    val headerDate = remember(year, month) {
        val c = Calendar.getInstance()
        c.set(year, month, 1)
        monthFormat.format(c.time)
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeekSetting = remember { cal.firstDayOfWeek }
    val firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK)
    val offset = (firstDayOfMonth - firstDayOfWeekSetting + 7) % 7

    val todayMidnight = remember { todayMidnight() }
    val dayLabels = remember(firstDayOfWeekSetting) {
        val symbols = java.text.DateFormatSymbols.getInstance(Locale.getDefault())
        val weekdays = symbols.shortWeekdays // index 1 is Sunday, 7 is Saturday
        val list = mutableListOf<String>()
        var day = firstDayOfWeekSetting
        for (i in 0 until 7) {
            list.add(weekdays[day])
            day = if (day == Calendar.SATURDAY) Calendar.SUNDAY else day + 1
        }
        list
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.prev_month_desc)
                    )
                }
                Text(
                    text = headerDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.next_month_desc)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day of week labels
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid
            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - offset + 1

                        if (day in 1..daysInMonth) {
                            val dayCal = Calendar.getInstance()
                            dayCal.set(year, month, day, 0, 0, 0)
                            dayCal.set(Calendar.MILLISECOND, 0)
                            val dayMillis = dayCal.timeInMillis

                            val isSelected = dayMillis == selectedDate
                            val isToday = dayMillis == todayMidnight
                            val hasWorkout = datesWithWorkouts.contains(dayMillis)
                            val isPast = dayMillis < todayMidnight

                            val isDark = isSystemInDarkTheme()
                            val workoutDoneColor = if (isDark) WorkoutDoneDark else WorkoutDoneLight
                            val onWorkoutDoneColor = if (isDark) OnWorkoutDoneDark else OnWorkoutDoneLight

                            val bgColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                hasWorkout -> workoutDoneColor
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                hasWorkout -> onWorkoutDoneColor
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val borderModifier = if (isToday && !isSelected) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else {
                                Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .then(borderModifier)
                                    .clickable {
                                        onDateSelected(year, month, day)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}
