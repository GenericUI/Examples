package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.fragment.helper.TabItemSpec
import com.nexustools.genericui.component.fragment.helper.TabbedChromeMode
import com.nexustools.genericui.component.fragment.helper.TabbedFragment
import com.nexustools.genericui.component.fragment.helper.TabbedFragmentConfig
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.AlignItems
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexDirection
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.JustifyContent
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer

/**
 * Google Chrome Desktop and Mobile multi-tab browser clone extending [TabbedFragment].
 *
 * Uses the unified [TabbedFragment] API (`openTab`, `selectTab`, `closeTab`, `requestNewTab`,
 * `openMobileTabOverview`, `closeMobileTabOverview`, `toggleMobileTabOverview`, `updateViewportSize`)
 * across both responsive postures:
 * - **Desktop ([TabbedChromeMode.DESKTOP_BROWSER_TABS])**: Horizontal Chrome tab strip with pinned
 *   favicon shortcuts, full tabs, `+` button, `✦ Ask Gemini` pill button, omnibox toolbar, and bookmarks bar.
 * - **Mobile ([TabbedChromeMode.MOBILE_SWITCHER_PILL])**: Compact Chrome Android top bar (`[🏠 Home]`,
 *   `[🔒 Omnibox Pill]`, `[[5] Tab Counter Badge]`, `[⋮ Menu]`), 2-column card-grid tab switcher overview,
 *   and bottom Android system navigation bar (`|||`, `○`, `<`).
 */
