package com.nexustools.genericui.examples.dockstudio

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.dock.DockHeaderAction
import com.nexustools.genericui.component.dock.DockPanel
import com.nexustools.genericui.component.dock.StudioDocument as DockStudioDocument
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.fragment.helper.OverlayDockWorkspaceFragment
import com.nexustools.genericui.component.layout.Container
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.dock.DockPosition
import com.nexustools.genericui.enums.dock.StudioWindowMode
import com.nexustools.genericui.ext.dsl.bootstrap
import com.nexustools.genericui.ext.dsl.style
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.window.TitleBar

/**
 * Modular studio workspace extending [OverlayDockWorkspaceFragment] (and [com.nexustools.genericui.component.fragment.helper.DockWorkspaceFragment]).
 *
 * Demonstrates:
 * - `LEFT` Toolbox [DockPanel], `RIGHT` Properties [DockPanel], and `BOTTOM` Console [DockPanel] with custom [TitleBar] configurations.
 * - `CENTER` [com.nexustools.genericui.component.tabs.TabContainer] hosting [com.nexustools.genericui.component.tabs.CloseableTab] document tabs.
 * - Live switching between [StudioWindowMode.SINGLE_WINDOW_MODE] (`SINGLE_WINDOW`) and [StudioWindowMode.MULTI_WINDOW_MODE] (`MULTI_WINDOW`) with [com.nexustools.genericui.component.dock.PanelWindow] detachment.
 * - Floating HUD overlay panel toggled over the workspace canvas.
 */
