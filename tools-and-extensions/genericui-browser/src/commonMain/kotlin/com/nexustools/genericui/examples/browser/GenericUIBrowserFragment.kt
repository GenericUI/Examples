package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.action.styled.StyledButton
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.styled.StyledTextField
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.fragment.helper.TabItemSpec
import com.nexustools.genericui.component.fragment.helper.TabbedChromeMode
import com.nexustools.genericui.component.fragment.helper.TabbedChromeStyle
import com.nexustools.genericui.component.fragment.helper.TabbedFragment
import com.nexustools.genericui.component.fragment.helper.TabbedFragmentConfig
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.shell.Application
import com.nexustools.genericui.component.shell.CheckMenuItem
import com.nexustools.genericui.component.shell.ContextMenu
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.shell.RadioMenuItem
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.core.AbstractComponent
import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.enums.bootstrap.AlignItems
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.JustifyContent
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.css.CssBorderStyle
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer
import com.nexustools.genericui.style.css.CssCornerRadii
import com.nexustools.genericui.style.css.CssEdgeLengths
import com.nexustools.genericui.style.css.CssLength

/**
 * Multi-tab Desktop and Mobile web browser extending [TabbedFragment] with
 * [TabbedChromeStyle.CHROMIUM] chrome, titlebar-aware desktop tabs, a pill [StyledTextField]
 * omnibox, circular [StyledButton] toolbar controls, and accessible [Label.target] settings.
 *
 * Starts with **0 open pages** displaying a clean `GenericUI Browser` New Tab / Search landing view.
 * Submitting any URL or search query from the top omnibox or New Tab search field opens (or navigates)
 * a tab powered by `:extensions:ui:webview` ([com.nexustools.genericui.ext.webview.WebView]),
 * while closing the last tab cleanly returns to 0 open pages and the Default Search / New Tab view.
 */
