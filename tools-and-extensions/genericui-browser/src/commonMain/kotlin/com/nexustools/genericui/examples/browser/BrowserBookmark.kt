package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.io.ByteOrder
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer

/**
 * Represents a bookmarked web page displayed on the Chrome bookmarks bar and New Tab speed-dial grid.
 */
data class BrowserBookmark(
    val id: String,
    val title: String,
    val url: String,
    val iconLabel: String = "*"
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "id" to id,
        "title" to title,
        "url" to url,
        "iconLabel" to iconLabel
    )

    /**
     * Writes this bookmark into [writable] in binary format.
     */
    fun writeToBuffer(writable: WritableByteBuffer) {
        writeUtf8Field(writable, id)
        writeUtf8Field(writable, title)
        writeUtf8Field(writable, url)
        writeUtf8Field(writable, iconLabel)
    }

    companion object {
        const val BOOKMARK_MAGIC: Int = 0x4755424D // "GUBM"

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(BrowserBookmark::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): BrowserBookmark = BrowserBookmark(
            id = fields["id"]?.asString("bm-default") ?: "bm-default",
            title = fields["title"]?.asString("Bookmark") ?: "Bookmark",
            url = fields["url"]?.asString("https://one.google.com/ai/activity") ?: "https://one.google.com/ai/activity",
            iconLabel = fields["iconLabel"]?.asString("*") ?: "*"
        )

        /**
         * Reads a single [BrowserBookmark] from [readable].
         */
        fun readFromBuffer(readable: ReadableByteBuffer): BrowserBookmark = BrowserBookmark(
            id = readUtf8Field(readable),
            title = readUtf8Field(readable),
            url = readUtf8Field(readable),
            iconLabel = readUtf8Field(readable).ifEmpty { "*" }
        )

        /**
         * Exports a list of [BrowserBookmark] items into [writable].
         */
        fun writeListToBuffer(bookmarks: List<BrowserBookmark>, writable: WritableByteBuffer) {
            writable.writeInt(BOOKMARK_MAGIC, ByteOrder.BIG_ENDIAN)
            writable.writeInt(bookmarks.size, ByteOrder.BIG_ENDIAN)
            for (bookmark in bookmarks) {
                bookmark.writeToBuffer(writable)
            }
        }

        /**
         * Imports a list of [BrowserBookmark] items from [readable].
         */
        fun readListFromBuffer(readable: ReadableByteBuffer): List<BrowserBookmark> {
            if (readable.remaining < 8) return emptyList()
            val magic = readable.nextInt(ByteOrder.BIG_ENDIAN)
            require(magic == BOOKMARK_MAGIC) {
                "Invalid BrowserBookmark binary header: 0x${magic.toUInt().toString(16)}"
            }
            val count = readable.nextInt(ByteOrder.BIG_ENDIAN).coerceIn(0, 10_000)
            return List(count) {
                readFromBuffer(readable)
            }
        }

        fun defaultBookmarks(): List<BrowserBookmark> = listOf(
            BrowserBookmark(
                id = "bm-ai-credits",
                title = "AI credits activity",
                url = "https://one.google.com/ai/activity",
                iconLabel = "✦"
            ),
            BrowserBookmark(
                id = "bm-github-berth",
                title = "T-Crypt/berth",
                url = "https://github.com/T-Crypt/berth",
                iconLabel = "</>"
            ),
            BrowserBookmark(
                id = "bm-keep",
                title = "Google Keep",
                url = "https://keep.google.com",
                iconLabel = "[!]"
            ),
            BrowserBookmark(
                id = "bm-gemini",
                title = "Ask Gemini",
                url = "https://gemini.google.com/app",
                iconLabel = "★"
            )
        )

        private fun writeUtf8Field(writable: WritableByteBuffer, text: String) {
            val bytes = text.encodeToByteArray()
            writable.writeInt(bytes.size, ByteOrder.BIG_ENDIAN)
            if (bytes.isNotEmpty()) {
                writable.writeBytes(bytes, 0, bytes.size)
            }
        }

        private fun readUtf8Field(readable: ReadableByteBuffer): String {
            if (readable.remaining < 4) return ""
            val length = readable.nextInt(ByteOrder.BIG_ENDIAN).coerceAtLeast(0)
            if (length == 0) return ""
            return readable.nextString(length, "utf8")
        }
    }
}
