package com.nexustools.genericui.examples.filterlab

import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Interactive visual test runner entry point that opens a real platform window
 * on Windows / JVM and steps through the filter presets and sliders live.
 */
object GpuImageFilterLabVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "GPU Image Filter Lab Visual Suite",
            args = args
        ) {
            describe("GPU Filter Pipeline Presets, Sliders, and Histogram") {
                it("cycles through Vivid, Noir, Brightness, Blur, and Reset") {
                    val controller = launchApp { GpuImageFilterLabApp() }
                    val fragment = controller.app.filterLabFragment

                    controller.findById("btn-preset-vivid").performClick()
                    expectNode(controller.findById("badge-preset")).toHaveText("Preset: Vivid")
                    fragment.previewSurface.dispatchVBlankFrame(16.0)
                    fragment.histogramSurface.dispatchVBlankFrame(16.0)

                    controller.findById("btn-preset-noir").performClick()
                    expectNode(controller.findById("badge-preset")).toHaveText("Preset: Noir")
                    fragment.previewSurface.dispatchVBlankFrame(32.0)
                    fragment.histogramSurface.dispatchVBlankFrame(32.0)

                    controller.findById("slider-brightness").performSetSliderValue(0.35)
                    controller.findById("slider-blur").performSetSliderValue(3.0)
                    fragment.previewSurface.dispatchVBlankFrame(48.0)
                    fragment.histogramSurface.dispatchVBlankFrame(48.0)

                    controller.findById("btn-preset-reset").performClick()
                    expectNode(controller.findById("badge-preset")).toHaveText("Preset: Default")
                }
            }
        }
}
