package com.nexustools.genericui.examples.codeeditor

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.codeeditor.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.ext.codeeditor.CodeLanguage
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] hosting [CodeEditorRegexFragment].
 */
@AppApplication
class CodeEditorRegexApp(
    preferenceStore: PreferenceStore = PreferenceStore("CodeEditorRegexExample"),
    val regexFragment: CodeEditorRegexFragment = CodeEditorRegexFragment(preferenceStore)
) : FragmentApplication(
    initialFragment = regexFragment,
    title = "GenericUI — Syntax-Highlighted CodeEditor & Regex Playground",
    initialThemeColor = "#6366F1"
) {

    init {
        setThemeMode(ThemeMode.DARK)
        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = buildMenuBar()
        regexFragment.setTitleBar(titleBar)
        regexFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildMenuBar(): MenuBar {
        val fileMenu = Menu("File")
            .addItem(MenuItem("Save Current Snippet", shortcutText = "Ctrl+S") {
                regexFragment.saveCurrentBufferAsSnippet()
            })
            .addSeparator()
            .addItem(MenuItem("Exit") {
                exit(0)
            })

        val languageMenu = Menu("Language")
            .addItem(MenuItem("Kotlin (.kt)") {
                regexFragment.switchSyntaxLanguage(CodeLanguage.KOTLIN)
            })
            .addItem(MenuItem("TypeScript (.ts)") {
                regexFragment.switchSyntaxLanguage(CodeLanguage.TYPESCRIPT)
            })
            .addItem(MenuItem("JSON (.json)") {
                regexFragment.switchSyntaxLanguage(CodeLanguage.JSON)
            })
            .addItem(MenuItem("CSS (.css)") {
                regexFragment.switchSyntaxLanguage(CodeLanguage.CSS)
            })

        val presetsMenu = Menu("Presets")
            .addItem(MenuItem("Kotlin Functions & Annotations") {
                regexFragment.loadPresetById("kotlin-functions")
            })
            .addItem(MenuItem("TypeScript REST Endpoints") {
                regexFragment.loadPresetById("typescript-endpoints")
            })
            .addItem(MenuItem("JSON Package Versions") {
                regexFragment.loadPresetById("json-dependencies")
            })
            .addItem(MenuItem("CSS Design Tokens & Hex Colors") {
                regexFragment.loadPresetById("css-theme-tokens")
            })

        return MenuBar()
            .addMenu(fileMenu)
            .addMenu(languageMenu)
            .addMenu(presetsMenu)
    }
}
