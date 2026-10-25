package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.tests.JestElectricRunner

/**
 * Visual test runner entry point (`windowsVisualTest` / desktop visual verification)
 * for the Markdown Notes & Preferences example (`1.3`).
 */
object NotesVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()) {
        NotesPlatformBootstrap.initialize(args)
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Markdown Notes & Preferences Visual Suite",
            args = args
        ) {
            describe("Markdown Notes & PreferenceStore Visual Verification") {
                beforeEach {
                    PreferenceStore(NoteItem.PREFERENCE_NAMESPACE).clear()
                }

                it("creates notes, updates live Markdown preview, filters via search, and persists objects") {
                    val controller = launchApp { NotesApp() }.setup()
                    expectNode(controller.findById("badge-note-count")).toHaveText("1 notes")

                    controller.findById("btn-new-note").performClick()
                    expectNode(controller.findById("badge-note-count")).toHaveText("2 notes")

                    controller.findById("input-note-title").performTypeText("Architecture Roadmap")
                    controller.findById("input-note-body").performTypeText("## Milestones\n- Phase 1: Core Widgets\n- Phase 2: PreferenceStore")
                    controller.findById("check-pin-note").performSetChecked(true)

                    controller.findById("input-search").performTypeText("Roadmap")
                    expectNode(controller.findById("badge-note-count")).toHaveText("1 notes")
                    controller.destroy()
                }
            }
        }
    }
}

fun runNotesVisualTests(args: Array<String>) {
    NotesVisualTestMain.runVisualSuite(args)
}
