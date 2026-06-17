package com.ayogeshwaran.workoutlogger.presentation.history

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
            // View Mode Toggle
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ViewModeToggle(
                    currentMode = uiState.viewMode,
                    onModeSelected = { viewModel.setViewMode(it) }
                )
            }

            if (uiState.viewMode == HistoryViewMode.WEEKLY) {
                // Weekly View (Last 7 Days)
                item {
                    WeeklyView(
                        selectedDate = uiState.selectedDate,
                        datesWithWorkouts = datesWithWorkouts,
                        onDateSelected = { year, month, day ->
                            viewModel.onDateSelected(year, month, day)
                        },
                        onPreviousDay = { viewModel.adjustSelectedDate(-1) },
                        onNextDay = { viewModel.adjustSelectedDate(1) },
                        workoutsCount = workouts.size
                    )
                }

                // Workouts Grouped by Date (Weekly view)
                if (workouts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.empty_workouts_weekly_history),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val groupedWorkouts = workouts.groupBy { workout ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = workout.timestamp
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        cal.timeInMillis
                    }.toSortedMap(reverseOrder())

                    groupedWorkouts.forEach { (dateMillis, dayWorkouts) ->
                        item(key = "header_$dateMillis") {
                            val dateString = remember(dateMillis) {
                                android.text.format.DateUtils.formatDateTime(
                                    context,
                                    dateMillis,
                                    android.text.format.DateUtils.FORMAT_SHOW_DATE or
                                            android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY or
                                            android.text.format.DateUtils.FORMAT_SHOW_YEAR
                                )
                            }
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(
                            items = dayWorkouts,
                            key = { it.id }
                        ) { workout ->
                            SwipeToDeleteWorkoutCard(
                                workout = workout,
                                onDelete = { viewModel.deleteWorkout(workout) },
                                onEditNotes = { editingWorkout = it }
                            )
                        }
                    }
                }
            } else {
                // Monthly Calendar
                item {
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

                // Workout list or empty (Monthly view)
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

                            val bgColor = when {
                                hasWorkout -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                hasWorkout -> MaterialTheme.colorScheme.onPrimary
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val borderModifier = when {
                                isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                else -> Modifier
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

@Composable
private fun ViewModeToggle(
    currentMode: HistoryViewMode,
    onModeSelected: (HistoryViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HistoryViewMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val label = when (mode) {
                HistoryViewMode.WEEKLY -> stringResource(R.string.weekly_view_title)
                HistoryViewMode.MONTHLY -> stringResource(R.string.monthly_view_title)
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyView(
    selectedDate: Long,
    datesWithWorkouts: Set<Long>,
    onDateSelected: (Int, Int, Int) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    workoutsCount: Int
) {
    val todayMidnight = remember { todayMidnight() }
    val lastSevenDays = remember(selectedDate) {
        val list = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDate
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            list.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val activeDaysCount = remember(lastSevenDays, datesWithWorkouts) {
        lastSevenDays.count { datesWithWorkouts.contains(it) }
    }

    val rangeString = remember(selectedDate) {
        val startCal = Calendar.getInstance().apply { timeInMillis = selectedDate; add(Calendar.DAY_OF_YEAR, -6) }
        val endCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        
        val format = SimpleDateFormat("MMM d", Locale.getDefault())
        val formatWithYear = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        
        if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
            "${format.format(startCal.time)} - ${formatWithYear.format(endCal.time)}"
        } else {
            "${formatWithYear.format(startCal.time)} - ${formatWithYear.format(endCal.time)}"
        }
    }

    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayOfMonthFormat = remember { SimpleDateFormat("d", Locale.getDefault()) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousDay) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.prev_month_desc)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = rangeString,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.weekly_view_subtitle, workoutsCount, activeDaysCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onNextDay) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.next_month_desc)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                lastSevenDays.forEach { dayMillis ->
                    val cal = remember(dayMillis) {
                        Calendar.getInstance().apply { timeInMillis = dayMillis }
                    }
                    val isSelected = dayMillis == selectedDate
                    val isToday = dayMillis == todayMidnight
                    val hasWorkout = datesWithWorkouts.contains(dayMillis)

                    val weekdayLabel = remember(dayMillis) {
                        dayOfWeekFormat.format(cal.time)
                    }
                    val dayLabel = remember(dayMillis) {
                        dayOfMonthFormat.format(cal.time)
                    }

                    val bgColor = when {
                        hasWorkout -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }

                    val textColor = when {
                        hasWorkout -> MaterialTheme.colorScheme.onPrimary
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val borderModifier = when {
                        isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        else -> Modifier
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable {
                                onDateSelected(
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                )
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = weekdayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .then(borderModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
