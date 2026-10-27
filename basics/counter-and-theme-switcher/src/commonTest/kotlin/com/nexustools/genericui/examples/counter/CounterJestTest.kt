package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Automated cross-platform [JestElectricRunner] BDD & Robolectric-style UI test suite for
 * the Counter & Theme Switcher example.
 */
class CounterJestTest {

    @Test
    fun runCounterAndThemeSwitcherSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Counter & Theme Switcher JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Counter Interactions, Step Multiplier & Theme Persistence") {
                it("increments, decrements, and resets the counter via native button clicks") {
                    val store = PreferenceStore("CounterTestStore_1").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { CounterApp(store) }

                    expectNode(controller.findById("counter-value-badge")).toBeVisible()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("0")

                    controller.findById("btn-increment").performClick()
                    controller.findById("btn-increment").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("2")

                    controller.findById("btn-decrement").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("1")

                    controller.findById("btn-reset").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("0")
                }

                it("applies the selected step size multiplier (1, 5, 10) to increment and decrement") {
                    val store = PreferenceStore("CounterTestStore_2").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { CounterApp(store) }

                    controller.findById("select-step").performSelectValue("5")
                    controller.findById("btn-increment").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("5")

                    controller.findById("btn-increment").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("10")

                    controller.findById("select-step").performSelectValue("10")
                    controller.findById("btn-decrement").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("0")

                    controller.findById("btn-decrement").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("-10")
                }

                it("updates application theme mode (AUTO, LIGHT, DARK) and accent themeColor dynamically") {
                    val store = PreferenceStore("CounterTestStore_3").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { CounterApp(store) }

                    expect(controller.app.getTheme()).toBe(ThemeMode.AUTO)
                    expect(controller.app.getResolvedTheme().isAuto).toBe(false)
                    expect(controller.app.getThemeColor()).toBe("#2563EB")

                    controller.findById("select-theme-mode").performSelectValue("LIGHT")
                    expect(controller.app.getTheme()).toBe(ThemeMode.LIGHT)
                    expect(controller.app.getResolvedTheme()).toBe(ThemeMode.LIGHT)
                    expectNode(controller.findById("badge-active-theme")).toContainText("LIGHT")

                    controller.findById("btn-color-emerald").performClick()
                    expect(controller.app.getThemeColor()).toBe("#10B981")
                    expectNode(controller.findById("badge-active-theme")).toContainText("#10B981")

                    controller.findById("select-theme-mode").performSelectValue("DARK")
                    controller.findById("btn-color-purple").performClick()
                    expect(controller.app.getTheme()).toBe(ThemeMode.DARK)
                    expect(controller.app.getResolvedTheme()).toBe(ThemeMode.DARK)
                    expect(controller.app.getThemeColor()).toBe("#8B5CF6")

                    controller.findById("select-theme-mode").performSelectValue("AUTO")
                    expect(controller.app.getTheme()).toBe(ThemeMode.AUTO)
                }

                it("isolates ComponentBootstrap enum properties and ComponentStyle inline CSS from user-added custom classes") {
                    val store = PreferenceStore("CounterTestStore_StyleIsolation").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { CounterApp(store) }
                    val fragment = controller.app.counterFragment

                    expect(fragment.incrementButton.getBootstrap().type).toBe(BootstrapType.PRIMARY)
                    expect(fragment.incrementButton.getBootstrap().size).toBe(BootstrapSize.MD)
                    expect(fragment.incrementButton.hasClass("btn-primary")).toBe(false)
                    expect(fragment.incrementButton.getClasses().isEmpty()).toBe(true)

                    expect(fragment.counterValueBadge.hasClass("counter-badge")).toBe(true)
                    expect(fragment.counterValueBadge.hasClass("badge")).toBe(false)
                    expect(fragment.autoSaveSwitch.getStyle().accentColor).toBe(RgbaColor.fromHex("#2563EB"))
                }

                it("persists and restores CounterSettings across application restarts when auto-save is enabled") {
                    val store = PreferenceStore("CounterTestStore_4").apply { clear() }

                    val firstSession = launchApp(visualOnWindows = false) { CounterApp(store) }
                    expectNode(firstSession.findById("switch-autosave")).toBeChecked()

                    firstSession.findById("select-step").performSelectValue("5")
                    firstSession.findById("btn-increment").performClick()
                    firstSession.findById("btn-increment").performClick()
                    firstSession.findById("btn-increment").performClick()
                    firstSession.findById("select-theme-mode").performSelectValue("LIGHT")
                    firstSession.findById("btn-color-purple").performClick()
                    expectNode(firstSession.findById("counter-value-badge")).toHaveText("15")
                    firstSession.destroy()

                    val secondSession = launchApp(visualOnWindows = false) { CounterApp(store) }
                    expectNode(secondSession.findById("counter-value-badge")).toHaveText("15")
                    expect(secondSession.app.getThemeMode()).toBe(ThemeMode.LIGHT)
                    expect(secondSession.app.getThemeColor()).toBe("#8B5CF6")
                    expect(secondSession.app.counterFragment.stepSelect.selectedValue).toBe("5")

                    // Disable auto-save, increment in memory, and verify store retains 15 until manually saved
                    secondSession.findById("switch-autosave").performSetChecked(false)
                    secondSession.findById("btn-increment").performClick()
                    expectNode(secondSession.findById("counter-value-badge")).toHaveText("20")
                    expect(CounterSettings.loadFrom(store).count).toBe(15)

                    secondSession.findById("btn-save-settings").performClick()
                    expect(CounterSettings.loadFrom(store).count).toBe(20)
                }
            }
        }
        report.assertAllPassed()
    }
}
