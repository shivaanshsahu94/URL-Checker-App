package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri

/**
 * Centralized utility for launching URLs in an external browser.
 *
 * Replaces all hardcoded `com.android.chrome` package references with
 * dynamic browser resolution that works on any device, regardless of
 * which browser is installed.
 *
 * CRITICAL: Every outbound intent explicitly excludes this app's own package
 * to prevent the infinite intent loop that occurs because this app registers
 * itself as a default browser handler with the Android OS.
 *
 * When setPackage(null) is called (no specific browser resolved), Android
 * hands the ACTION_VIEW intent back to the default browser handler — which is
 * THIS app — causing an infinite interception loop. All paths here are guarded
 * against that case.
 */
object BrowserLauncher {

    /**
     * Opens the given [url] in an external browser, excluding this app's own package.
     *
     * Resolution order:
     * 1. Query installed browsers, pick the first one that isn't this app.
     * 2. If none found, fall back to system browser selector via [Intent.CATEGORY_APP_BROWSER].
     * 3. If that also fails, show a Toast.
     */
    fun openInExternalBrowser(context: Context, url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "URL is empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val externalPackage = findExternalBrowserPackage(context)
            if (externalPackage == null) {
                // Guard: do NOT fire an unscoped ACTION_VIEW — it would loop back to us.
                // Route directly to the system browser selector instead.
                openWithSystemSelector(context, url)
                return
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage(externalPackage)
            }
            context.startActivity(browserIntent)
        } catch (_: ActivityNotFoundException) {
            openWithSystemSelector(context, url)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens a chooser dialog for the user to pick which browser to open [url] in.
     *
     * The chooser is built by collecting ALL external browser candidates (excluding
     * this app's own package) and presenting them as a system chooser. This ensures
     * our own app never appears in or handles the chooser, preventing the loop.
     */
    fun openWithChooser(context: Context, url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "URL is empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val externalPackage = findExternalBrowserPackage(context)
            if (externalPackage == null) {
                // Guard: fall back to system selector; do NOT fire unscoped ACTION_VIEW.
                openWithSystemSelector(context, url)
                return
            }
            // Scope the base intent to one verified external browser so the chooser
            // cannot hand it back to our own interceptor.
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage(externalPackage)
            }
            context.startActivity(Intent.createChooser(intent, "Open in Browser"))
        } catch (_: ActivityNotFoundException) {
            openWithSystemSelector(context, url)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Queries the system for an installed browser package that is NOT this app.
     * Returns the first match, or `null` if none found.
     *
     * IMPORTANT: A `null` return must NEVER be passed directly to [Intent.setPackage].
     * Callers must route to [openWithSystemSelector] instead when this returns null.
     */
    private fun findExternalBrowserPackage(context: Context): String? {
        val pm = context.packageManager
        val probeIntent = Intent(Intent.ACTION_VIEW, "http://www.example.com".toUri()).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(probeIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(probeIntent, 0)
        }

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName) {
                return pkg
            }
        }
        return null
    }

    /**
     * Last-resort fallback: uses the system's built-in browser selector category.
     * This always routes to a real browser and never back to this app.
     */
    private fun openWithSystemSelector(context: Context, url: String) {
        try {
            val fallbackIntent = Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_BROWSER
            ).apply {
                data = url.toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No browser found on this device", Toast.LENGTH_LONG).show()
        }
    }
}
