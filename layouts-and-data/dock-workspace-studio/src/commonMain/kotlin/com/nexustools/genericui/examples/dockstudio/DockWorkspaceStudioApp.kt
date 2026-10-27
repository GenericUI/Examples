package com.nexustools.genericui.examples.dockstudio

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.dockstudio.styles.StylesPrecompiledStyleSheet

/**
 * Multiplatform [FragmentApplication] hosting [DockWorkspaceStudioFragment].
 */
@AppApplication
class DockWorkspaceStudioApp(
    val studioFragment: DockWorkspaceStudioFragment = DockWorkspaceStudioFragment()
) : FragmentApplication(
    initialFragment = studioFragment,
    title = "GenericUI — Dock Workspace Studio",
    initialThemeColor = "#0EA5E9"
) {
    init {
        setTheme(ThemeMode.AUTO)
        setThemeColor("#0EA5E9")
        addStyleSheet(StylesPrecompiledStyleSheet)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }
}

