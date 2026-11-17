package com.nexustools.genericui.examples.platformer2d

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.platformer2d.res.Resource
import com.nexustools.genericui.window.TitleBar

/**
 * Main `@AppApplication` entry point for Skyline Courier 2D (`examples/games/svg-skeletal-platformer-2d`).
 */
@AppApplication
class PlatformerApp(
    val controller: PlatformerEngineController = PlatformerEngineController(),
    val platformerFragment: PlatformerFragment = PlatformerFragment(controller)
) : FragmentApplication(
    initialFragment = platformerFragment,
    title = "Skyline Courier 2D — SVG Skeletal Platformer",
    initialThemeColor = "#38BDF8"
) {
    init {
        setThemeMode(ThemeMode.DARK)
        setThemeColor("#38BDF8")
        addStyleSheet(Resource.style.styles)

        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = MenuBar()
            .addMenu(
                Menu("Game")
                    .addItem(MenuItem("Reset Stage") {
                        controller.resetStage()
                        platformerFragment.refreshHud()
                    })
                    .addItem(MenuItem("Pause / Resume") {
                        platformerFragment.togglePauseMenu()
                    })
            )
            .addMenu(
                Menu("Rig & Gamepad")
                    .addItem(MenuItem("Toggle Bone Overlay") {
                        controller.toggleBoneOverlay()
                        platformerFragment.refreshHud()
                    })
                    .addItem(MenuItem("Cycle Controller Profile") {
                        controller.cycleControllerProfile()
                        platformerFragment.refreshHud()
                    })
            )

        platformerFragment.setTitleBar(titleBar)
        platformerFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
    }
}
