package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.action.Toolbar
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.lazy.LazyGridTree
import com.nexustools.genericui.component.lazy.LazyGridTreeColumn
import com.nexustools.genericui.component.lazy.LazyGridTreeNode
import com.nexustools.genericui.component.lazy.TreeNode
import com.nexustools.genericui.component.lazy.TreeView
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.system.StatusBar
import com.nexustools.genericui.component.tabs.CloseableTab
import com.nexustools.genericui.component.tabs.TabContainer
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.SpacingScale

/**
 * Visual multi-tab document editor [Fragment] for `.json` (JSON), `.bson` ([com.nexustools.genericui.util.BinaryJson]),
 * and `.plist` ([com.nexustools.genericui.util.BinaryPlist] `bplist00`) files.
 *
 * Uses [LazyGridTree] (`documentGridTree`) as its primary visual editor so every branch and leaf
 * across the document hierarchy is directly editable inline:
 * - **Each Leaf Row** (`STRING`, `BYTE`, `SHORT`, `INT`, `LONG`, `FLOAT`, `DOUBLE`, `BOOLEAN`, `NULL`)
 *   has an editable Key [TextField] (when inside an `OBJECT`/`MAP`), an editable Value [TextField]
 *   (or [CheckBox] switch for `BOOLEAN`), a 12-datatype [Select] dropdown, and action buttons to move
 *   up (`↑`), move down (`↓`), duplicate (`Dup`), or delete (`Del`).
 * - **Each Branch Row** (`OBJECT`, `MAP`, `ARRAY`) has a collapse/expand disclosure button (`▼`/`▶`),
 *   an editable Key [TextField] (when non-root and inside an `OBJECT`/`MAP`), a child count summary badge,
 *   a 12-datatype [Select] dropdown, and action buttons to create a new leaf (`+ Leaf`), create a new
 *   nested branch (`+ Branch`), move up (`↑`), move down (`↓`), duplicate (`Dup`), or delete (`Del`).
 */
