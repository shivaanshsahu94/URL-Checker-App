package com.example.viewmodel

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.*
import com.example.ui.navigation.Tab
import com.example.util.SecurityConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = HistoryRepository(db.historyDao())
    private val settingsManager = SettingsManager(application)

    val historyItems: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isDefaultBrowser = MutableStateFlow(false)
    val isDefaultBrowser: StateFlow<Boolean> = _isDefaultBrowser.asStateFlow()

    // Intercepted URL state is delegated to the shared application-scoped repository
    val isLinkIntercepted: StateFlow<Boolean> = InterceptedUrlRepository.isLinkIntercepted
    val originalUrl: StateFlow<String> = InterceptedUrlRepository.originalUrl
    val resolvedUrl: StateFlow<String> = InterceptedUrlRepository.resolvedUrl
    val isResolving: StateFlow<Boolean> = InterceptedUrlRepository.isResolving

    // Tab state: type-safe sealed class navigation
    private val _selectedTab = MutableStateFlow<Tab>(Tab.History)
    val selectedTab: StateFlow<Tab> = _selectedTab.asStateFlow()

    // Settings States — properly encapsulated with private backing fields
    private val _profileName = MutableStateFlow(settingsManager.profileName)
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileAvatar = MutableStateFlow(settingsManager.profileAvatar)
    val profileAvatar: StateFlow<Int> = _profileAvatar.asStateFlow()

    private val _defaultChecker = MutableStateFlow(settingsManager.defaultChecker)
    val defaultChecker: StateFlow<String> = _defaultChecker.asStateFlow()

    private val _appTheme = MutableStateFlow(settingsManager.theme)
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _saveHistory = MutableStateFlow(settingsManager.saveHistory)
    val saveHistory: StateFlow<Boolean> = _saveHistory.asStateFlow()

    private val _autoDeleteDays = MutableStateFlow(settingsManager.autoDeleteDays)
    val autoDeleteDays: StateFlow<Int> = _autoDeleteDays.asStateFlow()

    // Feedback submission state
    private val _feedbackSubmitting = MutableStateFlow(false)
    val feedbackSubmitting: StateFlow<Boolean> = _feedbackSubmitting.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            checkDefaultBrowser()
        }
        viewModelScope.launch(Dispatchers.Default) {
            scheduleCleanupWorker()
        }
    }

    fun checkDefaultBrowser() {
        val context = getApplication<Application>()
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        _isDefaultBrowser.value = roleManager?.isRoleHeld(RoleManager.ROLE_BROWSER) == true
    }

    private fun scheduleCleanupWorker() {
        val context = getApplication<Application>()
        val request = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "history_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Intercepts, filters, and processes incoming system Intent.ACTION_VIEW events.
     * Delegates to the shared [InterceptedUrlRepository] so state is visible
     * across all activity instances.
     */
    fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data = intent.dataString
        // Capture standard external browser navigation hooks
        if (action == Intent.ACTION_VIEW && !data.isNullOrBlank()) {
            InterceptedUrlRepository.handleInterceptedUrl(
                rawUrl = data,
                saveHistory = _saveHistory.value,
                historyRepository = repository,
                scope = viewModelScope
            )
        }
    }

    fun closeInterceptor() {
        InterceptedUrlRepository.closeInterceptor()
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun setTab(tab: Tab) {
        _selectedTab.value = tab
    }

    fun updateProfileName(name: String) {
        _profileName.value = name
        settingsManager.profileName = name
    }

    fun updateProfileAvatar(avatarIdx: Int) {
        _profileAvatar.value = avatarIdx
        settingsManager.profileAvatar = avatarIdx
    }

    fun updateDefaultChecker(checker: String) {
        _defaultChecker.value = checker
        settingsManager.defaultChecker = checker
    }

    fun updateTheme(theme: String) {
        _appTheme.value = theme
        settingsManager.theme = theme
    }

    fun updateSaveHistory(save: Boolean) {
        _saveHistory.value = save
        settingsManager.saveHistory = save
    }

    fun updateAutoDeleteDays(days: Int) {
        _autoDeleteDays.value = days
        settingsManager.autoDeleteDays = days
    }

    /**
     * Helper method to generate URL signatures suitable for standard API scanning interfaces.
     * Encodes target strings safely into URL-friendly, non-padded, non-wrapped Base64 patterns.
     */
    fun getEncodedUrlForVirusTotal(url: String): String {
        return try {
            val bytes = url.toByteArray(Charsets.UTF_8)
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Submits user feedback via the configured webhook endpoint.
     * Uses the shared [NetworkModule.feedbackClient] to avoid per-call OkHttpClient creation.
     *
     * @param text The feedback text from the user
     * @param onResult Callback with true on success, false on failure
     */
    fun submitFeedback(text: String, onResult: (Boolean) -> Unit) {
        val webhookUrl = SecurityConfig.getFeedbackUrl()
        if (webhookUrl.isBlank()) {
            onResult(false)
            return
        }

        _feedbackSubmitting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonPayload = org.json.JSONObject().apply {
                    put("content", "New App Feedback: $text")
                }.toString()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonPayload.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(webhookUrl)
                    .post(requestBody)
                    .build()

                NetworkModule.feedbackClient.newCall(request).execute().use { response ->
                    val success = response.isSuccessful || response.code == 204
                    withContext(Dispatchers.Main) {
                        _feedbackSubmitting.value = false
                        onResult(success)
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _feedbackSubmitting.value = false
                    onResult(false)
                }
            }
        }
    }

    fun getCheckerUrl(url: String): String {
        return when (_defaultChecker.value) {
            "Google Safe Browsing" -> {
                try {
                    val encoded = java.net.URLEncoder.encode(url, "UTF-8")
                    "https://transparencyreport.google.com/safe-browsing/search?url=$encoded"
                } catch (_: Exception) {
                    "https://transparencyreport.google.com/safe-browsing/search"
                }
            }
            "URLVoid" -> "https://www.urlvoid.com/"
            else -> {
                val base64Url = getEncodedUrlForVirusTotal(url)
                "https://www.virustotal.com/gui/home/search"
            }
        }
    }

    /**
     * Returns a display label for the user's selected default checker.
     */
    fun getCheckerLabel(): String {
        return when (_defaultChecker.value) {
            "Google Safe Browsing" -> "Safe Browsing"
            "URLVoid" -> "URLVoid"
            else -> "VirusTotal"
        }
    }
}
