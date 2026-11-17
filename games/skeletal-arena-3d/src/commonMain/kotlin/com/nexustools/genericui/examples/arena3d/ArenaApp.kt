package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.arena3d.res.Resource
import com.nexustools.genericui.window.TitleBar

/**
 * Main `@AppApplication` entry point for Vanguard Arena 3D (`examples/games/skeletal-arena-3d`).
 */
@AppApplication
class ArenaApp(
    val controller: ArenaEngineController = ArenaEngineController(),
    val arenaFragment: ArenaFragment = ArenaFragment(controller)
) : FragmentApplication(
    initialFragment = arenaFragment,
    title = "Vanguard Arena 3D — GenericUI",
    initialThemeColor = "#EC4899"
) {
    init {
        setThemeMode(ThemeMode.DARK)
        setThemeColor("#EC4899")
        addStyleSheet(Resource.style.styles)

        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = MenuBar()
            .addMenu(
                Menu("Arena")
                    .addItem(MenuItem("Reset Arena") {
                        controller.resetArena()
                        arenaFragment.refreshHud()
                    })
                    .addItem(MenuItem("Pause / Resume") {
                        arenaFragment.togglePauseMenu()
                    })
            )
            .addMenu(
                Menu("Camera & Pad")
                    .addItem(MenuItem("Toggle Follow / Orbit Camera") {
                        controller.toggleCameraMode()
                        arenaFragment.refreshHud()
                    })
                    .addItem(MenuItem("Toggle 6-Axis Gyro Aim") {
                        controller.toggleGyroAim()
                        arenaFragment.refreshHud()
                    })
                    .addItem(MenuItem("Cycle Controller Profile") {
                        controller.cycleControllerProfile()
                        arenaFragment.refreshHud()
                    })
            )

        arenaFragment.setTitleBar(titleBar)
        arenaFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
    }
}