@MainAppFragment
class DockWorkspaceStudioFragment : OverlayDockWorkspaceFragment(
    backgroundComponent = Container("studio-canvas-backdrop"),
    initialMode = StudioWindowMode.SINGLE_WINDOW_MODE
) {

    private val _studioModels = linkedMapOf<String, StudioDocument>()
    val studioDocuments: List<StudioDocument>
        get() = _studioModels.values.toList()

    private var nextDocumentSequence: Int = 3

    var activeToolName: String = "Move"
        private set

    var isHudOverlayVisible: Boolean = true
        private set

    private val consoleEvents = mutableListOf<String>()

    val newDocTabButton: Button = Button("New Document Tab", BootstrapType.PRIMARY, BootstrapSize.SM).apply {
        id = "btn-new-doc-tab"
        bootstrap {
            type = BootstrapType.PRIMARY
            size = BootstrapSize.SM
            fontWeight = FontWeight.SEMIBOLD
        }
        style {
            accentColor = RgbaColor.fromHex("#0EA5E9")
        }
        onClick = { createAndOpenNewDocument() }
    }

    val toggleWindowModeButton: Button = Button("Toggle Single/Multi Window", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-toggle-window-mode"
        bootstrap {
            type = BootstrapType.INFO
            size = BootstrapSize.SM
        }
        onClick = {
            toggleWindowMode()
            syncStudioStatus()
        }
    }

    val toggleHudOverlayButton: Button = Button("Toggle HUD Overlay", BootstrapType.WARNING, BootstrapSize.SM).apply {
        id = "btn-toggle-hud-overlay"
        bootstrap {
            type = BootstrapType.WARNING
            size = BootstrapSize.SM
        }
        onClick = { toggleHudOverlay() }
    }

    val closeActiveDocButton: Button = Button("Close Active Tab", BootstrapType.DANGER, BootstrapSize.SM, outline = true).apply {
        id = "btn-close-active-doc"
        bootstrap {
            type = BootstrapType.DANGER
            size = BootstrapSize.SM
            outline = true
        }
        onClick = { closeActiveStudioDocument() }
    }

    val windowModeBadge: Badge = Badge("SINGLE_WINDOW", BootstrapType.PRIMARY, pill = true).apply {
        id = "badge-window-mode"
    }

    val openTabsBadge: Badge = Badge("Tabs: 0", BootstrapType.SUCCESS, pill = true).apply {
        id = "badge-open-tabs"
    }

    val activeDocBadge: Badge = Badge("Active: None", BootstrapType.INFO, pill = true).apply {
        id = "badge-active-doc"
    }

    val activeToolBadge: Badge = Badge("Tool: Move", BootstrapType.SECONDARY, pill = true).apply {
        id = "badge-active-tool"
    }

    val toolboxPanel: DockPanel = DockPanel(
        panelId = "panel-toolbox",
        title = "Toolbox",
        preferredPosition = DockPosition.LEFT
    ).apply {
        id = "dock-panel-toolbox"
        setTitleBar(
            TitleBar(showWindowTitle = true).apply {
                addTrailing(Badge("Tools", BootstrapType.PRIMARY, pill = true))
            }
        )
    }

    val propertiesPanel: DockPanel = DockPanel(
        panelId = "panel-properties",
        title = "Properties Inspector",
        preferredPosition = DockPosition.RIGHT
    ).apply {
        id = "dock-panel-properties"
        setTitleBar(
            TitleBar(showWindowTitle = true).apply {
                addTrailing(Badge("Inspector", BootstrapType.INFO, pill = true))
            }
        )
        addHeaderAction(
            DockHeaderAction(
                id = "action-reset-zoom",
                label = "100%",
                tooltip = "Reset Zoom to 100%",
                onClick = { updateActiveDocumentZoom(100) }
            )
        )
    }

    val consolePanel: DockPanel = DockPanel(
        panelId = "panel-console",
        title = "Studio Console",
        preferredPosition = DockPosition.BOTTOM
    ).apply {
        id = "dock-panel-console"
        setTitleBar(
            TitleBar(showWindowTitle = true).apply {
                addTrailing(Badge("Logs", BootstrapType.SECONDARY, pill = true))
            }
        )
    }

    val hudOverlayPanel: DockPanel = DockPanel(
        panelId = "panel-hud-overlay",
        title = "Floating Viewport HUD",
        preferredPosition = DockPosition.FLOATING
    ).apply {
        id = "hud-overlay-panel"
        setTitleBar(
            TitleBar(showWindowTitle = true).apply {
                addTrailing(Badge("HUD", BootstrapType.WARNING, pill = true))
            }
        )
    }

    val propDocTitleLabel: Label = Label("Document: None").apply {
        id = "label-prop-doc-title"
    }

    val propLayersLabel: Label = Label("Layers: 0").apply {
        id = "label-prop-layers"
    }

    val propZoomLabel: Label = Label("Zoom: 100%").apply {
        id = "label-prop-zoom"
    }

    val propNotesLabel: Label = Label("Notes: -").apply {
        id = "label-prop-notes"
    }

    val consoleLastEventLabel: Label = Label("Studio initialized.").apply {
        id = "label-console-last-event"
    }

    val hudTelemetryLabel: Label = Label("HUD: Ready").apply {
        id = "label-hud-telemetry"
    }

    val hudStatusBadge: Badge = Badge("HUD: VISIBLE", BootstrapType.SUCCESS, pill = true).apply {
        id = "badge-hud-status"
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)

        // Build top toolbar above dock areas
        val studioToolbar = HStack(gap = SpacingScale.S2).apply {
            id = "studio-top-toolbar"
            addClass("studio-toolbar")
            bootstrap {
                padding = SpacingScale.S2
                borderRadius = BorderRadiusStyle.SMALL
            }
            addChild(Heading("Dock Workspace Studio", level = 5))
            addChild(newDocTabButton)
            addChild(closeActiveDocButton)
            addChild(toggleWindowModeButton)
            addChild(toggleHudOverlayButton)
            addChild(windowModeBadge)
            addChild(openTabsBadge)
            addChild(activeDocBadge)
            addChild(activeToolBadge)
        }
        dockWorkspace.topArea.addChild(studioToolbar)

        populateToolboxPanel()
        populatePropertiesPanel()
        populateConsolePanel()
        populateHudOverlayPanel()

        addDockPanel(toolboxPanel, DockPosition.LEFT)
        addDockPanel(propertiesPanel, DockPosition.RIGHT)
        addDockPanel(consolePanel, DockPosition.BOTTOM)
        registerOverlayPanel(hudOverlayPanel, DockPosition.FLOATING, xOffsetPx = 280, yOffsetPx = 64)

        addOnActiveDocumentChangedListener {
            syncStudioStatus()
        }
        addOnWindowModeChangedListener { mode ->
            logConsoleEvent("Switched workspace mode to ${formatWindowModeLabel(mode)}")
            syncStudioStatus()
        }

        // Open 2 initial studio documents
        openStudioDocumentModel(
            StudioDocument(
                id = "doc-1",
                title = "Hero Banner.gul",
                layerCount = 6,
                zoomPercent = 100,
                notes = "Primary hero illustration with vector & raster layers."
            ),
            activate = true
        )
        openStudioDocumentModel(
            StudioDocument(
                id = "doc-2",
                title = "Icon Sheet.gul",
                layerCount = 12,
                zoomPercent = 200,
                notes = "Multi-resolution SVG and pixel icon atlas."
            ),
            activate = false
        )
        activateDocument("doc-1")
        syncStudioStatus()
    }

    /**
     * Creates and opens a new [StudioDocument] tab in the central [com.nexustools.genericui.component.tabs.TabContainer].
     */
    fun createAndOpenNewDocument(): StudioDocument {
        val seq = nextDocumentSequence++
        val doc = StudioDocument(
            id = "doc-$seq",
            title = "Untitled-$seq.gul",
            layerCount = 3,
            zoomPercent = 100,
            notes = "Newly created studio canvas #$seq."
        )
        openStudioDocumentModel(doc, activate = true)
        logConsoleEvent("Opened new document tab '${doc.title}' (${doc.id})")
        return doc
    }

    /**
     * Opens a [StudioDocument] model in the [DockWorkspaceFragment] and registers its tab content.
     */
    fun openStudioDocumentModel(model: StudioDocument, activate: Boolean = true) {
        _studioModels[model.id] = model
        val contentPane = buildDocumentCanvasPane(model)
        val dockDoc = DockStudioDocument(
            id = model.id,
            title = model.title,
            content = contentPane,
            isDirty = false,
            isClosable = true
        )
        dockWorkspace.addDocument(dockDoc)
        openDocument(dockDoc, activate = activate)
        documentTabContainer.isVisible = true
        syncStudioStatus()
    }

    /**
     * Closes a studio document by [documentId] and updates the open tab count badge.
     */
    fun closeStudioDocumentById(documentId: String): Boolean {
        val closed = closeDocument(documentId)
        if (closed) {
            _studioModels.remove(documentId)
            dockWorkspace.removeDocument(documentId)
            documentTabContainer.isVisible = true
            logConsoleEvent("Closed document '$documentId'")
            syncStudioStatus()
        }
        return closed
    }

    /**
     * Closes whichever document tab is currently active.
     */
    fun closeActiveStudioDocument(): Boolean {
        val currentId = activeDocument?.documentId ?: return false
        return closeStudioDocumentById(currentId)
    }

    /**
     * Selects a tool in the `LEFT` Toolbox [DockPanel].
     */
    fun selectTool(toolName: String) {
        activeToolName = toolName
        activeToolBadge.text = "Tool: $toolName"
        logConsoleEvent("Selected tool: $toolName")
        syncStudioStatus()
    }

    /**
     * Updates the zoom percentage of the active [StudioDocument].
     */
    fun updateActiveDocumentZoom(newZoomPercent: Int) {
        val activeId = activeDocument?.documentId ?: return
        val existing = _studioModels[activeId] ?: return
        val clamped = newZoomPercent.coerceIn(25, 800)
        _studioModels[activeId] = existing.copy(zoomPercent = clamped)
        logConsoleEvent("Updated '${existing.title}' zoom to ${clamped}%")
        syncStudioStatus()
    }

    /**
     * Adds a layer to the active [StudioDocument] and updates the `RIGHT` Properties [DockPanel].
     */
    fun addLayerToActiveDocument() {
        val activeId = activeDocument?.documentId ?: return
        val existing = _studioModels[activeId] ?: return
        val updated = existing.copy(layerCount = existing.layerCount + 1)
        _studioModels[activeId] = updated
        logConsoleEvent("Added layer to '${updated.title}' (total ${updated.layerCount} layers)")
        syncStudioStatus()
    }

    /**
     * Toggles the visibility of the floating HUD overlay panel.
     */
    fun toggleHudOverlay(): Boolean {
        isHudOverlayVisible = !isHudOverlayVisible
        hudOverlayPanel.isVisible = isHudOverlayVisible
        hudStatusBadge.text = if (isHudOverlayVisible) "HUD: VISIBLE" else "HUD: HIDDEN"
        hudStatusBadge.getBootstrap().type = if (isHudOverlayVisible) BootstrapType.SUCCESS else BootstrapType.SECONDARY
        logConsoleEvent("Floating HUD overlay visibility: $isHudOverlayVisible")
        return isHudOverlayVisible
    }

    fun getActiveStudioDocumentModel(): StudioDocument? =
        activeDocument?.documentId?.let { _studioModels[it] }

    private fun populateToolboxPanel() {
        val toolsStack = VStack(gap = SpacingScale.S2)
        val tools = listOf(
            "Move" to "btn-tool-move",
            "Brush" to "btn-tool-brush",
            "Vector Path" to "btn-tool-vector",
            "Zoom" to "btn-tool-zoom"
        )
        for ((toolLabel, btnId) in tools) {
            val btn = Button(toolLabel, BootstrapType.PRIMARY, BootstrapSize.SM, outline = true).apply {
                id = btnId
                onClick = { selectTool(toolLabel) }
            }
            toolsStack.addChild(btn)
        }
        toolboxPanel.addChild(toolsStack)
    }

    private fun populatePropertiesPanel() {
        val propsStack = VStack(gap = SpacingScale.S2)
        propsStack.addChild(propDocTitleLabel)
        propsStack.addChild(propLayersLabel)
        propsStack.addChild(propZoomLabel)
        propsStack.addChild(propNotesLabel)

        val actionRow = HStack(gap = SpacingScale.S2).apply {
            val zoomInBtn = Button("Zoom +25%", BootstrapType.INFO, BootstrapSize.SM, outline = true).apply {
                id = "btn-zoom-in"
                onClick = {
                    val current = getActiveStudioDocumentModel()?.zoomPercent ?: 100
                    updateActiveDocumentZoom(current + 25)
                }
            }
            val zoomOutBtn = Button("Zoom -25%", BootstrapType.SECONDARY, BootstrapSize.SM, outline = true).apply {
                id = "btn-zoom-out"
                onClick = {
                    val current = getActiveStudioDocumentModel()?.zoomPercent ?: 100
                    updateActiveDocumentZoom(current - 25)
                }
            }
            val addLayerBtn = Button("+ Add Layer", BootstrapType.SUCCESS, BootstrapSize.SM).apply {
                id = "btn-add-layer"
                onClick = { addLayerToActiveDocument() }
            }
            addChild(zoomInBtn)
            addChild(zoomOutBtn)
            addChild(addLayerBtn)
        }
        propsStack.addChild(actionRow)
        propertiesPanel.addChild(propsStack)
    }

    private fun populateConsolePanel() {
        val consoleStack = HStack(gap = SpacingScale.S2).apply {
            addChild(Badge("CONSOLE", BootstrapType.INFO))
            addChild(consoleLastEventLabel)
            addChild(hudStatusBadge)
        }
        consolePanel.addChild(consoleStack)
    }

    private fun populateHudOverlayPanel() {
        val hudContent = VStack(gap = SpacingScale.S1).apply {
            addClass("studio-hud-overlay")
            addChild(hudTelemetryLabel)
        }
        hudOverlayPanel.addChild(hudContent)
    }

    private fun buildDocumentCanvasPane(model: StudioDocument): Container {
        val pane = VStack(gap = SpacingScale.S2).apply {
            id = "doc-canvas-${model.id}"
            addClass("studio-doc-surface")
        }
        val card = Card(model.title).apply {
            bootstrap {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.S2
            }
            addChild(Label("Document ID: ${model.id}"))
            addChild(Label("Initial Layers: ${model.layerCount} | Initial Zoom: ${model.zoomPercent}%"))
            addChild(Label(model.notes))
            val activateBtn = Button(
                text = "Focus ${model.title}",
                type = BootstrapType.PRIMARY,
                size = BootstrapSize.SM,
                outline = true
            ).apply {
                id = "btn-focus-${model.id}"
                onClick = {
                    activateDocument(model.id)
                    syncStudioStatus()
                }
            }
            addChild(activateBtn)
        }
        pane.addChild(card)
        return pane
    }

    private fun logConsoleEvent(message: String) {
        consoleEvents.add(message)
        consoleLastEventLabel.text = message
    }

    private fun formatWindowModeLabel(mode: StudioWindowMode): String =
        if (mode == StudioWindowMode.SINGLE_WINDOW_MODE) "SINGLE_WINDOW" else "MULTI_WINDOW"

    private fun syncStudioStatus() {
        windowModeBadge.text = formatWindowModeLabel(windowMode)
        openTabsBadge.text = "Tabs: ${documents.size}"

        val activeModel = getActiveStudioDocumentModel()
        if (activeModel != null) {
            activeDocBadge.text = "Active: ${activeModel.title} (${activeModel.zoomPercent}%)"
            propDocTitleLabel.text = "Document: ${activeModel.title}"
            propLayersLabel.text = "Layers: ${activeModel.layerCount}"
            propZoomLabel.text = "Zoom: ${activeModel.zoomPercent}%"
            propNotesLabel.text = "Notes: ${activeModel.notes}"
            hudTelemetryLabel.text =
                "HUD | ${activeModel.title} | ${activeModel.layerCount} Layers @ ${activeModel.zoomPercent}% | Tool: $activeToolName"
        } else {
            activeDocBadge.text = "Active: None"
            propDocTitleLabel.text = "Document: None"
            propLayersLabel.text = "Layers: 0"
            propZoomLabel.text = "Zoom: -"
            propNotesLabel.text = "Notes: -"
            hudTelemetryLabel.text = "HUD | No active document | Tool: $activeToolName"
        }
    }
}
