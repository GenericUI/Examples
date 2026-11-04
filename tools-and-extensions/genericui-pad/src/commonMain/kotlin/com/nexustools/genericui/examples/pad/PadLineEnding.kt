package com.nexustools.genericui.examples.pad

/**
 * Line-ending conventions supported by `GenericUIPad` documents and status bar display.
 */
enum class PadLineEnding(
    val label: String,
    val sequence: String
) {
    WINDOWS_CRLF("Windows (CRLF)", "\r\n"),
    UNIX_LF("Unix (LF)", "\n"),
    CLASSIC_MAC_CR("Macintosh (CR)", "\r");

    companion object {
        fun fromLabelOrName(raw: String?): PadLineEnding {
            if (raw.isNullOrBlank()) return WINDOWS_CRLF
            val trimmed = raw.trim()
            return entries.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true) ||
                    it.label.equals(trimmed, ignoreCase = true)
            } ?: WINDOWS_CRLF
        }
    }
}
