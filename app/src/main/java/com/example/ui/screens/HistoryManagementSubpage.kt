package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel

// --- SCREEN 4: HISTORY MANAGEMENT SUBPAGE ---
@Composable
fun HistoryManagementSubpage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab(Tab.Settings) }
    val context = LocalContext.current
    val saveActive by viewModel.saveHistory.collectAsStateWithLifecycle()
    val deleteDays by viewModel.autoDeleteDays.collectAsStateWithLifecycle()

    var showDropdownMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val autoDeleteDaysOptions = remember { listOf(1, 15, 30, 60) }

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
                            autoDeleteDaysOptions.forEach { days ->
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
