package com.example.ui.screens

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryItem
import com.example.ui.components.*
import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// --- SCREEN 2: HISTORY SCREEN ---
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val items by viewModel.historyItems.collectAsStateWithLifecycle()
    val name by viewModel.profileName.collectAsStateWithLifecycle()
    val avatar by viewModel.profileAvatar.collectAsStateWithLifecycle()

    val onDeleteItem = remember(viewModel) {
        { id: Int -> viewModel.deleteHistoryItem(id) }
    }
    val onAvatarClick = remember(viewModel) {
        { viewModel.setTab(Tab.SettingsProfile) }
    }

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
                onClick = onAvatarClick,
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
                    HistoryLogCard(index = index, item = item, onDelete = onDeleteItem)
                }
            }
        }
    }
}

@Composable
fun HistoryLogCard(
    index: Int = 0,
    item: HistoryItem,
    onDelete: (Int) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current
    val displayUrl = item.resolvedUrl.ifEmpty { item.originalUrl }

    val animAlpha = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(36.dp.value) }

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
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animAlpha.value
                translationY = animOffsetY.value * density
            }
            .clickable {
                // Fixed: Removed spurious overlay permission check.
                // InterceptorActivity is a normal activity, not a system overlay.
                try {
                    val intent = Intent(Intent.ACTION_VIEW, item.originalUrl.toUri()).apply {
                        setClass(context, com.example.InterceptorActivity::class.java)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    android.widget.Toast.makeText(context, "Could not open link.", android.widget.Toast.LENGTH_SHORT).show()
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
                    onDelete(item.id)
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
