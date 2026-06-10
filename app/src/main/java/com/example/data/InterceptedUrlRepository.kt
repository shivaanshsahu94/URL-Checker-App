package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Application-scoped singleton holding intercepted URL state.
 *
 * Both [com.example.MainActivity] and [com.example.InterceptorActivity] share
 * this repository so that intercepted URL data is observable across all
 * activity instances. Previously, each activity created its own ViewModel
 * with isolated state, making it impossible for the main dashboard to observe
 * URLs intercepted by the InterceptorActivity.
 */
object InterceptedUrlRepository {

    private val _isLinkIntercepted = MutableStateFlow(false)
    val isLinkIntercepted: StateFlow<Boolean> = _isLinkIntercepted.asStateFlow()

    private val _originalUrl = MutableStateFlow("")
    val originalUrl: StateFlow<String> = _originalUrl.asStateFlow()

    private val _resolvedUrl = MutableStateFlow("")
    val resolvedUrl: StateFlow<String> = _resolvedUrl.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private var resolveJob: kotlinx.coroutines.Job? = null

    /**
     * Processes an intercepted URL: resolves redirects in the background,
     * updates all state flows, and optionally logs to history.
     *
     * @param rawUrl The raw URL string from the intercepted intent
     * @param saveHistory Whether to persist this entry to Room
     * @param historyRepository The repository to insert history into (if saving)
     * @param scope A CoroutineScope to launch the background work in
     */
    fun handleInterceptedUrl(
        rawUrl: String,
        saveHistory: Boolean,
        historyRepository: HistoryRepository?,
        scope: CoroutineScope
    ) {
        _isLinkIntercepted.value = true
        _isResolving.value = true

        resolveJob?.cancel()
        resolveJob = scope.launch {
            val trimmedUrl = withContext(Dispatchers.Default) {
                rawUrl.trim()
            }

            _originalUrl.value = trimmedUrl

            // Resolve redirect chain in the background
            val resolved = UrlResolver.resolveUrl(trimmedUrl)
            _resolvedUrl.value = resolved
            _isResolving.value = false

            // Persist to history if enabled
            if (saveHistory && historyRepository != null) {
                historyRepository.insert(trimmedUrl, resolved)
            }
        }
    }

    fun closeInterceptor() {
        resolveJob?.cancel()
        resolveJob = null
        _isLinkIntercepted.value = false
        _isResolving.value = false
        _originalUrl.value = ""
        _resolvedUrl.value = ""
    }
}
