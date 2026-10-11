package com.ayogeshwaran.workoutlogger.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ayogeshwaran.workoutlogger.R
import com.ayogeshwaran.workoutlogger.domain.model.WorkoutEntry
import com.ayogeshwaran.workoutlogger.domain.model.getEmoji
import com.ayogeshwaran.workoutlogger.domain.model.localizedType
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWorkoutCard(
    workout: WorkoutEntry,
    onDelete: () -> Unit,
    onEditNotes: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                label = "swipe_bg"
            )
            val iconColor by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onError
                    else -> MaterialTheme.colorScheme.onErrorContainer
                },
                label = "swipe_icon"
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
                    contentDescription = stringResource(R.string.delete_action),
                    tint = iconColor
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        WorkoutCard(
            workout = workout, 
            onEditNotes = onEditNotes,
            selectionMode = selectionMode,
            isSelected = isSelected,
            onToggleSelection = onToggleSelection,
            onLongPress = onLongPress
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutCard(
    workout: WorkoutEntry,
    onEditNotes: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val context = LocalContext.current
    val timeFormat = remember(context) { android.text.format.DateFormat.getTimeFormat(context) }

    val cardModifier = if (selectionMode) {
        modifier.fillMaxWidth().combinedClickable(
            onClick = onToggleSelection,
            onLongClick = onLongPress
        )
    } else {
        modifier.fillMaxWidth().combinedClickable(
            onClick = {},
            onLongClick = onLongPress
        )
    }
    ElevatedCard(
        modifier = cardModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.localizedType(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (workout.notes.isNotBlank()) {
                    Text(
                        text = workout.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditNotes(workout) }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = timeFormat.format(Date(workout.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { onEditNotes(workout) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_notes),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditNotesDialog(
    workout: WorkoutEntry,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(workout.notes) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_notes_dialog_title)) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(
                        stringResource(
                            R.string.edit_notes_workout_label,
                            workout.localizedType()
                        )
                    )
                },
                placeholder = { Text(stringResource(R.string.edit_notes_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(text)
                    onDismiss()
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

fun shareWorkouts(context: Context, dateString: String, workouts: List<WorkoutEntry>) {
    val packageName = context.packageName
    val playStoreLink = "https://play.google.com/store/apps/details?id=$packageName"

    val stringBuilder = java.lang.StringBuilder()
    
    if (workouts.isEmpty()) {
        stringBuilder.append("📅 $dateString\n")
    } else {
        val groupedWorkouts = workouts.groupBy { it.date }.toSortedMap(reverseOrder())
        
        var isFirstDate = true
        groupedWorkouts.forEach { (dateMillis, dailyWorkouts) ->
            if (!isFirstDate) {
                stringBuilder.append("\n")
            }
            isFirstDate = false
            
            val formattedDate = android.text.format.DateUtils.formatDateTime(
                context,
                dateMillis,
                android.text.format.DateUtils.FORMAT_SHOW_DATE or
                        android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY or
                        android.text.format.DateUtils.FORMAT_SHOW_YEAR
            )
            stringBuilder.append("📅 $formattedDate\n")
            
            dailyWorkouts.forEach { workout ->
                val displayName = workout.localizedType(context.resources)
                val emoji = workout.getEmoji()
                if (workout.notes.isNotBlank()) {
                    val cleanNotes = workout.notes.trim().replace("\n", ", ")
                    stringBuilder.append("• $emoji $displayName ($cleanNotes)\n")
                } else {
                    stringBuilder.append("• $emoji $displayName\n")
                }
            }
        }
    }

    stringBuilder.append("\nLog your workouts offline, ad-free, and privately with FlinkLog! Download it here:\n$playStoreLink")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
    }
    val chooser = Intent.createChooser(intent, "Share Workouts")
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        // No sharing activity available
    }
}
