package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.component.shell.MdiWindowState
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.dock.StudioWindowMode
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.mdishowcase.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.ext.codeeditor.CodeLanguage
import com.nexustools.genericui.ext.codeeditor.CursorPosition
import com.nexustools.genericui.ext.codeeditor.ScssLanguageSupport
import com.nexustools.genericui.ext.codeeditor.SyntaxTokenType
import com.nexustools.genericui.ext.codeeditor.SyntaxTokenizer
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Cross-platform [JestElectricRunner] BDD & Robolectric UI test suite for `mdi-component-and-scss-showcase`.
 */
class MdiComponentAndScssShowcaseJestTest {

    @Test
    fun runMdiComponentAndScssShowcaseSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "MDI Component & Live SCSS Stylesheet Showcase JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("MDI Component & Live SCSS Stylesheet Showcase") {

                it("mounts 2 main tabs, 6 category MdiWindows, and preloads Rainbow Prism SCSS scoped strictly to MdiContainer") {
                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val frag = controller.fragment

                    // Verify 2 main tabs and 6 MDI windows
                    expect(frag.mainTabContainer.tabs).toHaveLength(2)
                    expect(frag.mdiContainer.windows).toHaveLength(6)
                    expectNode(controller.findById("badge-active-main-tab")).toHaveText("Preview (MDI Showcase)")

                    val windowIds = frag.mdiContainer.windows.map { it.windowId }
                    expect(windowIds).toEqual(
                        listOf(
                            "mdi-win-htmlcompat",
                            "mdi-win-actions-nav",
                            "mdi-win-forms",
                            "mdi-win-surfaces-feedback",
                            "mdi-win-lazy-layouts",
                            "mdi-win-dock-workspace"
                        )
                    )

                    // Verify preloaded stylesheet is RAINBOW_PRISM
                    expect(frag.activePreset).toBe(ScssThemePreset.RAINBOW_PRISM)
                    expect(frag.scssEditor.getText().contains("\$rainbow-red: #ff2a6d;")).toBe(true)
                    expect(frag.scssEditor.getText().contains("genericui:mdicontainer")).toBe(true)
                    expectNode(controller.findById("badge-scss-error-count")).toHaveText("0 errors")

                    // Verify rainbow styles are applied to components INSIDE MdiContainer
                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#ff2a6d"))
                    expect(frag.compatH2.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#ff7b00"))
                    expect(frag.compatH3.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#ffd000"))
                    expect(frag.compatH4.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#00e676"))
                    expect(frag.compatH5.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#00d2ff"))
                    expect(frag.compatH6.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#9d4edd"))
                    expect(frag.htmlCompatWindow.computedStyle?.backgroundColor).toEqual(RgbaColor.fromHex("#12172e"))
                    expect(frag.htmlCompatWindow.computedStyle?.borderColor).toEqual(RgbaColor.fromHex("#00d2ff"))
                    expect(frag.demoPrimaryButton.computedStyle?.backgroundColor).toEqual(RgbaColor.fromHex("#ff2a6d"))
                    expect(frag.demoPrimaryButton.computedStyle?.backgroundGradient).toBeNotNull()
                    expect(frag.demoCard.computedStyle?.borderColor).toEqual(RgbaColor.fromHex("#00e676"))
                    expect(frag.demoCard.computedStyle?.borderRadiusPx).toBe(14f)
                    expect(frag.demoMenuBar.computedStyle?.backgroundGradient).toBeNotNull()

                    // Verify components OUTSIDE MdiContainer (IdeCodeEditor, outer MenuBar, outer buttons) are NOT affected
                    expect(frag.scssEditor.computedStyle?.backgroundGradient).toBeNull()
                    expect(frag.outerMenuBar.computedStyle?.backgroundGradient).toBeNull()
                    expect(frag.outerMenuBar.computedStyle?.backgroundColor).toBeNull()
                    expect(frag.switchPreviewTabButton.computedStyle?.backgroundGradient).toBeNull()

                    controller.destroy()
                }

                it("verifies htmlTag values across htmlcompat, Button ('button'), and genericui:* custom tags") {
                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val frag = controller.fragment

                    // HTML compat tags
                    expect(frag.compatHeader.htmlTag).toBe("header")
                    expect(frag.compatH1.htmlTag).toBe("h1")
                    expect(frag.compatH2.htmlTag).toBe("h2")
                    expect(frag.compatH3.htmlTag).toBe("h3")
                    expect(frag.compatH4.htmlTag).toBe("h4")
                    expect(frag.compatH5.htmlTag).toBe("h5")
                    expect(frag.compatH6.htmlTag).toBe("h6")
                    expect(frag.compatDiv.htmlTag).toBe("div")
                    expect(frag.compatSpan.htmlTag).toBe("span")
                    expect(frag.compatP.htmlTag).toBe("p")

                    // Bootstrap standard button tag
                    expect(frag.demoPrimaryButton.htmlTag).toBe("button")

                    // Custom genericui:* tags
                    expect(frag.mdiContainer.htmlTag).toBe("genericui:mdicontainer")
                    expect(frag.htmlCompatWindow.htmlTag).toBe("genericui:mdiwindow")
                    expect(frag.demoMenuBar.htmlTag).toBe("genericui:menubar")
                    expect(frag.demoCard.htmlTag).toBe("genericui:card")
                    expect(frag.demoAlert.htmlTag).toBe("genericui:alert")
                    expect(frag.demoProgressBar.htmlTag).toBe("genericui:progressbar")
                    expect(frag.demoLazyList.htmlTag).toBe("genericui:lazylist")
                    expect(frag.demoLazyGrid.htmlTag).toBe("genericui:lazygrid")
                    expect(frag.demoTreeView.htmlTag).toBe("genericui:treeview")

                    controller.destroy()
                }

                it("manages EmbeddedDockWorkspaceSubFragment inside mdi-win-dock-workspace") {
                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val frag = controller.fragment
                    val dockFrag = frag.dockSubFragment

                    expect(frag.dockWorkspaceWindow.hostedFragment).toBe(dockFrag)
                    expect(dockFrag.toolsDockPanel.getTitleBar()).toBeNotNull()
                    expect(dockFrag.inspectorDockPanel.getTitleBar()).toBeNotNull()
                    expect(dockFrag.consoleDockPanel.getTitleBar()).toBeNotNull()
                    expect(dockFrag.documents).toHaveLength(2)
                    expectNode(controller.findById("badge-dock-tab-count")).toHaveText("2 tabs")
                    expect(dockFrag.windowMode).toBe(StudioWindowMode.SINGLE_WINDOW_MODE)

                    // Add a new canvas tab inside the embedded dock workspace
                    controller.findById("btn-dock-add-tab").performClick()
                    expect(dockFrag.documents).toHaveLength(3)
                    expectNode(controller.findById("badge-dock-tab-count")).toHaveText("3 tabs")

                    // Toggle SINGLE_WINDOW_MODE <-> MULTI_WINDOW_MODE
                    controller.findById("btn-dock-toggle-mode").performClick()
                    expect(dockFrag.windowMode).toBe(StudioWindowMode.MULTI_WINDOW_MODE)
                    expectNode(controller.findById("badge-dock-window-mode")).toHaveText("MULTI_WINDOW_MODE")

                    controller.findById("btn-dock-toggle-mode").performClick()
                    expect(dockFrag.windowMode).toBe(StudioWindowMode.SINGLE_WINDOW_MODE)
                    expectNode(controller.findById("badge-dock-window-mode")).toHaveText("SINGLE_WINDOW_MODE")

                    controller.destroy()
                }

                it("activates, minimizes, maximizes, restores, cascades, and tiles MdiWindows inside MdiContainer") {
                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val frag = controller.fragment
                    val mdi = frag.mdiContainer

                    // Activate Window 2
                    mdi.activateWindow(frag.actionsNavWindow)
                    expect(mdi.activeWindow).toBe(frag.actionsNavWindow)
                    expect(frag.actionsNavWindow.isActiveWindow).toBe(true)

                    // Minimize Window 2
                    frag.actionsNavWindow.minimize()
                    expect(frag.actionsNavWindow.windowState).toBe(MdiWindowState.MINIMIZED)
                    expect(frag.actionsNavWindow.isVisible).toBe(false)

                    // Restore Window 2
                    frag.actionsNavWindow.restore()
                    expect(frag.actionsNavWindow.windowState).toBe(MdiWindowState.NORMAL)
                    expect(frag.actionsNavWindow.isVisible).toBe(true)

                    // Maximize Window 1
                    frag.htmlCompatWindow.maximize(desktopWidth = 1200, desktopHeight = 680)
                    expect(frag.htmlCompatWindow.windowState).toBe(MdiWindowState.MAXIMIZED)
                    expect(frag.htmlCompatWindow.windowBounds.width).toBe(1200)
                    expect(frag.htmlCompatWindow.windowBounds.height).toBe(680)

                    // Restore Window 1
                    frag.htmlCompatWindow.restore()
                    expect(frag.htmlCompatWindow.windowState).toBe(MdiWindowState.NORMAL)

                    // Minimize all and Restore all via toolbar buttons
                    controller.findById("mdi-btn-minimize-all").performClick()
                    expect(mdi.windows.all { it.windowState == MdiWindowState.MINIMIZED }).toBe(true)

                    controller.findById("mdi-btn-restore-all").performClick()
                    expect(mdi.windows.all { it.windowState == MdiWindowState.NORMAL && it.isVisible }).toBe(true)

                    // Tile and Cascade via toolbar buttons
                    controller.findById("mdi-btn-tile").performClick()
                    expect(frag.htmlCompatWindow.windowBounds.x != frag.actionsNavWindow.windowBounds.x).toBe(true)

                    controller.findById("mdi-btn-cascade").performClick()
                    expect(frag.actionsNavWindow.windowBounds.x > frag.htmlCompatWindow.windowBounds.x).toBe(true)

                    controller.destroy()
                }

                it("switches to Stylesheet Editor tab, switches presets, and live-edits SCSS without affecting outer components") {
                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val frag = controller.fragment

                    // Switch to Tab 2 (Stylesheet Editor)
                    controller.findById("btn-switch-scss-tab").performClick()
                    expect(frag.mainTabContainer.selectedIndex).toBe(1)
                    expectNode(controller.findById("badge-active-main-tab")).toHaveText("Stylesheet Editor (SCSS)")

                    // Switch to Emerald Studio preset
                    controller.findById("btn-preset-emerald").performClick()
                    expect(frag.activePreset).toBe(ScssThemePreset.EMERALD_STUDIO)
                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#34d399"))
                    expect(frag.demoCard.computedStyle?.borderRadiusPx).toBe(16f)

                    // Switch to Sunset Warm preset
                    controller.findById("btn-preset-sunset").performClick()
                    expect(frag.activePreset).toBe(ScssThemePreset.SUNSET_WARM)
                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#fb923c"))
                    expect(frag.demoCard.computedStyle?.borderRadiusPx).toBe(18f)

                    // Live-edit custom SCSS inside IdeCodeEditor
                    val customScss = """
                        ${'$'}primary-color: #00f5d4;
                        ${'$'}win-bg: #141e30;
                        ${'$'}btn-color: #9b5de5;

                        genericui:mdicontainer {
                          h1 {
                            color: ${'$'}primary-color;
                            font-size: 30px;
                          }
                          genericui:mdiwindow {
                            background-color: ${'$'}win-bg;
                            border-radius: 20px;
                          }
                          button.btn {
                            background-color: ${'$'}btn-color;
                            border-radius: 14px;
                          }
                        }
                    """.trimIndent()
                    frag.scssEditor.setText(customScss)

                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#00f5d4"))
                    expect(frag.compatH1.computedStyle?.fontSizePx).toBe(30f)
                    expect(frag.htmlCompatWindow.computedStyle?.backgroundColor).toEqual(RgbaColor.fromHex("#141e30"))
                    expect(frag.htmlCompatWindow.computedStyle?.borderRadiusPx).toBe(20f)
                    expect(frag.demoPrimaryButton.computedStyle?.backgroundColor).toEqual(RgbaColor.fromHex("#9b5de5"))
                    expect(frag.demoPrimaryButton.computedStyle?.borderRadiusPx).toBe(14f)

                    // Outer components remain untouched
                    expect(frag.outerMenuBar.computedStyle?.backgroundColor).toBeNull()
                    expect(frag.scssEditor.computedStyle?.backgroundColor).toBeNull()

                    controller.destroy()
                }

                it("reports SCSS syntax diagnostics on invalid SCSS and recovers when fixed") {
                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val frag = controller.fragment

                    // Enter invalid SCSS with an unclosed '{'
                    frag.scssEditor.setText(
                        """
                        genericui:mdicontainer {
                          h1 {
                            color: #ff2a6d;
                        """.trimIndent()
                    )

                    expect(frag.scssEditor.getDiagnostics().isNotEmpty()).toBe(true)
                    expectNode(controller.findById("badge-scss-error-count")).toHaveText("1 errors")

                    // Fix the SCSS by closing the braces
                    frag.scssEditor.setText(
                        """
                        genericui:mdicontainer {
                          h1 {
                            color: #ff2a6d;
                          }
                        }
                        """.trimIndent()
                    )

                    expect(frag.scssEditor.getDiagnostics()).toHaveLength(0)
                    expectNode(controller.findById("badge-scss-error-count")).toHaveText("0 errors")
                    expect(frag.compatH1.computedStyle?.textColor).toEqual(RgbaColor.fromHex("#ff2a6d"))

                    controller.destroy()
                }

                it("tokenizes SCSS variables, @directives, genericui:* tags, hex colors, units, and provides autocomplete") {
                    val tokenizer = SyntaxTokenizer(CodeLanguage.SCSS)
                    val tokens = tokenizer.tokenizeLine(
                        "\$primary: #ff2a6d; @include elevated-surface(14px); genericui:mdiwindow &:hover { background-color: #00d2ff !important; }"
                    ).filter { it.type != SyntaxTokenType.WHITESPACE }

                    expect(tokens.any { it.text == "\$primary" && it.type == SyntaxTokenType.VARIABLE }).toBe(true)
                    expect(tokens.any { it.text == "#ff2a6d" && it.type == SyntaxTokenType.NUMBER_LITERAL }).toBe(true)
                    expect(tokens.any { it.text == "@include" && it.type == SyntaxTokenType.ANNOTATION }).toBe(true)
                    expect(tokens.any { it.text == "14px" && it.type == SyntaxTokenType.NUMBER_LITERAL }).toBe(true)
                    expect(tokens.any { it.text == "genericui:mdiwindow" && it.type == SyntaxTokenType.TAG_NAME }).toBe(true)
                    expect(tokens.any { it.text == "&:hover" && it.type == SyntaxTokenType.KEYWORD }).toBe(true)
                    expect(tokens.any { it.text == "background-color" && it.type == SyntaxTokenType.PROPERTY }).toBe(true)
                    expect(tokens.any { it.text == "!important" && it.type == SyntaxTokenType.KEYWORD }).toBe(true)

                    val controller = launchFragment {
                        MdiComponentAndScssShowcaseFragment()
                    }
                    val provider = ScssLanguageSupport.createCompletionProvider()
                    val completions = provider.computeCompletions(
                        controller.fragment.scssEditor.document,
                        CursorPosition(0, 0),
                        ""
                    )
                    expect(completions.any { it.label == "\$rainbow-red" }).toBe(true)
                    expect(completions.any { it.label == "elevated-surface" }).toBe(true)
                    expect(completions.any { it.label == "genericui:mdicontainer" }).toBe(true)
                    expect(completions.any { it.label == "background-color" }).toBe(true)
                    controller.destroy()
                }

                it("manages IApplication.addStyleSheet / removeStyleSheet, ThemeMode.AUTO / DARK / LIGHT, getBootstrap(), and getStyle()") {
                    val controller = launchApp(visualOnWindows = false) { MdiComponentAndScssShowcaseApp() }
                    val app = controller.app
                    val frag = app.showcaseFragment

                    // Verify initial ThemeMode.AUTO and global precompiled stylesheet registration
                    expect(app.getTheme()).toBe(ThemeMode.AUTO)
                    assertTrue(app.hasStyleSheet(StylesPrecompiledStyleSheet))
                    assertTrue(frag.hasClass("mdi-showcase-root"))

                    // Switch theme mode between DARK, LIGHT, and AUTO
                    app.selectAppThemeMode(ThemeMode.DARK)
                    expect(app.getTheme()).toBe(ThemeMode.DARK)
                    expect(app.getResolvedTheme()).toBe(ThemeMode.DARK)

                    app.selectAppThemeMode(ThemeMode.LIGHT)
                    expect(app.getTheme()).toBe(ThemeMode.LIGHT)
                    expect(app.getResolvedTheme()).toBe(ThemeMode.LIGHT)

                    app.selectAppThemeMode(ThemeMode.AUTO)
                    expect(app.getTheme()).toBe(ThemeMode.AUTO)

                    // Toggle global stylesheet via removeStyleSheet / addStyleSheet
                    assertTrue(app.setGlobalStyleSheetEnabled(false))
                    assertFalse(app.hasStyleSheet(StylesPrecompiledStyleSheet))
                    assertTrue(app.setGlobalStyleSheetEnabled(true))
                    assertTrue(app.hasStyleSheet(StylesPrecompiledStyleSheet))

                    // Verify getBootstrap() and getStyle() on showcase controls
                    expect(frag.scssStatusBadge.getBootstrap().type).toBe(BootstrapType.SUCCESS)
                    expect(frag.presetRainbowButton.getBootstrap().outline).toBe(false)
                    expect(frag.presetEmeraldButton.getBootstrap().outline).toBe(true)
                    val topBar = controller.findById("outer-shell-topbar")
                    expect(topBar.component?.getStyle()?.borderRadiusPx).toBe(8f)
                }
            }
        }
        report.assertAllPassed()
    }
}