@MainAppFragment(label = "GenericUIBrowser")
class GenericUIBrowserFragment(
    private val customPreferenceStore: PreferenceStore? = null
) : TabbedFragment(
    config = TabbedFragmentConfig(
        chromeMode = TabbedChromeMode.AUTO,
        mobileBreakpointWidthPx = 700,
        showNewTabButton = true,
        allowCloseLastTab = false,
        persistLastActiveTabKey = PERSIST_ACTIVE_TAB_KEY
    )
) {

    private var sessionStateLoaded: Boolean = false
    private var _sessionState: BrowserSessionState = BrowserSessionState()
    private val _bookmarks = BrowserBookmark.defaultBookmarks().toMutableList()
    private val _globalHistory = mutableListOf<BrowserHistoryEntry>()
    private var dynamicTabCounter: Int = 5

    /** Current persisted [BrowserSessionState]. */
    val sessionState: BrowserSessionState
        get() = _sessionState

    /** Current list of browser bookmarks displayed in the bookmarks bar and New Tab speed-dial. */
    val bookmarks: List<BrowserBookmark>
        get() = _bookmarks.toList()

    /** Aggregated navigation history across all tabs in this session. */
    val globalHistory: List<BrowserHistoryEntry>
        get() = _globalHistory.toList()

    /** Returns the currently active tab's [BrowserTabPageFragment], or `null` if no tab is active. */
    val activeTabPageFragment: BrowserTabPageFragment?
        get() = activeFragment as? BrowserTabPageFragment
            ?: activeTabId?.let { getOrCreateTabFragment(it) as? BrowserTabPageFragment }

    // --- Desktop Chrome Navigation & Omnibox Toolbar ---
    val pinnedFaviconsStrip: HStack = HStack(gap = SpacingScale.S1).apply {
        alignItems = AlignItems.CENTER
        flexWrap = FlexWrap.WRAP
        addClass("browser-pinned-favicons-strip")
    }

    val desktopToolbarRow: HStack = HStack(gap = SpacingScale.S1).apply {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.BETWEEN
        flexWrap = FlexWrap.WRAP
        addClass("browser-desktop-toolbar")
    }
    val navBackButton: Button = Button("<", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 34
    }
    val navForwardButton: Button = Button(">", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 34
    }
    val reloadButton: Button = Button("R", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 34
    }
    val securityLockBadge: Badge = Badge("HTTPS", type = BootstrapType.SUCCESS, pill = true).apply {
        widthPx = 56
    }
    val omniboxField: TextField = TextField(
        value = BrowserTabPageFragment.URL_AI_CREDITS,
        placeholder = "Search Google or type a URL"
    ).apply {
        addClass("browser-omnibox-pill")
        widthPx = 660
    }
    val goOmniboxButton: Button = Button("Go", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
        widthPx = 46
    }
    val bookmarkStarButton: Button = Button("★", type = BootstrapType.SECONDARY, size = BootstrapSize.SM).apply {
        widthPx = 36
    }
    val extensionsButton: Button = Button("Ext", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 48
    }
    val askGeminiPillButton: Button = Button(
        text = "✦ Ask Gemini",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM
    ).apply {
        addClass("browser-ask-gemini-btn")
        widthPx = 112
    }
    val profileButton: Button = Button("Profile", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 68
    }
    val desktopMenuButton: Button = Button("...", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 38
    }

    // --- Mobile Chrome Navigation & Pill Omnibox Toolbar ---
    val mobileToolbarRow: HStack = HStack(gap = SpacingScale.S2).apply {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.BETWEEN
        flexWrap = FlexWrap.WRAP
        addClass("browser-mobile-toolbar")
    }
    val mobileHomeButton: Button = Button("Home", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 56
    }
    val mobileSecurityLockBadge: Badge = Badge("HTTPS", type = BootstrapType.SUCCESS, pill = true).apply {
        widthPx = 56
    }
    val mobileOmniboxField: TextField = TextField(
        value = BrowserTabPageFragment.URL_AI_CREDITS,
        placeholder = "Search or type URL"
    ).apply {
        addClass("browser-omnibox-pill")
        widthPx = 200
    }
    val mobileTabCounterBadgeButton: Button = Button(
        text = "5",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        addClass("genericui-tabbed-mobile-pill")
        widthPx = 38
    }
    val mobileMenuButton: Button = Button("...", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        widthPx = 38
    }

    // --- Bookmarks Bar ---
    val bookmarksBarRow: HStack = HStack(gap = SpacingScale.S2).apply {
        alignItems = AlignItems.CENTER
        flexWrap = FlexWrap.WRAP
        addClass("browser-bookmarks-bar")
    }

    // --- Collapsible "✦ Ask Gemini" AI Assistant Side Panel ---
    val askGeminiSidebar: Card = Card("✦ Ask Gemini — Chrome AI Companion").apply {
        addClass("browser-gemini-sidebar")
        isVisible = false
    }
    val geminiContextSummaryLabel: Label = Label("Page context: AI credits activity (one.google.com)")
    val geminiPromptField: TextField = TextField(
        value = "",
        placeholder = "Ask Gemini about this page..."
    )
    val geminiSendButton: Button = Button("Ask", type = BootstrapType.PRIMARY, size = BootstrapSize.SM)
    val geminiResponseLabel: Label = Label(
        "Gemini is ready to summarize the active tab, explain repository architecture, or analyze AI credit usage."
    )

    // --- Mobile Android System Bottom Navigation Bar (`|||`, `○`, `<`) ---
    val mobileBottomNavBar: HStack = HStack(gap = SpacingScale.S4).apply {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.AROUND
        addClass("browser-mobile-bottom-nav")
        isVisible = false
    }
    val androidRecentsButton: Button = Button("|||", type = BootstrapType.DARK, size = BootstrapSize.SM)
    val androidHomeCircleButton: Button = Button("○", type = BootstrapType.DARK, size = BootstrapSize.SM)
    val androidBackTriangleButton: Button = Button("<", type = BootstrapType.DARK, size = BootstrapSize.SM)

    init {
        addClass("genericui-browser-root")
        ensureSessionStateLoaded()
        buildBrowserChromeLayout()
        wireBrowserChromeEvents()
        seedDefaultFiveTabs()
        syncResponsiveBrowserChrome()
        syncOmniboxAndNavigationState()
    }

    override fun getPreferenceStore(): PreferenceStore =
        customPreferenceStore ?: super.getPreferenceStore()

    override fun onCreate(savedState: FragmentBundle?) {
        ensureSessionStateLoaded()
        super.onCreate(savedState)
        syncAllTabCreditsBalances(_sessionState.aiCreditsBalance)
        syncResponsiveBrowserChrome()
        syncOmniboxAndNavigationState()
    }

    private fun ensureSessionStateLoaded() {
        if (sessionStateLoaded) return
        sessionStateLoaded = true
        val store = getPreferenceStore()
        _sessionState = BrowserSessionState.loadFrom(store)
        askGeminiSidebar.isVisible = _sessionState.askGeminiSidebarOpen
    }

    private fun persistSessionState() {
        BrowserSessionState.saveTo(getPreferenceStore(), _sessionState)
    }

    private fun buildBrowserChromeLayout() {
        // Pinned favicon badges in the desktop strip area (matching the Chrome Desktop screenshot)
        val pinnedIcons = listOf("G", "</>", "[!]", "★", "✦")
        for (icon in pinnedIcons) {
            pinnedFaviconsStrip.addChild(
                Badge(icon, type = BootstrapType.DARK, pill = true).apply {
                    addClass("pinned-favicon-badge")
                }
            )
        }

        // Desktop Omnibox & Actions row (flat row with explicit widths so omnibox spans wide)
        desktopToolbarRow.addChild(navBackButton)
        desktopToolbarRow.addChild(navForwardButton)
        desktopToolbarRow.addChild(reloadButton)
        desktopToolbarRow.addChild(securityLockBadge)
        desktopToolbarRow.addChild(omniboxField)
        desktopToolbarRow.addChild(goOmniboxButton)
        desktopToolbarRow.addChild(bookmarkStarButton)
        desktopToolbarRow.addChild(extensionsButton)
        desktopToolbarRow.addChild(askGeminiPillButton)
        desktopToolbarRow.addChild(profileButton)
        desktopToolbarRow.addChild(desktopMenuButton)

        // Mobile Top Bar: [Home] [HTTPS] [Rounded Pill Omnibox] [[5] Tab Counter Badge] [... Menu]
        mobileToolbarRow.addChild(mobileHomeButton)
        mobileToolbarRow.addChild(mobileSecurityLockBadge)
        mobileToolbarRow.addChild(mobileOmniboxField)
        mobileToolbarRow.addChild(mobileTabCounterBadgeButton)
        mobileToolbarRow.addChild(mobileMenuButton)

        // Bookmarks bar
        rebuildBookmarksBar()

        // Attach toolbars to TabbedFragment's headerSlotContainer
        headerSlotContainer.addChild(desktopToolbarRow)
        headerSlotContainer.addChild(mobileToolbarRow)
        headerSlotContainer.addChild(bookmarksBarRow)

        // Build Ask Gemini Sidebar
        val geminiStack = VStack(gap = SpacingScale.S2)
        geminiStack.addChild(geminiContextSummaryLabel)
        val promptRow = HStack(gap = SpacingScale.S1).apply {
            alignItems = AlignItems.CENTER
        }
        promptRow.addChild(geminiPromptField)
        promptRow.addChild(geminiSendButton)
        geminiStack.addChild(promptRow)
        geminiStack.addChild(geminiResponseLabel)
        askGeminiSidebar.addChild(geminiStack)
        addChild(askGeminiSidebar)

        // Build Mobile Bottom Navigation Bar (`|||`, `○`, `<`)
        mobileBottomNavBar.addChild(androidRecentsButton)
        mobileBottomNavBar.addChild(androidHomeCircleButton)
        mobileBottomNavBar.addChild(androidBackTriangleButton)
        addChild(mobileBottomNavBar)
    }

    private fun wireBrowserChromeEvents() {
        onNewTabRequested = {
            createNewTabSpec(
                url = BrowserTabPageFragment.URL_NEW_TAB,
                title = "New Tab"
            )
        }

        onActiveTabChanged = { _, _, _ ->
            syncOmniboxAndNavigationState()
            syncResponsiveBrowserChrome()
        }

        onTabOpened = {
            syncMobileTabCounterBadges()
            styleDesktopTabStrip()
        }

        onTabClosed = {
            syncMobileTabCounterBadges()
            styleDesktopTabStrip()
        }

        onMobileOverviewStateChanged = {
            syncMobileTabCounterBadges()
        }

        navBackButton.setOnClick {
            navigateBackInActiveTab()
        }
        navForwardButton.setOnClick {
            navigateForwardInActiveTab()
        }
        reloadButton.setOnClick {
            reloadActiveTab()
        }
        goOmniboxButton.setOnClick {
            navigateActiveTabTo(omniboxField.value)
        }
        omniboxField.onSubmit = { submitted ->
            navigateActiveTabTo(submitted)
        }
        mobileOmniboxField.onSubmit = { submitted ->
            navigateActiveTabTo(submitted)
        }

        bookmarkStarButton.setOnClick {
            toggleBookmarkForActiveTab()
        }

        askGeminiPillButton.setOnClick {
            toggleAskGeminiSidebar()
        }

        val handleGeminiAsk: () -> Unit = {
            val prompt = geminiPromptField.value.trim().ifEmpty { "Summarize this page" }
            val activePage = activeTabPageFragment
            val pageTitle = activePage?.currentTitle ?: activeTabSpec?.title ?: "Active Page"
            val pageUrl = activePage?.currentUrl ?: ""
            geminiResponseLabel.text =
                "✦ Gemini response for '$prompt' on $pageTitle ($pageUrl): Balance is ${
                    BrowserCreditActivityRow.formatNumberWithCommas(_sessionState.aiCreditsBalance)
                } AI credits."
        }
        geminiSendButton.setOnClick(handleGeminiAsk)
        geminiPromptField.onSubmit = { handleGeminiAsk() }

        mobileHomeButton.setOnClick {
            navigateHomeInActiveTab()
        }
        mobileTabCounterBadgeButton.setOnClick {
            toggleMobileTabOverview()
        }
        androidRecentsButton.setOnClick {
            toggleMobileTabOverview()
        }
        androidHomeCircleButton.setOnClick {
            if (isMobileTabOverviewOpen) {
                closeMobileTabOverview()
            }
            navigateHomeInActiveTab()
        }
        androidBackTriangleButton.setOnClick {
            onBackPressed()
        }
    }

    private fun seedDefaultFiveTabs() {
        if (tabs.isNotEmpty()) return

        val persistedTabId = getPreferenceStore()
            .getString(PERSIST_ACTIVE_TAB_KEY, null)
            ?.takeIf { it.isNotBlank() }

        // 1. Google Keep
        openTab(
            createTabSpecWithId(
                tabId = TAB_ID_KEEP,
                title = "Google Keep",
                subtitle = "keep.google.com",
                url = BrowserTabPageFragment.URL_KEEP,
                iconName = "[!]"
            ),
            selectImmediately = false
        )

        // 2. Overcoming Overwhelming Anxiety (Gemini Doc)
        openTab(
            createTabSpecWithId(
                tabId = TAB_ID_GEMINI_DOC,
                title = "Overcoming Overwhelming Anxiety",
                subtitle = "gemini.google.com/app",
                url = BrowserTabPageFragment.URL_GEMINI_DOC,
                iconName = "★"
            ),
            selectImmediately = false
        )

        // 3. T-Crypt/berth (GitHub Repository from Mobile & Desktop screenshots)
        openTab(
            createTabSpecWithId(
                tabId = TAB_ID_GITHUB_BERTH,
                title = "T-Crypt/berth",
                subtitle = "github.com/T-Crypt/berth",
                url = BrowserTabPageFragment.URL_GITHUB_BERTH,
                iconName = "</>"
            ),
            selectImmediately = false
        )

        // 4. Chrome New Tab
        openTab(
            createTabSpecWithId(
                tabId = TAB_ID_NEW_TAB,
                title = "New Tab",
                subtitle = "chrome://newtab",
                url = BrowserTabPageFragment.URL_NEW_TAB,
                iconName = "[+]"
            ),
            selectImmediately = false
        )

        // 5. Google One - AI credits activity (Initial Active Tab on Desktop screenshot)
        openTab(
            createTabSpecWithId(
                tabId = TAB_ID_AI_CREDITS,
                title = "AI credits activity",
                subtitle = "one.google.com/ai/activity",
                url = BrowserTabPageFragment.URL_AI_CREDITS,
                iconName = "✦"
            ),
            selectImmediately = false
        )

        val initialTarget = if (persistedTabId != null && tabs.any { it.tabId == persistedTabId }) {
            persistedTabId
        } else {
            TAB_ID_AI_CREDITS
        }
        selectTab(initialTarget)
    }

    private fun createNewTabSpec(url: String, title: String? = null): TabItemSpec {
        dynamicTabCounter += 1
        val tabId = "tab-$dynamicTabCounter"
        val resolvedTitle = title ?: BrowserTabPageFragment.resolveTitleForUrl(url)
        val resolvedSubtitle = BrowserTabPageFragment.resolveSubtitleForUrl(url)
        val resolvedIcon = BrowserTabPageFragment.resolveIconForUrl(url)
        return createTabSpecWithId(
            tabId = tabId,
            title = resolvedTitle,
            subtitle = resolvedSubtitle,
            url = url,
            iconName = resolvedIcon
        )
    }

    private fun createTabSpecWithId(
        tabId: String,
        title: String,
        subtitle: String,
        url: String,
        iconName: String
    ): TabItemSpec = TabItemSpec(
        tabId = tabId,
        title = title,
        subtitle = subtitle,
        iconName = iconName,
        closable = true,
        pinned = false,
        fragmentFactory = {
            BrowserTabPageFragment(
                initialUrl = url,
                initialCreditsBalance = _sessionState.aiCreditsBalance,
                initialBookmarks = _bookmarks
            ).also { pageFragment ->
                wireTabPageFragmentCallbacks(tabId, pageFragment)
            }
        }
    )

    private fun wireTabPageFragmentCallbacks(tabId: String, pageFragment: BrowserTabPageFragment) {
        recordGlobalHistoryEntry(pageFragment.currentUrl, pageFragment.currentTitle)

        pageFragment.onPageNavigated = { newUrl, newTitle, _ ->
            updateTabTitle(tabId, newTitle)
            updateTabSubtitle(tabId, BrowserTabPageFragment.resolveSubtitleForUrl(newUrl))
            recordGlobalHistoryEntry(newUrl, newTitle)
            if (activeTabId == tabId) {
                syncOmniboxAndNavigationState()
            }
        }

        pageFragment.onCreditsBalanceChanged = { newBalance, _ ->
            _sessionState = _sessionState.copy(aiCreditsBalance = newBalance)
            persistSessionState()
            syncAllTabCreditsBalances(newBalance)
            syncOmniboxAndNavigationState()
        }

        pageFragment.onViewportResized = { _, _ ->
            syncResponsiveBrowserChrome()
        }
    }

    private fun syncAllTabCreditsBalances(newBalance: Int) {
        for (spec in tabs) {
            val existing = getTabFragment(spec.tabId) as? BrowserTabPageFragment
            existing?.syncAiCreditsBalance(newBalance)
        }
    }

    private fun recordGlobalHistoryEntry(url: String, title: String) {
        if (_globalHistory.lastOrNull()?.url != url) {
            _globalHistory.add(
                BrowserHistoryEntry(
                    url = url,
                    title = title,
                    timestampIso = "2026-10-27T06:00:00Z"
                )
            )
        }
    }

    /**
     * Opens a new browser tab pointing at [url] using [TabbedFragment.openTab].
     */
    fun openBrowserTab(
        url: String = BrowserTabPageFragment.URL_NEW_TAB,
        title: String? = null,
        selectImmediately: Boolean = true
    ): TabItemSpec {
        val spec = createNewTabSpec(url = url, title = title)
        return openTab(spec, selectImmediately = selectImmediately)
    }

    /**
     * Navigates the currently active tab to [urlOrQuery] via the omnibox.
     */
    fun navigateActiveTabTo(urlOrQuery: String) {
        val page = activeTabPageFragment ?: return
        page.navigateTo(urlOrQuery, recordHistory = true, searchPrefix = _sessionState.searchEnginePrefix)
        syncOmniboxAndNavigationState()
    }

    /**
     * Navigates backward in the active tab's history stack if possible.
     */
    fun navigateBackInActiveTab(): Boolean {
        val moved = activeTabPageFragment?.goBack() ?: false
        syncOmniboxAndNavigationState()
        return moved
    }

    /**
     * Navigates forward in the active tab's history stack if possible.
     */
    fun navigateForwardInActiveTab(): Boolean {
        val moved = activeTabPageFragment?.goForward() ?: false
        syncOmniboxAndNavigationState()
        return moved
    }

    /**
     * Reloads the active tab.
     */
    fun reloadActiveTab() {
        activeTabPageFragment?.reload()
        syncOmniboxAndNavigationState()
    }

    /**
     * Navigates the active tab to [BrowserSessionState.homeUrl].
     */
    fun navigateHomeInActiveTab() {
        navigateActiveTabTo(_sessionState.homeUrl)
    }

    /**
     * Increments the Google One AI credits balance by [delta] (default `1,000`) and persists it.
     */
    fun addAiCredits(delta: Int = 1000): Int {
        val creditsTab = getOrCreateTabFragment(TAB_ID_AI_CREDITS) as? BrowserTabPageFragment
            ?: activeTabPageFragment
        if (creditsTab != null) {
            creditsTab.addAiCredits(delta)
        } else {
            val updated = _sessionState.aiCreditsBalance + delta
            _sessionState = _sessionState.copy(aiCreditsBalance = updated)
            persistSessionState()
        }
        return _sessionState.aiCreditsBalance
    }

    /**
     * Toggles the `✦ Ask Gemini` side panel open or closed and persists the preference.
     */
    fun toggleAskGeminiSidebar(): Boolean {
        val nextOpen = !askGeminiSidebar.isVisible
        askGeminiSidebar.isVisible = nextOpen
        _sessionState = _sessionState.copy(askGeminiSidebarOpen = nextOpen)
        persistSessionState()
        return nextOpen
    }

    /**
     * Toggles visibility of the desktop bookmarks bar and persists the preference.
     */
    fun toggleBookmarksBar(): Boolean {
        val nextShow = !_sessionState.showBookmarksBar
        _sessionState = _sessionState.copy(showBookmarksBar = nextShow)
        persistSessionState()
        syncResponsiveBrowserChrome()
        return nextShow
    }

    /**
     * Adds or removes a bookmark for the currently active tab's URL.
     *
     * @return `true` if the active URL is now bookmarked, or `false` if its bookmark was removed.
     */
    fun toggleBookmarkForActiveTab(): Boolean {
        val page = activeTabPageFragment ?: return false
        val targetUrl = page.currentUrl
        val existingIdx = _bookmarks.indexOfFirst { it.url == targetUrl }
        val isNowBookmarked = if (existingIdx >= 0) {
            _bookmarks.removeAt(existingIdx)
            false
        } else {
            _bookmarks.add(
                BrowserBookmark(
                    id = "bm-${_bookmarks.size + 1}",
                    title = page.currentTitle,
                    url = targetUrl,
                    iconLabel = page.currentIcon
                )
            )
            true
        }
        rebuildBookmarksBar()
        syncAllTabSpeedDials()
        syncOmniboxAndNavigationState()
        return isNowBookmarked
    }

    /**
     * Exports all current [bookmarks] into [writable] using [BrowserBookmark.writeListToBuffer].
     */
    fun exportBookmarksToBuffer(writable: WritableByteBuffer) {
        BrowserBookmark.writeListToBuffer(_bookmarks, writable)
    }

    /**
     * Imports bookmarks from [readable] using [BrowserBookmark.readListFromBuffer] and updates the bookmarks bar.
     */
    fun importBookmarksFromBuffer(readable: ReadableByteBuffer): List<BrowserBookmark> {
        val imported = BrowserBookmark.readListFromBuffer(readable)
        _bookmarks.clear()
        _bookmarks.addAll(imported)
        rebuildBookmarksBar()
        syncAllTabSpeedDials()
        syncOmniboxAndNavigationState()
        return _bookmarks.toList()
    }

    private fun rebuildBookmarksBar() {
        bookmarksBarRow.clearChildren()
        bookmarksBarRow.addChild(Badge("Bookmarks:", type = BootstrapType.SECONDARY, pill = true))
        for (bookmark in _bookmarks) {
            val btn = Button(
                text = "${bookmark.iconLabel} ${bookmark.title}",
                type = BootstrapType.DARK,
                size = BootstrapSize.SM
            ).apply {
                addClass("bookmark-bar-item")
                setOnClick {
                    navigateActiveTabTo(bookmark.url)
                }
            }
            bookmarksBarRow.addChild(btn)
        }
    }

    private fun styleDesktopTabStrip() {
        for (child in desktopTabStripContainer.children) {
            if (child is FlexBox && child.hasClass("genericui-tabbed-chip")) {
                val isActive = child.hasClass("active")
                var selectW = 140
                var closeW = 0
                for (inner in child.children) {
                    if (inner is Button && inner.hasClass("genericui-tabbed-select-btn")) {
                        inner.type = if (isActive) BootstrapType.PRIMARY else BootstrapType.DARK
                        selectW = (inner.text.length * 6 + 22).coerceIn(104, 232)
                        inner.widthPx = selectW
                    } else if (inner is Button && inner.hasClass("genericui-tabbed-close-btn")) {
                        inner.type = if (isActive) BootstrapType.PRIMARY else BootstrapType.DARK
                        inner.outline = false
                        closeW = 26
                        inner.widthPx = closeW
                    }
                }
                child.widthPx = selectW + (if (closeW > 0) closeW + 6 else 0)
            } else if (child is Button && child.hasClass("genericui-tabbed-new-tab-btn")) {
                child.type = BootstrapType.SECONDARY
                child.widthPx = 34
            }
        }
    }

    private fun syncAllTabSpeedDials() {
        for (spec in tabs) {
            val frag = getTabFragment(spec.tabId) as? BrowserTabPageFragment
            frag?.setSpeedDialBookmarks(_bookmarks)
        }
    }

    private fun syncMobileTabCounterBadges() {
        val countText = tabCount.toString()
        mobileTabCounterBadgeButton.text = countText
    }

    /**
     * Synchronizes Desktop vs. Mobile toolbar, bookmarks bar, and bottom navigation bar visibility
     * with [resolvedChromeMode].
     */
    fun syncResponsiveBrowserChrome() {
        val mode = resolvedChromeMode
        val isDesktop = (mode == TabbedChromeMode.DESKTOP_BROWSER_TABS)
        val isMobile = (mode == TabbedChromeMode.MOBILE_SWITCHER_PILL)

        desktopToolbarRow.isVisible = isDesktop
        bookmarksBarRow.isVisible = isDesktop && _sessionState.showBookmarksBar
        mobileToolbarRow.isVisible = isMobile
        mobileBottomNavBar.isVisible = isMobile
        headerSlotContainer.heightPx = when {
            isDesktop && _sessionState.showBookmarksBar -> 108
            else -> 72
        }
        styleDesktopTabStrip()
        syncMobileTabCounterBadges()
    }

    private fun syncOmniboxAndNavigationState() {
        val page = activeTabPageFragment
        val url = page?.currentUrl ?: activeTabSpec?.subtitle ?: ""
        val title = page?.currentTitle ?: activeTabSpec?.title ?: "GenericUIBrowser"

        omniboxField.value = url
        mobileOmniboxField.value = BrowserTabPageFragment.resolveSubtitleForUrl(url).ifBlank { url }
        navBackButton.isEnabled = page?.canGoBack == true
        navForwardButton.isEnabled = page?.canGoForward == true

        val isBookmarked = _bookmarks.any { it.url == url }
        bookmarkStarButton.text = if (isBookmarked) "★" else "*"
        geminiContextSummaryLabel.text = "Page context: $title (${BrowserTabPageFragment.resolveSubtitleForUrl(url)})"
        styleDesktopTabStrip()
        syncMobileTabCounterBadges()
    }

    override fun onResize(viewportWidth: Int, viewportHeight: Int) {
        super.onResize(viewportWidth, viewportHeight)
        syncResponsiveBrowserChrome()
    }

    override fun onBackPressed(): Boolean {
        if (isMobileTabOverviewOpen) {
            closeMobileTabOverview()
            return true
        }
        if (navigateBackInActiveTab()) {
            return true
        }
        return super.onBackPressed()
    }

    companion object {
        const val PERSIST_ACTIVE_TAB_KEY: String = "browser.activeTabId"
        const val TAB_ID_KEEP: String = "tab-keep"
        const val TAB_ID_GEMINI_DOC: String = "tab-gemini-doc"
        const val TAB_ID_GITHUB_BERTH: String = "tab-github-berth"
        const val TAB_ID_NEW_TAB: String = "tab-newtab"
        const val TAB_ID_AI_CREDITS: String = "tab-ai-credits"
    }
}
