package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.component.shell.MdiWindowState
import com.nexustools.genericui.style.RgbaColor
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point using [JestElectricRunner.runVisualAppTestMain] for the
 * MDI Component & Live SCSS Stylesheet Showcase example.
 */
object MdiComponentAndScssShowcaseVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "MDI Component & Live SCSS Stylesheet Showcase Visual Suite",
            args = args
        ) {
            describe("MDI Component & Live Rainbow SCSS Stylesheet Showcase") {
                it("mounts MdiContainer with 6 MDI windows and applies default Rainbow Prism SCSS strictly inside MDI") {
                    val controller = launchApp(visualOnWindows = visualOnWindows) {
                        MdiComponentAndScssShowcaseApp()
                    }
                    val frag = controller.app.showcaseFragment

                    expect(frag.mdiContainer.windows.size).toBe(6)
                    expect(frag.activePreset).toBe(ScssThemePreset.RAINBOW_PRISM)
                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#ff2a6d"))
                    expect(frag.compatH2.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#ff7b00"))
                    expect(frag.demoPrimaryButton.htmlTag).toBe("button")
                    expect(frag.demoPrimaryButton.computedStyle?.backgroundGradient).toBeNotNull()

                    // Verify outer components outside MdiContainer are unaffected by MDI SCSS
                    expect(frag.scssEditor.computedStyle?.backgroundGradient).toBeNull()
                    expect(frag.outerMenuBar.computedStyle?.backgroundGradient).toBeNull()

                    // Minimizing, maximizing, restoring, tiling, and cascading MDI windows
                    frag.htmlCompatWindow.maximize()
                    expect(frag.htmlCompatWindow.windowState).toBe(MdiWindowState.MAXIMIZED)
                    frag.htmlCompatWindow.restore()
                    expect(frag.htmlCompatWindow.windowState).toBe(MdiWindowState.NORMAL)
                    frag.mdiContainer.tileWindows()
                    frag.mdiContainer.cascadeWindows()

                    controller.destroy()
                }

                it("switches to Stylesheet Editor tab, switches presets, and live-edits SCSS") {
                    val controller = launchApp(visualOnWindows = visualOnWindows) {
                        MdiComponentAndScssShowcaseApp()
                    }
                    val frag = controller.app.showcaseFragment

                    controller.findById("btn-switch-scss-tab").performClick()
                    expectNode(controller.findById("badge-active-main-tab")).toHaveText("Stylesheet Editor (SCSS)")

                    controller.findById("btn-preset-emerald").performClick()
                    expect(frag.activePreset).toBe(ScssThemePreset.EMERALD_STUDIO)
                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#34d399"))

                    controller.destroy()
                }
            }
        }
}
