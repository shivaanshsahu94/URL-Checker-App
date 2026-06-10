package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sailing
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Subtle haptic feedback utilities
fun triggerHapticFeedback(context: Context) {
    try {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(12, android.os.VibrationEffect.DEFAULT_AMPLITUDE),
                    android.os.VibrationAttributes.createForUsage(android.os.VibrationAttributes.USAGE_TOUCH)
                )
            } else {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(12, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
    } catch (_: Exception) {
        // Fallback
    }
}

fun triggerSuccessHaptic(context: Context) {
    try {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            val timings = longArrayOf(0, 10, 80, 15)
            val amplitudes = intArrayOf(0, android.os.VibrationEffect.DEFAULT_AMPLITUDE, 0, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createWaveform(timings, amplitudes, -1),
                    android.os.VibrationAttributes.createForUsage(android.os.VibrationAttributes.USAGE_TOUCH)
                )
            } else {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
            }
        }
    } catch (_: Exception) {
        // Fallback
    }
}

// Custom physics-based bounce depress scale animations on press
fun Modifier.bounce(interactionSource: MutableInteractionSource): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceAnimation"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// Custom physics-based bounce click modifier for non-Material surfaces
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceClickableScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClick = {
                triggerHapticFeedback(context)
                onClick()
            }
        )
}

// Agnostic Scroll UI fading edge utility for smooth gradient list borders
fun Modifier.verticalFadingEdge(
    fadeLength: Dp = 48.dp
): Modifier = this
    .graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        val fadeLengthPx = fadeLength.toPx()
        val totalHeight = size.height
        if (totalHeight > 0f && fadeLengthPx > 0f) {
            val topStop = (fadeLengthPx / totalHeight).coerceIn(0f, 0.5f)
            val bottomStop = (1f - fadeLengthPx / totalHeight).coerceIn(0.5f, 1f)
            val brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    topStop to Color.Black,
                    bottomStop to Color.Black,
                    1f to Color.Transparent
                )
            )
            drawRect(
                brush = brush,
                blendMode = BlendMode.DstIn
            )
        }
    }

// Render dynamic colored avatar representation
@Composable
fun ProfileAvatarView(
    avatarIndex: Int,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isDark = LocalDarkTheme.current
    val borderAlpha = if (isDark) 0.5f else 0.3f
    val border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = borderAlpha) else Color.Black.copy(alpha = borderAlpha))

    val iconTriple: Triple<androidx.compose.ui.graphics.vector.ImageVector, Color, Color> = when(avatarIndex) {
        0 -> Triple(Icons.Rounded.Person, if (isDark) Color(0xFFA0C2F9) else Color(0xFF3F51B5), if (isDark) Color(0x403F51B5) else Color(0x253F51B5))
        1 -> Triple(Icons.Rounded.LocalFlorist, if (isDark) Color(0xFFD0BCFF) else Color(0xFF6650A4), if (isDark) Color(0x406650A4) else Color(0x256650A4))
        2 -> Triple(Icons.Rounded.Landscape, if (isDark) Color(0xFF80DEEA) else Color(0xFF00838F), if (isDark) Color(0x4000838F) else Color(0x2500838F))
        3 -> Triple(Icons.Rounded.Sailing, if (isDark) Color(0xFF81D4FA) else Color(0xFF0277BD), if (isDark) Color(0x400277BD) else Color(0x250277BD))
        else -> Triple(Icons.Rounded.Person, if (isDark) Color(0xFFB0BEC5) else Color(0xFF546E7A), if (isDark) Color(0x40546E7A) else Color(0x25546E7A))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(iconTriple.third)
            .border(border, CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.bounceClickable(onClick = onClick)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconTriple.first,
            contentDescription = "Profile Avatar",
            tint = iconTriple.second,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

// Hoisted SimpleDateFormat to avoid repeated allocation on every recomposition
private val timestampFormat = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault())
    }
}

// Shared Utility to format timestamps correctly
fun formatTimestamp(timestamp: Long): String {
    return try {
        timestampFormat.get()!!.format(Date(timestamp))
    } catch (_: Exception) {
        "Just Now"
    }
}
