package com.nexustools.genericui.examples.sketchpad

import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.sketchpad.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] hosting the [SketchpadFragment] 2D vector and Bezier sketchpad.
 */
class SketchpadApp(
    val sketchpadFragment: SketchpadFragment = SketchpadFragment()
) : FragmentApplication(
    initialFragment = sketchpadFragment,
    title = "Canvas2D Vector & Bezier Sketchpad",
    initialThemeColor = "#3B82F6"
) {
    init {
        setThemeMode(ThemeMode.DARK)
        val titleBar = TitleBar(
            embedMenuBar = false,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        sketchpadFragment.setTitleBar(titleBar)
        mainWindow.setTitleBar(titleBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    companion object {
        fun start(args: Array<String> = emptyArray()) {
            if (args.contains("--visual-test")) {
                SketchpadVisualTestMain.runVisualSuite(args)
                return
            }
            val app = SketchpadApp()
            val factory = GenericUI.activeFactory
            if (factory != null) {
                app.run(factory)
            } else {
                app.mainWindow.show(null)
            }
        }
    }
}

fun main(args: Array<String>) {
    SketchpadApp.start(args)
}
