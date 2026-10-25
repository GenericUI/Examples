package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point using [JestElectricRunner.runVisualAppTestMain] for the
 * Validated Registration Wizard example.
 */
object RegistrationWizardVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport {
        RegistrationWizardPlatformBootstrap.initialize(args)
        return JestElectricRunner.runVisualAppTestMain(
            suiteName = "Validated Registration Wizard Visual Suite",
            args = args
        ) {
            describe("3-Step Validated Registration Flow") {
                it("completes all 3 wizard steps and persists the registration profile") {
                    val store = PreferenceStore("RegistrationWizardVisualStore").apply { clear() }
                    val controller = launchApp { RegistrationWizardApp(store) }

                    controller.findById("input-username").performTypeText("alex_dev")
                    controller.findById("input-email").performTypeText("alex@nexustools.dev")
                    controller.findById("input-password").performTypeText("SecretPass123")
                    controller.findById("btn-next").performClick()

                    controller.findById("select-role").performSelectValue("DevOps Architect")
                    controller.findById("slider-experience").performSetSliderValue(8.0)
                    controller.findById("btn-next").performClick()

                    controller.findById("check-terms").performSetChecked(true)
                    controller.findById("btn-submit").performClick()

                    val saved = RegistrationProfile.loadFrom(store)
                    expect(saved?.username).toBe("alex_dev")
                    expect(saved?.role).toBe("DevOps Architect")
                    expect(saved?.experienceYears).toBe(8)
                }
            }
        }
    }
}

fun mainVisualTest(args: Array<String>) {
    RegistrationWizardVisualTestMain.runVisualSuite(args)
}
