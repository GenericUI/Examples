package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.dataeditor.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multi-platform [FragmentApplication] entry point for the visual BSON / JSON / Binary Plist Editor
 * powered by [com.nexustools.genericui.component.lazy.LazyGridTree].
 */
@AppApplication
class DataEditorApp(
    val editorFragment: DataEditorFragment = DataEditorFragment()
) : FragmentApplication(
    initialFragment = editorFragment,
    title = "BSON / JSON / Binary Plist Visual Editor",
    initialThemeColor = "#1E293B"
) {

    constructor(preferenceStore: PreferenceStore) : this(DataEditorFragment(preferenceStore))

    val fragment: DataEditorFragment
        get() = editorFragment

    init {
        setTheme(ThemeMode.DARK)
        addStyleSheet(StylesPrecompiledStyleSheet)
        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = buildApplicationMenuBar()
        editorFragment.setTitleBar(titleBar)
        editorFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildApplicationMenuBar(): MenuBar {
        val bar = MenuBar()

        val fileMenu = Menu("File")
            .addItem(MenuItem("New JSON Tab (.json)", shortcutText = "Ctrl+N") {
                editorFragment.createNewEmptyTab(DataFormat.JSON)
            })
            .addItem(MenuItem("New BinaryJson / BSON Tab (.bson)") {
                editorFragment.createNewEmptyTab(DataFormat.BINARY_JSON_BSON)
            })
            .addItem(MenuItem("New Binary Plist Tab (.plist)") {
                editorFragment.createNewEmptyTab(DataFormat.BINARY_PLIST)
            })
            .addSeparator()
            .addItem(MenuItem("Open Document...", shortcutText = "Ctrl+O") {
                editorFragment.openDocumentFromDisk()
            })
            .addItem(MenuItem("Save Active Document...", shortcutText = "Ctrl+S") {
                editorFragment.saveActiveDocumentToDisk()
            })
            .addSeparator()
            .addItem(MenuItem("Close Active Tab", shortcutText = "Ctrl+W") {
                editorFragment.activeTabId?.let { editorFragment.closeTabById(it) }
            })
            .addItem(MenuItem("Exit") {
                exit(0)
            })

        val editMenu = Menu("Edit")
            .addItem(MenuItem("Add Leaf Node to Selected Branch") {
                val targetPath = editorFragment.activeTabState?.selectedNodePath ?: "root"
                editorFragment.addLeafToBranchInActiveTab(targetPath)
            })
            .addItem(MenuItem("Add Branch Container to Selected Branch") {
                val targetPath = editorFragment.activeTabState?.selectedNodePath ?: "root"
                editorFragment.addBranchToBranchInActiveTab(targetPath)
            })
            .addSeparator()
            .addItem(MenuItem("Move Selected Node Up", shortcutText = "Alt+Up") {
                editorFragment.activeTabState?.selectedNodePath?.let { editorFragment.moveNodeUpInActiveTab(it) }
            })
            .addItem(MenuItem("Move Selected Node Down", shortcutText = "Alt+Down") {
                editorFragment.activeTabState?.selectedNodePath?.let { editorFragment.moveNodeDownInActiveTab(it) }
            })
            .addSeparator()
            .addItem(MenuItem("Duplicate Selected Node", shortcutText = "Ctrl+D") {
                editorFragment.activeTabState?.selectedNodePath?.let { editorFragment.duplicateNodeInActiveTab(it) }
            })
            .addItem(MenuItem("Delete Selected Node", shortcutText = "Delete") {
                editorFragment.activeTabState?.selectedNodePath?.let { editorFragment.deleteNodeInActiveTab(it) }
            })

        val convertMenu = Menu("Convert")
            .addItem(MenuItem("Convert Active Tab to JSON (.json)") {
                editorFragment.convertActiveTabFormat(DataFormat.JSON)
            })
            .addItem(MenuItem("Convert Active Tab to BinaryJson / BSON (.bson)") {
                editorFragment.convertActiveTabFormat(DataFormat.BINARY_JSON_BSON)
            })
            .addItem(MenuItem("Convert Active Tab to Binary Plist (.plist)") {
                editorFragment.convertActiveTabFormat(DataFormat.BINARY_PLIST)
            })

        val samplesMenu = Menu("Samples")
            .addItem(MenuItem("Load Sample User Profile (.json)") {
                editorFragment.loadSamplePreset("profile", DataFormat.JSON)
            })
            .addItem(MenuItem("Load Sample App Manifest (.bson)") {
                editorFragment.loadSamplePreset("manifest", DataFormat.BINARY_JSON_BSON)
            })
            .addItem(MenuItem("Load Sample Game Save (.plist)") {
                editorFragment.loadSamplePreset("game", DataFormat.BINARY_PLIST)
            })

        return bar
            .addMenu(fileMenu)
            .addMenu(editMenu)
            .addMenu(convertMenu)
            .addMenu(samplesMenu)
    }
}
