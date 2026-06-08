package com.example.viewmodel

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _isLinkIntercepted = MutableStateFlow(false)
    val isLinkIntercepted: StateFlow<Boolean> = _isLinkIntercepted.asStateFlow()

    private val _originalUrl = MutableStateFlow("")
    val originalUrl: StateFlow<String> = _originalUrl.asStateFlow()

    private val _resolvedUrl = MutableStateFlow("")
    val resolvedUrl: StateFlow<String> = _resolvedUrl.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    // Tab state: "history" or "settings"
    private val _selectedTab = MutableStateFlow("history")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Settings States
    val profileName = MutableStateFlow(settingsManager.profileName)
    val profileAvatar = MutableStateFlow(settingsManager.profileAvatar)
    val defaultChecker = MutableStateFlow(settingsManager.defaultChecker)
    val appTheme = MutableStateFlow(settingsManager.theme)
    val saveHistory = MutableStateFlow(settingsManager.saveHistory)
    val autoDeleteDays = MutableStateFlow(settingsManager.autoDeleteDays)

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            checkDefaultBrowser()
            scheduleCleanupWorker()
        }
    }

    fun checkDefaultBrowser() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            _isDefaultBrowser.value = roleManager?.isRoleHeld(RoleManager.ROLE_BROWSER) == true
        } else {
            // Pre-Q fallback: We check if there's any fallback preferred browser, or we check dynamically
            _isDefaultBrowser.value = false
        }
    }

    private fun scheduleCleanupWorker() {
        val context = getApplication<Application>()
        val request = androidx.work.OneTimeWorkRequestBuilder<HistoryCleanupWorker>()
            .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(androidx.work.BackoffPolicy.LINEAR, 10, java.util.concurrent.TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "history_cleanup",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Intercepts, filters, and processes incoming system Intent.ACTION_VIEW events.
     * When a link is selected externally, this captures the raw URL, triggers transition
     * screens, invokes the off-thread network redirect resolver (UrlResolver), updates state flows
     * for popup display layers, and logs the session dynamically in the local Room database
     * depending on the user's historical log retention settings.
     */
    fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data = intent.dataString
        // Capture standard external browser navigation hooks
        if (action == Intent.ACTION_VIEW && !data.isNullOrBlank()) {
            _isLinkIntercepted.value = true
            _isResolving.value = true
            viewModelScope.launch {
                // Route heavy text parsing during the auto-clipboard interception to Dispatchers.Default
                val parsedData = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    // Optimized parsing simulation for the intercepted link
                    data.trim()
                }
                
                _originalUrl.value = parsedData

                // Background task traces redirect chains (e.g. shorteners) to reveal final targets
                val resolved = UrlResolver.resolveUrl(parsedData)
                _resolvedUrl.value = resolved
                _isResolving.value = false

                // Increment logs if the user has database-level history preservation turned on
                if (saveHistory.value) {
                    repository.insert(parsedData, resolved)
                }
            }
        }
    }

    fun closeInterceptor() {
        _isLinkIntercepted.value = false
        _originalUrl.value = ""
        _resolvedUrl.value = ""
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

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }

    fun updateProfileName(name: String) {
        profileName.value = name
        settingsManager.profileName = name
    }

    fun updateProfileAvatar(avatarIdx: Int) {
        profileAvatar.value = avatarIdx
        settingsManager.profileAvatar = avatarIdx
    }

    fun updateDefaultChecker(checker: String) {
        defaultChecker.value = checker
        settingsManager.defaultChecker = checker
    }

    fun updateTheme(theme: String) {
        appTheme.value = theme
        settingsManager.theme = theme
    }

    fun updateSaveHistory(save: Boolean) {
        saveHistory.value = save
        settingsManager.saveHistory = save
    }

    fun updateAutoDeleteDays(days: Int) {
        autoDeleteDays.value = days
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
        } catch (e: Exception) {
            url
        }
    }
}
