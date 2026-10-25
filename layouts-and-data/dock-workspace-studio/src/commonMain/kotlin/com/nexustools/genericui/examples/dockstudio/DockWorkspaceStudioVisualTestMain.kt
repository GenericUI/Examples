package com.nexustools.genericui.examples.dockstudio

import com.nexustools.genericui.enums.dock.StudioWindowMode
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point for the Dock Workspace Studio example using
 * [JestElectricRunner.runVisualAppTestMain].
 */
object DockWorkspaceStudioVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Dock Workspace Studio Visual Suite",
            args = args
        ) {
            describe("Dock Panels, Document Tabs, Window Mode & HUD Overlay Visual Verification") {
                it("opens document tabs, toggles SINGLE_WINDOW and MULTI_WINDOW mode, and toggles HUD overlay") {
                    val controller = launchApp { DockWorkspaceStudioApp() }
                    val fragment = controller.app.studioFragment

                    expectNode(controller.findById("badge-window-mode")).toHaveText("SINGLE_WINDOW")
                    expectNode(controller.findById("badge-open-tabs")).toHaveText("Tabs: 2")

                    controller.findById("btn-new-doc-tab").performClick()
                    expectNode(controller.findById("badge-open-tabs")).toHaveText("Tabs: 3")

                    controller.findById("btn-toggle-window-mode").performClick()
                    expect(fragment.windowMode).toBe(StudioWindowMode.MULTI_WINDOW_MODE)
                    expectNode(controller.findById("badge-window-mode")).toHaveText("MULTI_WINDOW")

                    controller.findById("btn-toggle-window-mode").performClick()
                    expect(fragment.windowMode).toBe(StudioWindowMode.SINGLE_WINDOW_MODE)
                    expectNode(controller.findById("badge-window-mode")).toHaveText("SINGLE_WINDOW")

                    controller.findById("btn-toggle-hud-overlay").performClick()
                    expectComponent(fragment.hudOverlayPanel).toBeHidden()

                    controller.findById("btn-toggle-hud-overlay").performClick()
                    expectComponent(fragment.hudOverlayPanel).toBeVisible()
                }
            }
        }
}
