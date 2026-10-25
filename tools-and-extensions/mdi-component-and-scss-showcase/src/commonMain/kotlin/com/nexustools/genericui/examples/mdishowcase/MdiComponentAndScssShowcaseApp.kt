package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.mdishowcase.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] hosting [MdiComponentAndScssShowcaseFragment].
 */
class MdiComponentAndScssShowcaseApp(
    val showcaseFragment: MdiComponentAndScssShowcaseFragment = MdiComponentAndScssShowcaseFragment()
) : FragmentApplication(
    initialFragment = showcaseFragment,
    title = "GenericUI — MDI Component & Live SCSS Stylesheet Showcase",
    initialThemeColor = "#FF2A6D"
) {

    init {
        MdiShowcasePlatformBootstrap.initialize()
        setThemeMode(ThemeMode.DARK)
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
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    companion object {
        fun start(args: Array<String> = emptyArray()) {
            MdiShowcasePlatformBootstrap.initialize(args)
            if (args.contains("--visual-test")) {
                MdiComponentAndScssShowcaseVisualTestMain.runVisualSuite(args)
                return
            }
            val app = MdiComponentAndScssShowcaseApp()
            app.run()
        }
    }
}

fun main(args: Array<String>) {
    MdiComponentAndScssShowcaseApp.start(args)
}
