package com.charles.warmwords.app.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charles.warmwords.app.ui.theme.WarmOrange
import com.charles.warmwords.app.ui.theme.WarmPink

enum class AvatarState {
    IDLE,
    LISTENING,
    THINKING,
    HAPPY,
    CONCERNED,
    SUPPORTIVE
}

@Composable
fun Avatar(
    state: AvatarState,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = state, label = "avatarTransition")

    val eyeOpenProgress by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(durationMillis = 300) },
        label = "eyeOpen"
    ) { targetState ->
        when (targetState) {
            AvatarState.LISTENING -> 1f
            AvatarState.THINKING -> 0.3f
            AvatarState.CONCERNED -> 0.7f
            AvatarState.HAPPY -> 1f
            AvatarState.SUPPORTIVE -> 0.9f
            AvatarState.IDLE -> 0.8f
        }
    }

    val pulseScale by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(durationMillis = 800) },
        label = "pulseScale"
    ) { targetState ->
        when (targetState) {
            AvatarState.LISTENING -> 1.05f
            AvatarState.THINKING -> 0.97f
            AvatarState.HAPPY -> 1.08f
            else -> 1f
        }
    }

    val mouthCurvature by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(durationMillis = 500) },
        label = "mouthCurvature"
    ) { targetState ->
        when (targetState) {
            AvatarState.HAPPY -> 0.8f
            AvatarState.CONCERNED -> 0.3f
            AvatarState.SUPPORTIVE -> 0.6f
            AvatarState.LISTENING -> 0.5f
            AvatarState.THINKING -> 0f
            AvatarState.IDLE -> 0.4f
        }
    }

    val density = androidx.compose.ui.unit.Density(1f, 1f)

    Canvas(
        modifier = modifier
            .size(size * pulseScale)
    ) {
        val canvasWidth = size.toPx()
        val centerX = canvasWidth / 2f
        val centerY = canvasWidth / 2f
        val headRadius = centerX * 0.4f

        drawCircle(
            color = WarmPink,
            radius = headRadius,
            style = Stroke(width = headRadius * 0.08f)
        )

        drawCircle(
            color = WarmOrange,
            radius = headRadius * 0.88f
        )

        val eyeY = centerY - headRadius * 0.15f
        val eyeRadius = headRadius * 0.08f
        val eyeOffset = headRadius * 0.3f
        val openHeight = eyeRadius * eyeOpenProgress

        drawRoundRect(
            color = Color(0xFF424242),
            topLeft = Offset(centerX - eyeOffset - eyeRadius, eyeY - openHeight / 2),
            size = Size(eyeRadius * 2, openHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        drawRoundRect(
            color = Color(0xFF424242),
            topLeft = Offset(centerX + eyeOffset - eyeRadius, eyeY - openHeight / 2),
            size = Size(eyeRadius * 2, openHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        val mouthY = centerY + headRadius * 0.25f
        if (mouthCurvature > 0f) {
            val mouthWidth = headRadius * 0.35f * mouthCurvature
            val mouthHeight = headRadius * 0.15f
            val mouthStartY = mouthY - mouthHeight * (1f - mouthCurvature)

            drawArc(
                color = Color(0xFF424242),
                startAngle = 180f,
                sweepAngle = -180f,
                useCenter = false,
                topLeft = Offset(centerX - mouthWidth / 2, mouthStartY - mouthHeight / 2),
                size = Size(mouthWidth, mouthHeight),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        } else {
            drawLine(
                color = Color(0xFF424242),
                start = Offset(centerX - headRadius * 0.15f, mouthY),
                end = Offset(centerX + headRadius * 0.15f, mouthY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}
