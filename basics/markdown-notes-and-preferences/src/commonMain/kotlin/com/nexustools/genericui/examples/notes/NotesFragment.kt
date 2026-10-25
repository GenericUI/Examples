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
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.shell.SplitPane
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.surface.ListGroup
import com.nexustools.genericui.component.surface.ListGroupItem
import com.nexustools.genericui.component.system.PreferenceChangeListener
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.MarkdownView
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.Orientation

/**
 * Primary UI [Fragment] for the Markdown Notes & Preferences example (`1.3`).
 *
 * Demonstrates:
 * - Two-pane layout using [SplitPane] (`note-list` sidebar on the left, editor + live [MarkdownView] on the right).
 * - Structured object persistence in [PreferenceStore] (`putObject` / `getObject` with [NoteItem]).
 * - Live [PreferenceChangeListener] notifications updating a status [Badge] (`badge-last-change`).
 * - Live search filtering via [SearchField] (`input-search`).
 * - Native file import/export via [Dialogs.openFile] and [Dialogs.saveFile].
 */
class NotesFragment(
    val preferenceStore: PreferenceStore = PreferenceStore(NoteItem.PREFERENCE_NAMESPACE)
) : Fragment() {

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
    }

    val deleteNoteButton: Button = Button(
        text = "Delete Note",
        type = BootstrapType.DANGER,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-delete-note"
    }

    val restoreNoteButton: Button = Button(
        text = "Restore Note",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-restore-note"
    }

    val noteCountBadge: Badge = Badge(
        text = "0 notes",
        type = BootstrapType.PRIMARY
    ).apply {
        id = "badge-note-count"
    }

    val lastChangeBadge: Badge = Badge(
        text = "Pref changed: none",
        type = BootstrapType.INFO
    ).apply {
        id = "badge-last-change"
    }

    val noteListGroup: ListGroup = ListGroup().apply {
        id = "note-list-group"
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
        preferenceStore.addChangeListener(preferenceListener)
        loadNotesFromPreferences()
        ensureUiBuilt()
        syncAllViews()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        preferenceStore.addChangeListener(preferenceListener)
        if (!uiBuilt || children.isEmpty()) {
            uiBuilt = false
            ensureUiBuilt()
            syncAllViews()
        }
        schedulePinCheckBoxSyncLoop()
    }

    override fun onDestroy() {
        preferenceStore.removeChangeListener(preferenceListener)
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

        val headerBar = HStack(gap = SpacingScale.TWO)
        headerBar.addChild(Heading("Markdown Notes & Preferences", level = 3))
        headerBar.addChild(noteCountBadge)
        headerBar.addChild(lastChangeBadge)
        rootStack.addChild(headerBar)

        // Left Pane: Search + Note List + Create/Delete/Restore controls
        val leftSidebar = VStack(gap = SpacingScale.TWO)
        val sidebarActions = HStack(gap = SpacingScale.TWO)
        sidebarActions.addChild(newNoteButton)
        sidebarActions.addChild(deleteNoteButton)
        sidebarActions.addChild(restoreNoteButton)
        leftSidebar.addChild(searchInput)
        leftSidebar.addChild(sidebarActions)

        val listCard = Card(title = "Saved Notes")
        listCard.addClass("notes-sidebar-card")
        listCard.addChild(noteListGroup)
        leftSidebar.addChild(listCard)

        // Right Pane: Note Editor + Live MarkdownView Preview
        val rightEditorStack = VStack(gap = SpacingScale.TWO)
        val editorCard = Card(title = "Note Editor")
        editorCard.addClass("notes-editor-card")
        val editorFields = VStack(gap = SpacingScale.TWO)

        val titleAndPinRow = HStack(gap = SpacingScale.TWO)
        titleAndPinRow.addChild(noteTitleInput)
        titleAndPinRow.addChild(pinNoteCheckBox)
        titleAndPinRow.addChild(importMarkdownButton)
        titleAndPinRow.addChild(exportMarkdownButton)

        editorFields.addChild(titleAndPinRow)
        editorFields.addChild(noteBodyInput)
        editorCard.addChild(editorFields)
        rightEditorStack.addChild(editorCard)

        val previewCard = Card(title = "Live Markdown Preview")
        previewCard.addClass("notes-preview-card")
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
        if (!preferenceStore.contains(NoteItem.NOTE_IDS_KEY)) {
            val starterNote = NoteItem(
                id = "note-1",
                title = "Welcome to GenericUI Notes",
                content = "# Welcome to GenericUI Notes\n\nUse **PreferenceStore** to persist structured notes across all 8 backends.",
                pinned = true
            )
            notes.add(starterNote)
            nextNoteSequence = 2
            activeNoteId = starterNote.id
            persistNote(starterNote)
            persistNoteIds()
            return
        }

        val ids = preferenceStore.getStringList(NoteItem.NOTE_IDS_KEY)
        var maxNum = 0
        for (id in ids) {
            val loaded = preferenceStore.getObject(
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
        preferenceStore.remove("${NoteItem.NOTE_KEY_PREFIX}${removed.id}")
        persistNoteIds()
        preferenceStore.flush()
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
        if (searchQuery.isNotBlank()) {
            rebuildNoteListView()
        }
    }

    private fun updateActiveNotePinned(pinned: Boolean) {
        val active = getActiveNote() ?: return
        active.pinned = pinned
        persistNote(active)
        rebuildNoteListView()
    }

    private fun persistNote(note: NoteItem) {
        preferenceStore.putObject("${NoteItem.NOTE_KEY_PREFIX}${note.id}", note)
        preferenceStore.flush()
    }

    private fun persistNoteIds() {
        preferenceStore.putStringList(NoteItem.NOTE_IDS_KEY, notes.map { it.id })
        preferenceStore.flush()
    }

    private fun syncAllViews() {
        rebuildNoteListView()
        syncEditorWithActiveNote()
    }

    private fun rebuildNoteListView() {
        val visible = filteredNotes()
        noteCountBadge.text = "${visible.size} notes"
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
            listItem.addChild(itemButton)
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
