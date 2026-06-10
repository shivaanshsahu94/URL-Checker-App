package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.ui.screens.MainLayoutScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val initialBlurEnabled = windowManager.isCrossWindowBlurEnabled
        
        if (initialBlurEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            viewModel.handleIntent(intent)
        }

        setContent {
            val themeValue by viewModel.appTheme.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = remember(themeValue, isSystemDark) {
                when (themeValue) {
                    "Dark" -> true
                    "Light" -> false
                    else -> isSystemDark
                }
            }

            // Use dynamic color only when the user hasn't explicitly chosen a theme
            val useDynamicColor = remember(themeValue) {
                themeValue == "System"
            }

            // Statically track the blur setting once to prevent unbounded state observations or loop invalidations
            val isBlurEnabled = remember {
                initialBlurEnabled
            }

            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = useDynamicColor) {
                val scaffoldBg = remember(isBlurEnabled, darkTheme) {
                    if (isBlurEnabled) {
                        androidx.compose.ui.graphics.Color(0x20000000)
                    } else if (darkTheme) {
                        androidx.compose.ui.graphics.Color(0xD9000000)
                    } else {
                        androidx.compose.ui.graphics.Color(0xD9FFFFFF)
                    }
                }

                // Remove conflicting navigationBarsPadding on background; the background window should be a static frame
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = scaffoldBg,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        MainLayoutScreen(viewModel = viewModel)
                    }
                }
            }
        }
        
        if (initialBlurEnabled) {
            window.setBackgroundBlurRadius(200)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDefaultBrowser()
    }
}
