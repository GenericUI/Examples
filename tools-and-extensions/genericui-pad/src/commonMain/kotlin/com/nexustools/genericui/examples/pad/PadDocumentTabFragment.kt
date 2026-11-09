package com.nexustools.genericui.examples.pad

import com.nexustools.genericui.component.form.TextArea
import com.nexustools.genericui.component.fragment.AppFragment
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.component.typography.MarkdownView
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.SpacingScale

/**
 * Single document tab editor [Fragment] inside `GenericUIPad`.
 *
 * Hosts the primary [TextArea] editor surface, live spell-check findings bar with wavy red
 * underlines for technical identifiers (such as `CharGenView` and `DCGame`), and an optional
 * live [MarkdownView] preview pane when [PadDocument.mode] is [PadDocumentMode.MARKDOWN].
 */
@AppFragment
class PadDocumentTabFragment(
    initialDocument: PadDocument = PadDocument(
        docId = "tab-default",
        title = "Untitled",
        content = ""
    ),
    initialSpellCheckEnabled: Boolean = true
) : Fragment() {

    var spellCheckEnabled: Boolean = initialSpellCheckEnabled
        private set

    var document: PadDocument = initialDocument.let { doc ->
        val (endLine, endCol) = if (doc.cursorLine == 1 && doc.cursorCol == 1 && doc.content.isNotEmpty()) {
            PadDocument.computeEndCursorPosition(doc.content)
        } else {
            doc.cursorLine to doc.cursorCol
        }
        val findings = if (doc.spellCheckFindings.isNotEmpty() || !initialSpellCheckEnabled) {
            doc.spellCheckFindings
        } else {
            PadDocument.detectSpellCheckFindings(doc.content, initialSpellCheckEnabled)
        }
        doc.copy(
            cursorLine = endLine,
            cursorCol = endCol,
            spellCheckFindings = findings
        )
    }
        private set

    /** Callback notified whenever [document] content, mode, cursor, or zoom state changes. */
    var onDocumentChanged: ((PadDocument) -> Unit)? = null

    /** Primary multi-line text editor surface (`#1C1C1F`). */
    val editorTextArea: TextArea = TextArea(
        value = document.content,
        placeholder = "Type text or Markdown...",
        rows = 16
    ).apply {
        id = "pad-editor-${document.docId}"
        addClass("pad-editor-textarea")
    }

    /** Spell-check findings bar highlighting technical identifiers with red wavy underlines. */
    val spellCheckBar: HStack = HStack(gap = SpacingScale.S2).apply {
        addClass("pad-spellcheck-bar")
        flexWrap = FlexWrap.WRAP
    }

    private val _spellCheckBadges = mutableListOf<Badge>()

    /** Currently rendered spell-check token badges (e.g. `CharGenView`, `DCGame`). */
    val spellCheckBadges: List<Badge>
        get() = _spellCheckBadges.toList()

    /** Live Markdown preview component shown when [PadDocument.mode] is [PadDocumentMode.MARKDOWN]. */
    val markdownPreviewPane: MarkdownView = MarkdownView(markdown = document.content).apply {
        addClass("pad-markdown-preview")
        isVisible = (document.mode == PadDocumentMode.MARKDOWN)
    }

    init {
        addClass("pad-tab-fragment")
        routeKey = document.docId
        pageTitle = "${document.title} - Notepad"

        val rootStack = VStack(gap = SpacingScale.S2)
        rootStack.addChild(editorTextArea)
        rootStack.addChild(spellCheckBar)
        rootStack.addChild(markdownPreviewPane)
        addChild(rootStack)

        editorTextArea.onValueChanged = { updatedText ->
            updateContent(updatedText, markModified = true)
        }

        rebuildSpellCheckBar()
        syncEditorStyles()
    }

    /**
     * Replaces the document text content, recalculating cursor coordinates, character count,
     * spell-check findings, and live Markdown preview.
     */
    fun updateContent(
        newContent: String,
        markModified: Boolean = true,
        cursorLine: Int? = null,
        cursorCol: Int? = null
    ) {
        val (computedLine, computedCol) = PadDocument.computeEndCursorPosition(newContent)
        val findings = PadDocument.detectSpellCheckFindings(newContent, spellCheckEnabled)
        document = document.copy(
            content = newContent,
            isModified = if (markModified) true else document.isModified,
            cursorLine = cursorLine ?: computedLine,
            cursorCol = cursorCol ?: computedCol,
            spellCheckFindings = findings
        )
        if (editorTextArea.value != newContent) {
            editorTextArea.value = newContent
        }
        markdownPreviewPane.markdown = newContent
        rebuildSpellCheckBar()
        onDocumentChanged?.invoke(document)
    }

    /**
     * Updates the 1-based cursor line and column reported in the bottom status bar.
     */
    fun setCursorPosition(line: Int, col: Int) {
        val safeLine = line.coerceAtLeast(1)
        val safeCol = col.coerceAtLeast(1)
        if (document.cursorLine == safeLine && document.cursorCol == safeCol) return
        document = document.copy(cursorLine = safeLine, cursorCol = safeCol)
        onDocumentChanged?.invoke(document)
    }

    /**
     * Switches this tab between [PadDocumentMode.PLAIN_TEXT] and [PadDocumentMode.MARKDOWN].
     */
    fun setDocumentMode(mode: PadDocumentMode) {
        document = document.copy(mode = mode)
        markdownPreviewPane.markdown = document.content
        markdownPreviewPane.isVisible = (mode == PadDocumentMode.MARKDOWN)
        onDocumentChanged?.invoke(document)
    }

    /**
     * Sets the editor zoom percentage (clamped to `25..500`).
     */
    fun setZoomPercent(zoomPercent: Int) {
        val clamped = zoomPercent.coerceIn(25, 500)
        document = document.copy(zoomPercent = clamped)
        syncEditorStyles()
        onDocumentChanged?.invoke(document)
    }

    /**
     * Sets the document's line-ending convention (`WINDOWS_CRLF`, `UNIX_LF`, or `CLASSIC_MAC_CR`).
     */
    fun setLineEnding(lineEnding: PadLineEnding) {
        document = document.copy(lineEnding = lineEnding)
        onDocumentChanged?.invoke(document)
    }

    /**
     * Enables or disables spell-check identifier detection for this tab.
     */
    fun setSpellCheck(enabled: Boolean) {
        spellCheckEnabled = enabled
        val findings = PadDocument.detectSpellCheckFindings(document.content, enabled)
        document = document.copy(spellCheckFindings = findings)
        rebuildSpellCheckBar()
        onDocumentChanged?.invoke(document)
    }

    /**
     * Applies a rich formatting or Markdown action from the Windows 11 Notepad command bar.
     */
    fun applyFormatAction(action: PadFormatAction): String {
        val current = document.content
        val transformed = when (action) {
            PadFormatAction.HEADING_1 -> applyHeadingPrefix(current, "# ")
            PadFormatAction.HEADING_2 -> applyHeadingPrefix(current, "## ")
            PadFormatAction.HEADING_3 -> applyHeadingPrefix(current, "### ")
            PadFormatAction.BULLET_LIST -> {
                if (current.isBlank()) {
                    "- List item"
                } else {
                    current.split('\n').joinToString("\n") { line ->
                        val stripped = line.removePrefix("- ").removePrefix("* ")
                        "- $stripped"
                    }
                }
            }
            PadFormatAction.NUMBERED_LIST -> {
                if (current.isBlank()) {
                    "1. List item"
                } else {
                    current.split('\n').mapIndexed { idx, line ->
                        val stripped = line.replace(Regex("""^\d+\.\s+"""), "")
                        "${idx + 1}. $stripped"
                    }.joinToString("\n")
                }
            }
            PadFormatAction.BOLD -> {
                if (current.isBlank()) "**Bold**" else "**$current**"
            }
            PadFormatAction.ITALIC -> {
                if (current.isBlank()) "*Italic*" else "*$current*"
            }
            PadFormatAction.STRIKETHROUGH -> {
                if (current.isBlank()) "~~Strikethrough~~" else "~~$current~~"
            }
            PadFormatAction.LINK -> {
                if (current.isBlank()) {
                    "[Link](https://example.com)"
                } else {
                    "$current [Link](https://example.com)"
                }
            }
            PadFormatAction.TABLE -> {
                val tableSnippet = "| Column 1 | Column 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |"
                if (current.isBlank()) tableSnippet else "$current\n$tableSnippet"
            }
            PadFormatAction.CLEAR_FORMATTING -> {
                current
                    .replace(Regex("""^#{1,6}\s+""", RegexOption.MULTILINE), "")
                    .replace(Regex("""^[-*]\s+""", RegexOption.MULTILINE), "")
                    .replace(Regex("""^\d+\.\s+""", RegexOption.MULTILINE), "")
                    .replace("**", "")
                    .replace("~~", "")
                    .replace("*", "")
            }
        }
        updateContent(transformed, markModified = true)
        return document.content
    }

    /**
     * Finds the next occurrence of [query] starting at [startIndex] (wrapping around if needed).
     * Updates the cursor line and column to the match location and returns the 0-based character index, or `-1`.
     */
    fun findNext(query: String, startIndex: Int = 0): Int {
        if (query.isEmpty() || document.content.isEmpty()) return -1
        val text = document.content
        val safeStart = startIndex.coerceIn(0, text.length)
        var idx = text.indexOf(query, startIndex = safeStart, ignoreCase = true)
        if (idx < 0 && safeStart > 0) {
            idx = text.indexOf(query, startIndex = 0, ignoreCase = true)
        }
        if (idx >= 0) {
            val prefix = text.substring(0, idx)
            val (line, col) = PadDocument.computeEndCursorPosition(prefix)
            setCursorPosition(line, col)
        }
        return idx
    }

    /**
     * Replaces all occurrences of [query] with [replacement] in this tab's content.
     *
     * @return Total number of occurrences replaced.
     */
    fun replaceAll(query: String, replacement: String): Int {
        if (query.isEmpty() || document.content.isEmpty()) return 0
        val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
        val matches = regex.findAll(document.content).count()
        if (matches > 0) {
            val updated = regex.replace(document.content, replacement)
            updateContent(updated, markModified = true)
        }
        return matches
    }

    private fun applyHeadingPrefix(text: String, prefix: String): String {
        if (text.isBlank()) return "${prefix}Heading"
        val lines = text.split('\n').toMutableList()
        val firstCleaned = lines[0].replace(Regex("""^#{1,6}\s+"""), "")
        lines[0] = "$prefix$firstCleaned"
        return lines.joinToString("\n")
    }

    private fun rebuildSpellCheckBar() {
        spellCheckBar.clearChildren()
        _spellCheckBadges.clear()
        val findings = document.spellCheckFindings
        if (!spellCheckEnabled || findings.isEmpty()) {
            spellCheckBar.isVisible = false
            return
        }
        spellCheckBar.isVisible = true
        spellCheckBar.addChild(Label("Spell check:").apply {
            addClass("pad-spellcheck-label")
            widthPx = 92
        })
        for (word in findings) {
            val badge = Badge(word, type = BootstrapType.DANGER, pill = true).apply {
                addClass("pad-spellcheck-token")
                widthPx = (word.length * 6 + 22).coerceAtLeast(64)
            }
            _spellCheckBadges.add(badge)
            spellCheckBar.addChild(badge)
        }
    }

    private fun syncEditorStyles() {
        val baseFontPx = 15f
        val scaledPx = (baseFontPx * (document.zoomPercent / 100f)).coerceIn(9f, 48f)
        editorTextArea.getStyle().fontSizePx = scaledPx
    }
}
