package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication

/**
 * Multiplatform [FragmentApplication] hosting [VirtualizedDataExplorerFragment].
 */
@AppApplication
class VirtualizedDataExplorerApp(
    val explorerFragment: VirtualizedDataExplorerFragment = VirtualizedDataExplorerFragment()
) : FragmentApplication(
    initialFragment = explorerFragment,
    title = "GenericUI — Virtualized Data Explorer (10,000 Rows)",
    initialThemeColor = "#2563EB"
)

