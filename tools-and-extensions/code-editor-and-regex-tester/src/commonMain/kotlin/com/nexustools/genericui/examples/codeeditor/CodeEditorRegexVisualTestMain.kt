package com.nexustools.genericui.examples.codeeditor

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.ext.codeeditor.CodeLanguage
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point using [JestElectricRunner.runVisualAppTestMain] for the
 * Syntax-Highlighted CodeEditor & Regex Playground example.
 */
object CodeEditorRegexVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "CodeEditor & Regex Playground Visual Suite",
            args = args
        ) {
            describe("CodeEditor & Live Regex Playground") {
                it("evaluates initial Kotlin function regex and populates capture group rows") {
                    val store = PreferenceStore("CodeEditorRegexVisualTest_1")
                    store.clear()
                    val controller = launchApp(visualOnWindows = visualOnWindows) {
                        CodeEditorRegexApp(store)
                    }

                    expectNode(controller.findById("badge-active-language")).toHaveText("Kotlin")
                    expectNode(controller.findById("badge-match-count")).toHaveText("2 matches")
                    expect(controller.app.regexFragment.lastRegexMatches.size).toBe(2)
                    expect(controller.app.regexFragment.lastRegexMatches[0].groups).toEqual(
                        listOf("calculateSubtotal", "items: List<InvoiceItem>, discountRate: Double", "Int")
                    )
                    controller.destroy()
                }

                it("switches syntax languages, edits buffer text, toggles ignore-case, and saves snippets to PreferenceStore") {
                    val store = PreferenceStore("CodeEditorRegexVisualTest_2")
                    store.clear()
                    val controller = launchApp(visualOnWindows = visualOnWindows) {
                        CodeEditorRegexApp(store)
                    }

                    controller.findById("select-language").performSelectValue(CodeLanguage.TYPESCRIPT.name)
                    expectNode(controller.findById("badge-active-language")).toHaveText("TypeScript")

                    controller.app.regexFragment.setBufferText(
                        """
                        const endpointA = "HTTP://api.nexustools.io/v1/status";
                        const endpointB = "https://api.nexustools.io/v2/metrics";
                        """.trimIndent()
                    )

                    controller.findById("input-regex-pattern").performTypeText("""https?://([a-z.]+)/v(\d+)/(\w+)""")
                    controller.findById("check-ignore-case").performSetChecked(false)
                    controller.findById("btn-run-regex").performClick()
                    expectNode(controller.findById("badge-match-count")).toHaveText("1 matches")

                    controller.findById("check-ignore-case").performSetChecked(true)
                    controller.findById("btn-run-regex").performClick()
                    expectNode(controller.findById("badge-match-count")).toHaveText("2 matches")

                    controller.findById("input-snippet-title").performTypeText("Nexus API URLs")
                    controller.findById("btn-save-snippet").performClick()

                    val savedPreset = controller.app.regexFragment.loadSavedSnippetFromStore("nexus-api-urls")
                    expect(savedPreset).toBeNotNull()
                    expect(savedPreset?.title).toBe("Nexus API URLs")
                    controller.destroy()
                }
            }
        }
}
