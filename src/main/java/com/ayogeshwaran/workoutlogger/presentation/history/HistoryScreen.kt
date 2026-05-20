package com.ayogeshwaran.workoutlogger.presentation.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.presentation.home.todayMidnight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.WorkoutDeleted -> {
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
                val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateFormat.format(Date(uiState.selectedDate)),
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
                            text = "No workouts on this day.",
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
                    SwipeToDeleteCard(
                        workout = workout,
                        onDelete = { viewModel.deleteWorkout(workout) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
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
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

    val todayMidnight = remember { todayMidnight() }
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

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
                        contentDescription = "Previous month"
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
                        contentDescription = "Next month"
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
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1

                        if (day in 1..daysInMonth) {
                            val dayCal = Calendar.getInstance()
                            dayCal.set(year, month, day, 0, 0, 0)
                            dayCal.set(Calendar.MILLISECOND, 0)
                            val dayMillis = dayCal.timeInMillis

                            val isSelected = dayMillis == selectedDate
                            val isToday = dayMillis == todayMidnight
                            val hasWorkout = datesWithWorkouts.contains(dayMillis)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected) Modifier.background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                        else if (isToday) Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                        else Modifier
                                    )
                                    .clickable {
                                        onDateSelected(year, month, day)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasWorkout) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
    workout: WorkoutEntry,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        HistoryWorkoutCard(workout = workout)
    }
}

@Composable
private fun HistoryWorkoutCard(workout: WorkoutEntry) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

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
            Text(
                text = workout.workoutType,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = timeFormat.format(Date(workout.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
