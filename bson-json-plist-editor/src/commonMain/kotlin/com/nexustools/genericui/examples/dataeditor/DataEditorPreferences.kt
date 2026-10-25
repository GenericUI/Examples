package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore

/**
 * Persisted user preferences for the visual BSON / JSON / Binary Plist Editor stored via [PreferenceStore].
 */
data class DataEditorPreferences(
    val prettyPrintIndent: Int = 2,
    val showTypeBadges: Boolean = true,
    val showByteSizeMetrics: Boolean = true,
    val recentFileNames: List<String> = emptyList()
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "prettyPrintIndent" to prettyPrintIndent,
        "showTypeBadges" to showTypeBadges,
        "showByteSizeMetrics" to showByteSizeMetrics,
        "recentFileNames" to recentFileNames
    )

    fun withRecentFile(fileName: String): DataEditorPreferences {
        val clean = fileName.trim()
        if (clean.isEmpty()) return this
        val updated = buildList {
            add(clean)
            for (existing in recentFileNames) {
                if (existing != clean && size < MAX_RECENT_FILES) {
                    add(existing)
                }
            }
        }
        return copy(recentFileNames = updated)
    }

    companion object {
        const val PREFERENCES_KEY: String = "editor_preferences"
        const val MAX_RECENT_FILES: Int = 10

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(DataEditorPreferences::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): DataEditorPreferences {
            val indent = fields["prettyPrintIndent"]?.asInt(2)?.coerceIn(1, 8) ?: 2
            val showBadges = fields["showTypeBadges"]?.asBoolean(true) ?: true
            val showMetrics = fields["showByteSizeMetrics"]?.asBoolean(true) ?: true
            val recentsElem = fields["recentFileNames"]
            val recents = if (recentsElem != null && recentsElem.isArray) {
                recentsElem.arrayElements.mapNotNull { it.asString() }.filter { it.isNotBlank() }
            } else {
                emptyList()
            }
            return DataEditorPreferences(
                prettyPrintIndent = indent,
                showTypeBadges = showBadges,
                showByteSizeMetrics = showMetrics,
                recentFileNames = recents
            )
        }

        fun loadFrom(store: PreferenceStore): DataEditorPreferences {
            registerFactory()
            return store.getOrDefault(PREFERENCES_KEY, DataEditorPreferences())
        }

        fun saveTo(store: PreferenceStore, prefs: DataEditorPreferences) {
            registerFactory()
            store.put(PREFERENCES_KEY, prefs)
            store.flush()
        }
    }
}
