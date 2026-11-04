package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer
import com.nexustools.genericui.util.BinaryJson

/**
 * Persistent browser session state stored via [PreferenceStore] and exportable/importable
 * via [WritableByteBuffer], [ReadableByteBuffer], and [BinaryJson].
 */
data class BrowserSessionState(
    val homeUrl: String = DEFAULT_HOME_URL,
    val searchEnginePrefix: String = DEFAULT_SEARCH_PREFIX,
    val showBookmarksBar: Boolean = true,
    val askGeminiSidebarOpen: Boolean = false,
    val aiCreditsBalance: Int = DEFAULT_AI_CREDITS_BALANCE
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "homeUrl" to homeUrl,
        "searchEnginePrefix" to searchEnginePrefix,
        "showBookmarksBar" to showBookmarksBar,
        "askGeminiSidebarOpen" to askGeminiSidebarOpen,
        "aiCreditsBalance" to aiCreditsBalance
    )

    /**
     * Encodes this [BrowserSessionState] into [writable] using [BinaryJson].
     */
    fun writeToBuffer(writable: WritableByteBuffer) {
        BinaryJson.encode(this, writable)
    }

    /**
     * Encodes this [BrowserSessionState] into a binary JSON [ByteArray].
     */
    fun toBinaryJsonBytes(): ByteArray = BinaryJson.encode(this)

    companion object {
        const val SESSION_STATE_KEY: String = "browser.sessionState"
        const val BOOKMARKS_KEY: String = "browser.bookmarks"
        const val DEFAULT_HOME_URL: String = "https://one.google.com/ai/activity"
        const val DEFAULT_SEARCH_PREFIX: String = "https://www.google.com/search?q="
        const val DEFAULT_AI_CREDITS_BALANCE: Int = 6488

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(BrowserSessionState::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): BrowserSessionState = BrowserSessionState(
            homeUrl = fields["homeUrl"]?.asString(DEFAULT_HOME_URL) ?: DEFAULT_HOME_URL,
            searchEnginePrefix = fields["searchEnginePrefix"]?.asString(DEFAULT_SEARCH_PREFIX) ?: DEFAULT_SEARCH_PREFIX,
            showBookmarksBar = fields["showBookmarksBar"]?.asBoolean(true) ?: true,
            askGeminiSidebarOpen = fields["askGeminiSidebarOpen"]?.asBoolean(false) ?: false,
            aiCreditsBalance = fields["aiCreditsBalance"]?.asInt(DEFAULT_AI_CREDITS_BALANCE) ?: DEFAULT_AI_CREDITS_BALANCE
        )

        /**
         * Decodes a [BrowserSessionState] from a [ReadableByteBuffer] encoded with [BinaryJson].
         */
        fun readFromBuffer(readable: ReadableByteBuffer): BrowserSessionState {
            registerFactory()
            val bytes = readable.nextBytes(readable.remaining)
            return BinaryJson.decodeAs(bytes, BrowserSessionState::class, BrowserSessionState())
                ?: BrowserSessionState()
        }

        /**
         * Decodes a [BrowserSessionState] from raw [BinaryJson] bytes.
         */
        fun fromBinaryJsonBytes(bytes: ByteArray): BrowserSessionState {
            registerFactory()
            return BinaryJson.decodeAs(bytes, BrowserSessionState::class, BrowserSessionState())
                ?: BrowserSessionState()
        }

        /**
         * Loads the persisted [BrowserSessionState] from [store], or returns default values if absent.
         */
        fun loadFrom(store: PreferenceStore): BrowserSessionState {
            registerFactory()
            return store.getOrDefault(SESSION_STATE_KEY, BrowserSessionState())
        }

        /**
         * Saves [state] to [store] and flushes immediately.
         */
        fun saveTo(store: PreferenceStore, state: BrowserSessionState) {
            registerFactory()
            store.put(SESSION_STATE_KEY, state)
            store.flush()
        }
    }
}
