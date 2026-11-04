package com.nexustools.genericui.examples.pad

/**
 * Document editing mode displayed in the Windows 11 Notepad status bar and editor pane.
 */
enum class PadDocumentMode(
    val label: String
) {
    PLAIN_TEXT("Plain text"),
    MARKDOWN("Markdown");

    companion object {
        fun fromLabelOrName(raw: String?): PadDocumentMode {
            if (raw.isNullOrBlank()) return PLAIN_TEXT
            val trimmed = raw.trim()
            return entries.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true) ||
                    it.label.equals(trimmed, ignoreCase = true)
            } ?: PLAIN_TEXT
        }
    }
}
