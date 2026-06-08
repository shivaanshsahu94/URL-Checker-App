package com.example.ui.screens

import android.app.role.RoleManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.rounded.Sailing
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.Icons
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ChainStyle
import com.example.data.HistoryItem
import com.example.ui.components.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Replaced with beautiful curved shape to support exclusively curved geometry overhauled globally
val AsymmetricalCardShape = RoundedCornerShape(32.dp)

// Flower Scalloped Custom Shape for the primary browser selector
val FlowerShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = size.width / 2f
            val minRadius = maxRadius * 0.72f
            val petals = 12

            for (i in 0..360) {
                val angleRad = Math.toRadians(i.toDouble())
                val radius = minRadius + (maxRadius - minRadius) * abs(sin(petals * angleRad / 2.0)).toFloat()
                val x = centerX + radius * cos(angleRad).toFloat()
                val y = centerY + radius * sin(angleRad).toFloat()
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            close()
        }
        return Outline.Generic(path)
    }
}

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
    } catch (e: Exception) {
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
    } catch (e: Exception) {
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

@Composable
fun GlassBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "back_to_settings_btn"
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
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

    val backBlurEffect = remember {
        android.graphics.RenderEffect.createBlurEffect(
            20f, 20f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

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
                .graphicsLayer { renderEffect = backBlurEffect }
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

// Reusable Translucent Card background component with hyper-realistic liquid glass & frosted glassmorphism effect
@Composable
fun TranslucentGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    border: BorderStroke? = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
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

    val cardBlurEffect = remember {
        android.graphics.RenderEffect.createBlurEffect(
            30f, 30f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

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
                .graphicsLayer { renderEffect = cardBlurEffect }
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
    val isDark = isSystemInDarkTheme()
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

    val pillBlurEffect = remember {
        android.graphics.RenderEffect.createBlurEffect(
            20f, 20f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

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
                .graphicsLayer { renderEffect = pillBlurEffect }
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
    val isDark = isSystemInDarkTheme()
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

    val circularBlurEffect = remember {
        android.graphics.RenderEffect.createBlurEffect(
            20f, 20f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

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
                .clip(androidx.compose.foundation.shape.CircleShape)
                .graphicsLayer { renderEffect = circularBlurEffect }
                .background(defaultContainerColor)
        )

        // Reflection Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(androidx.compose.foundation.shape.CircleShape)
                .border(border ?: BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)), androidx.compose.foundation.shape.CircleShape)
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

// Background glassmorphism ambient canvas simulation with vibrant glowing neon orbs under ultra blur
@Composable
fun GlassmorphicBackgroundContainer(
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        Color(0x450F172A) // Truly translucent deep background
    } else {
        Color(0x35EDF2F7) // Truly translucent frosty background
    }

    val ambientBlurEffect = remember {
        android.graphics.RenderEffect.createBlurEffect(
            30f, 30f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Blur Background layer: applying real-time blur over the translucent background window
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { renderEffect = ambientBlurEffect }
                .background(bgColor)
        )

        // Content layer (drawn crisp and unblurred on top)
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

// Render dynamic colored avatar representation
@Composable
fun ProfileAvatarView(
    avatarIndex: Int,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
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

// Shared Utility to format timestamps correctly
fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Just Now"
    }
}

// Main Selector router Screen
@Composable
fun MainLayoutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDefault by viewModel.isDefaultBrowser.collectAsStateWithLifecycle()
    val isIntercepted by viewModel.isLinkIntercepted.collectAsStateWithLifecycle()
    val activeTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    GlassmorphicBackgroundContainer {
        if (!isDefault) {
            WelcomeScreen(viewModel = viewModel)
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.systemBars,
                bottomBar = {
                    if (activeTab == "history" || activeTab == "settings") {
                        FloatingBottomNavigation(
                            selectedTab = activeTab,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                var offsetX by remember { mutableFloatStateOf(0f) }
                
                Box(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .pointerInput(activeTab) {
                        if (activeTab == "history" || activeTab == "settings") {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX < -100 && activeTab == "history") {
                                        viewModel.setTab("settings")
                                    } else if (offsetX > 100 && activeTab == "settings") {
                                        viewModel.setTab("history")
                                    }
                                    offsetX = 0f
                                },
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float ->
                                    change.consume()
                                    offsetX += dragAmount
                                }
                            )
                        }
                    }
                ) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            val from = initialState
                            val to = targetState
                            if (from == "history" && to == "settings") {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { -it / 3 } + fadeOut()
                                )
                            } else if (from == "settings" && to == "history") {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { -it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { it / 3 } + fadeOut()
                                )
                            } else {
                                (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300))).togetherWith(
                                    fadeOut(animationSpec = tween(150))
                                )
                            }
                        },
                        label = "tabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            "history" -> HistoryScreen(viewModel = viewModel)
                            "settings" -> SettingsScreen(viewModel = viewModel)
                            "settings_history" -> HistoryManagementSubpage(viewModel = viewModel)
                            "settings_profile" -> ProfileSetupPage(viewModel = viewModel)
                            "settings_checker" -> CheckerSetupPage(viewModel = viewModel)
                            "settings_theme" -> ThemeSetupPage(viewModel = viewModel)
                            "settings_help" -> HelpFeedbackPage(viewModel = viewModel)
                            "settings_about" -> AboutSubpage(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Active float overlay dialog when URL intercepts
        if (isIntercepted) {
            InterceptorPopupLayout(viewModel = viewModel)
        }
    }
}

// --- SCREEN 1: ONBOARDING WELCOME ---
@Composable
fun WelcomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val roleLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.checkDefaultBrowser()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager?.isRoleHeld(RoleManager.ROLE_BROWSER) == true) {
                viewModel.setTab("history")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(28.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StickSecurityIcon(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "URL Checker",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your secure link interceptor and automated redirect unmasker. Verify shortening tracks, prevent fishing hooks, and audit landing safety safely.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

        val buttonInteractionSource = remember { MutableInteractionSource() }
        Button(
            onClick = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                    val requestIntent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                    if (requestIntent != null) {
                        roleLauncher.launch(requestIntent)
                    }
                } else {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    roleLauncher.launch(intent)
                }
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            interactionSource = buttonInteractionSource,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.85f)
                .bounce(buttonInteractionSource)
                .testTag("set_default_browser_btn"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Set as Default Browser",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val textButtonInteractionSource = remember { MutableInteractionSource() }
        TextButton(
            onClick = { viewModel.checkDefaultBrowser() },
            interactionSource = textButtonInteractionSource,
            modifier = Modifier
                .bounce(textButtonInteractionSource)
                .testTag("refresh_status_btn")
        ) {
            Text(
                text = "Check Setup Status",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

// --- SCREEN 2: HISTORY SCREEN ---
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val items by viewModel.historyItems.collectAsStateWithLifecycle()
    val name by viewModel.profileName.collectAsStateWithLifecycle()
    val avatar by viewModel.profileAvatar.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Top header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Link History",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            ProfileAvatarView(
                avatarIndex = avatar,
                size = 42.dp,
                onClick = { viewModel.setTab("settings_profile") },
                modifier = Modifier.testTag("onboarding_avatar_btn")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StickHistoryIcon(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "History logs are empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Click browser links inside apps to intercept & trace them here instantly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .verticalFadingEdge(fadeLength = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    HistoryLogCard(index = index, item = item, onDelete = { viewModel.deleteHistoryItem(item.id) })
                }
            }
        }
    }
}

@Composable
fun HistoryLogCard(
    index: Int = 0,
    item: HistoryItem,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val displayUrl = item.resolvedUrl.ifEmpty { item.originalUrl }

    val animAlpha = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(36.dp.value) } // Elegant, standard conversion

    LaunchedEffect(key1 = item.id) {
        val staggerDelay = (index * 45L).coerceAtMost(300L)
        kotlinx.coroutines.delay(staggerDelay)
        
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            animOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    TranslucentGlassCard(
        shape = RoundedCornerShape(24.dp), // matched perfectly with reference image
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animAlpha.value
                translationY = animOffsetY.value * density
            }
            .clickable {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
                    android.widget.Toast.makeText(context, "Please grant 'Appear on top' permission in Settings.", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(item.originalUrl)).apply {
                            setClass(context, com.example.InterceptorActivity::class.java)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Please grant 'Appear on top' permission.", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Beautifully wrapped, high-readability url
                Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Date and time secondary line on left
                Text(
                    text = formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Clean outline trash can on right with tactile feedback
            val trashInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = {
                    triggerSuccessHaptic(context)
                    onDelete()
                },
                interactionSource = trashInteraction,
                modifier = Modifier
                    .size(44.dp)
                    .bounce(trashInteraction)
                    .testTag("delete_log_${item.id}")
            ) {
                StickDustbinIcon(
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// --- SCREEN 3: SETTINGS VIEW ---
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val name by viewModel.profileName.collectAsStateWithLifecycle()
    val avatar by viewModel.profileAvatar.collectAsStateWithLifecycle()
    val checker by viewModel.defaultChecker.collectAsStateWithLifecycle()
    val themeChoice by viewModel.appTheme.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            ProfileAvatarView(
                avatarIndex = avatar,
                size = 42.dp,
                onClick = { viewModel.setTab("settings_profile") }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .verticalFadingEdge(fadeLength = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
        ) {
            item {
                SettingsOptionCard(
                    icon = { StickProfileIcon(color = if (isSystemInDarkTheme()) Color.White else Color.Black, modifier = Modifier.size(24.dp)) },
                    title = "Profile",
                    subtitle = "Local Name & Photo Setup\n(No personal info required)",
                    onClick = { viewModel.setTab("settings_profile") },
                    testTag = "settings_profile_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickSecurityIcon(color = if (isSystemInDarkTheme()) Color.White else Color.Black, modifier = Modifier.size(24.dp)) },
                    title = "Default URL Checker",
                    subtitle = "e.g., VirusTotal",
                    onClick = { viewModel.setTab("settings_checker") },
                    testTag = "settings_checker_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickThemeIcon(color = if (isSystemInDarkTheme()) Color.White else Color.Black, modifier = Modifier.size(24.dp)) },
                    title = "Theme",
                    subtitle = "System Default / Light / Dark",
                    onClick = { viewModel.setTab("settings_theme") },
                    testTag = "settings_theme_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickHistoryIcon(color = if (isSystemInDarkTheme()) Color.White else Color.Black, modifier = Modifier.size(24.dp)) },
                    title = "History Management",
                    subtitle = "Auto-Delete, Clear History options\n(Sub-page)",
                    onClick = { viewModel.setTab("settings_history") },
                    testTag = "settings_history_nav_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickAboutIcon(color = if (isSystemInDarkTheme()) Color.White else Color.Black, modifier = Modifier.size(24.dp)) },
                    title = "About",
                    subtitle = "0.9.50(beta4)",
                    onClick = { viewModel.setTab("settings_about") },
                    testTag = "settings_about_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickHelpIcon(color = if (isSystemInDarkTheme()) Color.White else Color.Black, modifier = Modifier.size(24.dp)) },
                    title = "Help & Feedback",
                    subtitle = "Contact us, provide suggestions",
                    onClick = { viewModel.setTab("settings_help") },
                    testTag = "settings_help_btn"
                )
            }
        }
    }
}

@Composable
fun SettingsOptionCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val isDark = isSystemInDarkTheme()
    TranslucentGlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp), // Rounded corner cards perfectly matching images
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                     text = title,
                     style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                     color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                     text = subtitle,
                     style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Elegantly clean, sharp arrow right icon pointing forward
            StickArrowBackIcon(
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = 180f }
            )
        }
    }
}

// --- SCREEN 4: HISTORY MANAGEMENT SUBPAGE ---
@Composable
fun HistoryManagementSubpage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab("settings") }
    val context = LocalContext.current
    val saveActive by viewModel.saveHistory.collectAsStateWithLifecycle()
    val deleteDays by viewModel.autoDeleteDays.collectAsStateWithLifecycle()

    var showDropdownMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBackIconButton(
                onClick = { viewModel.setTab("settings") },
                testTag = "back_to_settings_btn"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "History Management",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TranslucentGlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Save Link History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Store unmasked click paths on local DB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = saveActive,
                    onCheckedChange = { viewModel.updateSaveHistory(it) },
                    modifier = Modifier.testTag("save_history_switch")
                )
            }
        }

        // Conditional display based on history toggle
        if (saveActive) {
            Spacer(modifier = Modifier.height(16.dp))

            TranslucentGlassCard {
                Column {
                    Text(
                        text = "Auto-Delete Logging Period",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select time parameter to erase historic logs dynamically:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box {
                        Button(
                            onClick = { showDropdownMenu = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auto_delete_dropdown_btn")
                        ) {
                            Text(
                                text = if (deleteDays == 1) "24 Hours" else "Every $deleteDays Days",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            listOf(1, 15, 30, 60).forEach { days ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = if (days == 1) "24 Hours" else "Every $days Days", 
                                            modifier = Modifier.testTag("select_days_$days")
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.updateAutoDeleteDays(days)
                                        showDropdownMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showClearConfirmation = true },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            ),
            modifier = Modifier
                .height(52.dp)
                .fillMaxWidth()
                .testTag("clear_all_logs_btn")
        ) {
            Text(
                text = "Erase All Logs Now",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Databases") },
            text = { Text("Confirm erasure process. Once selected, link unmasker logs are completely removed permanently from your local device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerSuccessHaptic(context)
                        viewModel.clearAllLogs()
                        showClearConfirmation = false
                    },
                    modifier = Modifier.testTag("confirm_clear_logs_btn")
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- SCREEN 5: TRANSLUCENT FLOATING POPUP OVERLAY ---
@Composable
fun InterceptorPopupLayout(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Define pre-calculated / remembered popup shared blur effect at the function scope
    val popupBackdropBlur = remember {
        android.graphics.RenderEffect.createBlurEffect(
            30f, 30f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

    BackHandler {
        viewModel.closeInterceptor()
        (context as? android.app.Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        triggerSuccessHaptic(context)
    }

    val original by viewModel.originalUrl.collectAsStateWithLifecycle()
    val resolved by viewModel.resolvedUrl.collectAsStateWithLifecycle()
    val isResolving by viewModel.isResolving.collectAsStateWithLifecycle()

    // Floating Center Screen Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.closeInterceptor()
                (context as? android.app.Activity)?.finish()
            },
        contentAlignment = Alignment.Center
    ) {
        // Broad screen real-time blur backdrop
        val fullBgColor = if (isDark) Color(0x350A0E17) else Color(0x25EDF2F7)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { renderEffect = popupBackdropBlur }
                .background(fullBgColor)
        )

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            val (glassCardRef, searchIconRef) = createRefs()

            // The main URL container card: inherits TranslucentGlassCard style perfectly!
            TranslucentGlassCard(
                shape = RoundedCornerShape(36.dp),
                border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(glassCardRef) {
                        top.linkTo(parent.top, margin = 36.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                // Content overlay (Crisp and readable)
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 56.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Redirect Security Scan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isResolving) {
                        // Intermediate Tracing Screen loader with GlassCard
                        TranslucentGlassCard(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tracing Redirect Tracks...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = original,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.61f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Prominent URL display card using TranslucentGlassCard
                        TranslucentGlassCard(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Column {
                                Text(
                                    text = "Original Link:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = original,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Unmasked Destination:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = resolved,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Secondary actions: Copy & Share as GlassyPillButtons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassyPillButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Resolved URL", resolved)
                                    clipboard?.setPrimaryClip(clip)
                                    triggerSuccessHaptic(context)
                                    android.widget.Toast.makeText(context, "URL copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                testTag = "copy_resolved_btn",
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StickCopyIcon(
                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Copy URL", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            GlassyPillButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, resolved)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share URL Target"))
                                },
                                testTag = "share_resolved_btn",
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StickShareIcon(
                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Share URL", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Pill: Cancel Button
                        GlassyPillButton(
                            onClick = { 
                                viewModel.closeInterceptor()
                                (context as? android.app.Activity)?.finish()
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                            testTag = "cancel_popup_btn",
                            modifier = Modifier.weight(1f)
                        ) {
                            StickCloseIcon(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cancel", 
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                             )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Middle: Central circle open browser button (M3 primary colored!)
                        GlassyCircularButton(
                            onClick = {
                                resolved?.takeIf { it.isNotEmpty() }?.let { urlToOpen ->
                                    try {
                                        Intent(Intent.ACTION_VIEW, android.net.Uri.parse(urlToOpen)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            run {
                                                // Find external browser package within a strict GC scope
                                                val pm = context.packageManager
                                                val httpIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://www.google.com")).apply {
                                                    addCategory(Intent.CATEGORY_BROWSABLE)
                                                }
                                                val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                    pm.queryIntentActivities(httpIntent, android.content.pm.PackageManager.ResolveInfoFlags.of(0L))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    pm.queryIntentActivities(httpIntent, 0)
                                                }
                                                var targetBrowserPackage: String? = null
                                                for (info in resolveInfos) {
                                                    val pkg = info.activityInfo.packageName
                                                    if (pkg != context.packageName) {
                                                        targetBrowserPackage = pkg
                                                        break
                                                    }
                                                }
                                                setPackage(targetBrowserPackage ?: "com.android.chrome")
                                            }
                                        }.let { browserIntent ->
                                            context.startActivity(Intent.createChooser(browserIntent, "Open Browser"))
                                        }
                                    } catch (anfe: android.content.ActivityNotFoundException) {
                                        anfe.printStackTrace()
                                        android.widget.Toast.makeText(context, "No default browser found: ${anfe.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context, "Unexpected URL: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } ?: run {
                                    android.widget.Toast.makeText(context, "URL is null or empty", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                viewModel.closeInterceptor()
                                (context as? android.app.Activity)?.finish()
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            border = BorderStroke(0.dp, Color.Transparent),
                            testTag = "open_browser_scalloped_btn"
                        ) {
                            StickBrowserIcon(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Pill: Check URL (VirusTotal) Button (M3 primaryContainer/onPrimaryContainer styled!)
                        GlassyPillButton(
                            onClick = {
                                resolved?.takeIf { it.isNotEmpty() }?.let { urlToCheck ->
                                    run {
                                        // Scope clipboard operations separately
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Resolved URL", urlToCheck)
                                        clipboard?.setPrimaryClip(clip)
                                    }
                                    android.widget.Toast.makeText(context, "URL copied to clipboard. Please paste it in the search box.", android.widget.Toast.LENGTH_LONG).show()
                                    
                                    val vtUrl = "https://www.virustotal.com/gui/home/url"
                                    try {
                                        Intent(Intent.ACTION_VIEW, android.net.Uri.parse(vtUrl)).apply {
                                            setPackage("com.android.chrome")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }.let { vtIntent ->
                                            context.startActivity(vtIntent)
                                        }
                                    } catch (anfe: android.content.ActivityNotFoundException) {
                                        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER).apply {
                                            data = android.net.Uri.parse(vtUrl)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }.let { fallbackIntent ->
                                            context.startActivity(fallbackIntent)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context, "Unexpected error: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } ?: run {
                                    android.widget.Toast.makeText(context, "URL is null or empty", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                viewModel.closeInterceptor()
                                (context as? android.app.Activity)?.finish()
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            testTag = "check_virustotal_btn",
                            modifier = Modifier.weight(1f)
                        ) {
                            StickSecurityIcon(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Check URL", 
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // The Overlapping stick icon (magnifying glass) sits on the exact top edge of the surface popup (Y-center constrained)
            val circleShape = RoundedCornerShape(50)
            val glassBgColor = if (isDark) Color(0x23111827) else Color(0x40FFFFFF)
            val glassBorder = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f))

            // Overlapping search icon card component with custom shape and glassmorphism styling
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .constrainAs(searchIconRef) {
                        top.linkTo(glassCardRef.top)
                        bottom.linkTo(glassCardRef.top)
                        start.linkTo(glassCardRef.start)
                        end.linkTo(glassCardRef.end)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Liquid glass blur underlay for the circular container
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(circleShape)
                        .graphicsLayer { renderEffect = popupBackdropBlur }
                        .background(glassBgColor)
                )

                // Bright semi-transparent reflective border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(circleShape)
                        .border(glassBorder, circleShape)
                )

                Image(
                    painter = painterResource(id = com.example.R.drawable.ic_generated_logo),
                    contentDescription = "App Icon Header",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

/**
 * Circumvent recursive default browser loops to open links safely in external web browsers.
 * Since this application is registered dynamically as a default handler for HTTP/HTTPS intents,
 * calling standard startActivity(intent) would trigger our own app interceptor again, resulting
 * in an infinite loop of popups. This function bypasses the self-open cycle by querying the
 * system's browser ecosystem, filtering out our package name, and explicitly targeting an
 * external browser package or using a dummy selector fallback strategy.
 */
private fun bypassSelfOpenUrl(context: Context, url: String) {
    val parsedUri = android.net.Uri.parse(url)
    val baseIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }

    val pm = context.packageManager
    val activities = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(baseIntent, android.content.pm.PackageManager.ResolveInfoFlags.of(0L))
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(baseIntent, 0)
    }
    val ourApp = context.packageName

    // Identify standard browser alternative packages that are not our application
    val otherBrowser = activities.firstOrNull {
        it.activityInfo.packageName != ourApp
    }

    if (otherBrowser != null) {
        val browserIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
            setClassName(otherBrowser.activityInfo.packageName, otherBrowser.activityInfo.name)
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(browserIntent)
            return
        } catch (e: Exception) {
            // Revert back on failure and seek alternative fallback schemes
        }
    }

    // Classic Android selector bypass fallback:
    // Attaching a dummy selector restricted to general browser protocols forces the Intent
    // system to ignore the default app-specific association and delegate selection outward.
    val dummySelector = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://")).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    val fallbackBaseIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        selector = dummySelector
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(fallbackBaseIntent)
    } catch (e: Exception) {
        val simpleIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(simpleIntent)
    }
}

// --- SUBDIALOGS & ALERTS ---

@Composable
fun ProfileSetupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.profileName.collectAsStateWithLifecycle()
    val avatarIdx by viewModel.profileAvatar.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf(name) }
    var selectedIdx by remember { mutableStateOf(avatarIdx) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure Local Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Render dynamic colored avatar options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    (0..3).forEach { index ->
                        val isSelected = index == selectedIdx
                        Box(
                            modifier = Modifier
                                .then(
                                    if (isSelected) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        CircleShape
                                    ) else Modifier
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileAvatarView(
                                avatarIndex = index,
                                size = 48.dp,
                                onClick = { selectedIdx = index }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Display Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.updateProfileName(textInput)
                            viewModel.updateProfileAvatar(selectedIdx)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("save_profile_dialog_btn")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckerSetupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val checker by viewModel.defaultChecker.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Select Default URL Checker",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                listOf("VirusTotal", "Google Safe Browsing", "URLVoid").forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateDefaultChecker(item)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (checker == item),
                            onClick = {
                                viewModel.updateDefaultChecker(item)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSetupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val themeChoice by viewModel.appTheme.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Choose Application Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                listOf("System", "Light", "Dark").forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateTheme(choice)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (themeChoice == choice),
                            onClick = {
                                viewModel.updateTheme(choice)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = choice, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun HelpFeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Help & Feedback",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (submitted) {
                    Text(
                        text = "Thank you for your feedback! We will review your trace suggestions soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(onClick = onDismiss, shape = RoundedCornerShape(50)) {
                        Text("Close")
                    }
                } else {
                    Text(
                        text = "To unmask shortened domains (e.g. bit.ly, tinyurl), simply tap any browser link. Make sure URL Checker is configured as your active Default Browser app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Your Feedback & Bug Reports") },
                        placeholder = { Text("Write comments or suggest additions here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("feedback_input_field"),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    val currentFeedback = feedbackText
                                    android.widget.Toast.makeText(context, "Sending...", android.widget.Toast.LENGTH_SHORT).show()
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val client = OkHttpClient.Builder()
                                                .followRedirects(true)
                                                .followSslRedirects(true)
                                                .connectTimeout(10, TimeUnit.SECONDS)
                                                .readTimeout(10, TimeUnit.SECONDS)
                                                .writeTimeout(10, TimeUnit.SECONDS)
                                                .build()

                                            val jsonPayload = org.json.JSONObject().apply {
                                                put("content", "New App Feedback: $currentFeedback")
                                            }.toString()

                                            val mediaType = "application/json; charset=utf-8".toMediaType()
                                            val requestBody = jsonPayload.toRequestBody(mediaType)
                                            val request = Request.Builder()
                                                .url(com.example.util.SecurityConfig.getFeedbackUrl())
                                                .post(requestBody)
                                                .build()

                                            client.newCall(request).execute().use { response ->
                                                if (response.isSuccessful || response.code == 204 || response.code == 200) {
                                                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        feedbackText = ""
                                                        submitted = true
                                                        onDismiss()
                                                        android.widget.Toast.makeText(context, "Feedback sent successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        android.widget.Toast.makeText(context, "Failed to send. Please check your internet connection.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                android.widget.Toast.makeText(context, "Failed to send. Please check your internet connection.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Please enter your feedback text first", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("submit_feedback_btn")
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun FloatingBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        Color(0x35122030) // Deep transparent obsidian blue
    } else {
        Color(0x75FFFFFF) // Highly translucent frosty white tint
    }
    val border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.55f))
    val navShape = RoundedCornerShape(50)

    val navBlurEffect = remember {
        android.graphics.RenderEffect.createBlurEffect(
            25f, 25f, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Blur Background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(navShape)
                    .graphicsLayer { renderEffect = navBlurEffect }
                    .background(bgColor)
            )

            // Reflective edge stroke
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(navShape)
                    .border(border, navShape)
            )

            val items = listOf("history" to "History", "settings" to "Settings")
            val activeIndex = items.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val pillWidth = maxWidth / items.size
                    val offsetX by animateDpAsState(
                        targetValue = pillWidth * activeIndex,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "pillOffset"
                    )
                    
                    Box(
                        modifier = Modifier
                            .offset(x = offsetX)
                            .width(pillWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(if (isDark) Color(0xFF3F51B5) else Color(0xFFA0C2F9))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { (tab, label) ->
                        val isActive = selectedTab == tab
                        val itemBgColor = Color.Transparent

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.90f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "itemBounce"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(50))
                            .background(itemBgColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current
                            ) { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val iconColor = if (isActive) {
                                if (isDark) Color.White else Color(0xFF1F2937)
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                            }

                            if (tab == "history") {
                                StickHistoryIcon(color = iconColor, modifier = Modifier.size(18.dp))
                            } else {
                                StickSettingsIcon(color = iconColor, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = iconColor,
                                modifier = Modifier.testTag("nav_tab_${tab}")
                            )
                        }
                    }
                }
            } // Close Row
            } // Close extra Box
        } // Close inner Box
    } // Close outer Box
}

// --- SUBPAGES ---

@Composable
fun ProfileSetupPage(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    BackHandler { viewModel.setTab("settings") }

    val name by viewModel.profileName.collectAsStateWithLifecycle()
    val avatarIdx by viewModel.profileAvatar.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf(name) }
    var selectedIdx by remember { mutableStateOf(avatarIdx) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBackIconButton(
                onClick = { viewModel.setTab("settings") },
                testTag = "back_to_settings_btn"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Profile Setup",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TranslucentGlassCard {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure Local Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Render dynamic colored avatar options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    (0..3).forEach { index ->
                        val isSelected = index == selectedIdx
                        Box(
                            modifier = Modifier
                                .then(
                                    if (isSelected) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        CircleShape
                                    ) else Modifier
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileAvatarView(
                                avatarIndex = index,
                                size = 48.dp,
                                onClick = { selectedIdx = index }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Display Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { viewModel.setTab("settings") }
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.updateProfileName(textInput)
                            viewModel.updateProfileAvatar(selectedIdx)
                            viewModel.setTab("settings")
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("save_profile_dialog_btn")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckerSetupPage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab("settings") }
    val checker by viewModel.defaultChecker.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBackIconButton(
                onClick = { viewModel.setTab("settings") },
                testTag = "back_to_settings_btn"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Default URL Checker",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TranslucentGlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Default URL Checker",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                listOf("VirusTotal", "Google Safe Browsing", "URLVoid").forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateDefaultChecker(item)
                                viewModel.setTab("settings")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (checker == item),
                            onClick = {
                                viewModel.updateDefaultChecker(item)
                                viewModel.setTab("settings")
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item, 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSetupPage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab("settings") }
    val themeChoice by viewModel.appTheme.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBackIconButton(
                onClick = { viewModel.setTab("settings") },
                testTag = "back_to_settings_btn"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Theme Config",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TranslucentGlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Choose Application Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                listOf("System", "Light", "Dark").forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateTheme(choice)
                                viewModel.setTab("settings")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (themeChoice == choice),
                            onClick = {
                                viewModel.updateTheme(choice)
                                viewModel.setTab("settings")
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = choice, 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HelpFeedbackPage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab("settings") }
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBackIconButton(
                onClick = { viewModel.setTab("settings") },
                testTag = "back_to_settings_btn"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Feedback & Support",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TranslucentGlassCard {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Help & Feedback Support",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (submitted) {
                    Text(
                        text = "Thank you for your feedback! We will review your suggestions soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.setTab("settings") }, 
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Close")
                    }
                } else {
                    Text(
                        text = "To unmask shortened domains (e.g. bit.ly, tinyurl), simply tap any browser link. Make sure URL Checker is configured as your active Default Browser app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Your Feedback & Bug Reports") },
                        placeholder = { Text("Write comments or suggest additions here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("feedback_input_field"),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { viewModel.setTab("settings") }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    val currentFeedback = feedbackText
                                    android.widget.Toast.makeText(context, "Sending...", android.widget.Toast.LENGTH_SHORT).show()
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val client = OkHttpClient.Builder()
                                                .followRedirects(true)
                                                .followSslRedirects(true)
                                                .connectTimeout(10, TimeUnit.SECONDS)
                                                .readTimeout(10, TimeUnit.SECONDS)
                                                .writeTimeout(10, TimeUnit.SECONDS)
                                                .build()

                                            val jsonPayload = org.json.JSONObject().apply {
                                                put("content", "New App Feedback: $currentFeedback")
                                            }.toString()

                                            val mediaType = "application/json; charset=utf-8".toMediaType()
                                            val requestBody = jsonPayload.toRequestBody(mediaType)
                                            val request = Request.Builder()
                                                .url(com.example.util.SecurityConfig.getFeedbackUrl())
                                                .post(requestBody)
                                                .build()

                                            client.newCall(request).execute().use { response ->
                                                if (response.isSuccessful || response.code == 204 || response.code == 200) {
                                                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        feedbackText = ""
                                                        submitted = true
                                                        android.widget.Toast.makeText(context, "Feedback sent successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        android.widget.Toast.makeText(context, "Failed to send. Please check your internet connection.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                android.widget.Toast.makeText(context, "Failed to send. Please check your internet connection.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Please enter your feedback text first", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("submit_feedback_btn")
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSubpage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab("settings") }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBackIconButton(
                onClick = { viewModel.setTab("settings") },
                testTag = "back_to_settings_btn"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TranslucentGlassCard {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Newly generated app icon cleanly wrapped in a circular container with bright semi-transparent reflective border
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                        .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_generated_logo),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Application Name
                Text(
                    text = "URL Checker",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version Number
                Text(
                    text = "0.9.50(beta4)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Redirect security scan unmasks hidden destination targets, prevents trackers, and scans domains safely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                val devCreditString = buildAnnotatedString {
                    append("Developed by Shivaansh Sahu (")
                    pushStringAnnotation(tag = "URL", annotation = "https://github.com/shivaanshsahu94")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("@shivaanshsahu94")
                    }
                    pop()
                    append(")")
                }
                ClickableText(
                    text = devCreditString,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    onClick = { offset ->
                        devCreditString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val url = annotation.item
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                        setPackage("com.android.chrome")
                                    }
                                    context.startActivity(intent)
                                } catch (e: android.content.ActivityNotFoundException) {
                                    val fallbackIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER).apply {
                                        data = android.net.Uri.parse(url)
                                    }
                                    context.startActivity(fallbackIntent)
                                }
                            }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.setTab("settings") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("about_ok_btn")
                ) {
                    Text("OK")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val url = "https://github.com/shivaanshsahu94/URL-Checker-App/releases"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                setPackage("com.android.chrome")
                            }
                            context.startActivity(intent)
                        } catch (e: android.content.ActivityNotFoundException) {
                            val fallbackIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER).apply {
                                data = android.net.Uri.parse(url)
                            }
                            context.startActivity(fallbackIntent)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("about_github_releases_btn")
                ) {
                    Text("View GitHub Releases")
                }
            }
        }
    }
}
