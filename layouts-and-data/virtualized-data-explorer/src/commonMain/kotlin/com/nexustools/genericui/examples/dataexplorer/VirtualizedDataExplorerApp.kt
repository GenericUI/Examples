package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.component.shell.FragmentApplication

/**
 * Multiplatform [FragmentApplication] hosting [VirtualizedDataExplorerFragment].
 */
class VirtualizedDataExplorerApp(
    val explorerFragment: VirtualizedDataExplorerFragment = VirtualizedDataExplorerFragment()
) : FragmentApplication(
    initialFragment = explorerFragment,
    title = "GenericUI — Virtualized Data Explorer (10,000 Rows)",
    initialThemeColor = "#2563EB"
)

fun main(args: Array<String>) {
    if (args.contains("--visual-test")) {
        VirtualizedDataExplorerVisualTestMain.runVisualSuite(args)
        return
    }
    VirtualizedDataExplorerApp().run()
}
