package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.feedback.Alert
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.Switch
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.Column
import com.nexustools.genericui.component.layout.ColumnContainer
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.system.StatusBar
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.color.RgbaColor
import kotlin.math.abs

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
    private val customPreferenceStore: PreferenceStore? = null,
    var onThemeUpdated: ((ThemeMode, String) -> Unit)? = null
) : Fragment() {

    override fun getPreferenceStore(): PreferenceStore =
        customPreferenceStore ?: super.getPreferenceStore()

    var settings: CounterSettings = CounterSettings.loadFrom(getPreferenceStore())
        private set

    val counterValueBadge: Badge = Badge(
        text = settings.count.toString(),
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "counter-value-badge"
        widthPx = 68
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

    val counterMagnitudeBar: ProgressBar = ProgressBar(
        progress = (abs(settings.count) % 100) / 100f,
        type = BootstrapType.PRIMARY
    ).apply {
        id = "progress-counter-magnitude"
        widthPx = 220
        label = "Count: ${settings.count}"
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

    val quickStep1Button: Button = Button(
        text = "x1",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM,
        outline = false
    ).apply {
        id = "btn-quick-step-1"
    }

    val quickStep5Button: Button = Button(
        text = "x5",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-quick-step-5"
    }

    val quickStep10Button: Button = Button(
        text = "x10",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-quick-step-10"
    }

    val themeModeSelect: Select = Select(
        options = CounterSettings.THEME_MODE_OPTIONS.map { SelectOption(it, it) },
        selectedValue = settings.themeModeName
    ).apply {
        id = "select-theme-mode"
    }

    val bluePresetButton: Button = Button(
        text = "Blue #2563EB",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-color-blue"
    }

    val emeraldPresetButton: Button = Button(
        text = "Emerald #10B981",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-color-emerald"
    }

    val purplePresetButton: Button = Button(
        text = "Purple #8B5CF6",
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
        type = BootstrapType.INFO,
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

    val stepMultiplierBadge: Badge = Badge(
        text = "Step: +/-${settings.stepSize}",
        type = BootstrapType.PRIMARY
    ).apply {
        id = "badge-step-multiplier"
    }

    val summaryAlert: Alert = Alert(
        message = "Reactive Counter & Theme Switcher ready — changes sync to PreferenceStore (CounterThemeExample).",
        type = BootstrapType.INFO
    ).apply {
        id = "alert-counter-summary"
    }

    val shortcutsTable: Table = Table().apply {
        id = "table-shortcuts"
        setColumns(
            listOf(
                TableColumn("action", "Action"),
                TableColumn("shortcut", "Trigger"),
                TableColumn("menu", "Menu / Target"),
                TableColumn("effect", "Effect")
            )
        )
        setColumnWeights(1.2f, 1.0f, 1.3f, 1.5f)
        setRows(
            listOf(
                TableRow("inc", listOf(TableCell("Increment"), TableCell("Ctrl+Up"), TableCell("Counter > Increment"), TableCell("Adds +stepSize"))),
                TableRow("dec", listOf(TableCell("Decrement"), TableCell("Ctrl+Down"), TableCell("Counter > Decrement"), TableCell("Subtracts -stepSize"))),
                TableRow("rst", listOf(TableCell("Reset to 0"), TableCell("Ctrl+R"), TableCell("Counter > Reset to 0"), TableCell("Resets count to 0"))),
                TableRow("sav", listOf(TableCell("Save Settings"), TableCell("Ctrl+S"), TableCell("Counter > Save"), TableCell("Flushes to store"))),
                TableRow("stp", listOf(TableCell("Step Multiplier"), TableCell("Select / x1..x10"), TableCell("Step Size Controls"), TableCell("Sets step (1, 5, 10)"))),
                TableRow("thm", listOf(TableCell("Theme Mode"), TableCell("Menu / Select"), TableCell("Theme > Mode"), TableCell("AUTO / LIGHT / DARK"))),
                TableRow("acc", listOf(TableCell("Accent Preset"), TableCell("Menu / Swatch"), TableCell("Theme > Accent"), TableCell("Updates themeColor"))),
                TableRow("aut", listOf(TableCell("Auto-Save Toggle"), TableCell("Switch"), TableCell("Persistence Row"), TableCell("Toggles auto-flush")))
            )
        )
    }

    val storeInspectorTable: Table = Table().apply {
        id = "table-store-inspector"
        setColumns(
            listOf(
                TableColumn("key", "Preference Key"),
                TableColumn("type", "Type"),
                TableColumn("val", "Live Value"),
                TableColumn("sync", "Sync Mode")
            )
        )
        setColumnWeights(1.4f, 0.9f, 1.1f, 1.0f)
    }

    val statusFooterBar: StatusBar = StatusBar().apply {
        id = "status-footer-bar"
        addClass("status-footer")
        addChild(Text("Store: ${CounterSettings.DEFAULT_STORE_NAME}"))
        addChild(Text("Key: ${CounterSettings.SETTINGS_KEY}"))
        addChild(Text("Codec: PreferenceObjectCodec"))
        addChild(Text("Stylesheet: Resource.style.styles"))
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
                CounterSettings.saveTo(getPreferenceStore(), settings)
            }
        }
    }

    private fun ensureUiBuilt() {
        if (uiBuilt) return
        uiBuilt = true
        clearChildren()
        addClass("counter-root")

        val headerRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        headerRow.addChild(Heading("Counter & Theme Switcher", level = 3))
        headerRow.addChild(activeThemeBadge)
        headerRow.addChild(persistenceStatusBadge)
        headerRow.addChild(stepMultiplierBadge)
        addChild(headerRow)

        addChild(summaryAlert)

        val mainGrid = ColumnContainer(gutter = SpacingScale.THREE)

        // Left Column: Reactive Counter Card + Keyboard Shortcuts Table Card
        val leftCol = Column(span = 12, md = 6, lg = 6)

        val counterCard = Card("Reactive Counter & Step Multiplier").apply {
            heightPx = 210
            addClass("counter-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }
        val badgeRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        badgeRow.addChild(Text("Current Count:"))
        badgeRow.addChild(counterValueBadge)
        badgeRow.addChild(counterMagnitudeBar)
        counterCard.addChild(badgeRow)

        val actionButtonsRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        actionButtonsRow.addChild(decrementButton)
        actionButtonsRow.addChild(incrementButton)
        actionButtonsRow.addChild(resetButton)
        counterCard.addChild(actionButtonsRow)

        val stepRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        stepRow.addChild(Text("Step Size:"))
        stepRow.addChild(stepSelect)
        stepRow.addChild(quickStep1Button)
        stepRow.addChild(quickStep5Button)
        stepRow.addChild(quickStep10Button)
        counterCard.addChild(stepRow)

        leftCol.addChild(counterCard)

        val shortcutsCard = Card("Menu Bar Commands & Keyboard Shortcuts").apply {
            addClass("counter-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }
        shortcutsCard.addChild(shortcutsTable)
        leftCol.addChild(shortcutsCard)

        // Right Column: Theme & Persistence Card + PreferenceStore Inspector Card
        val rightCol = Column(span = 12, md = 6, lg = 6)

        val themeCard = Card("Theme Mode, Accent Color & Persistence").apply {
            heightPx = 210
            addClass("theme-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }

        val modeRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        modeRow.addChild(Text("Theme Mode:"))
        modeRow.addChild(themeModeSelect)
        modeRow.addChild(Badge("System-Aware", type = BootstrapType.SECONDARY))
        themeCard.addChild(modeRow)

        val colorPresetRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        colorPresetRow.addChild(Text("Accent Color:"))
        colorPresetRow.addChild(bluePresetButton)
        colorPresetRow.addChild(emeraldPresetButton)
        colorPresetRow.addChild(purplePresetButton)
        themeCard.addChild(colorPresetRow)

        val persistenceRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        persistenceRow.addChild(autoSaveSwitch)
        persistenceRow.addChild(saveNowButton)
        themeCard.addChild(persistenceRow)

        rightCol.addChild(themeCard)

        val storeInspectorCard = Card("PreferenceStore Live State Inspector").apply {
            addClass("theme-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }
        storeInspectorCard.addChild(storeInspectorTable)
        rightCol.addChild(storeInspectorCard)

        mainGrid.addColumn(leftCol)
        mainGrid.addColumn(rightCol)
        addChild(mainGrid)

        addChild(statusFooterBar)

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
        quickStep1Button.setOnClick {
            updateStepSize(1)
        }
        quickStep5Button.setOnClick {
            updateStepSize(5)
        }
        quickStep10Button.setOnClick {
            updateStepSize(10)
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
            CounterSettings.saveTo(getPreferenceStore(), settings)
        }
    }

    fun saveSettingsNow() {
        syncSwitchStateIfNeeded()
        CounterSettings.saveTo(getPreferenceStore(), settings)
    }

    fun reloadFromStore() {
        settings = CounterSettings.loadFrom(getPreferenceStore())
        lastObservedAutoSave = settings.autoSave
        refreshUiFromSettings()
        onThemeUpdated?.invoke(settings.resolvedThemeMode(), settings.themeColorHex)
    }

    private fun persistIfAutoSave() {
        if (autoSaveSwitch.isChecked) {
            CounterSettings.saveTo(getPreferenceStore(), settings)
        }
    }

    private fun refreshUiFromSettings() {
        counterValueBadge.text = settings.count.toString()
        counterValueBadge.getBootstrap().type = when {
            settings.count > 0 -> BootstrapType.PRIMARY
            settings.count < 0 -> BootstrapType.WARNING
            else -> BootstrapType.SECONDARY
        }
        counterMagnitudeBar.progress = ((abs(settings.count) % 100) / 100f).coerceIn(0.05f, 1f)
        counterMagnitudeBar.label = "Count: ${settings.count} (Step ${settings.stepSize})"

        quickStep1Button.outline = settings.stepSize != 1
        quickStep1Button.type = if (settings.stepSize == 1) BootstrapType.PRIMARY else BootstrapType.INFO
        quickStep5Button.outline = settings.stepSize != 5
        quickStep5Button.type = if (settings.stepSize == 5) BootstrapType.PRIMARY else BootstrapType.INFO
        quickStep10Button.outline = settings.stepSize != 10
        quickStep10Button.type = if (settings.stepSize == 10) BootstrapType.PRIMARY else BootstrapType.INFO

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
        stepMultiplierBadge.text = "Step: +/-${settings.stepSize}"
        summaryAlert.messageText.text =
            "Active Count: ${settings.count} | Step Multiplier: +/-${settings.stepSize} | Theme: ${settings.themeModeName} (${settings.themeColorHex}) | Persistence: ${if (settings.autoSave) "Auto-Save ON" else "Manual Save"}"
        val syncMode = if (settings.autoSave) "Auto-Saved" else "Pending Save"
        storeInspectorTable.setRows(
            listOf(
                TableRow("k-count", listOf(TableCell("settings.count"), TableCell("Int"), TableCell(settings.count.toString()), TableCell(syncMode))),
                TableRow("k-step", listOf(TableCell("settings.stepSize"), TableCell("Int"), TableCell(settings.stepSize.toString()), TableCell(syncMode))),
                TableRow("k-mode", listOf(TableCell("settings.themeMode"), TableCell("String"), TableCell(settings.themeModeName), TableCell(syncMode))),
                TableRow("k-color", listOf(TableCell("settings.themeColor"), TableCell("String"), TableCell(settings.themeColorHex), TableCell(syncMode))),
                TableRow("k-auto", listOf(TableCell("settings.autoSave"), TableCell("Boolean"), TableCell(settings.autoSave.toString()), TableCell("Immediate"))),
                TableRow("k-res", listOf(TableCell("app.resolvedTheme"), TableCell("ThemeMode"), TableCell(settings.resolvedThemeMode().name), TableCell("Runtime Computed"))),
                TableRow("k-boot", listOf(TableCell("ComponentBootstrap"), TableCell("Enum API"), TableCell("Isolated"), TableCell("Typed Snapshot"))),
                TableRow("k-styl", listOf(TableCell("ComponentStyle"), TableCell("Inline CSS"), TableCell("Accent Applied"), TableCell("Active")))
            )
        )
    }
}
