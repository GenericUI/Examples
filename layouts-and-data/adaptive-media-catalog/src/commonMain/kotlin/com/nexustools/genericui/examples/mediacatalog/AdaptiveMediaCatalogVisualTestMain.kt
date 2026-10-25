package com.nexustools.genericui.examples.mediacatalog

import com.nexustools.genericui.component.fragment.helper.AdaptiveSplitTabFragment
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point for the Adaptive Media Catalog example using
 * [JestElectricRunner.runVisualAppTestMain].
 */
object AdaptiveMediaCatalogVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Adaptive Media Catalog Visual Suite",
            args = args
        ) {
            describe("Responsive Layout Posture & Transport Visual Verification") {
                it("transitions across Desktop Split, Phone Landscape Fullscreen, and Phone Portrait Tabs") {
                    val controller = launchApp { AdaptiveMediaCatalogApp() }
                    val fragment = controller.app.catalogFragment

                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.TABLET_DESKTOP_COLUMNS)

                    controller.findById("btn-posture-phone-landscape").performClick()
                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.PHONE_LANDSCAPE_FULLSCREEN)

                    controller.findById("btn-posture-phone-portrait").performClick()
                    expect(fragment.getActiveLayoutMode())
                        .toBe(AdaptiveSplitTabFragment.LayoutMode.PORTRAIT_SINGLE_FLOW_TABS)

                    controller.findById("btn-posture-desktop").performClick()
                    controller.findById("btn-select-channel-ch-kotlin-conf").performClick()
                    expectNode(controller.findById("label-now-playing-title"))
                        .toHaveText("Multiplatform UI Architecture")
                }
            }
        }
}
