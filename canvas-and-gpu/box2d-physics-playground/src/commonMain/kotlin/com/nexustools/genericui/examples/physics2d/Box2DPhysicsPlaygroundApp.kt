package com.nexustools.genericui.examples.physics2d

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.physics2d.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multi-backend [FragmentApplication] entry point for the Box2D Physics Playground example application.
 */
@AppApplication
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
}
