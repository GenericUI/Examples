package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.notes.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] entry point for the Markdown Notes & Preferences example (`1.3`).
 */
class NotesApp(
    val preferenceStore: PreferenceStore = PreferenceStore(NoteItem.PREFERENCE_NAMESPACE),
    val notesFragment: NotesFragment = NotesFragment(preferenceStore)
) : FragmentApplication(
    initialFragment = notesFragment,
    title = "GenericUI — Markdown Notes & Preferences",
    initialThemeColor = "#8B5CF6"
) {

    init {
        setThemeColor("#8B5CF6")
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

    companion object {
        fun start(args: Array<String> = emptyArray()) {
            NotesPlatformBootstrap.initialize(args)
            val app = NotesApp()
            app.run()
        }
    }
}

/**
 * Standard multiplatform `main(args)` entry point.
 */
fun main(args: Array<String>) {
    NotesApp.start(args)
}
