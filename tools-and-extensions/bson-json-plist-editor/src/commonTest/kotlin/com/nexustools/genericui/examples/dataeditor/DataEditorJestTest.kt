package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.examples.dataeditor.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertTrue

class DataEditorJestTest {

    @Test
    fun runDataEditorJestElectricSuite() {
        JestElectricRunner.runSuite(
            suiteName = "BSON / JSON / Binary Plist LazyGridTree Visual Editor Suite",
            visualOnWindows = false
        ) {
            describe("Visual Multi-Tab Document Lifecycle & LazyGridTree Node Editing") {
                test("launches DataEditorApp and verifies initial LazyGridTree, getStyle(), getBootstrap(), and tab state") {
                    val controller = launchApp(visualOnWindows = false) { DataEditorApp() }.setup()
                    val frag = controller.app.fragment
                    assertTrue(controller.app.hasStyleSheet(StylesPrecompiledStyleSheet))
                    assertTrue(frag.hasClass("data-editor-root"))
                    expect(frag.topToolbar.getStyle().borderRadiusPx).toBe(6f)
                    expect(frag.openTabs.size).toBe(1)
                    expect(frag.activeFormatBadge.text).toBe("JSON")
                    expect(frag.activeFormatBadge.getBootstrap().type).toBe(BootstrapType.PRIMARY)
                    expect(frag.convertToJsonButton.getBootstrap().outline).toBe(false)
                    expect(frag.convertToBsonButton.getBootstrap().outline).toBe(true)
                    expect(frag.documentGridTree.columns).toBe(4)
                    expect(frag.documentGridTree.visibleRowCount).toBeGreaterThan(1)
                    controller.destroy()
                }

                test("loads JSON string into new tab, mutates visual LazyGridTree leaves and branches, and provides updated JSON string") {
                    val store = PreferenceStore.open("DataEditorJestTest_Store")
                    store.clear()
                    val controller = launchFragment { DataEditorFragment(store) }.setup()
                    val frag = controller.fragment

                    val tab = frag.loadJsonStringIntoNewTab(
                        jsonText = """{"app":"GenericUI","version":2,"features":{"mdi":true,"scss":true}}""",
                        title = "feature-flags.json",
                        format = DataFormat.JSON
                    )
                    expect(tab).toBeNotNull()
                    expect(frag.openTabs.size).toBe(2)

                    // Edit leaf value and key via LazyGridTree
                    expect(frag.updateLeafValueInActiveTab("root.app", "GenericUI Studio")).toBe(true)
                    expect(frag.renameNodeInActiveTab("root.version", "majorVersion")).toBe("root.majorVersion")
                    expect(frag.moveNodeUpInActiveTab("root.majorVersion")).toBe("root.majorVersion")

                    // Add a new leaf to branch "root.features"
                    val newLeafPath = frag.addLeafToBranchInActiveTab(
                        branchPath = "root.features",
                        key = "lazyGridTree",
                        type = DataNodeValueType.BOOLEAN,
                        value = "true"
                    )
                    expect(newLeafPath).toBe("root.features.lazyGridTree")

                    val outputJson = frag.provideActiveTabJsonString(pretty = false)
                    expect(outputJson).toContain("GenericUI Studio")
                    expect(outputJson).toContain("majorVersion")
                    expect(outputJson).toContain("lazyGridTree")

                    frag.convertActiveTabFormat(DataFormat.BINARY_JSON_BSON)
                    expect(frag.activeFormatBadge.text).toBe("BSON")
                    expect(frag.activeFormatBadge.getBootstrap().type).toBe(BootstrapType.INFO)
                    expect(frag.convertToBsonButton.getBootstrap().outline).toBe(false)

                    frag.convertActiveTabFormat(DataFormat.BINARY_PLIST)
                    expect(frag.activeFormatBadge.text).toBe("PLIST")
                    expect(frag.activeFormatBadge.getBootstrap().type).toBe(BootstrapType.SUCCESS)
                    expect(frag.convertToPlistButton.getBootstrap().outline).toBe(false)

                    controller.destroy()
                }
            }
        }
    }
}
