package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig

import com.example.ui.navigation.Tab
import com.example.util.BrowserLauncher
import com.example.viewmodel.MainViewModel

@Composable
fun AboutSubpage(viewModel: MainViewModel) {
    BackHandler { viewModel.setTab(Tab.Settings) }
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current

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

                // Version Number (Dynamic)
                Text(
                    text = BuildConfig.VERSION_NAME,
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
                Text(
                    text = devCreditString,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.clickable {
                        BrowserLauncher.openInExternalBrowser(context, "https://github.com/shivaanshsahu94")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.setTab(Tab.Settings) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("about_ok_btn")
                ) {
                    Text("OK")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        BrowserLauncher.openInExternalBrowser(context, "https://github.com/shivaanshsahu94/URL-Checker-App/releases")
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
