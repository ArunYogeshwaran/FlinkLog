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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.ayogeshwaran.workoutlogger.R
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.presentation.components.EditNotesDialog
import com.ayogeshwaran.workoutlogger.presentation.components.EmptyHistoryIllustration
import com.ayogeshwaran.workoutlogger.presentation.components.SwipeToDeleteWorkoutCard
import com.ayogeshwaran.workoutlogger.presentation.components.shareWorkouts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val datesWithWorkouts by viewModel.datesWithWorkouts.collectAsStateWithLifecycle()
    val workouts by viewModel.workoutsForSelectedDate.collectAsStateWithLifecycle()
    
    val pagedWorkouts = viewModel.pagedWorkouts.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var editingWorkout by remember { mutableStateOf<WorkoutEntry?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedWorkouts = remember { mutableStateListOf<WorkoutEntry>() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedWorkouts.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedWorkouts.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        if (selectedWorkouts.isNotEmpty()) {
                            IconButton(onClick = {
                                shareWorkouts(context, "Selected Workouts", selectedWorkouts.toList())
                                selectionMode = false
                                selectedWorkouts.clear()
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share selected")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ViewModeToggle(
                currentMode = uiState.viewMode,
                onModeSelected = { viewModel.setViewMode(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.viewMode == HistoryViewMode.TIMELINE) {
                AgendaTimeline(
                    pagedWorkouts = pagedWorkouts,
                    onDelete = { viewModel.deleteWorkout(it) },
                    onEditNotes = { editingWorkout = it },
                    selectionMode = selectionMode,
                    selectedWorkouts = selectedWorkouts,
                    onToggleSelection = { workout ->
                        if (selectedWorkouts.contains(workout)) {
                            selectedWorkouts.remove(workout)
                            if (selectedWorkouts.isEmpty()) selectionMode = false
                        } else {
                            selectedWorkouts.add(workout)
                        }
                    },
                    onLongPress = { workout ->
                        if (!selectionMode) {
                            selectionMode = true
                            selectedWorkouts.add(workout)
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (workouts.isNotEmpty()) {
                                IconButton(
                                    onClick = { shareWorkouts(context, dateString, workouts) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = stringResource(R.string.share_workouts_desc),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (workouts.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                EmptyHistoryIllustration(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .padding(bottom = 16.dp)
                                )
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
}

@Composable
fun ViewModeToggle(
    currentMode: HistoryViewMode,
    onModeSelected: (HistoryViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HistoryViewMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val label = when (mode) {
                HistoryViewMode.TIMELINE -> stringResource(R.string.timeline_view_title)
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
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    year: Int,
    month: Int,
    selectedDate: Long,
    datesWithWorkouts: Set<Long>,
    onDateSelected: (Int, Int, Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    val monthString = remember(year, month) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        monthFormat.format(cal.time)
    }

    val daysInMonth = remember(year, month) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(year, month) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.get(Calendar.DAY_OF_WEEK)
    }

    val todayMidnight = remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }

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
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.prev_month_desc)
                    )
                }
                Text(
                    text = monthString,
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

            Spacer(modifier = Modifier.height(16.dp))

            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val weeks = Math.ceil((daysInMonth + firstDayOfWeek - 1) / 7.0).toInt()
            var dayCounter = 1

            for (week in 0 until weeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayOfWeek in 1..7) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek || dayCounter > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val currentDay = dayCounter
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, currentDay)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val dateMillis = cal.timeInMillis
                            val isSelected = dateMillis == selectedDate
                            val isToday = dateMillis == todayMidnight
                            val hasWorkout = datesWithWorkouts.contains(dateMillis)

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
                                isSelected -> Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                )
                                isToday -> Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    CircleShape
                                )
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
                                        onDateSelected(year, month, currentDay)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentDay.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}
