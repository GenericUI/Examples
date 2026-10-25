package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.wizard.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] entry point for the Validated Registration Wizard example.
 */
class RegistrationWizardApp(
    val preferenceStore: PreferenceStore = PreferenceStore(RegistrationProfile.DEFAULT_STORE_NAME),
    val wizardFragment: RegistrationWizardFragment = RegistrationWizardFragment(preferenceStore)
) : FragmentApplication(
    initialFragment = wizardFragment,
    title = "GenericUI — Validated Registration Wizard",
    initialThemeColor = "#10B981"
) {

    init {
        setThemeMode(ThemeMode.DARK)
        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = buildApplicationMenuBar()
        wizardFragment.setTitleBar(titleBar)
        wizardFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildApplicationMenuBar(): MenuBar {
        val wizardMenu = Menu("Wizard")
            .addItem(MenuItem("Next Step") {
                wizardFragment.goToNextStep()
            })
            .addItem(MenuItem("Previous Step") {
                wizardFragment.goToPreviousStep()
            })
            .addSeparator()
            .addItem(MenuItem("Reset Wizard") {
                wizardFragment.resetWizard()
            })

        return MenuBar().addMenu(wizardMenu)
    }

    companion object {
        fun start(args: Array<String> = emptyArray()) {
            RegistrationWizardPlatformBootstrap.initialize(args)
            val app = RegistrationWizardApp()
            app.run()
        }
    }
}

fun main(args: Array<String>) {
    RegistrationWizardApp.start(args)
}
