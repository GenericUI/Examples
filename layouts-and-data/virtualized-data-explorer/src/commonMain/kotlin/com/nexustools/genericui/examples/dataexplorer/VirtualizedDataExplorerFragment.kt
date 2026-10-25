package com.nexustools.genericui.examples.dataexplorer

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.lazy.LazyGrid
import com.nexustools.genericui.component.lazy.LazyList
import com.nexustools.genericui.component.lazy.LazyViewportWindow
import com.nexustools.genericui.component.lazy.TreeNode
import com.nexustools.genericui.component.lazy.TreeView
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.core.AbstractComponent
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.Orientation

/**
 * Primary [Fragment] for the Virtualized Data Explorer example.
 *
 * Generates 10,000 [DatasetRecord] entries and renders them using [LazyList], [LazyGrid],
 * and [TreeView] with true viewport window virtualization (`[firstVisible .. lastVisible]`),
 * live recycling counters, search and category filtering, and item selection telemetry.
 */
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
        onClick = { switchViewMode(ExplorerViewMode.LIST) }
    }

    val modeGridButton: Button = Button("Grid View", BootstrapType.SECONDARY, BootstrapSize.SM).apply {
        id = "btn-mode-grid"
        onClick = { switchViewMode(ExplorerViewMode.GRID) }
    }

    val modeTreeButton: Button = Button("Tree View", BootstrapType.SECONDARY, BootstrapSize.SM).apply {
        id = "btn-mode-tree"
        onClick = { switchViewMode(ExplorerViewMode.TREE) }
    }

    val filterTextField: TextField = TextField(
        value = "",
        placeholder = "Filter 10,000 records by title, status, or #id..."
    ).apply {
        id = "input-filter"
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
        onSelectionChanged = { category ->
            applyFilter(query = filterQuery, category = category)
        }
    }

    val clearFilterButton: Button = Button("Reset Filter", BootstrapType.OUTLINE_SECONDARY, BootstrapSize.SM).apply {
        id = "btn-clear-filter"
        onClick = {
            filterTextField.value = ""
            categorySelect.selectedValue = CATEGORY_ALL
            applyFilter(query = "", category = CATEGORY_ALL)
        }
    }

    val scrollTopButton: Button = Button("Scroll Top (0)", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-scroll-top"
        onClick = { scrollToRecordIndex(0) }
    }

    val scrollMidButton: Button = Button("Jump #5000", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-scroll-mid"
        onClick = { scrollToRecordIndex(5000) }
    }

    val scrollBottomButton: Button = Button("Jump End", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-scroll-bottom"
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

    val lazyList: LazyList = LazyList(
        orientation = Orientation.VERTICAL,
        estimatedItemExtentPx = 48f,
        overscanItems = 3
    ).apply {
        id = "explorer-lazy-list"
    }

    val lazyGrid: LazyGrid = LazyGrid(
        columns = 4,
        estimatedRowHeightPx = 120f,
        overscanRows = 2
    ).apply {
        id = "explorer-lazy-grid"
        isVisible = false
    }

    val treeView: TreeView<DatasetRecord> = TreeView<DatasetRecord>(
        estimatedRowHeightPx = 32f,
        overscanRows = 4
    ).apply {
        id = "explorer-tree-view"
        isVisible = false
        onNodeSelected = { node ->
            node.data?.let { selectRecord(it) }
        }
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("explorer-root")

        val rootStack = VStack(gap = SpacingScale.S3)

        val headerRow = HStack(gap = SpacingScale.S2).apply {
            addChild(Heading("Virtualized Data Explorer (10,000 Rows)", level = 4))
            addChild(activeModeBadge)
        }

        val controlsBar = HStack(gap = SpacingScale.S2).apply {
            addClass("explorer-toolbar")
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
            addClass("explorer-telemetry-bar")
            addChild(totalItemsBadge)
            addChild(activeWindowBadge)
            addChild(recycleStatsBadge)
            addChild(selectedItemBadge)
        }

        val viewportCard = Card("Active Virtualized Viewport").apply {
            addChild(lazyList)
            addChild(lazyGrid)
            addChild(treeView)
        }

        rootStack.addChild(headerRow)
        rootStack.addChild(controlsBar)
        rootStack.addChild(telemetryBar)
        rootStack.addChild(viewportCard)
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

        modeListButton.type = if (mode == ExplorerViewMode.LIST) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        modeGridButton.type = if (mode == ExplorerViewMode.GRID) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        modeTreeButton.type = if (mode == ExplorerViewMode.TREE) BootstrapType.PRIMARY else BootstrapType.SECONDARY
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
     * Selects a specific [DatasetRecord] and updates the selection telemetry badge.
     */
    fun selectRecord(record: DatasetRecord) {
        selectedRecord = record
        treeView.selectedNodeId = "node-record-${record.id}"
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
                children = recordsInCat.take(100).map { rec ->
                    TreeNode(
                        id = "node-record-${rec.id}",
                        label = "#${rec.id} ${rec.title} [${rec.latencyMs}ms - ${rec.status}]",
                        data = rec
                    )
                }.toMutableList()
            )
        }
        treeView.setRootNodes(roots)

        activeWindow = when (activeMode) {
            ExplorerViewMode.LIST -> lazyList.onViewportChanged(0f, lazyList.viewportExtentPx)
            ExplorerViewMode.GRID -> lazyGrid.onViewportChanged(0f, lazyGrid.viewportHeightPx)
            ExplorerViewMode.TREE -> treeView.onViewportChanged(0f, treeView.viewportHeightPx)
        }
        updateTelemetryBadges()
    }

    private fun buildListRowComponent(index: Int, record: DatasetRecord): AbstractComponent {
        val row = HStack(gap = SpacingScale.S2).apply {
            id = "record-row-${record.id}"
            addClass("explorer-row-item")
            onClick = { selectRecord(record) }
        }
        val selectBtn = Button(
            text = "#${record.id} ${record.title}",
            type = BootstrapType.OUTLINE_PRIMARY,
            size = BootstrapSize.SM
        ).apply {
            id = "btn-select-record-${record.id}"
            onClick = { selectRecord(record) }
        }
        val categoryBadge = Badge(record.category, BootstrapType.INFO, pill = true)
        val latencyLabel = Label("${record.latencyMs} ms (idx $index)")
        val statusBadge = Badge(
            record.status,
            when (record.status) {
                "HEALTHY" -> BootstrapType.SUCCESS
                "DEGRADED" -> BootstrapType.WARNING
                else -> BootstrapType.SECONDARY
            }
        )
        row.addChild(selectBtn)
        row.addChild(categoryBadge)
        row.addChild(latencyLabel)
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
