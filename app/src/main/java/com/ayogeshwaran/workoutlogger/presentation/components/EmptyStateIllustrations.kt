package com.ayogeshwaran.workoutlogger.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun EmptyHomeIllustration(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.aspectRatio(1.5f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Glow/Shadow shadow beneath kettlebell
            drawOval(
                color = primaryColor.copy(alpha = 0.1f),
                topLeft = Offset(width * 0.3f, height * 0.75f),
                size = Size(width * 0.4f, height * 0.1f)
            )

            // Kettlebell handle
            val handlePath = Path().apply {
                moveTo(width * 0.38f, height * 0.45f)
                lineTo(width * 0.38f, height * 0.28f)
                quadraticTo(width * 0.38f, height * 0.15f, width * 0.5f, height * 0.15f)
                quadraticTo(width * 0.62f, height * 0.15f, width * 0.62f, height * 0.28f)
                lineTo(width * 0.62f, height * 0.45f)
            }

            drawPath(
                path = handlePath,
                color = primaryColor,
                style = Stroke(
                    width = 12.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Inner handle cutout layer to overlay properly
            drawPath(
                path = handlePath,
                color = surfaceVariant,
                style = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Kettlebell main body
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f)),
                    start = Offset(width * 0.35f, height * 0.35f),
                    end = Offset(width * 0.65f, height * 0.75f)
                ),
                radius = width * 0.22f,
                center = Offset(width * 0.5f, height * 0.52f)
            )

            // Modern gloss highlight curve on the body
            val highlightPath = Path().apply {
                val cx = width * 0.5f
                val cy = height * 0.52f
                val r = width * 0.22f
                arcTo(
                    rect = Rect(cx - r, cy - r, cx + r, cy + r),
                    startAngleDegrees = -150f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.4f),
                style = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun EmptyHistoryIllustration(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryLight = primaryColor.copy(alpha = 0.12f)
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.aspectRatio(1.5f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Soft shadow
            drawOval(
                color = primaryColor.copy(alpha = 0.08f),
                topLeft = Offset(width * 0.25f, height * 0.78f),
                size = Size(width * 0.5f, height * 0.1f)
            )

            // Rounded calendar card base
            val cardRect = RoundRect(
                left = width * 0.32f,
                top = height * 0.22f,
                right = width * 0.68f,
                bottom = height * 0.75f,
                cornerRadius = CornerRadius(12.dp.toPx())
            )
            val cardPath = Path().apply {
                addRoundRect(cardRect)
            }

            drawPath(
                path = cardPath,
                color = primaryLight
            )

            drawPath(
                path = cardPath,
                color = primaryColor,
                style = Stroke(
                    width = 4.dp.toPx()
                )
            )

            // Header bar of the calendar
            val headerPath = Path().apply {
                val r = 12.dp.toPx()
                moveTo(width * 0.32f, height * 0.22f + r)
                quadraticTo(width * 0.32f, height * 0.22f, width * 0.32f + r, height * 0.22f)
                lineTo(width * 0.68f - r, height * 0.22f)
                quadraticTo(width * 0.68f, height * 0.22f, width * 0.68f, height * 0.22f + r)
                lineTo(width * 0.68f, height * 0.35f)
                lineTo(width * 0.32f, height * 0.35f)
                close()
            }
            drawPath(
                path = headerPath,
                color = primaryColor
            )

            // Top binding rings
            drawRoundRect(
                color = onSurface.copy(alpha = 0.3f),
                topLeft = Offset(width * 0.4f, height * 0.16f),
                size = Size(5.dp.toPx(), 12.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawRoundRect(
                color = onSurface.copy(alpha = 0.3f),
                topLeft = Offset(width * 0.57f, height * 0.16f),
                size = Size(5.dp.toPx(), 12.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // Sleeping crescent moon for the empty/rest day concept
            val moonPath = Path().apply {
                val mx = width * 0.5f
                val my = height * 0.55f
                val mr = width * 0.09f

                arcTo(
                    rect = Rect(mx - mr, my - mr, mx + mr, my + mr),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = true
                )
                arcTo(
                    rect = Rect(mx - mr * 0.4f, my - mr, mx + mr * 1.2f, my + mr),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = moonPath,
                color = primaryColor
            )

            // Restful stars
            drawCircle(
                color = primaryColor.copy(alpha = 0.6f),
                radius = 2.dp.toPx(),
                center = Offset(width * 0.42f, height * 0.46f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.6f),
                radius = 1.5f.dp.toPx(),
                center = Offset(width * 0.58f, height * 0.48f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.6f),
                radius = 3.dp.toPx(),
                center = Offset(width * 0.44f, height * 0.64f)
            )
        }
    }
}
