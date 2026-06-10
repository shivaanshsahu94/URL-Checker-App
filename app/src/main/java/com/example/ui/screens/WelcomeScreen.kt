package com.example.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.navigation.Tab
import com.example.viewmodel.MainViewModel

// --- SCREEN 1: ONBOARDING WELCOME ---
@Composable
fun WelcomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val roleLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.checkDefaultBrowser()
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        if (roleManager?.isRoleHeld(RoleManager.ROLE_BROWSER) == true) {
            viewModel.setTab(Tab.History)
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
                try {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                    val requestIntent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                    if (requestIntent != null) {
                        roleLauncher.launch(requestIntent)
                    } else {
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        roleLauncher.launch(intent)
                    }
                } catch (e: Exception) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        roleLauncher.launch(intent)
                    } catch (_: Exception) {
                        android.widget.Toast.makeText(context, "Could not open default app settings.", android.widget.Toast.LENGTH_SHORT).show()
                    }
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
