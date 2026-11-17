package com.nexustools.genericui.examples.dungeoncrawler

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.dungeoncrawler.res.Resource
import com.nexustools.genericui.window.TitleBar

/**
 * Main `@AppApplication` entry point for Abyssal Crypt (`examples/games/ui-dungeon-crawler`).
 */
@AppApplication
class DungeonCrawlerApp(
    val controller: DungeonEngineController = DungeonEngineController(),
    val dungeonFragment: DungeonCrawlerFragment = DungeonCrawlerFragment(controller)
) : FragmentApplication(
    initialFragment = dungeonFragment,
    title = "Abyssal Crypt — GenericUI Dungeon Crawler",
    initialThemeColor = "#F59E0B"
) {
    init {
        setThemeMode(ThemeMode.DARK)
        setThemeColor("#F59E0B")
        addStyleSheet(Resource.style.styles)

        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = MenuBar()
            .addMenu(
                Menu("Expedition")
                    .addItem(MenuItem("Restart Run") {
                        controller.resetRun()
                        dungeonFragment.refreshUi()
                    })
                    .addItem(MenuItem("Pause / Resume") {
                        dungeonFragment.togglePauseMenu()
                    })
            )
            .addMenu(
                Menu("Controller")
                    .addItem(MenuItem("Cycle Hardware Profile") {
                        controller.cycleControllerProfile()
                        dungeonFragment.refreshUi()
                    })
                    .addItem(MenuItem("Toggle Gyro Cursor") {
                        controller.toggleGyroAimCursor()
                        dungeonFragment.refreshUi()
                    })
                    .addItem(MenuItem("Switch Focus Strategy") {
                        dungeonFragment.switchFocusMode()
                    })
            )

        dungeonFragment.setTitleBar(titleBar)
        dungeonFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
    }
}
