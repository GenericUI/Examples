package com.nexustools.genericui.examples.pad

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.io.Readable
import com.nexustools.genericui.io.Writable
import com.nexustools.genericui.util.BinaryJson
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Persisted session settings for `GenericUIPad`, stored via [PreferenceStore] (`"pad.settings"`)
 * and exportable/importable over [Writable] / [Readable] streams using [BinaryJson].
 */
data class PadSessionSettings(
    val wordWrap: Boolean = true,
    val spellCheckEnabled: Boolean = true,
    val showStatusBar: Boolean = true,
    val defaultZoomPercent: Int = 100,
    val defaultEncoding: String = "UTF-8",
    val activeTabId: String = "tab-chargen",
    val openTabCount: Int = 6
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "wordWrap" to wordWrap,
        "spellCheckEnabled" to spellCheckEnabled,
        "showStatusBar" to showStatusBar,
        "defaultZoomPercent" to defaultZoomPercent,
        "defaultEncoding" to defaultEncoding,
        "activeTabId" to activeTabId,
        "openTabCount" to openTabCount
    )

    companion object {
        const val SETTINGS_KEY: String = "pad.settings"

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(PadSessionSettings::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): PadSessionSettings {
            return PadSessionSettings(
                wordWrap = fields["wordWrap"]?.asBoolean(true) ?: true,
                spellCheckEnabled = fields["spellCheckEnabled"]?.asBoolean(true) ?: true,
                showStatusBar = fields["showStatusBar"]?.asBoolean(true) ?: true,
                defaultZoomPercent = fields["defaultZoomPercent"]?.asInt(100)?.coerceIn(25, 500) ?: 100,
                defaultEncoding = fields["defaultEncoding"]?.asString("UTF-8") ?: "UTF-8",
                activeTabId = fields["activeTabId"]?.asString("tab-chargen") ?: "tab-chargen",
                openTabCount = fields["openTabCount"]?.asInt(6)?.coerceAtLeast(1) ?: 6
            )
        }

        fun saveToStore(store: PreferenceStore, settings: PadSessionSettings) {
            registerFactory()
            store.put(SETTINGS_KEY, settings)
            store.flush()
        }

        fun loadFromStore(
            store: PreferenceStore,
            defaultSettings: PadSessionSettings = PadSessionSettings()
        ): PadSessionSettings {
            registerFactory()
            return store.get(SETTINGS_KEY, defaultSettings) ?: defaultSettings
        }

        fun exportToWritable(settings: PadSessionSettings, writable: Writable) {
            registerFactory()
            BinaryJson.encode(settings, writable)
        }

        fun importFromReadable(
            readable: Readable,
            defaultSettings: PadSessionSettings = PadSessionSettings()
        ): PadSessionSettings {
            registerFactory()
            var result: Result<PadSessionSettings>? = null
            val block: suspend () -> PadSessionSettings = {
                BinaryJson.decodeAs(readable, PadSessionSettings::class, defaultSettings)
                    ?: defaultSettings
            }
            block.startCoroutine(Continuation(EmptyCoroutineContext) { outcome ->
                result = outcome
            })
            return result?.getOrNull() ?: defaultSettings
        }
    }
}
