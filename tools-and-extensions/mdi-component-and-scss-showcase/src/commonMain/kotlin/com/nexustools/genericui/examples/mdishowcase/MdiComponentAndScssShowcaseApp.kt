package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.mdishowcase.res.Resource
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] hosting [MdiComponentAndScssShowcaseFragment].
 */
@AppApplication
class MdiComponentAndScssShowcaseApp(
    val showcaseFragment: MdiComponentAndScssShowcaseFragment = MdiComponentAndScssShowcaseFragment()
) : FragmentApplication(
    initialFragment = showcaseFragment,
    title = "GenericUI — MDI Component & Live SCSS Stylesheet Showcase",
    initialThemeColor = "#FF2A6D"
) {

    init {
        setThemeMode(ThemeMode.AUTO)
        addStyleSheet(Resource.style.styles)
        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        showcaseFragment.setTitleBar(titleBar)
        showcaseFragment.setMenuBar(showcaseFragment.outerMenuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(showcaseFragment.outerMenuBar)
    }

    /**
     * Switches the application theme mode ([ThemeMode.AUTO], [ThemeMode.DARK], or [ThemeMode.LIGHT]).
     */
    fun selectAppThemeMode(mode: ThemeMode) {
        setThemeMode(mode)
    }

    /**
     * Toggles the global precompiled stylesheet (`styles.scss`) via [addStyleSheet] / [removeStyleSheet].
     */
    fun setGlobalStyleSheetEnabled(enabled: Boolean): Boolean {
        return if (enabled) {
            addStyleSheet(Resource.style.styles)
            hasStyleSheet(Resource.style.styles)
        } else {
            removeStyleSheet(Resource.style.styles)
        }
    }
}
