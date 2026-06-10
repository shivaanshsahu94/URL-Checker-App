package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.*
import com.example.ui.navigation.Tab
import com.example.ui.theme.LocalDarkTheme
import com.example.viewmodel.MainViewModel

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
                onClick = { viewModel.setTab(Tab.SettingsProfile) }
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
                    icon = { StickProfileIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                    title = "Profile",
                    subtitle = "Local Name & Photo Setup\n(No personal info required)",
                    onClick = { viewModel.setTab(Tab.SettingsProfile) },
                    testTag = "settings_profile_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickSecurityIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                    title = "Default URL Checker",
                    subtitle = "e.g., VirusTotal",
                    onClick = { viewModel.setTab(Tab.SettingsChecker) },
                    testTag = "settings_checker_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickThemeIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                    title = "Theme",
                    subtitle = "System Default / Light / Dark",
                    onClick = { viewModel.setTab(Tab.SettingsTheme) },
                    testTag = "settings_theme_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickHistoryIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                    title = "History Management",
                    subtitle = "Auto-Delete, Clear History options\n(Sub-page)",
                    onClick = { viewModel.setTab(Tab.SettingsHistory) },
                    testTag = "settings_history_nav_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickAboutIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                    title = "About",
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = { viewModel.setTab(Tab.SettingsAbout) },
                    testTag = "settings_about_btn"
                )
            }

            item {
                SettingsOptionCard(
                    icon = { StickHelpIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                    title = "Help & Feedback",
                    subtitle = "Contact us, provide suggestions",
                    onClick = { viewModel.setTab(Tab.SettingsHelp) },
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
    val isDark = LocalDarkTheme.current
    TranslucentGlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
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