@MainAppFragment(label = "GenericUIBrowser")
class GenericUIBrowserFragment(
    private val customPreferenceStore: PreferenceStore? = null
) : TabbedFragment(
    config = TabbedFragmentConfig(
        chromeMode = TabbedChromeMode.AUTO,
        mobileBreakpointWidthPx = 700,
        showNewTabButton = true,
        allowCloseLastTab = true,
        persistLastActiveTabKey = null,
        chromeStyle = TabbedChromeStyle.CHROMIUM,
        showTabSearchButton = true,
        connectActiveTabToNavbar = true,
        showTitleBarWindowIcon = false
    )
) {

    private var sessionStateLoaded: Boolean = false
    private var _sessionState: BrowserSessionState = BrowserSessionState()
    private val _bookmarks = BrowserBookmark.defaultBookmarks().toMutableList()
    private val _globalHistory = mutableListOf<BrowserHistoryEntry>()
    private var dynamicTabCounter: Int = 0
    private var historySequence: Long = 0L
    private var explicitThemeOverride: ThemeMode? = null
    private var observedBrowserThemeApp: Application? = null
    private var lastResponsiveSyncedMode: TabbedChromeMode? = null
    private var lastBookmarksBarVisible: Boolean? = null
    private var lastStageEmptyVisible: Boolean? = null
    private val browserThemeChangeListener: (ThemeMode) -> Unit = {
        syncBrowserThemeVisuals()
    }

    /** Current persisted [BrowserSessionState]. */
    val sessionState: BrowserSessionState
        get() = _sessionState

    /** Current list of user-saved bookmarks displayed in the bookmarks bar and New Tab view. */
    val bookmarks: List<BrowserBookmark>
        get() = _bookmarks.toList()

    /** Aggregated navigation history across all tabs in this session. */
    val globalHistory: List<BrowserHistoryEntry>
        get() = _globalHistory.toList()

    /** Returns the currently active tab's [BrowserTabPageFragment], or `null` when 0 tabs are open. */
    val activeTabPageFragment: BrowserTabPageFragment?
        get() {
            val id = activeTabId ?: return null
            return getTabFragment(id) as? BrowserTabPageFragment
                ?: (activeFragment as? BrowserTabPageFragment)?.takeIf { it.routeKey == id }
                ?: getOrCreateTabFragment(id) as? BrowserTabPageFragment
        }

    // --- Default Search / New Tab Surface (Shown when 0 open pages) ---
    val defaultNewTabSurface: BrowserNewTabView = BrowserNewTabView(
        showNewTabButton = true,
        initialBookmarks = emptyList(),
        idPrefix = "browser-newtab"
    ).apply {
        id = "browser-default-newtab"
        addClass("browser-default-newtab-page")
    }

    // --- Unified Chromium Navbar Mounted via TabbedFragment.setTabNavbar(...) ---
    val navToolbar: VStack = VStack(gapPx = 0).apply {
        id = "browser-nav-toolbar"
        addClass("browser-nav-toolbar")
        getStyle().apply {
            setWidth(CssLength.PERCENT_100)
            setFlexGrow(1f)
        }
    }

    // --- Desktop Browser Navigation & Pill Omnibox Toolbar ---
    val desktopToolbarRow: HStack = HStack(gapPx = 8).apply {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.BETWEEN
        flexWrap = FlexWrap.NOWRAP
        addClass("browser-desktop-toolbar")
        getStyle().apply {
            setWidth(CssLength.PERCENT_100)
            setFlexGrow(1f)
            setPadding(CssEdgeLengths.px(4, 8, 4, 8))
        }
    }
    val navLeadingGroup: HStack = HStack(gapPx = 4).apply {
        alignItems = AlignItems.CENTER
        addClass("browser-toolbar-group")
    }
    val omniboxGroup: HStack = HStack(gapPx = 6).apply {
        alignItems = AlignItems.CENTER
        addClass("browser-omnibox-group")
        getStyle().apply {
            setFlexGrow(1f)
            setMinWidthPx(220)
        }
    }
    val navTrailingGroup: HStack = HStack(gapPx = 4).apply {
        alignItems = AlignItems.CENTER
        addClass("browser-toolbar-group")
    }
    val navBackButton: StyledButton = StyledButton("←", type = null, size = BootstrapSize.SM).apply {
        id = "browser-nav-back"
        addClass("browser-icon-btn")
        tooltipText = "Back"
    }
    val navForwardButton: StyledButton = StyledButton("→", type = null, size = BootstrapSize.SM).apply {
        id = "browser-nav-forward"
        addClass("browser-icon-btn")
        tooltipText = "Forward"
    }
    val reloadButton: StyledButton = StyledButton("↻", type = null, size = BootstrapSize.SM).apply {
        id = "browser-nav-reload"
        addClass("browser-icon-btn")
        tooltipText = "Reload"
    }
    val homeButton: StyledButton = StyledButton("⌂", type = null, size = BootstrapSize.SM).apply {
        id = "browser-nav-home"
        addClass("browser-icon-btn")
        tooltipText = "Home"
    }
    val securityLockBadge: StyledButton = StyledButton("ⓘ", type = null, size = BootstrapSize.SM).apply {
        id = "browser-security-badge"
        addClass("browser-security-badge")
        tooltipText = "New Tab"
    }
    val omniboxField: StyledTextField = StyledTextField(
        value = "",
        placeholder = "Search or enter web address",
        multilineMode = false
    ).apply {
        id = "browser-omnibox"
        addClass("browser-omnibox-pill")
    }
    val browserMenuButton: StyledButton = StyledButton("⋮", type = null, size = BootstrapSize.SM).apply {
        id = "browser-menu-btn"
        addClass("browser-icon-btn")
        tooltipText = "Customize and control GenericUIBrowser"
    }
    val browserOverflowMenu: ContextMenu = ContextMenu().apply {
        id = "browser-overflow-menu"
    }

    // --- Mobile Browser Navigation & Pill Omnibox Toolbar ---
    val mobileToolbarRow: HStack = HStack(gapPx = 6).apply {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.BETWEEN
        flexWrap = FlexWrap.NOWRAP
        addClass("browser-mobile-toolbar")
        getStyle().apply {
            setWidth(CssLength.PERCENT_100)
            setFlexGrow(1f)
            setPadding(CssEdgeLengths.px(4, 6, 4, 6))
        }
    }
    val mobileHomeButton: StyledButton = StyledButton("⌂", type = null, size = BootstrapSize.SM).apply {
        id = "browser-mobile-home"
        addClass("browser-icon-btn")
        tooltipText = "Home"
    }
    val mobileSecurityLockBadge: StyledButton = StyledButton("ⓘ", type = null, size = BootstrapSize.SM).apply {
        id = "browser-mobile-security-badge"
        addClass("browser-security-badge")
        tooltipText = "New Tab"
    }
    val mobileOmniboxField: StyledTextField = StyledTextField(
        value = "",
        placeholder = "Search or enter web address",
        multilineMode = false
    ).apply {
        id = "browser-mobile-omnibox"
        addClass("browser-omnibox-pill")
    }
    val mobileTabCounterBadgeButton: StyledButton = StyledButton(
        text = "0",
        type = null,
        size = BootstrapSize.SM
    ).apply {
        id = "browser-mobile-tab-count"
        addClass("genericui-tabbed-mobile-pill", "browser-pill-action-btn")
        minWidthPx = 36
    }
    val mobileMenuButton: StyledButton = StyledButton("⋮", type = null, size = BootstrapSize.SM).apply {
        id = "browser-mobile-menu"
        addClass("browser-icon-btn")
        tooltipText = "Customize and control GenericUIBrowser"
    }

    // --- Bookmarks Bar ---
    val bookmarksBarRow: HStack = HStack(gapPx = 8).apply {
        alignItems = AlignItems.CENTER
        flexWrap = FlexWrap.WRAP
        addClass("browser-bookmarks-bar")
        getStyle().apply {
            setWidth(CssLength.PERCENT_100)
            setPadding(CssEdgeLengths.px(5, 14, 5, 14))
            setMargin(CssEdgeLengths.ZERO)
        }
    }

    // --- Collapsible Browser Settings Panel (with explicit Label.target bindings) ---
    val settingsPanel: Card = Card("Browser Settings").apply {
        id = "browser-settings-panel"
        addClass("browser-settings-panel")
        isVisible = false
        getStyle().apply {
            setMargin(CssEdgeLengths.px(8, 14, 6, 14))
            setPadding(CssEdgeLengths.px(16, 20, 16, 20))
            setBorderRadius(CssCornerRadii.px(14))
        }
    }
    val settingsHomeUrlField: StyledTextField = StyledTextField(
        value = BrowserSessionState.DEFAULT_HOME_URL,
        placeholder = "Home URL (e.g. about:newtab or https://...)",
        multilineMode = false
    ).apply {
        id = "browser-settings-home-url"
        addClass("browser-settings-input")
    }
    val settingsHomeUrlLabel: Label = Label(
        text = "Home URL",
        target = settingsHomeUrlField
    ).apply {
        id = "browser-settings-home-label"
        addClass("browser-settings-label")
    }
    val settingsSearchPrefixField: StyledTextField = StyledTextField(
        value = BrowserSessionState.DEFAULT_SEARCH_PREFIX,
        placeholder = "Search engine URL prefix",
        multilineMode = false
    ).apply {
        id = "browser-settings-search-prefix"
        addClass("browser-settings-input")
    }
    val settingsSearchPrefixLabel: Label = Label(
        text = "Search Engine",
        target = settingsSearchPrefixField
    ).apply {
        id = "browser-settings-search-label"
        addClass("browser-settings-label")
    }
    val settingsShowBookmarksCheckBox: CheckBox = CheckBox(
        label = "Visible below toolbar",
        checked = BrowserSessionState().showBookmarksBar
    ).apply {
        id = "browser-settings-bookmarks-checkbox"
        addClass("browser-settings-checkbox")
    }
    val settingsShowBookmarksLabel: Label = Label(
        text = "Bookmarks Bar",
        target = settingsShowBookmarksCheckBox
    ).apply {
        id = "browser-settings-bookmarks-label"
        addClass("browser-settings-label")
    }
    val settingsThemeSelect: Select = Select(
        options = listOf(
            SelectOption(ThemeMode.AUTO.name, "System Default"),
            SelectOption(ThemeMode.LIGHT.name, "Light"),
            SelectOption(ThemeMode.DARK.name, "Dark")
        ),
        selectedValue = ThemeMode.AUTO.name
    ).apply {
        id = "browser-settings-theme-select"
        addClass("form-select", "browser-settings-select")
    }
    val settingsThemeLabel: Label = Label(
        text = "Theme",
        target = settingsThemeSelect
    ).apply {
        id = "browser-settings-theme-label"
        addClass("browser-settings-label")
    }
    val settingsSaveButton: StyledButton = StyledButton("Save", type = null, size = BootstrapSize.SM).apply {
        id = "browser-settings-save"
        addClass("browser-settings-save-btn")
    }
    val settingsToggleBookmarksButton: StyledButton = StyledButton(
        text = "Toggle Bookmarks Bar",
        type = null,
        size = BootstrapSize.SM
    ).apply {
        id = "browser-settings-bookmarks-toggle"
        addClass("browser-pill-action-btn")
    }
    val settingsClearHistoryButton: StyledButton = StyledButton(
        text = "Clear History",
        type = BootstrapType.DANGER,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "browser-settings-clear-history"
    }
    val settingsCloseButton: StyledButton = StyledButton("Close", type = null, size = BootstrapSize.SM).apply {
        id = "browser-settings-close"
        addClass("browser-pill-action-btn")
    }

    // --- Mobile Bottom System Navigation Bar (`|||`, `○`, `<`) ---
    val mobileBottomNavBar: HStack = HStack(gap = SpacingScale.S4).apply {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.AROUND
        addClass("browser-mobile-bottom-nav")
        isVisible = false
    }
    val androidRecentsButton: StyledButton = StyledButton("|||", type = null, size = BootstrapSize.SM).apply {
        addClass("browser-icon-btn")
    }
    val androidHomeCircleButton: StyledButton = StyledButton("○", type = null, size = BootstrapSize.SM).apply {
        addClass("browser-icon-btn")
    }
    val androidBackTriangleButton: StyledButton = StyledButton("‹", type = null, size = BootstrapSize.SM).apply {
        addClass("browser-icon-btn")
    }

    init {
        addClass("genericui-browser-root")
        ensureSessionStateLoaded()
        buildBrowserChromeLayout()
        wireBrowserChromeEvents()
        syncBrowserThemeVisuals()
        syncResponsiveBrowserChrome()
        syncOmniboxAndNavigationState()
        syncStageEmptyNewTabVisibility()
    }

    override fun getPreferenceStore(): PreferenceStore =
        customPreferenceStore ?: super.getPreferenceStore()

    override fun onCreate(savedState: FragmentBundle?) {
        ensureSessionStateLoaded()
        super.onCreate(savedState)
        syncResponsiveBrowserChrome()
        syncOmniboxAndNavigationState()
        syncStageEmptyNewTabVisibility()
    }

    override fun onStart() {
        super.onStart()
        val nextApp = (application as? Application) ?: Application.current
        if (observedBrowserThemeApp !== nextApp) {
            observedBrowserThemeApp?.removeThemeChangeListener(browserThemeChangeListener)
            observedBrowserThemeApp = nextApp
            nextApp?.addThemeChangeListener(browserThemeChangeListener)
        }
        syncBrowserThemeVisuals()
    }

    override fun onDestroy() {
        observedBrowserThemeApp?.removeThemeChangeListener(browserThemeChangeListener)
        observedBrowserThemeApp = null
        super.onDestroy()
    }

    private fun ensureSessionStateLoaded() {
        if (sessionStateLoaded) return
        sessionStateLoaded = true
        val store = getPreferenceStore()
        _sessionState = BrowserSessionState.loadFrom(store)
        _bookmarks.clear()
        _bookmarks.addAll(BrowserBookmark.loadListFrom(store))
        settingsHomeUrlField.value = _sessionState.homeUrl
        settingsSearchPrefixField.value = _sessionState.searchEnginePrefix
        settingsShowBookmarksCheckBox.isChecked = _sessionState.showBookmarksBar
        defaultNewTabSurface.setBookmarks(_bookmarks)
    }

    private fun persistSessionState() {
        BrowserSessionState.saveTo(getPreferenceStore(), _sessionState)
    }

    private fun persistBookmarks() {
        BrowserBookmark.saveListTo(getPreferenceStore(), _bookmarks)
    }

    private fun buildSettingsRow(label: Label, control: com.nexustools.genericui.core.AbstractComponent): HStack {
        label.getStyle().apply {
            setMinWidthPx(116)
            setFontWeight(FontWeight.SEMIBOLD)
            setFontSizePx(13f)
        }
        return HStack(gapPx = 12).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
            addClass("browser-settings-row")
            getStyle().apply {
                setWidth(CssLength.PERCENT_100)
                setPadding(CssEdgeLengths.px(2, 0, 2, 0))
            }
            addChild(label)
            addChild(control)
        }
    }

    private fun buildBrowserChromeLayout() {
        navLeadingGroup.addChild(navBackButton)
        navLeadingGroup.addChild(navForwardButton)
        navLeadingGroup.addChild(reloadButton)
        navLeadingGroup.addChild(homeButton)

        omniboxGroup.addChild(securityLockBadge)
        omniboxGroup.addChild(omniboxField)

        navTrailingGroup.addChild(browserMenuButton)

        desktopToolbarRow.addChild(navLeadingGroup)
        desktopToolbarRow.addChild(omniboxGroup)
        desktopToolbarRow.addChild(navTrailingGroup)

        mobileToolbarRow.addChild(mobileHomeButton)
        mobileToolbarRow.addChild(mobileSecurityLockBadge)
        mobileToolbarRow.addChild(mobileOmniboxField)
        mobileToolbarRow.addChild(mobileTabCounterBadgeButton)
        mobileToolbarRow.addChild(mobileMenuButton)

        navToolbar.addChild(desktopToolbarRow)
        navToolbar.addChild(mobileToolbarRow)
        setTabNavbar(navToolbar)

        rebuildBookmarksBar()
        val navSlotIdx = children.indexOf(tabNavbarSlotContainer)
        val bookmarksInsertIdx = if (navSlotIdx >= 0) navSlotIdx + 1 else 2
        addChild(bookmarksBarRow, bookmarksInsertIdx)

        val settingsStack = VStack(gapPx = 12).apply {
            addClass("browser-settings-form")
            getStyle().apply {
                setWidth(CssLength.PERCENT_100)
                setPadding(CssEdgeLengths.px(8, 0, 4, 0))
            }
        }
        settingsStack.addChild(buildSettingsRow(settingsHomeUrlLabel, settingsHomeUrlField))
        settingsStack.addChild(buildSettingsRow(settingsSearchPrefixLabel, settingsSearchPrefixField))
        settingsStack.addChild(buildSettingsRow(settingsShowBookmarksLabel, settingsShowBookmarksCheckBox))
        settingsStack.addChild(buildSettingsRow(settingsThemeLabel, settingsThemeSelect))

        val actionsRow = HStack(gapPx = 8).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
            addClass("browser-settings-actions")
            getStyle().apply {
                setWidth(CssLength.PERCENT_100)
                setPadding(CssEdgeLengths.px(4, 0, 0, 0))
            }
        }
        actionsRow.addChild(settingsSaveButton)
        actionsRow.addChild(settingsToggleBookmarksButton)
        actionsRow.addChild(settingsClearHistoryButton)
        actionsRow.addChild(settingsCloseButton)
        settingsStack.addChild(actionsRow)
        settingsPanel.addChild(settingsStack)
        addChild(settingsPanel, bookmarksInsertIdx + 1)

        // Default Search / New Tab surface fills the stage whenever 0 tabs are open.
        stageContainer.addFillLayer(defaultNewTabSurface, zIndex = 0)

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
            syncStageEmptyNewTabVisibility()
            syncOmniboxAndNavigationState()
            syncResponsiveBrowserChrome()
        }

        onTabOpened = {
            syncStageEmptyNewTabVisibility()
            syncMobileTabCounterBadges()
            styleDesktopTabStrip()
        }

        onTabClosed = {
            syncStageEmptyNewTabVisibility()
            syncOmniboxAndNavigationState()
            syncMobileTabCounterBadges()
            styleDesktopTabStrip()
        }

        onMobileOverviewStateChanged = {
            syncStageEmptyNewTabVisibility()
            syncMobileTabCounterBadges()
        }

        onChromeLayoutSynced = {
            syncResponsiveBrowserChrome()
        }

        defaultNewTabSurface.onSubmitQuery = { query ->
            navigateActiveTabTo(query)
        }
        defaultNewTabSurface.onNewTabClicked = {
            requestNewTab()
        }
        defaultNewTabSurface.onBookmarkSelected = { bookmark ->
            navigateActiveTabTo(bookmark.url)
        }

        navBackButton.setOnClick {
            navigateBackInActiveTab()
        }
        navForwardButton.setOnClick {
            navigateForwardInActiveTab()
        }
        reloadButton.setOnClick {
            if (activeTabPageFragment?.isPageLoading == true) {
                stopLoadingInActiveTab()
            } else {
                reloadActiveTab()
            }
        }
        homeButton.setOnClick {
            navigateHomeInActiveTab()
        }
        securityLockBadge.setOnClick {
            omniboxField.isFocused = true
        }
        omniboxField.onSubmit = { submitted ->
            navigateActiveTabTo(submitted)
        }
        mobileSecurityLockBadge.setOnClick {
            mobileOmniboxField.isFocused = true
        }
        mobileOmniboxField.onSubmit = { submitted ->
            navigateActiveTabTo(submitted)
        }

        browserMenuButton.setOnClick {
            openBrowserMenu(browserMenuButton)
        }
        mobileMenuButton.setOnClick {
            openBrowserMenu(mobileMenuButton)
        }

        settingsShowBookmarksCheckBox.onCheckedChanged = { checked ->
            if (_sessionState.showBookmarksBar != checked) {
                toggleBookmarksBar()
            }
        }
        settingsThemeSelect.onSelectionChanged = { selectedName ->
            val selectedMode = ThemeMode.entries.firstOrNull { it.name == selectedName } ?: ThemeMode.AUTO
            setBrowserThemeMode(selectedMode)
        }
        settingsSaveButton.setOnClick {
            applySettingsFromInputs()
        }
        settingsToggleBookmarksButton.setOnClick {
            toggleBookmarksBar()
        }
        settingsClearHistoryButton.setOnClick {
            clearHistory()
        }
        settingsCloseButton.setOnClick {
            settingsPanel.isVisible = false
        }

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

    private fun createNewTabSpec(url: String, title: String? = null): TabItemSpec {
        dynamicTabCounter += 1
        val tabId = "tab-$dynamicTabCounter"
        val normalized = BrowserTabPageFragment.normalizeOmniboxInput(url, _sessionState.searchEnginePrefix)
        val resolvedTitle = title ?: BrowserTabPageFragment.resolveTitleForUrl(normalized)
        val resolvedSubtitle = BrowserTabPageFragment.resolveSubtitleForUrl(normalized)
        return TabItemSpec(
            tabId = tabId,
            title = resolvedTitle,
            subtitle = resolvedSubtitle,
            iconName = null,
            iconUrl = null,
            tintIconToTheme = false,
            closable = true,
            pinned = false,
            fragmentFactory = {
                BrowserTabPageFragment(
                    initialUrl = normalized,
                    initialBookmarks = _bookmarks,
                    initialSearchPrefix = _sessionState.searchEnginePrefix
                ).also { pageFragment ->
                    wireTabPageFragmentCallbacks(tabId, pageFragment)
                }
            }
        )
    }

    private fun wireTabPageFragmentCallbacks(tabId: String, pageFragment: BrowserTabPageFragment) {
        if (!BrowserTabPageFragment.isNewTabUrl(pageFragment.currentUrl)) {
            recordGlobalHistoryEntry(pageFragment.currentUrl, pageFragment.currentTitle)
        }
        if (pageFragment.currentFaviconUrl != null) {
            updateTabIcon(tabId, iconUrl = pageFragment.currentFaviconUrl, tintIconToTheme = false)
        }

        pageFragment.onFaviconChanged = { faviconUrl ->
            updateTabIcon(tabId, iconUrl = faviconUrl, tintIconToTheme = false)
        }

        pageFragment.onPageNavigated = { newUrl, newTitle, _ ->
            updateTabTitle(tabId, newTitle, BrowserTabPageFragment.resolveSubtitleForUrl(newUrl))
            updateTabIcon(tabId, iconUrl = pageFragment.currentFaviconUrl, tintIconToTheme = false)
            if (!BrowserTabPageFragment.isNewTabUrl(newUrl)) {
                recordGlobalHistoryEntry(newUrl, newTitle)
            }
            if (activeTabId == tabId) {
                syncOmniboxAndNavigationState()
            } else {
                styleDesktopTabStrip()
            }
        }

        pageFragment.onLoadingStateUpdated = {
            if (activeTabId == tabId) {
                syncLoadingIndicator()
            }
        }

        pageFragment.onViewportResized = { _, _ ->
            if (resolvedChromeMode != lastResponsiveSyncedMode) {
                syncResponsiveBrowserChrome()
            }
        }
    }

    private fun recordGlobalHistoryEntry(url: String, title: String) {
        val lastIdx = _globalHistory.lastIndex
        if (lastIdx >= 0 && _globalHistory[lastIdx].url == url) {
            if (title.isNotBlank() && _globalHistory[lastIdx].title != title) {
                _globalHistory[lastIdx] = _globalHistory[lastIdx].copy(title = title)
            }
            return
        }
        historySequence += 1L
        _globalHistory.add(
            BrowserHistoryEntry(
                url = url,
                title = title,
                sequenceNumber = historySequence
            )
        )
    }

    /**
     * Rebuilds [browserOverflowMenu] with current browser state and opens it below [anchor].
     */
    fun openBrowserMenu(anchor: AbstractComponent = browserMenuButton) {
        buildBrowserOverflowMenu()
        val anchorY = anchor.bounds.height.takeIf { it > 0 } ?: 30
        val hasFactory = anchor.resolveActiveFactory() != null ||
            browserOverflowMenu.resolveActiveFactory() != null ||
            GenericUI.activeFactory != null
        if (hasFactory) {
            browserOverflowMenu.showAt(anchor, 0, anchorY)
        }
    }

    private fun buildBrowserOverflowMenu() {
        browserOverflowMenu.clearChildren()

        val activePage = activeTabPageFragment
        val isPageLoading = activePage?.isPageLoading == true
        val activeUrl = activePage?.currentUrl.orEmpty()
        val isNavigatedPage = activePage != null && !activePage.isShowingNewTabPage
        val isCurrentBookmarked = isNavigatedPage && _bookmarks.any { it.url == activeUrl }

        browserOverflowMenu.addItem(
            MenuItem(label = "New Tab", shortcutText = "Ctrl+T") {
                requestNewTab()
            }
        )
        browserOverflowMenu.addItem(
            MenuItem(
                label = if (isPageLoading) "Stop loading" else "Reload",
                shortcutText = "Ctrl+R",
                disabled = (activePage == null)
            ) {
                if (activeTabPageFragment?.isPageLoading == true) {
                    stopLoadingInActiveTab()
                } else {
                    reloadActiveTab()
                }
            }
        )
        browserOverflowMenu.addItem(
            MenuItem(
                label = "Close Tab",
                shortcutText = "Ctrl+W",
                disabled = (activeTabId == null)
            ) {
                activeTabId?.let { closeTab(it) }
            }
        )
        browserOverflowMenu.addSeparator()

        val bookmarksMenu = Menu("Bookmarks")
            .addItem(
                MenuItem(
                    label = if (isCurrentBookmarked) "Remove Bookmark" else "Bookmark This Page",
                    shortcutText = "Ctrl+D",
                    disabled = !isNavigatedPage
                ) {
                    toggleBookmarkForActiveTab()
                }
            )
            .addItem(
                MenuItem(
                    label = "${if (_sessionState.showBookmarksBar) "Hide" else "Show"} Bookmarks Bar",
                    shortcutText = "Ctrl+Shift+B"
                ) {
                    toggleBookmarksBar()
                }
            )
            .addSeparator()
        if (_bookmarks.isEmpty()) {
            bookmarksMenu.addItem(
                MenuItem(label = "No Bookmarks Saved", shortcutText = null, disabled = true)
            )
        } else {
            for (bm in _bookmarks) {
                val subtitle = BrowserTabPageFragment.resolveSubtitleForUrl(bm.url)
                val itemLabel = if (subtitle.isNotBlank()) "${bm.title} — $subtitle" else bm.title
                bookmarksMenu.addItem(
                    MenuItem(label = itemLabel) {
                        navigateActiveTabTo(bm.url)
                    }
                )
            }
        }
        bookmarksMenu
            .addSeparator()
            .addItem(
                MenuItem(
                    label = "Clear Bookmarks",
                    shortcutText = null,
                    disabled = _bookmarks.isEmpty()
                ) {
                    clearBookmarks()
                }
            )
        browserOverflowMenu.addSubmenu(bookmarksMenu)

        val historyMenu = Menu("History")
            .addItem(
                MenuItem(
                    label = "Back",
                    shortcutText = "Alt+Left",
                    disabled = activePage?.canGoBack != true
                ) {
                    navigateBackInActiveTab()
                }
            )
            .addItem(
                MenuItem(
                    label = "Forward",
                    shortcutText = "Alt+Right",
                    disabled = activePage?.canGoForward != true
                ) {
                    navigateForwardInActiveTab()
                }
            )
            .addSeparator()
        val recentHistory = _globalHistory.asReversed().take(10)
        if (recentHistory.isEmpty()) {
            historyMenu.addItem(
                MenuItem(label = "No Recent History", shortcutText = null, disabled = true)
            )
        } else {
            for (entry in recentHistory) {
                val subtitle = BrowserTabPageFragment.resolveSubtitleForUrl(entry.url)
                val itemLabel = if (subtitle.isNotBlank()) "${entry.title} — $subtitle" else entry.title
                historyMenu.addItem(
                    MenuItem(label = itemLabel) {
                        navigateActiveTabTo(entry.url)
                    }
                )
            }
        }
        historyMenu
            .addSeparator()
            .addItem(
                MenuItem(
                    label = "Clear History",
                    shortcutText = null,
                    disabled = _globalHistory.isEmpty()
                ) {
                    clearHistory()
                }
            )
        browserOverflowMenu.addSubmenu(historyMenu)

        val currentThemeMode = explicitThemeOverride
            ?: (application ?: Application.current)?.getThemeMode()
            ?: ThemeMode.AUTO
        val themeMenu = Menu("Theme")
            .addItem(
                RadioMenuItem(
                    label = "System Default",
                    checked = (currentThemeMode == ThemeMode.AUTO)
                ) {
                    setBrowserThemeMode(ThemeMode.AUTO)
                }
            )
            .addItem(
                RadioMenuItem(
                    label = "Light",
                    checked = (currentThemeMode == ThemeMode.LIGHT)
                ) {
                    setBrowserThemeMode(ThemeMode.LIGHT)
                }
            )
            .addItem(
                RadioMenuItem(
                    label = "Dark",
                    checked = (currentThemeMode == ThemeMode.DARK)
                ) {
                    setBrowserThemeMode(ThemeMode.DARK)
                }
            )
        browserOverflowMenu.addSubmenu(themeMenu)

        val viewMenu = Menu("View")
            .addItem(
                MenuItem("Desktop Browser Mode (1280×800)") {
                    chromeModeOverride = null
                    updateViewportSize(1280, 800)
                }
            )
            .addItem(
                MenuItem("Mobile Browser Mode (412×915)") {
                    chromeModeOverride = null
                    updateViewportSize(412, 915)
                }
            )
            .addItem(
                MenuItem("Mobile Tab Switcher Overview") {
                    chromeModeOverride = TabbedChromeMode.MOBILE_SWITCHER_PILL
                    toggleMobileTabOverview()
                }
            )
        browserOverflowMenu.addSubmenu(viewMenu)

        browserOverflowMenu.addSeparator()
        browserOverflowMenu.addItem(
            CheckMenuItem(
                label = "Settings",
                checked = settingsPanel.isVisible
            ) { _ ->
                toggleSettingsPanel()
            }
        )
        browserOverflowMenu.addItem(
            MenuItem("Exit") {
                (application ?: Application.current)?.exit(0)
            }
        )
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
     * Navigates the active tab to [urlOrQuery], or opens a new active tab if starting from 0 open pages.
     */
    fun navigateActiveTabTo(urlOrQuery: String) {
        val trimmed = urlOrQuery.trim()
        if (trimmed.isEmpty()) return
        val normalized = BrowserTabPageFragment.normalizeOmniboxInput(
            rawInput = trimmed,
            searchPrefix = _sessionState.searchEnginePrefix
        )
        val page = activeTabPageFragment
        if (page == null) {
            openBrowserTab(url = normalized, selectImmediately = true)
        } else {
            page.navigateTo(
                targetUrl = normalized,
                recordHistory = true,
                searchPrefix = _sessionState.searchEnginePrefix
            )
        }
        syncStageEmptyNewTabVisibility()
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
     * Reloads the active tab if one is open.
     */
    fun reloadActiveTab() {
        activeTabPageFragment?.reload()
        syncOmniboxAndNavigationState()
    }

    /**
     * Stops any active page load in the active tab.
     */
    fun stopLoadingInActiveTab() {
        activeTabPageFragment?.stopLoading()
        syncOmniboxAndNavigationState()
    }

    /**
     * Navigates the active tab to [BrowserSessionState.homeUrl], or opens a tab when [homeUrl] is a web page.
     */
    fun navigateHomeInActiveTab() {
        val home = _sessionState.homeUrl.trim().ifEmpty { BrowserTabPageFragment.URL_NEW_TAB }
        val page = activeTabPageFragment
        if (page == null) {
            if (!BrowserTabPageFragment.isNewTabUrl(home)) {
                openBrowserTab(url = home, selectImmediately = true)
            } else {
                syncOmniboxAndNavigationState()
            }
            return
        }
        page.navigateTo(home, recordHistory = true, searchPrefix = _sessionState.searchEnginePrefix)
        syncOmniboxAndNavigationState()
    }

    /**
     * Toggles visibility of the desktop bookmarks bar and persists the preference.
     */
    fun toggleBookmarksBar(): Boolean {
        val nextShow = !_sessionState.showBookmarksBar
        _sessionState = _sessionState.copy(showBookmarksBar = nextShow)
        settingsShowBookmarksCheckBox.isChecked = nextShow
        persistSessionState()
        syncResponsiveBrowserChrome()
        return nextShow
    }

    /**
     * Toggles between light and dark Chromium browser themes.
     */
    fun toggleThemeMode(): ThemeMode {
        val next = if (BrowserChromiumTheme.isDarkTheme(explicitThemeOverride)) {
            ThemeMode.LIGHT
        } else {
            ThemeMode.DARK
        }
        setBrowserThemeMode(next)
        return next
    }

    /**
     * Applies [mode] to the host application (when available) and updates Chromium chrome visuals.
     */
    fun setBrowserThemeMode(mode: ThemeMode) {
        explicitThemeOverride = if (mode == ThemeMode.AUTO) null else mode
        (application ?: Application.current)?.setThemeMode(mode)
        settingsThemeSelect.selectedValue = mode.name
        syncBrowserThemeVisuals()
    }

    /**
     * Toggles visibility of the collapsible browser settings panel.
     */
    fun toggleSettingsPanel(): Boolean {
        val nextVisible = !settingsPanel.isVisible
        if (nextVisible) {
            settingsHomeUrlField.value = _sessionState.homeUrl
            settingsSearchPrefixField.value = _sessionState.searchEnginePrefix
            settingsShowBookmarksCheckBox.isChecked = _sessionState.showBookmarksBar
            val currentThemeMode = explicitThemeOverride
                ?: (application ?: Application.current)?.getThemeMode()
                ?: ThemeMode.AUTO
            settingsThemeSelect.selectedValue = currentThemeMode.name
        }
        settingsPanel.isVisible = nextVisible
        return nextVisible
    }

    /**
     * Updates [sessionState] home URL and search engine prefix, persisting via [PreferenceStore].
     */
    fun updateSessionPreferences(
        homeUrl: String = _sessionState.homeUrl,
        searchEnginePrefix: String = _sessionState.searchEnginePrefix
    ) {
        val cleanHome = homeUrl.trim().ifEmpty { BrowserSessionState.DEFAULT_HOME_URL }
        val cleanSearch = searchEnginePrefix.trim().ifEmpty { BrowserSessionState.DEFAULT_SEARCH_PREFIX }
        _sessionState = _sessionState.copy(
            homeUrl = cleanHome,
            searchEnginePrefix = cleanSearch
        )
        settingsHomeUrlField.value = cleanHome
        settingsSearchPrefixField.value = cleanSearch
        persistSessionState()
        for (spec in tabs) {
            val frag = getTabFragment(spec.tabId) as? BrowserTabPageFragment
            frag?.updateSearchEnginePrefix(cleanSearch)
        }
    }

    private fun applySettingsFromInputs() {
        updateSessionPreferences(
            homeUrl = settingsHomeUrlField.value,
            searchEnginePrefix = settingsSearchPrefixField.value
        )
        settingsPanel.isVisible = false
    }

    /**
     * Clears the in-session navigation history across all tabs.
     */
    fun clearHistory() {
        _globalHistory.clear()
    }

    /**
     * Adds or removes a bookmark for the currently active navigated tab's URL.
     *
     * @return `true` if the active URL is now bookmarked, or `false` if removed or unavailable.
     */
    fun toggleBookmarkForActiveTab(): Boolean {
        val page = activeTabPageFragment ?: return false
        val targetUrl = page.currentUrl
        if (BrowserTabPageFragment.isNewTabUrl(targetUrl)) return false

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
                    iconLabel = page.currentIcon?.takeIf { it.isNotBlank() } ?: "★"
                )
            )
            true
        }
        persistBookmarks()
        rebuildBookmarksBar()
        syncAllTabSpeedDials()
        syncOmniboxAndNavigationState()
        return isNowBookmarked
    }

    /**
     * Removes all saved bookmarks and updates the bookmarks bar and New Tab surfaces.
     */
    fun clearBookmarks() {
        _bookmarks.clear()
        persistBookmarks()
        rebuildBookmarksBar()
        syncAllTabSpeedDials()
        syncOmniboxAndNavigationState()
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
        persistBookmarks()
        rebuildBookmarksBar()
        syncAllTabSpeedDials()
        syncOmniboxAndNavigationState()
        return _bookmarks.toList()
    }

    private fun rebuildBookmarksBar() {
        bookmarksBarRow.clearChildren()
        val palette = BrowserChromiumTheme.resolvePalette(explicitThemeOverride)
        bookmarksBarRow.addChild(Badge("Bookmarks", type = BootstrapType.SECONDARY, pill = true))
        if (_bookmarks.isEmpty()) {
            bookmarksBarRow.addChild(
                Label("No bookmarks saved").apply {
                    addClass("browser-bookmarks-empty")
                    getStyle().apply {
                        setColor(palette.textSecondary)
                        setFontSizePx(12f)
                    }
                }
            )
            return
        }
        for (bookmark in _bookmarks) {
            val btn = StyledButton(
                text = "${bookmark.iconLabel} ${bookmark.title}",
                type = null,
                size = BootstrapSize.SM
            ).apply {
                addClass("bookmark-bar-item")
                BrowserChromiumTheme.styleTonalPillButton(
                    button = this,
                    palette = palette,
                    heightPx = 26,
                    horizontalPaddingPx = 10,
                    fontSizePx = 12f
                )
                setOnClick {
                    navigateActiveTabTo(bookmark.url)
                }
            }
            bookmarksBarRow.addChild(btn)
        }
    }

    private fun styleDesktopTabStrip() {
        if (resolvedChromeStyle == TabbedChromeStyle.CHROMIUM) return
        for (child in desktopTabStripContainer.children) {
            if (child is FlexBox && child.hasClass("genericui-tabbed-chip")) {
                val isActive = child.hasClass("active")
                var selectW = 140
                var closeW = 0
                for (inner in child.children) {
                    if (inner is com.nexustools.genericui.component.action.Button &&
                        inner.hasClass("genericui-tabbed-select-btn")
                    ) {
                        inner.type = if (isActive) BootstrapType.PRIMARY else BootstrapType.DARK
                        selectW = (inner.text.length * 6 + 22).coerceIn(104, 232)
                        inner.widthPx = selectW
                    } else if (inner is com.nexustools.genericui.component.action.Button &&
                        inner.hasClass("genericui-tabbed-close-btn")
                    ) {
                        inner.type = if (isActive) BootstrapType.PRIMARY else BootstrapType.DARK
                        inner.outline = false
                        closeW = 26
                        inner.widthPx = closeW
                    }
                }
                child.widthPx = selectW + (if (closeW > 0) closeW + 6 else 0)
            } else if (child is com.nexustools.genericui.component.action.Button &&
                child.hasClass("genericui-tabbed-new-tab-btn")
            ) {
                child.type = BootstrapType.SECONDARY
                child.widthPx = 34
            }
        }
    }

    private fun syncBrowserThemeVisuals() {
        val palette = BrowserChromiumTheme.resolvePalette(explicitThemeOverride)
        if (palette.isDark) {
            removeClass("browser-theme-light")
            addClass("browser-theme-dark")
        } else {
            removeClass("browser-theme-dark")
            addClass("browser-theme-light")
        }

        BrowserChromiumTheme.stylePillTextField(
            field = omniboxField,
            palette = palette,
            minWidthPx = 220,
            heightPx = 34,
            fontSizePx = 13.5f,
            horizontalPaddingPx = 16,
            flexGrow = 1f
        )
        BrowserChromiumTheme.stylePillTextField(
            field = mobileOmniboxField,
            palette = palette,
            minWidthPx = 120,
            heightPx = 34,
            fontSizePx = 13.5f,
            horizontalPaddingPx = 14,
            flexGrow = 1f
        )

        val circleButtons = listOf(
            navBackButton,
            navForwardButton,
            reloadButton,
            homeButton,
            browserMenuButton,
            mobileHomeButton,
            mobileMenuButton,
            androidRecentsButton,
            androidHomeCircleButton,
            androidBackTriangleButton
        )
        for (btn in circleButtons) {
            BrowserChromiumTheme.styleCircleIconButton(btn, palette, sizePx = 30, fontSizePx = 14f)
        }

        val page = activeTabPageFragment
        val rawUrl = page?.currentUrl ?: ""
        val isHttps = page != null &&
            BrowserTabPageFragment.resolveSchemeBadgeForUrl(rawUrl) == "HTTPS"
        BrowserChromiumTheme.styleSecurityBadgePill(
            button = securityLockBadge,
            palette = palette,
            isSecure = isHttps
        )
        BrowserChromiumTheme.styleSecurityBadgePill(
            button = mobileSecurityLockBadge,
            palette = palette,
            isSecure = isHttps
        )

        BrowserChromiumTheme.styleTonalPillButton(
            button = mobileTabCounterBadgeButton,
            palette = palette,
            heightPx = 30,
            horizontalPaddingPx = 10,
            fontSizePx = 12f
        )

        BrowserChromiumTheme.stylePillTextField(
            field = settingsHomeUrlField,
            palette = palette,
            minWidthPx = 220,
            heightPx = 36,
            fontSizePx = 13.5f,
            horizontalPaddingPx = 12,
            flexGrow = 1f,
            radiusPx = 10f
        )
        BrowserChromiumTheme.stylePillTextField(
            field = settingsSearchPrefixField,
            palette = palette,
            minWidthPx = 220,
            heightPx = 36,
            fontSizePx = 13.5f,
            horizontalPaddingPx = 12,
            flexGrow = 1f,
            radiusPx = 10f
        )
        BrowserChromiumTheme.styleAccentPillButton(
            button = settingsSaveButton,
            palette = palette,
            heightPx = 34,
            horizontalPaddingPx = 16,
            fontSizePx = 13f
        )
        BrowserChromiumTheme.styleTonalPillButton(
            button = settingsToggleBookmarksButton,
            palette = palette,
            heightPx = 34,
            horizontalPaddingPx = 14,
            fontSizePx = 12.5f
        )
        BrowserChromiumTheme.styleTonalPillButton(
            button = settingsCloseButton,
            palette = palette,
            heightPx = 34,
            horizontalPaddingPx = 14,
            fontSizePx = 12.5f
        )

        bookmarksBarRow.getStyle().apply {
            setBackgroundColor(palette.toolbarSurface)
            setBorderWidth(CssEdgeLengths.px(0, 0, 1, 0))
            setBorderStyle(CssBorderStyle.SOLID)
            setBorderBottomColor(palette.toolbarBorder)
        }
        rebuildBookmarksBar()

        defaultNewTabSurface.applyThemeVisuals(explicitThemeOverride)
        for (spec in tabs) {
            val tabFrag = getTabFragment(spec.tabId) as? BrowserTabPageFragment
            tabFrag?.newTabSurface?.applyThemeVisuals(explicitThemeOverride)
        }
    }

    private fun syncAllTabSpeedDials() {
        defaultNewTabSurface.setBookmarks(_bookmarks)
        for (spec in tabs) {
            val frag = getTabFragment(spec.tabId) as? BrowserTabPageFragment
            frag?.setSpeedDialBookmarks(_bookmarks)
        }
    }

    private fun syncStageEmptyNewTabVisibility() {
        defaultNewTabSurface.isVisible = (tabCount == 0 && !isMobileTabOverviewOpen)
    }

    private fun syncMobileTabCounterBadges() {
        val countText = tabCount.toString()
        mobileTabCounterBadgeButton.text = countText
    }

    private fun syncLoadingIndicator() {
        val page = activeTabPageFragment
        val isLoading = page?.isPageLoading == true
        reloadButton.text = if (isLoading) "×" else "↻"
        reloadButton.tooltipText = if (isLoading) "Stop loading" else "Reload"
    }

    /**
     * Synchronizes Desktop vs. Mobile toolbar, bookmarks bar, and bottom navigation bar visibility
     * with [resolvedChromeMode].
     */
    fun syncResponsiveBrowserChrome() {
        val mode = resolvedChromeMode
        val isDesktop = (mode == TabbedChromeMode.DESKTOP_BROWSER_TABS)
        val isMobile = (mode == TabbedChromeMode.MOBILE_SWITCHER_PILL)
        val showBookmarks = isDesktop && _sessionState.showBookmarksBar
        val expectEmptyVisible = (tabCount == 0 && !isMobileTabOverviewOpen)

        if (lastResponsiveSyncedMode == mode &&
            lastBookmarksBarVisible == showBookmarks &&
            lastStageEmptyVisible == expectEmptyVisible &&
            desktopToolbarRow.isVisible == isDesktop &&
            mobileToolbarRow.isVisible == isMobile
        ) {
            return
        }

        lastResponsiveSyncedMode = mode
        lastBookmarksBarVisible = showBookmarks
        lastStageEmptyVisible = expectEmptyVisible

        desktopToolbarRow.isVisible = isDesktop
        bookmarksBarRow.isVisible = showBookmarks
        mobileToolbarRow.isVisible = isMobile
        mobileBottomNavBar.isVisible = isMobile
        syncStageEmptyNewTabVisibility()
        styleDesktopTabStrip()
        syncMobileTabCounterBadges()
    }

    private fun syncOmniboxAndNavigationState() {
        val page = activeTabPageFragment
        val rawUrl = page?.currentUrl ?: ""
        val isNewTab = page == null || BrowserTabPageFragment.isNewTabUrl(rawUrl)
        val omniboxText = if (isNewTab) "" else rawUrl

        omniboxField.value = omniboxText
        mobileOmniboxField.value = if (isNewTab) {
            ""
        } else {
            BrowserTabPageFragment.resolveSubtitleForUrl(rawUrl).ifBlank { rawUrl }
        }

        val schemeBadgeCode = if (page == null) {
            "New Tab"
        } else {
            BrowserTabPageFragment.resolveSchemeBadgeForUrl(rawUrl)
        }
        val isHttps = schemeBadgeCode == "HTTPS"
        val securityGlyph = if (isHttps) "🔒" else "ⓘ"
        val securityTooltip = when (schemeBadgeCode) {
            "HTTPS" -> "Connection is secure (HTTPS)"
            "HTTP" -> "Connection is not secure (HTTP)"
            "FILE" -> "Viewing local file (FILE)"
            "ABOUT" -> "Browser internal page (ABOUT)"
            "DATA" -> "Data document (DATA)"
            "New Tab" -> "New Tab"
            else -> "Web page ($schemeBadgeCode)"
        }
        securityLockBadge.text = securityGlyph
        securityLockBadge.tooltipText = securityTooltip
        mobileSecurityLockBadge.text = securityGlyph
        mobileSecurityLockBadge.tooltipText = securityTooltip

        val palette = BrowserChromiumTheme.resolvePalette(explicitThemeOverride)
        BrowserChromiumTheme.styleSecurityBadgePill(
            button = securityLockBadge,
            palette = palette,
            isSecure = isHttps
        )
        BrowserChromiumTheme.styleSecurityBadgePill(
            button = mobileSecurityLockBadge,
            palette = palette,
            isSecure = isHttps
        )

        navBackButton.isEnabled = page?.canGoBack == true
        navForwardButton.isEnabled = page?.canGoForward == true
        reloadButton.isEnabled = page != null && !isNewTab

        syncLoadingIndicator()
        styleDesktopTabStrip()
        syncMobileTabCounterBadges()
    }

    override fun onResize(viewportWidth: Int, viewportHeight: Int) {
        val previousMode = resolvedChromeMode
        super.onResize(viewportWidth, viewportHeight)
        if (resolvedChromeMode != previousMode || lastResponsiveSyncedMode != resolvedChromeMode) {
            syncResponsiveBrowserChrome()
        }
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
}
