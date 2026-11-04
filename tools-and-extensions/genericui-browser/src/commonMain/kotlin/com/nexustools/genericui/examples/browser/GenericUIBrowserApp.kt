package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.fragment.helper.TabbedChromeMode
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.browser.res.GeneratedResourceRegistry
import com.nexustools.genericui.examples.browser.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multi-platform [FragmentApplication] entry point for `GenericUIBrowser`, a Google Chrome
 * Desktop and Mobile clone powered by [GenericUIBrowserFragment], [com.nexustools.genericui.component.fragment.helper.TabbedFragment],
 * and [com.nexustools.genericui.ext.webview.WebView].
 */
@AppApplication
class GenericUIBrowserApp(
    val browserFragment: GenericUIBrowserFragment = GenericUIBrowserFragment()
) : FragmentApplication(
    initialFragment = browserFragment,
    title = "GenericUIBrowser",
    initialThemeColor = "#1F2937"
) {

    constructor(preferenceStore: PreferenceStore) : this(GenericUIBrowserFragment(preferenceStore))

    val fragment: GenericUIBrowserFragment
        get() = browserFragment

    init {
        GeneratedResourceRegistry.ensureInitialized()
        setThemeMode(GeneratedResourceRegistry.DEFAULT_THEME_MODE)
        addStyleSheet(StylesPrecompiledStyleSheet)
        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = buildBrowserMenuBar()
        browserFragment.setTitleBar(titleBar)
        browserFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildBrowserMenuBar(): MenuBar {
        val bar = MenuBar()

        val fileMenu = Menu("File")
            .addItem(MenuItem("New Tab", shortcutText = "Ctrl+T") {
                browserFragment.requestNewTab()
            })
            .addItem(MenuItem("Open Google One AI Credits Tab") {
                browserFragment.selectTab(GenericUIBrowserFragment.TAB_ID_AI_CREDITS)
            })
            .addItem(MenuItem("Open T-Crypt/berth GitHub Tab") {
                browserFragment.selectTab(GenericUIBrowserFragment.TAB_ID_GITHUB_BERTH)
            })
            .addSeparator()
            .addItem(MenuItem("Close Active Tab", shortcutText = "Ctrl+W") {
                browserFragment.activeTabId?.let { browserFragment.closeTab(it) }
            })
            .addItem(MenuItem("Exit") {
                exit(0)
            })

        val viewMenu = Menu("View")
            .addItem(MenuItem("Desktop Browser Mode (1280×800)") {
                browserFragment.chromeModeOverride = null
                browserFragment.updateViewportSize(1280, 800)
            })
            .addItem(MenuItem("Mobile Chrome Switcher Mode (412×915)") {
                browserFragment.chromeModeOverride = null
                browserFragment.updateViewportSize(412, 915)
            })
            .addItem(MenuItem("Toggle Mobile Tab Switcher Overview") {
                browserFragment.chromeModeOverride = TabbedChromeMode.MOBILE_SWITCHER_PILL
                browserFragment.toggleMobileTabOverview()
            })
            .addSeparator()
            .addItem(MenuItem("Toggle Bookmarks Bar", shortcutText = "Ctrl+Shift+B") {
                browserFragment.toggleBookmarksBar()
            })
            .addItem(MenuItem("Toggle ✦ Ask Gemini Side Panel") {
                browserFragment.toggleAskGeminiSidebar()
            })

        val historyMenu = Menu("History")
            .addItem(MenuItem("Back", shortcutText = "Alt+Left") {
                browserFragment.navigateBackInActiveTab()
            })
            .addItem(MenuItem("Forward", shortcutText = "Alt+Right") {
                browserFragment.navigateForwardInActiveTab()
            })
            .addItem(MenuItem("Reload", shortcutText = "Ctrl+R") {
                browserFragment.reloadActiveTab()
            })
            .addItem(MenuItem("Home") {
                browserFragment.navigateHomeInActiveTab()
            })

        val bookmarksMenu = Menu("Bookmarks")
            .addItem(MenuItem("Bookmark This Tab", shortcutText = "Ctrl+D") {
                browserFragment.toggleBookmarkForActiveTab()
            })
            .addSeparator()
            .addItem(MenuItem("🔵 AI credits activity") {
                browserFragment.navigateActiveTabTo(BrowserTabPageFragment.URL_AI_CREDITS)
            })
            .addItem(MenuItem("🐙 T-Crypt/berth") {
                browserFragment.navigateActiveTabTo(BrowserTabPageFragment.URL_GITHUB_BERTH)
            })
            .addItem(MenuItem("💡 Google Keep") {
                browserFragment.navigateActiveTabTo(BrowserTabPageFragment.URL_KEEP)
            })
            .addItem(MenuItem("✦ Ask Gemini") {
                browserFragment.navigateActiveTabTo(BrowserTabPageFragment.URL_GEMINI_DOC)
            })

        return bar
            .addMenu(fileMenu)
            .addMenu(viewMenu)
            .addMenu(historyMenu)
            .addMenu(bookmarksMenu)
    }
}
