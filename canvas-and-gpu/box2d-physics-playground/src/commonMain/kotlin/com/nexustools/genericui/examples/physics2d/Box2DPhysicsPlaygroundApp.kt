package com.nexustools.genericui.examples.physics2d

import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.physics2d.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multi-backend [FragmentApplication] entry point for the Box2D Physics Playground example application.
 */
class Box2DPhysicsPlaygroundApp(
    val physicsFragment: Box2DPhysicsPlaygroundFragment = Box2DPhysicsPlaygroundFragment()
) : FragmentApplication(
    initialFragment = physicsFragment,
    title = "Box2D Rigid-Body Physics Playground",
    initialThemeColor = "#10B981"
) {
    init {
        setThemeMode(ThemeMode.DARK)
        val titleBar = TitleBar(
            embedMenuBar = false,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        physicsFragment.setTitleBar(titleBar)
        mainWindow.setTitleBar(titleBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    companion object {
        fun start(args: Array<String> = emptyArray()) {
            if (args.contains("--visual-test")) {
                Box2DPhysicsPlaygroundVisualTestMain.runVisualSuite(args)
                return
            }
            val app = Box2DPhysicsPlaygroundApp()
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
    Box2DPhysicsPlaygroundApp.start(args)
}
