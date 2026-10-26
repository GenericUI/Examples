package com.nexustools.genericui.examples.dockstudio

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication

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
)

