package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.InterceptorPopupLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class InterceptorActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val initialBlurEnabled = windowManager.isCrossWindowBlurEnabled
        
        if (initialBlurEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
        
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.0f)
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
                val boxBg = remember(isBlurEnabled, darkTheme) {
                    if (isBlurEnabled) {
                        Color.Transparent
                    } else if (darkTheme) {
                        Color(0xD9000000)
                    } else {
                        Color(0xD9FFFFFF)
                    }
                }

                // Remove conflicting navigationBarsPadding on background; the background window should be a static frame
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(boxBg)
                        .windowInsetsPadding(WindowInsets.systemBars),
                    contentAlignment = Alignment.Center
                ) {
                    InterceptorPopupLayout(viewModel = viewModel)
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
}
