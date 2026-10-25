package com.nexustools.genericui.examples.visualizer

import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point using [JestElectricRunner.runVisualAppTestMain] for the
 * Audio Spectrum Visualizer & Media Codec Inspector example.
 */
object AudioSpectrumVisualizerVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Audio Spectrum Visualizer Visual Suite",
            args = args
        ) {
            describe("Media Transport, FFT Energy Bands & Subtitle Synchronization") {
                it("synthesizes test chord frames, updates Bass/Mid/Treble energy badges, and switches visualizer modes") {
                    val controller = launchApp(visualOnWindows = visualOnWindows) {
                        AudioSpectrumVisualizerApp()
                    }

                    expectNode(controller.findById("badge-codec-backend")).toContainText("Vulkan")
                    expectNode(controller.findById("btn-synth-chord")).performClick()

                    val bands = controller.app.visualizerFragment.audioAnalyzer.computeEnergyBands()
                    expect(bands.bass).toBeGreaterThan(0.5f)
                    expect(bands.mid).toBeGreaterThan(0.4f)
                    expect(bands.treble).toBeGreaterThan(0.3f)

                    expectNode(controller.findById("badge-bass")).toContainText("Bass:")
                    expectNode(controller.findById("badge-mid")).toContainText("Mid:")
                    expectNode(controller.findById("badge-treble")).toContainText("Treble:")

                    controller.findById("btn-play-pause").performClick()
                    expectNode(controller.findById("badge-playback-state")).toHaveText("PLAYING")
                    expectNode(controller.findById("badge-subtitle-cue")).toContainText("Welcome to GenericUI")

                    controller.findById("btn-seek-forward").performClick()
                    expectNode(controller.findById("badge-subtitle-cue")).toContainText("Real-time 64-band FFT")

                    controller.findById("select-viz-mode")
                        .performSelectValue(VisualizerDisplayMode.RADIAL_ENERGY_RINGS.name)
                    expectNode(controller.findById("badge-active-viz-mode"))
                        .toHaveText(VisualizerDisplayMode.RADIAL_ENERGY_RINGS.displayName)

                    controller.destroy()
                }
            }
        }
}
