package com.nexustools.genericui.examples.pad

/**
 * Immutable snapshot of a single document tab in `GenericUIPad`.
 */
data class PadDocument(
    val docId: String,
    val title: String,
    val filePath: String? = null,
    val content: String,
    val isModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorCol: Int = 1,
    val zoomPercent: Int = 100,
    val lineEnding: PadLineEnding = PadLineEnding.WINDOWS_CRLF,
    val encoding: String = "UTF-8",
    val mode: PadDocumentMode = PadDocumentMode.PLAIN_TEXT,
    val spellCheckFindings: List<String> = emptyList()
) {
    /** Total number of characters in [content]. */
    val characterCount: Int
        get() = content.length

    /** Formatted cursor position for the first status bar column (e.g. `"Ln 1, Col 343"`). */
    val formattedCursorStatus: String
        get() = "Ln $cursorLine, Col $cursorCol"

    /** Formatted character count for the second status bar column (e.g. `"342 characters"`). */
    val formattedCharacterCountStatus: String
        get() = if (characterCount == 1) "1 character" else "$characterCount characters"

    /** Formatted zoom percentage for the fourth status bar column (e.g. `"100%"`). */
    val formattedZoomStatus: String
        get() = "$zoomPercent%"

    companion object {
        private val CAMEL_IDENTIFIER_REGEX = Regex("""\b[A-Z][a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*\b""")

        /**
         * Computes line and 1-based column coordinates when the cursor is placed at the end of [text].
         */
        fun computeEndCursorPosition(text: String): Pair<Int, Int> {
            if (text.isEmpty()) return 1 to 1
            val normalized = text.replace("\r\n", "\n").replace('\r', '\n')
            val lines = normalized.split('\n')
            val lineNum = lines.size.coerceAtLeast(1)
            val colNum = lines.last().length + 1
            return lineNum to colNum
        }

        /**
         * Detects technical mixed-case identifiers (such as `CharGenView` and `DCGame`) that Windows 11
         * Notepad highlights with a red wavy spell-check underline.
         */
        fun detectSpellCheckFindings(text: String, enabled: Boolean = true): List<String> {
            if (!enabled || text.isBlank()) return emptyList()
            val findings = LinkedHashSet<String>()
            for (match in CAMEL_IDENTIFIER_REGEX.findAll(text)) {
                val word = match.value
                if (word.length >= 4) {
                    findings.add(word)
                }
            }
            return findings.toList()
        }
    }
}
