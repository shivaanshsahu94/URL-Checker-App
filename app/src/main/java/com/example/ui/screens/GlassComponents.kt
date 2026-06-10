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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.SharedEffects

// Reusable Translucent Card background component with hyper-realistic liquid glass & frosted glassmorphism effect
@Composable
fun TranslucentGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    border: BorderStroke? = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalDarkTheme.current
    val bgColor = if (isDark) {
        Color(0x23111827) // ~14% opacity premium obsidian dark tint
    } else {
        Color(0x40FFFFFF) // ~25% opacity frosted white tint
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceScale"
    )



    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = androidx.compose.foundation.LocalIndication.current,
                            onClick = onClick
                        )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Blur background overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .graphicsLayer { renderEffect = SharedEffects.glassBlur }
                .background(bgColor)
        )

        // Glossy outline borders for light-refraction simulation
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
        )

        // High gloss, perfectly sharp and legible content
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

// Standalone glossy, tactile, glassmorphic pill button
@Composable
fun GlassyPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    testTag: String = "",
    content: @Composable RowScope.() -> Unit
) {
    val isDark = LocalDarkTheme.current
    val defaultContainerColor = containerColor ?: if (isDark) {
        Color(0x30FFFFFF)
    } else {
        Color(0x45FFFFFF)
    }
    val defaultContentColor = contentColor ?: if (isDark) {
        Color.White
    } else {
        MaterialTheme.colorScheme.primary
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "buttonScale"
    )



    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Blurred Glass Base
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(50))
                .graphicsLayer { renderEffect = SharedEffects.pillBlur }
                .background(defaultContainerColor)
        )

        // Reflection Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(50))
                .border(border ?: BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(50))
        )

        // Crisp Content Overlay with wrap content size and breathing padding inside
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                content()
            }
        }
    }
}

// Standalone glossy, tactile, glassmorphic circular button/FAB
@Composable
fun GlassyCircularButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    border: BorderStroke? = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)),
    testTag: String = "",
    content: @Composable () -> Unit
) {
    val isDark = LocalDarkTheme.current
    val defaultContainerColor = containerColor ?: if (isDark) {
        Color(0x30FFFFFF)
    } else {
        Color(0x45FFFFFF)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "buttonScale"
    )



    Box(
        modifier = modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Blurred Glass Base
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .graphicsLayer { renderEffect = SharedEffects.pillBlur }
                .background(defaultContainerColor)
        )

        // Reflection Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(border ?: BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)), CircleShape)
        )

        // Crisp Content Overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun GlassBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "back_to_settings_btn"
) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current
    val backInteractionSource = remember { MutableInteractionSource() }
    val backIsPressed by backInteractionSource.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        targetValue = if (backIsPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "backBounceScale"
    )



    Box(
        modifier = modifier
            .testTag(testTag)
            .size(44.dp)
            .graphicsLayer {
                scaleX = backScale
                scaleY = backScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = backInteractionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) {
                triggerHapticFeedback(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val backBgColor = if (isDark) Color(0x35122030) else Color(0x40FFFFFF)
        val backBorderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.55f)

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .graphicsLayer { renderEffect = SharedEffects.pillBlur }
                .background(backBgColor)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(BorderStroke(1.2.dp, backBorderColor), CircleShape)
        )

        StickArrowBackIcon(
            color = if (isDark) Color.White else Color.Black,
            modifier = Modifier.size(18.dp)
        )
    }
}

// Background glassmorphism ambient canvas simulation with vibrant glowing neon orbs under ultra blur
@Composable
fun GlassmorphicBackgroundContainer(
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalDarkTheme.current
    val bgColor = if (isDark) {
        Color(0x450F172A) // Truly translucent deep background
    } else {
        Color(0x35EDF2F7) // Truly translucent frosty background
    }



    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Blur Background layer: applying real-time blur over the translucent background window
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { renderEffect = SharedEffects.glassBlur }
                .background(bgColor)
        )

        // Content layer (drawn crisp and unblurred on top)
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
