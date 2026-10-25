package com.nexustools.genericui.examples.mediacatalog

import com.nexustools.genericui.component.fragment.helper.AdaptiveSplitTabFragment
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Cross-platform [JestElectricRunner] test suite for [AdaptiveMediaCatalogApp] and
 * [AdaptiveMediaCatalogFragment].
 */
class AdaptiveMediaCatalogJestTest {

    @Test
    fun runAdaptiveMediaCatalogSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Adaptive Media Catalog JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Adaptive Layout Mode Transitions (Desktop, Phone Landscape, Phone Portrait)") {
                it("switches layout postures cleanly while preserving the primary media stage") {
                    val controller = launchApp(visualOnWindows = false) { AdaptiveMediaCatalogApp() }
                    val fragment = controller.app.catalogFragment

                    // Initial Desktop / Tablet Landscape posture (1280x800)
                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.TABLET_DESKTOP_COLUMNS)
                    expectNode(controller.findById("badge-layout-mode"))
                        .toHaveText("TABLET_DESKTOP_COLUMNS")
                    expect(fragment.isFullscreen()).toBe(false)

                    // Switch to Phone Landscape posture (800x390 -> immersive fullscreen primary view)
                    controller.findById("btn-posture-phone-landscape").performClick()
                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.PHONE_LANDSCAPE_FULLSCREEN)
                    expectNode(controller.findById("badge-layout-mode"))
                        .toHaveText("PHONE_LANDSCAPE_FULLSCREEN")
                    expectNode(controller.findById("badge-viewport-size"))
                        .toHaveText("800x390")
                    expect(fragment.isFullscreen()).toBe(true)

                    // Switch to Phone Portrait posture (390x844 -> vertical stack with tabs)
                    controller.findById("btn-posture-phone-portrait").performClick()
                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.PORTRAIT_SINGLE_FLOW_TABS)
                    expectNode(controller.findById("badge-layout-mode"))
                        .toHaveText("PORTRAIT_SINGLE_FLOW_TABS")
                    expectNode(controller.findById("badge-viewport-size"))
                        .toHaveText("390x844")
                    expect(fragment.isFullscreen()).toBe(false)

                    // Return to Desktop Split posture (1280x800)
                    controller.findById("btn-posture-desktop").performClick()
                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.TABLET_DESKTOP_COLUMNS)
                    expectNode(controller.findById("badge-viewport-size"))
                        .toHaveText("1280x800")
                }
            }

            describe("Channel Selection, Playback Queue & Transport Controls") {
                it("selects channels and updates Now Playing and Details views") {
                    val controller = launchApp(visualOnWindows = false) { AdaptiveMediaCatalogApp() }
                    val fragment = controller.app.catalogFragment

                    expectNode(controller.findById("label-now-playing-title"))
                        .toHaveText("Neon Horizon Live")

                    controller.findById("btn-select-channel-ch-kotlin-conf").performClick()
                    expect(fragment.selectedChannel.id).toBe("ch-kotlin-conf")
                    expectNode(controller.findById("label-now-playing-title"))
                        .toHaveText("Multiplatform UI Architecture")
                    expectNode(controller.findById("badge-now-playing-genre"))
                        .toHaveText("Engineering")
                    expectNode(controller.findById("label-detail-channel-id"))
                        .toContainText("ch-kotlin-conf")
                }

                it("manages the playback queue and toggles transport controls") {
                    val controller = launchApp(visualOnWindows = false) { AdaptiveMediaCatalogApp() }
                    val fragment = controller.app.catalogFragment

                    expect(fragment.playbackQueue.size).toBe(2)
                    expectNode(controller.findById("badge-queue-count")).toHaveText("Queue: 2")

                    controller.findById("btn-enqueue-channel-ch-lofi-studio").performClick()
                    expect(fragment.playbackQueue.size).toBe(3)
                    expectNode(controller.findById("badge-queue-count")).toHaveText("Queue: 3")
                    expectNode(controller.findById("label-queue-summary"))
                        .toContainText("Midnight Coding Beats")

                    // Toggle Play/Pause
                    expectNode(controller.findById("badge-transport-state")).toHaveText("PLAYING")
                    controller.findById("btn-transport-play-pause").performClick()
                    expect(fragment.isPlaying).toBe(false)
                    expectNode(controller.findById("badge-transport-state")).toHaveText("PAUSED")

                    controller.findById("btn-transport-play-pause").performClick()
                    expect(fragment.isPlaying).toBe(true)
                    expectNode(controller.findById("badge-transport-state")).toHaveText("PLAYING")

                    // Skip Next & Previous
                    controller.findById("btn-transport-next").performClick()
                    expect(fragment.selectedChannel.id).toBe("ch-kotlin-conf")

                    controller.findById("btn-transport-prev").performClick()
                    expect(fragment.selectedChannel.id).toBe("ch-synthwave")

                    // Clear Queue
                    controller.findById("btn-queue-clear").performClick()
                    expect(fragment.playbackQueue.size).toBe(0)
                    expectNode(controller.findById("badge-queue-count")).toHaveText("Queue: 0")
                    expectNode(controller.findById("label-queue-summary")).toHaveText("Queue is empty")
                }
            }
        }
        report.assertAllPassed()
    }
}
