package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point for the Virtualized Data Explorer example using
 * [JestElectricRunner.runVisualAppTestMain].
 */
object VirtualizedDataExplorerVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Virtualized Data Explorer Visual Suite",
            args = args
        ) {
            describe("10,000-Row Virtualized Data Explorer Visual Verification") {
                it("loads 10,000 items with lazy window bounds, scrolls, switches modes, and filters") {
                    val controller = launchApp { VirtualizedDataExplorerApp() }
                    val fragment = controller.app.explorerFragment

                    expect(fragment.allRecords.size).toBe(10_000)
                    expectNode(controller.findById("badge-total-items")).toContainText("10000 / 10000")

                    controller.findById("btn-scroll-mid").performClick()
                    expect(fragment.getActiveViewportWindow().containsActive(5000)).toBe(true)

                    controller.findById("btn-mode-grid").performClick()
                    expectNode(controller.findById("badge-active-mode")).toHaveText("Mode: GRID")

                    controller.findById("btn-mode-tree").performClick()
                    expectNode(controller.findById("badge-active-mode")).toHaveText("Mode: TREE")

                    controller.findById("btn-mode-list").performClick()
                    controller.findById("btn-scroll-top").performClick()
                    controller.findById("select-category").performSelectValue("Storage")
                    expect(fragment.getFilteredRecords().size).toBe(2_000)
                }
            }
        }
}
