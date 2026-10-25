package com.nexustools.genericui.examples.filterlab

import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.math.abs
import kotlin.test.Test

/**
 * Cross-platform unit and UI component tests for [GpuImageFilterLabApp] and
 * [GpuImageFilterLabFragment] powered by [JestElectricRunner].
 */
class GpuImageFilterLabJestTest {

    @Test
    fun filterLabPipelineAndHistogramSuitePasses() {
        val report = JestElectricRunner.runSuite(
            suiteName = "GpuImageFilterLab Jest Suite",
            visualOnWindows = false
        ) {
            describe("Initial procedural image and histogram state") {
                it("mounts controls and computes initial 256-bin histogram") {
                    val controller = launchApp(visualOnWindows = false) { GpuImageFilterLabApp() }
                    val fragment = controller.app.filterLabFragment

                    expectNode(controller.findById("lbl-title")).toHaveText("GPU Image Filter Lab")
                    expectNode(controller.findById("badge-preset")).toHaveText("Preset: Default")
                    expect(fragment.histogram.totalPixels).toBe(160 * 120)
                    expect(fragment.meanLuminance > 40f).toBe(true)
                }
            }

            describe("Brightness, saturation, and Gaussian blur filter adjustments") {
                it("shifts mean luminance when brightness slider increases") {
                    val controller = launchApp(visualOnWindows = false) { GpuImageFilterLabApp() }
                    val fragment = controller.app.filterLabFragment

                    val baselineLuma = fragment.meanLuminance
                    controller.findById("slider-brightness").performSetSliderValue(0.45)

                    val brightenedLuma = fragment.meanLuminance
                    expect(brightenedLuma > baselineLuma + 25f).toBe(true)
                    expectNode(controller.findById("badge-preset")).toHaveText("Preset: Custom")
                }

                it("desaturates pixels to grayscale when Noir preset is clicked") {
                    val controller = launchApp(visualOnWindows = false) { GpuImageFilterLabApp() }
                    val fragment = controller.app.filterLabFragment

                    controller.findById("btn-preset-noir").performClick()
                    expectNode(controller.findById("badge-preset")).toHaveText("Preset: Noir")

                    // Sample center pixel to verify R, G, B channels are equalized by saturation = 0
                    val cx = fragment.processedImage.width / 2
                    val cy = fragment.processedImage.height / 2
                    val centerPixel = fragment.processedImage.getPixel(cx, cy)
                    expect(abs(centerPixel.r - centerPixel.g) <= 2).toBe(true)
                    expect(abs(centerPixel.g - centerPixel.b) <= 2).toBe(true)
                }

                it("applies Gaussian blur and resets back to baseline") {
                    val controller = launchApp(visualOnWindows = false) { GpuImageFilterLabApp() }
                    val fragment = controller.app.filterLabFragment
                    val baselineLuma = fragment.meanLuminance

                    controller.findById("slider-blur").performSetSliderValue(4.0)
                    expect(fragment.filterState.blurRadius).toBe(4)

                    // Render preview and histogram onto Canvas2DExtSurface
                    fragment.previewSurface.dispatchVBlankFrame(16.0)
                    fragment.histogramSurface.dispatchVBlankFrame(16.0)
                    val previewBuffer = fragment.previewSurface.toImageBuffer()
                    expect(previewBuffer.getPixel(40, 40).a).toBe(255)

                    // Reset restores default state and baseline mean luminance
                    controller.findById("btn-preset-reset").performClick()
                    expect(fragment.filterState.blurRadius).toBe(0)
                    expect(abs(fragment.meanLuminance - baselineLuma) < 0.01f).toBe(true)
                }
            }
        }

        report.assertAllPassed()
    }
}
