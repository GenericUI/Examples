package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.dialog.Dialogs
import com.nexustools.genericui.component.dialog.FileTypeFilter
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.SearchField
import com.nexustools.genericui.component.form.TextArea
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.ScrollView
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.shell.SplitPane
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.surface.ListGroup
import com.nexustools.genericui.component.surface.ListGroupItem
import com.nexustools.genericui.component.system.PreferenceChangeListener
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.component.typography.MarkdownView
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.Orientation
import com.nexustools.genericui.style.color.RgbaColor

/**
 * Primary UI [Fragment] for the Markdown Notes & Preferences example (`1.3`).
 *
 * Demonstrates:
 * - Two-pane layout using [SplitPane] (`note-list` sidebar on the left, editor + live [MarkdownView] on the right).
 * - Structured object persistence in [PreferenceStore] (`put` / `get` with [NoteItem] via [getPreferenceStore]).
 * - Live [PreferenceChangeListener] notifications updating a status [Badge] (`badge-last-change`).
 * - Live search filtering via [SearchField] (`input-search`).
 * - Native file import/export via [Dialogs.openFile] and [Dialogs.saveFile].
 */
@MainAppFragment
class NotesFragment(
    private val customPreferenceStore: PreferenceStore? = null
) : Fragment() {

    override fun getPreferenceStore(): PreferenceStore =
        customPreferenceStore ?: super.getPreferenceStore()

    private val notes: MutableList<NoteItem> = mutableListOf()
    var activeNoteId: String? = null
        private set
    var searchQuery: String = ""
        private set
    var lastDeletedNote: NoteItem? = null
        private set
    var preferenceChangeCount: Int = 0
        private set
    var lastChangedKey: String = ""
        private set

    private var nextNoteSequence: Int = 1
    private var suppressEditorCallbacks: Boolean = false
    private var uiBuilt: Boolean = false
    private var lastObservedPinChecked: Boolean = false

    private val preferenceListener = PreferenceChangeListener { _, key, _ ->
        val resolvedKey = key ?: "clear"
        preferenceChangeCount += 1
        lastChangedKey = resolvedKey
        lastChangeBadge.text = "Pref changed: $resolvedKey (#$preferenceChangeCount)"
    }

    val searchInput: SearchField = SearchField(
        query = "",
        placeholder = "Search notes by title or content..."
    ).apply {
        id = "input-search"
    }

    val newNoteButton: Button = Button(
        text = "New Note",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-new-note"
        getBootstrap().apply {
            type = BootstrapType.PRIMARY
            size = BootstrapSize.SM
            fontWeight = FontWeight.SEMIBOLD
        }
    }

    val deleteNoteButton: Button = Button(
        text = "Delete Note",
        type = BootstrapType.DANGER,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-delete-note"
        getBootstrap().apply {
            type = BootstrapType.DANGER
            size = BootstrapSize.SM
        }
    }

    val restoreNoteButton: Button = Button(
        text = "Restore Note",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-restore-note"
        getBootstrap().apply {
            type = BootstrapType.SECONDARY
            size = BootstrapSize.SM
            outline = true
        }
    }

    val noteCountBadge: Badge = Badge(
        text = "0 notes",
        type = BootstrapType.PRIMARY
    ).apply {
        id = "badge-note-count"
        getBootstrap().apply {
            type = BootstrapType.PRIMARY
            fontWeight = FontWeight.SEMIBOLD
        }
    }

    val lastChangeBadge: Badge = Badge(
        text = "Pref changed: none",
        type = BootstrapType.INFO
    ).apply {
        id = "badge-last-change"
        getBootstrap().apply {
            type = BootstrapType.INFO
        }
    }

    val namespaceBadge: Badge = Badge(
        text = "Store: ${NoteItem.PREFERENCE_NAMESPACE}",
        type = BootstrapType.SECONDARY
    ).apply {
        id = "badge-pref-namespace"
        getBootstrap().apply {
            type = BootstrapType.SECONDARY
        }
    }

    val noteListGroup: ListGroup = ListGroup().apply {
        id = "note-list-group"
    }

    val noteListScrollView: ScrollView = ScrollView().apply {
        id = "note-list-scroll"
    }

    val prefNamespaceLabel: Label = Label(
        text = "Namespace: ${NoteItem.PREFERENCE_NAMESPACE}"
    ).apply {
        id = "label-pref-namespace"
        textColorType = BootstrapType.PRIMARY
    }

    val prefCodecLabel: Label = Label(
        text = "Codec: PreferenceObjectCodec (NoteItem)"
    ).apply {
        id = "label-pref-codec"
        textColorType = BootstrapType.SECONDARY
    }

    val prefIndexKeyLabel: Label = Label(
        text = "Index Key: ${NoteItem.NOTE_IDS_KEY} (1 stored)"
    ).apply {
        id = "label-pref-index-key"
        textColorType = BootstrapType.INFO
    }

    val prefActiveEntryLabel: Label = Label(
        text = "Active Key: note.note-1 (Pinned: yes)"
    ).apply {
        id = "label-pref-active-key"
        textColorType = BootstrapType.SECONDARY
    }

    val noteTitleInput: TextField = TextField(
        value = "",
        placeholder = "Note title..."
    ).apply {
        id = "input-note-title"
    }

    val noteBodyInput: TextArea = TextArea(
        value = "",
        placeholder = "Write Markdown note content...",
        rows = 8
    ).apply {
        id = "input-note-body"
    }

    val pinNoteCheckBox: CheckBox = CheckBox(
        label = "Pin to top",
        checked = false
    ).apply {
        id = "check-pin-note"
        getStyle().apply {
            accentColor = RgbaColor.fromHex("#8B5CF6")
        }
    }

    val exportMarkdownButton: Button = Button(
        text = "Export .md",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-export-md"
    }

    val importMarkdownButton: Button = Button(
        text = "Import .md",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-import-md"
    }

    val markdownPreview: MarkdownView = MarkdownView(
        markdown = ""
    ).apply {
        id = "markdown-preview"
    }

    val splitPane: SplitPane = SplitPane(
        orientation = Orientation.HORIZONTAL,
        dividerPosition = 0.35f
    ).apply {
        id = "notes-split-pane"
    }

    init {
        NoteItem.registerCodec()
        getPreferenceStore().addChangeListener(preferenceListener)
        loadNotesFromPreferences()
        ensureUiBuilt()
        syncAllViews()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        getPreferenceStore().addChangeListener(preferenceListener)
        if (!uiBuilt || children.isEmpty()) {
            uiBuilt = false
            ensureUiBuilt()
            syncAllViews()
        }
        schedulePinCheckBoxSyncLoop()
    }

    override fun onDestroy() {
        getPreferenceStore().removeChangeListener(preferenceListener)
        super.onDestroy()
    }

    private fun schedulePinCheckBoxSyncLoop() {
        setTimeout(1) {
            syncPinCheckBoxIfNeeded()
            schedulePinCheckBoxSyncLoop()
        }
    }

    private fun syncPinCheckBoxIfNeeded() {
        val currentChecked = pinNoteCheckBox.isChecked
        if (currentChecked != lastObservedPinChecked) {
            lastObservedPinChecked = currentChecked
            if (!suppressEditorCallbacks) {
                updateActiveNotePinned(currentChecked)
            }
        }
    }

    private fun ensureUiBuilt() {
        if (uiBuilt) return
        uiBuilt = true
        clearChildren()
        addClass("notes-shell")

        val rootStack = VStack(gap = SpacingScale.THREE)

        val headerBar = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        headerBar.addChild(Heading("Markdown Notes & Preferences", level = 3))
        headerBar.addChild(noteCountBadge)
        headerBar.addChild(lastChangeBadge)
        headerBar.addChild(namespaceBadge)
        rootStack.addChild(headerBar)

        // Left Pane: Search + Note List + Create/Delete/Restore controls + PreferenceStore Inspector
        val leftSidebar = VStack(gap = SpacingScale.TWO)
        val sidebarActions = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        sidebarActions.addChild(newNoteButton)
        sidebarActions.addChild(deleteNoteButton)
        sidebarActions.addChild(restoreNoteButton)
        leftSidebar.addChild(searchInput)
        leftSidebar.addChild(sidebarActions)

        val listCard = Card(title = "Saved Notes").apply {
            addClass("notes-sidebar-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.TWO
            }
        }
        noteListScrollView.clearChildren()
        noteListScrollView.addChild(noteListGroup)
        listCard.addChild(noteListScrollView)
        leftSidebar.addChild(listCard)

        val prefInspectorCard = Card(title = "PreferenceStore Inspector").apply {
            addClass("notes-inspector-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.TWO
            }
        }
        prefInspectorCard.addChild(prefNamespaceLabel)
        prefInspectorCard.addChild(prefCodecLabel)
        prefInspectorCard.addChild(prefIndexKeyLabel)
        prefInspectorCard.addChild(prefActiveEntryLabel)
        leftSidebar.addChild(prefInspectorCard)

        // Right Pane: Note Editor + Live MarkdownView Preview
        val rightEditorStack = VStack(gap = SpacingScale.TWO)
        val editorCard = Card(title = "Note Editor").apply {
            addClass("notes-editor-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.TWO
            }
        }
        val editorFields = VStack(gap = SpacingScale.TWO)

        val titleAndPinRow = HStack(gap = SpacingScale.TWO).apply {
            flexWrap = FlexWrap.WRAP
        }
        titleAndPinRow.addChild(noteTitleInput)
        titleAndPinRow.addChild(pinNoteCheckBox)
        titleAndPinRow.addChild(importMarkdownButton)
        titleAndPinRow.addChild(exportMarkdownButton)

        editorFields.addChild(titleAndPinRow)
        editorFields.addChild(noteBodyInput)
        editorCard.addChild(editorFields)
        rightEditorStack.addChild(editorCard)

        val previewCard = Card(title = "Live Markdown Preview").apply {
            addClass("notes-preview-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.TWO
            }
        }
        previewCard.addChild(markdownPreview)
        rightEditorStack.addChild(previewCard)

        splitPane.setPanes(leftSidebar, rightEditorStack)
        rootStack.addChild(splitPane)

        addChild(rootStack)
        wireEventHandlers()
    }

    private fun wireEventHandlers() {
        searchInput.onValueChanged = { query ->
            searchQuery = query
            rebuildNoteListView()
        }

        newNoteButton.onClick = {
            createNewNote()
        }

        deleteNoteButton.onClick = {
            deleteActiveNote()
        }

        restoreNoteButton.onClick = {
            restoreLastDeletedNote()
        }

        noteTitleInput.onValueChanged = { newTitle ->
            if (!suppressEditorCallbacks) {
                updateActiveNoteTitle(newTitle)
            }
        }

        noteBodyInput.onValueChanged = { newContent ->
            if (!suppressEditorCallbacks) {
                updateActiveNoteContent(newContent)
            }
        }

        pinNoteCheckBox.onCheckedChanged = { checked ->
            lastObservedPinChecked = checked
            if (!suppressEditorCallbacks) {
                updateActiveNotePinned(checked)
            }
        }

        exportMarkdownButton.onClick = {
            val active = getActiveNote()
            if (active != null) {
                val safeFileName = active.title
                    .lowercase()
                    .replace(Regex("[^a-z0-9]+"), "-")
                    .trim('-')
                    .ifEmpty { "note" } + ".md"
                Dialogs.saveFile(
                    title = "Export Markdown Note",
                    suggestedFileName = safeFileName,
                    filters = listOf(
                        FileTypeFilter("Markdown Files", listOf("md", "markdown")),
                        FileTypeFilter("Text Files", listOf("txt"))
                    ),
                    ownerWindow = attachedWindow,
                    onSelected = { selectedFile ->
                        lastChangeBadge.text = "Exported: ${selectedFile.name}"
                    }
                )
            }
        }

        importMarkdownButton.onClick = {
            Dialogs.openFile(
                title = "Import Markdown Note",
                filters = listOf(
                    FileTypeFilter("Markdown & Text Files", listOf("md", "markdown", "txt"))
                ),
                ownerWindow = attachedWindow,
                onSelected = { selectedFile ->
                    val importedTitle = selectedFile.name.substringBeforeLast('.').ifBlank { "Imported Note" }
                    createNewNote(title = importedTitle, content = "# $importedTitle\n\nImported from `${selectedFile.name}`.")
                }
            )
        }
    }

    fun loadNotesFromPreferences() {
        notes.clear()
        if (!getPreferenceStore().contains(NoteItem.NOTE_IDS_KEY)) {
            val starterNote = NoteItem(
                id = "note-1",
                title = "Welcome to GenericUI Notes",
                content = buildString {
                    appendLine("# Welcome to GenericUI Notes")
                    appendLine()
                    appendLine("Use **PreferenceStore** to persist structured notes across all 8 backends.")
                    appendLine()
                    appendLine("## Persistence Architecture")
                    appendLine("- **PreferenceObjectCodec** serializes `NoteItem` fields (`id`, `title`, `content`, `pinned`)")
                    appendLine("- **PreferenceChangeListener** streams real-time key mutation events to the status bar")
                    appendLine("- **Index Key (`notes.ids`)** tracks note ordering and instant crash-safe recovery")
                    appendLine()
                    appendLine("## Workspace Features")
                    appendLine("- Live full-text search filtering across note titles and Markdown bodies")
                    appendLine("- Pin priority notes to the top of the sidebar list")
                    appendLine("- Single-step undo via **Restore Note** and `.md` file import/export")
                }.trimEnd(),
                pinned = true
            )
            notes.add(starterNote)
            nextNoteSequence = 2
            activeNoteId = starterNote.id
            persistNote(starterNote)
            persistNoteIds()
            return
        }

        val ids = getPreferenceStore().getStringList(NoteItem.NOTE_IDS_KEY)
        var maxNum = 0
        for (id in ids) {
            val loaded = getPreferenceStore().get(
                key = "${NoteItem.NOTE_KEY_PREFIX}$id",
                targetClass = NoteItem::class
            )
            if (loaded != null) {
                notes.add(loaded)
                val suffixNum = id.removePrefix("note-").toIntOrNull() ?: 0
                if (suffixNum > maxNum) {
                    maxNum = suffixNum
                }
            }
        }
        nextNoteSequence = maxNum + 1
        activeNoteId = orderedNotes().firstOrNull()?.id
    }

    fun orderedNotes(): List<NoteItem> =
        notes.sortedWith(compareByDescending<NoteItem> { it.pinned }.thenBy { it.id })

    fun filteredNotes(): List<NoteItem> {
        val q = searchQuery.trim().lowercase()
        val base = orderedNotes()
        if (q.isEmpty()) return base
        return base.filter { note ->
            note.title.lowercase().contains(q) || note.content.lowercase().contains(q)
        }
    }

    fun getActiveNote(): NoteItem? =
        notes.firstOrNull { it.id == activeNoteId }

    fun selectNote(noteId: String) {
        if (notes.any { it.id == noteId }) {
            activeNoteId = noteId
            syncAllViews()
        }
    }

    fun createNewNote(
        title: String = "Untitled Note $nextNoteSequence",
        content: String = "# $title\n\nStart writing Markdown here...",
        pinned: Boolean = false
    ): NoteItem {
        val id = "note-${nextNoteSequence++}"
        val note = NoteItem(
            id = id,
            title = title,
            content = content,
            pinned = pinned
        )
        notes.add(note)
        activeNoteId = note.id
        persistNote(note)
        persistNoteIds()
        syncAllViews()
        return note
    }

    fun deleteActiveNote(): NoteItem? {
        val currentId = activeNoteId ?: return null
        val index = notes.indexOfFirst { it.id == currentId }
        if (index < 0) return null
        val removed = notes.removeAt(index)
        lastDeletedNote = removed
        getPreferenceStore().remove("${NoteItem.NOTE_KEY_PREFIX}${removed.id}")
        persistNoteIds()
        getPreferenceStore().flush()
        activeNoteId = orderedNotes().firstOrNull()?.id
        syncAllViews()
        return removed
    }

    fun restoreLastDeletedNote(): NoteItem? {
        val restored = lastDeletedNote ?: return null
        lastDeletedNote = null
        notes.removeAll { it.id == restored.id }
        notes.add(restored)
        activeNoteId = restored.id
        persistNote(restored)
        persistNoteIds()
        syncAllViews()
        return restored
    }

    private fun updateActiveNoteTitle(newTitle: String) {
        val active = getActiveNote() ?: return
        active.title = newTitle
        persistNote(active)
        rebuildNoteListView()
    }

    private fun updateActiveNoteContent(newContent: String) {
        val active = getActiveNote() ?: return
        active.content = newContent
        markdownPreview.markdown = newContent
        persistNote(active)
        rebuildNoteListView()
    }

    private fun updateActiveNotePinned(pinned: Boolean) {
        val active = getActiveNote() ?: return
        active.pinned = pinned
        persistNote(active)
        rebuildNoteListView()
    }

    private fun persistNote(note: NoteItem) {
        getPreferenceStore().put("${NoteItem.NOTE_KEY_PREFIX}${note.id}", note)
        getPreferenceStore().flush()
    }

    private fun persistNoteIds() {
        getPreferenceStore().putStringList(NoteItem.NOTE_IDS_KEY, notes.map { it.id })
        getPreferenceStore().flush()
    }

    private fun syncAllViews() {
        rebuildNoteListView()
        syncEditorWithActiveNote()
    }

    private fun updatePreferenceInspectorLabels() {
        val active = getActiveNote()
        prefIndexKeyLabel.text = "Index Key: ${NoteItem.NOTE_IDS_KEY} (${notes.size} stored)"
        prefActiveEntryLabel.text = if (active != null) {
            val pinState = if (active.pinned) "yes" else "no"
            "Active Key: ${NoteItem.NOTE_KEY_PREFIX}${active.id} (Pinned: $pinState)"
        } else {
            "Active Key: none"
        }
    }

    private fun rebuildNoteListView() {
        val visible = filteredNotes()
        noteCountBadge.text = "${visible.size} notes"
        updatePreferenceInspectorLabels()
        noteListGroup.clearChildren()

        if (visible.isEmpty()) {
            val emptyItem = ListGroupItem(id = "note-item-empty", isActive = false)
            emptyItem.addChild(Text("No matching notes"))
            noteListGroup.addChild(emptyItem)
            return
        }

        for (note in visible) {
            val prefix = if (note.pinned) "[Pinned] " else ""
            val displayTitle = note.title.ifBlank { "Untitled" }
            val isCurrent = note.id == activeNoteId
            val listItem = ListGroupItem(isActive = isCurrent)
            val itemButton = Button(
                text = "$prefix$displayTitle",
                type = if (isCurrent) BootstrapType.PRIMARY else BootstrapType.SECONDARY,
                size = BootstrapSize.SM,
                outline = !isCurrent
            ).apply {
                id = "note-item-${note.id}"
                onClick = {
                    selectNote(note.id)
                }
            }
            val lineCount = note.content.lines().size
            val charCount = note.content.length
            val itemMeta = Label(
                text = "Key: ${NoteItem.NOTE_KEY_PREFIX}${note.id} | $lineCount lines | $charCount chars"
            ).apply {
                textColorType = BootstrapType.SECONDARY
            }
            listItem.addChild(itemButton)
            listItem.addChild(itemMeta)
            noteListGroup.addChild(listItem)
        }
    }

    private fun syncEditorWithActiveNote() {
        suppressEditorCallbacks = true
        try {
            val active = getActiveNote()
            if (active != null) {
                noteTitleInput.value = active.title
                noteBodyInput.value = active.content
                pinNoteCheckBox.isChecked = active.pinned
                lastObservedPinChecked = active.pinned
                markdownPreview.markdown = active.content
            } else {
                noteTitleInput.value = ""
                noteBodyInput.value = ""
                pinNoteCheckBox.isChecked = false
                lastObservedPinChecked = false
                markdownPreview.markdown = "_No note selected._"
            }
        } finally {
            suppressEditorCallbacks = false
        }
    }
}
