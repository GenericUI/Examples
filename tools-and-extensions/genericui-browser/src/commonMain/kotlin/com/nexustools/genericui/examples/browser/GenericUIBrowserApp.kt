package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.browser.res.GeneratedResourceRegistry
import com.nexustools.genericui.examples.browser.res.Resource
import com.nexustools.genericui.geometry.Size
import com.nexustools.genericui.window.TitleBar

/**
 * Multi-platform [FragmentApplication] entry point for `GenericUIBrowser`, powered by
 * [GenericUIBrowserFragment], [com.nexustools.genericui.component.fragment.helper.TabbedFragment],
 * and [com.nexustools.genericui.ext.webview.WebView].
 */
@AppApplication
class GenericUIBrowserApp(
    val browserFragment: GenericUIBrowserFragment = GenericUIBrowserFragment()
) : FragmentApplication(
    initialFragment = browserFragment,
    title = "GenericUIBrowser"
) {

    constructor(preferenceStore: PreferenceStore) : this(GenericUIBrowserFragment(preferenceStore))

    val fragment: GenericUIBrowserFragment
        get() = browserFragment

    init {
        GeneratedResourceRegistry.ensureInitialized()
        setThemeMode(GeneratedResourceRegistry.DEFAULT_THEME_MODE)
        addStyleSheet(Resource.style.styles)
        setDefaultMainWindowGeometry(1200, 800)
        mainWindow.setWindowSize(Size(1200, 800))

        val titleBar = TitleBar(
            embedMenuBar = false,
            showWindowTitle = false,
            showWindowIcon = false,
            customTitleBar = true
        )
        browserFragment.setTitleBar(titleBar)
        mainWindow.setTitleBar(titleBar)
    }
}