class DataEditorFragment(
    val preferenceStore: PreferenceStore = PreferenceStore.open("BsonJsonPlistEditor")
) : Fragment() {

    private var preferences: DataEditorPreferences = DataEditorPreferences.loadFrom(preferenceStore)
    private var tabCounter: Int = 0

    private val openTabsMap = linkedMapOf<String, DataDocumentTabState>()
    val openTabs: List<DataDocumentTabState>
        get() = openTabsMap.values.toList()

    var activeTabId: String? = null
        private set

    val activeTabState: DataDocumentTabState?
        get() = activeTabId?.let { openTabsMap[it] } ?: openTabsMap.values.firstOrNull()

    // Top Action Toolbar Controls
    val topToolbar: Toolbar = Toolbar()
    val newJsonTabButton: Button = Button("+ JSON Tab", type = BootstrapType.PRIMARY, size = BootstrapSize.SM)
    val newBsonTabButton: Button = Button("+ BSON Tab", type = BootstrapType.INFO, size = BootstrapSize.SM)
    val newPlistTabButton: Button = Button("+ Plist Tab", type = BootstrapType.SUCCESS, size = BootstrapSize.SM)
    val openFileButton: Button = Button("Open File...", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val saveFileButton: Button = Button("Save As...", type = BootstrapType.PRIMARY, size = BootstrapSize.SM, outline = true)
    val loadUserProfileSampleButton: Button = Button("Sample: Profile", type = BootstrapType.DARK, size = BootstrapSize.SM, outline = true)
    val loadManifestSampleButton: Button = Button("Sample: Manifest", type = BootstrapType.DARK, size = BootstrapSize.SM, outline = true)
    val loadGameSaveSampleButton: Button = Button("Sample: Game Save", type = BootstrapType.DARK, size = BootstrapSize.SM, outline = true)

    // Multi-Tab Document Bar
    val documentTabContainer: TabContainer = TabContainer(fadeTransition = true)

    // Filter & Tree Controls Bar
    val treeFilterField: TextField = TextField(value = "", placeholder = "Filter nodes by key, value, or path...")
    val clearFilterButton: Button = Button("Clear", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val expandAllButton: Button = Button("Expand All", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val collapseAllButton: Button = Button("Collapse All", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val addRootLeafButton: Button = Button("+ Root Leaf", type = BootstrapType.SUCCESS, size = BootstrapSize.SM)
    val addRootBranchButton: Button = Button("+ Root Branch", type = BootstrapType.PRIMARY, size = BootstrapSize.SM, outline = true)
    val treeSummaryLabel: Label = Label("0 nodes")

    // Primary Visual Editor: Multi-Column Collapsible LazyGridTree
    val documentGridTree: LazyGridTree<String> = LazyGridTree(
        treeColumns = listOf(
            LazyGridTreeColumn(
                id = "key",
                headerText = "Key / Hierarchy",
                widthWeight = 1.6f,
                minWidthPx = 220,
                isTreeColumn = true
            ),
            LazyGridTreeColumn(
                id = "value",
                headerText = "Value",
                widthWeight = 1.8f,
                minWidthPx = 220,
                isTreeColumn = false
            ),
            LazyGridTreeColumn(
                id = "type",
                headerText = "Type (12 DataTypes)",
                widthWeight = 1.1f,
                minWidthPx = 150,
                isTreeColumn = false
            ),
            LazyGridTreeColumn(
                id = "actions",
                headerText = "Node Actions",
                widthWeight = 1.7f,
                minWidthPx = 250,
                isTreeColumn = false
            )
        ),
        estimatedRowHeightPx = 38f,
        overscanRows = 20
    )

    // Inline row widget lookup maps (indexed by node path in the active tab)
    private val _rowKeyFieldsByPath = linkedMapOf<String, TextField>()
    val rowKeyFieldsByPath: Map<String, TextField> get() = _rowKeyFieldsByPath

    private val _rowValueFieldsByPath = linkedMapOf<String, TextField>()
    val rowValueFieldsByPath: Map<String, TextField> get() = _rowValueFieldsByPath

    private val _rowBoolCheckBoxesByPath = linkedMapOf<String, CheckBox>()
    val rowBoolCheckBoxesByPath: Map<String, CheckBox> get() = _rowBoolCheckBoxesByPath

    private val _rowTypeSelectsByPath = linkedMapOf<String, Select>()
    val rowTypeSelectsByPath: Map<String, Select> get() = _rowTypeSelectsByPath

    private val _rowMoveUpButtonsByPath = linkedMapOf<String, Button>()
    val rowMoveUpButtonsByPath: Map<String, Button> get() = _rowMoveUpButtonsByPath

    private val _rowMoveDownButtonsByPath = linkedMapOf<String, Button>()
    val rowMoveDownButtonsByPath: Map<String, Button> get() = _rowMoveDownButtonsByPath

    private val _rowAddLeafButtonsByPath = linkedMapOf<String, Button>()
    val rowAddLeafButtonsByPath: Map<String, Button> get() = _rowAddLeafButtonsByPath

    private val _rowAddBranchButtonsByPath = linkedMapOf<String, Button>()
    val rowAddBranchButtonsByPath: Map<String, Button> get() = _rowAddBranchButtonsByPath

    private val _rowDuplicateButtonsByPath = linkedMapOf<String, Button>()
    val rowDuplicateButtonsByPath: Map<String, Button> get() = _rowDuplicateButtonsByPath

    private val _rowDeleteButtonsByPath = linkedMapOf<String, Button>()
    val rowDeleteButtonsByPath: Map<String, Button> get() = _rowDeleteButtonsByPath

    // Synchronized Selected-Node Inspector Controls (for programmatic & detail access)
    val documentTreeView: TreeView<String> = TreeView(estimatedRowHeightPx = 30f)
    val selectedPathBadge: Badge = Badge("root", type = BootstrapType.INFO, pill = true)
    val selectedTypeBadge: Badge = Badge("OBJ", type = BootstrapType.PRIMARY)
    val nodeKeyField: TextField = TextField(value = "root", placeholder = "Property key name")
    val nodeTypeSelect: Select = Select(
        options = DataNodeValueType.entries.map { SelectOption(it.name, "${it.displayName} [${it.badgeLabel}]") },
        selectedValue = DataNodeValueType.OBJECT.name
    )
    val nodeValueField: TextField = TextField(value = "", placeholder = "Primitive value")
    val nodeBooleanCheckBox: CheckBox = CheckBox(label = "Boolean Value (true / false)", checked = false, switchStyle = true)
    val applyNodeChangesButton: Button = Button("Apply Node Changes", type = BootstrapType.PRIMARY, size = BootstrapSize.SM)
    val selectParentNodeButton: Button = Button("Select Parent", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val duplicateNodeButton: Button = Button("Duplicate Node", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true)
    val deleteNodeButton: Button = Button("Delete Node", type = BootstrapType.DANGER, size = BootstrapSize.SM, outline = true)

    val addChildTargetLabel: Label = Label("Target Container: root")
    val newChildKeyField: TextField = TextField(value = "newField", placeholder = "New field key (for Object/Map)")
    val newChildTypeSelect: Select = Select(
        options = DataNodeValueType.entries.map { SelectOption(it.name, "${it.displayName} [${it.badgeLabel}]") },
        selectedValue = DataNodeValueType.STRING.name
    )
    val newChildValueField: TextField = TextField(value = "", placeholder = "Initial value")
    val newChildBoolCheckBox: CheckBox = CheckBox(label = "Initial Boolean = true", checked = false, switchStyle = true)
    val addChildNodeButton: Button = Button("+ Add Child Node", type = BootstrapType.SUCCESS, size = BootstrapSize.SM)

    val containerChildrenTable: Table = Table()

    // Visual Document Format & Byte-Size Comparison Card
    val activeFormatBadge: Badge = Badge("JSON", type = BootstrapType.PRIMARY, pill = true)
    val formatSelector: Select = Select(
        options = DataFormat.entries.map { SelectOption(it.name, "${it.displayName} (${it.defaultExtension})") },
        selectedValue = DataFormat.JSON.name
    )
    val convertToJsonButton: Button = Button("Set JSON (.json)", type = BootstrapType.PRIMARY, size = BootstrapSize.SM, outline = true)
    val convertToBsonButton: Button = Button("Set BSON (.bson)", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true)
    val convertToPlistButton: Button = Button("Set Plist (.plist)", type = BootstrapType.SUCCESS, size = BootstrapSize.SM, outline = true)
    val sizeComparisonSummaryLabel: Label = Label("JSON: 0 B | BinaryJson: 0 B | BinaryPlist: 0 B")
    val jsonSizeBar: ProgressBar = ProgressBar(progress = 0.5f, type = BootstrapType.PRIMARY)
    val bsonSizeBar: ProgressBar = ProgressBar(progress = 0.5f, type = BootstrapType.INFO)
    val plistSizeBar: ProgressBar = ProgressBar(progress = 0.5f, type = BootstrapType.SUCCESS)

    // Bottom Status Bar
    val statusBar: StatusBar = StatusBar()
    val statusMessageLabel: Label = Label("Ready")

    init {
        addClass("data-editor-root")
        documentGridTree.onViewportChanged(0f, 1000f)
        buildVisualWorkspaceLayout()
        wireVisualEvents()
        ensureInitialDefaultTab()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        if (openTabsMap.isEmpty()) {
            ensureInitialDefaultTab()
        }
        refreshActiveTabWorkspace()
    }

    private fun ensureInitialDefaultTab() {
        if (openTabsMap.isNotEmpty()) return
        val initialTab = DataEditorSamplePresets.createSampleTabState(
            tabId = nextTabId(),
            presetName = "profile",
            format = DataFormat.JSON
        )
        registerAndSelectTab(initialTab, statusNote = "Loaded sample User Profile (${initialTab.format.displayName}).")
    }

    private fun buildVisualWorkspaceLayout() {
        val rootStack = VStack(gap = SpacingScale.S2)

        // 1. Top Toolbar
        val toolbarRow = HStack(gap = SpacingScale.S1)
        toolbarRow.addClass("editor-toolbar")
        toolbarRow.addChild(newJsonTabButton)
        toolbarRow.addChild(newBsonTabButton)
        toolbarRow.addChild(newPlistTabButton)
        toolbarRow.addChild(openFileButton)
        toolbarRow.addChild(saveFileButton)
        toolbarRow.addChild(loadUserProfileSampleButton)
        toolbarRow.addChild(loadManifestSampleButton)
        toolbarRow.addChild(loadGameSaveSampleButton)
        topToolbar.addChild(toolbarRow)
        rootStack.addChild(topToolbar)

        // 2. Multi-Tab Document Bar
        rootStack.addChild(documentTabContainer)

        // 3. Filter, Tree Controls & Quick Root Actions Bar
        val filterAndActionsRow = HStack(gap = SpacingScale.S1)
        filterAndActionsRow.addClass("grid-tree-toolbar")
        filterAndActionsRow.addChild(treeFilterField)
        filterAndActionsRow.addChild(clearFilterButton)
        filterAndActionsRow.addChild(expandAllButton)
        filterAndActionsRow.addChild(collapseAllButton)
        filterAndActionsRow.addChild(addRootLeafButton)
        filterAndActionsRow.addChild(addRootBranchButton)
        filterAndActionsRow.addChild(treeSummaryLabel)
        rootStack.addChild(filterAndActionsRow)

        // 4. Primary Visual LazyGridTree Editor Card
        val gridTreeCard = Card("Visual Structured Document Tree-Grid (LazyGridTree)")
        gridTreeCard.addClass("node-tree-panel")
        gridTreeCard.addChild(documentGridTree)
        rootStack.addChild(gridTreeCard)

        // 5. Visual Format & Storage Comparison Card
        val formatCard = Card("Document Format & Binary Size Comparison")
        formatCard.addClass("format-comparison-card")
        val formatStack = VStack(gap = SpacingScale.S2)

        val formatHeaderRow = HStack(gap = SpacingScale.S2)
        formatHeaderRow.addChild(Label("Active Document Format:"))
        formatHeaderRow.addChild(activeFormatBadge)
        formatHeaderRow.addChild(formatSelector)
        formatHeaderRow.addChild(convertToJsonButton)
        formatHeaderRow.addChild(convertToBsonButton)
        formatHeaderRow.addChild(convertToPlistButton)
        formatStack.addChild(formatHeaderRow)

        formatStack.addChild(sizeComparisonSummaryLabel)
        val barsRow = HStack(gap = SpacingScale.S2)
        barsRow.addChild(jsonSizeBar)
        barsRow.addChild(bsonSizeBar)
        barsRow.addChild(plistSizeBar)
        formatStack.addChild(barsRow)

        formatCard.addChild(formatStack)
        rootStack.addChild(formatCard)

        // Configure auxiliary children table columns
        containerChildrenTable.setColumns(
            listOf(
                TableColumn(id = "key", headerText = "Key / Index"),
                TableColumn(id = "type", headerText = "Type"),
                TableColumn(id = "value", headerText = "Value / Summary"),
                TableColumn(id = "path", headerText = "Node Path")
            )
        )

        // 6. Status Bar
        statusBar.addClass("status-bar")
        val statusRow = HStack(gap = SpacingScale.S2)
        statusRow.addChild(Label("Selected:"))
        statusRow.addChild(selectedPathBadge)
        statusRow.addChild(selectedTypeBadge)
        statusRow.addChild(statusMessageLabel)
        statusBar.addChild(statusRow)
        rootStack.addChild(statusBar)

        addChild(rootStack)
    }

    private fun wireVisualEvents() {
        newJsonTabButton.setOnClick {
            createNewEmptyTab(DataFormat.JSON)
        }
        newBsonTabButton.setOnClick {
            createNewEmptyTab(DataFormat.BINARY_JSON_BSON)
        }
        newPlistTabButton.setOnClick {
            createNewEmptyTab(DataFormat.BINARY_PLIST)
        }
        openFileButton.setOnClick {
            openDocumentFromDisk()
        }
        saveFileButton.setOnClick {
            saveActiveDocumentToDisk()
        }
        loadUserProfileSampleButton.setOnClick {
            loadSamplePreset("profile", DataFormat.JSON)
        }
        loadManifestSampleButton.setOnClick {
            loadSamplePreset("manifest", DataFormat.BINARY_JSON_BSON)
        }
        loadGameSaveSampleButton.setOnClick {
            loadSamplePreset("game", DataFormat.BINARY_PLIST)
        }

        documentTabContainer.onTabEntrySelected = { entry ->
            if (entry.id in openTabsMap && activeTabId != entry.id) {
                activeTabId = entry.id
                refreshActiveTabWorkspace()
            }
        }
        documentTabContainer.onTabCloseRequested = { entry ->
            handleTabCloseRequested(entry.id)
        }

        treeFilterField.onValueChanged = { query ->
            documentGridTree.filterQuery = query
            updateTreeSummaryCount()
        }
        clearFilterButton.setOnClick {
            treeFilterField.value = ""
            documentGridTree.filterQuery = ""
            updateTreeSummaryCount()
        }
        expandAllButton.setOnClick {
            val tab = activeTabState ?: return@setOnClick
            tab.seedExpandedBranchPaths()
            documentGridTree.expandAll()
            updateTreeSummaryCount()
            setStatus("Expanded all branch nodes.")
        }
        collapseAllButton.setOnClick {
            val tab = activeTabState ?: return@setOnClick
            tab.expandedBranchPaths.clear()
            tab.expandedBranchPaths.add("root")
            documentGridTree.collapseAll(keepRootsExpanded = true)
            updateTreeSummaryCount()
            setStatus("Collapsed nested branch nodes.")
        }
        addRootLeafButton.setOnClick {
            addLeafToBranchInActiveTab("root")
        }
        addRootBranchButton.setOnClick {
            addBranchToBranchInActiveTab("root")
        }

        documentGridTree.onNodeExpansionChanged = { node, expanded ->
            val tab = activeTabState
            if (tab != null) {
                if (expanded) {
                    tab.expandedBranchPaths.add(node.id)
                } else {
                    tab.expandedBranchPaths.remove(node.id)
                }
            }
            updateTreeSummaryCount()
        }
        documentGridTree.onNodeSelected = { gridNode ->
            val path = gridNode.data ?: gridNode.id
            selectNodeInActiveTab(path)
        }

        documentTreeView.onNodeSelected = { treeNode ->
            val path = treeNode.data ?: treeNode.id
            selectNodeInActiveTab(path)
        }

        nodeTypeSelect.onSelectionChanged = { selectedTypeName ->
            val chosenType = DataNodeValueType.fromNameOrDefault(selectedTypeName)
            updateInspectorInputVisibility(chosenType)
        }
        nodeBooleanCheckBox.onCheckedChanged = { checked ->
            nodeValueField.value = checked.toString()
        }
        applyNodeChangesButton.setOnClick {
            applyInspectorEditsToSelectedNode()
        }
        selectParentNodeButton.setOnClick {
            val tab = activeTabState ?: return@setOnClick
            val parentPath = EditableDataNode.parentPathOf(tab.selectedNodePath) ?: "root"
            selectNodeInActiveTab(parentPath)
        }
        duplicateNodeButton.setOnClick {
            duplicateSelectedNodeInActiveTab()
        }
        deleteNodeButton.setOnClick {
            deleteSelectedNodeInActiveTab()
        }

        newChildTypeSelect.onSelectionChanged = { selectedTypeName ->
            val chosenType = DataNodeValueType.fromNameOrDefault(selectedTypeName)
            updateNewChildInputVisibility(chosenType)
        }
        newChildBoolCheckBox.onCheckedChanged = { checked ->
            newChildValueField.value = checked.toString()
        }
        addChildNodeButton.setOnClick {
            addChildNodeInActiveTab()
        }

        formatSelector.onSelectionChanged = { selectedFormatName ->
            val newFormat = DataFormat.fromSelectValue(selectedFormatName)
            convertActiveTabFormat(newFormat)
        }
        convertToJsonButton.setOnClick {
            convertActiveTabFormat(DataFormat.JSON)
        }
        convertToBsonButton.setOnClick {
            convertActiveTabFormat(DataFormat.BINARY_JSON_BSON)
        }
        convertToPlistButton.setOnClick {
            convertActiveTabFormat(DataFormat.BINARY_PLIST)
        }
    }

    // --- Public Multi-Tab & Inline LazyGridTree Operations ---

    /**
     * Creates and activates a new visual document tab with the specified [format].
     */
    fun createNewEmptyTab(
        format: DataFormat = DataFormat.JSON,
        customTitle: String? = null
    ): DataDocumentTabState {
        val tabId = nextTabId()
        val indexNumber = openTabsMap.size + 1
        val title = customTitle ?: "document-$indexNumber${format.defaultExtension}"
        val root = EditableDataNode(
            keyOrIndex = "root",
            valueType = DataNodeValueType.OBJECT
        )
        root.addChildNode("name", DataNodeValueType.STRING, "Untitled Document $indexNumber")
        root.addChildNode("version", DataNodeValueType.INT, "1")
        root.addChildNode("active", DataNodeValueType.BOOLEAN, "true")

        val state = DataDocumentTabState(
            tabId = tabId,
            title = title,
            format = format,
            rootNode = root,
            selectedNodePath = "root",
            isDirty = false
        )
        registerAndSelectTab(state, statusNote = "Created new ${format.displayName} tab '${state.title}'.")
        return state
    }

    /**
     * Loads one of the built-in sample presets (`"profile"`, `"manifest"`, `"game"`) into a new visual tab.
     */
    fun loadSamplePreset(presetName: String, format: DataFormat = DataFormat.JSON): DataDocumentTabState {
        val state = DataEditorSamplePresets.createSampleTabState(
            tabId = nextTabId(),
            presetName = presetName,
            format = format
        )
        registerAndSelectTab(state, statusNote = "Opened sample preset '${state.title}' as ${format.displayName}.")
        return state
    }

    /**
     * Programmatically loads a UTF-8 JSON string into a new visual editor tab.
     * Returns the created [DataDocumentTabState], or `null` if [jsonText] is invalid.
     */
    fun loadJsonStringIntoNewTab(
        jsonText: String,
        title: String = "imported.json",
        format: DataFormat = DataFormat.JSON
    ): DataDocumentTabState? {
        val tabId = nextTabId()
        val state = DataDocumentTabState(
            tabId = tabId,
            title = title,
            format = format
        )
        if (!state.loadFromJsonString(jsonText, markDirty = false)) {
            setStatus("Invalid JSON string: ${state.lastJsonError}")
            return null
        }
        registerAndSelectTab(state, statusNote = "Loaded JSON document into tab '${state.title}'.")
        return state
    }

    /**
     * Programmatically loads a UTF-8 JSON string into the currently active visual editor tab.
     */
    fun loadJsonStringIntoActiveTab(jsonText: String): Boolean {
        val tab = activeTabState ?: return false
        val ok = tab.loadFromJsonString(jsonText, markDirty = true)
        if (ok) {
            syncTabHeader(tab)
            refreshActiveTabWorkspace()
            setStatus("Updated active tab '${tab.title}' from JSON string.")
        } else {
            setStatus("Invalid JSON string: ${tab.lastJsonError}")
        }
        return ok
    }

    /**
     * Programmatically provides the UTF-8 JSON string for the currently active tab.
     */
    fun provideActiveTabJsonString(pretty: Boolean = true): String {
        val tab = activeTabState ?: return "{}"
        return tab.toJsonString(pretty = pretty, indentSpaces = preferences.prettyPrintIndent)
    }

    /**
     * Programmatically provides the UTF-8 JSON string for the tab matching [tabId], or `null` if not found.
     */
    fun provideTabJsonString(tabId: String, pretty: Boolean = true): String? {
        val tab = openTabsMap[tabId] ?: return null
        return tab.toJsonString(pretty = pretty, indentSpaces = preferences.prettyPrintIndent)
    }

    /**
     * Programmatically loads raw bytes (`.json`, `.bson`, or `.plist` `bplist00`) into a new visual tab.
     */
    fun openBytesInNewTab(
        bytes: ByteArray,
        fileName: String,
        explicitFormat: DataFormat? = null
    ): DataDocumentTabState? {
        val state = DataDocumentTabState(
            tabId = nextTabId(),
            title = fileName
        )
        if (!state.loadFromBytes(bytes, explicitFormat = explicitFormat, fileNameHint = fileName, markDirty = false)) {
            setStatus("Failed to load '$fileName': ${state.lastJsonError}")
            return null
        }
        registerAndSelectTab(state, statusNote = "Opened '$fileName' (${state.format.displayName}).")
        return state
    }

    /**
     * Switches the active visual tab by [tabId].
     */
    fun selectTab(tabId: String): Boolean {
        if (tabId !in openTabsMap) return false
        activeTabId = tabId
        documentTabContainer.setActiveTab(tabId)
        refreshActiveTabWorkspace()
        return true
    }

    /**
     * Closes the tab with [tabId]. If all tabs are closed, a fresh empty JSON tab is created automatically.
     */
    fun closeTabById(tabId: String): Boolean {
        if (tabId !in openTabsMap) return false
        documentTabContainer.closeTab(tabId)
        return tabId !in openTabsMap
    }

    /**
     * Selects a node by path (e.g. `"root.user.name"` or `"root.roles[0]"`) in the active tab.
     */
    fun selectNodeInActiveTab(path: String): Boolean {
        val tab = activeTabState ?: return false
        val found = tab.rootNode.findByPath(path) ?: return false
        tab.selectedNodePath = path
        refreshInspectorForSelectedNode(tab, found)
        refreshChildrenTable(tab, found)
        documentTreeView.selectedNodeId = path
        documentGridTree.selectedNodeId = path
        setStatus("Selected node '$path' (${found.valueType.displayName}).")
        return true
    }

    /**
     * Renames the key of the node at [path] in the active tab and returns the new path.
     */
    fun renameNodeInActiveTab(path: String, newKey: String, rebuildTree: Boolean = true): String? {
        val tab = activeTabState ?: return null
        val updatedPath = tab.renameNodeAtPath(path, newKey) ?: return null
        syncTabHeader(tab)
        if (rebuildTree) {
            refreshActiveTabWorkspace()
        } else {
            val selected = tab.selectedNode()
            refreshInspectorForSelectedNode(tab, selected)
            refreshFormatComparisonCard(tab)
        }
        setStatus("Renamed '$path' to '$updatedPath'.")
        return updatedPath
    }

    /**
     * Updates the primitive value of the leaf node at [path] in the active tab.
     */
    fun updateLeafValueInActiveTab(path: String, newValue: String, rebuildTree: Boolean = false): Boolean {
        val tab = activeTabState ?: return false
        val ok = tab.updateNodeValueAtPath(path, newValue)
        if (ok) {
            syncTabHeader(tab)
            if (rebuildTree) {
                refreshActiveTabWorkspace()
            } else {
                val selected = tab.selectedNode()
                refreshInspectorForSelectedNode(tab, selected)
                refreshChildrenTable(tab, selected)
                refreshFormatComparisonCard(tab)
            }
            setStatus("Updated value of '$path' to '$newValue'.")
        }
        return ok
    }

    /**
     * Changes the [DataNodeValueType] of the node at [path] in the active tab (supports all 12 datatypes).
     */
    fun changeNodeTypeInActiveTab(
        path: String,
        newType: DataNodeValueType,
        initialPrimitiveText: String? = null
    ): Boolean {
        val tab = activeTabState ?: return false
        val ok = tab.updateNodeTypeAtPath(path, newType, initialPrimitiveText)
        if (ok) {
            refreshActiveTabWorkspace()
            setStatus("Changed type of '$path' to ${newType.displayName}.")
        }
        return ok
    }

    /**
     * Moves the branch or leaf node at [path] up one position inside its parent container.
     */
    fun moveNodeUpInActiveTab(path: String): String? {
        val tab = activeTabState ?: return null
        val newPath = tab.moveNodeUpAtPath(path) ?: return null
        refreshActiveTabWorkspace()
        setStatus("Moved node up to '$newPath'.")
        return newPath
    }

    /**
     * Moves the branch or leaf node at [path] down one position inside its parent container.
     */
    fun moveNodeDownInActiveTab(path: String): String? {
        val tab = activeTabState ?: return null
        val newPath = tab.moveNodeDownAtPath(path) ?: return null
        refreshActiveTabWorkspace()
        setStatus("Moved node down to '$newPath'.")
        return newPath
    }

    /**
     * Adds a new leaf child to the branch at [branchPath] in the active tab and returns the new leaf's path.
     */
    fun addLeafToBranchInActiveTab(
        branchPath: String = activeTabState?.selectedNodePath ?: "root",
        key: String = "newField",
        type: DataNodeValueType = DataNodeValueType.STRING,
        value: String = type.defaultPrimitiveText()
    ): String? {
        val tab = activeTabState ?: return null
        val targetBranchPath = resolveBranchContainerPath(tab, branchPath)
        val newPath = tab.addLeafToBranchAtPath(targetBranchPath, key, type, value)
        refreshActiveTabWorkspace()
        setStatus("Added leaf '$newPath' (${type.displayName}) to '$targetBranchPath'.")
        return newPath
    }

    /**
     * Adds a new nested branch (`OBJECT`, `MAP`, or `ARRAY`) to the branch at [branchPath] in the active tab.
     */
    fun addBranchToBranchInActiveTab(
        branchPath: String = activeTabState?.selectedNodePath ?: "root",
        key: String = "newObject",
        branchType: DataNodeValueType = DataNodeValueType.OBJECT
    ): String? {
        val tab = activeTabState ?: return null
        val targetBranchPath = resolveBranchContainerPath(tab, branchPath)
        val newPath = tab.addBranchToBranchAtPath(targetBranchPath, key, branchType)
        refreshActiveTabWorkspace()
        setStatus("Added branch '$newPath' (${branchType.displayName}) to '$targetBranchPath'.")
        return newPath
    }

    /**
     * Duplicates the branch or leaf node at [path] in the active tab.
     */
    fun duplicateNodeInActiveTab(path: String): String? {
        val tab = activeTabState ?: return null
        val dupPath = tab.duplicateNodeAtPath(path) ?: return null
        refreshActiveTabWorkspace()
        setStatus("Duplicated '$path' to '$dupPath'.")
        return dupPath
    }

    /**
     * Deletes the branch or leaf node at [path] in the active tab (cannot delete `"root"`).
     */
    fun deleteNodeInActiveTab(path: String): Boolean {
        val tab = activeTabState ?: return false
        val removed = tab.deleteNodeAtPath(path)
        if (removed) {
            refreshActiveTabWorkspace()
            setStatus("Deleted node '$path'.")
        } else {
            setStatus("Root node cannot be deleted.")
        }
        return removed
    }

    /**
     * Converts the active tab's format (`JSON`, `BINARY_JSON_BSON`, or `BINARY_PLIST`).
     */
    fun convertActiveTabFormat(newFormat: DataFormat) {
        val tab = activeTabState ?: return
        if (tab.format == newFormat) {
            refreshFormatComparisonCard(tab)
            return
        }
        tab.convertFormat(newFormat)
        syncTabHeader(tab)
        refreshFormatComparisonCard(tab)
        setStatus("Converted '${tab.title}' to ${newFormat.displayName}.")
    }

    /**
     * Opens the native multi-format file browser dialog (`*.json`, `*.bson`, `*.plist`).
     */
    fun openDocumentFromDisk() {
        DataEditorFileIoHelper.openDocumentWithDialog(
            ownerWindow = attachedWindow,
            nextTabId = nextTabId(),
            onLoaded = { loadedTab, selectedFile ->
                preferences = preferences.withRecentFile(selectedFile.name)
                DataEditorPreferences.saveTo(preferenceStore, preferences)
                registerAndSelectTab(
                    loadedTab,
                    statusNote = "Opened '${selectedFile.name}' (${loadedTab.format.displayName})."
                )
            },
            onError = { errMsg ->
                setStatus(errMsg)
                alert(errMsg, title = "Open Document Error")
            }
        )
    }

    /**
     * Saves the active tab using the native file save dialog in [targetFormat].
     */
    fun saveActiveDocumentToDisk(targetFormat: DataFormat? = null) {
        val tab = activeTabState ?: return
        val fmt = targetFormat ?: tab.format
        DataEditorFileIoHelper.saveDocumentWithDialog(
            ownerWindow = attachedWindow,
            tabState = tab,
            targetFormat = fmt,
            prettyJsonIndent = preferences.prettyPrintIndent,
            onSaved = { savedFile, writtenBytes ->
                preferences = preferences.withRecentFile(savedFile.name)
                DataEditorPreferences.saveTo(preferenceStore, preferences)
                syncTabHeader(tab)
                refreshFormatComparisonCard(tab)
                setStatus("Saved '${savedFile.name}' (${writtenBytes.size} bytes, ${tab.format.displayName}).")
            },
            onError = { errMsg ->
                setStatus(errMsg)
                alert(errMsg, title = "Save Document Error")
            }
        )
    }

    // --- Internal Visual Workspace Synchronization ---

    private fun resolveBranchContainerPath(tab: DataDocumentTabState, candidatePath: String): String {
        val normalized = candidatePath.trim().ifEmpty { "root" }
        val node = tab.rootNode.findByPath(normalized)
        return when {
            node == null -> "root"
            node.isContainer -> normalized
            else -> EditableDataNode.parentPathOf(normalized) ?: "root"
        }
    }

    private fun registerAndSelectTab(state: DataDocumentTabState, statusNote: String) {
        openTabsMap[state.tabId] = state
        activeTabId = state.tabId

        val tabPlaceholderContent = HStack()
        val tabHeader = CloseableTab(
            tabId = state.tabId,
            title = state.displayTabTitle(),
            isCloseable = true,
            isDirty = state.isDirty,
            onCloseRequested = {
                handleTabCloseRequested(state.tabId)
            }
        )
        documentTabContainer.addTab(tabHeader, tabPlaceholderContent)
        documentTabContainer.setActiveTab(state.tabId)
        refreshActiveTabWorkspace()
        setStatus(statusNote)
    }

    private fun handleTabCloseRequested(tabId: String): Boolean {
        val removed = openTabsMap.remove(tabId) ?: return false
        documentTabContainer.removeTab(tabId)
        if (openTabsMap.isEmpty()) {
            createNewEmptyTab(DataFormat.JSON)
        } else if (activeTabId == tabId) {
            activeTabId = openTabsMap.keys.lastOrNull()
            activeTabId?.let { documentTabContainer.setActiveTab(it) }
            refreshActiveTabWorkspace()
        }
        setStatus("Closed tab '${removed.title}'.")
        return true
    }

    private fun syncTabHeader(tab: DataDocumentTabState) {
        val tabComponent = documentTabContainer.getTab(tab.tabId)
        if (tabComponent != null) {
            tabComponent.title = tab.displayTabTitle()
            tabComponent.isDirty = tab.isDirty
        }
    }

    fun refreshActiveTabWorkspace() {
        val tab = activeTabState ?: return
        syncTabHeader(tab)
        refreshTreePane()
        val selected = tab.selectedNode()
        refreshInspectorForSelectedNode(tab, selected)
        refreshChildrenTable(tab, selected)
        refreshFormatComparisonCard(tab)
    }

    private fun refreshTreePane() {
        val tab = activeTabState ?: return

        _rowKeyFieldsByPath.clear()
        _rowValueFieldsByPath.clear()
        _rowBoolCheckBoxesByPath.clear()
        _rowTypeSelectsByPath.clear()
        _rowMoveUpButtonsByPath.clear()
        _rowMoveDownButtonsByPath.clear()
        _rowAddLeafButtonsByPath.clear()
        _rowAddBranchButtonsByPath.clear()
        _rowDuplicateButtonsByPath.clear()
        _rowDeleteButtonsByPath.clear()

        // 1. Build LazyGridTree hierarchy with inline editable cells for every branch and leaf
        val rootGridNode = buildLazyGridTreeNode(
            tab = tab,
            node = tab.rootNode,
            path = "root",
            parentNode = null
        )
        documentGridTree.filterQuery = treeFilterField.value.trim()
        documentGridTree.selectedNodeId = tab.selectedNodePath
        documentGridTree.setRootNodes(listOf(rootGridNode))

        // 2. Keep auxiliary TreeView synchronized
        val rootTreeNode = buildTreeViewNode(tab.rootNode, "root")
        documentTreeView.setRootNodes(listOf(rootTreeNode))
        documentTreeView.selectedNodeId = tab.selectedNodePath

        updateTreeSummaryCount()
    }

    private fun updateTreeSummaryCount() {
        val tab = activeTabState ?: return
        val totalCount = tab.rootNode.flattenWithPaths("root").size
        val visibleCount = documentGridTree.visibleRowCount
        treeSummaryLabel.text = "$visibleCount / $totalCount nodes"
    }

    private fun buildLazyGridTreeNode(
        tab: DataDocumentTabState,
        node: EditableDataNode,
        path: String,
        parentNode: EditableDataNode?
    ): LazyGridTreeNode<String> {
        val isBranch = node.isContainer
        val isRoot = (path == "root")
        val isExpanded = isRoot || (path in tab.expandedBranchPaths)
        val canRenameKey = !isRoot && parentNode != null &&
            (parentNode.valueType == DataNodeValueType.OBJECT || parentNode.valueType == DataNodeValueType.MAP)
        val canMoveUp = tab.rootNode.canMoveUpByPath(path)
        val canMoveDown = tab.rootNode.canMoveDownByPath(path)

        val gridNode = LazyGridTreeNode(
            id = path,
            keyText = node.keyOrIndex,
            data = path,
            isBranch = isBranch,
            isExpanded = isExpanded,
            icon = node.valueType.badgeLabel
        )

        // --- Column 0: Key / Hierarchy (Disclosure button is automatically rendered by LazyGridTree) ---
        val col0Row = HStack(gap = SpacingScale.S1).apply {
            addClass("grid-col-key")
        }
        val typeBadge = Badge(
            text = node.valueType.badgeLabel,
            type = badgeTypeForValueType(node.valueType)
        )
        col0Row.addChild(typeBadge)

        if (canRenameKey) {
            var trackedPath = path
            val keyField = TextField(
                value = node.keyOrIndex,
                placeholder = "key"
            ).apply {
                id = "grid-key-$path"
                addClass("grid-key-input")
                tooltipText = "Edit key for '$path'"
                onValueChanged = { updatedKey ->
                    val renamed = renameNodeInActiveTab(trackedPath, updatedKey, rebuildTree = false)
                    if (renamed != null) {
                        trackedPath = renamed
                    }
                }
                onSubmit = { submittedKey ->
                    val renamed = renameNodeInActiveTab(trackedPath, submittedKey, rebuildTree = true)
                    if (renamed != null) {
                        trackedPath = renamed
                    }
                }
            }
            _rowKeyFieldsByPath[path] = keyField
            col0Row.addChild(keyField)
        } else {
            val keyBadge = Badge(
                text = node.keyOrIndex,
                type = if (isRoot) BootstrapType.PRIMARY else BootstrapType.SECONDARY,
                pill = true
            ).apply {
                id = "grid-key-badge-$path"
            }
            val selectBtn = Button(
                text = if (isRoot) "root" else node.keyOrIndex,
                type = if (tab.selectedNodePath == path) BootstrapType.PRIMARY else BootstrapType.SECONDARY,
                size = BootstrapSize.SM,
                outline = (tab.selectedNodePath != path)
            ).apply {
                id = "grid-select-$path"
                setOnClick {
                    selectNodeInActiveTab(path)
                }
            }
            col0Row.addChild(keyBadge)
            col0Row.addChild(selectBtn)
        }
        gridNode.setComponentCell(0, col0Row, tooltipText = "Path: $path")

        // --- Column 1: Value (Editable TextField / Boolean Switch for Leaves, Summary Badge for Branches) ---
        val col1Row = HStack(gap = SpacingScale.S1).apply {
            addClass("grid-col-value")
        }
        when {
            isBranch -> {
                val summaryBadge = Badge(
                    text = node.summaryText(),
                    type = if (node.valueType == DataNodeValueType.ARRAY) BootstrapType.INFO else BootstrapType.PRIMARY,
                    pill = true
                ).apply {
                    id = "grid-branch-summary-$path"
                }
                col1Row.addChild(summaryBadge)
            }
            node.valueType == DataNodeValueType.BOOLEAN -> {
                val boolCheck = CheckBox(
                    label = if (node.primitiveText.equals("true", ignoreCase = true)) "true" else "false",
                    checked = node.primitiveText.equals("true", ignoreCase = true),
                    switchStyle = true
                ).apply {
                    id = "grid-bool-$path"
                    addClass("grid-bool-input")
                    onCheckedChanged = { checked ->
                        label = if (checked) "true" else "false"
                        updateLeafValueInActiveTab(path, checked.toString(), rebuildTree = false)
                    }
                }
                _rowBoolCheckBoxesByPath[path] = boolCheck
                col1Row.addChild(boolCheck)
            }
            node.valueType == DataNodeValueType.NULL -> {
                val nullBadge = Badge(
                    text = "null",
                    type = BootstrapType.SECONDARY,
                    pill = true
                ).apply {
                    id = "grid-null-$path"
                }
                col1Row.addChild(nullBadge)
            }
            else -> {
                val valField = TextField(
                    value = node.primitiveText,
                    placeholder = "Enter ${node.valueType.displayName} value..."
                ).apply {
                    id = "grid-val-$path"
                    addClass("grid-value-input")
                    tooltipText = "Edit ${node.valueType.displayName} value at '$path'"
                    onValueChanged = { updatedVal ->
                        updateLeafValueInActiveTab(path, updatedVal, rebuildTree = false)
                    }
                    onSubmit = { submittedVal ->
                        updateLeafValueInActiveTab(path, submittedVal, rebuildTree = true)
                    }
                }
                _rowValueFieldsByPath[path] = valField
                col1Row.addChild(valField)
            }
        }
        gridNode.setComponentCell(1, col1Row, tooltipText = "Value at '$path'")

        // --- Column 2: Type Selector (Supports all 12 DataNodeValueType entries) ---
        val typeSelect = Select(
            options = DataNodeValueType.entries.map {
                SelectOption(it.name, "${it.displayName} [${it.badgeLabel}]")
            },
            selectedValue = node.valueType.name
        ).apply {
            id = "grid-type-$path"
            addClass("grid-type-select")
            tooltipText = "Change datatype for '$path'"
            onSelectionChanged = { selectedTypeName ->
                val chosenType = DataNodeValueType.fromNameOrDefault(selectedTypeName)
                if (chosenType != node.valueType) {
                    changeNodeTypeInActiveTab(path, chosenType)
                }
            }
        }
        _rowTypeSelectsByPath[path] = typeSelect
        gridNode.setComponentCell(2, typeSelect, tooltipText = "DataType of '$path'")

        // --- Column 3: Row Action Buttons ---
        val actionsRow = HStack(gap = SpacingScale.S1).apply {
            addClass("grid-col-actions")
        }

        if (isBranch) {
            val addLeafBtn = Button(
                text = "+ Leaf",
                type = BootstrapType.SUCCESS,
                size = BootstrapSize.SM
            ).apply {
                id = "grid-add-leaf-$path"
                tooltipText = "Add a new leaf property/element inside '$path'"
                setOnClick {
                    addLeafToBranchInActiveTab(path)
                }
            }
            val addBranchBtn = Button(
                text = "+ Branch",
                type = BootstrapType.PRIMARY,
                size = BootstrapSize.SM,
                outline = true
            ).apply {
                id = "grid-add-branch-$path"
                tooltipText = "Add a nested container branch inside '$path'"
                setOnClick {
                    addBranchToBranchInActiveTab(path)
                }
            }
            _rowAddLeafButtonsByPath[path] = addLeafBtn
            _rowAddBranchButtonsByPath[path] = addBranchBtn
            actionsRow.addChild(addLeafBtn)
            actionsRow.addChild(addBranchBtn)
        }

        val moveUpBtn = Button(
            text = "↑",
            type = BootstrapType.SECONDARY,
            size = BootstrapSize.SM,
            outline = true
        ).apply {
            id = "grid-up-$path"
            isEnabled = canMoveUp
            tooltipText = "Move '$path' up within its parent container"
            setOnClick {
                moveNodeUpInActiveTab(path)
            }
        }
        val moveDownBtn = Button(
            text = "↓",
            type = BootstrapType.SECONDARY,
            size = BootstrapSize.SM,
            outline = true
        ).apply {
            id = "grid-down-$path"
            isEnabled = canMoveDown
            tooltipText = "Move '$path' down within its parent container"
            setOnClick {
                moveNodeDownInActiveTab(path)
            }
        }
        val dupBtn = Button(
            text = "Dup",
            type = BootstrapType.INFO,
            size = BootstrapSize.SM,
            outline = true
        ).apply {
            id = "grid-dup-$path"
            isEnabled = !isRoot
            tooltipText = "Duplicate '$path'"
            setOnClick {
                duplicateNodeInActiveTab(path)
            }
        }
        val delBtn = Button(
            text = "Del",
            type = BootstrapType.DANGER,
            size = BootstrapSize.SM,
            outline = true
        ).apply {
            id = "grid-del-$path"
            isEnabled = !isRoot
            tooltipText = "Delete '$path'"
            setOnClick {
                deleteNodeInActiveTab(path)
            }
        }

        _rowMoveUpButtonsByPath[path] = moveUpBtn
        _rowMoveDownButtonsByPath[path] = moveDownBtn
        _rowDuplicateButtonsByPath[path] = dupBtn
        _rowDeleteButtonsByPath[path] = delBtn

        actionsRow.addChild(moveUpBtn)
        actionsRow.addChild(moveDownBtn)
        actionsRow.addChild(dupBtn)
        actionsRow.addChild(delBtn)

        gridNode.setComponentCell(3, actionsRow, tooltipText = "Actions for '$path'")

        // Recursively populate child LazyGridTreeNodes for branch containers
        when (node.valueType) {
            DataNodeValueType.ARRAY -> {
                node.children.forEachIndexed { idx, child ->
                    child.keyOrIndex = "[$idx]"
                    val childPath = "$path[$idx]"
                    gridNode.children.add(
                        buildLazyGridTreeNode(
                            tab = tab,
                            node = child,
                            path = childPath,
                            parentNode = node
                        )
                    )
                }
            }
            DataNodeValueType.OBJECT, DataNodeValueType.MAP -> {
                for (child in node.children) {
                    val childPath = "$path.${child.keyOrIndex}"
                    gridNode.children.add(
                        buildLazyGridTreeNode(
                            tab = tab,
                            node = child,
                            path = childPath,
                            parentNode = node
                        )
                    )
                }
            }
            else -> Unit
        }

        return gridNode
    }

    private fun buildTreeViewNode(node: EditableDataNode, path: String): TreeNode<String> {
        val treeNode = TreeNode(
            id = path,
            label = "[${node.valueType.badgeLabel}] ${node.keyOrIndex}: ${node.summaryText()}",
            data = path,
            isExpanded = true
        )
        when (node.valueType) {
            DataNodeValueType.ARRAY -> {
                node.children.forEachIndexed { idx, child ->
                    treeNode.children.add(buildTreeViewNode(child, "$path[$idx]"))
                }
            }
            DataNodeValueType.OBJECT, DataNodeValueType.MAP -> {
                for (child in node.children) {
                    treeNode.children.add(buildTreeViewNode(child, "$path.${child.keyOrIndex}"))
                }
            }
            else -> Unit
        }
        return treeNode
    }

    private fun refreshInspectorForSelectedNode(tab: DataDocumentTabState, selected: EditableDataNode) {
        val path = tab.selectedNodePath
        val parent = tab.rootNode.findParentOfPath(path)

        selectedPathBadge.text = path
        selectedTypeBadge.text = selected.valueType.badgeLabel
        selectedTypeBadge.type = badgeTypeForValueType(selected.valueType)

        nodeKeyField.value = selected.keyOrIndex
        val canRenameKey = parent != null &&
            (parent.valueType == DataNodeValueType.OBJECT || parent.valueType == DataNodeValueType.MAP)
        nodeKeyField.isReadOnly = !canRenameKey
        nodeKeyField.isEnabled = canRenameKey

        nodeTypeSelect.selectedValue = selected.valueType.name
        nodeValueField.value = selected.primitiveText
        nodeBooleanCheckBox.isChecked = selected.primitiveText.trim().equals("true", ignoreCase = true)
        updateInspectorInputVisibility(selected.valueType)

        val isRoot = (path == "root")
        selectParentNodeButton.isEnabled = !isRoot
        duplicateNodeButton.isEnabled = !isRoot
        deleteNodeButton.isEnabled = !isRoot

        val targetContainerPath = if (selected.isContainer) {
            path
        } else {
            EditableDataNode.parentPathOf(path) ?: "root"
        }
        val targetContainer = tab.rootNode.findByPath(targetContainerPath) ?: tab.rootNode
        addChildTargetLabel.text = "Target Container: $targetContainerPath (${targetContainer.valueType.displayName})"
        newChildKeyField.isEnabled = (targetContainer.valueType != DataNodeValueType.ARRAY)
        updateNewChildInputVisibility(DataNodeValueType.fromNameOrDefault(newChildTypeSelect.selectedValue))
    }

    private fun updateInspectorInputVisibility(valueType: DataNodeValueType) {
        when {
            valueType.isContainer || valueType == DataNodeValueType.NULL -> {
                nodeValueField.isVisible = false
                nodeBooleanCheckBox.isVisible = false
            }
            valueType == DataNodeValueType.BOOLEAN -> {
                nodeValueField.isVisible = false
                nodeBooleanCheckBox.isVisible = true
            }
            else -> {
                nodeValueField.isVisible = true
                nodeBooleanCheckBox.isVisible = false
            }
        }
    }

    private fun updateNewChildInputVisibility(valueType: DataNodeValueType) {
        when {
            valueType.isContainer || valueType == DataNodeValueType.NULL -> {
                newChildValueField.isVisible = false
                newChildBoolCheckBox.isVisible = false
            }
            valueType == DataNodeValueType.BOOLEAN -> {
                newChildValueField.isVisible = false
                newChildBoolCheckBox.isVisible = true
            }
            else -> {
                newChildValueField.isVisible = true
                newChildBoolCheckBox.isVisible = false
            }
        }
    }

    private fun refreshChildrenTable(tab: DataDocumentTabState, selected: EditableDataNode) {
        val containerPath = if (selected.isContainer) {
            tab.selectedNodePath
        } else {
            EditableDataNode.parentPathOf(tab.selectedNodePath) ?: "root"
        }
        val containerNode = tab.rootNode.findByPath(containerPath) ?: tab.rootNode
        val rows = containerNode.children.mapIndexed { idx, child ->
            val childPath = if (containerNode.valueType == DataNodeValueType.ARRAY) {
                "$containerPath[$idx]"
            } else {
                "$containerPath.${child.keyOrIndex}"
            }
            TableRow(
                id = childPath,
                cells = listOf(
                    TableCell(child.keyOrIndex),
                    TableCell(child.valueType.displayName),
                    TableCell(child.summaryText()),
                    TableCell(childPath)
                )
            )
        }
        containerChildrenTable.setRows(rows)
    }

    private fun refreshFormatComparisonCard(tab: DataDocumentTabState) {
        activeFormatBadge.text = tab.format.shortBadge
        activeFormatBadge.type = when (tab.format) {
            DataFormat.JSON -> BootstrapType.PRIMARY
            DataFormat.BINARY_JSON_BSON -> BootstrapType.INFO
            DataFormat.BINARY_PLIST -> BootstrapType.SUCCESS
        }
        formatSelector.selectedValue = tab.format.name

        val element = tab.toPreferenceElement()
        val sizes = DataDocumentCodec.computeFormatSizes(element, preferences.prettyPrintIndent)
        val jsonSize = sizes[DataFormat.JSON] ?: 0
        val bsonSize = sizes[DataFormat.BINARY_JSON_BSON] ?: 0
        val plistSize = sizes[DataFormat.BINARY_PLIST] ?: 0
        val maxSize = maxOf(jsonSize, bsonSize, plistSize, 1).toFloat()

        sizeComparisonSummaryLabel.text = DataDocumentCodec.formatSizeComparison(element, preferences.prettyPrintIndent)

        jsonSizeBar.progress = (jsonSize / maxSize).coerceIn(0.05f, 1f)
        jsonSizeBar.label = "JSON: $jsonSize B"

        bsonSizeBar.progress = (bsonSize / maxSize).coerceIn(0.05f, 1f)
        bsonSizeBar.label = "BinaryJson (BSON): $bsonSize B"

        plistSizeBar.progress = (plistSize / maxSize).coerceIn(0.05f, 1f)
        plistSizeBar.label = "BinaryPlist (bplist00): $plistSize B"
    }

    private fun applyInspectorEditsToSelectedNode() {
        val tab = activeTabState ?: return
        val newType = DataNodeValueType.fromNameOrDefault(nodeTypeSelect.selectedValue)
        val newPrimitive = if (newType == DataNodeValueType.BOOLEAN) {
            nodeBooleanCheckBox.isChecked.toString()
        } else {
            nodeValueField.value
        }
        tab.updateSelectedNode(
            newKeyName = nodeKeyField.value,
            newType = newType,
            newPrimitiveValue = newPrimitive
        )
        refreshActiveTabWorkspace()
        setStatus("Updated node '${tab.selectedNodePath}' (${newType.displayName}).")
    }

    private fun addChildNodeInActiveTab() {
        val tab = activeTabState ?: return
        val childType = DataNodeValueType.fromNameOrDefault(newChildTypeSelect.selectedValue)
        val childValue = when {
            childType.isContainer || childType == DataNodeValueType.NULL -> childType.defaultPrimitiveText()
            childType == DataNodeValueType.BOOLEAN -> newChildBoolCheckBox.isChecked.toString()
            else -> newChildValueField.value.ifEmpty { childType.defaultPrimitiveText() }
        }
        val newPath = tab.addChildToSelectedOrParent(
            childKey = newChildKeyField.value,
            childType = childType,
            childValue = childValue
        )
        refreshActiveTabWorkspace()
        setStatus("Added child node '$newPath' (${childType.displayName}).")
    }

    private fun duplicateSelectedNodeInActiveTab() {
        val tab = activeTabState ?: return
        val duplicatedPath = tab.duplicateSelectedNode()
        if (duplicatedPath != null) {
            refreshActiveTabWorkspace()
            setStatus("Duplicated node to '$duplicatedPath'.")
        } else {
            setStatus("Root node cannot be duplicated.")
        }
    }

    private fun deleteSelectedNodeInActiveTab() {
        val tab = activeTabState ?: return
        val oldPath = tab.selectedNodePath
        if (tab.deleteSelectedNode()) {
            refreshActiveTabWorkspace()
            setStatus("Deleted node '$oldPath'.")
        } else {
            setStatus("Root node cannot be deleted.")
        }
    }

    private fun badgeTypeForValueType(type: DataNodeValueType): BootstrapType = when (type) {
        DataNodeValueType.OBJECT, DataNodeValueType.MAP -> BootstrapType.PRIMARY
        DataNodeValueType.ARRAY -> BootstrapType.INFO
        DataNodeValueType.STRING -> BootstrapType.SUCCESS
        DataNodeValueType.BOOLEAN -> BootstrapType.WARNING
        DataNodeValueType.NULL -> BootstrapType.SECONDARY
        else -> BootstrapType.DARK
    }

    private fun nextTabId(): String {
        tabCounter += 1
        return "doc_tab_$tabCounter"
    }

    private fun setStatus(message: String) {
        statusMessageLabel.text = message
    }
}
