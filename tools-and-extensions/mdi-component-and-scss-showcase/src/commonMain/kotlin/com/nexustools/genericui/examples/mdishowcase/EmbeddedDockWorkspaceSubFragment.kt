package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.dock.CategorizedTreeView
import com.nexustools.genericui.component.dock.ColorSwatchBox
import com.nexustools.genericui.component.dock.DockHeaderAction
import com.nexustools.genericui.component.dock.DockPanel
import com.nexustools.genericui.component.dock.StudioDocument
import com.nexustools.genericui.component.dock.ToolPalette
import com.nexustools.genericui.component.dock.ToolPaletteItem
import com.nexustools.genericui.component.fragment.AppFragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.helper.DockWorkspaceFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.dock.DockPosition
import com.nexustools.genericui.enums.dock.StudioWindowMode
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.window.TitleBar

/**
 * Embedded [DockWorkspaceFragment] hosted inside the 6th MDI window (`"mdi-win-dock-workspace"`),
 * demonstrating a full multi-panel studio workspace (`LEFT` tools dock, `RIGHT` categorized inspector dock,
 * `BOTTOM` event console dock, and `CENTER` document tabs) running inside an [com.nexustools.genericui.component.shell.MdiWindow].
 */
@AppFragment
class EmbeddedDockWorkspaceSubFragment : DockWorkspaceFragment(
    initialMode = StudioWindowMode.SINGLE_WINDOW_MODE
) {

    val toolPalette: ToolPalette = ToolPalette(columns = 3).apply {
        id = "dock-tool-palette"
        setTools(
            listOf(
                ToolPaletteItem("tool-move", "Move", "move"),
                ToolPaletteItem("tool-brush", "Brush", "brush"),
                ToolPaletteItem("tool-pen", "Pen", "pen"),
                ToolPaletteItem("tool-gradient", "Grad", "gradient"),
                ToolPaletteItem("tool-eyedropper", "Pick", "eyedropper"),
                ToolPaletteItem("tool-zoom", "Zoom", "zoom")
            )
        )
    }

    val colorSwatchBox: ColorSwatchBox = ColorSwatchBox(
        foreground = RgbaColor.fromHex("#FF2A6D"),
        background = RgbaColor.fromHex("#00D2FF")
    ).apply {
        id = "dock-color-swatch-box"
        setPaletteSwatches(
            listOf(
                RgbaColor.fromHex("#FF2A6D"),
                RgbaColor.fromHex("#FF7B00"),
                RgbaColor.fromHex("#FFD000"),
                RgbaColor.fromHex("#00E676"),
                RgbaColor.fromHex("#00D2FF"),
                RgbaColor.fromHex("#9D4EDD")
            )
        )
    }

    val categorizedInspectorTree: CategorizedTreeView = CategorizedTreeView().apply {
        id = "dock-categorized-tree"
        addCategoryItem("Filters & Effects", "filter-bloom", "Prism Neon Bloom")
        addCategoryItem("Filters & Effects", "filter-chromatic", "Chromatic Aberration")
        addCategoryItem("Layers", "layer-vector-ui", "Vector UI Overlay")
        addCategoryItem("Layers", "layer-background", "Rainbow Gradient Backdrop")
        addCategoryItem("Shared Objects", "obj-button-rig", "Reusable Button Component")
        selectItem("filter-bloom")
        addChild(Label("v Filters: Prism Neon Bloom"))
        addChild(Label("v Layers: Vector UI Overlay"))
    }

    val toolsDockPanel: DockPanel = DockPanel(
        panelId = "dock-panel-tools",
        title = "Studio Tools & Swatches",
        preferredPosition = DockPosition.LEFT
    ).apply {
        id = "dock-panel-tools"
    }

    val inspectorDockPanel: DockPanel = DockPanel(
        panelId = "dock-panel-inspector",
        title = "Inspector & Filter Presets",
        preferredPosition = DockPosition.LEFT
    ).apply {
        id = "dock-panel-inspector"
    }

    val consoleDockPanel: DockPanel = DockPanel(
        panelId = "dock-panel-console",
        title = "Studio Event Console",
        preferredPosition = DockPosition.TOP
    ).apply {
        id = "dock-panel-console"
    }

    val toggleModeButton: Button = Button(
        text = "Toggle Mode",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-dock-toggle-mode"
    }

    val addTabButton: Button = Button(
        text = "+ Canvas Tab",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-dock-add-tab"
    }

    val windowModeBadge: Badge = Badge(
        text = StudioWindowMode.SINGLE_WINDOW_MODE.name,
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "badge-dock-window-mode"
    }

    val tabCountBadge: Badge = Badge(
        text = "2 tabs",
        type = BootstrapType.SECONDARY,
        pill = true
    ).apply {
        id = "badge-dock-tab-count"
    }

    val consoleLogLabel: Label = Label("Studio workspace initialized inside MDI window.").apply {
        id = "label-dock-console-log"
    }

    private var createdTabCounter: Int = 2

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        id = "embedded-dock-workspace-fragment"
        dockWorkspace.id = "embedded-dock-workspace"

        // Configure LEFT DockPanels with custom TitleBars, ToolPalette, ColorSwatchBox, and CategorizedTreeView
        val toolsTitleBar = TitleBar(showWindowTitle = true, customTitleBar = true)
            .addLeading(Badge("TOOLS", BootstrapType.PRIMARY, pill = true))
            .addTrailing(Badge("6", BootstrapType.SECONDARY, pill = true))
        toolsDockPanel.setTitleBar(toolsTitleBar)
        toolsDockPanel.addHeaderAction(
            DockHeaderAction("swap-colors", "Swap Swatches", "swap") {
                colorSwatchBox.swapColors()
                consoleLogLabel.text = "Swapped foreground/background swatches."
            }
        )
        val toolsRow = HStack(gap = SpacingScale.S1).apply {
            heightPx = 56
            flexWrap = FlexWrap.WRAP
            toolPalette.widthPx = 122
            toolPalette.heightPx = 56
            colorSwatchBox.widthPx = 56
            colorSwatchBox.heightPx = 56
            addChild(toolPalette)
            addChild(colorSwatchBox)
        }
        toolsDockPanel.addChild(toolsRow)
        addDockPanel(toolsDockPanel, DockPosition.LEFT)

        val inspectorTitleBar = TitleBar(showWindowTitle = true, customTitleBar = true)
            .addLeading(Badge("INSPECTOR", BootstrapType.INFO, pill = true))
        inspectorDockPanel.setTitleBar(inspectorTitleBar)
        inspectorDockPanel.addChild(categorizedInspectorTree)
        addDockPanel(inspectorDockPanel, DockPosition.LEFT)

        // Configure TOP DockPanel with studio mode controls and live console log
        val consoleTitleBar = TitleBar(showWindowTitle = true, customTitleBar = true)
            .addLeading(Badge("CONSOLE", BootstrapType.SUCCESS, pill = true))
        consoleDockPanel.setTitleBar(consoleTitleBar)
        val consoleControlsRow = HStack(gap = SpacingScale.S1).apply {
            heightPx = 24
            flexWrap = FlexWrap.WRAP
            addChild(toggleModeButton)
            addChild(addTabButton)
            addChild(windowModeBadge)
            addChild(tabCountBadge)
        }
        consoleDockPanel.addChild(consoleControlsRow)
        consoleDockPanel.addChild(consoleLogLabel)
        addDockPanel(consoleDockPanel, DockPosition.TOP)

        // Open initial StudioDocuments in the central TabContainer
        openStudioCanvasDocument("doc-main-canvas", "Main Vector Canvas.svg", activate = true)
        openStudioCanvasDocument("doc-shared-symbol", "Shared Prism Symbol.svg", activate = false)
        syncBadges()

        toggleModeButton.setOnClick {
            toggleWindowMode()
            syncBadges()
            consoleLogLabel.text = "Switched studio mode to ${windowMode.name}."
        }

        addTabButton.setOnClick {
            createdTabCounter += 1
            val newDocId = "doc-canvas-$createdTabCounter"
            val newTitle = "Canvas #$createdTabCounter.svg"
            openStudioCanvasDocument(newDocId, newTitle, activate = true)
            syncBadges()
            consoleLogLabel.text = "Opened studio document '$newTitle' (${documents.size} total)."
        }

        toolPalette.onToolSelected = { toolId ->
            consoleLogLabel.text = "Selected studio tool: $toolId"
        }

        categorizedInspectorTree.onItemSelected = { itemId ->
            consoleLogLabel.text = "Selected inspector node: $itemId"
        }
    }

    fun openStudioCanvasDocument(docId: String, title: String, activate: Boolean = true): StudioDocument {
        val card = Card(title).apply {
            id = "studio-card-$docId"
            addChild(Label("Active Viewport Document: $title"))
            addChild(Badge("Hybrid Raster + Vector", BootstrapType.PRIMARY, pill = true))
        }
        val doc = StudioDocument(
            id = docId,
            title = title,
            content = card,
            isDirty = false,
            isClosable = true
        )
        openDocument(doc, activate = activate)
        syncBadges()
        return doc
    }

    private fun syncBadges() {
        windowModeBadge.text = windowMode.name
        windowModeBadge.getBootstrap().type =
            if (windowMode == StudioWindowMode.SINGLE_WINDOW_MODE) BootstrapType.PRIMARY else BootstrapType.WARNING
        tabCountBadge.text = "${documents.size} tabs"
    }
}
