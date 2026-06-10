package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object UrlResolver {
    // Custom OkHttpClient with redirect-following disabled internally.
    // This allows the app to intercept each HTTP 3xx redirection hop manually,
    // evaluate the safe target, and prevent infinite redirection loops or malicious trapping.
    private val client = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    /**
     * Programmatically traces nested redirections (HTTP 3xx statuses) up to 8 max hops.
     * Uses efficient HEAD requests by default to minimize network load and bandwidth.
     * Falls back to GET only if the server explicitly rejects HEAD (405/501),
     * and all subsequent hops in the chain continue using GET.
     */
    suspend fun resolveUrl(url: String): String {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url
        }

        return withContext(Dispatchers.IO) {
            var currentUrl = url
            var redirects = 0
            val maxRedirects = 8
            var useGet = false // Once a server rejects HEAD, use GET for rest of chain
            val visited = mutableSetOf<String>()

            while (redirects < maxRedirects) {
                if (currentUrl in visited) {
                    break // cycle detected
                }
                visited.add(currentUrl)
                try {
                    val request = Request.Builder()
                        .url(currentUrl)
                        .apply { if (useGet) get() else head() }
                        .build()

                    client.newCall(request).execute().use { response ->
                        val code = response.code

                        // If HEAD returned a non-successful status code (error or non-redirect), switch to GET and retry
                        if (!useGet && code !in 200..399) {
                            useGet = true
                            // Retry this same URL with GET instead of advancing
                            continue
                        }

                        if (code in 300..399) {
                            val location = response.header("Location")
                            if (!location.isNullOrBlank()) {
                                currentUrl = resolveRelativeUrl(currentUrl, location)
                                redirects++
                                continue
                            }
                        }
                        // Non-redirect response (2xx, 4xx, etc.) — we've reached the final destination
                    }
                } catch (_: Exception) {
                    // Network error — stop resolution, return current best URL
                    break
                }
                break
            }
            currentUrl
        }
    }

    private fun resolveRelativeUrl(base: String, relative: String): String {
        return try {
            java.net.URI(base).resolve(relative).toString()
        } catch (_: Exception) {
            relative
        }
    }
}
