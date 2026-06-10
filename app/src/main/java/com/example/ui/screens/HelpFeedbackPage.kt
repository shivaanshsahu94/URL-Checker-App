package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel

@Composable
fun HelpFeedbackPage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab(Tab.Settings) }
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    // Read the submitting state from the view model
    val isSubmitting by viewModel.feedbackSubmitting.collectAsStateWithLifecycle()

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
                        onClick = { viewModel.setTab(Tab.Settings) }, 
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
                        maxLines = 4,
                        enabled = !isSubmitting
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
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
                                    if (feedbackText.isNotBlank()) {
                                        android.widget.Toast.makeText(context, "Sending...", android.widget.Toast.LENGTH_SHORT).show()
                                        viewModel.submitFeedback(feedbackText) { success ->
                                            if (success) {
                                                feedbackText = ""
                                                submitted = true
                                                android.widget.Toast.makeText(context, "Feedback sent successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to send. Please check your internet connection.", android.widget.Toast.LENGTH_SHORT).show()
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
}
