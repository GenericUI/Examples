package com.nexustools.genericui.examples.codeeditor

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.action.Toolbar
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.ScrollView
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.shell.SplitPane
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.system.StatusBar
import com.nexustools.genericui.component.tabs.CloseableTab
import com.nexustools.genericui.component.tabs.TabContainer
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.Orientation
import com.nexustools.genericui.ext.codeeditor.CodeDiagnostic
import com.nexustools.genericui.ext.codeeditor.CodeDiagnosticSeverity
import com.nexustools.genericui.ext.codeeditor.CodeEditorTheme
import com.nexustools.genericui.ext.codeeditor.CodeLanguage
import com.nexustools.genericui.ext.codeeditor.CursorPosition
import com.nexustools.genericui.ext.codeeditor.IdeCodeEditor
import com.nexustools.genericui.ext.codeeditor.TextRange

/**
 * Primary [Fragment] combining `:extensions:codeeditor` ([IdeCodeEditor] and [CodeLanguage])
 * with an interactive Regular Expression playground, capture-group inspector table,
 * multi-buffer [TabContainer], and [PreferenceStore]-backed snippet library.
 */
class CodeEditorRegexFragment(
    val preferenceStore: PreferenceStore = PreferenceStore("CodeEditorRegexExample")
) : Fragment() {

    private val snippetRegistry = linkedMapOf<String, CodeSnippetPreset>()
    var activeSnippetId: String = CodeSnippetPreset.BUILT_IN_PRESETS.first().id
        private set

    var lastRegexMatches: List<RegexMatchRecord> = emptyList()
        private set

    var lastRegexError: String? = null
        private set

    // Top Toolbar Controls
    val topToolbar: Toolbar = Toolbar()

    val languageSelect: Select = Select(
        options = listOf(
            SelectOption(CodeLanguage.KOTLIN.name, "Kotlin (.kt)"),
            SelectOption(CodeLanguage.TYPESCRIPT.name, "TypeScript (.ts)"),
            SelectOption(CodeLanguage.JAVASCRIPT.name, "JavaScript (.js)"),
            SelectOption(CodeLanguage.JSON.name, "JSON (.json)"),
            SelectOption(CodeLanguage.CSS.name, "CSS (.css)"),
            SelectOption(CodeLanguage.PYTHON.name, "Python (.py)"),
            SelectOption(CodeLanguage.RUST.name, "Rust (.rs)"),
            SelectOption(CodeLanguage.SQL.name, "SQL (.sql)"),
            SelectOption(CodeLanguage.MARKDOWN.name, "Markdown (.md)")
        ),
        selectedValue = CodeLanguage.KOTLIN.name
    ).apply {
        id = "select-language"
    }

    val editorThemeSelect: Select = Select(
        options = listOf(
            SelectOption("DARK_IDE", "Dark IDE"),
            SelectOption("DRACULA", "Dracula"),
            SelectOption("MONOKAI", "Monokai Pro"),
            SelectOption("LIGHT_IDE", "Light IDE")
        ),
        selectedValue = "DARK_IDE"
    ).apply {
        id = "select-editor-theme"
    }

    val snippetPresetSelect: Select = Select(
        options = CodeSnippetPreset.BUILT_IN_PRESETS.map { SelectOption(it.id, it.title) },
        selectedValue = CodeSnippetPreset.BUILT_IN_PRESETS.first().id
    ).apply {
        id = "select-snippet-preset"
    }

    val snippetTitleInput: TextField = TextField(
        value = CodeSnippetPreset.BUILT_IN_PRESETS.first().title,
        placeholder = "Custom snippet title..."
    ).apply {
        id = "input-snippet-title"
    }

    val saveSnippetButton: Button = Button(
        text = "Save Snippet",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-save-snippet"
    }

    val loadSnippetButton: Button = Button(
        text = "Load Snippet",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-load-snippet"
    }

    // Multi-Buffer Tab Bar
    val bufferTabContainer: TabContainer = TabContainer(fadeTransition = true).apply {
        id = "tabs-code-buffers"
    }

    // Main Split Workspace: Left = IdeCodeEditor, Right = Live Regex Tester & Capture Group Table
    val workspaceSplitPane: SplitPane = SplitPane(
        orientation = Orientation.HORIZONTAL,
        dividerPosition = 0.56f
    ).apply {
        id = "split-editor-regex"
    }

    val codeEditor: IdeCodeEditor = IdeCodeEditor(
        initialText = CodeSnippetPreset.BUILT_IN_PRESETS.first().code,
        language = CodeLanguage.KOTLIN,
        theme = CodeEditorTheme.DARK_IDE,
        width = 680,
        height = 520
    ).apply {
        id = "ide-code-editor"
    }

    val activeLanguageBadge: Badge = Badge(
        text = CodeLanguage.KOTLIN.displayName,
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "badge-active-language"
    }

    val lineCountBadge: Badge = Badge(
        text = "0 lines",
        type = BootstrapType.SECONDARY,
        pill = true
    ).apply {
        id = "badge-line-count"
    }

    // Regex Controls
    val regexPatternInput: TextField = TextField(
        value = CodeSnippetPreset.BUILT_IN_PRESETS.first().defaultRegex,
        placeholder = "Enter regular expression pattern..."
    ).apply {
        id = "input-regex-pattern"
    }

    val ignoreCaseCheckBox: CheckBox = CheckBox(
        label = "Ignore Case (i)",
        checked = false
    ).apply {
        id = "check-ignore-case"
    }

    val multilineCheckBox: CheckBox = CheckBox(
        label = "Multiline (m)",
        checked = true
    ).apply {
        id = "check-multiline"
    }

    val runRegexButton: Button = Button(
        text = "Run Regex",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-run-regex"
    }

    val matchCountBadge: Badge = Badge(
        text = "0 matches",
        type = BootstrapType.INFO,
        pill = true
    ).apply {
        id = "badge-match-count"
    }

    val regexStatusLabel: Label = Label("Ready").apply {
        id = "label-regex-status"
    }

    // Capture Groups Table & Match Cards List
    val captureGroupsTable: Table = Table().apply {
        id = "table-capture-groups"
    }

    val matchesListStack: VStack = VStack(gap = SpacingScale.S1).apply {
        id = "list-regex-matches"
    }

    // Bottom Status Bar
    val statusBar: StatusBar = StatusBar()
    val statusMessageLabel: Label = Label("Ready").apply {
        id = "label-status-bar"
    }

    init {
        CodeSnippetPreset.registerWithPreferenceStore()
        addClass("code-regex-root")
        initializeSnippetLibrary()
        buildWorkspaceLayout()
        wireEvents()
        loadPresetById(activeSnippetId)
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        evaluateRegex()
    }

    private fun initializeSnippetLibrary() {
        snippetRegistry.clear()
        for (preset in CodeSnippetPreset.BUILT_IN_PRESETS) {
            snippetRegistry[preset.id] = preset
        }
        val savedIds = preferenceStore.getStringSet(KEY_SAVED_SNIPPET_IDS, emptySet())
        for (savedId in savedIds) {
            val loaded = preferenceStore.get<CodeSnippetPreset>("$KEY_SNIPPET_PREFIX$savedId")
            if (loaded != null) {
                snippetRegistry[loaded.id] = loaded
            }
        }
        rebuildPresetSelectorOptions()
    }

    private fun rebuildPresetSelectorOptions() {
        snippetPresetSelect.setOptions(
            snippetRegistry.values.map { SelectOption(it.id, it.title) }
        )
    }

    private fun buildWorkspaceLayout() {
        val rootStack = VStack(gap = SpacingScale.S2)

        // 1. Top Toolbar
        val toolbarRow = HStack(gap = SpacingScale.S2)
        toolbarRow.addClass("regex-toolbar")
        toolbarRow.addChild(Label("Language:"))
        toolbarRow.addChild(languageSelect)
        toolbarRow.addChild(Label("Theme:"))
        toolbarRow.addChild(editorThemeSelect)
        toolbarRow.addChild(Label("Preset:"))
        toolbarRow.addChild(snippetPresetSelect)
        toolbarRow.addChild(loadSnippetButton)
        toolbarRow.addChild(snippetTitleInput)
        toolbarRow.addChild(saveSnippetButton)
        topToolbar.addChild(toolbarRow)
        rootStack.addChild(topToolbar)

        // 2. Buffer Tabs
        for (preset in snippetRegistry.values) {
            val tab = CloseableTab(
                tabId = preset.id,
                title = preset.title,
                isCloseable = preset.id !in CodeSnippetPreset.BUILT_IN_PRESETS.map { it.id }
            )
            bufferTabContainer.addTab(tab, HStack())
        }
        rootStack.addChild(bufferTabContainer)

        // 3. Left Pane: Syntax-Highlighted IdeCodeEditor
        val editorCard = Card("Syntax-Highlighted Code Buffer")
        editorCard.addClass("editor-pane-card")
        val editorStack = VStack(gap = SpacingScale.S2)
        val editorMetaRow = HStack(gap = SpacingScale.S2)
        editorMetaRow.addChild(Label("Active Syntax:"))
        editorMetaRow.addChild(activeLanguageBadge)
        editorMetaRow.addChild(lineCountBadge)
        editorStack.addChild(editorMetaRow)
        editorStack.addChild(codeEditor)
        editorCard.addChild(editorStack)

        // 4. Right Pane: Regular Expression Tester & Capture Group Table
        val rightScroll = ScrollView()
        val rightStack = VStack(gap = SpacingScale.S2)

        val regexControlsCard = Card("Regular Expression Playground")
        regexControlsCard.addClass("regex-inspector-card")
        val regexFormStack = VStack(gap = SpacingScale.S2)

        val patternRow = HStack(gap = SpacingScale.S2)
        patternRow.addChild(Label("Pattern:"))
        patternRow.addChild(regexPatternInput)
        patternRow.addChild(runRegexButton)
        regexFormStack.addChild(patternRow)

        val flagsRow = HStack(gap = SpacingScale.S2)
        flagsRow.addChild(ignoreCaseCheckBox)
        flagsRow.addChild(multilineCheckBox)
        flagsRow.addChild(matchCountBadge)
        regexFormStack.addChild(flagsRow)

        regexFormStack.addChild(regexStatusLabel)
        regexControlsCard.addChild(regexFormStack)
        rightStack.addChild(regexControlsCard)

        val captureCard = Card("Extracted Matches & Capture Groups")
        captureCard.addClass("capture-groups-card")
        val captureStack = VStack(gap = SpacingScale.S2)
        captureGroupsTable.setColumns(
            listOf(
                TableColumn(id = "matchIndex", headerText = "#"),
                TableColumn(id = "fullMatch", headerText = "Full Match"),
                TableColumn(id = "range", headerText = "Range"),
                TableColumn(id = "groups", headerText = "Capture Groups")
            )
        )
        captureStack.addChild(captureGroupsTable)
        captureStack.addChild(Heading("Match Detail Badges", level = 6))
        captureStack.addChild(matchesListStack)
        captureCard.addChild(captureStack)
        rightStack.addChild(captureCard)

        rightScroll.addChild(rightStack)
        workspaceSplitPane.setPanes(editorCard, rightScroll)
        rootStack.addChild(workspaceSplitPane)

        // 5. Status Bar
        statusBar.addClass("status-bar")
        statusBar.addChild(statusMessageLabel)
        rootStack.addChild(statusBar)

        addChild(rootStack)
    }

    private fun wireEvents() {
        languageSelect.onSelectionChanged = { selectedLangName ->
            val resolved = CodeLanguage.entries.firstOrNull {
                it.name.equals(selectedLangName, ignoreCase = true)
            } ?: CodeLanguage.KOTLIN
            switchSyntaxLanguage(resolved)
        }

        editorThemeSelect.onSelectionChanged = { selectedThemeName ->
            applyEditorTheme(selectedThemeName)
        }

        snippetPresetSelect.onSelectionChanged = { presetId ->
            loadPresetById(presetId)
        }

        loadSnippetButton.setOnClick {
            val targetId = snippetPresetSelect.selectedValue ?: activeSnippetId
            loadPresetById(targetId)
        }

        saveSnippetButton.setOnClick {
            saveCurrentBufferAsSnippet()
        }

        bufferTabContainer.onTabEntrySelected = { entry ->
            if (entry.id in snippetRegistry && entry.id != activeSnippetId) {
                loadPresetById(entry.id)
            }
        }

        codeEditor.setOnTextChanged {
            updateLineCountBadge()
            evaluateRegex()
        }

        regexPatternInput.onValueChanged = {
            evaluateRegex()
        }

        ignoreCaseCheckBox.onCheckedChanged = {
            evaluateRegex()
        }

        multilineCheckBox.onCheckedChanged = {
            evaluateRegex()
        }

        runRegexButton.setOnClick {
            evaluateRegex()
        }
    }

    /**
     * Switches the active syntax language in [codeEditor] and updates status badges.
     */
    fun switchSyntaxLanguage(language: CodeLanguage) {
        codeEditor.language = language
        languageSelect.selectedValue = language.name
        activeLanguageBadge.text = language.displayName
        statusMessageLabel.text = "Switched syntax highlighting to ${language.displayName}."
    }

    /**
     * Applies one of the built-in [CodeEditorTheme] presets (`DARK_IDE`, `DRACULA`, `MONOKAI`, `LIGHT_IDE`).
     */
    fun applyEditorTheme(themeName: String) {
        val resolvedTheme = when (themeName.uppercase()) {
            "DRACULA" -> CodeEditorTheme.DRACULA
            "MONOKAI" -> CodeEditorTheme.MONOKAI
            "LIGHT_IDE" -> CodeEditorTheme.LIGHT_IDE
            else -> CodeEditorTheme.DARK_IDE
        }
        codeEditor.theme = resolvedTheme
        editorThemeSelect.selectedValue = themeName.uppercase()
        statusMessageLabel.text = "Applied theme '${resolvedTheme.name}'."
    }

    /**
     * Replaces the code buffer text in [codeEditor] and immediately re-evaluates the active regex pattern.
     */
    fun setBufferText(newCode: String) {
        codeEditor.setText(newCode)
        updateLineCountBadge()
        evaluateRegex()
    }

    /**
     * Loads a built-in or user-saved [CodeSnippetPreset] by [presetId].
     */
    fun loadPresetById(presetId: String): CodeSnippetPreset? {
        val preset = snippetRegistry[presetId]
            ?: preferenceStore.get<CodeSnippetPreset>("$KEY_SNIPPET_PREFIX$presetId")
            ?: return null
        snippetRegistry[preset.id] = preset
        activeSnippetId = preset.id
        snippetPresetSelect.selectedValue = preset.id
        snippetTitleInput.value = preset.title
        bufferTabContainer.setActiveTab(preset.id)

        val resolvedLanguage = CodeLanguage.entries.firstOrNull {
            it.name.equals(preset.language, ignoreCase = true)
        } ?: CodeLanguage.KOTLIN

        codeEditor.language = resolvedLanguage
        languageSelect.selectedValue = resolvedLanguage.name
        activeLanguageBadge.text = resolvedLanguage.displayName

        codeEditor.setText(preset.code)
        regexPatternInput.value = preset.defaultRegex
        updateLineCountBadge()
        evaluateRegex()
        statusMessageLabel.text = "Loaded snippet '${preset.title}' (${resolvedLanguage.displayName})."
        return preset
    }

    /**
     * Evaluates the regular expression in [regexPatternInput] against [codeEditor]'s buffer,
     * updates [matchCountBadge], populates [captureGroupsTable], and highlights matches via [IdeCodeEditor.setDiagnostics].
     */
    fun evaluateRegex(): List<RegexMatchRecord> {
        val patternText = regexPatternInput.value
        val sourceText = codeEditor.getText()
        updateLineCountBadge()

        if (patternText.isEmpty()) {
            lastRegexMatches = emptyList()
            lastRegexError = null
            matchCountBadge.text = "0 matches"
            matchCountBadge.type = BootstrapType.SECONDARY
            regexStatusLabel.text = "Enter a regular expression pattern to evaluate."
            captureGroupsTable.setRows(emptyList())
            matchesListStack.clearChildren()
            codeEditor.setDiagnostics(emptyList())
            return emptyList()
        }

        val options = mutableSetOf<RegexOption>()
        if (ignoreCaseCheckBox.isChecked) {
            options.add(RegexOption.IGNORE_CASE)
        }
        if (multilineCheckBox.isChecked) {
            options.add(RegexOption.MULTILINE)
        }

        val compiledRegex = try {
            Regex(patternText, options)
        } catch (ex: Throwable) {
            lastRegexMatches = emptyList()
            lastRegexError = ex.message ?: "Invalid regular expression"
            matchCountBadge.text = "0 matches"
            matchCountBadge.type = BootstrapType.DANGER
            regexStatusLabel.text = "Regex syntax error: $lastRegexError"
            captureGroupsTable.setRows(emptyList())
            matchesListStack.clearChildren()
            codeEditor.setDiagnostics(emptyList())
            statusMessageLabel.text = "Regex error: $lastRegexError"
            return emptyList()
        }

        lastRegexError = null
        val records = mutableListOf<RegexMatchRecord>()
        val diagnostics = mutableListOf<CodeDiagnostic>()

        val allMatches = compiledRegex.findAll(sourceText).toList()
        for ((index, match) in allMatches.withIndex()) {
            val startOffset = match.range.first
            val endOffsetExclusive = (match.range.last + 1).coerceAtLeast(startOffset)
            val startPos = charOffsetToCursorPosition(sourceText, startOffset)
            val endPos = charOffsetToCursorPosition(sourceText, endOffsetExclusive)

            val capturedGroups = if (match.groupValues.size > 1) {
                match.groupValues.drop(1)
            } else {
                emptyList()
            }

            val rangeLabel = "L${startPos.line + 1}:C${startPos.column + 1}..L${endPos.line + 1}:C${endPos.column + 1} ($startOffset..$endOffsetExclusive)"
            records.add(
                RegexMatchRecord(
                    matchIndex = index,
                    fullMatch = match.value,
                    rangeText = rangeLabel,
                    groups = capturedGroups
                )
            )

            diagnostics.add(
                CodeDiagnostic(
                    range = TextRange(startPos, endPos),
                    message = "Match #${index + 1}: ${match.value}",
                    severity = CodeDiagnosticSeverity.INFO,
                    code = "REGEX_${index + 1}"
                )
            )
        }

        lastRegexMatches = records
        codeEditor.setDiagnostics(diagnostics)

        matchCountBadge.text = "${records.size} matches"
        matchCountBadge.type = if (records.isNotEmpty()) BootstrapType.SUCCESS else BootstrapType.WARNING
        regexStatusLabel.text = "Evaluated /${patternText}/ (${records.size} matches found)"

        val tableRows = records.map { rec ->
            val groupsFormatted = if (rec.groups.isEmpty()) {
                "(none)"
            } else {
                rec.groups.mapIndexed { idx, g -> "$${idx + 1}=\"$g\"" }.joinToString(", ")
            }
            TableRow(
                listOf(
                    TableCell("#${rec.matchIndex + 1}"),
                    TableCell(rec.fullMatch),
                    TableCell(rec.rangeText),
                    TableCell(groupsFormatted)
                )
            )
        }
        captureGroupsTable.setRows(tableRows)

        matchesListStack.clearChildren()
        for (rec in records) {
            val row = HStack(gap = SpacingScale.S1)
            row.addChild(Badge("#${rec.matchIndex + 1}", type = BootstrapType.PRIMARY, pill = true))
            row.addChild(Label(rec.fullMatch))
            row.addChild(Badge(rec.rangeText, type = BootstrapType.SECONDARY))
            for ((gIdx, groupVal) in rec.groups.withIndex()) {
                row.addChild(Badge("$${gIdx + 1}: $groupVal", type = BootstrapType.INFO, pill = true))
            }
            matchesListStack.addChild(row)
        }

        statusMessageLabel.text = "Matched ${records.size} occurrence(s) for pattern '$patternText'."
        return records
    }

    /**
     * Saves the current buffer, language, and regex pattern into [preferenceStore] as a [CodeSnippetPreset].
     */
    fun saveCurrentBufferAsSnippet(customId: String? = null): CodeSnippetPreset {
        val rawTitle = snippetTitleInput.value.trim().ifEmpty { "Saved Snippet ${snippetRegistry.size + 1}" }
        val resolvedId = customId ?: rawTitle
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "snippet-${snippetRegistry.size + 1}" }

        val preset = CodeSnippetPreset(
            id = resolvedId,
            title = rawTitle,
            language = codeEditor.language.name,
            code = codeEditor.getText(),
            defaultRegex = regexPatternInput.value
        )

        snippetRegistry[preset.id] = preset
        activeSnippetId = preset.id

        val savedIds = preferenceStore.getStringSet(KEY_SAVED_SNIPPET_IDS, emptySet()).toMutableSet()
        savedIds.add(preset.id)
        preferenceStore.putStringSet(KEY_SAVED_SNIPPET_IDS, savedIds)
        preferenceStore.put("$KEY_SNIPPET_PREFIX${preset.id}", preset)
        preferenceStore.putString(KEY_LAST_ACTIVE_SNIPPET_ID, preset.id)
        preferenceStore.flush()

        rebuildPresetSelectorOptions()
        snippetPresetSelect.selectedValue = preset.id
        if (bufferTabContainer.getTab(preset.id) == null) {
            val tab = CloseableTab(tabId = preset.id, title = preset.title, isCloseable = true)
            bufferTabContainer.addTab(tab, HStack())
        }
        bufferTabContainer.setActiveTab(preset.id)
        statusMessageLabel.text = "Saved snippet '${preset.title}' (${preset.id}) to PreferenceStore."
        return preset
    }

    /**
     * Loads a previously persisted [CodeSnippetPreset] directly from [preferenceStore].
     */
    fun loadSavedSnippetFromStore(snippetId: String): CodeSnippetPreset? =
        preferenceStore.get<CodeSnippetPreset>("$KEY_SNIPPET_PREFIX$snippetId")

    private fun updateLineCountBadge() {
        lineCountBadge.text = "${codeEditor.document.lineCount} lines"
    }

    private fun charOffsetToCursorPosition(text: String, offset: Int): CursorPosition {
        val clamped = offset.coerceIn(0, text.length)
        var line = 0
        var col = 0
        for (i in 0 until clamped) {
            if (text[i] == '\n') {
                line++
                col = 0
            } else {
                col++
            }
        }
        return CursorPosition(line, col)
    }

    companion object {
        const val KEY_SAVED_SNIPPET_IDS: String = "saved_snippet_ids"
        const val KEY_SNIPPET_PREFIX: String = "snippet_"
        const val KEY_LAST_ACTIVE_SNIPPET_ID: String = "last_active_snippet_id"
    }
}
