package com.nexustools.genericui.examples.pad

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.pad.res.GeneratedResourceRegistry
import com.nexustools.genericui.examples.pad.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] entry point for `GenericUIPad` (Windows 11 Dark-Mode Tabbed Notepad Clone).
 */
@AppApplication
class GenericUIPadApp(
    val padFragment: GenericUIPadFragment = GenericUIPadFragment()
) : FragmentApplication(
    initialFragment = padFragment,
    title = "GenericUIPad",
    initialThemeColor = "#1E1E1E"
) {

    constructor(preferenceStore: PreferenceStore) : this(GenericUIPadFragment(preferenceStore))

    val fragment: GenericUIPadFragment
        get() = padFragment

    init {
        GeneratedResourceRegistry.ensureInitialized()
        setThemeMode(GeneratedResourceRegistry.DEFAULT_THEME_MODE)
        addStyleSheet(StylesPrecompiledStyleSheet)

        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true,
            iconPath = "src/commonMain/resources/icon.svg"
        )
        val menuBar = buildNotepadMenuBar()

        padFragment.setTitleBar(titleBar)
        padFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildNotepadMenuBar(): MenuBar {
        val fileMenu = Menu("File")
            .addItem(MenuItem("New Tab", shortcutText = "Ctrl+N") {
                padFragment.requestNewTab()
            })
            .addItem(MenuItem("Save Session Settings", shortcutText = "Ctrl+S") {
                padFragment.saveSessionSettingsToPreferences()
            })
            .addSeparator()
            .addItem(MenuItem("Close Tab", shortcutText = "Ctrl+W") {
                padFragment.activeTabId?.let { padFragment.closeTab(it) }
            })
            .addItem(MenuItem("Exit") {
                exit(0)
            })

        val editMenu = Menu("Edit")
            .addItem(MenuItem("Find / Replace", shortcutText = "Ctrl+F") {
                padFragment.toggleFindReplaceBar()
            })
            .addSeparator()
            .addItem(MenuItem("Bold", shortcutText = "Ctrl+B") {
                padFragment.applyFormatToActiveTab(PadFormatAction.BOLD)
            })
            .addItem(MenuItem("Italic", shortcutText = "Ctrl+I") {
                padFragment.applyFormatToActiveTab(PadFormatAction.ITALIC)
            })
            .addItem(MenuItem("Strikethrough") {
                padFragment.applyFormatToActiveTab(PadFormatAction.STRIKETHROUGH)
            })
            .addItem(MenuItem("Insert Link") {
                padFragment.applyFormatToActiveTab(PadFormatAction.LINK)
            })
            .addItem(MenuItem("Insert Table") {
                padFragment.applyFormatToActiveTab(PadFormatAction.TABLE)
            })
            .addItem(MenuItem("Clear Formatting") {
                padFragment.applyFormatToActiveTab(PadFormatAction.CLEAR_FORMATTING)
            })

        val viewMenu = Menu("View")
            .addItem(MenuItem("Zoom In", shortcutText = "Ctrl+Plus") {
                padFragment.zoomIn()
            })
            .addItem(MenuItem("Zoom Out", shortcutText = "Ctrl+Minus") {
                padFragment.zoomOut()
            })
            .addItem(MenuItem("Restore Default Zoom", shortcutText = "Ctrl+0") {
                padFragment.resetZoom()
            })
            .addSeparator()
            .addItem(MenuItem("Toggle Plain Text / Markdown Mode") {
                padFragment.toggleActiveDocumentMode()
            })
            .addItem(MenuItem("Notepad Settings") {
                padFragment.toggleSettingsPanel()
            })

        return MenuBar()
            .addMenu(fileMenu)
            .addMenu(editMenu)
            .addMenu(viewMenu)
    }
}
