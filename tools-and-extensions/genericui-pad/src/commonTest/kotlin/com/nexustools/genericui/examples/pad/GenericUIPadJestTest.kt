package com.nexustools.genericui.examples.pad

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.pad.res.Resource
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertTrue

class GenericUIPadJestTest {

    @Test
    fun runGenericUIPadJestElectricSuite() {
        JestElectricRunner.runSuite(
            suiteName = "GenericUIPad Windows 11 Dark-Mode Tabbed Notepad Suite",
            visualOnWindows = false
        ) {
            describe("Initial 6 Windows 11 Notepad Tabs, Active 'CharG' Tab & 6-Column Status Bar") {
                test("launches GenericUIPadApp with 6 tabs and active 'CharG' tab showing 342 characters and 'Ln 1, Col 343'") {
                    val controller = launchApp(visualOnWindows = false) { GenericUIPadApp() }
                    controller.takeScreenshot("genericui-pad.jpg")
                    val frag = controller.app.fragment

                    assertTrue(controller.app.hasStyleSheet(Resource.style.styles))
                    assertTrue(frag.hasClass("genericui-pad-root"))

                    expect(frag.tabCount).toBe(6)
                    expect(frag.tabs.map { it.title }).toEqual(
                        listOf("is", "pink", "=== E", "include", "I've cre", "CharG")
                    )
                    expect(frag.tabs.all { it.badgeText == "•" }).toBe(true)

                    expect(frag.activeTabId).toBe("tab-chargen")
                    val charGenDoc = frag.activeDocument
                    expect(charGenDoc).toBeNotNull()
                    expect(charGenDoc?.title).toBe("CharG")
                    expect(charGenDoc?.characterCount).toBe(342)
                    expect(charGenDoc?.content).toBe(GenericUIPadFragment.CHARGEN_TAB_CONTENT)
                    expect(charGenDoc?.spellCheckFindings).toContain("CharGenView")
                    expect(charGenDoc?.spellCheckFindings).toContain("DCGame")

                    // Verify 6-column bottom status bar
                    expect(frag.cursorStatusLabel.text).toBe("Ln 1, Col 343")
                    expect(frag.charCountStatusLabel.text).toBe("342 characters")
                    expect(frag.modeStatusLabel.text).toBe("Plain text")
                    expect(frag.zoomStatusLabel.text).toBe("100%")
                    expect(frag.lineEndingStatusLabel.text).toBe("Windows (CRLF)")
                    expect(frag.encodingStatusLabel.text).toBe("UTF-8")

                    controller.destroy()
                }
            }

            describe("Tab Switching, Opening New Tab via '+', Closing Tabs, Formatting, Find/Replace & Zoom") {
                test("switches tabs, opens a new tab via requestNewTab(), closes a tab, applies formatting, find & replace, and zoom") {
                    val store = PreferenceStore.open("GenericUIPadJestTest_Actions")
                    store.clear()
                    val controller = launchFragment { GenericUIPadFragment(store) }
                    val frag = controller.fragment

                    // Switch to "tab-pink"
                    expect(frag.selectTab("tab-pink")).toBe(true)
                    expect(frag.activeTabId).toBe("tab-pink")
                    expect(frag.activeDocument?.title).toBe("pink")

                    // Open a new tab via `+` (`requestNewTab()`)
                    val openedSpec = frag.requestNewTab()
                    expect(openedSpec).toBeNotNull()
                    expect(frag.tabCount).toBe(7)
                    expect(frag.activeTabId).toBe(openedSpec?.tabId)
                    expect(frag.activeDocument?.title).toBe("Untitled")
                    expect(frag.charCountStatusLabel.text).toBe("0 characters")

                    // Edit text and apply rich formatting actions (`BOLD`, `HEADING_1`, `BULLET_LIST`, `TABLE`)
                    val activeTabFrag = frag.activeDocumentTabFragment!!
                    activeTabFrag.updateContent("Notepad Formatting Test")
                    expect(frag.applyFormatToActiveTab(PadFormatAction.BOLD)).toBe("**Notepad Formatting Test**")
                    expect(frag.applyFormatToActiveTab(PadFormatAction.HEADING_1)).toContain("# ")
                    expect(frag.applyFormatToActiveTab(PadFormatAction.BULLET_LIST)).toContain("- ")
                    expect(frag.applyFormatToActiveTab(PadFormatAction.TABLE)).toContain("| Column 1 | Column 2 |")

                    // Switch back to "tab-chargen" and test Find & Replace
                    expect(frag.selectTab("tab-chargen")).toBe(true)
                    frag.findQuery = "CharGenView"
                    val firstMatchIndex = frag.findNext()
                    expect(firstMatchIndex).toBe(0)

                    frag.replaceText = "CharacterSheetView"
                    val replacedCount = frag.replaceAll()
                    expect(replacedCount).toBe(2)
                    expect(frag.activeDocument?.content).toContain("CharacterSheetView")

                    // Test Zoom In / Zoom Out / Reset Zoom
                    expect(frag.zoomIn()).toBe(110)
                    expect(frag.zoomStatusLabel.text).toBe("110%")
                    expect(frag.zoomOut(20)).toBe(90)
                    expect(frag.zoomStatusLabel.text).toBe("90%")
                    expect(frag.resetZoom()).toBe(100)
                    expect(frag.zoomStatusLabel.text).toBe("100%")

                    // Close the newly created tab
                    expect(frag.closeTab(openedSpec!!.tabId)).toBe(true)
                    expect(frag.tabCount).toBe(6)

                    controller.destroy()
                }
            }

            describe("WritableByteBuffer / ReadableByteBuffer Stream I/O & PreferenceStore Session Persistence") {
                test("exports and imports documents via WritableByteBuffer and ReadableByteBuffer and persists PadSessionSettings") {
                    val store = PreferenceStore.open("GenericUIPadJestTest_Persistence")
                    store.clear()
                    val controller = launchFragment { GenericUIPadFragment(store) }
                    val frag = controller.fragment

                    // Export "tab-chargen" to WritableByteBuffer
                    val writableBuffer = WritableByteBuffer()
                    val writtenBytes = frag.exportDocumentToWritable("tab-chargen", writableBuffer)
                    expect(writtenBytes).toBe(342)
                    expect(writableBuffer.toString("utf8")).toBe(GenericUIPadFragment.CHARGEN_TAB_CONTENT)

                    // Import a new Markdown document from ReadableByteBuffer
                    val markdownSample = "# GenericUI Pad\r\nCross-platform Windows 11 Notepad clone."
                    val readableBuffer = ReadableByteBuffer(markdownSample.encodeToByteArray())
                    val importedDoc = frag.importDocumentFromReadable("notes.md", readableBuffer)
                    expect(importedDoc.title).toBe("notes.md")
                    expect(importedDoc.mode).toBe(PadDocumentMode.MARKDOWN)
                    expect(importedDoc.lineEnding).toBe(PadLineEnding.WINDOWS_CRLF)
                    expect(frag.activeTabId).toBe(importedDoc.docId)
                    expect(frag.modeStatusLabel.text).toBe("Markdown")

                    // Persist and restore PadSessionSettings via getPreferenceStore()
                    val customSettings = PadSessionSettings(
                        wordWrap = false,
                        spellCheckEnabled = true,
                        showStatusBar = true,
                        defaultZoomPercent = 120,
                        defaultEncoding = "UTF-8",
                        activeTabId = "tab-include",
                        openTabCount = frag.tabCount
                    )
                    frag.getPreferenceStore().put("pad.settings", customSettings)
                    val restoredFromStore = frag.getPreferenceStore().get("pad.settings", PadSessionSettings())
                    expect(restoredFromStore).toBe(customSettings)

                    // Also test BinaryJson session stream export/import over WritableByteBuffer & ReadableByteBuffer
                    val settingsOut = WritableByteBuffer()
                    frag.updateSessionSettings(customSettings)
                    frag.exportSessionSettingsToWritable(settingsOut)
                    val settingsIn = ReadableByteBuffer(settingsOut.toByteArray())
                    val decodedSettings = PadSessionSettings.importFromReadable(settingsIn)
                    expect(decodedSettings).toBe(customSettings)

                    controller.destroy()
                }
            }
        }
    }
}
