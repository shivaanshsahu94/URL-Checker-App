package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel

@Composable
fun ThemeSetupPage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab(Tab.Settings) }
    val themeChoice by viewModel.appTheme.collectAsStateWithLifecycle()
    val themes = remember { listOf("System", "Light", "Dark") }

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

                themes.forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateTheme(choice)
                                viewModel.setTab(Tab.Settings)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (themeChoice == choice),
                            onClick = {
                                viewModel.updateTheme(choice)
                                viewModel.setTab(Tab.Settings)
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
