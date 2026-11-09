package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.lazy.LazyGrid
import com.nexustools.genericui.component.lazy.LazyList
import com.nexustools.genericui.component.lazy.LazyViewportWindow
import com.nexustools.genericui.component.lazy.TreeNode
import com.nexustools.genericui.component.lazy.TreeView
import com.nexustools.genericui.component.shell.SplitPane
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.core.AbstractComponent
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.Orientation
import com.nexustools.genericui.ext.dsl.bootstrap
import com.nexustools.genericui.ext.dsl.style
import com.nexustools.genericui.style.color.RgbaColor
import kotlin.math.roundToInt

/**
 * Primary [Fragment] for the Virtualized Data Explorer example.
 *
 * Generates 10,000 [DatasetRecord] entries and renders them inside a 3-pane explorer workspace:
 * - Left pane: Department Hierarchy [TreeView] for browsing departments and representative nodes.
 * - Center pane: Active Virtualized Viewport switching between [LazyList], [LazyGrid], and [TreeView]
 *   with true viewport window virtualization (`[firstVisible .. lastVisible]`).
 * - Right pane: Record Detail & SLA Telemetry Inspector with live progress meters and department summary table.
 */
@MainAppFragment
class VirtualizedDataExplorerFragment(
    val totalDatasetSize: Int = DEFAULT_RECORD_COUNT
) : Fragment() {

    val allRecords: List<DatasetRecord> = generateDataset(totalDatasetSize)
    private var filteredRecords: List<DatasetRecord> = allRecords

    var activeMode: ExplorerViewMode = ExplorerViewMode.LIST
        private set

    var filterQuery: String = ""
        private set

    var selectedCategory: String = CATEGORY_ALL
        private set

    var selectedRecord: DatasetRecord? = null
        private set

    var totalLoadEvents: Int = 0
        private set

    var totalUnloadEvents: Int = 0
        private set

    private var activeWindow: LazyViewportWindow = LazyViewportWindow.EMPTY

    val modeListButton: Button = Button("List View", BootstrapType.PRIMARY, BootstrapSize.SM).apply {
        id = "btn-mode-list"
        widthPx = 88
        bootstrap {
            type = BootstrapType.PRIMARY
            size = BootstrapSize.SM
            fontWeight = FontWeight.SEMIBOLD
        }
        style {
            accentColor = RgbaColor.fromHex("#2563EB")
        }
        onClick = { switchViewMode(ExplorerViewMode.LIST) }
    }

    val modeGridButton: Button = Button("Grid View", BootstrapType.SECONDARY, BootstrapSize.SM).apply {
        id = "btn-mode-grid"
        widthPx = 88
        bootstrap {
            type = BootstrapType.SECONDARY
            size = BootstrapSize.SM
        }
        onClick = { switchViewMode(ExplorerViewMode.GRID) }
    }

    val modeTreeButton: Button = Button("Tree View", BootstrapType.SECONDARY, BootstrapSize.SM).apply {
        id = "btn-mode-tree"
        widthPx = 88
        bootstrap {
            type = BootstrapType.SECONDARY
            size = BootstrapSize.SM
        }
        onClick = { switchViewMode(ExplorerViewMode.TREE) }
    }

    val filterTextField: TextField = TextField(
        value = "",
        placeholder = "Filter 10,000 records by title, status, or #id..."
    ).apply {
        id = "input-filter"
        widthPx = 330
        onValueChanged = { query ->
            applyFilter(query = query, category = selectedCategory)
        }
    }

    val categorySelect: Select = Select(
        options = listOf(
            SelectOption(CATEGORY_ALL, "All Categories"),
            SelectOption("Compute", "Compute"),
            SelectOption("Storage", "Storage"),
            SelectOption("Network", "Network"),
            SelectOption("Security", "Security"),
            SelectOption("Analytics", "Analytics")
        ),
        selectedValue = CATEGORY_ALL
    ).apply {
        id = "select-category"
        widthPx = 148
        onSelectionChanged = { category ->
            applyFilter(query = filterQuery, category = category)
        }
    }

    val clearFilterButton: Button = Button("Reset Filter", BootstrapType.SECONDARY, BootstrapSize.SM, outline = false).apply {
        id = "btn-clear-filter"
        widthPx = 104
        onClick = {
            filterTextField.value = ""
            categorySelect.selectedValue = CATEGORY_ALL
            applyFilter(query = "", category = CATEGORY_ALL)
        }
    }

    val scrollTopButton: Button = Button("Scroll Top (0)", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-scroll-top"
        widthPx = 116
        onClick = { scrollToRecordIndex(0) }
    }

    val scrollMidButton: Button = Button("Jump #5000", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-scroll-mid"
        widthPx = 98
        onClick = { scrollToRecordIndex(5000) }
    }

    val scrollBottomButton: Button = Button("Jump End", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-scroll-bottom"
        widthPx = 86
        onClick = { scrollToRecordIndex((filteredRecords.size - 1).coerceAtLeast(0)) }
    }

    val totalItemsBadge: Badge = Badge("Total: 10000 / 10000", BootstrapType.PRIMARY, pill = true).apply {
        id = "badge-total-items"
    }

    val activeWindowBadge: Badge = Badge("Window: [0 .. 0] (0 bound)", BootstrapType.INFO, pill = true).apply {
        id = "badge-active-window"
    }

    val selectedItemBadge: Badge = Badge("Selected: None", BootstrapType.SUCCESS, pill = true).apply {
        id = "badge-selected-item"
    }

    val activeModeBadge: Badge = Badge("Mode: LIST", BootstrapType.SECONDARY, pill = true).apply {
        id = "badge-active-mode"
    }

    val recycleStatsBadge: Badge = Badge("Bound: 0 | Recycled: 0", BootstrapType.WARNING, pill = true).apply {
        id = "badge-recycle-stats"
    }

    val viewportProgressBar: ProgressBar = ProgressBar(
        progress = 0.02f,
        label = "Viewport Window: 0.2% of 10,000 rows",
        type = BootstrapType.INFO
    ).apply {
        id = "progress-viewport-window"
        widthPx = 280
    }

    val departmentTreeView: TreeView<DatasetRecord> = TreeView<DatasetRecord>(
        estimatedRowHeightPx = 24f,
        overscanRows = 6
    ).apply {
        id = "explorer-department-tree"
        selectedNodeId = "dept-node-1"
        onNodeSelected = { node ->
            node.data?.let { selectRecord(it) }
        }
    }

    val departmentCountLabel: Label = Label("5 Depts | 10,000 Active Nodes")

    val listColumnHeaderBar: HStack = HStack(gap = SpacingScale.S2).apply {
        addClass("explorer-column-header")
        addChild(Label("NODE RECORD").apply { widthPx = 116 })
        addChild(Label("DEPT").apply { widthPx = 78 })
        addChild(Label("CLUSTER").apply { widthPx = 94 })
        addChild(Label("LATENCY").apply { widthPx = 96 })
        addChild(Label("LOAD %").apply { widthPx = 92 })
        addChild(Label("STATUS").apply { widthPx = 78 })
    }

    val lazyList: LazyList = LazyList(
        orientation = Orientation.VERTICAL,
        estimatedItemExtentPx = 42f,
        overscanItems = 3
    ).apply {
        id = "explorer-lazy-list"
    }

    val lazyGrid: LazyGrid = LazyGrid(
        columns = 3,
        estimatedRowHeightPx = 104f,
        overscanRows = 2
    ).apply {
        id = "explorer-lazy-grid"
        minColumnWidthPx = 220
        isVisible = false
    }

    val treeView: TreeView<DatasetRecord> = TreeView<DatasetRecord>(
        estimatedRowHeightPx = 28f,
        overscanRows = 4
    ).apply {
        id = "explorer-tree-view"
        isVisible = false
        onNodeSelected = { node ->
            node.data?.let { selectRecord(it) }
        }
    }

    val inspectorNodeBadge: Badge = Badge("#1 Node-00001", BootstrapType.PRIMARY, pill = true)
    val inspectorCategoryBadge: Badge = Badge("Compute", BootstrapType.INFO, pill = true)
    val inspectorStatusBadge: Badge = Badge("HEALTHY", BootstrapType.SUCCESS, pill = true)
    val inspectorClusterLabel: Label = Label("Cluster: us-east-1 / rack-c1 | Index: 0")
    val inspectorLatencyLabel: Label = Label("Latency Budget: 4 ms / 250 ms SLA")
    val inspectorLatencyBar: ProgressBar = ProgressBar(
        progress = 0.08f,
        label = "4 ms (98% SLA Headroom)",
        type = BootstrapType.SUCCESS
    )
    val inspectorLoadLabel: Label = Label("Compute & I/O Utilization: 35%")
    val inspectorLoadBar: ProgressBar = ProgressBar(
        progress = 0.35f,
        label = "CPU 35% | 14.2k IOPS",
        type = BootstrapType.PRIMARY
    )
    val inspectorPoolLabel: Label = Label("ViewHolder Pool: 24 Bound | 0 Recycled")
    val inspectorPoolBar: ProgressBar = ProgressBar(
        progress = 0.80f,
        label = "24 / 30 Active Holders",
        type = BootstrapType.WARNING
    )
    val departmentSummaryTable: Table = Table().apply {
        id = "explorer-department-table"
        isStriped = true
        setColumns(
            listOf(
                TableColumn("dept", "DEPT"),
                TableColumn("nodes", "NODES"),
                TableColumn("avgMs", "AVG MS"),
                TableColumn("slo", "SLA %")
            )
        )
        setColumnWeights(1.4f, 0.9f, 0.9f, 0.8f)
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("explorer-root")

        val rootStack = VStack(gap = SpacingScale.S2)

        val headerRow = HStack(gap = SpacingScale.S2).apply {
            flexWrap = FlexWrap.WRAP
            addChild(Heading("Virtualized Data Explorer (10,000 Rows)", level = 4))
            addChild(activeModeBadge)
            addChild(Badge("O(1) ViewHolder Recycling", BootstrapType.INFO, pill = true))
            addChild(Badge("5 Departments | 20 Clusters", BootstrapType.SECONDARY, pill = true))
            addChild(Badge("SLA Target < 180 ms", BootstrapType.SUCCESS, pill = true))
        }

        val controlsBar = HStack(gap = SpacingScale.S2).apply {
            flexWrap = FlexWrap.WRAP
            addClass("explorer-toolbar")
            bootstrap {
                padding = SpacingScale.S2
                borderRadius = BorderRadiusStyle.MEDIUM
            }
            addChild(modeListButton)
            addChild(modeGridButton)
            addChild(modeTreeButton)
            addChild(filterTextField)
            addChild(categorySelect)
            addChild(clearFilterButton)
            addChild(scrollTopButton)
            addChild(scrollMidButton)
            addChild(scrollBottomButton)
        }

        val telemetryBar = HStack(gap = SpacingScale.S2).apply {
            flexWrap = FlexWrap.WRAP
            addClass("explorer-telemetry-bar")
            bootstrap {
                padding = SpacingScale.S2
                borderRadius = BorderRadiusStyle.SMALL
            }
            addChild(totalItemsBadge)
            addChild(activeWindowBadge)
            addChild(recycleStatsBadge)
            addChild(selectedItemBadge)
            addChild(viewportProgressBar)
        }

        val departmentSidebarCard = Card("Department Hierarchy").apply {
            addClass("explorer-sidebar-card")
            bootstrap {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.S2
            }
            addChild(departmentCountLabel)
            addChild(departmentTreeView)
        }

        val viewportCard = Card("Active Virtualized Viewport").apply {
            addClass("explorer-viewport-card")
            bootstrap {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.S2
            }
            addChild(listColumnHeaderBar)
            addChild(lazyList)
            addChild(lazyGrid)
            addChild(treeView)
        }

        val inspectorBadgesRow = HStack(gap = SpacingScale.S1).apply {
            flexWrap = FlexWrap.WRAP
            addChild(inspectorNodeBadge)
            addChild(inspectorCategoryBadge)
            addChild(inspectorStatusBadge)
        }

        val inspectorCard = Card("Record Detail Inspector").apply {
            addClass("explorer-inspector-card")
            bootstrap {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.S2
            }
            addChild(inspectorBadgesRow)
            addChild(inspectorClusterLabel)
            addChild(inspectorLatencyLabel)
            addChild(inspectorLatencyBar)
            addChild(inspectorLoadLabel)
            addChild(inspectorLoadBar)
            addChild(inspectorPoolLabel)
            addChild(inspectorPoolBar)
            addChild(Heading("Department SLA Breakdown", level = 5))
            addChild(departmentSummaryTable)
        }

        val centerAndRightSplit = SplitPane(
            orientation = Orientation.HORIZONTAL,
            dividerPosition = 0.66f
        ).apply {
            setPanes(viewportCard, inspectorCard)
        }

        val workspaceSplit = SplitPane(
            orientation = Orientation.HORIZONTAL,
            dividerPosition = 0.22f
        ).apply {
            setPanes(departmentSidebarCard, centerAndRightSplit)
        }

        rootStack.addChild(headerRow)
        rootStack.addChild(controlsBar)
        rootStack.addChild(telemetryBar)
        rootStack.addChild(workspaceSplit)
        addChild(rootStack)

        rebuildVirtualizedDataViews()
    }

    /**
     * Switches between [ExplorerViewMode.LIST], [ExplorerViewMode.GRID], and [ExplorerViewMode.TREE].
     */
    fun switchViewMode(mode: ExplorerViewMode) {
        activeMode = mode
        lazyList.isVisible = (mode == ExplorerViewMode.LIST)
        lazyGrid.isVisible = (mode == ExplorerViewMode.GRID)
        treeView.isVisible = (mode == ExplorerViewMode.TREE)
        listColumnHeaderBar.isVisible = (mode == ExplorerViewMode.LIST)

        modeListButton.getBootstrap().type = if (mode == ExplorerViewMode.LIST) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        modeGridButton.getBootstrap().type = if (mode == ExplorerViewMode.GRID) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        modeTreeButton.getBootstrap().type = if (mode == ExplorerViewMode.TREE) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        activeModeBadge.text = "Mode: ${mode.name}"

        activeWindow = when (mode) {
            ExplorerViewMode.LIST -> lazyList.onViewportChanged(lazyList.scrollOffsetPx, lazyList.viewportExtentPx)
            ExplorerViewMode.GRID -> lazyGrid.onViewportChanged(lazyGrid.scrollOffsetPx, lazyGrid.viewportHeightPx)
            ExplorerViewMode.TREE -> treeView.onViewportChanged(treeView.scrollOffsetPx, treeView.viewportHeightPx)
        }
        updateTelemetryBadges()
    }

    /**
     * Filters the 10,000-record dataset by [query] and [category] and refreshes the active virtualized view.
     */
    fun applyFilter(query: String, category: String = selectedCategory) {
        filterQuery = query.trim()
        selectedCategory = category.ifBlank { CATEGORY_ALL }

        filteredRecords = allRecords.filter { record ->
            val matchesCategory = selectedCategory.equals(CATEGORY_ALL, ignoreCase = true) ||
                record.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = filterQuery.isEmpty() ||
                record.title.contains(filterQuery, ignoreCase = true) ||
                record.category.contains(filterQuery, ignoreCase = true) ||
                record.status.contains(filterQuery, ignoreCase = true) ||
                "#${record.id}".contains(filterQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        rebuildVirtualizedDataViews()
    }

    /**
     * Scrolls the active virtualized viewport to [scrollOffsetPx] with [viewportExtentPx] height.
     */
    fun scrollViewportTo(scrollOffsetPx: Float, viewportExtentPx: Float = 480f): LazyViewportWindow {
        activeWindow = when (activeMode) {
            ExplorerViewMode.LIST -> lazyList.onViewportChanged(scrollOffsetPx, viewportExtentPx)
            ExplorerViewMode.GRID -> lazyGrid.onViewportChanged(scrollOffsetPx, viewportExtentPx)
            ExplorerViewMode.TREE -> treeView.onViewportChanged(scrollOffsetPx, viewportExtentPx)
        }
        updateTelemetryBadges()
        return activeWindow
    }

    /**
     * Scrolls the active virtualized view so that the item at [index] is brought into the viewport window.
     */
    fun scrollToRecordIndex(index: Int) {
        val safeIndex = index.coerceIn(0, (filteredRecords.size - 1).coerceAtLeast(0))
        when (activeMode) {
            ExplorerViewMode.LIST -> {
                lazyList.scrollToIndex(safeIndex)
                activeWindow = lazyList.refreshLazyViewport()
            }
            ExplorerViewMode.GRID -> {
                lazyGrid.scrollToIndex(safeIndex)
                activeWindow = lazyGrid.refreshLazyViewport()
            }
            ExplorerViewMode.TREE -> {
                val targetOffset = safeIndex * treeView.estimatedRowHeightPx
                activeWindow = treeView.onViewportChanged(targetOffset, treeView.viewportHeightPx)
            }
        }
        updateTelemetryBadges()
    }

    /**
     * Selects a specific [DatasetRecord] and updates the selection telemetry badge and detail inspector.
     */
    fun selectRecord(record: DatasetRecord) {
        selectedRecord = record
        treeView.selectedNodeId = "node-record-${record.id}"
        departmentTreeView.selectedNodeId = "dept-node-${record.id}"
        updateTelemetryBadges()
    }

    /**
     * Selects a record by its unique [recordId] if present in the dataset.
     */
    fun selectRecordById(recordId: Int): Boolean {
        val match = allRecords.firstOrNull { it.id == recordId } ?: return false
        selectRecord(match)
        return true
    }

    fun getFilteredRecords(): List<DatasetRecord> = filteredRecords

    fun getActiveViewportWindow(): LazyViewportWindow = activeWindow

    private fun rebuildVirtualizedDataViews() {
        lazyList.setItems(
            count = filteredRecords.size,
            onLoadItem = { index ->
                totalLoadEvents++
                buildListRowComponent(index, filteredRecords[index])
            },
            onUnloadItem = { _, _ ->
                totalUnloadEvents++
            }
        )

        lazyGrid.setItems(
            count = filteredRecords.size,
            onLoadItem = { index ->
                totalLoadEvents++
                buildGridCellComponent(index, filteredRecords[index])
            },
            onUnloadItem = { _, _ ->
                totalUnloadEvents++
            }
        )

        val categoryGroups = filteredRecords.groupBy { it.category }
        val roots = categoryGroups.entries.map { (catName, recordsInCat) ->
            TreeNode(
                id = "node-category-$catName",
                label = "$catName (${recordsInCat.size} items)",
                data = recordsInCat.firstOrNull(),
                isExpanded = true,
                children = recordsInCat.take(4).map { rec ->
                    TreeNode(
                        id = "node-record-${rec.id}",
                        label = "#${rec.id} ${rec.title} [${rec.latencyMs}ms - ${rec.status}]",
                        data = rec
                    )
                }.toMutableList()
            )
        }
        treeView.setRootNodes(roots)

        val deptRoots = CATEGORIES.mapNotNull { catName ->
            val recordsInCat = categoryGroups[catName] ?: emptyList()
            if (recordsInCat.isEmpty() && selectedCategory != CATEGORY_ALL) return@mapNotNull null
            TreeNode(
                id = "dept-category-$catName",
                label = "$catName (${recordsInCat.size})",
                data = recordsInCat.firstOrNull(),
                isExpanded = true,
                children = recordsInCat.take(3).map { rec ->
                    TreeNode(
                        id = "dept-node-${rec.id}",
                        label = "#${rec.id} ${rec.title} (${rec.latencyMs}ms)",
                        data = rec
                    )
                }.toMutableList()
            )
        }
        departmentTreeView.setRootNodes(deptRoots)
        departmentCountLabel.text = "${deptRoots.size} Depts | ${filteredRecords.size} Active Nodes"

        rebuildDepartmentSummaryTable(categoryGroups)

        activeWindow = when (activeMode) {
            ExplorerViewMode.LIST -> lazyList.onViewportChanged(0f, lazyList.viewportExtentPx)
            ExplorerViewMode.GRID -> lazyGrid.onViewportChanged(0f, lazyGrid.viewportHeightPx)
            ExplorerViewMode.TREE -> treeView.onViewportChanged(0f, treeView.viewportHeightPx)
        }
        updateTelemetryBadges()
    }

    private fun rebuildDepartmentSummaryTable(categoryGroups: Map<String, List<DatasetRecord>>) {
        val rows = mutableListOf<TableRow>()
        for (cat in CATEGORIES) {
            val list = categoryGroups[cat].orEmpty()
            val avgLatency = if (list.isNotEmpty()) list.sumOf { it.latencyMs } / list.size else 0
            val healthyPct = if (list.isNotEmpty()) {
                ((list.count { it.status == "HEALTHY" || it.status == "SYNCING" } * 100.0) / list.size).roundToInt()
            } else {
                100
            }
            rows.add(
                TableRow(
                    id = "dept-row-$cat",
                    cells = listOf(
                        TableCell(cat),
                        TableCell(list.size.toString()),
                        TableCell("${avgLatency}ms"),
                        TableCell("$healthyPct%")
                    )
                )
            )
        }
        val zones = listOf("us-east-1", "us-west-2", "eu-west-1", "ap-east-1")
        for ((zIdx, zone) in zones.withIndex()) {
            val zCount = (filteredRecords.size / 4) + if (zIdx == 0) (filteredRecords.size % 4) else 0
            rows.add(
                TableRow(
                    id = "zone-row-$zone",
                    cells = listOf(
                        TableCell(zone),
                        TableCell(zCount.toString()),
                        TableCell("${98 + zIdx * 9}ms"),
                        TableCell("${99 - zIdx}%")
                    )
                )
            )
        }
        departmentSummaryTable.setRows(rows)
    }

    private fun buildListRowComponent(index: Int, record: DatasetRecord): AbstractComponent {
        val row = HStack(gap = SpacingScale.S2).apply {
            id = "record-row-${record.id}"
            addClass("explorer-row-item")
            onClick = { selectRecord(record) }
        }
        val selectBtn = Button(
            text = "#${record.id} ${record.title}",
            type = BootstrapType.PRIMARY,
            size = BootstrapSize.SM,
            outline = false
        ).apply {
            id = "btn-select-record-${record.id}"
            widthPx = 116
            onClick = { selectRecord(record) }
        }
        val categoryBadge = Badge(record.category, BootstrapType.INFO, pill = true).apply {
            widthPx = 78
        }
        val clusterZone = "us-e${(record.id % 4) + 1}/c${(record.id % 8) + 1}"
        val clusterLabel = Label(clusterZone).apply {
            widthPx = 94
        }
        val latencyLabel = Label("${record.latencyMs} ms (idx $index)").apply {
            widthPx = 96
        }
        val loadPct = 22 + ((record.id * 13) % 72)
        val loadBar = ProgressBar(
            progress = loadPct / 100f,
            label = "$loadPct%",
            type = when {
                loadPct >= 80 -> BootstrapType.WARNING
                loadPct <= 45 -> BootstrapType.SUCCESS
                else -> BootstrapType.PRIMARY
            }
        ).apply {
            widthPx = 92
        }
        val statusBadge = Badge(
            record.status,
            when (record.status) {
                "HEALTHY" -> BootstrapType.SUCCESS
                "DEGRADED" -> BootstrapType.WARNING
                "SYNCING" -> BootstrapType.INFO
                else -> BootstrapType.SECONDARY
            }
        ).apply {
            widthPx = 78
        }
        row.addChild(selectBtn)
        row.addChild(categoryBadge)
        row.addChild(clusterLabel)
        row.addChild(latencyLabel)
        row.addChild(loadBar)
        row.addChild(statusBadge)
        return row
    }

    private fun buildGridCellComponent(index: Int, record: DatasetRecord): AbstractComponent {
        val cell = VStack(gap = SpacingScale.S1).apply {
            id = "record-card-${record.id}"
            addClass("explorer-grid-card")
            onClick = { selectRecord(record) }
        }
        val titleBtn = Button(
            text = "#${record.id} ${record.title}",
            type = BootstrapType.PRIMARY,
            size = BootstrapSize.SM
        ).apply {
            id = "btn-grid-select-${record.id}"
            onClick = { selectRecord(record) }
        }
        cell.addChild(titleBtn)
        cell.addChild(Label("${record.category} | ${record.latencyMs} ms | cell $index"))
        cell.addChild(Badge(record.status, BootstrapType.INFO))
        return cell
    }

    private fun updateTelemetryBadges() {
        totalItemsBadge.text = "Total: ${filteredRecords.size} / ${allRecords.size}"
        val boundCount = when (activeMode) {
            ExplorerViewMode.LIST -> lazyList.loadedItemCount
            ExplorerViewMode.GRID -> lazyGrid.loadedItemCount
            ExplorerViewMode.TREE -> treeView.loadedRowIndices.size
        }
        activeWindowBadge.text =
            "Window: [${activeWindow.firstVisibleIndex} .. ${activeWindow.lastVisibleIndex}] ($boundCount bound)"
        recycleStatsBadge.text = "Bound: $boundCount | Recycled: $totalUnloadEvents"
        val currentSelection = selectedRecord
        selectedItemBadge.text = if (currentSelection != null) {
            "Selected: #${currentSelection.id} ${currentSelection.title} (${currentSelection.category})"
        } else {
            "Selected: None"
        }

        val totalSize = filteredRecords.size.coerceAtLeast(1)
        val endIdx = activeWindow.lastVisibleIndex.coerceIn(0, totalSize - 1)
        val windowFrac = ((endIdx + 1).toFloat() / totalSize.toFloat()).coerceIn(0.02f, 1f)
        viewportProgressBar.progress = windowFrac
        viewportProgressBar.label = "Window [${activeWindow.firstVisibleIndex}..$endIdx] of ${filteredRecords.size}"

        val inspected = currentSelection
            ?: filteredRecords.getOrNull(activeWindow.firstVisibleIndex)
            ?: allRecords.firstOrNull()
        if (inspected != null) {
            val inspectedIdx = (inspected.id - 1).coerceAtLeast(0)
            inspectorNodeBadge.text = "#${inspected.id} ${inspected.title}"
            inspectorCategoryBadge.text = inspected.category
            inspectorStatusBadge.text = inspected.status
            inspectorStatusBadge.type = when (inspected.status) {
                "HEALTHY" -> BootstrapType.SUCCESS
                "DEGRADED" -> BootstrapType.WARNING
                "SYNCING" -> BootstrapType.INFO
                else -> BootstrapType.SECONDARY
            }
            inspectorClusterLabel.text =
                "Cluster: us-east-${(inspected.id % 4) + 1} / rack-c${(inspected.id % 8) + 1} | Idx: $inspectedIdx"
            inspectorLatencyLabel.text = "Latency Budget: ${inspected.latencyMs} ms / 250 ms SLA"
            val latFrac = (inspected.latencyMs / 250f).coerceIn(0.04f, 1f)
            val headroom = (100 - (latFrac * 100).roundToInt()).coerceAtLeast(0)
            inspectorLatencyBar.progress = latFrac
            inspectorLatencyBar.label = "${inspected.latencyMs} ms ($headroom% SLA Headroom)"
            inspectorLatencyBar.type = if (inspected.latencyMs > 180) BootstrapType.WARNING else BootstrapType.SUCCESS

            val loadPct = 22 + ((inspected.id * 13) % 72)
            inspectorLoadLabel.text = "Compute & I/O Utilization: $loadPct%"
            inspectorLoadBar.progress = loadPct / 100f
            inspectorLoadBar.label = "CPU $loadPct% | ${10 + (inspected.id % 15)}k IOPS"
        }
        inspectorPoolLabel.text = "ViewHolder Pool: $boundCount Bound | $totalUnloadEvents Recycled"
        inspectorPoolBar.progress = (boundCount / 30f).coerceIn(0.05f, 1f)
        inspectorPoolBar.label = "$boundCount / 30 Active Holders"
    }

    companion object {
        const val DEFAULT_RECORD_COUNT: Int = 10_000
        const val CATEGORY_ALL: String = "ALL"

        private val CATEGORIES = listOf("Compute", "Storage", "Network", "Security", "Analytics")
        private val STATUSES = listOf("HEALTHY", "DEGRADED", "IDLE", "SYNCING")

        fun generateDataset(count: Int = DEFAULT_RECORD_COUNT): List<DatasetRecord> =
            List(count.coerceAtLeast(0)) { idx ->
                val id = idx + 1
                val category = CATEGORIES[idx % CATEGORIES.size]
                val status = STATUSES[idx % STATUSES.size]
                val latency = 4 + ((idx * 17) % 240)
                val padded = id.toString().padStart(5, '0')
                DatasetRecord(
                    id = id,
                    title = "Node-$padded",
                    category = category,
                    latencyMs = latency,
                    status = status
                )
            }
    }
}
