package com.ayogeshwaran.workoutlogger.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.presentation.components.SwipeToDeleteWorkoutCard
import java.util.Calendar

@Composable
fun AgendaTimeline(
    pagedWorkouts: LazyPagingItems<WorkoutEntry>,
    onDelete: (WorkoutEntry) -> Unit,
    onEditNotes: (WorkoutEntry) -> Unit,
    selectionMode: Boolean = false,
    selectedWorkouts: List<WorkoutEntry> = emptyList(),
    onToggleSelection: (WorkoutEntry) -> Unit = {},
    onLongPress: (WorkoutEntry) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true
    ) {
        items(
            count = pagedWorkouts.itemCount,
            key = pagedWorkouts.itemKey { it.id },
            contentType = pagedWorkouts.itemContentType { "WorkoutEntry" }
        ) { index ->
            val workout = pagedWorkouts[index]
            if (workout != null) {
                Column {

                    val isNewDate = if (index == pagedWorkouts.itemCount - 1) {
                        true
                    } else {
                        val prevWorkout = pagedWorkouts[index + 1]
                        prevWorkout != null && workout.date != prevWorkout.date
                    }

                    if (isNewDate) {
                        val dateString = android.text.format.DateUtils.formatDateTime(
                            androidx.compose.ui.platform.LocalContext.current,
                            workout.date,
                            android.text.format.DateUtils.FORMAT_SHOW_DATE or
                                    android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY or
                                    android.text.format.DateUtils.FORMAT_SHOW_YEAR
                        )
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    SwipeToDeleteWorkoutCard(
                        workout = workout,
                        onDelete = { onDelete(workout) },
                        onEditNotes = { onEditNotes(workout) },
                        selectionMode = selectionMode,
                        isSelected = selectedWorkouts.contains(workout),
                        onToggleSelection = { onToggleSelection(workout) },
                        onLongPress = { onLongPress(workout) }
                    )
                }
            }
        }
    }
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
