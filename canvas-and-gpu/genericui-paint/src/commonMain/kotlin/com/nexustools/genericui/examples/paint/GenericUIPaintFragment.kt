package com.nexustools.genericui.examples.paint

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.dialog.Dialogs
import com.nexustools.genericui.component.dialog.FileTypeFilter
import com.nexustools.genericui.component.form.ColorPicker
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.htmlcompat.Div
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.media.Canvas2D
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.css.CssCursor
import com.nexustools.genericui.enums.css.CssDisplay
import com.nexustools.genericui.event.PointerEvent
import com.nexustools.genericui.ext.canvas2d.dsl.canvas2D
import com.nexustools.genericui.ext.dsl.badge
import com.nexustools.genericui.ext.dsl.build
import com.nexustools.genericui.ext.dsl.button
import com.nexustools.genericui.ext.dsl.component
import com.nexustools.genericui.ext.dsl.div
import com.nexustools.genericui.ext.dsl.hstack
import com.nexustools.genericui.ext.dsl.label
import com.nexustools.genericui.ext.dsl.rangeSlider
import com.nexustools.genericui.ext.dsl.scrollView
import com.nexustools.genericui.ext.dsl.select
import com.nexustools.genericui.ext.dsl.vstack
import com.nexustools.genericui.ext.imageprocessing.context2d.ImageBuffer2DContext
import com.nexustools.genericui.image.ImageBuffer
import com.nexustools.genericui.io.Readable
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.Writable
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.style.css.CssCornerRadii
import com.nexustools.genericui.style.css.CssCustomCursor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Windows 11 Dark-Mode Paint (`Untitled - Paint`) clone built with `:extensions:canvas2d` ([Canvas2D]),
 * `:extensions:imageprocessing` ([ImageBuffer2DContext]), `:extensions:dsl`, [Writable] / [Readable]
 * binary project serialization, and `getPreferenceStore()` persistence.
 */
@MainAppFragment
class GenericUIPaintFragment : Fragment() {

    private val _layers = mutableListOf(
        PaintLayer(layerId = "layer-1", name = "Layer 1", visible = true, opacityPercent = 100)
    )
    val layers: List<PaintLayer>
        get() = _layers.toList()

    var activeLayerIndex: Int = 0
        private set

    val activeLayer: PaintLayer
        get() = _layers[activeLayerIndex.coerceIn(0, _layers.lastIndex)]

    private val _redoStack = mutableListOf<Pair<Int, PaintStrokeCommand>>()
    val redoCount: Int
        get() = _redoStack.size

    val totalStrokeCount: Int
        get() = _layers.sumOf { it.strokes.size }

    var canvasWidthPx: Int = 131
        private set

    var canvasHeightPx: Int = 128
        private set

    var activeTool: PaintTool = PaintTool.BRUSH
        private set

    var activeBrushStyle: PaintBrushStyle = PaintBrushStyle.BRUSH
        private set

    var activeShapeType: PaintShapeType = PaintShapeType.RECTANGLE
        private set

    var shapeOutlineEnabled: Boolean = true
        private set

    var shapeFillEnabled: Boolean = false
        private set

    var foregroundHex: String = "#000000"
        private set

    var backgroundHex: String = "#FFFFFF"
        private set

    /**
     * When `true`, clicking a palette swatch sets [foregroundHex] (Color 1);
     * when `false`, clicking a palette swatch sets [backgroundHex] (Color 2).
     */
    var editingForegroundColor: Boolean = true
        private set

    var brushSizePx: Int = 8
        private set

    var brushOpacityPercent: Int = 100
        private set

    var zoomPercent: Int = 100
        private set

    var layersPanelOpen: Boolean = false
        private set

    var lastCursorX: Int = 0
        private set

    var lastCursorY: Int = 0
        private set

    var selectionDimensionsText: String = "No selection"
        private set

    var renderedFrameCount: Long = 0L
        private set

    private var activePointerPoints: MutableList<Pair<Float, Float>>? = null
    private var nextLayerNumber: Int = 2
    private var copilotSeedCounter: Int = 0

    val defaultPaletteHex: List<String> = listOf(
        // Row 1: 10 standard Windows 11 Paint dark/vibrant swatches
        "#000000", "#7F7F7F", "#880015", "#ED1C24", "#FF7F27",
        "#FFF200", "#22B14C", "#00A2E8", "#3F48CC", "#A349A4",
        // Row 2: 10 standard Windows 11 Paint light/pastel swatches
        "#FFFFFF", "#C3C3C3", "#B97A57", "#FFAEC9", "#FFC90E",
        "#EFE4B0", "#B5E61D", "#99D9EA", "#7092BE", "#C8BFE7"
    )

    private val _customPaletteHex = MutableList(10) { index ->
        if (index == 0) "#38BDF8" else "#27272C"
    }
    val customPaletteHex: List<String>
        get() = _customPaletteHex.toList()

    // Top Command Bar Controls
    lateinit var fileMenuButton: Button
    lateinit var editMenuButton: Button
    lateinit var viewMenuButton: Button
    lateinit var saveButton: Button
    lateinit var exportButton: Button
    lateinit var undoButton: Button
    lateinit var redoButton: Button
    lateinit var statusMessageLabel: Label

    // Ribbon Controls
    lateinit var selectionToolButton: Button
    lateinit var cropButton: Button
    lateinit var rotateButton: Button
    lateinit var flipButton: Button
    lateinit var removeBgButton: Button

    lateinit var pencilToolButton: Button
    lateinit var fillToolButton: Button
    lateinit var textToolButton: Button
    lateinit var eraserToolButton: Button
    lateinit var colorPickerToolButton: Button
    lateinit var magnifierToolButton: Button

    lateinit var brushToolButton: Button
    lateinit var brushStyleSelect: Select

    private val shapeButtons = linkedMapOf<PaintShapeType, Button>()
    lateinit var shapeOutlineButton: Button
    lateinit var shapeFillButton: Button

    lateinit var foregroundSwatchButton: Button
    lateinit var backgroundSwatchButton: Button
    private val paletteButtons = mutableListOf<Button>()
    private val customSwatchButtons = mutableListOf<Button>()
    lateinit var editColorsButton: Button
    val customColorPicker: ColorPicker = ColorPicker(RgbaColor.fromHex("#38BDF8")).apply {
        id = "picker-custom-color"
        widthPx = 56
        heightPx = 28
    }

    lateinit var copilotButton: Button
    lateinit var layersToggleButton: Button

    // Workspace Controls
    lateinit var brushSizeSlider: RangeSlider
    lateinit var brushSizeBadge: Badge
    lateinit var brushOpacitySlider: RangeSlider
    lateinit var brushOpacityBadge: Badge
    lateinit var paintCanvas: Canvas2D
    lateinit var layersSidebar: Div
    lateinit var layersListContainer: VStack
    lateinit var addLayerButton: Button
    lateinit var toggleLayerVisibilityButton: Button
    lateinit var deleteLayerButton: Button
    lateinit var mergeDownLayerButton: Button

