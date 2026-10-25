package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Cross-platform [JestElectricRunner] test suite for [VirtualizedDataExplorerApp] and
 * [VirtualizedDataExplorerFragment].
 */
class VirtualizedDataExplorerJestTest {

    @Test
    fun runVirtualizedDataExplorerSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Virtualized Data Explorer JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("10,000-Row Dataset Generation & Viewport Window Virtualization") {
                it("loads 10,000 records while binding only the active viewport window") {
                    val controller = launchApp(visualOnWindows = false) { VirtualizedDataExplorerApp() }
                    val fragment = controller.app.explorerFragment

                    expect(fragment.allRecords.size).toBe(10_000)
                    expectNode(controller.findById("badge-total-items")).toHaveText("Total: 10000 / 10000")

                    val initialWindow = fragment.getActiveViewportWindow()
                    expect(initialWindow.firstVisibleIndex).toBe(0)
                    expect(initialWindow.lastVisibleIndex).toBeGreaterThan(5)
                    expect(fragment.lazyList.loadedItemCount).toBeLessThan(30)

                    // First item is bound in the tree, while item #5000 is not materialized yet
                    expectNode(controller.findById("record-row-1")).toExist()
                    expectNode(controller.findById("record-row-5000")).not.toExist()
                }

                it("recycles off-screen ViewHolders when scrolling to the middle and end of the 10,000 items") {
                    val controller = launchApp(visualOnWindows = false) { VirtualizedDataExplorerApp() }
                    val fragment = controller.app.explorerFragment

                    controller.findById("btn-scroll-mid").performClick()
                    val midWindow = fragment.getActiveViewportWindow()
                    expect(midWindow.containsActive(5000)).toBe(true)
                    expect(fragment.totalUnloadEvents).toBeGreaterThan(0)
                    expectNode(controller.findById("record-row-1")).not.toExist()
                    expectNode(controller.findById("record-row-5001")).toExist()

                    controller.findById("btn-scroll-bottom").performClick()
                    val endWindow = fragment.getActiveViewportWindow()
                    expect(endWindow.containsActive(9999)).toBe(true)
                    expectNode(controller.findById("record-row-10000")).toExist()

                    controller.findById("btn-scroll-top").performClick()
                    expectNode(controller.findById("record-row-1")).toExist()
                }
            }

            describe("Mode Switching (LazyList, LazyGrid, TreeView) & Item Selection") {
                it("switches cleanly between List, Grid, and Tree modes and updates telemetry badges") {
                    val controller = launchApp(visualOnWindows = false) { VirtualizedDataExplorerApp() }
                    val fragment = controller.app.explorerFragment

                    expect(fragment.activeMode).toBe(ExplorerViewMode.LIST)
                    expectComponent(fragment.lazyList).toBeVisible()
                    expectComponent(fragment.lazyGrid).toBeHidden()
                    expectComponent(fragment.treeView).toBeHidden()

                    controller.findById("btn-mode-grid").performClick()
                    expect(fragment.activeMode).toBe(ExplorerViewMode.GRID)
                    expectNode(controller.findById("badge-active-mode")).toHaveText("Mode: GRID")
                    expectComponent(fragment.lazyList).toBeHidden()
                    expectComponent(fragment.lazyGrid).toBeVisible()
                    expectNode(controller.findById("record-card-1")).toExist()

                    controller.findById("btn-mode-tree").performClick()
                    expect(fragment.activeMode).toBe(ExplorerViewMode.TREE)
                    expectNode(controller.findById("badge-active-mode")).toHaveText("Mode: TREE")
                    expectComponent(fragment.lazyGrid).toBeHidden()
                    expectComponent(fragment.treeView).toBeVisible()
                    expect(fragment.treeView.rootNodes.size).toBe(5)

                    controller.findById("btn-mode-list").performClick()
                    expect(fragment.activeMode).toBe(ExplorerViewMode.LIST)
                    expectComponent(fragment.lazyList).toBeVisible()
                }

                it("selects a bound dataset item and updates badge-selected-item") {
                    val controller = launchApp(visualOnWindows = false) { VirtualizedDataExplorerApp() }
                    expectNode(controller.findById("badge-selected-item")).toHaveText("Selected: None")

                    controller.findById("btn-select-record-1").performClick()
                    expectNode(controller.findById("badge-selected-item"))
                        .toHaveText("Selected: #1 Node-00001 (Compute)")
                }
            }

            describe("Live Search & Category Filtering") {
                it("filters the 10,000 records via performTypeText and performSelectValue and resets cleanly") {
                    val controller = launchApp(visualOnWindows = false) { VirtualizedDataExplorerApp() }
                    val fragment = controller.app.explorerFragment

                    controller.findById("select-category").performSelectValue("Network")
                    expect(fragment.getFilteredRecords().size).toBe(2_000)
                    expectNode(controller.findById("badge-total-items")).toHaveText("Total: 2000 / 10000")

                    controller.findById("input-filter").performTypeText("Node-00003")
                    expect(fragment.getFilteredRecords().size).toBe(1)
                    expectNode(controller.findById("badge-total-items")).toHaveText("Total: 1 / 10000")

                    controller.findById("btn-select-record-3").performClick()
                    expectNode(controller.findById("badge-selected-item"))
                        .toHaveText("Selected: #3 Node-00003 (Network)")

                    controller.findById("btn-clear-filter").performClick()
                    expect(fragment.getFilteredRecords().size).toBe(10_000)
                    expectNode(controller.findById("badge-total-items")).toHaveText("Total: 10000 / 10000")
                }
            }
        }
        report.assertAllPassed()
    }
}
