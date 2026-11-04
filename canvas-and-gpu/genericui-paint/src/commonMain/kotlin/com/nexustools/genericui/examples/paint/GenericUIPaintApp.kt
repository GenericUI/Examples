package com.nexustools.genericui.examples.paint

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.examples.paint.res.GeneratedResourceRegistry
import com.nexustools.genericui.examples.paint.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] hosting [GenericUIPaintFragment] (`Untitled - Paint`),
 * configuring the dark-mode Windows 11 Paint [TitleBar], [MenuBar], [StylesPrecompiledStyleSheet],
 * and [getPreferenceStore].
 */
@AppApplication
class GenericUIPaintApp(
    val paintFragment: GenericUIPaintFragment = GenericUIPaintFragment()
) : FragmentApplication(
    initialFragment = paintFragment,
    title = "Untitled - Paint",
    initialThemeColor = "#202024"
) {
    init {
        GeneratedResourceRegistry.ensureInitialized()
        PaintPreferences.registerFactory()
        setThemeMode(GeneratedResourceRegistry.DEFAULT_THEME_MODE)
        addStyleSheet(StylesPrecompiledStyleSheet)

        val titleBar = TitleBar(
            embedMenuBar = false,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )

        val menuBar = MenuBar().apply {
            addMenu(
                Menu(label = "File").apply {
                    addItem(label = "New") { paintFragment.handleNewCanvas() }
                    addItem(label = "Save Preferences") { paintFragment.savePreferencesToStore() }
                    addSeparator()
                    addItem(label = "Exit") { exit(0) }
                }
            )
            addMenu(
                Menu(label = "Edit").apply {
                    addItem(label = "Undo") { paintFragment.undo() }
                    addItem(label = "Redo") { paintFragment.redo() }
                    addSeparator()
                    addItem(label = "Clear Active Layer") { paintFragment.clearActiveLayer() }
                }
            )
            addMenu(
                Menu(label = "View").apply {
                    addItem(label = "Fit to Window (100%)") { paintFragment.fitToWindow() }
                    addItem(label = "Zoom In") {
                        paintFragment.updateZoomPercent((paintFragment.zoomPercent + 25).coerceAtMost(800))
                    }
                    addItem(label = "Zoom Out") {
                        paintFragment.updateZoomPercent((paintFragment.zoomPercent - 25).coerceAtLeast(25))
                    }
                    addSeparator()
                    addItem(label = "Toggle Layers Panel") { paintFragment.toggleLayersPanel() }
                }
            )
        }

        paintFragment.application = this
        paintFragment.setTitleBar(titleBar)
        paintFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)

        // Ensure application PreferenceStore is initialized
        getPreferenceStore()
    }
}
