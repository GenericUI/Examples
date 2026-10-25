package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.MarkdownView
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Multiplatform [JestElectricRunner] BDD & Robolectric-style UI test suite
 * for the Markdown Notes & Preferences example (`1.3`).
 */
class NotesJestTest {

    @Test
    fun runMarkdownNotesJestElectricSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Markdown Notes & Preferences Suite",
            visualOnWindows = false
        ) {
            describe("Markdown Notes CRUD, Search Filtering, and PreferenceStore Persistence") {
                it("creates notes, edits title and markdown body, updates live preview, and pins notes") {
                    val store = PreferenceStore("MarkdownNotesTest_1").apply { clear() }
                    val controller = launchApp(visualOnWindows = false) { NotesApp(store) }.setup()

                    // Starter note is automatically created on first launch
                    expectNode(controller.findById("badge-note-count")).toHaveText("1 notes")
                    expectNode(controller.findById("note-item-note-1")).toExist()

                    // Create a second note
                    controller.findById("btn-new-note").performClick()
                    expectNode(controller.findById("badge-note-count")).toHaveText("2 notes")
                    expectNode(controller.findById("note-item-note-2")).toExist()

                    // Edit title and markdown body
                    controller.findById("input-note-title").performTypeText("Release Checklist")
                    controller.findById("input-note-body").performTypeText("# Release v1.0\n- All 8 backends active")

                    val preview = controller.findById("markdown-preview").component as MarkdownView
                    expect(preview.markdown).toContain("# Release v1.0")
                    expectNode(controller.findById("note-item-note-2")).toContainText("Release Checklist")

                    // Pin the new note
                    controller.findById("check-pin-note").performSetChecked(true)
                    expectNode(controller.findById("check-pin-note")).toBeChecked()
                    expectNode(controller.findById("note-item-note-2")).toContainText("[Pinned] Release Checklist")

                    // PreferenceChangeListener badge should reflect recent key change
                    expectNode(controller.findById("badge-last-change")).toContainText("Pref changed:")

                    controller.destroy()
                }

                it("filters visible notes live when typing into the search field") {
                    val store = PreferenceStore("MarkdownNotesTest_2").apply { clear() }
                    val controller = launchFragment { NotesFragment(store) }.setup()

                    controller.findById("btn-new-note").performClick()
                    controller.findById("input-note-title").performTypeText("Kotlin Multiplatform Guide")
                    controller.findById("input-note-body").performTypeText("Targets Windows, macOS, Linux, Swing, Android, iOS, and Web.")

                    controller.findById("btn-new-note").performClick()
                    controller.findById("input-note-title").performTypeText("Grocery Shopping List")
                    controller.findById("input-note-body").performTypeText("- Coffee beans\n- Oat milk")

                    expectNode(controller.findById("badge-note-count")).toHaveText("3 notes")

                    // Filter by "Kotlin"
                    controller.findById("input-search").performTypeText("Kotlin")
                    expectNode(controller.findById("badge-note-count")).toHaveText("1 notes")
                    expectNode(controller.findById("note-item-note-2")).toExist()
                    expectNode(controller.findById("note-item-note-3")).not.toExist()

                    // Filter by unmatched query
                    controller.findById("input-search").performTypeText("nonexistent-query-xyz")
                    expectNode(controller.findById("badge-note-count")).toHaveText("0 notes")
                    expectNode(controller.findById("note-item-empty")).toExist()

                    // Clear filter
                    controller.findById("input-search").performTypeText("")
                    expectNode(controller.findById("badge-note-count")).toHaveText("3 notes")

                    controller.destroy()
                }

                it("deletes and restores notes and persists structured NoteItem objects across app restarts") {
                    val store = PreferenceStore("MarkdownNotesTest_3").apply { clear() }
                    val firstSession = launchApp(visualOnWindows = false) { NotesApp(store) }.setup()

                    firstSession.findById("btn-new-note").performClick()
                    firstSession.findById("input-note-title").performTypeText("Persistent Design Spec")
                    firstSession.findById("input-note-body").performTypeText("## Storage\nSaved via PreferenceObjectCodec.")
                    firstSession.findById("check-pin-note").performSetChecked(true)

                    expectNode(firstSession.findById("badge-note-count")).toHaveText("2 notes")

                    // Delete the active note ("note-2") and verify count drops to 1
                    firstSession.findById("btn-delete-note").performClick()
                    expectNode(firstSession.findById("badge-note-count")).toHaveText("1 notes")
                    expectNode(firstSession.findById("note-item-note-2")).not.toExist()

                    // Restore the deleted note and verify it returns
                    firstSession.findById("btn-restore-note").performClick()
                    expectNode(firstSession.findById("badge-note-count")).toHaveText("2 notes")
                    expectNode(firstSession.findById("note-item-note-2")).toExist()

                    firstSession.destroy()

                    // Verify structured NoteItem directly in PreferenceStore
                    val savedNote = store.getObject("note.note-2", NoteItem::class)
                    expect(savedNote).toBeNotNull()
                    expect(savedNote?.title).toBe("Persistent Design Spec")
                    expect(savedNote?.pinned).toBe(true)

                    // Launch a second app session and verify state is restored
                    val secondSession = launchApp(visualOnWindows = false) { NotesApp(store) }.setup()
                    expectNode(secondSession.findById("badge-note-count")).toHaveText("2 notes")
                    expectNode(secondSession.findById("note-item-note-2")).toContainText("[Pinned] Persistent Design Spec")

                    secondSession.findById("note-item-note-2").performClick()
                    expect(secondSession.app.notesFragment.noteTitleInput.value).toBe("Persistent Design Spec")
                    expectNode(secondSession.findById("check-pin-note")).toBeChecked()

                    secondSession.destroy()
                }
            }
        }
        report.assertAllPassed()
    }
}
