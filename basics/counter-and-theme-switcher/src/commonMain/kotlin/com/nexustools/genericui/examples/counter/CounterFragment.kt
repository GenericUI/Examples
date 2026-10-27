package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.Switch
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.color.RgbaColor

/**
 * Primary UI [Fragment] for the Counter & Theme Switcher example.
 *
 * Demonstrates:
 * - Reactive counter controls (`Increment`, `Decrement`, `Reset`) with a configurable step multiplier (`1`, `5`, `10`).
 * - Runtime theme mode switching (`AUTO`, `LIGHT`, `DARK`) and accent `themeColor` presets (`#2563EB`, `#10B981`, `#8B5CF6`).
 * - Typed `getBootstrap()` (`ComponentBootstrap`) and `getStyle()` (`ComponentStyle`) configuration with strict custom CSS class isolation.
 * - Automatic or manual persistence of [CounterSettings] via [PreferenceStore].
 */
@MainAppFragment
class CounterFragment(
    val preferenceStore: PreferenceStore = PreferenceStore(CounterSettings.DEFAULT_STORE_NAME),
    var onThemeUpdated: ((ThemeMode, String) -> Unit)? = null
) : Fragment() {

    var settings: CounterSettings = CounterSettings.loadFrom(preferenceStore)
        private set

    val counterValueBadge: Badge = Badge(
        text = settings.count.toString(),
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "counter-value-badge"
        addClass("counter-badge")
        getBootstrap().apply {
            fontWeight = FontWeight.BOLD
            paddingX = SpacingScale.FOUR
            paddingY = SpacingScale.TWO
        }
        getStyle().apply {
            fontSizePx = 28f
            borderRadiusPx = 999f
        }
    }

    val incrementButton: Button = Button(
        text = "Increment",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.MD
    ).apply {
        id = "btn-increment"
        getBootstrap().apply {
            type = BootstrapType.PRIMARY
            size = BootstrapSize.MD
            fontWeight = FontWeight.SEMIBOLD
        }
    }

    val decrementButton: Button = Button(
        text = "Decrement",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.MD
    ).apply {
        id = "btn-decrement"
        getBootstrap().apply {
            type = BootstrapType.SECONDARY
            size = BootstrapSize.MD
        }
    }

    val resetButton: Button = Button(
        text = "Reset",
        type = BootstrapType.DANGER,
        size = BootstrapSize.MD
    ).apply {
        id = "btn-reset"
        getBootstrap().apply {
            type = BootstrapType.DANGER
            size = BootstrapSize.MD
        }
    }

    val stepSelect: Select = Select(
        options = CounterSettings.STEP_OPTIONS.map { SelectOption(it, "Step: $it") },
        selectedValue = settings.stepSize.toString()
    ).apply {
        id = "select-step"
    }

    val themeModeSelect: Select = Select(
        options = CounterSettings.THEME_MODE_OPTIONS.map { SelectOption(it, it) },
        selectedValue = settings.themeModeName
    ).apply {
        id = "select-theme-mode"
    }

    val bluePresetButton: Button = Button(
        text = "#2563EB",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-color-blue"
    }

    val emeraldPresetButton: Button = Button(
        text = "#10B981",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-color-emerald"
    }

    val purplePresetButton: Button = Button(
        text = "#8B5CF6",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-color-purple"
    }

    val autoSaveSwitch: Switch = Switch(
        label = "Auto-Save to PreferenceStore",
        checked = settings.autoSave
    ).apply {
        id = "switch-autosave"
    }

    val saveNowButton: Button = Button(
        text = "Save Now",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-save-settings"
    }

    val activeThemeBadge: Badge = Badge(
        text = "${settings.themeModeName} (${settings.themeColorHex})",
        type = BootstrapType.INFO
    ).apply {
        id = "badge-active-theme"
    }

    val persistenceStatusBadge: Badge = Badge(
        text = if (settings.autoSave) "Auto-Save: ON" else "Auto-Save: OFF",
        type = if (settings.autoSave) BootstrapType.SUCCESS else BootstrapType.WARNING
    ).apply {
        id = "badge-persistence-status"
    }

    private var uiBuilt: Boolean = false
    private var lastObservedAutoSave: Boolean = settings.autoSave

    init {
        ensureUiBuilt()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        if (!uiBuilt || children.isEmpty()) {
            uiBuilt = false
            ensureUiBuilt()
        }
        scheduleSwitchSyncLoop()
    }

    private fun scheduleSwitchSyncLoop() {
        setTimeout(1) {
            syncSwitchStateIfNeeded()
            scheduleSwitchSyncLoop()
        }
    }

    private fun syncSwitchStateIfNeeded() {
        val currentChecked = autoSaveSwitch.isChecked
        if (currentChecked != lastObservedAutoSave) {
            lastObservedAutoSave = currentChecked
            settings = settings.copy(autoSave = currentChecked)
            refreshStatusBadges()
            if (currentChecked) {
                CounterSettings.saveTo(preferenceStore, settings)
            }
        }
    }

    private fun ensureUiBuilt() {
        if (uiBuilt) return
        uiBuilt = true
        clearChildren()
        addClass("counter-root")

        val rootStack = VStack(gap = SpacingScale.THREE)

        val headerRow = HStack(gap = SpacingScale.TWO)
        headerRow.addChild(Heading("Counter & Theme Switcher", level = 3))
        headerRow.addChild(activeThemeBadge)
        headerRow.addChild(persistenceStatusBadge)
        rootStack.addChild(headerRow)

        val counterCard = Card("Reactive Counter & Step Multiplier").apply {
            addClass("counter-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }
        val counterCardBody = VStack(gap = SpacingScale.TWO)
        val badgeRow = HStack(gap = SpacingScale.TWO)
        badgeRow.addChild(Text("Current Count:"))
        badgeRow.addChild(counterValueBadge)
        counterCardBody.addChild(badgeRow)

        val actionButtonsRow = HStack(gap = SpacingScale.TWO)
        actionButtonsRow.addChild(decrementButton)
        actionButtonsRow.addChild(incrementButton)
        actionButtonsRow.addChild(resetButton)
        counterCardBody.addChild(actionButtonsRow)

        val stepRow = HStack(gap = SpacingScale.TWO)
        stepRow.addChild(Text("Step Size:"))
        stepRow.addChild(stepSelect)
        counterCardBody.addChild(stepRow)
        counterCard.addChild(counterCardBody)
        rootStack.addChild(counterCard)

        val themeCard = Card("Theme Mode, Accent Color & Persistence").apply {
            addClass("theme-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }
        val themeCardBody = VStack(gap = SpacingScale.TWO)

        val modeRow = HStack(gap = SpacingScale.TWO)
        modeRow.addChild(Text("Theme Mode:"))
        modeRow.addChild(themeModeSelect)
        themeCardBody.addChild(modeRow)

        val colorPresetRow = HStack(gap = SpacingScale.TWO)
        colorPresetRow.addChild(Text("Accent Theme Color:"))
        colorPresetRow.addChild(bluePresetButton)
        colorPresetRow.addChild(emeraldPresetButton)
        colorPresetRow.addChild(purplePresetButton)
        themeCardBody.addChild(colorPresetRow)

        val persistenceRow = HStack(gap = SpacingScale.TWO)
        persistenceRow.addChild(autoSaveSwitch)
        persistenceRow.addChild(saveNowButton)
        themeCardBody.addChild(persistenceRow)

        themeCard.addChild(themeCardBody)
        rootStack.addChild(themeCard)

        addChild(rootStack)
        wireHandlers()
        refreshUiFromSettings()
    }

    private fun wireHandlers() {
        incrementButton.setOnClick {
            increment()
        }
        decrementButton.setOnClick {
            decrement()
        }
        resetButton.setOnClick {
            resetCount()
        }
        stepSelect.onSelectionChanged = { selected ->
            val parsed = selected.trim().toIntOrNull()?.coerceAtLeast(1) ?: 1
            updateStepSize(parsed)
        }
        themeModeSelect.onSelectionChanged = { selected ->
            updateThemeMode(selected)
        }
        bluePresetButton.setOnClick {
            updateThemeColor("#2563EB")
        }
        emeraldPresetButton.setOnClick {
            updateThemeColor("#10B981")
        }
        purplePresetButton.setOnClick {
            updateThemeColor("#8B5CF6")
        }
        autoSaveSwitch.onCheckedChanged = { checked ->
            setAutoSave(checked)
        }
        autoSaveSwitch.setOnClick {
            setAutoSave(autoSaveSwitch.isChecked)
        }
        saveNowButton.setOnClick {
            saveSettingsNow()
        }
    }

    fun increment() {
        syncSwitchStateIfNeeded()
        val currentStep = stepSelect.selectedValue?.toIntOrNull()?.coerceAtLeast(1) ?: settings.stepSize
        settings = settings.copy(
            count = settings.count + currentStep,
            stepSize = currentStep,
            autoSave = autoSaveSwitch.isChecked
        )
        refreshUiFromSettings()
        persistIfAutoSave()
    }

    fun decrement() {
        syncSwitchStateIfNeeded()
        val currentStep = stepSelect.selectedValue?.toIntOrNull()?.coerceAtLeast(1) ?: settings.stepSize
        settings = settings.copy(
            count = settings.count - currentStep,
            stepSize = currentStep,
            autoSave = autoSaveSwitch.isChecked
        )
        refreshUiFromSettings()
        persistIfAutoSave()
    }

    fun resetCount() {
        syncSwitchStateIfNeeded()
        settings = settings.copy(
            count = 0,
            autoSave = autoSaveSwitch.isChecked
        )
        refreshUiFromSettings()
        persistIfAutoSave()
    }

    fun updateStepSize(newStep: Int) {
        syncSwitchStateIfNeeded()
        val clamped = newStep.coerceAtLeast(1)
        settings = settings.copy(
            stepSize = clamped,
            autoSave = autoSaveSwitch.isChecked
        )
        stepSelect.selectedValue = clamped.toString()
        refreshUiFromSettings()
        persistIfAutoSave()
    }

    fun updateThemeMode(modeName: String) {
        syncSwitchStateIfNeeded()
        val mode = ThemeMode.fromString(modeName)
        settings = settings.copy(
            themeModeName = mode.name,
            autoSave = autoSaveSwitch.isChecked
        )
        themeModeSelect.selectedValue = mode.name
        refreshUiFromSettings()
        onThemeUpdated?.invoke(mode, settings.themeColorHex)
        persistIfAutoSave()
    }

    fun updateThemeColor(colorHex: String) {
        syncSwitchStateIfNeeded()
        val normalized = colorHex.trim().uppercase()
        settings = settings.copy(
            themeColorHex = normalized,
            autoSave = autoSaveSwitch.isChecked
        )
        refreshUiFromSettings()
        onThemeUpdated?.invoke(settings.resolvedThemeMode(), normalized)
        persistIfAutoSave()
    }

    fun setAutoSave(enabled: Boolean) {
        lastObservedAutoSave = enabled
        autoSaveSwitch.isChecked = enabled
        settings = settings.copy(autoSave = enabled)
        refreshStatusBadges()
        if (enabled) {
            CounterSettings.saveTo(preferenceStore, settings)
        }
    }

    fun saveSettingsNow() {
        syncSwitchStateIfNeeded()
        CounterSettings.saveTo(preferenceStore, settings)
    }

    fun reloadFromStore() {
        settings = CounterSettings.loadFrom(preferenceStore)
        lastObservedAutoSave = settings.autoSave
        refreshUiFromSettings()
        onThemeUpdated?.invoke(settings.resolvedThemeMode(), settings.themeColorHex)
    }

    private fun persistIfAutoSave() {
        if (autoSaveSwitch.isChecked) {
            CounterSettings.saveTo(preferenceStore, settings)
        }
    }

    private fun refreshUiFromSettings() {
        counterValueBadge.text = settings.count.toString()
        counterValueBadge.getBootstrap().type = when {
            settings.count > 0 -> BootstrapType.PRIMARY
            settings.count < 0 -> BootstrapType.WARNING
            else -> BootstrapType.SECONDARY
        }
        val accentRgba = RgbaColor.fromHex(settings.themeColorHex)
        autoSaveSwitch.getStyle().accentColor = accentRgba
        incrementButton.getStyle().accentColor = accentRgba
        stepSelect.selectedValue = settings.stepSize.toString()
        themeModeSelect.selectedValue = settings.themeModeName
        autoSaveSwitch.isChecked = settings.autoSave
        lastObservedAutoSave = settings.autoSave
        refreshStatusBadges()
    }

    private fun refreshStatusBadges() {
        activeThemeBadge.text = "${settings.themeModeName} (${settings.themeColorHex})"
        persistenceStatusBadge.text = if (settings.autoSave) "Auto-Save: ON" else "Auto-Save: OFF"
        persistenceStatusBadge.getBootstrap().type = if (settings.autoSave) BootstrapType.SUCCESS else BootstrapType.WARNING
    }
}
