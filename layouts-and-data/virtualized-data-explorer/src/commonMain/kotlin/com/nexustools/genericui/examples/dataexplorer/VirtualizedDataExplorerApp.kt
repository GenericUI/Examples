package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.dataexplorer.res.Resource

/**
 * Multiplatform [FragmentApplication] hosting [VirtualizedDataExplorerFragment].
 */
@AppApplication
class VirtualizedDataExplorerApp(
    val explorerFragment: VirtualizedDataExplorerFragment = VirtualizedDataExplorerFragment()
) : FragmentApplication(
    initialFragment = explorerFragment,
    title = "GenericUI — Virtualized Data Explorer",
    initialThemeColor = "#2563EB"
) {
    init {
        setThemeMode(ThemeMode.AUTO)
        setThemeColor("#2563EB")
        addStyleSheet(Resource.style.styles)
    }
}

