package com.nexustools.genericui.examples.dockstudio

import com.nexustools.genericui.component.shell.FragmentApplication

/**
 * Multiplatform [FragmentApplication] hosting [DockWorkspaceStudioFragment].
 */
class DockWorkspaceStudioApp(
    val studioFragment: DockWorkspaceStudioFragment = DockWorkspaceStudioFragment()
) : FragmentApplication(
    initialFragment = studioFragment,
    title = "GenericUI — Dock Workspace Studio",
    initialThemeColor = "#0EA5E9"
)

fun main(args: Array<String>) {
    if (args.contains("--visual-test")) {
        DockWorkspaceStudioVisualTestMain.runVisualSuite(args)
        return
    }
    DockWorkspaceStudioApp().run()
}
