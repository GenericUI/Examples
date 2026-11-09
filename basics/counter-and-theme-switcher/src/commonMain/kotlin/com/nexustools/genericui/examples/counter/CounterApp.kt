package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.counter.res.Resource
import com.nexustools.genericui.window.TitleBar

/**
 * Multiplatform [FragmentApplication] entry point for the Counter & Theme Switcher example.
 */
@AppApplication
class CounterApp(
    private val customPreferenceStore: PreferenceStore? = null,
    val counterFragment: CounterFragment = CounterFragment(customPreferenceStore)
) : FragmentApplication(
    initialFragment = counterFragment,
    title = "GenericUI — Counter & Theme Switcher",
    initialThemeColor = counterFragment.settings.themeColorHex
) {

    override fun getPreferenceStore(): PreferenceStore =
        customPreferenceStore ?: super.getPreferenceStore()

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
        addStyleSheet(Resource.style.styles)
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
            .addItem(MenuItem("Auto (System) Mode") {
                counterFragment.updateThemeMode("AUTO")
            })
            .addItem(MenuItem("Light Mode") {
                counterFragment.updateThemeMode("LIGHT")
            })
            .addItem(MenuItem("Dark Mode") {
                counterFragment.updateThemeMode("DARK")
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
}
