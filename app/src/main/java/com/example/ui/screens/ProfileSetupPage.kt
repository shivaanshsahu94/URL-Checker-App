package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel

@Composable
fun ProfileSetupPage(viewModel: MainViewModel) {
    val context = LocalContext.current
    BackHandler { viewModel.setTab(Tab.Settings) }

    val name by viewModel.profileName.collectAsStateWithLifecycle()
    val avatarIdx by viewModel.profileAvatar.collectAsStateWithLifecycle()

    var textInput by remember(name) { mutableStateOf(name) }
    var selectedIdx by remember(avatarIdx) { mutableStateOf(avatarIdx) }

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
                onClick = { viewModel.setTab(Tab.Settings) },
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
                        onClick = { viewModel.setTab(Tab.Settings) }
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.updateProfileName(textInput)
                            viewModel.updateProfileAvatar(selectedIdx)
                            viewModel.setTab(Tab.Settings)
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
