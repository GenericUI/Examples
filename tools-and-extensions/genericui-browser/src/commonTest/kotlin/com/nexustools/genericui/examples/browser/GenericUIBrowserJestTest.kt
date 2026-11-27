package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.action.styled.StyledButton
import com.nexustools.genericui.component.form.styled.StyledTextField
import com.nexustools.genericui.component.fragment.helper.TabbedChromeMode
import com.nexustools.genericui.component.fragment.helper.TabbedChromeStyle
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.styled.StyledContainer
import com.nexustools.genericui.component.media.Image
import com.nexustools.genericui.component.shell.CheckMenuItem
import com.nexustools.genericui.component.shell.ContextMenu
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.shell.RadioMenuItem
import com.nexustools.genericui.component.shell.Window
import com.nexustools.genericui.component.surface.styled.StyledComponent
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.ILabelTarget
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.browser.res.Resource
import com.nexustools.genericui.geometry.Size
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer
import com.nexustools.genericui.style.color.ImageTintMode
import com.nexustools.genericui.style.css.CssCornerRadii
import com.nexustools.genericui.style.css.CssLength
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.RoboComponentTreeFinder
import com.nexustools.genericui.window.TitleBar
import com.nexustools.genericui.window.TitleCapabilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GenericUIBrowserJestTest {

    private fun collectMenuText(menuBar: MenuBar?): List<String> {
        if (menuBar == null) return emptyList()
        val out = mutableListOf<String>()
        fun walkMenu(menu: Menu) {
            out.add(menu.getLabel())
            for (child in menu.children) {
                when (child) {
                    is MenuItem -> out.add(child.getLabel())
                    is Menu -> walkMenu(child)
                }
            }
        }
        for (child in menuBar.children) {
            if (child is Menu) walkMenu(child)
        }
        return out
    }

    private fun collectContextMenuLabels(menu: ContextMenu): List<String> {
        val out = mutableListOf<String>()
        fun walkMenu(sub: Menu) {
            out.add(sub.getLabel())
            for (child in sub.children) {
                when (child) {
                    is MenuItem -> out.add(child.getLabel())
                    is Menu -> walkMenu(child)
                }
            }
        }
        for (child in menu.children) {
            when (child) {
                is MenuItem -> out.add(child.getLabel())
                is Menu -> walkMenu(child)
            }
        }
        return out
    }

    private fun findSubmenu(menu: ContextMenu, label: String): Menu =
        menu.children.filterIsInstance<Menu>().first { it.getLabel() == label }

    private fun findMenuItem(menu: ContextMenu, label: String): MenuItem =
        menu.children.filterIsInstance<MenuItem>().first { it.getLabel() == label }

    private fun findSubmenuItem(submenu: Menu, label: String): MenuItem =
        submenu.children.filterIsInstance<MenuItem>().first { it.getLabel() == label }

    private fun assertNoForbiddenScreenshotContent(texts: Iterable<String>, context: String) {
        val forbiddenTerms = listOf(
            "T-Crypt",
            "berth",
            "Google One",
            "AI credits",
            "BrowserCreditActivityRow",
            "Google Keep",
            "Overcoming Overwhelming Anxiety",
            "Ask Gemini",
            "one.google.com",
            "keep.google.com",
            "gemini.google.com"
        )
        for (text in texts) {
            for (term in forbiddenTerms) {
                assertFalse(
                    text.contains(term, ignoreCase = true),
                    "Unexpected hardcoded screenshot content '$term' found in $context: '$text'"
                )
            }
        }
    }

    @Test
    fun runGenericUIBrowserJestElectricSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "GenericUIBrowser Clean Empty-Start & WebView Navigation Suite",
            visualOnWindows = false
        ) {
            describe("Empty-Start Default Search / New Tab Page & Responsive Browser Frame") {
                test("starts with 0 open pages, shows Default Search / New Tab page, switches Desktop/Mobile postures, and contains zero hardcoded screenshot pages") {
                    val store = PreferenceStore.open("GenericUIBrowser_EmptyStart_Test")
                    store.clear()
                    val controller = launchApp(visualOnWindows = false) { GenericUIBrowserApp(store) }.setup()
                    controller.takeScreenshot("genericui-browser.jpg")
                    val frag = controller.app.browserFragment

                    expect(controller.app.getThemeMode()).toBe(ThemeMode.AUTO)
                    assertTrue(controller.app.hasStyleSheet(Resource.style.styles))
                    assertTrue(frag.hasClass("genericui-browser-root"))
                    expect(frag.config.chromeStyle).toBe(TabbedChromeStyle.CHROMIUM)
                    assertTrue(frag.config.showNewTabButton)
                    assertTrue(frag.config.showTabSearchButton)
                    assertTrue(frag.config.connectActiveTabToNavbar)
                    assertSame(frag.navToolbar, frag.currentTabNavbar)
                    assertTrue(frag.tabNavbarSlotContainer.isVisible)
                    assertNull(frag.headerSlotContainer.heightPx)

                    // Window TitleBar hides top-left icon and omits MenuBar in favor of the unified ⋮ menu
                    val appTitleBar = assertNotNull(controller.app.mainWindow.getTitleBar())
                    assertFalse(appTitleBar.showWindowIcon)
                    assertFalse(appTitleBar.embedMenuBar)
                    assertFalse(appTitleBar.showWindowTitle)
                    assertNull(appTitleBar.iconPath)
                    assertNull(appTitleBar.heightPx)
                    assertFalse(frag.config.showTitleBarWindowIcon)
                    assertNull(controller.app.mainWindow.getMenuBar())
                    assertNull(frag.getMenuBar())

                    // Starts cleanly with 0 open pages and empty bookmarks/history
                    expect(frag.tabCount).toBe(0)
                    assertTrue(frag.tabs.isEmpty())
                    assertNull(frag.activeTabId)
                    assertNull(frag.activeTabPageFragment)
                    assertTrue(frag.bookmarks.isEmpty())
                    assertTrue(frag.globalHistory.isEmpty())
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("0")
                    expect(frag.mobileTabCounterBadgeButton.text).toBe("0")

                    // Default Search / New Tab page is visible and ready
                    assertTrue(frag.defaultNewTabSurface.isVisible)
                    expect(frag.defaultNewTabSurface.brandHeading.text).toBe("GenericUI Browser")
                    assertTrue(frag.defaultNewTabSurface.searchField.isVisible)
                    assertTrue(frag.defaultNewTabSurface.searchButton.isVisible)
                    assertTrue(frag.defaultNewTabSurface.newTabActionButton.isVisible)
                    expect(frag.omniboxField.value).toBe("")
                    expect(frag.securityLockBadge.text).toBe("ⓘ")
                    expect(frag.securityLockBadge.tooltipText).toBe("New Tab")
                    assertFalse(frag.navBackButton.isEnabled)
                    assertFalse(frag.navForwardButton.isEnabled)
                    assertFalse(frag.reloadButton.isEnabled)
                    expect(frag.browserMenuButton.text).toBe("⋮")
                    expect(frag.mobileMenuButton.text).toBe("⋮")

                    // Desktop toolbar stays clean: only navLeadingGroup -> omniboxGroup -> navTrailingGroup(⋮)
                    assertEquals(
                        listOf(frag.navLeadingGroup, frag.omniboxGroup, frag.navTrailingGroup),
                        frag.desktopToolbarRow.children
                    )
                    assertEquals(listOf(frag.securityLockBadge, frag.omniboxField), frag.omniboxGroup.children)
                    assertEquals(listOf(frag.browserMenuButton), frag.navTrailingGroup.children)
                    val forbiddenToolbarLabels = setOf("Go", "Ready", "☆", "★", "Bookmarks", "☾", "☀", "⚙")
                    for (child in frag.desktopToolbarRow.children) {
                        val text = RoboComponentTreeFinder.extractText(child)
                        assertFalse(text in forbiddenToolbarLabels, "Unexpected clutter control '$text' in desktop toolbar")
                    }
                    frag.openBrowserMenu()
                    val overflowLabels = collectContextMenuLabels(frag.browserOverflowMenu)
                    frag.browserOverflowMenu.hide()

                    // Assert zero hardcoded Chrome-screenshot pages anywhere in rendered tree, menus, tabs, bookmarks, or history
                    val renderedTexts = controller.findAllComponents()
                        .map { RoboComponentTreeFinder.extractText(it) }
                        .filter { it.isNotEmpty() }
                    val menuTexts = collectMenuText(controller.app.mainWindow.getMenuBar()) +
                        collectMenuText(frag.getMenuBar()) +
                        overflowLabels
                    assertNoForbiddenScreenshotContent(
                        renderedTexts + menuTexts + listOf(
                            frag.sessionState.homeUrl,
                            frag.sessionState.searchEnginePrefix
                        ),
                        context = "initial browser tree and menus"
                    )

                    // 1. Desktop mode: 1280x800 -> DESKTOP_BROWSER_TABS
                    frag.updateViewportSize(1280, 800)
                    expect(frag.resolvedChromeMode).toBe(TabbedChromeMode.DESKTOP_BROWSER_TABS)
                    assertTrue(frag.desktopTabStripContainer.isVisible)
                    assertTrue(frag.desktopToolbarRow.isVisible)
                    assertFalse(frag.mobileHeaderBarContainer.isVisible)
                    assertFalse(frag.mobileToolbarRow.isVisible)
                    assertTrue(frag.defaultNewTabSurface.isVisible)

                    // 2. Mobile mode: 412x915 -> MOBILE_SWITCHER_PILL with "0" badge
                    frag.updateViewportSize(412, 915)
                    expect(frag.resolvedChromeMode).toBe(TabbedChromeMode.MOBILE_SWITCHER_PILL)
                    assertFalse(frag.desktopTabStripContainer.isVisible)
                    assertTrue(frag.mobileHeaderBarContainer.isVisible)
                    assertTrue(frag.mobileToolbarRow.isVisible)
                    assertTrue(frag.mobileBottomNavBar.isVisible)
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("0")
                    expect(frag.mobileTabCounterBadgeButton.text).toBe("0")
                    assertTrue(frag.defaultNewTabSurface.isVisible)

                    // 3. Toggle Mobile Tab Switcher overview cleanly even at 0 tabs
                    assertFalse(frag.isMobileTabOverviewOpen)
                    frag.openMobileTabOverview()
                    assertTrue(frag.isMobileTabOverviewOpen)
                    assertTrue(frag.mobileTabOverviewContainer.isVisible)
                    assertFalse(frag.defaultNewTabSurface.isVisible)
                    expect(frag.mobileTabOverviewContainer.children.size).toBe(0)

                    frag.closeMobileTabOverview()
                    assertFalse(frag.isMobileTabOverviewOpen)
                    assertFalse(frag.mobileTabOverviewContainer.isVisible)
                    assertTrue(frag.defaultNewTabSurface.isVisible)

                    controller.destroy()
                    store.clear()
                }
            }

            describe("Omnibox & New Tab URL/Search Normalization, WebView Loading, History & Tab Close") {
                test("opens a tab from 0 open pages on URL or search entry, wires WebView & history, and returns to 0 pages when all tabs close") {
                    val store = PreferenceStore.open("GenericUIBrowser_Navigation_Test")
                    store.clear()
                    val controller = launchFragment { GenericUIBrowserFragment(store) }.setup()
                    val frag = controller.fragment

                    // Verify clean empty start
                    expect(frag.tabCount).toBe(0)
                    assertTrue(frag.defaultNewTabSurface.isVisible)

                    // Enter domain URL via the Default New Tab search box -> opens tab 1 normalized to https://
                    frag.defaultNewTabSurface.searchField.value = "example.org/docs"
                    frag.defaultNewTabSurface.searchButton.onClick?.invoke()

                    expect(frag.tabCount).toBe(1)
                    assertFalse(frag.defaultNewTabSurface.isVisible)
                    val firstTabId = frag.activeTabId
                    assertNotNull(firstTabId)
                    val firstPage = frag.activeTabPageFragment
                    assertNotNull(firstPage)
                    expect(firstPage.currentUrl).toBe("https://example.org/docs")
                    expect(firstPage.webView.url).toBe("https://example.org/docs")
                    assertTrue(firstPage.webView.isVisible)
                    assertFalse(firstPage.newTabSurface.isVisible)
                    expect(frag.omniboxField.value).toBe("https://example.org/docs")
                    expect(frag.securityLockBadge.text).toBe("🔒")
                    expect(frag.securityLockBadge.tooltipText).toBe("Connection is secure (HTTPS)")
                    assertTrue(frag.reloadButton.isEnabled)
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("1")

                    // Simulate WebView page load completion callback and verify loading state finishes
                    firstPage.webView.onPageFinished?.invoke("https://example.org/docs")
                    assertFalse(firstPage.isPageLoading)
                    expect(frag.reloadButton.text).toBe("↻")

                    // Navigate active tab via top omnibox with plain search query
                    frag.omniboxField.value = "kotlin multiplatform browser"
                    frag.omniboxField.onSubmit?.invoke(frag.omniboxField.value)

                    val expectedSearchUrl = "${BrowserSessionState.DEFAULT_SEARCH_PREFIX}kotlin+multiplatform+browser"
                    expect(firstPage.currentUrl).toBe(expectedSearchUrl)
                    expect(firstPage.webView.url).toBe(expectedSearchUrl)
                    assertTrue(firstPage.canGoBack)
                    assertTrue(frag.navBackButton.isEnabled)

                    // Verify file:// and about: URL normalization
                    frag.navigateActiveTabTo("file:///tmp/demo.html")
                    expect(firstPage.currentUrl).toBe("file:///tmp/demo.html")
                    expect(firstPage.webView.url).toBe("file:///tmp/demo.html")
                    expect(frag.securityLockBadge.text).toBe("ⓘ")
                    expect(frag.securityLockBadge.tooltipText).toBe("Viewing local file (FILE)")

                    frag.navigateActiveTabTo("about:blank")
                    expect(firstPage.currentUrl).toBe("about:blank")
                    expect(firstPage.webView.url).toBe("about:blank")
                    expect(frag.securityLockBadge.text).toBe("ⓘ")
                    expect(frag.securityLockBadge.tooltipText).toBe("Browser internal page (ABOUT)")

                    // Traverse Back and Forward history via toolbar buttons
                    frag.navBackButton.onClick?.invoke()
                    expect(firstPage.currentUrl).toBe("file:///tmp/demo.html")
                    assertTrue(firstPage.canGoForward)
                    assertTrue(frag.navForwardButton.isEnabled)

                    frag.navBackButton.onClick?.invoke()
                    expect(firstPage.currentUrl).toBe(expectedSearchUrl)

                    frag.navForwardButton.onClick?.invoke()
                    expect(firstPage.currentUrl).toBe("file:///tmp/demo.html")

                    // Active navigation above still has loading state pending -> menu shows "Stop loading" & reloadButton stops first
                    assertTrue(firstPage.isPageLoading)
                    frag.openBrowserMenu()
                    assertNotNull(findMenuItem(frag.browserOverflowMenu, "Stop loading"))
                    frag.browserOverflowMenu.hide()
                    expect(frag.reloadButton.text).toBe("×")
                    val prevReloads = firstPage.reloadCount
                    frag.reloadButton.onClick?.invoke()
                    assertFalse(firstPage.isPageLoading)
                    expect(frag.reloadButton.text).toBe("↻")
                    expect(firstPage.reloadCount).toBe(prevReloads)

                    // Reload active tab now that loading is idle, then simulate completion
                    frag.reloadButton.onClick?.invoke()
                    expect(firstPage.reloadCount).toBe(prevReloads + 1)
                    assertTrue(firstPage.isPageLoading)
                    firstPage.webView.onPageFinished?.invoke("file:///tmp/demo.html")
                    assertFalse(firstPage.isPageLoading)
                    expect(frag.reloadButton.text).toBe("↻")

                    // Open a second tab via '+ New Tab' and submit a URL in its New Tab search field
                    val secondSpec = frag.requestNewTab()
                    assertNotNull(secondSpec)
                    expect(frag.tabCount).toBe(2)
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("2")
                    val secondPage = frag.activeTabPageFragment
                    assertNotNull(secondPage)
                    expect(secondPage.currentUrl).toBe(BrowserTabPageFragment.URL_NEW_TAB)
                    assertTrue(secondPage.newTabSurface.isVisible)
                    assertFalse(secondPage.webView.isVisible)
                    expect(frag.omniboxField.value).toBe("")

                    secondPage.newTabSurface.searchField.onSubmit?.invoke("https://genericui.dev")
                    expect(secondPage.currentUrl).toBe("https://genericui.dev")
                    expect(secondPage.webView.url).toBe("https://genericui.dev")
                    assertTrue(secondPage.webView.isVisible)
                    assertFalse(secondPage.newTabSurface.isVisible)

                    // Close second tab -> returns to first tab
                    assertTrue(frag.closeTab(secondSpec.tabId))
                    expect(frag.tabCount).toBe(1)
                    expect(frag.activeTabId).toBe(firstTabId)

                    // Close last remaining tab -> returns cleanly to 0 open pages & Default Search / New Tab page
                    assertTrue(frag.closeTab(firstTabId))
                    expect(frag.tabCount).toBe(0)
                    assertTrue(frag.tabs.isEmpty())
                    assertNull(frag.activeTabId)
                    assertNull(frag.activeTabPageFragment)
                    assertTrue(frag.defaultNewTabSurface.isVisible)
                    expect(frag.omniboxField.value).toBe("")
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("0")
                    expect(frag.mobileTabCounterBadgeButton.text).toBe("0")
                    assertFalse(frag.navBackButton.isEnabled)
                    assertFalse(frag.navForwardButton.isEnabled)
                    assertFalse(frag.reloadButton.isEnabled)
                    expect(frag.securityLockBadge.text).toBe("ⓘ")

                    controller.destroy()
                    store.clear()
                }
            }

            describe("Bookmarks Binary Buffer Export/Import & PreferenceStore Persistence") {
                test("persists settings and user bookmarks cleanly while starting at 0 open pages") {
                    val store = PreferenceStore.open("GenericUIBrowser_Persistence_Test")
                    store.clear()

                    val firstController = launchFragment { GenericUIBrowserFragment(store) }.setup()
                    val firstFrag = firstController.fragment

                    // Navigate to a real URL and bookmark it
                    firstFrag.navigateActiveTabTo("https://example.com/guide")
                    assertTrue(firstFrag.toggleBookmarkForActiveTab())
                    expect(firstFrag.bookmarks.size).toBe(1)
                    expect(firstFrag.bookmarks[0].url).toBe("https://example.com/guide")

                    // Update session settings
                    firstFrag.updateSessionPreferences(
                        homeUrl = "https://example.com/home",
                        searchEnginePrefix = "https://search.example.org/?q="
                    )
                    assertFalse(firstFrag.toggleBookmarksBar())

                    // Export bookmarks to WritableByteBuffer
                    val writable = WritableByteBuffer()
                    firstFrag.exportBookmarksToBuffer(writable)
                    val exportedBytes = writable.toByteArray()
                    assertTrue(exportedBytes.isNotEmpty())

                    // Verify BinaryJson session state round-trip
                    val sessionWritable = WritableByteBuffer()
                    firstFrag.sessionState.writeToBuffer(sessionWritable)
                    val decodedSession = BrowserSessionState.readFromBuffer(
                        ReadableByteBuffer(sessionWritable.toByteArray())
                    )
                    assertEquals("https://example.com/home", decodedSession.homeUrl)
                    assertEquals("https://search.example.org/?q=", decodedSession.searchEnginePrefix)
                    assertFalse(decodedSession.showBookmarksBar)

                    firstController.destroy()

                    // Relaunch against the same PreferenceStore -> starts at 0 open tabs with persisted settings & bookmarks
                    val secondController = launchFragment { GenericUIBrowserFragment(store) }.setup()
                    val secondFrag = secondController.fragment

                    expect(secondFrag.tabCount).toBe(0)
                    assertNull(secondFrag.activeTabId)
                    assertTrue(secondFrag.defaultNewTabSurface.isVisible)
                    expect(secondFrag.sessionState.homeUrl).toBe("https://example.com/home")
                    expect(secondFrag.sessionState.searchEnginePrefix).toBe("https://search.example.org/?q=")
                    assertFalse(secondFrag.sessionState.showBookmarksBar)
                    expect(secondFrag.bookmarks.size).toBe(1)
                    expect(secondFrag.bookmarks[0].url).toBe("https://example.com/guide")

                    val restoredBookmarks = secondFrag.importBookmarksFromBuffer(ReadableByteBuffer(exportedBytes))
                    expect(restoredBookmarks.size).toBe(1)
                    expect(restoredBookmarks[0].url).toBe("https://example.com/guide")

                    secondController.destroy()
                    store.clear()
                }
            }

            describe("Chromium Tab Strip, TitleBar Embedding, Pill Omnibox & Circular Controls") {
                test("embeds Chromium strip into TitleBar when supported, falls back to content header on Swing, and wires pill StyledTextField and circular StyledButtons") {
                    val store = PreferenceStore.open("GenericUIBrowser_ChromiumChrome_Test")
                    store.clear()
                    val controller = launchApp(visualOnWindows = false) { GenericUIBrowserApp(store) }.setup()
                    try {
                    val frag = controller.app.browserFragment

                    // Verify moved .styled components and Chromium configuration
                    assertTrue(frag.desktopTabStripContainer is StyledContainer)
                    assertTrue(frag.chromiumDragSpacer is StyledComponent)
                    assertTrue(frag.omniboxField is StyledTextField)
                    assertTrue(frag.mobileOmniboxField is StyledTextField)
                    assertTrue(frag.defaultNewTabSurface.searchField is StyledTextField)
                    expect(frag.config.chromeStyle).toBe(TabbedChromeStyle.CHROMIUM)
                    assertTrue(frag.config.showNewTabButton)
                    assertTrue(frag.config.showTabSearchButton)
                    assertTrue(frag.config.connectActiveTabToNavbar)
                    assertSame(frag.navToolbar, frag.currentTabNavbar)
                    assertTrue(frag.tabNavbarSlotContainer.isVisible)
                    assertNull(frag.headerSlotContainer.heightPx)

                    // Verify pill omnibox & New Tab search StyledTextField geometry
                    expect(frag.omniboxField.placeholder).toBe("Search or enter web address")
                    expect(frag.omniboxField.borderRadiusStyle).toBe(BorderRadiusStyle.PILL)
                    assertEquals(CssCornerRadii.px(999), frag.omniboxField.getStyle().borderRadius)
                    expect(frag.omniboxField.getStyle().flexGrow).toBe(1f)
                    expect(frag.omniboxField.getStyle().minWidth).toBe(CssLength.px(220))

                    expect(frag.defaultNewTabSurface.searchField.placeholder).toBe("Search or enter web address")
                    expect(frag.defaultNewTabSurface.searchField.borderRadiusStyle).toBe(BorderRadiusStyle.PILL)
                    assertEquals(CssCornerRadii.px(999), frag.defaultNewTabSurface.searchField.getStyle().borderRadius)
                    expect(frag.defaultNewTabSurface.searchField.getStyle().maxWidth).toBe(CssLength.px(640))

                    // Verify circular 30x30 StyledButton controls (only ← → ↻ ⌂ on left and ⋮ on right)
                    val circleControls: List<StyledButton> = listOf(
                        frag.navBackButton,
                        frag.navForwardButton,
                        frag.reloadButton,
                        frag.homeButton,
                        frag.browserMenuButton
                    )
                    for (circleBtn in circleControls) {
                        expect(circleBtn.getStyle().width).toBe(CssLength.px(30))
                        expect(circleBtn.getStyle().height).toBe(CssLength.px(30))
                        assertEquals(CssCornerRadii.px(999), circleBtn.getStyle().borderRadius)
                    }
                    expect(frag.securityLockBadge.getStyle().width).toBe(CssLength.px(28))
                    expect(frag.securityLockBadge.getStyle().height).toBe(CssLength.px(28))
                    assertEquals(CssCornerRadii.px(999), frag.securityLockBadge.getStyle().borderRadius)

                    // Single-line pill omnibox submits search queries ("youtube") and explicit URLs ("https://example.com")
                    frag.omniboxField.onSubmit?.invoke("youtube")
                    val activeTab = frag.activeTabPageFragment
                    assertNotNull(activeTab)
                    expect(activeTab.currentUrl).toBe("${BrowserSessionState.DEFAULT_SEARCH_PREFIX}youtube")
                    if (activeTab.isPageLoading) {
                        expect(frag.reloadButton.text).toBe("×")
                        frag.reloadButton.triggerClick()
                        assertFalse(activeTab.isPageLoading)
                        expect(frag.reloadButton.text).toBe("↻")
                    } else {
                        assertFalse(activeTab.isPageLoading)
                        expect(frag.reloadButton.text).toBe("↻")
                    }

                    frag.omniboxField.onSubmit?.invoke("https://example.com")
                    assertEquals("https://example.com", activeTab.currentUrl.removeSuffix("/"))
                    if (activeTab.isPageLoading) {
                        activeTab.webView.onPageFinished?.invoke("https://example.com")
                    }
                    assertFalse(activeTab.isPageLoading)

                    // Real HTML <title> & favicon callbacks update the tab in-place without ASCII "[W]" / "[+]" badges
                    frag.navigateActiveTabTo("about:blank")
                    activeTab.webView.onFaviconChanged?.invoke("data:,")
                    assertNull(activeTab.currentFaviconUrl)

                    val activeId = assertNotNull(frag.activeTabId)
                    assertNull(frag.tabs.first { it.tabId == activeId }.iconName)
                    assertNull(frag.tabs.first { it.tabId == activeId }.iconUrl)
                    val tabContainerBefore = frag.desktopTabStripContainer.children
                        .filterIsInstance<StyledContainer>()
                        .first { it.getAttribute("data-tab-id") == activeId }
                    val tabIconSlot = tabContainerBefore.children
                        .filterIsInstance<FlexBox>()
                        .first { it.hasClass("gui-tab-icon-slot") }
                    val initialIconImg = tabIconSlot.children.filterIsInstance<Image>().first()
                    assertTrue(initialIconImg.sourceUrl?.startsWith("data:image/svg+xml") == true)

                    activeTab.webView.onTitleChanged?.invoke("Example Domain")
                    expect(activeTab.currentTitle).toBe("Example Domain")
                    expect(frag.tabs.first { it.tabId == activeId }.title).toBe("Example Domain")
                    val tabTitleLabel = tabContainerBefore.children
                        .filterIsInstance<Label>()
                        .first { it.hasClass("gui-tab-title") }
                    expect(tabTitleLabel.text).toBe("Example Domain")

                    // Subsequent onPageFinished preserves the HTML <title> instead of overwriting it with URL fallback
                    activeTab.webView.onPageFinished?.invoke("about:blank")
                    expect(activeTab.currentTitle).toBe("Example Domain")
                    expect(tabTitleLabel.text).toBe("Example Domain")

                    // Real site favicon updates TabItemSpec.iconUrl and TabbedFragment Image in place with tintMode NONE
                    activeTab.webView.onFaviconChanged?.invoke("https://example.com/favicon.png")
                    expect(activeTab.currentFaviconUrl).toBe("https://example.com/favicon.png")
                    val updatedSpec = frag.tabs.first { it.tabId == activeId }
                    expect(updatedSpec.iconUrl).toBe("https://example.com/favicon.png")
                    assertFalse(updatedSpec.tintIconToTheme)
                    val updatedIconImg = tabIconSlot.children.filterIsInstance<Image>().first()
                    expect(updatedIconImg.sourceUrl).toBe("https://example.com/favicon.png")
                    expect(updatedIconImg.tintMode).toBe(ImageTintMode.NONE)

                    // Navigating back to about:newtab clears the site favicon and restores the vector globe fallback
                    frag.navigateActiveTabTo(BrowserTabPageFragment.URL_NEW_TAB)
                    assertNull(activeTab.currentFaviconUrl)
                    assertNull(frag.tabs.first { it.tabId == activeId }.iconUrl)
                    assertTrue(updatedIconImg.sourceUrl?.startsWith("data:image/svg+xml") == true)

                    // Staying in Desktop mode on resize does NOT rebuild the desktop tab strip
                    frag.updateViewportSize(1280, 800)
                    frag.updateViewportSize(1600, 900)
                    frag.onResize(1600, 900)
                    val tabContainerAfterResize = frag.desktopTabStripContainer.children
                        .filterIsInstance<StyledContainer>()
                        .first { it.getAttribute("data-tab-id") == activeId }
                    assertSame(tabContainerBefore, tabContainerAfterResize)

                    // BrowserNewTabView pill StyledTextField opens a new tab search cleanly
                    frag.requestNewTab()
                    val newTabPage = frag.activeTabPageFragment
                    assertNotNull(newTabPage)
                    assertTrue(newTabPage.newTabSurface.searchField is StyledTextField)
                    newTabPage.newTabSurface.searchField.onSubmit?.invoke("genericui chromium")
                    expect(newTabPage.currentUrl).toBe("${BrowserSessionState.DEFAULT_SEARCH_PREFIX}genericui+chromium")

                    // Theme mode toggle alternates DARK / LIGHT Chromium palettes cleanly
                    val firstToggled = frag.toggleThemeMode()
                    assertTrue(firstToggled == ThemeMode.LIGHT || firstToggled == ThemeMode.DARK)
                    val secondToggled = frag.toggleThemeMode()
                    assertEquals(if (firstToggled == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK, secondToggled)

                    // Verify TitleBar embedding when canEmbedComponentsInTitleBar == true
                    val embedStore = PreferenceStore.open("GenericUIBrowser_EmbedHost_Test")
                    embedStore.clear()
                    val desktopCaps = TitleCapabilities(
                        isSupported = true,
                        isTitleBarRequired = true,
                        supportsCustomTitleBar = true,
                        supportsCustomComponents = true
                    )
                    val desktopWindow = object : Window(title = "DesktopChromiumHost", initialSize = Size(1200, 800)) {
                        override val titleCapabilities: TitleCapabilities
                            get() = desktopCaps
                    }
                    val desktopTb = TitleBar(
                        embedMenuBar = true,
                        showWindowTitle = false,
                        showWindowIcon = true,
                        customTitleBar = true
                    )
                    desktopWindow.setTitleBar(desktopTb)
                    val desktopFrag = GenericUIBrowserFragment(embedStore)
                    desktopFrag.setTitleBar(desktopTb)
                    desktopWindow.setFragment(desktopFrag)
                    try {
                        desktopWindow.show()
                        desktopFrag.openBrowserTab("about:blank", selectImmediately = true)
                        assertTrue(desktopFrag.isEmbeddedInTitleBar)
                        assertTrue(desktopFrag.desktopTabStripContainer.mountedInTitleBar)
                        assertFalse(desktopFrag.headerSlotContainer.isVisible)
                        assertTrue(desktopFrag.tabNavbarSlotContainer.isVisible)
                        assertSame(desktopFrag.navToolbar, desktopFrag.currentTabNavbar)
                        assertTrue(desktopTb.leadingComponents.contains(desktopFrag.desktopTabStripContainer))
                    } finally {
                        desktopWindow.close()
                        embedStore.clear()
                    }

                    // Verify Swing/non-embedding fallback renders tabs at top of content header above navToolbar
                    val swingStore = PreferenceStore.open("GenericUIBrowser_SwingHost_Test")
                    swingStore.clear()
                    val swingCaps = TitleCapabilities(
                        isSupported = true,
                        isTitleBarRequired = true,
                        supportsCustomTitleBar = false,
                        supportsCustomComponents = false
                    )
                    val swingWindow = object : Window(title = "SwingHost", initialSize = Size(1200, 800)) {
                        override val titleCapabilities: TitleCapabilities
                            get() = swingCaps
                    }
                    val swingFrag = GenericUIBrowserFragment(swingStore)
                    swingWindow.setFragment(swingFrag)
                    try {
                        swingWindow.show()
                        swingFrag.openBrowserTab("about:blank", selectImmediately = true)
                        assertFalse(swingFrag.isEmbeddedInTitleBar)
                        assertFalse(swingFrag.desktopTabStripContainer.mountedInTitleBar)
                        assertTrue(swingFrag.headerSlotContainer.isVisible)
                        assertSame(swingFrag.headerSlotContainer, swingFrag.desktopTabStripContainer.parent)
                        assertTrue(swingFrag.tabNavbarSlotContainer.isVisible)
                        assertSame(swingFrag.navToolbar, swingFrag.currentTabNavbar)
                    } finally {
                        swingWindow.close()
                        swingStore.clear()
                    }
                    } finally {
                        controller.destroy()
                        store.clear()
                    }
                }
            }

            describe("Unified Chromium ⋮ Overflow Menu (Bookmarks, History, Theme, View, Settings)") {
                test("opens anchored ⋮ overflow menu with dynamic Bookmarks, History, Theme checkmarks, View, and Settings actions") {
                    val store = PreferenceStore.open("GenericUIBrowser_OverflowMenu_Test")
                    store.clear()
                    val controller = launchApp(visualOnWindows = false) { GenericUIBrowserApp(store) }.setup()
                    try {
                        val frag = controller.app.browserFragment

                        // 0 tabs open: Reload, Close Tab, Bookmark This Page, Clear Bookmarks, Clear History disabled
                        frag.browserMenuButton.triggerClick()
                        assertTrue(frag.browserOverflowMenu.isShowing)
                        assertSame(frag.browserMenuButton, frag.browserOverflowMenu.lastAnchor)
                        expect(frag.browserOverflowMenu.lastShownX).toBe(0)

                        assertFalse(findMenuItem(frag.browserOverflowMenu, "Reload").isEnabled)
                        assertFalse(findMenuItem(frag.browserOverflowMenu, "Close Tab").isEnabled)

                        val initialBookmarksMenu = findSubmenu(frag.browserOverflowMenu, "Bookmarks")
                        assertFalse(findSubmenuItem(initialBookmarksMenu, "Bookmark This Page").isEnabled)
                        assertFalse(findSubmenuItem(initialBookmarksMenu, "No Bookmarks Saved").isEnabled)
                        assertFalse(findSubmenuItem(initialBookmarksMenu, "Clear Bookmarks").isEnabled)

                        val initialHistoryMenu = findSubmenu(frag.browserOverflowMenu, "History")
                        assertFalse(findSubmenuItem(initialHistoryMenu, "Back").isEnabled)
                        assertFalse(findSubmenuItem(initialHistoryMenu, "Forward").isEnabled)
                        assertFalse(findSubmenuItem(initialHistoryMenu, "No Recent History").isEnabled)
                        assertFalse(findSubmenuItem(initialHistoryMenu, "Clear History").isEnabled)

                        val initialThemeMenu = findSubmenu(frag.browserOverflowMenu, "Theme")
                        val autoItem = findSubmenuItem(initialThemeMenu, "System Default")
                        val lightItem = findSubmenuItem(initialThemeMenu, "Light")
                        val darkItem = findSubmenuItem(initialThemeMenu, "Dark")
                        assertTrue(autoItem is RadioMenuItem && autoItem.isChecked())
                        assertFalse(lightItem.isChecked())
                        assertFalse(darkItem.isChecked())

                        val viewMenu = findSubmenu(frag.browserOverflowMenu, "View")
                        assertNotNull(findSubmenuItem(viewMenu, "Desktop Browser Mode (1280×800)"))
                        assertNotNull(findSubmenuItem(viewMenu, "Mobile Browser Mode (412×915)"))
                        assertNotNull(findSubmenuItem(viewMenu, "Mobile Tab Switcher Overview"))

                        val initialSettingsItem = findMenuItem(frag.browserOverflowMenu, "Settings")
                        assertTrue(initialSettingsItem is CheckMenuItem)
                        assertFalse(initialSettingsItem.isChecked())
                        assertNotNull(findMenuItem(frag.browserOverflowMenu, "Exit"))

                        // Trigger Dark theme via Theme submenu item -> updates app theme & checkmark on next open
                        darkItem.triggerClick()
                        expect(controller.app.getThemeMode()).toBe(ThemeMode.DARK)

                        frag.browserMenuButton.triggerClick()
                        val darkThemeMenu = findSubmenu(frag.browserOverflowMenu, "Theme")
                        assertFalse(findSubmenuItem(darkThemeMenu, "System Default").isChecked())
                        assertFalse(findSubmenuItem(darkThemeMenu, "Light").isChecked())
                        assertTrue(findSubmenuItem(darkThemeMenu, "Dark").isChecked())

                        // Navigate to two pages so Reload/Stop, Close Tab, Bookmarks, and History populate
                        frag.navigateActiveTabTo("https://example.org/one")
                        val page = assertNotNull(frag.activeTabPageFragment)
                        page.webView.onPageFinished?.invoke("https://example.org/one")
                        frag.navigateActiveTabTo("https://example.org/two")

                        frag.browserMenuButton.triggerClick()
                        assertTrue(findMenuItem(frag.browserOverflowMenu, "Close Tab").isEnabled)
                        if (page.isPageLoading) {
                            val stopItem = findMenuItem(frag.browserOverflowMenu, "Stop loading")
                            assertTrue(stopItem.isEnabled)
                            stopItem.triggerClick()
                            assertFalse(page.isPageLoading)
                        } else {
                            val reloadItem = findMenuItem(frag.browserOverflowMenu, "Reload")
                            assertTrue(reloadItem.isEnabled)
                            val prevReloads = page.reloadCount
                            reloadItem.triggerClick()
                            expect(page.reloadCount).toBe(prevReloads + 1)
                            page.webView.onPageFinished?.invoke("https://example.org/two")
                            assertFalse(page.isPageLoading)
                        }

                        // Trigger Bookmark This Page via menu -> adds bookmark and flips label to Remove Bookmark
                        frag.browserMenuButton.triggerClick()
                        val bookmarksBeforeSave = findSubmenu(frag.browserOverflowMenu, "Bookmarks")
                        val saveBookmarkItem = findSubmenuItem(bookmarksBeforeSave, "Bookmark This Page")
                        assertTrue(saveBookmarkItem.isEnabled)
                        saveBookmarkItem.triggerClick()
                        expect(frag.bookmarks.size).toBe(1)
                        expect(frag.bookmarks[0].url).toBe("https://example.org/two")

                        frag.browserMenuButton.triggerClick()
                        val bookmarksAfterSave = findSubmenu(frag.browserOverflowMenu, "Bookmarks")
                        assertNotNull(findSubmenuItem(bookmarksAfterSave, "Remove Bookmark"))
                        val expectedBookmarkLabel = "${frag.bookmarks[0].title} — ${BrowserTabPageFragment.resolveSubtitleForUrl(frag.bookmarks[0].url)}"
                        assertNotNull(findSubmenuItem(bookmarksAfterSave, expectedBookmarkLabel))
                        assertTrue(findSubmenuItem(bookmarksAfterSave, "Clear Bookmarks").isEnabled)

                        val populatedHistoryMenu = findSubmenu(frag.browserOverflowMenu, "History")
                        assertTrue(findSubmenuItem(populatedHistoryMenu, "Back").isEnabled)
                        val historyEntries = frag.globalHistory
                        expect(historyEntries.size).toBe(2)
                        assertNotNull(
                            findSubmenuItem(
                                populatedHistoryMenu,
                                "${historyEntries[1].title} — ${BrowserTabPageFragment.resolveSubtitleForUrl(historyEntries[1].url)}"
                            )
                        )
                        assertNotNull(
                            findSubmenuItem(
                                populatedHistoryMenu,
                                "${historyEntries[0].title} — ${BrowserTabPageFragment.resolveSubtitleForUrl(historyEntries[0].url)}"
                            )
                        )
                        findSubmenuItem(populatedHistoryMenu, "Clear History").triggerClick()
                        assertTrue(frag.globalHistory.isEmpty())

                        // Toggle Settings panel via CheckMenuItem -> checkmark syncs on next open
                        findMenuItem(frag.browserOverflowMenu, "Settings").triggerClick()
                        assertTrue(frag.settingsPanel.isVisible)
                        frag.browserMenuButton.triggerClick()
                        assertTrue(findMenuItem(frag.browserOverflowMenu, "Settings").isChecked())

                        // Mobile ⋮ menu button opens the same comprehensive menu anchored to mobileMenuButton
                        frag.mobileMenuButton.triggerClick()
                        assertSame(frag.mobileMenuButton, frag.browserOverflowMenu.lastAnchor)
                        assertNotNull(findMenuItem(frag.browserOverflowMenu, "New Tab"))

                        // Trigger New Tab & Close Tab via menu
                        findMenuItem(frag.browserOverflowMenu, "New Tab").triggerClick()
                        expect(frag.tabCount).toBe(2)
                        frag.browserMenuButton.triggerClick()
                        findMenuItem(frag.browserOverflowMenu, "Close Tab").triggerClick()
                        expect(frag.tabCount).toBe(1)
                    } finally {
                        controller.destroy()
                        store.clear()
                    }
                }
            }

            describe("Settings Panel Label.target Bindings") {
                test("binds Label.target for every Settings label-control pair and supports focus or HTML for association") {
                    val store = PreferenceStore.open("GenericUIBrowser_SettingsTarget_Test")
                    store.clear()
                    val controller = launchApp(visualOnWindows = false) { GenericUIBrowserApp(store) }.setup()
                    try {
                    val frag = controller.app.browserFragment

                    frag.toggleSettingsPanel()
                    assertTrue(frag.settingsPanel.isVisible)

                    val pairs = listOf<Pair<com.nexustools.genericui.component.typography.Label, ILabelTarget>>(
                        frag.settingsHomeUrlLabel to frag.settingsHomeUrlField,
                        frag.settingsSearchPrefixLabel to frag.settingsSearchPrefixField,
                        frag.settingsShowBookmarksLabel to frag.settingsShowBookmarksCheckBox,
                        frag.settingsThemeLabel to frag.settingsThemeSelect
                    )

                    val isWebLabels = frag.resolveActiveFactory()?.supportsNativeHtmlLabelFor == true
                    for ((label, expectedTarget) in pairs) {
                        assertSame(expectedTarget, label.target)
                        val targetComponent = expectedTarget.labelTargetComponent
                        if (isWebLabels) {
                            assertEquals(expectedTarget.ensureLabelTargetId(), label.getAttribute("for"))
                        } else {
                            targetComponent.isFocused = false
                            label.triggerClick()
                            assertTrue(
                                targetComponent.isFocused,
                                "Clicking Label '${label.text}' should focus its target ${targetComponent.id}"
                            )
                        }
                    }
                    } finally {
                        controller.destroy()
                        store.clear()
                    }
                }
            }
        }
        report.assertAllPassed()
    }
}
