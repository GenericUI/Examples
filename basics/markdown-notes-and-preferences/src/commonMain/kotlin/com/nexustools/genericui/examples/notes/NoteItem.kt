package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable

/**
 * Structured note object stored in [com.nexustools.genericui.component.system.PreferenceStore]
 * under `"note.<id>"`.
 */
data class NoteItem(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var pinned: Boolean = false
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "id" to id,
        "title" to title,
        "content" to content,
        "pinned" to pinned
    )

    companion object {
        const val PREFERENCE_NAMESPACE = "MarkdownNotesExample"
        const val NOTE_IDS_KEY = "notes.ids"
        const val NOTE_KEY_PREFIX = "note."

        init {
            registerCodec()
        }

        fun registerCodec() {
            PreferenceObjectCodec.registerObjectFactory(NoteItem::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): NoteItem = NoteItem(
            id = fields["id"]?.asString("") ?: "",
            title = fields["title"]?.asString("") ?: "",
            content = fields["content"]?.asString("") ?: "",
            pinned = fields["pinned"]?.asBoolean(false) ?: false
        )
    }
}
