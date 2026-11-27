package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.fragment.AppFragment
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.ext.webview.WebView
import com.nexustools.genericui.ext.webview.WebViewConfig

/**
 * Single browser tab content [Fragment] hosting a real `:extensions:ui:webview` [WebView]
 * alongside the clean [BrowserNewTabView] when unnavigated (`about:newtab`).
 */
@AppFragment(route = "browser/tab", label = "Browser Tab Page")
class BrowserTabPageFragment(
    initialUrl: String = URL_NEW_TAB,
    initialBookmarks: List<BrowserBookmark> = emptyList(),
    initialSearchPrefix: String = BrowserSessionState.DEFAULT_SEARCH_PREFIX
) : Fragment() {

    private var searchEnginePrefix: String = initialSearchPrefix
    private val navigationHistory = mutableListOf<BrowserHistoryEntry>()
    private var navigationIndex: Int = -1
    private var historySequence: Long = 0L
    private var activeLoadToken: Int = 0
    private var stoppedSupersededUrl: String? = null
    private var hasDocumentTitle: Boolean = false

    /** Embedded `:extensions:ui:webview` component synchronized with [currentUrl]. */
    val webView: WebView = WebView(
        initialUrl = null,
        config = WebViewConfig(
            enableJavaScript = true,
            domStorageEnabled = true,
            allowFileAccess = true
        )
    ).apply {
        id = "browser-tab-webview"
        addClass("browser-tab-webview")
        isVisible = !isNewTabUrl(initialUrl)
    }

    /** Thin indeterminate progress indicator shown while [webView] is loading a page. */
    val loadingProgressBar: ProgressBar = ProgressBar(
        progress = 0f,
        indeterminate = true
    ).apply {
        id = "browser-tab-loading-progress"
        addClass("browser-tab-loading-bar")
        isVisible = false
    }

    /** Unnavigated New Tab search surface (`about:newtab`). */
    val newTabSurface: BrowserNewTabView = BrowserNewTabView(
        showNewTabButton = false,
        initialBookmarks = initialBookmarks,
        idPrefix = "browser-tab-newtab"
    )

    /** Currently active URL in this tab. */
    var currentUrl: String = normalizeOmniboxInput(initialUrl, initialSearchPrefix)
        private set

    /** Resolved human-readable title for [currentUrl]. */
    var currentTitle: String = resolveTitleForUrl(currentUrl)
        private set

    /** Resolved icon badge label for [currentUrl], or `null` when using favicon / vector fallback. */
    var currentIcon: String? = resolveIconForUrl(currentUrl)
        private set

    /** Current favicon URL reported by [webView], or `null` when unnavigated or unavailable. */
    var currentFaviconUrl: String? = sanitizeFaviconUrl(webView.currentFaviconUrl)
        private set

    /** `true` while [webView] has an in-flight navigation load. */
    var isPageLoading: Boolean = false
        private set

    /** Number of times [reload] has been invoked on this tab. */
    var reloadCount: Int = 0
        private set

    /** `true` when this tab is displaying the unnavigated New Tab view (`about:newtab`). */
    val isShowingNewTabPage: Boolean
        get() = isNewTabUrl(currentUrl)

    /** Read-only snapshot of this tab's navigation history stack. */
    val historyStack: List<BrowserHistoryEntry>
        get() = navigationHistory.toList()

    /** Current index within [historyStack]. */
    val historyIndex: Int
        get() = navigationIndex

    /** `true` if this tab can navigate backward in its per-tab history stack. */
    val canGoBack: Boolean
        get() = navigationIndex > 0

    /** `true` if this tab can navigate forward in its per-tab history stack. */
    val canGoForward: Boolean
        get() = navigationIndex >= 0 && navigationIndex < navigationHistory.lastIndex

    /** Invoked whenever this tab navigates to a new URL, updates its title, or finishes loading. */
    var onPageNavigated: ((url: String, title: String, icon: String?) -> Unit)? = null

    /** Invoked whenever [currentFaviconUrl] changes. */
    var onFaviconChanged: ((faviconUrl: String?) -> Unit)? = null

    /** Invoked whenever [isPageLoading] changes. */
    var onLoadingStateUpdated: ((isLoading: Boolean) -> Unit)? = null

    /** Invoked whenever this tab fragment receives an [onResize] callback. */
    var onViewportResized: ((widthPx: Int, heightPx: Int) -> Unit)? = null

    init {
        addClass("browser-tab-page-fragment")

        webView.onFaviconChanged = { rawFavicon ->
            val cleaned = if (isShowingNewTabPage) {
                null
            } else {
                sanitizeFaviconUrl(rawFavicon)
            }
            updateFaviconUrl(cleaned)
        }

        webView.onTitleChanged = { rawTitle ->
            if (!isShowingNewTabPage) {
                val trimmed = rawTitle.trim()
                if (trimmed.isNotEmpty()) {
                    hasDocumentTitle = true
                    currentTitle = trimmed
                    pageTitle = trimmed
                    val currentEntry = navigationHistory.getOrNull(navigationIndex)
                    if (currentEntry != null) {
                        navigationHistory[navigationIndex] = currentEntry.copy(title = trimmed)
                    }
                    onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
                }
            }
        }

        webView.onPageFinished = { finishedUrl ->
            val resolved = finishedUrl.trim()
            val preempted = stoppedSupersededUrl
            if (preempted != null && resolved == preempted && currentUrl != resolved) {
                stoppedSupersededUrl = null
            } else if (!(webView.isLoading && resolved.isNotEmpty() && !isNewTabUrl(currentUrl) && resolved != currentUrl)) {
                stoppedSupersededUrl = null
                if (resolved.isNotEmpty() && !isNewTabUrl(currentUrl)) {
                    val docTitle = if (hasDocumentTitle) {
                        currentTitle
                    } else {
                        webView.currentTitle?.trim()?.takeIf { it.isNotBlank() }?.also {
                            hasDocumentTitle = true
                        }
                    }
                    applyLocationMetadata(resolved, titleOverride = docTitle)
                    val currentEntry = navigationHistory.getOrNull(navigationIndex)
                    if (currentEntry != null && (currentEntry.url != resolved || currentEntry.title != currentTitle)) {
                        navigationHistory[navigationIndex] = currentEntry.copy(
                            url = resolved,
                            title = currentTitle
                        )
                    }
                    val latestFavicon = sanitizeFaviconUrl(webView.currentFaviconUrl)
                    if (latestFavicon != null) {
                        updateFaviconUrl(latestFavicon)
                    }
                }
                activeLoadToken += 1
                setLoadingState(false)
                syncActiveSurface()
                onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
            }
        }

        newTabSurface.onSubmitQuery = { raw ->
            navigateTo(raw, recordHistory = true, searchPrefix = searchEnginePrefix)
        }
        newTabSurface.onBookmarkSelected = { bookmark ->
            navigateTo(bookmark.url, recordHistory = true, searchPrefix = searchEnginePrefix)
        }

        addChild(loadingProgressBar)
        addChild(newTabSurface)
        addChild(webView)

        navigateTo(initialUrl, recordHistory = true, searchPrefix = initialSearchPrefix)
    }

    /**
     * Updates the saved bookmarks shown on this tab's New Tab surface.
     */
    fun setSpeedDialBookmarks(bookmarks: List<BrowserBookmark>) {
        newTabSurface.setBookmarks(bookmarks)
    }

    /**
     * Updates the search engine URL prefix used when plain search terms are submitted.
     */
    fun updateSearchEnginePrefix(searchPrefix: String) {
        if (searchPrefix.isNotBlank()) {
            searchEnginePrefix = searchPrefix
        }
    }

    /**
     * Navigates this tab to [targetUrl], normalizing URLs (`https://...`, `file://...`, `about:...`,
     * or search URL for plain search queries), updating [webView], active surface visibility,
     * loading state, and the per-tab history stack when [recordHistory] is `true`.
     */
    fun navigateTo(
        targetUrl: String,
        recordHistory: Boolean = true,
        searchPrefix: String = searchEnginePrefix
    ) {
        searchEnginePrefix = searchPrefix
        val previousUrl = currentUrl
        val wasLoading = isPageLoading
        val normalized = normalizeOmniboxInput(targetUrl, searchPrefix)
        val urlChanged = previousUrl != normalized
        hasDocumentTitle = false
        applyLocationMetadata(normalized)

        if (recordHistory) {
            recordNavigationEntry(normalized, currentTitle)
        }

        if (isNewTabUrl(normalized)) {
            if (wasLoading) {
                stoppedSupersededUrl = previousUrl
                webView.stopLoading()
            }
            activeLoadToken += 1
            setLoadingState(false)
            syncActiveSurface()
            updateFaviconUrl(null, forceNotify = true)
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
            return
        }

        if (urlChanged) {
            updateFaviconUrl(null)
        }
        if (wasLoading) {
            stoppedSupersededUrl = previousUrl
            webView.stopLoading()
        }
        syncActiveSurface()
        val token = ++activeLoadToken
        setLoadingState(true)
        webView.loadUrl(normalized)
        if (activeLoadToken == token && isPageLoading) {
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
        }
    }

    /**
     * Navigates backward in this tab's history stack if [canGoBack] is `true`.
     */
    fun goBack(): Boolean {
        if (!canGoBack) return false
        navigationIndex -= 1
        navigateToHistoryEntry(navigationHistory[navigationIndex], isBack = true)
        return true
    }

    /**
     * Navigates forward in this tab's history stack if [canGoForward] is `true`.
     */
    fun goForward(): Boolean {
        if (!canGoForward) return false
        navigationIndex += 1
        navigateToHistoryEntry(navigationHistory[navigationIndex], isBack = false)
        return true
    }

    /**
     * Reloads the current page in this tab.
     */
    fun reload() {
        reloadCount += 1
        if (isNewTabUrl(currentUrl)) {
            setLoadingState(false)
            syncActiveSurface()
            return
        }
        val token = ++activeLoadToken
        setLoadingState(true)
        webView.reload()
        if (activeLoadToken == token && isPageLoading) {
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
        }
    }

    /**
     * Stops any active page load in this tab.
     */
    fun stopLoading() {
        activeLoadToken += 1
        webView.stopLoading()
        if (isPageLoading) {
            setLoadingState(false)
            syncActiveSurface()
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
        }
    }

    override fun onResize(viewportWidth: Int, viewportHeight: Int) {
        super.onResize(viewportWidth, viewportHeight)
        onViewportResized?.invoke(viewportWidth, viewportHeight)
    }

    override fun onDestroy() {
        webView.dispose()
        super.onDestroy()
    }

    private fun navigateToHistoryEntry(entry: BrowserHistoryEntry, isBack: Boolean) {
        val previousUrl = currentUrl
        hasDocumentTitle = false
        applyLocationMetadata(entry.url)
        if (isNewTabUrl(entry.url)) {
            activeLoadToken += 1
            setLoadingState(false)
            syncActiveSurface()
            updateFaviconUrl(null, forceNotify = true)
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
            return
        }

        syncActiveSurface()
        if (webView.url == entry.url) {
            webView.currentTitle?.trim()?.takeIf { it.isNotBlank() }?.let {
                hasDocumentTitle = true
                currentTitle = it
                pageTitle = it
            }
            activeLoadToken += 1
            setLoadingState(false)
            updateFaviconUrl(sanitizeFaviconUrl(webView.currentFaviconUrl))
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
            return
        }

        if (previousUrl != entry.url) {
            updateFaviconUrl(null)
        }
        val token = ++activeLoadToken
        setLoadingState(true)
        val navigatedViaWebView = if (isBack) {
            if (webView.canGoBack()) {
                webView.goBack()
                true
            } else {
                false
            }
        } else {
            if (webView.canGoForward()) {
                webView.goForward()
                true
            } else {
                false
            }
        }
        if (!navigatedViaWebView) {
            webView.loadUrl(entry.url)
        }
        if (activeLoadToken == token && isPageLoading) {
            onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
        }
    }

    private fun applyLocationMetadata(url: String, titleOverride: String? = null) {
        currentUrl = url
        currentTitle = titleOverride ?: resolveTitleForUrl(url)
        currentIcon = resolveIconForUrl(url)
        pageTitle = currentTitle
    }

    private fun updateFaviconUrl(nextFavicon: String?, forceNotify: Boolean = false) {
        val changed = currentFaviconUrl != nextFavicon
        currentFaviconUrl = nextFavicon
        if (changed || forceNotify) {
            onFaviconChanged?.invoke(nextFavicon)
        }
    }

    private fun recordNavigationEntry(url: String, title: String) {
        val currentAtCursor = navigationHistory.getOrNull(navigationIndex)
        if (currentAtCursor?.url == url) return
        while (navigationHistory.lastIndex > navigationIndex) {
            navigationHistory.removeAt(navigationHistory.lastIndex)
        }
        historySequence += 1L
        navigationHistory.add(
            BrowserHistoryEntry(
                url = url,
                title = title,
                sequenceNumber = historySequence
            )
        )
        navigationIndex = navigationHistory.lastIndex
    }

    private fun setLoadingState(loading: Boolean) {
        isPageLoading = loading
        loadingProgressBar.isVisible = loading && !isNewTabUrl(currentUrl)
        loadingProgressBar.isBusy = loading
        onLoadingStateUpdated?.invoke(loading)
    }

    private fun syncActiveSurface() {
        val showNewTab = isNewTabUrl(currentUrl)
        newTabSurface.isVisible = showNewTab
        webView.isVisible = !showNewTab
        if (showNewTab) {
            loadingProgressBar.isVisible = false
        }
    }

    companion object {
        const val URL_NEW_TAB: String = "about:newtab"
        const val URL_BLANK: String = "about:blank"

        fun isNewTabUrl(url: String): Boolean {
            val trimmed = url.trim().lowercase()
            return trimmed.isEmpty() || trimmed == URL_NEW_TAB
        }

        fun normalizeOmniboxInput(
            rawInput: String,
            searchPrefix: String = BrowserSessionState.DEFAULT_SEARCH_PREFIX
        ): String {
            val trimmed = rawInput.trim()
            if (trimmed.isEmpty()) return URL_NEW_TAB

            val lower = trimmed.lowercase()
            if (lower.startsWith("https://")) return "https://${trimmed.substring(8)}"
            if (lower.startsWith("http://")) return "http://${trimmed.substring(7)}"
            if (lower.startsWith("file://")) return "file://${trimmed.substring(7)}"
            if (lower.startsWith("about:")) return "about:${trimmed.substring(6)}"
            if (lower.startsWith("data:")) return trimmed

            if (!trimmed.contains(' ')) {
                if (lower == "localhost" || lower.startsWith("localhost:") || lower.startsWith("localhost/")) {
                    return "http://$trimmed"
                }
                if (trimmed.length >= 3 &&
                    trimmed[0].isLetter() &&
                    trimmed[1] == ':' &&
                    (trimmed[2] == '\\' || trimmed[2] == '/')
                ) {
                    return "file:///${trimmed.replace('\\', '/')}"
                }
                if (trimmed.startsWith("/")) {
                    return "file://$trimmed"
                }
                if (trimmed.contains('.') && !trimmed.startsWith(".") && !trimmed.endsWith(".")) {
                    return "https://$trimmed"
                }
            }

            return "$searchPrefix${encodeQueryComponent(trimmed)}"
        }

        fun resolveTitleForUrl(url: String): String {
            val trimmed = url.trim()
            val lower = trimmed.lowercase()
            return when {
                isNewTabUrl(trimmed) -> "New Tab"
                lower == URL_BLANK -> "Blank Page"
                lower.startsWith("about:") -> trimmed
                lower.startsWith("data:") -> "Data Document"
                lower.startsWith("file://") -> {
                    trimmed.substringAfterLast('/').substringBefore('?').substringBefore('#')
                        .ifBlank { trimmed }
                }
                lower.contains("?q=") || lower.contains("&q=") -> {
                    val rawQuery = trimmed.substringAfter("q=").substringBefore('&')
                    val decoded = decodeQueryComponent(rawQuery).ifBlank { "Search" }
                    "$decoded - Search"
                }
                else -> trimmed.removePrefix("https://").removePrefix("http://").trimEnd('/')
                    .ifBlank { "New Tab" }
            }
        }

        fun resolveSubtitleForUrl(url: String): String {
            val trimmed = url.trim()
            if (isNewTabUrl(trimmed)) return ""
            return trimmed
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')
        }

        fun resolveSchemeBadgeForUrl(url: String): String {
            val lower = url.trim().lowercase()
            return when {
                isNewTabUrl(lower) -> "New Tab"
                lower.startsWith("https://") -> "HTTPS"
                lower.startsWith("http://") -> "HTTP"
                lower.startsWith("file://") -> "FILE"
                lower.startsWith("about:") -> "ABOUT"
                lower.startsWith("data:") -> "DATA"
                else -> "WEB"
            }
        }

        fun resolveIconForUrl(url: String): String? = null

        private fun sanitizeFaviconUrl(rawFavicon: String?): String? {
            val trimmed = rawFavicon?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            if (trimmed.equals("data:,", ignoreCase = true)) return null
            if (trimmed.startsWith("data:", ignoreCase = true) && trimmed.substringAfter(',').isBlank()) return null
            return trimmed
        }

        private fun encodeQueryComponent(input: String): String {
            val bytes = input.encodeToByteArray()
            val out = StringBuilder(bytes.size)
            for (b in bytes) {
                val code = b.toInt() and 0xFF
                val ch = code.toChar()
                when {
                    ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' ||
                        ch == '-' || ch == '_' || ch == '.' || ch == '~' -> out.append(ch)
                    ch == ' ' -> out.append('+')
                    else -> {
                        val hex = code.toString(16).uppercase()
                        out.append('%')
                        if (hex.length < 2) out.append('0')
                        out.append(hex)
                    }
                }
            }
            return out.toString()
        }

        private fun decodeQueryComponent(encoded: String): String {
            val bytes = ArrayList<Byte>(encoded.length)
            var i = 0
            while (i < encoded.length) {
                val ch = encoded[i]
                if (ch == '+') {
                    bytes.add(' '.code.toByte())
                    i++
                } else if (ch == '%' && i + 2 < encoded.length) {
                    val parsed = encoded.substring(i + 1, i + 3).toIntOrNull(16)
                    if (parsed != null) {
                        bytes.add(parsed.toByte())
                        i += 3
                        continue
                    }
                    bytes.add(ch.code.toByte())
                    i++
                } else {
                    for (b in ch.toString().encodeToByteArray()) {
                        bytes.add(b)
                    }
                    i++
                }
            }
            return bytes.toByteArray().decodeToString()
        }
    }
}
