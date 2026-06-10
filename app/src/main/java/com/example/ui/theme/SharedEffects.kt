package com.example.ui.theme

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.ui.graphics.asComposeRenderEffect

/**
 * Application-wide shared [RenderEffect] instances for glassmorphic blur effects.
 *
 * Since [RenderEffect] is immutable and thread-safe, these can be safely shared
 * across all composables instead of re-creating identical instances via `remember {}`.
 */
object SharedEffects {

    /** Standard 30px blur used for glass card backgrounds and popup backdrops. */
    val glassBlur = RenderEffect.createBlurEffect(
        30f, 30f, Shader.TileMode.CLAMP
    ).asComposeRenderEffect()

    /** Lighter 25px blur used for the bottom navigation bar. */
    val navBarBlur = RenderEffect.createBlurEffect(
        25f, 25f, Shader.TileMode.CLAMP
    ).asComposeRenderEffect()

    /** Medium 20px blur used for pill buttons and small glass components. */
    val pillBlur = RenderEffect.createBlurEffect(
        20f, 20f, Shader.TileMode.CLAMP
    ).asComposeRenderEffect()
}
