package com.nexustools.genericui.examples.visualizer

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.visualizer.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] hosting [AudioSpectrumVisualizerFragment].
 */
@AppApplication
class AudioSpectrumVisualizerApp(
    val visualizerFragment: AudioSpectrumVisualizerFragment = AudioSpectrumVisualizerFragment()
) : FragmentApplication(
    initialFragment = visualizerFragment,
    title = "GenericUI — Media Player, Codec Inspector & FFT Spectrum Visualizer",
    initialThemeColor = "#10B981"
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
        visualizerFragment.setTitleBar(titleBar)
        visualizerFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildMenuBar(): MenuBar {
        val transportMenu = Menu("Transport")
            .addItem(MenuItem("Play / Pause", shortcutText = "Space") {
                visualizerFragment.togglePlayPause()
            })
            .addItem(MenuItem("Synthesize Test Chord", shortcutText = "Ctrl+T") {
                visualizerFragment.synthesizeTestChord()
            })
            .addItem(MenuItem("Seek +5s") {
                visualizerFragment.seekToSeconds(visualizerFragment.playbackPositionSeconds + 5.0)
            })
            .addItem(MenuItem("Reset Transport") {
                visualizerFragment.resetTransport()
            })
            .addSeparator()
            .addItem(MenuItem("Exit") {
                exit(0)
            })

        val modeMenu = Menu("Visualizer")
            .addItem(MenuItem("64-Band FFT Spectrum Bars") {
                visualizerFragment.switchVisualizerMode(VisualizerDisplayMode.FFT_SPECTRUM_BARS)
            })
            .addItem(MenuItem("Oscilloscope Time-Domain Waveform") {
                visualizerFragment.switchVisualizerMode(VisualizerDisplayMode.OSCILLOSCOPE_WAVEFORM)
            })
            .addItem(MenuItem("Radial Energy Rings") {
                visualizerFragment.switchVisualizerMode(VisualizerDisplayMode.RADIAL_ENERGY_RINGS)
            })

        return MenuBar()
            .addMenu(transportMenu)
            .addMenu(modeMenu)
    }
}
