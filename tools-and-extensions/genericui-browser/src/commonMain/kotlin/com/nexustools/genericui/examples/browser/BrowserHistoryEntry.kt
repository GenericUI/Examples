package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable

/**
 * Represents a visited URL entry in a tab's or session's navigation history.
 */
data class BrowserHistoryEntry(
    val url: String,
    val title: String,
    val timestampIso: String
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "url" to url,
        "title" to title,
        "timestampIso" to timestampIso
    )

    companion object {
        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(BrowserHistoryEntry::class) { fields ->
                BrowserHistoryEntry(
                    url = fields["url"]?.asString("") ?: "",
                    title = fields["title"]?.asString("") ?: "",
                    timestampIso = fields["timestampIso"]?.asString("2026-10-27T06:00:00Z") ?: "2026-10-27T06:00:00Z"
                )
            }
        }
    }
}
