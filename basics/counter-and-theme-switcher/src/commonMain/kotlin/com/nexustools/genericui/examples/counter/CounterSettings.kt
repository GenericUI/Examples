package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.ui.ThemeMode

/**
 * Persisted state model for the Counter & Theme Switcher example stored via [PreferenceStore].
 */
data class CounterSettings(
    val count: Int = 0,
    val stepSize: Int = 1,
    val themeModeName: String = "DARK",
    val themeColorHex: String = "#2563EB",
    val autoSave: Boolean = true
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "count" to count,
        "stepSize" to stepSize,
        "themeModeName" to themeModeName,
        "themeColorHex" to themeColorHex,
        "autoSave" to autoSave
    )

    fun resolvedThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name.equals(themeModeName, ignoreCase = true) }
            ?: ThemeMode.DARK

    companion object {
        const val DEFAULT_STORE_NAME: String = "CounterThemeExample"
        const val SETTINGS_KEY: String = "counter_settings"
        val STEP_OPTIONS: List<String> = listOf("1", "5", "10")
        val THEME_MODE_OPTIONS: List<String> = listOf("LIGHT", "DARK", "SYSTEM")
        val ACCENT_COLOR_PRESETS: List<String> = listOf("#2563EB", "#10B981", "#8B5CF6")

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(CounterSettings::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): CounterSettings {
            val countVal = fields["count"]?.asInt(0) ?: 0
            val stepVal = (fields["stepSize"]?.asInt(1) ?: 1).coerceAtLeast(1)
            val modeVal = fields["themeModeName"]?.asString("DARK") ?: "DARK"
            val colorVal = fields["themeColorHex"]?.asString("#2563EB") ?: "#2563EB"
            val autoSaveVal = fields["autoSave"]?.asBoolean(true) ?: true
            return CounterSettings(
                count = countVal,
                stepSize = stepVal,
                themeModeName = modeVal.uppercase(),
                themeColorHex = colorVal,
                autoSave = autoSaveVal
            )
        }

        fun loadFrom(store: PreferenceStore): CounterSettings {
            registerFactory()
            return store.getOrDefault(SETTINGS_KEY, CounterSettings())
        }

        fun saveTo(store: PreferenceStore, settings: CounterSettings) {
            registerFactory()
            store.put(SETTINGS_KEY, settings)
            store.flush()
        }
    }
}
