package com.nexustools.genericui.examples.pad

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.action.Toolbar
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.fragment.helper.TabItemSpec
import com.nexustools.genericui.component.fragment.helper.TabbedFragment
import com.nexustools.genericui.component.fragment.helper.TabbedFragmentConfig
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.system.StatusBar
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.AlignItems
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexDirection
import com.nexustools.genericui.enums.bootstrap.JustifyContent
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.io.Readable
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.Writable
import com.nexustools.genericui.io.WritableByteBuffer
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Primary Windows 11 Dark-Mode Tabbed Notepad [TabbedFragment] for `GenericUIPad`.
 *
 * Pre-populates the 6 session tabs matching the Windows 11 Notepad screenshot (`is`, `pink`,
 * `=== E`, `include`, `I've cre`, and `CharG`), renders the second-row command and rich formatting
 * toolbar (`File`, `Edit`, `View`, `H1 ▾`, `List ▾`, `B`, `I`, `S`, `Link`, `Table`, `Clear Formatting`,
 * Find/Replace toggle, Notification bell, Settings gear), a collapsible Find & Replace bar, and
 * the 6-column bottom status bar (`Ln 1, Col 343` | `342 characters` | `Plain text` | `100%` | `Windows (CRLF)` | `UTF-8`).
 */
@MainAppFragment
class GenericUIPadFragment(
    private val explicitPreferenceStore: PreferenceStore? = null
) : TabbedFragment(
    config = TabbedFragmentConfig(
        showNewTabButton = true,
        allowCloseLastTab = false,
        persistLastActiveTabKey = null
    )
) {

    private val documentsById = linkedMapOf<String, PadDocument>()
    private val tabFragmentsById = linkedMapOf<String, PadDocumentTabFragment>()
    private var untitledCounter: Int = 1

    /** Current persisted session settings for the Notepad workspace. */
    var sessionSettings: PadSessionSettings = PadSessionSettings()
        private set

    override fun getPreferenceStore(): PreferenceStore {
        return explicitPreferenceStore ?: super.getPreferenceStore()
    }

    /** Snapshot of all open [PadDocument]s in tab strip order. */
    val openDocuments: List<PadDocument>
        get() = tabs.mapNotNull { documentsById[it.tabId] }

    /** Currently active [PadDocument], or `null` if no tab is active. */
    val activeDocument: PadDocument?
        get() = activeTabId?.let { documentsById[it] }

    /** Currently active [PadDocumentTabFragment], or `null` if no tab is active. */
    val activeDocumentTabFragment: PadDocumentTabFragment?
        get() = activeTabId?.let { tabFragmentsById[it] } ?: (activeFragment as? PadDocumentTabFragment)

    // --- Second-Row Command & Rich Formatting Toolbar Controls ---

    val commandBar: Toolbar = Toolbar().apply {
        addClass("pad-command-bar")
    }

    // Left menu action buttons
    val fileMenuButton: Button = Button("File", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        addClass("pad-menu-btn")
    }
    val editMenuButton: Button = Button("Edit", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        addClass("pad-menu-btn")
    }
    val viewMenuButton: Button = Button("View", type = BootstrapType.DARK, size = BootstrapSize.SM).apply {
        addClass("pad-menu-btn")
    }

    // Center rich formatting controls
    val headingSelect: Select = Select(
        options = listOf(
            SelectOption("BODY", "Body"),
            SelectOption("H1", "H1"),
            SelectOption("H2", "H2"),
            SelectOption("H3", "H3")
        ),
        selectedValue = "H1"
    ).apply {
        addClass("pad-heading-select")
    }

    val listSelect: Select = Select(
        options = listOf(
            SelectOption("BULLET", "List • Bullet"),
            SelectOption("NUMBERED", "List 1. Numbered")
        ),
        selectedValue = "BULLET"
    ).apply {
        addClass("pad-list-select")
    }

    val bulletListButton: Button = Button("• List", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val numberedListButton: Button = Button("1. List", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val boldButton: Button = Button("B", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val italicButton: Button = Button("I", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val strikethroughButton: Button = Button("S", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val linkButton: Button = Button("🔗", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val tableButton: Button = Button("⊞", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val clearFormattingButton: Button = Button("Aₓ", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val modeToggleButton: Button = Button("Markdown Preview", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true)

    // Right action controls
    val findToggleButton: Button = Button("🔍", type = BootstrapType.DARK, size = BootstrapSize.SM)
    val whatsNewBellButton: Button = Button("🔔", type = BootstrapType.DARK, size = BootstrapSize.SM)
    val settingsGearButton: Button = Button("⚙", type = BootstrapType.DARK, size = BootstrapSize.SM)
    val notificationBadge: Badge = Badge("Win11 Pad", type = BootstrapType.INFO, pill = true).apply {
        isVisible = false
    }

    // --- Collapsible Find & Replace Bar ---

    private var _findQuery: String = ""
    var findQuery: String
        get() = _findQuery
        set(value) {
            _findQuery = value
            if (findQueryField.value != value) {
                findQueryField.value = value
            }
        }

    private var _replaceText: String = ""
    var replaceText: String
        get() = _replaceText
        set(value) {
            _replaceText = value
            if (replaceTextField.value != value) {
                replaceTextField.value = value
            }
        }

    val findReplaceBar: HStack = HStack(gap = SpacingScale.S2).apply {
        addClass("pad-find-replace-bar")
        isVisible = false
    }
    val findQueryField: TextField = TextField(value = "", placeholder = "Find in document...")
    val replaceTextField: TextField = TextField(value = "", placeholder = "Replace with...")
    val findNextButton: Button = Button("Find Next", type = BootstrapType.PRIMARY, size = BootstrapSize.SM)
    val replaceAllButton: Button = Button("Replace All", type = BootstrapType.SUCCESS, size = BootstrapSize.SM)
    val findStatusLabel: Label = Label("0 matches")

    // --- Collapsible Settings Panel ---

    val settingsPanel: HStack = HStack(gap = SpacingScale.S3).apply {
        addClass("pad-settings-panel")
        isVisible = false
    }
    val wordWrapCheckBox: CheckBox = CheckBox(label = "Word wrap", checked = true, switchStyle = true)
    val spellCheckCheckBox: CheckBox = CheckBox(label = "Spell check", checked = true, switchStyle = true)
    val statusBarCheckBox: CheckBox = CheckBox(label = "Status bar", checked = true, switchStyle = true)

    // --- 6-Column Bottom Status Bar ---

    val statusBar: StatusBar = StatusBar().apply {
        addClass("pad-status-bar")
    }
    val cursorStatusLabel: Label = Label("Ln 1, Col 343").apply { addClass("pad-status-cell") }
    val charCountStatusLabel: Label = Label("342 characters").apply { addClass("pad-status-cell") }
    val modeStatusLabel: Label = Label("Plain text").apply { addClass("pad-status-cell") }
    val zoomStatusLabel: Label = Label("100%").apply { addClass("pad-status-cell") }
    val lineEndingStatusLabel: Label = Label("Windows (CRLF)").apply { addClass("pad-status-cell") }
    val encodingStatusLabel: Label = Label("UTF-8").apply { addClass("pad-status-cell") }

    init {
        addClass("genericui-pad-root")
        pageTitle = "CharG - Notepad"
        buildChromeAndToolbars()
        wireCommandAndFindEvents()
        populateInitialSixWindows11Tabs()
        syncStatusBarFromActiveDocument()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        if (tabCount == 0) {
            populateInitialSixWindows11Tabs()
        }
        syncStatusBarFromActiveDocument()
    }

    private fun buildChromeAndToolbars() {
        val commandRow = FlexBox().apply {
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
            addClass("w-100", "gap-2")
        }

        val leftMenusGroup = HStack(gap = SpacingScale.S1).apply {
            addClass("pad-menu-triggers")
            addChild(fileMenuButton)
            addChild(editMenuButton)
            addChild(viewMenuButton)
        }

        val centerFormattingGroup = HStack(gap = SpacingScale.S1).apply {
            addClass("pad-format-toolbar")
            addChild(headingSelect)
            addChild(listSelect)
            addChild(bulletListButton)
            addChild(numberedListButton)
            addChild(boldButton)
            addChild(italicButton)
            addChild(strikethroughButton)
            addChild(linkButton)
            addChild(tableButton)
            addChild(clearFormattingButton)
            addChild(modeToggleButton)
        }

        val rightActionsGroup = HStack(gap = SpacingScale.S1).apply {
            addClass("pad-right-actions")
            addChild(findToggleButton)
            addChild(whatsNewBellButton)
            addChild(notificationBadge)
            addChild(settingsGearButton)
        }

        commandRow.addChild(leftMenusGroup)
        commandRow.addChild(centerFormattingGroup)
        commandRow.addChild(rightActionsGroup)
        commandBar.addChild(commandRow)

        findReplaceBar.addChild(Label("Find:"))
        findReplaceBar.addChild(findQueryField)
        findReplaceBar.addChild(Label("Replace:"))
        findReplaceBar.addChild(replaceTextField)
        findReplaceBar.addChild(findNextButton)
        findReplaceBar.addChild(replaceAllButton)
        findReplaceBar.addChild(findStatusLabel)

        settingsPanel.addChild(Label("Notepad Settings:"))
        settingsPanel.addChild(wordWrapCheckBox)
        settingsPanel.addChild(spellCheckCheckBox)
        settingsPanel.addChild(statusBarCheckBox)

        // Mount command bar, find/replace bar, and settings panel directly inside headerSlotContainer below the tab strip
        headerSlotContainer.addChild(commandBar)
        headerSlotContainer.addChild(findReplaceBar)
        headerSlotContainer.addChild(settingsPanel)

        // Build 6-column bottom status bar
        val statusRow = FlexBox().apply {
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
            addClass("w-100", "gap-3")
        }
        statusRow.addChild(cursorStatusLabel)
        statusRow.addChild(charCountStatusLabel)
        statusRow.addChild(modeStatusLabel)
        statusRow.addChild(zoomStatusLabel)
        statusRow.addChild(lineEndingStatusLabel)
        statusRow.addChild(encodingStatusLabel)
        statusBar.addChild(statusRow)
        addChild(statusBar)
    }

    private fun wireCommandAndFindEvents() {
        onNewTabRequested = {
            createAndRegisterTabSpec(
                PadDocument(
                    docId = "tab-untitled-${untitledCounter++}",
                    title = "Untitled",
                    content = "",
                    isModified = false,
                    cursorLine = 1,
                    cursorCol = 1,
                    zoomPercent = sessionSettings.defaultZoomPercent,
                    lineEnding = PadLineEnding.WINDOWS_CRLF,
                    encoding = sessionSettings.defaultEncoding,
                    mode = PadDocumentMode.PLAIN_TEXT
                )
            )
        }

        onActiveTabChanged = { tabId, _, _ ->
            if (tabId != null) {
                sessionSettings = sessionSettings.copy(
                    activeTabId = tabId,
                    openTabCount = tabCount
                )
            }
            syncStatusBarFromActiveDocument()
        }

        onTabOpened = { _ ->
            sessionSettings = sessionSettings.copy(openTabCount = tabCount)
        }

        onTabClosed = { closedSpec ->
            documentsById.remove(closedSpec.tabId)
            tabFragmentsById.remove(closedSpec.tabId)
            sessionSettings = sessionSettings.copy(
                activeTabId = activeTabId ?: "",
                openTabCount = tabCount
            )
            syncStatusBarFromActiveDocument()
        }

        fileMenuButton.setOnClick {
            requestNewTab()
        }
        editMenuButton.setOnClick {
            toggleFindReplaceBar()
        }
        viewMenuButton.setOnClick {
            toggleActiveDocumentMode()
        }

        headingSelect.onSelectionChanged = { selected ->
            when (selected) {
                "H1" -> applyFormatToActiveTab(PadFormatAction.HEADING_1)
                "H2" -> applyFormatToActiveTab(PadFormatAction.HEADING_2)
                "H3" -> applyFormatToActiveTab(PadFormatAction.HEADING_3)
                "BODY" -> applyFormatToActiveTab(PadFormatAction.CLEAR_FORMATTING)
            }
        }

        listSelect.onSelectionChanged = { selected ->
            when (selected) {
                "BULLET" -> applyFormatToActiveTab(PadFormatAction.BULLET_LIST)
                "NUMBERED" -> applyFormatToActiveTab(PadFormatAction.NUMBERED_LIST)
            }
        }

        bulletListButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.BULLET_LIST)
        }
        numberedListButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.NUMBERED_LIST)
        }
        boldButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.BOLD)
        }
        italicButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.ITALIC)
        }
        strikethroughButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.STRIKETHROUGH)
        }
        linkButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.LINK)
        }
        tableButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.TABLE)
        }
        clearFormattingButton.setOnClick {
            applyFormatToActiveTab(PadFormatAction.CLEAR_FORMATTING)
        }
        modeToggleButton.setOnClick {
            toggleActiveDocumentMode()
        }

        findToggleButton.setOnClick {
            toggleFindReplaceBar()
        }
        whatsNewBellButton.setOnClick {
            notificationBadge.isVisible = !notificationBadge.isVisible
        }
        settingsGearButton.setOnClick {
            toggleSettingsPanel()
        }

        findQueryField.onValueChanged = { updated ->
            _findQuery = updated
        }
        replaceTextField.onValueChanged = { updated ->
            _replaceText = updated
        }
        findNextButton.setOnClick {
            findNext()
        }
        replaceAllButton.setOnClick {
            replaceAll()
        }

        wordWrapCheckBox.onCheckedChanged = { checked ->
            updateSessionSettings(sessionSettings.copy(wordWrap = checked))
        }
        spellCheckCheckBox.onCheckedChanged = { checked ->
            updateSessionSettings(sessionSettings.copy(spellCheckEnabled = checked))
            for (frag in tabFragmentsById.values) {
                frag.setSpellCheck(checked)
            }
        }
        statusBarCheckBox.onCheckedChanged = { checked ->
            updateSessionSettings(sessionSettings.copy(showStatusBar = checked))
            statusBar.isVisible = checked
        }
    }

    private fun populateInitialSixWindows11Tabs() {
        val initialDocs = listOf(
            PadDocument(
                docId = "tab-is",
                title = "is",
                content = "is a lightweight cross-platform component note in Windows 11 Dark Mode.",
                isModified = true
            ),
            PadDocument(
                docId = "tab-pink",
                title = "pink",
                content = "pink accent theme tokens: #F472B6, #EC4899, #DB2777",
                isModified = true
            ),
            PadDocument(
                docId = "tab-eq",
                title = "=== E",
                content = "=== Execution Plan & Multi-Backend Verification ===",
                isModified = true
            ),
            PadDocument(
                docId = "tab-include",
                title = "include",
                content = "includeBuild(\"../../..\")\nrootProject.name = \"genericui-pad\"",
                isModified = true
            ),
            PadDocument(
                docId = "tab-ive-cre",
                title = "I've cre",
                content = "I've created the multi-tab dark mode Notepad workspace with rich Markdown formatting.",
                isModified = true
            ),
            PadDocument(
                docId = CHARGEN_TAB_ID,
                title = "CharG",
                content = CHARGEN_TAB_CONTENT,
                isModified = true,
                cursorLine = 1,
                cursorCol = 343,
                zoomPercent = 100,
                lineEnding = PadLineEnding.WINDOWS_CRLF,
                encoding = "UTF-8",
                mode = PadDocumentMode.PLAIN_TEXT,
                spellCheckFindings = listOf("CharGenView", "DCGame")
            )
        )

        for (doc in initialDocs) {
            val spec = createAndRegisterTabSpec(doc)
            val isCharGen = (doc.docId == CHARGEN_TAB_ID)
            openTab(spec, selectImmediately = isCharGen)
        }
        sessionSettings = sessionSettings.copy(
            activeTabId = activeTabId ?: CHARGEN_TAB_ID,
            openTabCount = tabCount
        )
    }

    private fun createAndRegisterTabSpec(initialDoc: PadDocument): TabItemSpec {
        val tabFrag = PadDocumentTabFragment(
            initialDocument = initialDoc,
            initialSpellCheckEnabled = sessionSettings.spellCheckEnabled
        )
        documentsById[initialDoc.docId] = tabFrag.document
        tabFragmentsById[initialDoc.docId] = tabFrag

        tabFrag.onDocumentChanged = { updatedDoc ->
            documentsById[updatedDoc.docId] = updatedDoc
            updateTabBadge(updatedDoc.docId, if (updatedDoc.isModified) "•" else null)
            if (activeTabId == updatedDoc.docId) {
                syncStatusBarFromDocument(updatedDoc)
            }
        }

        return TabItemSpec(
            tabId = initialDoc.docId,
            title = initialDoc.title,
            subtitle = "${tabFrag.document.characterCount} chars",
            badgeText = if (initialDoc.isModified) "•" else null,
            closable = true,
            pinned = false,
            fragmentFactory = { tabFrag }
        )
    }

    /**
     * Applies [action] to the currently active document tab and returns the updated document text.
     */
    fun applyFormatToActiveTab(action: PadFormatAction): String {
        val frag = activeDocumentTabFragment ?: return ""
        val updated = frag.applyFormatAction(action)
        syncStatusBarFromActiveDocument()
        return updated
    }

    /**
     * Toggles the Find & Replace bar visibility and returns the new visibility state.
     */
    fun toggleFindReplaceBar(): Boolean {
        findReplaceBar.isVisible = !findReplaceBar.isVisible
        return findReplaceBar.isVisible
    }

    /**
     * Toggles the Settings panel visibility and returns the new visibility state.
     */
    fun toggleSettingsPanel(): Boolean {
        settingsPanel.isVisible = !settingsPanel.isVisible
        return settingsPanel.isVisible
    }

    /**
     * Searches for [query] in the active tab, updating the cursor position and status label.
     *
     * @return 0-based index of the match, or `-1` if not found.
     */
    fun findNext(query: String = findQuery): Int {
        if (findQuery != query) {
            findQuery = query
        }
        val frag = activeDocumentTabFragment ?: return -1
        val matchIdx = frag.findNext(query)
        findStatusLabel.text = if (matchIdx >= 0) "Match at index $matchIdx" else "No matches"
        syncStatusBarFromActiveDocument()
        return matchIdx
    }

    /**
     * Replaces all occurrences of [query] with [replacement] in the active document tab.
     *
     * @return Number of replaced occurrences.
     */
    fun replaceAll(query: String = findQuery, replacement: String = replaceText): Int {
        if (findQuery != query) {
            findQuery = query
        }
        if (replaceText != replacement) {
            replaceText = replacement
        }
        val frag = activeDocumentTabFragment ?: return 0
        val count = frag.replaceAll(query, replacement)
        findStatusLabel.text = "Replaced $count occurrence(s)"
        syncStatusBarFromActiveDocument()
        return count
    }

    /**
     * Increases the zoom percentage of the active tab by [step] (default `10%`).
     */
    fun zoomIn(step: Int = 10): Int {
        val frag = activeDocumentTabFragment ?: return 100
        frag.setZoomPercent(frag.document.zoomPercent + step)
        syncStatusBarFromActiveDocument()
        return frag.document.zoomPercent
    }

    /**
     * Decreases the zoom percentage of the active tab by [step] (default `10%`).
     */
    fun zoomOut(step: Int = 10): Int {
        val frag = activeDocumentTabFragment ?: return 100
        frag.setZoomPercent(frag.document.zoomPercent - step)
        syncStatusBarFromActiveDocument()
        return frag.document.zoomPercent
    }

    /**
     * Resets the zoom percentage of the active tab to `100%`.
     */
    fun resetZoom(): Int {
        val frag = activeDocumentTabFragment ?: return 100
        frag.setZoomPercent(100)
        syncStatusBarFromActiveDocument()
        return frag.document.zoomPercent
    }

    /**
     * Toggles the active tab between [PadDocumentMode.PLAIN_TEXT] and [PadDocumentMode.MARKDOWN].
     */
    fun toggleActiveDocumentMode(): PadDocumentMode {
        val frag = activeDocumentTabFragment ?: return PadDocumentMode.PLAIN_TEXT
        val nextMode = if (frag.document.mode == PadDocumentMode.PLAIN_TEXT) {
            PadDocumentMode.MARKDOWN
        } else {
            PadDocumentMode.PLAIN_TEXT
        }
        frag.setDocumentMode(nextMode)
        syncStatusBarFromActiveDocument()
        return nextMode
    }

    /**
     * Updates the active tab's line-ending convention (`WINDOWS_CRLF`, `UNIX_LF`, or `CLASSIC_MAC_CR`).
     */
    fun setActiveDocumentLineEnding(lineEnding: PadLineEnding) {
        val frag = activeDocumentTabFragment ?: return
        frag.setLineEnding(lineEnding)
        syncStatusBarFromActiveDocument()
    }

    /**
     * Exports the content of [docId] into [writable] (e.g. a [WritableByteBuffer]) using its configured line endings.
     *
     * @return Number of UTF-8 bytes written.
     */
    fun exportDocumentToWritable(
        docId: String = activeTabId ?: CHARGEN_TAB_ID,
        writable: Writable
    ): Int {
        val doc = documentsById[docId] ?: return 0
        val normalized = doc.content.replace("\r\n", "\n").replace('\r', '\n')
        val formattedText = if (doc.lineEnding == PadLineEnding.UNIX_LF) {
            normalized
        } else {
            normalized.replace("\n", doc.lineEnding.sequence)
        }
        val bytes = formattedText.encodeToByteArray()
        writable.writeBytes(bytes, 0, bytes.size)
        runStreamSynchronously {
            writable.flush()
            writable.end()
        }
        return bytes.size
    }

    /**
     * Convenience helper that exports [docId] directly into a new [WritableByteBuffer].
     */
    fun exportDocumentToByteBuffer(docId: String = activeTabId ?: CHARGEN_TAB_ID): WritableByteBuffer {
        val buffer = WritableByteBuffer()
        exportDocumentToWritable(docId, buffer)
        return buffer
    }

    /**
     * Imports a document from any [Readable] stream (including [ReadableByteBuffer]), opens it in a new tab,
     * and selects it immediately.
     */
    fun importDocumentFromReadable(
        title: String,
        readable: Readable
    ): PadDocument {
        val rawText = runStreamSynchronously {
            readable.readAllText("utf8")
        }
        val detectedEnding = when {
            rawText.contains("\r\n") -> PadLineEnding.WINDOWS_CRLF
            rawText.contains('\r') -> PadLineEnding.CLASSIC_MAC_CR
            else -> PadLineEnding.UNIX_LF
        }
        val normalizedContent = rawText.replace("\r\n", "\n").replace('\r', '\n')
        val (endLine, endCol) = PadDocument.computeEndCursorPosition(normalizedContent)
        val docId = "tab-import-${untitledCounter++}"
        val importedDoc = PadDocument(
            docId = docId,
            title = title,
            content = normalizedContent,
            isModified = false,
            cursorLine = endLine,
            cursorCol = endCol,
            zoomPercent = sessionSettings.defaultZoomPercent,
            lineEnding = detectedEnding,
            encoding = sessionSettings.defaultEncoding,
            mode = if (title.endsWith(".md", ignoreCase = true)) PadDocumentMode.MARKDOWN else PadDocumentMode.PLAIN_TEXT
        )
        val spec = createAndRegisterTabSpec(importedDoc)
        openAndSelectTab(spec)
        return documentsById[docId] ?: importedDoc
    }

    /**
     * Updates and persists [newSettings] to [getPreferenceStore] under `"pad.settings"`.
     */
    fun updateSessionSettings(newSettings: PadSessionSettings): PadSessionSettings {
        applySessionSettings(newSettings)
        saveSessionSettingsToPreferences()
        return sessionSettings
    }

    /**
     * Persists [sessionSettings] to [getPreferenceStore] under `"pad.settings"`.
     */
    fun saveSessionSettingsToPreferences(): PadSessionSettings {
        val current = sessionSettings.copy(
            activeTabId = activeTabId ?: sessionSettings.activeTabId,
            openTabCount = tabCount
        )
        sessionSettings = current
        PadSessionSettings.saveToStore(getPreferenceStore(), current)
        return current
    }

    /**
     * Restores [PadSessionSettings] from [getPreferenceStore] (`"pad.settings"`) and applies them.
     */
    fun loadSessionSettingsFromPreferences(): PadSessionSettings {
        val loaded = PadSessionSettings.loadFromStore(getPreferenceStore(), sessionSettings)
        applySessionSettings(loaded)
        return loaded
    }

    /**
     * Exports [sessionSettings] into [writable] using [com.nexustools.genericui.util.BinaryJson].
     */
    fun exportSessionSettingsToWritable(writable: Writable) {
        PadSessionSettings.exportToWritable(sessionSettings, writable)
    }

    /**
     * Imports [PadSessionSettings] from [readable] using [com.nexustools.genericui.util.BinaryJson] and applies them.
     */
    fun importSessionSettingsFromReadable(readable: Readable): PadSessionSettings {
        val loaded = PadSessionSettings.importFromReadable(readable, sessionSettings)
        applySessionSettings(loaded)
        return loaded
    }

    private fun applySessionSettings(loaded: PadSessionSettings) {
        sessionSettings = loaded
        wordWrapCheckBox.isChecked = loaded.wordWrap
        spellCheckCheckBox.isChecked = loaded.spellCheckEnabled
        statusBarCheckBox.isChecked = loaded.showStatusBar
        statusBar.isVisible = loaded.showStatusBar
        for (frag in tabFragmentsById.values) {
            frag.setSpellCheck(loaded.spellCheckEnabled)
        }
        if (loaded.activeTabId.isNotBlank() && tabs.any { it.tabId == loaded.activeTabId }) {
            selectTab(loaded.activeTabId)
        }
        syncStatusBarFromActiveDocument()
    }

    private fun syncStatusBarFromActiveDocument() {
        val doc = activeDocument ?: return
        syncStatusBarFromDocument(doc)
    }

    private fun syncStatusBarFromDocument(doc: PadDocument) {
        cursorStatusLabel.text = doc.formattedCursorStatus
        charCountStatusLabel.text = doc.formattedCharacterCountStatus
        modeStatusLabel.text = doc.mode.label
        zoomStatusLabel.text = doc.formattedZoomStatus
        lineEndingStatusLabel.text = doc.lineEnding.label
        encodingStatusLabel.text = doc.encoding
        pageTitle = "${doc.title} - Notepad"
    }

    private fun <T> runStreamSynchronously(block: suspend () -> T): T {
        var outcomeResult: Result<T>? = null
        block.startCoroutine(Continuation(EmptyCoroutineContext) { result ->
            outcomeResult = result
        })
        return outcomeResult?.getOrThrow()
            ?: throw IllegalStateException("Stream operation suspended unexpectedly")
    }

    companion object {
        const val CHARGEN_TAB_ID: String = "tab-chargen"

        /**
         * Exact 342-character content from the Windows 11 Notepad screenshot (`Ln 1, Col 343`, `342 characters`).
         */
        const val CHARGEN_TAB_CONTENT: String =
            "CharGenView of DCGame implements a PDF/Page rendering system for generating PDF character sheets or printing character sheets, look at the original source for the file at the git head and create a class specifically designed for using components to generate pages of any standard page size, defaulting to A4, which can be used for CharGenView"
    }
}
