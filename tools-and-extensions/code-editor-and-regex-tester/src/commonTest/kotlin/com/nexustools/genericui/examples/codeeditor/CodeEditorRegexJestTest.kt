package com.nexustools.genericui.examples.codeeditor

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.ext.codeeditor.CodeLanguage
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Cross-platform [JestElectricRunner] BDD & Robolectric UI test suite for `code-editor-and-regex-tester`.
 */
class CodeEditorRegexJestTest {

    @Test
    fun runCodeEditorAndRegexTesterSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "CodeEditor & Regex Playground JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Syntax-Highlighted CodeEditor & Regex Evaluation") {

                it("mounts CodeEditorRegexFragment, verifies getStyle() and getBootstrap(), and evaluates initial Kotlin regex matches and capture groups") {
                    val store = PreferenceStore("CodeEditorRegexJestTest_Init")
                    store.clear()
                    val controller = launchFragment {
                        CodeEditorRegexFragment(store)
                    }

                    assertTrue(controller.fragment.hasClass("code-regex-root"))
                    expect(controller.fragment.topToolbar.getStyle().borderRadiusPx).toBe(8f)
                    expect(controller.fragment.codeEditor.getStyle().borderRadiusPx).toBe(6f)
                    expectNode(controller.findById("ide-code-editor")).toBeVisible()
                    expectNode(controller.findById("badge-active-language")).toHaveText("Kotlin")
                    expectNode(controller.findById("badge-match-count")).toHaveText("2 matches")
                    expect(controller.fragment.matchCountBadge.getBootstrap().type).toBe(BootstrapType.SUCCESS)

                    val matches = controller.fragment.lastRegexMatches
                    expect(matches).toHaveLength(2)
                    expect(matches[0].groups).toEqual(
                        listOf("calculateSubtotal", "items: List<InvoiceItem>, discountRate: Double", "Int")
                    )
                    expect(matches[1].groups).toEqual(
                        listOf("formatCurrency", "amountCents: Int, currencyCode: String", "String")
                    )
                    expect(controller.fragment.captureGroupsTable.rows).toHaveLength(2)
                    expect(controller.fragment.codeEditor.getDiagnostics()).toHaveLength(2)

                    controller.destroy()
                }

                it("switches syntax languages (KOTLIN, TYPESCRIPT, JSON, CSS) and edits code buffer") {
                    val store = PreferenceStore("CodeEditorRegexJestTest_Lang")
                    store.clear()
                    val controller = launchFragment {
                        CodeEditorRegexFragment(store)
                    }

                    controller.findById("select-language").performSelectValue(CodeLanguage.TYPESCRIPT.name)
                    expect(controller.fragment.codeEditor.language).toBe(CodeLanguage.TYPESCRIPT)
                    expectNode(controller.findById("badge-active-language")).toHaveText("TypeScript")

                    controller.findById("select-language").performSelectValue(CodeLanguage.JSON.name)
                    expect(controller.fragment.codeEditor.language).toBe(CodeLanguage.JSON)
                    expectNode(controller.findById("badge-active-language")).toHaveText("JSON")

                    controller.fragment.setBufferText(
                        """
                        {
                          "service": "auth-gateway",
                          "port": 8080,
                          "backupPort": 8443
                        }
                        """.trimIndent()
                    )
                    expect(controller.fragment.codeEditor.document.lineCount).toBe(5)
                    expectNode(controller.findById("badge-line-count")).toHaveText("5 lines")

                    controller.findById("input-regex-pattern").performTypeText(""""(\w*Port|port)"\s*:\s*(\d+)""")
                    controller.findById("btn-run-regex").performClick()

                    expectNode(controller.findById("badge-match-count")).toHaveText("2 matches")
                    expect(controller.fragment.lastRegexMatches[0].groups).toEqual(listOf("port", "8080"))
                    expect(controller.fragment.lastRegexMatches[1].groups).toEqual(listOf("backupPort", "8443"))

                    controller.destroy()
                }

                it("toggles case-insensitive and multiline flags and updates match counts accurately") {
                    val store = PreferenceStore("CodeEditorRegexJestTest_Flags")
                    store.clear()
                    val controller = launchFragment {
                        CodeEditorRegexFragment(store)
                    }

                    controller.fragment.setBufferText(
                        """
                        ERROR: Disk quota exceeded on node-01
                        error: Connection reset on node-02
                        WARN: Slow query on node-03
                        """.trimIndent()
                    )

                    controller.findById("input-regex-pattern").performTypeText("""^error:\s+(.+)""")
                    controller.findById("check-ignore-case").performSetChecked(false)
                    controller.findById("check-multiline").performSetChecked(true)
                    controller.findById("btn-run-regex").performClick()

                    expectNode(controller.findById("badge-match-count")).toHaveText("1 matches")
                    expect(controller.fragment.lastRegexMatches[0].groups).toEqual(
                        listOf("Connection reset on node-02")
                    )

                    // Enable case-insensitive matching -> matches both ERROR and error lines
                    controller.findById("check-ignore-case").performSetChecked(true)
                    controller.findById("btn-run-regex").performClick()

                    expectNode(controller.findById("badge-match-count")).toHaveText("2 matches")
                    expect(controller.fragment.lastRegexMatches[0].groups).toEqual(
                        listOf("Disk quota exceeded on node-01")
                    )
                    expect(controller.fragment.lastRegexMatches[1].groups).toEqual(
                        listOf("Connection reset on node-02")
                    )

                    controller.destroy()
                }

                it("saves and reloads custom code and regex snippets via PreferenceStore") {
                    val storeName = "CodeEditorRegexJestTest_Persistence"
                    val store = PreferenceStore(storeName)
                    store.clear()

                    val controller1 = launchFragment {
                        CodeEditorRegexFragment(store)
                    }

                    controller1.findById("select-language").performSelectValue(CodeLanguage.CSS.name)
                    controller1.fragment.setBufferText(
                        """
                        .btn-primary { color: #6366F1; }
                        .btn-success { color: #10B981; }
                        """.trimIndent()
                    )
                    controller1.findById("input-regex-pattern").performTypeText("""\.(btn-[a-z]+)\s*\{\s*color:\s*(#[0-9A-F]{6});\s*\}""")
                    controller1.findById("input-snippet-title").performTypeText("Custom Button Palette")
                    controller1.findById("btn-save-snippet").performClick()

                    expectNode(controller1.findById("badge-match-count")).toHaveText("2 matches")
                    controller1.destroy()

                    // Re-open with a fresh Fragment backed by the same PreferenceStore
                    val reloadedStore = PreferenceStore(storeName)
                    val controller2 = launchFragment {
                        CodeEditorRegexFragment(reloadedStore)
                    }

                    val loadedPreset = controller2.fragment.loadPresetById("custom-button-palette")
                    expect(loadedPreset).toBeNotNull()
                    expect(loadedPreset?.title).toBe("Custom Button Palette")
                    expect(loadedPreset?.language).toBe(CodeLanguage.CSS.name)
                    expectNode(controller2.findById("badge-active-language")).toHaveText("CSS")
                    expectNode(controller2.findById("badge-match-count")).toHaveText("2 matches")
                    expect(controller2.fragment.lastRegexMatches[0].groups).toEqual(listOf("btn-primary", "#6366F1"))
                    expect(controller2.fragment.lastRegexMatches[1].groups).toEqual(listOf("btn-success", "#10B981"))

                    controller2.destroy()
                    reloadedStore.clear()
                }
            }
        }
        report.assertAllPassed()
    }
}
