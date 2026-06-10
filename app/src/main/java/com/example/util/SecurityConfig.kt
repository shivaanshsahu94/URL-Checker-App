package com.example.util

import com.example.BuildConfig

/**
 * Provides access to sensitive endpoints via BuildConfig fields
 * sourced from the Secrets Gradle Plugin (.env file).
 *
 * The webhook URL is never committed to source control.
 * In production builds, it is injected at build time from the .env file
 * or CI environment secrets. Debug builds without the secret configured
 * will return an empty string, and feedback submission will be silently skipped.
 */
object SecurityConfig {

    fun getFeedbackUrl(): String {
        return BuildConfig.FEEDBACK_WEBHOOK_URL
    }
}
