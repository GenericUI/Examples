package com.nexustools.genericui.examples.filterlab

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.filterlab.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multi-backend [FragmentApplication] entry point for the GPU Image Filter Lab example application.
 */
@AppApplication
class GpuImageFilterLabApp(
    val filterLabFragment: GpuImageFilterLabFragment = GpuImageFilterLabFragment()
) : FragmentApplication(
    initialFragment = filterLabFragment,
    title = "Offscreen GPU Image Filter & Histogram Lab",
    initialThemeColor = "#8B5CF6"
) {
    init {
        setThemeMode(ThemeMode.DARK)
        val titleBar = TitleBar(
            embedMenuBar = false,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        filterLabFragment.setTitleBar(titleBar)
        mainWindow.setTitleBar(titleBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }
}
