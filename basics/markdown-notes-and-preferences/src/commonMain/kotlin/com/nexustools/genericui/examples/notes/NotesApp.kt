package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.notes.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] entry point for the Markdown Notes & Preferences example (`1.3`).
 */
@AppApplication
class NotesApp(
    private val customPreferenceStore: PreferenceStore? = null,
    val notesFragment: NotesFragment = NotesFragment(customPreferenceStore)
) : FragmentApplication(
    initialFragment = notesFragment,
    title = "GenericUI — Markdown Notes & Preferences",
    initialThemeColor = "#8B5CF6"
) {

    override fun getPreferenceStore(): PreferenceStore =
        customPreferenceStore ?: super.getPreferenceStore()

    val preferenceStore: PreferenceStore
        get() = getPreferenceStore()

    init {
        setThemeMode(ThemeMode.AUTO)
        setThemeColor("#8B5CF6")
        addStyleSheet(StylesPrecompiledStyleSheet)
        val titleBar = TitleBar(
            embedMenuBar = false,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        notesFragment.setTitleBar(titleBar)
        mainWindow.setTitleBar(titleBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }
}