    // Bottom Status Bar Controls
    lateinit var cursorPositionLabel: Label
    lateinit var selectionSizeLabel: Label
    lateinit var canvasSizeBadge: Badge
    lateinit var activeToolBadge: Badge
    lateinit var fitToWindowButton: Button
    lateinit var zoomSelect: Select
    lateinit var zoomOutButton: Button
    lateinit var zoomSlider: RangeSlider
    lateinit var zoomInButton: Button

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        PaintPreferences.registerFactory()
        addClass("paint-root")
        loadPreferencesFromStore()
        buildUi()
        syncAllControls()
    }

    private fun buildUi() {
        val self = this
        build(clearFirst = true) {
            vstack(classes = "paint-main-layout gap-0") {
                // 1. Top Command Row (File, Edit, View + Save/Export/Undo/Redo + Feedback/Profile/Settings)
                hstack(classes = "paint-command-bar justify-content-between align-items-center", id = "paint-command-bar") {
                    heightPx = 32
                    gapPx = 6

                    hstack(classes = "gap-1 align-items-center") {
                        widthPx = 468
                        heightPx = 28
                        gapPx = 4

                        self.fileMenuButton = button(
                            text = "File",
                            type = BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            id = "btn-menu-file",
                            onClick = { self.handleNewCanvas() }
                        ) { widthPx = 46; heightPx = 24 }

                        self.editMenuButton = button(
                            text = "Edit",
                            type = BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            id = "btn-menu-edit",
                            onClick = { self.clearActiveLayer() }
                        ) { widthPx = 46; heightPx = 24 }

                        self.viewMenuButton = button(
                            text = "View",
                            type = BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            id = "btn-menu-view",
                            onClick = { self.fitToWindow() }
                        ) { widthPx = 48; heightPx = 24 }

                        label(text = "|", classes = "mx-1") { widthPx = 14; heightPx = 18 }

                        self.saveButton = button(
                            text = "Save",
                            tooltipText = "Save preferences & project",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-cmd-save",
                            onClick = { self.savePreferencesToStore() }
                        ) { widthPx = 54; heightPx = 24 }

                        self.exportButton = button(
                            text = "Export",
                            tooltipText = "Export PNG",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-cmd-export",
                            onClick = { self.promptExportPng() }
                        ) { widthPx = 62; heightPx = 24 }

                        self.undoButton = button(
                            text = "< Undo",
                            tooltipText = "Undo (Ctrl+Z)",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-undo",
                            onClick = { self.undo() }
                        ) { widthPx = 60; heightPx = 24 }

                        self.redoButton = button(
                            text = "Redo >",
                            tooltipText = "Redo (Ctrl+Y)",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-redo",
                            onClick = { self.redo() }
                        ) { widthPx = 60; heightPx = 24 }
                    }

                    vstack(classes = "align-items-center justify-content-center") {
                        heightPx = 24
                        self.statusMessageLabel = label(
                            text = "Untitled - Paint  |  131 × 128px Workspace",
                            classes = "mb-0",
                            id = "label-paint-status"
                        ) { heightPx = 18 }
                    }

                    hstack(classes = "gap-1 align-items-center justify-content-end") {
                        widthPx = 228
                        heightPx = 28
                        gapPx = 4

                        button(
                            text = "Feedback",
                            tooltipText = "Feedback",
                            type = BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            id = "btn-cmd-feedback",
                            onClick = { self.statusMessageLabel.text = "Feedback ready" }
                        ) { widthPx = 74; heightPx = 24 }

                        button(
                            text = "Profile",
                            tooltipText = "Profile",
                            type = BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            id = "btn-cmd-profile",
                            onClick = { self.statusMessageLabel.text = "Signed in to GenericUIPaint" }
                        ) { widthPx = 66; heightPx = 24 }

                        button(
                            text = "Settings",
                            tooltipText = "Settings",
                            type = BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            id = "btn-cmd-settings",
                            onClick = { self.savePreferencesToStore() }
                        ) { widthPx = 74; heightPx = 24 }
                    }
                }

                // 2. Grouped Windows 11 Paint Ribbon Bar
                hstack(classes = "paint-ribbon-bar gap-2 align-items-stretch flex-wrap", id = "paint-ribbon-bar") {
                    heightPx = 104
                    gapPx = 6

                    // Selection Group
                    div(classes = "paint-ribbon-group d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 102
                        heightPx = 100

                        self.selectionToolButton = button(
                            text = "Select ▾",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-tool-selection",
                            onClick = { self.selectTool(PaintTool.SELECTION) }
                        ) { widthPx = 94; heightPx = 32 }

                        label(text = "Rect / Free", classes = "paint-ribbon-section-label") {
                            widthPx = 94
                            heightPx = 32
                        }

                        label(text = "Selection", classes = "paint-ribbon-section-label") {
                            widthPx = 94
                            heightPx = 16
                        }
                    }

                    // Image Group
                    div(classes = "paint-ribbon-group d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 152
                        heightPx = 100

                        hstack(classes = "gap-1 align-items-center") {
                            widthPx = 144
                            heightPx = 32
                            gapPx = 4

                            self.cropButton = button(
                                text = "Crop",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-image-crop",
                                onClick = { self.cropToSelectionOrTrim() }
                            ) { widthPx = 64; heightPx = 28 }

                            self.rotateButton = button(
                                text = "Rotate ▾",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-image-rotate",
                                onClick = { self.rotateCanvas90Clockwise() }
                            ) { widthPx = 74; heightPx = 28 }
                        }

                        hstack(classes = "gap-1 align-items-center") {
                            widthPx = 144
                            heightPx = 32
                            gapPx = 4

                            self.flipButton = button(
                                text = "Flip ▾",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-image-flip",
                                onClick = { self.flipCanvasHorizontal() }
                            ) { widthPx = 64; heightPx = 28 }

                            self.removeBgButton = button(
                                text = "Clean BG",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-image-remove-bg",
                                onClick = { self.removeBackground() }
                            ) { widthPx = 74; heightPx = 28 }
                        }

                        label(text = "Image", classes = "paint-ribbon-section-label") {
                            widthPx = 144
                            heightPx = 16
                        }
                    }

                    // Tools Group (2x3 Grid: Pencil, Fill Bucket, Text 'A', Eraser, Color Picker, Magnifier)
                    div(classes = "paint-ribbon-group d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 154
                        heightPx = 100

                        hstack(classes = "paint-tools-grid gap-1") {
                            widthPx = 146
                            heightPx = 32
                            gapPx = 4

                            self.pencilToolButton = button(
                                text = "Pen",
                                tooltipText = "Pencil",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-tool-pencil",
                                onClick = { self.selectTool(PaintTool.PENCIL) }
                            ) { widthPx = 45; heightPx = 28 }

                            self.fillToolButton = button(
                                text = "Fill",
                                tooltipText = "Fill with color",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-tool-fill",
                                onClick = { self.selectTool(PaintTool.FILL_BUCKET) }
                            ) { widthPx = 45; heightPx = 28 }

                            self.textToolButton = button(
                                text = "Text",
                                tooltipText = "Text",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-tool-text",
                                onClick = { self.selectTool(PaintTool.TEXT) }
                            ) { widthPx = 45; heightPx = 28 }
                        }

                        hstack(classes = "paint-tools-grid gap-1") {
                            widthPx = 146
                            heightPx = 32
                            gapPx = 4

                            self.eraserToolButton = button(
                                text = "Erase",
                                tooltipText = "Eraser",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-tool-eraser",
                                onClick = { self.selectTool(PaintTool.ERASER) }
                            ) { widthPx = 45; heightPx = 28 }

                            self.colorPickerToolButton = button(
                                text = "Pick",
                                tooltipText = "Color picker",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-tool-picker",
                                onClick = { self.selectTool(PaintTool.COLOR_PICKER) }
                            ) { widthPx = 45; heightPx = 28 }

                            self.magnifierToolButton = button(
                                text = "Zoom",
                                tooltipText = "Magnifier",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-tool-magnifier",
                                onClick = { self.selectTool(PaintTool.MAGNIFIER) }
                            ) { widthPx = 45; heightPx = 28 }
                        }

                        label(text = "Tools", classes = "paint-ribbon-section-label") {
                            widthPx = 146
                            heightPx = 16
                        }
                    }

                    // Brushes Group
                    div(classes = "paint-ribbon-group d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 118
                        heightPx = 100

                        self.brushToolButton = button(
                            text = "Brush ▾",
                            type = BootstrapType.PRIMARY,
                            size = BootstrapSize.SM,
                            id = "btn-tool-brush",
                            onClick = { self.selectTool(PaintTool.BRUSH) }
                        ) { widthPx = 110; heightPx = 32 }

                        self.brushStyleSelect = select(
                            options = PaintBrushStyle.entries.map { SelectOption(it.name, it.label) },
                            selectedValue = self.activeBrushStyle.name,
                            id = "select-brush-style",
                            onSelectionChanged = { selected ->
                                val parsed = PaintBrushStyle.entries.firstOrNull { it.name == selected }
                                if (parsed != null) {
                                    self.selectBrushStyle(parsed)
                                }
                            }
                        ) { widthPx = 110; heightPx = 32 }

                        label(text = "Brushes", classes = "paint-ribbon-section-label") {
                            widthPx = 110
                            heightPx = 16
                        }
                    }

                    // Shapes Group (20-shape gallery + Outline & Fill toggles)
                    div(classes = "paint-ribbon-group d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 302
                        heightPx = 100

                        hstack(classes = "gap-1 align-items-center") {
                            widthPx = 294
                            heightPx = 70
                            gapPx = 4

                            self.shapeButtons.clear()
                            scrollView(classes = "paint-shapes-box", id = "scroll-shapes-gallery") {
                                heightPx = 68
                                val allShapes = PaintShapeType.entries
                                val firstRow = allShapes.take(10)
                                val secondRow = allShapes.drop(10)

                                hstack(classes = "gap-1") {
                                    heightPx = 26
                                    gapPx = 2
                                    for (shape in firstRow) {
                                        val btn = button(
                                            text = shape.symbol,
                                            tooltipText = shape.label,
                                            type = BootstrapType.SECONDARY,
                                            size = BootstrapSize.SM,
                                            outline = true,
                                            id = "btn-shape-${shape.name.lowercase()}",
                                            onClick = { self.selectShape(shape) }
                                        ) { widthPx = 18; heightPx = 24 }
                                        self.shapeButtons[shape] = btn
                                    }
                                }

                                hstack(classes = "gap-1") {
                                    heightPx = 26
                                    gapPx = 2
                                    for (shape in secondRow) {
                                        val btn = button(
                                            text = shape.symbol,
                                            tooltipText = shape.label,
                                            type = BootstrapType.SECONDARY,
                                            size = BootstrapSize.SM,
                                            outline = true,
                                            id = "btn-shape-${shape.name.lowercase()}",
                                            onClick = { self.selectShape(shape) }
                                        ) { widthPx = 18; heightPx = 24 }
                                        self.shapeButtons[shape] = btn
                                    }
                                }
                            }

                            div(classes = "d-flex flex-column gap-1") {
                                widthPx = 74
                                heightPx = 68

                                self.shapeOutlineButton = button(
                                    text = "Outline ▾",
                                    type = BootstrapType.PRIMARY,
                                    size = BootstrapSize.SM,
                                    id = "btn-shape-outline",
                                    onClick = { self.toggleShapeOutline() }
                                ) { widthPx = 72; heightPx = 28 }

                                self.shapeFillButton = button(
                                    text = "Fill ▾",
                                    type = BootstrapType.SECONDARY,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-shape-fill",
                                    onClick = { self.toggleShapeFill() }
                                ) { widthPx = 72; heightPx = 28 }
                            }
                        }

                        label(text = "Shapes", classes = "paint-ribbon-section-label") {
                            widthPx = 294
                            heightPx = 16
                        }
                    }

                    // Colours Group (Active FG/BG + 20 default palette swatches + 10 custom slots + ColorPicker)
                    div(classes = "paint-ribbon-group d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 276
                        heightPx = 100

                        hstack(classes = "gap-1 align-items-center") {
                            widthPx = 268
                            heightPx = 74
                            gapPx = 4

                            // Active Foreground & Background swatches
                            div(classes = "d-flex flex-column gap-1 align-items-center") {
                                widthPx = 36
                                heightPx = 70

                                self.foregroundSwatchButton = button(
                                    text = "FG",
                                    tooltipText = "Color 1 (Foreground): ${self.foregroundHex}",
                                    type = BootstrapType.PRIMARY,
                                    size = BootstrapSize.SM,
                                    classes = "paint-active-swatch-fg",
                                    id = "btn-swatch-foreground",
                                    onClick = { self.selectColorSlot(foreground = true) }
                                ) { widthPx = 34; heightPx = 28 }

                                self.backgroundSwatchButton = button(
                                    text = "BG",
                                    tooltipText = "Color 2 (Background): ${self.backgroundHex}",
                                    type = BootstrapType.LIGHT,
                                    size = BootstrapSize.SM,
                                    classes = "paint-active-swatch-bg",
                                    id = "btn-swatch-background",
                                    onClick = { self.selectColorSlot(foreground = false) }
                                ) { widthPx = 34; heightPx = 28 }
                            }

                            // 20 Default Palette Circles (2 rows of 10) + 10 Custom Swatch Slots
                            self.paletteButtons.clear()
                            self.customSwatchButtons.clear()
                            div(classes = "d-flex flex-column gap-1") {
                                widthPx = 160
                                heightPx = 74

                                hstack(classes = "gap-1") {
                                    widthPx = 160
                                    heightPx = 20
                                    gapPx = 2
                                    for (i in 0 until 10) {
                                        val hex = self.defaultPaletteHex[i]
                                        val btn = button(
                                            text = " ",
                                            tooltipText = hex,
                                            type = BootstrapType.DARK,
                                            size = BootstrapSize.SM,
                                            classes = "paint-swatch-circle",
                                            id = "btn-palette-$i",
                                            onClick = { self.applyPaletteColor(hex) }
                                        ) {
                                            widthPx = 14
                                            heightPx = 20
                                            getStyle().apply {
                                                setBackgroundColor(RgbaColor.fromHex(hex))
                                                setTextColor(RgbaColor.fromHex(hex))
                                                setBorderRadius(CssCornerRadii.px(12f))
                                            }
                                        }
                                        self.paletteButtons.add(btn)
                                    }
                                }

                                hstack(classes = "gap-1") {
                                    widthPx = 160
                                    heightPx = 20
                                    gapPx = 2
                                    for (i in 10 until 20) {
                                        val hex = self.defaultPaletteHex[i]
                                        val btn = button(
                                            text = " ",
                                            tooltipText = hex,
                                            type = BootstrapType.DARK,
                                            size = BootstrapSize.SM,
                                            classes = "paint-swatch-circle",
                                            id = "btn-palette-$i",
                                            onClick = { self.applyPaletteColor(hex) }
                                        ) {
                                            widthPx = 14
                                            heightPx = 20
                                            getStyle().apply {
                                                setBackgroundColor(RgbaColor.fromHex(hex))
                                                setTextColor(RgbaColor.fromHex(hex))
                                                setBorderRadius(CssCornerRadii.px(12f))
                                            }
                                        }
                                        self.paletteButtons.add(btn)
                                    }
                                }

                                hstack(classes = "gap-1") {
                                    widthPx = 160
                                    heightPx = 20
                                    gapPx = 2
                                    for (i in 0 until 10) {
                                        val customHex = self._customPaletteHex[i]
                                        val btn = button(
                                            text = " ",
                                            tooltipText = "Custom swatch ${i + 1}: $customHex",
                                            type = BootstrapType.DARK,
                                            size = BootstrapSize.SM,
                                            classes = "paint-swatch-circle",
                                            id = "btn-custom-swatch-$i",
                                            onClick = { self.applyPaletteColor(self._customPaletteHex[i]) }
                                        ) {
                                            widthPx = 14
                                            heightPx = 20
                                            getStyle().apply {
                                                setBackgroundColor(RgbaColor.fromHex(customHex))
                                                setBorderRadius(CssCornerRadii.px(12f))
                                            }
                                        }
                                        self.customSwatchButtons.add(btn)
                                    }
                                }
                            }

                            // Edit Colors Button + ColorPicker
                            div(classes = "d-flex flex-column gap-1 align-items-center") {
                                widthPx = 58
                                heightPx = 70

                                self.editColorsButton = button(
                                    text = "Edit",
                                    tooltipText = "Edit colours",
                                    type = BootstrapType.INFO,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-edit-colors",
                                    onClick = { self.customColorPicker.openNativeColorDialog() }
                                ) { widthPx = 56; heightPx = 28 }

                                self.customColorPicker.onColorChanged = { chosen ->
                                    self.setCustomColor(chosen.toHex(includeAlpha = false).uppercase())
                                }
                                self.customColorPicker.onValueChanged = { raw ->
                                    val parsed = RgbaColor.fromCss(raw)
                                    if (parsed != null) {
                                        self.setCustomColor(parsed.toHex(includeAlpha = false).uppercase())
                                    }
                                }
                                component(self.customColorPicker)
                            }
                        }

                        label(text = "Colours", classes = "paint-ribbon-section-label") {
                            widthPx = 268
                            heightPx = 16
                        }
                    }

                    // Copilot & Layers Group
                    div(classes = "paint-ribbon-group-last d-flex flex-column justify-content-between align-items-center") {
                        widthPx = 118
                        heightPx = 100

                        self.copilotButton = button(
                            text = "* Copilot ▾",
                            tooltipText = "AI Assist: Generate sample illustration",
                            type = BootstrapType.INFO,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-copilot-generate",
                            onClick = { self.generateCopilotArtwork() }
                        ) { widthPx = 110; heightPx = 32 }

                        self.layersToggleButton = button(
                            text = "[] Layers",
                            tooltipText = "Toggle Layers side panel",
                            type = if (self.layersPanelOpen) BootstrapType.PRIMARY else BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = !self.layersPanelOpen,
                            id = "btn-toggle-layers",
                            onClick = { self.toggleLayersPanel() }
                        ) { widthPx = 110; heightPx = 32 }

                        label(text = "Copilot & Layers", classes = "paint-ribbon-section-label") {
                            widthPx = 110
                            heightPx = 16
                        }
                    }
                }

                // 3. Main Workspace Area: Left Vertical Sliders + Centered White Canvas + Collapsible Layers Panel
                div(
                    classes = "paint-workspace-area d-flex flex-row justify-content-between align-items-center gap-3",
                    id = "paint-workspace-area"
                ) {
                    // Left Floating Vertical Pill Controls (Brush Size 1..64px & Brush Opacity 1..100%)
                    div(classes = "paint-floating-pill d-flex flex-column gap-2 align-items-center", id = "paint-left-sliders-pill") {
                        widthPx = 148
                        heightPx = 216

                        label(text = "Brush Size", classes = "mb-0") { widthPx = 132; heightPx = 16 }
                        self.brushSizeBadge = badge(
                            text = "${self.brushSizePx}px",
                            type = BootstrapType.PRIMARY,
                            pill = true,
                            id = "badge-brush-size"
                        ) { widthPx = 132; heightPx = 22 }

                        self.brushSizeSlider = rangeSlider(
                            minValue = self.brushSizePx.toDouble(),
                            maxValue = 64.0,
                            min = 1.0,
                            max = 64.0,
                            step = 1.0,
                            id = "slider-brush-size"
                        ) {
                            widthPx = 132
                            heightPx = 26
                            onValueChanged = { updated ->
                                self.updateBrushSize(updated.roundToInt())
                            }
                        }

                        label(text = "Opacity", classes = "mb-0") { widthPx = 132; heightPx = 16 }
                        self.brushOpacityBadge = badge(
                            text = "${self.brushOpacityPercent}%",
                            type = BootstrapType.INFO,
                            pill = true,
                            id = "badge-brush-opacity"
                        ) { widthPx = 132; heightPx = 22 }

                        self.brushOpacitySlider = rangeSlider(
                            minValue = self.brushOpacityPercent.toDouble(),
                            maxValue = 100.0,
                            min = 1.0,
                            max = 100.0,
                            step = 1.0,
                            id = "slider-brush-opacity"
                        ) {
                            widthPx = 132
                            heightPx = 26
                            onValueChanged = { updated ->
                                self.updateBrushOpacity(updated.roundToInt())
                            }
                        }
                    }

                    // Centered White Drawing Canvas Stage with 8 Perimeter Square Resize Handles
                    vstack(classes = "paint-canvas-stage align-items-center justify-content-center", id = "paint-canvas-stage") {
                        // Top 3 resize handles (NW, N, NE)
                        hstack(classes = "justify-content-between w-100 mb-1") {
                            heightPx = 14
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-nw") { widthPx = 14; heightPx = 14 }
                            vstack {}
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-n") { widthPx = 14; heightPx = 14 }
                            vstack {}
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-ne") { widthPx = 14; heightPx = 14 }
                        }

                        // Middle row: W handle + Canvas2D + E handle
                        div(classes = "d-flex flex-row align-items-center gap-1") {
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-w") { widthPx = 14; heightPx = 14 }

                            div(classes = "paint-canvas-frame", id = "paint-canvas-frame") {
                                self.paintCanvas = canvas2D(
                                    width = self.canvasWidthPx,
                                    height = self.canvasHeightPx,
                                    id = "paint-canvas-surface",
                                    classes = "paint-canvas-surface",
                                    onRender = { ctx, _, _, frameIdx ->
                                        self.renderedFrameCount = max(self.renderedFrameCount + 1L, frameIdx)
                                        self.renderPaintDocument(ctx)
                                    }
                                ) {
                                    getStyle().setCursor(self.cursorForTool(self.activeTool))
                                    onPointerDown = { ev -> self.handlePointerDown(ev) }
                                    onPointerMove = { ev -> self.handlePointerMove(ev) }
                                    onPointerUp = { ev -> self.handlePointerUp(ev) }
                                }
                            }

                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-e") { widthPx = 14; heightPx = 14 }
                        }

                        // Bottom 3 resize handles (SW, S, SE)
                        hstack(classes = "justify-content-between w-100 mt-1") {
                            heightPx = 14
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-sw") { widthPx = 14; heightPx = 14 }
                            vstack {}
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-s") { widthPx = 14; heightPx = 14 }
                            vstack {}
                            label(text = "+", classes = "paint-resize-handle", id = "handle-resize-se") { widthPx = 14; heightPx = 14 }
                        }
                    }

                    // Collapsible Right-Side Layers Panel
                    self.layersSidebar = div(classes = "paint-layers-sidebar", id = "panel-layers") {
                        widthPx = 224
                        vstack(classes = "gap-2") {
                            hstack(classes = "justify-content-between align-items-center") {
                                heightPx = 28
                                label(text = "Layers", classes = "mb-0", id = "label-layers-header") { widthPx = 80; heightPx = 20 }
                                self.addLayerButton = button(
                                    text = "+ Add",
                                    tooltipText = "Add Layer",
                                    type = BootstrapType.PRIMARY,
                                    size = BootstrapSize.SM,
                                    id = "btn-layer-add",
                                    onClick = { self.addLayer() }
                                ) { widthPx = 68; heightPx = 24 }
                            }

                            hstack(classes = "gap-1") {
                                heightPx = 28
                                self.toggleLayerVisibilityButton = button(
                                    text = "Vis",
                                    tooltipText = "Toggle active layer visibility",
                                    type = BootstrapType.SECONDARY,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-layer-toggle-visibility",
                                    onClick = { self.toggleActiveLayerVisibility() }
                                ) { widthPx = 56; heightPx = 24 }

                                self.deleteLayerButton = button(
                                    text = "Del",
                                    tooltipText = "Delete active layer",
                                    type = BootstrapType.DANGER,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-layer-delete",
                                    onClick = { self.deleteActiveLayer() }
                                ) { widthPx = 56; heightPx = 24 }

                                self.mergeDownLayerButton = button(
                                    text = "Merge v",
                                    tooltipText = "Merge active layer down",
                                    type = BootstrapType.INFO,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-layer-merge-down",
                                    onClick = { self.mergeActiveLayerDown() }
                                ) { widthPx = 76; heightPx = 24 }
                            }

                            self.layersListContainer = vstack(classes = "gap-1", id = "layers-list-container")
                        }
                    }
                }

                // 4. Bottom Status Bar (Cursor Pos, Selection Size, Canvas Size 131 × 128px, Fit-to-Window, Zoom Slider)
                hstack(
                    classes = "paint-status-bar justify-content-between align-items-center flex-wrap",
                    id = "paint-status-bar"
                ) {
                    heightPx = 32
                    gapPx = 6

                    hstack(classes = "gap-2 align-items-center") {
                        widthPx = 560
                        heightPx = 26
                        gapPx = 8

                        self.cursorPositionLabel = label(
                            text = "+ ${self.lastCursorX}, ${self.lastCursorY}px",
                            classes = "mb-0",
                            id = "label-cursor-pos"
                        ) { widthPx = 96; heightPx = 18 }

                        self.selectionSizeLabel = label(
                            text = "[] ${self.selectionDimensionsText}",
                            classes = "mb-0",
                            id = "label-selection-size"
                        ) { widthPx = 136; heightPx = 18 }

                        self.canvasSizeBadge = badge(
                            text = "${self.canvasWidthPx} × ${self.canvasHeightPx}px",
                            type = BootstrapType.DARK,
                            pill = true,
                            id = "badge-canvas-size"
                        ) { widthPx = 112; heightPx = 22 }

                        self.activeToolBadge = badge(
                            text = "Tool: ${self.activeTool.name}",
                            type = BootstrapType.PRIMARY,
                            pill = true,
                            id = "badge-active-tool"
                        ) { widthPx = 136; heightPx = 22 }
                    }

                    vstack {}

                    hstack(classes = "gap-2 align-items-center justify-content-end") {
                        widthPx = 416
                        heightPx = 26
                        gapPx = 6

                        self.fitToWindowButton = button(
                            text = "[Fit]",
                            tooltipText = "Fit to window (100%)",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-zoom-fit",
                            onClick = { self.fitToWindow() }
                        ) { widthPx = 54; heightPx = 24 }

                        self.zoomSelect = select(
                            options = listOf(
                                SelectOption("25", "25%"),
                                SelectOption("50", "50%"),
                                SelectOption("100", "100%"),
                                SelectOption("200", "200%"),
                                SelectOption("400", "400%"),
                                SelectOption("800", "800%")
                            ),
                            selectedValue = self.zoomPercent.toString(),
                            id = "select-zoom-percent",
                            onSelectionChanged = { value ->
                                value.toIntOrNull()?.let { self.updateZoomPercent(it) }
                            }
                        ) { widthPx = 82; heightPx = 24 }

                        self.zoomOutButton = button(
                            text = "-",
                            tooltipText = "Zoom out",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-zoom-out",
                            onClick = { self.updateZoomPercent((self.zoomPercent - 25).coerceAtLeast(25)) }
                        ) { widthPx = 34; heightPx = 24 }

                        self.zoomSlider = rangeSlider(
                            minValue = self.zoomPercent.toDouble(),
                            maxValue = 800.0,
                            min = 25.0,
                            max = 800.0,
                            step = 25.0,
                            id = "slider-zoom"
                        ) {
                            widthPx = 160
                            heightPx = 22
                            onValueChanged = { updated ->
                                self.updateZoomPercent(updated.roundToInt())
                            }
                        }

                        self.zoomInButton = button(
                            text = "+",
                            tooltipText = "Zoom in",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-zoom-in",
                            onClick = { self.updateZoomPercent((self.zoomPercent + 25).coerceAtMost(800)) }
                        ) { widthPx = 34; heightPx = 24 }
                    }
                }
            }
        }
        rebuildLayersListUi()
    }

    /**
     * Builds a custom SVG tool cursor ([CssCustomCursor]) for the specified [tool].
     */
    fun cursorForTool(tool: PaintTool): CssCustomCursor = when (tool) {
        PaintTool.PENCIL, PaintTool.BRUSH -> CssCustomCursor.fromSvg(
            svgMarkup = """<svg viewBox="0 0 24 24"><line x1="3" y1="21" x2="18" y2="6" stroke="#60A5FA" stroke-width="2"/><circle cx="3" cy="21" r="2" fill="#60A5FA"/></svg>""",
            widthPx = 24,
            heightPx = 24,
            hotspotX = 3,
            hotspotY = 21,
            fallback = CssCursor.CROSSHAIR
        )
        PaintTool.FILL_BUCKET -> CssCustomCursor.fromSvg(
            svgMarkup = """<svg viewBox="0 0 24 24"><rect x="6" y="6" width="12" height="12" stroke="#F59E0B" stroke-width="2" fill="none"/><circle cx="18" cy="18" r="3" fill="#F59E0B"/></svg>""",
            widthPx = 24,
            heightPx = 24,
            hotspotX = 18,
            hotspotY = 18,
            fallback = CssCursor.CROSSHAIR
        )
        PaintTool.ERASER -> CssCustomCursor.fromSvg(
            svgMarkup = """<svg viewBox="0 0 24 24"><rect x="5" y="5" width="14" height="14" stroke="#FFFFFF" stroke-width="2" fill="#F43F5E"/></svg>""",
            widthPx = 24,
            heightPx = 24,
            hotspotX = 12,
            hotspotY = 12,
            fallback = CssCursor.CROSSHAIR
        )
        PaintTool.COLOR_PICKER -> CssCustomCursor.fromSvg(
            svgMarkup = """<svg viewBox="0 0 24 24"><line x1="4" y1="20" x2="19" y2="5" stroke="#22C55E" stroke-width="2"/><circle cx="4" cy="20" r="2" fill="#22C55E"/></svg>""",
            widthPx = 24,
            heightPx = 24,
            hotspotX = 4,
            hotspotY = 20,
            fallback = CssCursor.CROSSHAIR
        )
        else -> CssCustomCursor.fromSvg(
            svgMarkup = """<svg viewBox="0 0 24 24"><line x1="12" y1="3" x2="12" y2="21" stroke="#E2E8F0" stroke-width="2"/><line x1="3" y1="12" x2="21" y2="12" stroke="#E2E8F0" stroke-width="2"/></svg>""",
            widthPx = 24,
            heightPx = 24,
            hotspotX = 12,
            hotspotY = 12,
            fallback = CssCursor.CROSSHAIR
        )
    }

    /**
     * Switches the active [PaintTool], updates ribbon button states, and updates the canvas cursor.
     */
    fun selectTool(tool: PaintTool) {
        activeTool = tool
        if (::paintCanvas.isInitialized) {
            paintCanvas.getStyle().setCursor(cursorForTool(tool))
        }
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Tool: ${tool.label}"
        }
        syncAllControls()
    }

    /**
     * Selects a [PaintBrushStyle] and activates [PaintTool.BRUSH].
     */
    fun selectBrushStyle(style: PaintBrushStyle) {
        activeBrushStyle = style
        activeTool = PaintTool.BRUSH
        if (::brushStyleSelect.isInitialized && brushStyleSelect.selectedValue != style.name) {
            brushStyleSelect.selectedValue = style.name
        }
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Brush: ${style.label}"
        }
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Selects one of the 20 [PaintShapeType] entries in the `Shapes` gallery and activates [PaintTool.SHAPE].
     */
    fun selectShape(shape: PaintShapeType) {
        activeShapeType = shape
        activeTool = PaintTool.SHAPE
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Shape: ${shape.label}"
        }
        syncAllControls()
    }

    fun toggleShapeOutline() {
        shapeOutlineEnabled = !shapeOutlineEnabled
        if (!shapeOutlineEnabled && !shapeFillEnabled) {
            shapeFillEnabled = true
        }
        syncAllControls()
    }

    fun toggleShapeFill() {
        shapeFillEnabled = !shapeFillEnabled
        if (!shapeFillEnabled && !shapeOutlineEnabled) {
            shapeOutlineEnabled = true
        }
        syncAllControls()
    }

    /**
     * Selects whether palette clicks update Color 1 ([foreground] = `true`) or Color 2 (`false`).
     */
    fun selectColorSlot(foreground: Boolean) {
        editingForegroundColor = foreground
        syncAllControls()
    }

    /**
     * Applies [hex] to either [foregroundHex] or [backgroundHex] depending on [editingForegroundColor].
     */
    fun applyPaletteColor(hex: String) {
        val normalized = normalizeHex(hex)
        if (editingForegroundColor) {
            foregroundHex = normalized
        } else {
            backgroundHex = normalized
        }
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = if (editingForegroundColor) {
                "Foreground color: $normalized"
            } else {
                "Background color: $normalized"
            }
        }
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Explicitly sets [foregroundHex] (Color 1).
     */
    fun setForegroundColor(hex: String) {
        foregroundHex = normalizeHex(hex)
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Explicitly sets [backgroundHex] (Color 2).
     */
    fun setBackgroundColor(hex: String) {
        backgroundHex = normalizeHex(hex)
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Adds or updates a custom swatch color in the 10-slot custom palette row and applies it.
     */
    fun setCustomColor(hex: String, slotIndex: Int = 0) {
        val normalized = normalizeHex(hex)
        val idx = slotIndex.coerceIn(0, _customPaletteHex.lastIndex)
        _customPaletteHex[idx] = normalized
        if (idx < customSwatchButtons.size) {
            customSwatchButtons[idx].getStyle().setBackgroundColor(RgbaColor.fromHex(normalized))
        }
        applyPaletteColor(normalized)
    }

    /**
     * Updates the brush thickness (`1..64 px`) from the left vertical slider.
     */
    fun updateBrushSize(sizePx: Int) {
        brushSizePx = sizePx.coerceIn(1, 64)
        if (::brushSizeSlider.isInitialized && brushSizeSlider.value.roundToInt() != brushSizePx) {
            brushSizeSlider.value = brushSizePx.toDouble()
        }
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Updates the brush opacity (`1..100%`) from the left vertical slider.
     */
    fun updateBrushOpacity(opacityPercent: Int) {
        brushOpacityPercent = opacityPercent.coerceIn(1, 100)
        if (::brushOpacitySlider.isInitialized && brushOpacitySlider.value.roundToInt() != brushOpacityPercent) {
            brushOpacitySlider.value = brushOpacityPercent.toDouble()
        }
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Resizes the drawing canvas (`widthPx × heightPx`) and synchronizes the underlying [Canvas2D] surface.
     */
    fun resizeCanvas(widthPx: Int, heightPx: Int) {
        canvasWidthPx = widthPx.coerceIn(16, 4096)
        canvasHeightPx = heightPx.coerceIn(16, 4096)
        if (::paintCanvas.isInitialized) {
            paintCanvas.syncSurfaceSize(canvasWidthPx, canvasHeightPx)
        }
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Updates the viewport zoom percentage (`25..800%`).
     */
    fun updateZoomPercent(percent: Int) {
        zoomPercent = percent.coerceIn(25, 800)
        if (::zoomSlider.isInitialized && zoomSlider.value.roundToInt() != zoomPercent) {
            zoomSlider.value = zoomPercent.toDouble()
        }
        if (::zoomSelect.isInitialized) {
            zoomSelect.selectedValue = zoomPercent.toString()
        }
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Resets zoom to `100%` (Fit to Window).
     */
    fun fitToWindow() {
        updateZoomPercent(100)
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Fit to window (100%)"
        }
    }

    // -------------------------------------------------------------------------
    // Layers Management
    // -------------------------------------------------------------------------

    /**
     * Toggles the right-side `Layers` panel open or closed.
     */
    fun toggleLayersPanel(open: Boolean = !layersPanelOpen) {
        layersPanelOpen = open
        syncAllControls()
    }

    /**
     * Adds a new transparent [PaintLayer] above the current layer list and selects it.
     */
    fun addLayer(name: String = "Layer ${nextLayerNumber++}"): PaintLayer {
        val layer = PaintLayer(
            layerId = "layer-${_layers.size + 1}-${nextLayerNumber}",
            name = name,
            visible = true,
            opacityPercent = 100
        )
        _layers.add(layer)
        activeLayerIndex = _layers.lastIndex
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
        return layer
    }

    /**
     * Selects the active layer by [index].
     */
    fun selectLayer(index: Int) {
        if (index in _layers.indices) {
            activeLayerIndex = index
            rebuildLayersListUi()
            syncAllControls()
        }
    }

    /**
     * Toggles the visibility of the layer at [index] (defaults to [activeLayerIndex]).
     */
    fun toggleActiveLayerVisibility(index: Int = activeLayerIndex): Boolean {
        val target = _layers.getOrNull(index) ?: return false
        target.visible = !target.visible
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
        return target.visible
    }

    /**
     * Deletes the active layer when more than 1 layer exists.
     */
    fun deleteActiveLayer(): Boolean {
        if (_layers.size <= 1) return false
        _layers.removeAt(activeLayerIndex)
        activeLayerIndex = activeLayerIndex.coerceIn(0, _layers.lastIndex)
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
        return true
    }

    /**
     * Merges the active layer's strokes down into the layer beneath it.
     */
    fun mergeActiveLayerDown(): Boolean {
        if (activeLayerIndex <= 0 || _layers.size <= 1) return false
        val upper = _layers.removeAt(activeLayerIndex)
        val lower = _layers[activeLayerIndex - 1]
        lower.strokes.addAll(upper.strokes)
        activeLayerIndex = (activeLayerIndex - 1).coerceIn(0, _layers.lastIndex)
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
        return true
    }

    private fun rebuildLayersListUi() {
        if (!::layersListContainer.isInitialized) return
        val self = this
        layersListContainer.build(clearFirst = true) {
            for (idx in self._layers.indices.reversed()) {
                val layer = self._layers[idx]
                val isSelected = idx == self.activeLayerIndex
                div(
                    classes = "paint-layer-card",
                    id = "layer-item-$idx"
                ) {
                    hstack(classes = "justify-content-between align-items-center gap-1") {
                        button(
                            text = "${if (layer.visible) "[v]" else "[ ]"} ${layer.name} (${layer.strokes.size})",
                            type = if (isSelected) BootstrapType.PRIMARY else BootstrapType.DARK,
                            size = BootstrapSize.SM,
                            outline = !isSelected,
                            id = "btn-select-layer-$idx",
                            onClick = { self.selectLayer(idx) }
                        )
                        button(
                            text = if (layer.visible) "Hide" else "Show",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-vis-layer-$idx",
                            onClick = { self.toggleActiveLayerVisibility(idx) }
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Drawing, Undo/Redo, Image Transforms, & Copilot
    // -------------------------------------------------------------------------

    /**
     * Appends a [PaintStrokeCommand] to the active layer, clears redo history, and renders.
     */
    fun addStrokeCommand(command: PaintStrokeCommand) {
        activeLayer.strokes.add(command)
        _redoStack.clear()
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Simulates a pointer down -> move -> up drawing gesture on [paintCanvas].
     */
    fun simulateStrokeGesture(points: List<Pair<Float, Float>>, textContent: String = "Paint") {
        if (points.isEmpty()) return
        val first = points.first()
        handlePointerDown(PointerEvent(x = first.first, y = first.second, buttonsMask = 1), textContent = textContent)
        for (i in 1 until points.size) {
            val pt = points[i]
            handlePointerMove(PointerEvent(x = pt.first, y = pt.second, buttonsMask = 1))
        }
        val last = points.last()
        handlePointerUp(PointerEvent(x = last.first, y = last.second, buttonsMask = 0), textContent = textContent)
    }

    /**
     * Undoes the most recent stroke on the active layer (or the most recent non-empty layer).
     */
    fun undo(): Boolean {
        val targetIndex = if (activeLayer.strokes.isNotEmpty()) {
            activeLayerIndex
        } else {
            _layers.indexOfLast { it.strokes.isNotEmpty() }
        }
        if (targetIndex < 0) return false
        val layer = _layers[targetIndex]
        val removed = layer.strokes.removeAt(layer.strokes.lastIndex)
        _redoStack.add(targetIndex to removed)
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Undid ${removed.tool.label}"
        }
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
        return true
    }

    /**
     * Redoes the most recently undone [PaintStrokeCommand].
     */
    fun redo(): Boolean {
        if (_redoStack.isEmpty()) return false
        val (layerIdx, cmd) = _redoStack.removeAt(_redoStack.lastIndex)
        val safeIdx = layerIdx.coerceIn(0, _layers.lastIndex)
        _layers[safeIdx].strokes.add(cmd)
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Redid ${cmd.tool.label}"
        }
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
        return true
    }

    /**
     * Clears all strokes on the active layer.
     */
    fun clearActiveLayer() {
        activeLayer.strokes.clear()
        _redoStack.clear()
        selectionDimensionsText = "No selection"
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Resets the entire project to a fresh `131 × 128px` single-layer canvas.
     */
    fun handleNewCanvas() {
        _layers.clear()
        _layers.add(PaintLayer(layerId = "layer-1", name = "Layer 1", visible = true, opacityPercent = 100))
        activeLayerIndex = 0
        _redoStack.clear()
        selectionDimensionsText = "No selection"
        resizeCanvas(131, 128)
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
    }

    fun rotateCanvas90Clockwise() {
        val oldW = canvasWidthPx
        val oldH = canvasHeightPx
        for (layer in _layers) {
            val rotated = layer.strokes.map { stroke ->
                stroke.copy(
                    points = stroke.points.map { (x, y) -> (oldH - y) to x }
                )
            }
            layer.strokes.clear()
            layer.strokes.addAll(rotated)
        }
        resizeCanvas(oldH, oldW)
    }

    fun flipCanvasHorizontal() {
        val w = canvasWidthPx.toFloat()
        for (layer in _layers) {
            val flipped = layer.strokes.map { stroke ->
                stroke.copy(
                    points = stroke.points.map { (x, y) -> (w - x) to y }
                )
            }
            layer.strokes.clear()
            layer.strokes.addAll(flipped)
        }
        requestCanvasRender()
    }

    fun cropToSelectionOrTrim() {
        val selStroke = activeLayer.strokes.lastOrNull { it.tool == PaintTool.SELECTION && it.points.size >= 2 }
        if (selStroke != null) {
            val p0 = selStroke.points.first()
            val p1 = selStroke.points.last()
            val newW = abs(p1.first - p0.first).roundToInt().coerceAtLeast(16)
            val newH = abs(p1.second - p0.second).roundToInt().coerceAtLeast(16)
            resizeCanvas(newW, newH)
        }
    }

    fun removeBackground() {
        backgroundHex = "#141416"
        syncAllControls()
        requestCanvasRender()
    }

    /**
     * Copilot AI Assist action: adds a structured multi-shape illustration to the active layer.
     */
    fun generateCopilotArtwork(): List<PaintStrokeCommand> {
        val seed = copilotSeedCounter++
        val cx = canvasWidthPx * 0.5f
        val cy = canvasHeightPx * 0.5f
        val generated = listOf(
            PaintStrokeCommand(
                tool = PaintTool.SHAPE,
                shapeType = PaintShapeType.ROUNDED_RECTANGLE,
                primaryColorHex = "#00A2E8",
                secondaryColorHex = "#99D9EA",
                strokeWidthPx = 3f,
                opacityPercent = 95,
                outlineEnabled = true,
                fillEnabled = true,
                points = listOf(14f to 14f, (canvasWidthPx - 14f) to (canvasHeightPx - 14f))
            ),
            PaintStrokeCommand(
                tool = PaintTool.SHAPE,
                shapeType = if (seed % 2 == 0) PaintShapeType.FIVE_POINT_STAR else PaintShapeType.HEART,
                primaryColorHex = "#FF7F27",
                secondaryColorHex = "#FFF200",
                strokeWidthPx = 2f,
                opacityPercent = 100,
                outlineEnabled = true,
                fillEnabled = true,
                points = listOf((cx - 28f) to (cy - 28f), (cx + 28f) to (cy + 28f))
            )
        )
        for (cmd in generated) {
            addStrokeCommand(cmd)
        }
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Copilot generated ${generated.size} vector objects"
        }
        return generated
    }

    private fun handlePointerDown(event: PointerEvent, textContent: String = "Paint") {
        lastCursorX = event.x.roundToInt().coerceIn(0, canvasWidthPx)
        lastCursorY = event.y.roundToInt().coerceIn(0, canvasHeightPx)

        when (activeTool) {
            PaintTool.FILL_BUCKET -> {
                addStrokeCommand(
                    PaintStrokeCommand(
                        tool = PaintTool.FILL_BUCKET,
                        primaryColorHex = foregroundHex,
                        secondaryColorHex = backgroundHex,
                        opacityPercent = brushOpacityPercent,
                        points = listOf(event.x to event.y)
                    )
                )
            }
            PaintTool.TEXT -> {
                addStrokeCommand(
                    PaintStrokeCommand(
                        tool = PaintTool.TEXT,
                        primaryColorHex = foregroundHex,
                        secondaryColorHex = backgroundHex,
                        strokeWidthPx = brushSizePx.toFloat(),
                        opacityPercent = brushOpacityPercent,
                        textContent = textContent,
                        points = listOf(event.x to event.y)
                    )
                )
            }
            PaintTool.COLOR_PICKER -> {
                val sampled = sampleColorAt(event.x.roundToInt(), event.y.roundToInt())
                applyPaletteColor(sampled)
            }
            PaintTool.MAGNIFIER -> {
                val nextZoom = if (zoomPercent < 400) zoomPercent * 2 else 100
                updateZoomPercent(nextZoom)
            }
            else -> {
                activePointerPoints = mutableListOf(event.x to event.y)
            }
        }
        syncAllControls()
    }

    private fun handlePointerMove(event: PointerEvent) {
        lastCursorX = event.x.roundToInt().coerceIn(0, canvasWidthPx)
        lastCursorY = event.y.roundToInt().coerceIn(0, canvasHeightPx)
        val pts = activePointerPoints
        if (pts != null) {
            pts.add(event.x to event.y)
            if (activeTool == PaintTool.SELECTION && pts.size >= 2) {
                val p0 = pts.first()
                val rw = abs(event.x - p0.first).roundToInt()
                val rh = abs(event.y - p0.second).roundToInt()
                selectionDimensionsText = "$rw × ${rh}px"
            }
            requestCanvasRender()
        }
        if (::cursorPositionLabel.isInitialized) {
            cursorPositionLabel.text = "+ $lastCursorX, ${lastCursorY}px"
        }
        if (::selectionSizeLabel.isInitialized) {
            selectionSizeLabel.text = "[] $selectionDimensionsText"
        }
    }

    private fun handlePointerUp(event: PointerEvent, textContent: String = "") {
        lastCursorX = event.x.roundToInt().coerceIn(0, canvasWidthPx)
        lastCursorY = event.y.roundToInt().coerceIn(0, canvasHeightPx)
        val pts = activePointerPoints ?: return
        pts.add(event.x to event.y)
        activePointerPoints = null

        if (activeTool == PaintTool.SELECTION && pts.size >= 2) {
            val p0 = pts.first()
            val p1 = pts.last()
            val rw = abs(p1.first - p0.first).roundToInt()
            val rh = abs(p1.second - p0.second).roundToInt()
            selectionDimensionsText = "$rw × ${rh}px"
        }

        addStrokeCommand(
            PaintStrokeCommand(
                tool = activeTool,
                brushStyle = activeBrushStyle,
                shapeType = activeShapeType,
                primaryColorHex = foregroundHex,
                secondaryColorHex = backgroundHex,
                strokeWidthPx = brushSizePx.toFloat(),
                opacityPercent = brushOpacityPercent,
                outlineEnabled = shapeOutlineEnabled,
                fillEnabled = shapeFillEnabled,
                textContent = textContent,
                points = pts.toList()
            )
        )
    }

    private fun sampleColorAt(x: Int, y: Int): String {
        for (layer in _layers.asReversed()) {
            if (!layer.visible) continue
            val hit = layer.strokes.lastOrNull()
            if (hit != null) {
                return hit.primaryColorHex
            }
        }
        return if (x in 0 until canvasWidthPx && y in 0 until canvasHeightPx) {
            backgroundHex
        } else {
            foregroundHex
        }
    }

    private fun promptExportPng() {
        Dialogs.saveFile(
            title = "Export Paint PNG",
            suggestedFileName = "untitled-paint.png",
            filters = listOf(FileTypeFilter("PNG Images", listOf("png"))),
            ownerWindow = attachedWindow,
            onSelected = { file ->
                if (::statusMessageLabel.isInitialized) {
                    statusMessageLabel.text = "Exported ${file.name}"
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // PreferenceStore Persistence (`getPreferenceStore()`)
    // -------------------------------------------------------------------------

    /**
     * Captures the current workspace state as a [PaintPreferences] snapshot.
     */
    fun currentPreferences(): PaintPreferences = PaintPreferences(
        canvasWidthPx = canvasWidthPx,
        canvasHeightPx = canvasHeightPx,
        foregroundHex = foregroundHex,
        backgroundHex = backgroundHex,
        brushSizePx = brushSizePx,
        brushOpacityPercent = brushOpacityPercent,
        zoomPercent = zoomPercent,
        layersPanelOpen = layersPanelOpen
    )

    /**
     * Persists [currentPreferences] into `getPreferenceStore()`.
     */
    fun savePreferencesToStore(): PaintPreferences {
        PaintPreferences.registerFactory()
        val prefs = currentPreferences()
        val store = getPreferenceStore()
        store.put(PaintPreferences.PREFERENCE_KEY, prefs)
        store.flush()
        if (::statusMessageLabel.isInitialized) {
            statusMessageLabel.text = "Saved preferences (${prefs.canvasWidthPx}×${prefs.canvasHeightPx}px)"
        }
        return prefs
    }

    /**
     * Restores [PaintPreferences] from `getPreferenceStore()` if present.
     */
    fun loadPreferencesFromStore(): PaintPreferences {
        PaintPreferences.registerFactory()
        val store = getPreferenceStore()
        val loaded = store.getElement(PaintPreferences.PREFERENCE_KEY)?.let { elem ->
            if (elem.isObject || elem.isMap) {
                PaintPreferences.fromElementMap(elem.mapEntries)
            } else {
                null
            }
        } ?: store.get(PaintPreferences.PREFERENCE_KEY, PaintPreferences::class, null)
        if (loaded != null) {
            applyPreferences(loaded)
            return loaded
        }
        return currentPreferences()
    }

    /**
     * Applies a [PaintPreferences] instance to this fragment's active state and UI controls.
     */
    fun applyPreferences(prefs: PaintPreferences) {
        canvasWidthPx = prefs.canvasWidthPx.coerceIn(16, 4096)
        canvasHeightPx = prefs.canvasHeightPx.coerceIn(16, 4096)
        foregroundHex = normalizeHex(prefs.foregroundHex)
        backgroundHex = normalizeHex(prefs.backgroundHex)
        brushSizePx = prefs.brushSizePx.coerceIn(1, 64)
        brushOpacityPercent = prefs.brushOpacityPercent.coerceIn(1, 100)
        zoomPercent = prefs.zoomPercent.coerceIn(25, 800)
        layersPanelOpen = prefs.layersPanelOpen
        if (::paintCanvas.isInitialized) {
            paintCanvas.syncSurfaceSize(canvasWidthPx, canvasHeightPx)
            brushSizeSlider.value = brushSizePx.toDouble()
            brushOpacitySlider.value = brushOpacityPercent.toDouble()
            zoomSlider.value = zoomPercent.toDouble()
            zoomSelect.selectedValue = zoomPercent.toString()
            syncAllControls()
            requestCanvasRender()
        }
    }

    // -------------------------------------------------------------------------
    // Binary Project Serialization (`Writable` / `Readable`)
    // -------------------------------------------------------------------------

    /**
     * Serializes the entire multi-layer Paint document (canvas dimensions, colors, brush settings,
     * layers, and all [PaintStrokeCommand] items) into [writable].
     */
    fun exportCanvasToWritable(writable: Writable) {
        writable.writeInt(BINARY_MAGIC)
        writable.writeInt(BINARY_VERSION)
        writable.writeInt(canvasWidthPx)
        writable.writeInt(canvasHeightPx)
        PaintStrokeCommand.writeLengthPrefixedString(writable, foregroundHex)
        PaintStrokeCommand.writeLengthPrefixedString(writable, backgroundHex)
        writable.writeInt(brushSizePx)
        writable.writeInt(brushOpacityPercent)
        writable.writeInt(zoomPercent)
        writable.writeBoolean(layersPanelOpen)
        writable.writeInt(activeLayerIndex)
        writable.writeInt(_layers.size)
        for (layer in _layers) {
            PaintStrokeCommand.writeLengthPrefixedString(writable, layer.layerId)
            PaintStrokeCommand.writeLengthPrefixedString(writable, layer.name)
            writable.writeBoolean(layer.visible)
            writable.writeInt(layer.opacityPercent)
            writable.writeInt(layer.strokes.size)
            for (stroke in layer.strokes) {
                stroke.writeTo(writable)
            }
        }
    }

    /**
     * Deserializes a multi-layer Paint document from [readable] (backed by [ReadableByteBuffer])
     * and updates the canvas and UI controls.
     */
    fun importCanvasFromReadable(readable: Readable) {
        val reader = readable as? ReadableByteBuffer
            ?: throw IllegalArgumentException("Expected ReadableByteBuffer for synchronous canvas import")
        val magic = reader.nextInt()
        require(magic == BINARY_MAGIC) {
            "Invalid GenericUIPaint binary header: 0x${magic.toUInt().toString(16)}"
        }
        val version = reader.nextInt()
        require(version == BINARY_VERSION) {
            "Unsupported GenericUIPaint binary version: $version"
        }
        val importedWidth = reader.nextInt().coerceIn(16, 4096)
        val importedHeight = reader.nextInt().coerceIn(16, 4096)
        val importedFg = PaintStrokeCommand.readLengthPrefixedString(reader)
        val importedBg = PaintStrokeCommand.readLengthPrefixedString(reader)
        val importedBrushSize = reader.nextInt().coerceIn(1, 64)
        val importedBrushOpacity = reader.nextInt().coerceIn(1, 100)
        val importedZoom = reader.nextInt().coerceIn(25, 800)
        val importedLayersOpen = reader.nextBoolean()
        val importedActiveLayer = reader.nextInt()
        val layerCount = reader.nextInt().coerceIn(1, 256)

        val parsedLayers = ArrayList<PaintLayer>(layerCount)
        for (i in 0 until layerCount) {
            val id = PaintStrokeCommand.readLengthPrefixedString(reader)
            val name = PaintStrokeCommand.readLengthPrefixedString(reader)
            val visible = reader.nextBoolean()
            val opacity = reader.nextInt().coerceIn(1, 100)
            val strokeCount = reader.nextInt().coerceIn(0, 100_000)
            val strokes = ArrayList<PaintStrokeCommand>(strokeCount)
            for (s in 0 until strokeCount) {
                strokes.add(PaintStrokeCommand.readFrom(reader))
            }
            parsedLayers.add(
                PaintLayer(
                    layerId = id.ifBlank { "layer-${i + 1}" },
                    name = name.ifBlank { "Layer ${i + 1}" },
                    visible = visible,
                    opacityPercent = opacity,
                    strokes = strokes.toMutableList()
                )
            )
        }

        _layers.clear()
        _layers.addAll(parsedLayers)
        activeLayerIndex = importedActiveLayer.coerceIn(0, _layers.lastIndex)
        _redoStack.clear()

        applyPreferences(
            PaintPreferences(
                canvasWidthPx = importedWidth,
                canvasHeightPx = importedHeight,
                foregroundHex = importedFg,
                backgroundHex = importedBg,
                brushSizePx = importedBrushSize,
                brushOpacityPercent = importedBrushOpacity,
                zoomPercent = importedZoom,
                layersPanelOpen = importedLayersOpen
            )
        )
        rebuildLayersListUi()
        syncAllControls()
        requestCanvasRender()
    }

    // -------------------------------------------------------------------------
    // Canvas2D Rendering
    // -------------------------------------------------------------------------

    fun requestCanvasRender() {
        if (::paintCanvas.isInitialized) {
            paintCanvas.requestRender()
        }
    }

    /**
     * Captures the current [paintCanvas] framebuffer as an [ImageBuffer].
     */
    fun captureCanvasSnapshot(): ImageBuffer {
        paintCanvas.dispatchVBlankFrame(16.67)
        return paintCanvas.toImageBuffer()
    }

    /**
     * Renders the background fill, visible layers, and in-progress pointer preview onto [ctx].
     */
    fun renderPaintDocument(ctx: ImageBuffer2DContext) {
        val w = ctx.width.toFloat()
        val h = ctx.height.toFloat()

        // Base canvas fill with Color 2 (backgroundHex, default #FFFFFF)
        ctx.globalAlpha = 1f
        ctx.setFillColor(RgbaColor.fromHex(backgroundHex))
        ctx.fillRect(0f, 0f, w, h)

        for (layer in _layers) {
            if (!layer.visible) continue
            val layerAlpha = (layer.opacityPercent.coerceIn(1, 100) / 100f)
            for (stroke in layer.strokes) {
                drawStrokeCommand(ctx, stroke, layerAlpha)
            }
        }

        val previewPts = activePointerPoints
        if (previewPts != null && previewPts.isNotEmpty()) {
            drawStrokeCommand(
                ctx,
                PaintStrokeCommand(
                    tool = activeTool,
                    brushStyle = activeBrushStyle,
                    shapeType = activeShapeType,
                    primaryColorHex = foregroundHex,
                    secondaryColorHex = backgroundHex,
                    strokeWidthPx = brushSizePx.toFloat(),
                    opacityPercent = brushOpacityPercent,
                    outlineEnabled = shapeOutlineEnabled,
                    fillEnabled = shapeFillEnabled,
                    points = previewPts
                ),
                1f
            )
        }
        ctx.globalAlpha = 1f
    }

    private fun drawStrokeCommand(
        ctx: ImageBuffer2DContext,
        cmd: PaintStrokeCommand,
        layerAlpha: Float
    ) {
        val baseAlpha = ((cmd.opacityPercent.coerceIn(1, 100) / 100f) * layerAlpha).coerceIn(0.02f, 1f)
        val primaryColor = RgbaColor.fromHex(cmd.primaryColorHex).withAlpha(baseAlpha)
        val secondaryColor = RgbaColor.fromHex(cmd.secondaryColorHex).withAlpha(baseAlpha)
        val strokeW = cmd.strokeWidthPx.coerceAtLeast(1f)

        ctx.globalAlpha = baseAlpha
        ctx.setStrokeColor(primaryColor)
        ctx.setFillColor(secondaryColor)
        ctx.setLineWidth(strokeW)

        when (cmd.tool) {
            PaintTool.PENCIL -> {
                if (cmd.points.isEmpty()) return
                ctx.setLineWidth(max(1f, strokeW * 0.5f))
                ctx.beginPath()
                val first = cmd.points.first()
                ctx.moveTo(first.first, first.second)
                if (cmd.points.size == 1) {
                    ctx.lineTo(first.first + 0.5f, first.second + 0.5f)
                } else {
                    for (i in 1 until cmd.points.size) {
                        val pt = cmd.points[i]
                        ctx.lineTo(pt.first, pt.second)
                    }
                }
                ctx.stroke()
            }
            PaintTool.BRUSH -> {
                if (cmd.points.isEmpty()) return
                val style = cmd.brushStyle
                val effectiveAlpha = (baseAlpha * style.opacityScale).coerceIn(0.05f, 1f)
                val brushColor = RgbaColor.fromHex(cmd.primaryColorHex).withAlpha(effectiveAlpha)
                val radius = (strokeW * 0.5f).coerceAtLeast(1f)
                if (cmd.points.size == 1) {
                    val p = cmd.points.first()
                    ctx.stampBrushStroke(
                        x0 = p.first,
                        y0 = p.second,
                        x1 = p.first,
                        y1 = p.second,
                        radius = radius,
                        hardness = style.hardness,
                        color = brushColor,
                        opacity = effectiveAlpha,
                        erase = false
                    )
                } else {
                    for (i in 1 until cmd.points.size) {
                        val p0 = cmd.points[i - 1]
                        val p1 = cmd.points[i]
                        ctx.stampBrushStroke(
                            x0 = p0.first,
                            y0 = p0.second,
                            x1 = p1.first,
                            y1 = p1.second,
                            radius = radius,
                            hardness = style.hardness,
                            color = brushColor,
                            opacity = effectiveAlpha,
                            erase = false
                        )
                    }
                }
            }
            PaintTool.ERASER -> {
                if (cmd.points.isEmpty()) return
                val eraseColor = RgbaColor.fromHex(backgroundHex)
                ctx.setStrokeColor(eraseColor)
                ctx.setLineWidth(strokeW.coerceAtLeast(4f))
                ctx.beginPath()
                val first = cmd.points.first()
                ctx.moveTo(first.first, first.second)
                for (i in 1 until cmd.points.size) {
                    val pt = cmd.points[i]
                    ctx.lineTo(pt.first, pt.second)
                }
                ctx.stroke()
            }
            PaintTool.FILL_BUCKET -> {
                val seed = cmd.points.firstOrNull() ?: (0f to 0f)
                val sx = seed.first.roundToInt().coerceIn(0, (ctx.width - 1).coerceAtLeast(0))
                val sy = seed.second.roundToInt().coerceIn(0, (ctx.height - 1).coerceAtLeast(0))
                ctx.floodFill(sx, sy, primaryColor, tolerance = 24)
            }
            PaintTool.TEXT -> {
                val anchor = cmd.points.firstOrNull() ?: (12f to 24f)
                ctx.setFillColor(primaryColor)
                ctx.setFont("Segoe UI", max(12f, strokeW * 2f), fontWeight = 600)
                ctx.fillText(cmd.textContent.ifBlank { "Paint" }, anchor.first, anchor.second)
            }
            PaintTool.SELECTION -> {
                if (cmd.points.size < 2) return
                val p0 = cmd.points.first()
                val p1 = cmd.points.last()
                val x = min(p0.first, p1.first)
                val y = min(p0.second, p1.second)
                val rw = abs(p1.first - p0.first).coerceAtLeast(2f)
                val rh = abs(p1.second - p0.second).coerceAtLeast(2f)
                ctx.setStrokeColor(RgbaColor.fromHex("#60A5FA"))
                ctx.setLineWidth(1f)
                ctx.setLineDash(floatArrayOf(4f, 3f))
                ctx.strokeRect(x, y, rw, rh)
                ctx.setLineDash(FloatArray(0))
            }
            PaintTool.SHAPE -> {
                if (cmd.points.isEmpty()) return
                drawGeometricShape(ctx, cmd, primaryColor, secondaryColor, strokeW)
            }
            PaintTool.COLOR_PICKER, PaintTool.MAGNIFIER -> Unit
        }
    }

    private fun drawGeometricShape(
        ctx: ImageBuffer2DContext,
        cmd: PaintStrokeCommand,
        outlineColor: RgbaColor,
        fillColor: RgbaColor,
        strokeW: Float
    ) {
        val p0 = cmd.points.first()
        val p1 = cmd.points.last()
        val left = min(p0.first, p1.first)
        val top = min(p0.second, p1.second)
        val right = max(p0.first, p1.first).coerceAtLeast(left + 4f)
        val bottom = max(p0.second, p1.second).coerceAtLeast(top + 4f)
        val w = right - left
        val h = bottom - top
        val cx = left + w * 0.5f
        val cy = top + h * 0.5f

        ctx.setStrokeColor(outlineColor)
        ctx.setFillColor(fillColor)
        ctx.setLineWidth(strokeW)

        when (cmd.shapeType) {
            PaintShapeType.LINE -> {
                ctx.beginPath()
                ctx.moveTo(p0.first, p0.second)
                ctx.lineTo(p1.first, p1.second)
                ctx.stroke()
                return
            }
            PaintShapeType.CURVE -> {
                ctx.beginPath()
                ctx.moveTo(p0.first, p0.second)
                ctx.bezierCurveTo(
                    left + w * 0.3f,
                    top - h * 0.35f,
                    left + w * 0.7f,
                    bottom + h * 0.35f,
                    p1.first,
                    p1.second
                )
                ctx.stroke()
                return
            }
            PaintShapeType.RECTANGLE -> {
                if (cmd.fillEnabled) ctx.fillRect(left, top, w, h)
                if (cmd.outlineEnabled) ctx.strokeRect(left, top, w, h)
                return
            }
            PaintShapeType.ROUNDED_RECTANGLE -> {
                val radius = min(w, h) * 0.22f
                ctx.beginPath()
                ctx.roundRect(left, top, w, h, radius)
                if (cmd.fillEnabled) ctx.fill()
                if (cmd.outlineEnabled) ctx.stroke()
                return
            }
            PaintShapeType.OVAL -> {
                ctx.beginPath()
                ctx.ellipse(cx, cy, (w * 0.5f).coerceAtLeast(2f), (h * 0.5f).coerceAtLeast(2f))
                if (cmd.fillEnabled) ctx.fill()
                if (cmd.outlineEnabled) ctx.stroke()
                return
            }
            PaintShapeType.TRIANGLE -> {
                tracePolygon(ctx, listOf(cx to top, right to bottom, left to bottom))
            }
            PaintShapeType.RIGHT_TRIANGLE -> {
                tracePolygon(ctx, listOf(left to top, right to bottom, left to bottom))
            }
            PaintShapeType.DIAMOND -> {
                tracePolygon(ctx, listOf(cx to top, right to cy, cx to bottom, left to cy))
            }
            PaintShapeType.PENTAGON, PaintShapeType.POLYGON -> {
                traceRegularPolygon(ctx, cx, cy, w * 0.5f, h * 0.5f, sides = 5)
            }
            PaintShapeType.HEXAGON -> {
                traceRegularPolygon(ctx, cx, cy, w * 0.5f, h * 0.5f, sides = 6)
            }
            PaintShapeType.RIGHT_ARROW -> {
                tracePolygon(
                    ctx,
                    listOf(
                        left to (top + h * 0.3f),
                        (left + w * 0.6f) to (top + h * 0.3f),
                        (left + w * 0.6f) to top,
                        right to cy,
                        (left + w * 0.6f) to bottom,
                        (left + w * 0.6f) to (top + h * 0.7f),
                        left to (top + h * 0.7f)
                    )
                )
            }
            PaintShapeType.LEFT_ARROW -> {
                tracePolygon(
                    ctx,
                    listOf(
                        right to (top + h * 0.3f),
                        (left + w * 0.4f) to (top + h * 0.3f),
                        (left + w * 0.4f) to top,
                        left to cy,
                        (left + w * 0.4f) to bottom,
                        (left + w * 0.4f) to (top + h * 0.7f),
                        right to (top + h * 0.7f)
                    )
                )
            }
            PaintShapeType.UP_ARROW -> {
                tracePolygon(
                    ctx,
                    listOf(
                        cx to top,
                        right to (top + h * 0.4f),
                        (left + w * 0.7f) to (top + h * 0.4f),
                        (left + w * 0.7f) to bottom,
                        (left + w * 0.3f) to bottom,
                        (left + w * 0.3f) to (top + h * 0.4f),
                        left to (top + h * 0.4f)
                    )
                )
            }
            PaintShapeType.DOWN_ARROW -> {
                tracePolygon(
                    ctx,
                    listOf(
                        (left + w * 0.3f) to top,
                        (left + w * 0.7f) to top,
                        (left + w * 0.7f) to (top + h * 0.6f),
                        right to (top + h * 0.6f),
                        cx to bottom,
                        left to (top + h * 0.6f),
                        (left + w * 0.3f) to (top + h * 0.6f)
                    )
                )
            }
            PaintShapeType.FOUR_POINT_STAR -> {
                traceStar(ctx, cx, cy, w * 0.5f, h * 0.5f, points = 4)
            }
            PaintShapeType.FIVE_POINT_STAR -> {
                traceStar(ctx, cx, cy, w * 0.5f, h * 0.5f, points = 5)
            }
            PaintShapeType.SIX_POINT_STAR -> {
                traceStar(ctx, cx, cy, w * 0.5f, h * 0.5f, points = 6)
            }
            PaintShapeType.SPEECH_BUBBLE -> {
                val bodyBottom = top + h * 0.78f
                tracePolygon(
                    ctx,
                    listOf(
                        left to top,
                        right to top,
                        right to bodyBottom,
                        (left + w * 0.48f) to bodyBottom,
                        (left + w * 0.25f) to bottom,
                        (left + w * 0.28f) to bodyBottom,
                        left to bodyBottom
                    )
                )
            }
            PaintShapeType.HEART -> {
                val r = hypot(w, h) * 0.25f
                ctx.beginPath()
                ctx.moveTo(cx, bottom)
                ctx.bezierCurveTo(left - r * 0.2f, cy, left, top, cx, top + h * 0.28f)
                ctx.bezierCurveTo(right, top, right + r * 0.2f, cy, cx, bottom)
                ctx.closePath()
            }
        }

        if (cmd.fillEnabled) ctx.fill()
        if (cmd.outlineEnabled) ctx.stroke()
    }

    private fun tracePolygon(ctx: ImageBuffer2DContext, vertices: List<Pair<Float, Float>>) {
        if (vertices.isEmpty()) return
        ctx.beginPath()
        ctx.moveTo(vertices[0].first, vertices[0].second)
        for (i in 1 until vertices.size) {
            ctx.lineTo(vertices[i].first, vertices[i].second)
        }
        ctx.closePath()
    }

    private fun traceRegularPolygon(
        ctx: ImageBuffer2DContext,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        sides: Int
    ) {
        val pts = ArrayList<Pair<Float, Float>>(sides)
        val startAngle = -PI / 2.0
        for (i in 0 until sides) {
            val angle = startAngle + (2.0 * PI * i) / sides
            pts.add((cx + (rx * cos(angle)).toFloat()) to (cy + (ry * sin(angle)).toFloat()))
        }
        tracePolygon(ctx, pts)
    }

    private fun traceStar(
        ctx: ImageBuffer2DContext,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        points: Int
    ) {
        val totalVertices = points * 2
        val pts = ArrayList<Pair<Float, Float>>(totalVertices)
        val startAngle = -PI / 2.0
        for (i in 0 until totalVertices) {
            val factor = if (i % 2 == 0) 1.0f else 0.42f
            val angle = startAngle + (PI * i) / points
            pts.add((cx + (rx * factor * cos(angle)).toFloat()) to (cy + (ry * factor * sin(angle)).toFloat()))
        }
        tracePolygon(ctx, pts)
    }

    private fun syncAllControls() {
        if (!::paintCanvas.isInitialized) return

        // Tool button visual highlights
        val toolMap = mapOf(
            PaintTool.SELECTION to selectionToolButton,
            PaintTool.PENCIL to pencilToolButton,
            PaintTool.FILL_BUCKET to fillToolButton,
            PaintTool.TEXT to textToolButton,
            PaintTool.ERASER to eraserToolButton,
            PaintTool.COLOR_PICKER to colorPickerToolButton,
            PaintTool.MAGNIFIER to magnifierToolButton,
            PaintTool.BRUSH to brushToolButton
        )
        for ((tool, btn) in toolMap) {
            val selected = activeTool == tool
            btn.getBootstrap().apply {
                type = if (selected) BootstrapType.PRIMARY else BootstrapType.SECONDARY
                outline = !selected
            }
        }

        // Shape button visual highlights
        for ((shape, btn) in shapeButtons) {
            val selected = activeTool == PaintTool.SHAPE && activeShapeType == shape
            btn.getBootstrap().apply {
                type = if (selected) BootstrapType.PRIMARY else BootstrapType.SECONDARY
                outline = !selected
            }
        }

        shapeOutlineButton.getBootstrap().apply {
            type = if (shapeOutlineEnabled) BootstrapType.PRIMARY else BootstrapType.SECONDARY
            outline = !shapeOutlineEnabled
        }
        shapeFillButton.getBootstrap().apply {
            type = if (shapeFillEnabled) BootstrapType.PRIMARY else BootstrapType.SECONDARY
            outline = !shapeFillEnabled
        }

        // Active Foreground / Background swatches
        val fgColor = RgbaColor.fromHex(foregroundHex)
        val fgIsLight = (fgColor.r * 299 + fgColor.g * 587 + fgColor.b * 114) / 1000 >= 160
        foregroundSwatchButton.getStyle().setBackgroundColor(fgColor)
        foregroundSwatchButton.tooltipText = "Color 1 (Foreground): $foregroundHex"
        foregroundSwatchButton.getBootstrap().apply {
            type = if (fgIsLight) BootstrapType.LIGHT else if (editingForegroundColor) BootstrapType.PRIMARY else BootstrapType.DARK
            outline = false
        }

        val bgColor = RgbaColor.fromHex(backgroundHex)
        val bgIsLight = (bgColor.r * 299 + bgColor.g * 587 + bgColor.b * 114) / 1000 >= 160
        backgroundSwatchButton.getStyle().setBackgroundColor(bgColor)
        backgroundSwatchButton.tooltipText = "Color 2 (Background): $backgroundHex"
        backgroundSwatchButton.getBootstrap().apply {
            type = if (bgIsLight) BootstrapType.LIGHT else if (!editingForegroundColor) BootstrapType.PRIMARY else BootstrapType.DARK
            outline = false
        }

        // Left vertical sliders
        brushSizeBadge.text = "${brushSizePx}px"
        brushOpacityBadge.text = "${brushOpacityPercent}%"

        // Layers sidebar visibility
        layersToggleButton.getBootstrap().apply {
            type = if (layersPanelOpen) BootstrapType.PRIMARY else BootstrapType.SECONDARY
            outline = !layersPanelOpen
        }
        layersSidebar.isVisible = layersPanelOpen
        layersSidebar.getStyle().setDisplay(if (layersPanelOpen) CssDisplay.BLOCK else CssDisplay.NONE)
        deleteLayerButton.isEnabled = _layers.size > 1
        mergeDownLayerButton.isEnabled = activeLayerIndex > 0 && _layers.size > 1

        // Undo / Redo states
        undoButton.isEnabled = totalStrokeCount > 0
        redoButton.isEnabled = _redoStack.isNotEmpty()

        // Bottom Status Bar
        cursorPositionLabel.text = "+ $lastCursorX, ${lastCursorY}px"
        selectionSizeLabel.text = "[] $selectionDimensionsText"
        canvasSizeBadge.text = "$canvasWidthPx × ${canvasHeightPx}px"
        activeToolBadge.text = "Tool: ${activeTool.name}"
    }

    private fun normalizeHex(hex: String): String {
        val color = RgbaColor.fromHex(hex)
        return color.toHex(includeAlpha = false).uppercase()
    }

    companion object {
        const val BINARY_MAGIC: Int = 0x47555054 // "GUPT" (GenericUIPaint)
        const val BINARY_VERSION: Int = 1
    }
}
