package com.example.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Application-wide singleton providing shared OkHttpClient instances.
 * Prevents the severe memory/performance overhead of creating new client
 * instances per request (especially from composable coroutine scopes).
 */
object NetworkModule {

    /**
     * General-purpose HTTP client for feedback submission and other API calls.
     * Uses connection pooling and sensible timeouts.
     */
    val feedbackClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }
}
