package com.nexustools.genericui.examples.dockstudio

import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.dock.DockPosition
import com.nexustools.genericui.enums.dock.StudioWindowMode
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Cross-platform [JestElectricRunner] test suite for [DockWorkspaceStudioApp] and
 * [DockWorkspaceStudioFragment].
 */
class DockWorkspaceStudioJestTest {

    @Test
    fun runDockWorkspaceStudioSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Dock Workspace Studio JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Dock Areas, DockPanels & Panel TitleBars") {
                it("mounts LEFT Toolbox, RIGHT Properties, BOTTOM Console, and Floating HUD DockPanels with TitleBars") {
                    val controller = launchApp(visualOnWindows = false) { DockWorkspaceStudioApp() }
                    val fragment = controller.app.studioFragment

                    expectComponent(fragment.toolboxPanel).toBeVisible()
                    expectComponent(fragment.propertiesPanel).toBeVisible()
                    expectComponent(fragment.consolePanel).toBeVisible()
                    expectComponent(fragment.hudOverlayPanel).toBeVisible()

                    expect(fragment.toolboxPanel.preferredDockPosition).toBe(DockPosition.LEFT)
                    expect(fragment.propertiesPanel.preferredDockPosition).toBe(DockPosition.RIGHT)
                    expect(fragment.consolePanel.preferredDockPosition).toBe(DockPosition.BOTTOM)

                    expect(fragment.toolboxPanel.getTitleBar()).toBeNotNull()
                    expect(fragment.propertiesPanel.getTitleBar()).toBeNotNull()
                    expect(fragment.consolePanel.getTitleBar()).toBeNotNull()

                    // Select tools in LEFT Toolbox DockPanel
                    controller.findById("btn-tool-brush").performClick()
                    expect(fragment.activeToolName).toBe("Brush")
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: Brush")
                }
            }

            describe("Document Tab Management (Open, Switch, Inspect, Close)") {
                it("opens new StudioDocument tabs, switches active tabs, updates properties, and closes tabs") {
                    val controller = launchApp(visualOnWindows = false) { DockWorkspaceStudioApp() }
                    val fragment = controller.app.studioFragment

                    expect(fragment.documents.size).toBe(2)
                    expectNode(controller.findById("badge-open-tabs")).toHaveText("Tabs: 2")
                    expectNode(controller.findById("label-prop-doc-title")).toHaveText("Document: Hero Banner.gul")
                    expectNode(controller.findById("label-prop-layers")).toHaveText("Layers: 6")

                    // Adjust zoom and add a layer on doc-1
                    controller.findById("btn-zoom-in").performClick()
                    expectNode(controller.findById("label-prop-zoom")).toHaveText("Zoom: 125%")

                    controller.findById("btn-add-layer").performClick()
                    expectNode(controller.findById("label-prop-layers")).toHaveText("Layers: 7")

                    // Switch to doc-2 and verify inspector updates
                    fragment.activateDocument("doc-2")
                    expectNode(controller.findById("label-prop-doc-title")).toHaveText("Document: Icon Sheet.gul")
                    expectNode(controller.findById("label-prop-layers")).toHaveText("Layers: 12")

                    // Open a 3rd document tab via toolbar button
                    controller.findById("btn-new-doc-tab").performClick()
                    expect(fragment.documents.size).toBe(3)
                    expectNode(controller.findById("badge-open-tabs")).toHaveText("Tabs: 3")
                    expectNode(controller.findById("label-prop-doc-title")).toHaveText("Document: Untitled-3.gul")

                    // Close the active document tab (Untitled-3.gul)
                    controller.findById("btn-close-active-doc").performClick()
                    expect(fragment.documents.size).toBe(2)
                    expectNode(controller.findById("badge-open-tabs")).toHaveText("Tabs: 2")
                }
            }

            describe("SINGLE_WINDOW vs MULTI_WINDOW Mode & Floating HUD Overlay") {
                it("toggles between SINGLE_WINDOW and MULTI_WINDOW mode and detaches/re-attaches PanelWindows") {
                    val controller = launchApp(visualOnWindows = false) { DockWorkspaceStudioApp() }
                    val fragment = controller.app.studioFragment

                    expect(fragment.windowMode).toBe(StudioWindowMode.SINGLE_WINDOW_MODE)
                    expectNode(controller.findById("badge-window-mode")).toHaveText("SINGLE_WINDOW")
                    expect(fragment.toolboxPanel.isFloating).toBe(false)

                    // Switch to MULTI_WINDOW_MODE
                    controller.findById("btn-toggle-window-mode").performClick()
                    expect(fragment.windowMode).toBe(StudioWindowMode.MULTI_WINDOW_MODE)
                    expectNode(controller.findById("badge-window-mode")).toHaveText("MULTI_WINDOW")
                    expect(fragment.toolboxPanel.isFloating).toBe(true)
                    expect(fragment.propertiesPanel.isFloating).toBe(true)
                    expect(fragment.dockWorkspace.panelWindows.isNotEmpty()).toBe(true)

                    // Switch back to SINGLE_WINDOW_MODE
                    controller.findById("btn-toggle-window-mode").performClick()
                    expect(fragment.windowMode).toBe(StudioWindowMode.SINGLE_WINDOW_MODE)
                    expectNode(controller.findById("badge-window-mode")).toHaveText("SINGLE_WINDOW")
                    expect(fragment.toolboxPanel.isFloating).toBe(false)
                    expect(fragment.propertiesPanel.isFloating).toBe(false)
                }

                it("toggles the floating HUD overlay panel visibility") {
                    val controller = launchApp(visualOnWindows = false) { DockWorkspaceStudioApp() }
                    val fragment = controller.app.studioFragment

                    expect(fragment.isHudOverlayVisible).toBe(true)
                    expectComponent(fragment.hudOverlayPanel).toBeVisible()
                    expectNode(controller.findById("badge-hud-status")).toHaveText("HUD: VISIBLE")

                    controller.findById("btn-toggle-hud-overlay").performClick()
                    expect(fragment.isHudOverlayVisible).toBe(false)
                    expectComponent(fragment.hudOverlayPanel).toBeHidden()
                    expectNode(controller.findById("badge-hud-status")).toHaveText("HUD: HIDDEN")

                    controller.findById("btn-toggle-hud-overlay").performClick()
                    expect(fragment.isHudOverlayVisible).toBe(true)
                    expectComponent(fragment.hudOverlayPanel).toBeVisible()
                    expectNode(controller.findById("badge-hud-status")).toHaveText("HUD: VISIBLE")

                    // Verify ComponentBootstrap, ComponentStyle, and strict CSS class isolation
                    expect(controller.app.getTheme()).toBe(ThemeMode.AUTO)
                    expect(fragment.newDocTabButton.getBootstrap().type).toBe(BootstrapType.PRIMARY)
                    expect(fragment.newDocTabButton.getBootstrap().size).toBe(BootstrapSize.SM)
                    expect(fragment.newDocTabButton.hasClass("btn-primary")).toBe(false)
                    expect(fragment.closeActiveDocButton.getBootstrap().outline).toBe(true)
                    expect(fragment.newDocTabButton.getStyle().accentColor).toBe(RgbaColor.fromHex("#0EA5E9"))
                }
            }
        }
        report.assertAllPassed()
    }
}
