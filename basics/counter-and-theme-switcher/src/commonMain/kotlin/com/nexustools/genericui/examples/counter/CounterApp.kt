package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.counter.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] entry point for the Counter & Theme Switcher example.
 */
class CounterApp(
    val preferenceStore: PreferenceStore = PreferenceStore(CounterSettings.DEFAULT_STORE_NAME),
    val counterFragment: CounterFragment = CounterFragment(preferenceStore)
) : FragmentApplication(
    initialFragment = counterFragment,
    title = "GenericUI — Counter & Theme Switcher",
    initialThemeColor = counterFragment.settings.themeColorHex
) {

    init {
        setThemeMode(counterFragment.settings.resolvedThemeMode())
        setThemeColor(counterFragment.settings.themeColorHex)

        counterFragment.onThemeUpdated = { mode, colorHex ->
            setThemeMode(mode)
            setThemeColor(colorHex)
        }

        val titleBar = TitleBar(
            embedMenuBar = true,
            showWindowTitle = true,
            showWindowIcon = true,
            customTitleBar = true
        )
        val menuBar = buildApplicationMenuBar()
        counterFragment.setTitleBar(titleBar)
        counterFragment.setMenuBar(menuBar)
        mainWindow.setTitleBar(titleBar)
        mainWindow.setMenuBar(menuBar)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }

    private fun buildApplicationMenuBar(): MenuBar {
        val actionsMenu = Menu("Counter")
            .addItem(MenuItem("Increment", shortcutText = "Ctrl+Up") {
                counterFragment.increment()
            })
            .addItem(MenuItem("Decrement", shortcutText = "Ctrl+Down") {
                counterFragment.decrement()
            })
            .addSeparator()
            .addItem(MenuItem("Reset to 0", shortcutText = "Ctrl+R") {
                counterFragment.resetCount()
            })
            .addSeparator()
            .addItem(MenuItem("Save Settings", shortcutText = "Ctrl+S") {
                counterFragment.saveSettingsNow()
            })

        val themeMenu = Menu("Theme")
            .addItem(MenuItem("Light Mode") {
                counterFragment.updateThemeMode("LIGHT")
            })
            .addItem(MenuItem("Dark Mode") {
                counterFragment.updateThemeMode("DARK")
            })
            .addItem(MenuItem("System Mode") {
                counterFragment.updateThemeMode("SYSTEM")
            })
            .addSeparator()
            .addItem(MenuItem("Accent Blue (#2563EB)") {
                counterFragment.updateThemeColor("#2563EB")
            })
            .addItem(MenuItem("Accent Emerald (#10B981)") {
                counterFragment.updateThemeColor("#10B981")
            })
            .addItem(MenuItem("Accent Purple (#8B5CF6)") {
                counterFragment.updateThemeColor("#8B5CF6")
            })

        return MenuBar()
            .addMenu(actionsMenu)
            .addMenu(themeMenu)
    }

    companion object {
        fun start(args: Array<String> = emptyArray()) {
            CounterPlatformBootstrap.initialize(args)
            val app = CounterApp()
            app.run()
        }
    }
}

fun main(args: Array<String>) {
    CounterApp.start(args)
}
