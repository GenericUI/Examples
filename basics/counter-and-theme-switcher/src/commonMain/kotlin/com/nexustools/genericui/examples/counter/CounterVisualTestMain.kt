package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point using [JestElectricRunner.runVisualAppTestMain] for the
 * Counter & Theme Switcher example.
 */
object CounterVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport {
        CounterPlatformBootstrap.initialize(args)
        return JestElectricRunner.runVisualAppTestMain(
            suiteName = "Counter & Theme Switcher Visual Suite",
            args = args
        ) {
            describe("Counter Controls & Theme Switching") {
                it("increments, decrements, changes step multiplier, and updates theme color") {
                    val store = PreferenceStore("CounterVisualTestStore").apply { clear() }
                    val controller = launchApp { CounterApp(store) }

                    expectNode(controller.findById("counter-value-badge")).toHaveText("0")
                    controller.findById("btn-increment").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("1")

                    controller.findById("select-step").performSelectValue("5")
                    controller.findById("btn-increment").performClick()
                    expectNode(controller.findById("counter-value-badge")).toHaveText("6")

                    controller.findById("select-theme-mode").performSelectValue("LIGHT")
                    expect(controller.app.getThemeMode()).toBe(ThemeMode.LIGHT)

                    controller.findById("btn-color-emerald").performClick()
                    expect(controller.app.getThemeColor()).toBe("#10B981")
                }
            }
        }
    }
}

fun mainVisualTest(args: Array<String>) {
    CounterVisualTestMain.runVisualSuite(args)
}
