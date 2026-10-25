package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.util.BinaryPlist
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DataEditorExampleTest {

    @Test
    fun testLazyGridTreeInlineLeafAndBranchEditingAndReordering() {
        val fragment = DataEditorFragment(PreferenceStore.open("DataEditorExampleTest_1"))

        val inputJson = """
            {
              "project": "GenericUI",
              "backendsCount": 8,
              "visualOnly": true,
              "formats": ["json", "bson", "plist"],
              "metadata": {
                "author": "NexusTools",
                "rating": 4.95
              }
            }
        """.trimIndent()

        val tab = fragment.loadJsonStringIntoNewTab(
            jsonText = inputJson,
            title = "config.json",
            format = DataFormat.JSON
        )
        assertNotNull(tab, "Expected valid JSON string to load into a new visual LazyGridTree tab")
        assertEquals(DataFormat.JSON, tab.format)

        // Verify LazyGridTree has 4 columns and flattened rows populated
        assertEquals(4, fragment.documentGridTree.columns)
        assertTrue(fragment.documentGridTree.visibleRowCount > 0)

        // 1. Edit leaf value inline via LazyGridTree row controls ("root.metadata.author")
        val authorValField = fragment.rowValueFieldsByPath["root.metadata.author"]
        assertNotNull(authorValField, "Expected inline value TextField for leaf 'root.metadata.author'")
        assertEquals("NexusTools", authorValField.value)
        assertTrue(fragment.updateLeafValueInActiveTab("root.metadata.author", "NexusTools Studio"))
        assertTrue(tab.isDirty)

        // 2. Rename leaf key inline via LazyGridTree ("root.metadata.author" -> "root.metadata.creator")
        val renamedLeafPath = fragment.renameNodeInActiveTab("root.metadata.author", "creator")
        assertEquals("root.metadata.creator", renamedLeafPath)
        assertEquals("NexusTools Studio", tab.rootNode.findByPath("root.metadata.creator")?.primitiveText)

        // 3. Move leaf down and up within its parent branch ("root.metadata")
        assertTrue(tab.rootNode.canMoveDownByPath("root.metadata.creator"))
        val movedDownPath = fragment.moveNodeDownInActiveTab("root.metadata.creator")
        assertEquals("root.metadata.creator", movedDownPath)
        assertEquals(1, tab.rootNode.siblingIndexOfPath("root.metadata.creator"))

        val moveUpBtn = fragment.rowMoveUpButtonsByPath["root.metadata.creator"]
        assertNotNull(moveUpBtn)
        moveUpBtn.onClick?.invoke()
        assertEquals(0, tab.rootNode.siblingIndexOfPath("root.metadata.creator"))

        // 4. Add a new leaf to branch "root.metadata" using the branch's "+ Leaf" button
        val addLeafBtn = fragment.rowAddLeafButtonsByPath["root.metadata"]
        assertNotNull(addLeafBtn, "Expected '+ Leaf' button on branch 'root.metadata'")
        val addedLeafPath = fragment.addLeafToBranchInActiveTab(
            branchPath = "root.metadata",
            key = "releaseYear",
            type = DataNodeValueType.INT,
            value = "2026"
        )
        assertEquals("root.metadata.releaseYear", addedLeafPath)

        // 5. Add a nested branch to "root.metadata" and rename its key
        val addedBranchPath = fragment.addBranchToBranchInActiveTab(
            branchPath = "root.metadata",
            key = "links",
            branchType = DataNodeValueType.OBJECT
        )
        assertEquals("root.metadata.links", addedBranchPath)
        val renamedBranchPath = fragment.renameNodeInActiveTab("root.metadata.links", "urls")
        assertEquals("root.metadata.urls", renamedBranchPath)
        assertNotNull(tab.rootNode.findByPath("root.metadata.urls"))

        // 6. Move branch "root.metadata" up inside "root"
        val indexBeforeMove = tab.rootNode.siblingIndexOfPath("root.metadata")
        assertTrue(indexBeforeMove > 0)
        fragment.moveNodeUpInActiveTab("root.metadata")
        assertEquals(indexBeforeMove - 1, tab.rootNode.siblingIndexOfPath("root.metadata"))

        // 7. Reorder array elements inside "root.formats" (move "json" [0] down to [1])
        assertEquals("json", tab.rootNode.findByPath("root.formats[0]")?.primitiveText)
        assertEquals("bson", tab.rootNode.findByPath("root.formats[1]")?.primitiveText)
        val newArrayItemPath = fragment.moveNodeDownInActiveTab("root.formats[0]")
        assertEquals("root.formats[1]", newArrayItemPath)
        assertEquals("bson", tab.rootNode.findByPath("root.formats[0]")?.primitiveText)
        assertEquals("json", tab.rootNode.findByPath("root.formats[1]")?.primitiveText)

        // 8. Provide pretty and compact JSON strings from the mutated visual tree
        val prettyJson = fragment.provideActiveTabJsonString(pretty = true)
        val compactJson = fragment.provideActiveTabJsonString(pretty = false)

        assertTrue(prettyJson.contains("\"creator\": \"NexusTools Studio\""))
        assertTrue(prettyJson.contains("\"releaseYear\": 2026"))
        assertFalse(compactJson.contains("\n"))

        val reparsed = DataDocumentCodec.parseJsonString(prettyJson)
        assertEquals("NexusTools Studio", reparsed["metadata"]?.get("creator")?.asString())
        assertEquals(2026, reparsed["metadata"]?.get("releaseYear")?.asInt())
        assertEquals(8, reparsed["backendsCount"]?.asInt())
        assertEquals(true, reparsed["visualOnly"]?.asBoolean())
    }

    @Test
    fun testAllTwelveDataTypesAndLosslessRoundTripBetweenJsonBsonAndBinaryPlist() {
        val fragment = DataEditorFragment(PreferenceStore.open("DataEditorExampleTest_2"))
        val tab = fragment.createNewEmptyTab(DataFormat.JSON, customTitle = "all-types.json")

        // Populate and verify all 12 DataNodeValueType variants visually
        val bytePath = fragment.addLeafToBranchInActiveTab("root", "byteVal", DataNodeValueType.BYTE, "42")
        val shortPath = fragment.addLeafToBranchInActiveTab("root", "shortVal", DataNodeValueType.SHORT, "1024")
        val intPath = fragment.addLeafToBranchInActiveTab("root", "intVal", DataNodeValueType.INT, "65536")
        val longPath = fragment.addLeafToBranchInActiveTab("root", "longVal", DataNodeValueType.LONG, "9000000000")
        val floatPath = fragment.addLeafToBranchInActiveTab("root", "floatVal", DataNodeValueType.FLOAT, "3.25")
        val doublePath = fragment.addLeafToBranchInActiveTab("root", "doubleVal", DataNodeValueType.DOUBLE, "2.718281828")
        val boolPath = fragment.addLeafToBranchInActiveTab("root", "boolVal", DataNodeValueType.BOOLEAN, "true")
        val nullPath = fragment.addLeafToBranchInActiveTab("root", "nullVal", DataNodeValueType.NULL, "null")
        val mapBranchPath = fragment.addBranchToBranchInActiveTab("root", "mapContainer", DataNodeValueType.MAP)
        val arrBranchPath = fragment.addBranchToBranchInActiveTab("root", "arrContainer", DataNodeValueType.ARRAY)
        fragment.addLeafToBranchInActiveTab(mapBranchPath!!, "entryKey", DataNodeValueType.STRING, "entryValue")
        fragment.addLeafToBranchInActiveTab(arrBranchPath!!, "", DataNodeValueType.STRING, "firstElement")

        assertEquals(DataNodeValueType.BYTE, tab.rootNode.findByPath(bytePath!!)?.valueType)
        assertEquals(DataNodeValueType.SHORT, tab.rootNode.findByPath(shortPath!!)?.valueType)
        assertEquals(DataNodeValueType.INT, tab.rootNode.findByPath(intPath!!)?.valueType)
        assertEquals(DataNodeValueType.LONG, tab.rootNode.findByPath(longPath!!)?.valueType)
        assertEquals(DataNodeValueType.FLOAT, tab.rootNode.findByPath(floatPath!!)?.valueType)
        assertEquals(DataNodeValueType.DOUBLE, tab.rootNode.findByPath(doublePath!!)?.valueType)
        assertEquals(DataNodeValueType.BOOLEAN, tab.rootNode.findByPath(boolPath!!)?.valueType)
        assertEquals(DataNodeValueType.NULL, tab.rootNode.findByPath(nullPath!!)?.valueType)
        assertEquals(DataNodeValueType.MAP, tab.rootNode.findByPath(mapBranchPath)?.valueType)
        assertEquals(DataNodeValueType.ARRAY, tab.rootNode.findByPath(arrBranchPath)?.valueType)

        // Test inline type switching on a leaf (INT -> DOUBLE -> BOOLEAN)
        assertTrue(fragment.changeNodeTypeInActiveTab(intPath, DataNodeValueType.DOUBLE))
        assertEquals(DataNodeValueType.DOUBLE, tab.rootNode.findByPath(intPath)?.valueType)

        // Load Game Save preset and round-trip across JSON, BSON, and BinaryPlist
        val sampleTab = fragment.loadSamplePreset("game", DataFormat.JSON)

        // 1. Encode as UTF-8 JSON bytes
        val jsonBytes = sampleTab.toFileBytes(DataFormat.JSON)
        assertEquals(DataFormat.JSON, DataDocumentCodec.detectFormat("save.json", jsonBytes))

        // 2. Convert and encode as BinaryJson (.bson)
        fragment.convertActiveTabFormat(DataFormat.BINARY_JSON_BSON)
        assertEquals(DataFormat.BINARY_JSON_BSON, sampleTab.format)
        assertTrue(sampleTab.title.endsWith(".bson"))
        val bsonBytes = sampleTab.toFileBytes(DataFormat.BINARY_JSON_BSON)
        assertTrue(bsonBytes.isNotEmpty())
        assertEquals(DataFormat.BINARY_JSON_BSON, DataDocumentCodec.detectFormat("save.bson", bsonBytes))

        // 3. Convert and encode as Apple BinaryPlist (bplist00 .plist)
        fragment.convertActiveTabFormat(DataFormat.BINARY_PLIST)
        assertEquals(DataFormat.BINARY_PLIST, sampleTab.format)
        assertTrue(sampleTab.title.endsWith(".plist"))
        val plistBytes = sampleTab.toFileBytes(DataFormat.BINARY_PLIST)
        assertTrue(BinaryPlist.isBinaryPlist(plistBytes))
        assertEquals(DataFormat.BINARY_PLIST, DataDocumentCodec.detectFormat("save.plist", plistBytes))

        // 4. Open BSON and BinaryPlist bytes into new tabs and verify identical structure
        val fromBsonTab = fragment.openBytesInNewTab(bsonBytes, "reloaded.bson")
        assertNotNull(fromBsonTab)
        assertEquals(DataFormat.BINARY_JSON_BSON, fromBsonTab.format)
        assertEquals("Citadel of Glass", fromBsonTab.rootNode.findByPath("root.chapterTitle")?.primitiveText)
        assertEquals("Kaelen", fromBsonTab.rootNode.findByPath("root.hero.name")?.primitiveText)
        assertEquals("18", fromBsonTab.rootNode.findByPath("root.hero.level")?.primitiveText)
        assertEquals("Astral Longsword", fromBsonTab.rootNode.findByPath("root.inventory[0].name")?.primitiveText)

        val fromPlistTab = fragment.openBytesInNewTab(plistBytes, "reloaded.plist")
        assertNotNull(fromPlistTab)
        assertEquals(DataFormat.BINARY_PLIST, fromPlistTab.format)
        assertEquals("Citadel of Glass", fromPlistTab.rootNode.findByPath("root.chapterTitle")?.primitiveText)
        assertEquals("Kaelen", fromPlistTab.rootNode.findByPath("root.hero.name")?.primitiveText)
        assertEquals("18", fromPlistTab.rootNode.findByPath("root.hero.level")?.primitiveText)
        assertEquals("5", fromPlistTab.rootNode.findByPath("root.inventory[1].quantity")?.primitiveText)

        assertEquals(
            fromBsonTab.toJsonString(pretty = false),
            fromPlistTab.toJsonString(pretty = false)
        )
    }

    @Test
    fun testMultiTabSwitchingNodeDuplicationDeletionAndPreferences() {
        val store = PreferenceStore.open("DataEditorExampleTest_3")
        store.clear()
        val fragment = DataEditorFragment(store)

        // Initial default tab is open
        assertEquals(1, fragment.openTabs.size)
        val firstTabId = fragment.openTabs.first().tabId

        // Open a second BSON tab and a third Binary Plist tab
        val secondTab = fragment.loadSamplePreset("manifest", DataFormat.BINARY_JSON_BSON)
        val thirdTab = fragment.createNewEmptyTab(DataFormat.BINARY_PLIST, customTitle = "settings.plist")
        assertEquals(3, fragment.openTabs.size)
        assertEquals(thirdTab.tabId, fragment.activeTabId)

        // Switch back to the second tab (manifest.bson)
        assertTrue(fragment.selectTab(secondTab.tabId))
        assertEquals(secondTab.tabId, fragment.activeTabId)
        assertEquals("BSON", fragment.activeFormatBadge.text)
        assertTrue(fragment.documentGridTree.visibleRowCount > 1)

        // Duplicate and then delete a node in the third tab via LazyGridTree row actions
        assertTrue(fragment.selectTab(thirdTab.tabId))
        val dupPath = fragment.duplicateNodeInActiveTab("root.name")
        assertEquals("root.name_copy", dupPath)
        assertNotNull(thirdTab.rootNode.findByPath("root.name_copy"))

        assertTrue(fragment.deleteNodeInActiveTab("root.name_copy"))
        assertNull(thirdTab.rootNode.findByPath("root.name_copy"))

        // Close second tab and verify remaining tabs
        assertTrue(fragment.closeTabById(secondTab.tabId))
        assertEquals(2, fragment.openTabs.size)
        assertTrue(fragment.selectTab(firstTabId))
        assertEquals(firstTabId, fragment.activeTabId)

        // Verify DataEditorPreferences round-trip persistence via PreferenceStore
        val savedPrefs = DataEditorPreferences(
            prettyPrintIndent = 4,
            showTypeBadges = true,
            showByteSizeMetrics = true,
            recentFileNames = listOf("profile.json", "manifest.bson", "settings.plist")
        )
        DataEditorPreferences.saveTo(store, savedPrefs)
        val loadedPrefs = DataEditorPreferences.loadFrom(store)
        assertEquals(4, loadedPrefs.prettyPrintIndent)
        assertEquals(listOf("profile.json", "manifest.bson", "settings.plist"), loadedPrefs.recentFileNames)
    }
}
