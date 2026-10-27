package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Automated cross-platform [JestElectricRunner] BDD & Robolectric-style UI test suite for
 * the Validated Registration Wizard example.
 */
class RegistrationWizardJestTest {

    @Test
    fun runValidatedRegistrationWizardSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Validated Registration Wizard JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Step Validation, Form Controls & Profile Persistence") {
                it("blocks navigation when Step 1 credentials are empty or invalid") {
                    val store = PreferenceStore("WizardTestStore_1").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { RegistrationWizardApp(store) }

                    expect(controller.app.wizardFragment.currentStep).toBe(1)
                    expectNode(controller.findById("wizard-progress")).toBeVisible()

                    // Attempt to click Next with empty fields
                    controller.findById("btn-next").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(1)
                    expectNode(controller.findById("validation-message")).toContainText("Invalid username")

                    // Valid username, but invalid email
                    controller.findById("input-username").performTypeText("kaelen")
                    controller.findById("input-email").performTypeText("not-an-email")
                    controller.findById("input-password").performTypeText("123456")
                    controller.findById("btn-next").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(1)
                    expectNode(controller.findById("validation-message")).toContainText("Invalid email")

                    // Valid email, but short password
                    controller.findById("input-email").performTypeText("kaelen@nexustools.io")
                    controller.findById("input-password").performTypeText("123")
                    controller.findById("btn-next").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(1)
                    expectNode(controller.findById("validation-message")).toContainText("Invalid password")
                }

                it("advances through Steps 1 -> 2 -> 3, supports Back navigation, and saves RegistrationProfile") {
                    val store = PreferenceStore("WizardTestStore_2").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { RegistrationWizardApp(store) }

                    // Complete Step 1 with valid credentials
                    controller.findById("input-username").performTypeText("elena_vance")
                    controller.findById("input-email").performTypeText("elena@nexustools.io")
                    controller.findById("input-password").performTypeText("StrongPass#2026")
                    expectNode(controller.findById("validation-message")).toContainText("Step 1 valid")

                    controller.findById("btn-next").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(2)
                    expectNode(controller.findById("badge-current-step")).toContainText("Step 2 of 3")

                    // Test Back button returning to Step 1 and Next returning to Step 2
                    controller.findById("btn-back").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(1)
                    controller.findById("btn-next").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(2)

                    // Configure Step 2 Developer Profile
                    controller.findById("select-role").performSelectValue("QA Engineer")
                    controller.findById("slider-experience").performSetSliderValue(9.0)
                    expectNode(controller.findById("badge-experience-years")).toHaveText("9 years")

                    controller.findById("btn-next").performClick()
                    expect(controller.app.wizardFragment.currentStep).toBe(3)
                    expectNode(controller.findById("summary-username")).toContainText("elena_vance")
                    expectNode(controller.findById("summary-email")).toContainText("elena@nexustools.io")
                    expectNode(controller.findById("summary-role")).toContainText("QA Engineer")
                    expectNode(controller.findById("summary-experience")).toContainText("9 years")

                    // Attempt submit before accepting terms -> blocked
                    controller.findById("btn-submit").performClick()
                    expect(controller.app.wizardFragment.isSubmitted).toBe(false)
                    expect(RegistrationProfile.loadFrom(store)).toBeNull()

                    // Accept terms via performSetChecked(true) and submit
                    controller.findById("check-terms").performSetChecked(true)
                    expectNode(controller.findById("check-terms")).toBeChecked()
                    controller.findById("btn-submit").performClick()

                    expect(controller.app.wizardFragment.isSubmitted).toBe(true)
                    expectNode(controller.findById("validation-message")).toContainText("Registration complete")

                    val savedProfile = RegistrationProfile.loadFrom(store)
                    expect(savedProfile).toBeNotNull()
                    expect(savedProfile?.username).toBe("elena_vance")
                    expect(savedProfile?.email).toBe("elena@nexustools.io")
                    expect(savedProfile?.role).toBe("QA Engineer")
                    expect(savedProfile?.experienceYears).toBe(9)
                    expect(savedProfile?.acceptedTerms).toBe(true)

                    // Verify ComponentBootstrap, ComponentStyle, and strict CSS class isolation
                    val fragment = controller.app.wizardFragment
                    expect(controller.app.getTheme()).toBe(ThemeMode.AUTO)
                    expect(fragment.nextButton.getBootstrap().type).toBe(BootstrapType.PRIMARY)
                    expect(fragment.nextButton.getBootstrap().size).toBe(BootstrapSize.MD)
                    expect(fragment.nextButton.hasClass("btn-primary")).toBe(false)
                    expect(fragment.submitButton.getBootstrap().type).toBe(BootstrapType.SUCCESS)
                    expect(fragment.step1Panel.hasClass("wizard-step-panel")).toBe(true)
                    expect(fragment.experienceSlider.getStyle().accentColor).toBe(RgbaColor.fromHex("#10B981"))
                    expect(fragment.termsCheckBox.getStyle().accentColor).toBe(RgbaColor.fromHex("#10B981"))
                }
            }
        }
        report.assertAllPassed()
    }
}
