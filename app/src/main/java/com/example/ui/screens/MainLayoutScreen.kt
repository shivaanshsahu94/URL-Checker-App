package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.ui.theme.LocalDarkTheme
import com.example.ui.theme.SharedEffects
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StickHistoryIcon
import com.example.ui.components.StickSettingsIcon
import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel

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
                    if (activeTab.isPrimaryTab) {
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
                        if (activeTab.isPrimaryTab) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX < -100 && activeTab is Tab.History) {
                                        viewModel.setTab(Tab.Settings)
                                    } else if (offsetX > 100 && activeTab is Tab.Settings) {
                                        viewModel.setTab(Tab.History)
                                    }
                                    offsetX = 0f
                                },
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { change, dragAmount ->
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
                            if (from is Tab.History && to is Tab.Settings) {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { -it / 3 } + fadeOut()
                                )
                            } else if (from is Tab.Settings && to is Tab.History) {
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
                            Tab.History -> HistoryScreen(viewModel = viewModel)
                            Tab.Settings -> SettingsScreen(viewModel = viewModel)
                            Tab.SettingsHistory -> HistoryManagementSubpage(viewModel = viewModel)
                            Tab.SettingsProfile -> ProfileSetupPage(viewModel = viewModel)
                            Tab.SettingsChecker -> CheckerSetupPage(viewModel = viewModel)
                            Tab.SettingsTheme -> ThemeSetupPage(viewModel = viewModel)
                            Tab.SettingsHelp -> HelpFeedbackPage(viewModel = viewModel)
                            Tab.SettingsAbout -> AboutSubpage(viewModel = viewModel)
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

@Composable
fun FloatingBottomNavigation(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    val bgColor = if (isDark) {
        Color(0x35122030) // Deep transparent obsidian blue
    } else {
        Color(0x75FFFFFF) // Highly translucent frosty white tint
    }
    val border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.55f))
    val navShape = RoundedCornerShape(50)

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
                    .graphicsLayer { renderEffect = SharedEffects.navBarBlur }
                    .background(bgColor)
            )

            // Reflective edge stroke
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(navShape)
                    .border(border, navShape)
            )

            val items = listOf(Tab.History to "History", Tab.Settings to "Settings")
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

                            if (tab is Tab.History) {
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
                                modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                            )
                        }
                    }
                }
            } // Close Row
            } // Close extra Box
        } // Close inner Box
    } // Close outer Box
}
