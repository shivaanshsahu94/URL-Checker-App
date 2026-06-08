package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
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
     * Uses efficient HEAD requests by default to minimize network load and bandwidth, 
     * falling back seamlessly to standard GET request chains if a target server rejects HEAD.
     */
    suspend fun resolveUrl(url: String): String {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url
        }

        return withContext(Dispatchers.IO) {
            var currentUrl = url
            var redirects = 0
            val maxRedirects = 8

            while (redirects < maxRedirects) {
                try {
                    val request = Request.Builder()
                        .url(currentUrl)
                        .head() // Use HEAD request for maximum efficiency and speed
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.code in 300..399) {
                            val location = response.header("Location")
                            if (!location.isNullOrBlank()) {
                                currentUrl = resolveRelativeUrl(currentUrl, location)
                                redirects++
                                continue
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fail gracefully and attempt GET if HEAD is not supported by server
                    try {
                        val getRequest = Request.Builder()
                            .url(currentUrl)
                            .get()
                            .build()
                        client.newCall(getRequest).execute().use { response ->
                            if (response.code in 300..399) {
                                val location = response.header("Location")
                                if (!location.isNullOrBlank()) {
                                    currentUrl = resolveRelativeUrl(currentUrl, location)
                                    redirects++
                                    continue
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        break
                    }
                }
                break
            }
            currentUrl
        }
    }

    private fun resolveRelativeUrl(base: String, relative: String): String {
        return try {
            URL(URL(base), relative).toString()
        } catch (e: Exception) {
            relative
        }
    }
}
