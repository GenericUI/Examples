package com.nexustools.genericui.examples.sketchpad

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.dialog.Dialogs
import com.nexustools.genericui.component.dialog.FileTypeFilter
import com.nexustools.genericui.component.form.ColorPicker
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.media.Canvas2DContext
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.event.PointerEvent
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtSurface
import com.nexustools.genericui.ext.canvas2d.CurveEditorExt
import com.nexustools.genericui.ext.canvas2d.dsl.canvas2DExt
import com.nexustools.genericui.ext.canvas2d.dsl.curveEditorExt
import com.nexustools.genericui.ext.dsl.badge
import com.nexustools.genericui.ext.dsl.build
import com.nexustools.genericui.ext.dsl.button
import com.nexustools.genericui.ext.dsl.card
import com.nexustools.genericui.ext.dsl.component
import com.nexustools.genericui.ext.dsl.h5
import com.nexustools.genericui.ext.dsl.hstack
import com.nexustools.genericui.ext.dsl.label
import com.nexustools.genericui.ext.dsl.rangeSlider
import com.nexustools.genericui.ext.dsl.vstack
import com.nexustools.genericui.geometry.Point
import com.nexustools.genericui.image.ImageBuffer
import com.nexustools.genericui.style.RgbaColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Interactive 2D vector and cubic-Bezier sketchpad [Fragment] built with `:extensions:canvas2d`
 * ([Canvas2DExtSurface] and [CurveEditorExt]) and hardware VBlank rendering (`registerVBlankRender`).
 */
class SketchpadFragment : Fragment() {

    private val _commands = mutableListOf<SketchVectorCommand>()
    val commands: List<SketchVectorCommand>
        get() = _commands.toList()

    private val _redoStack = mutableListOf<SketchVectorCommand>()
    val redoCount: Int
        get() = _redoStack.size

    var activeTool: SketchShapeType = SketchShapeType.FREEHAND
        private set

    var activeStrokeWidth: Float = 4f
        private set

    var activeColorHex: String = "#38BDF8"
        private set

    var renderedFrameCount: Long = 0L
        private set

    var lastFrameDeltaMsValue: Double = 0.0
        private set

    private var activeStrokePoints: MutableList<Pair<Float, Float>>? = null
    private var sampleCounter: Int = 0

    // Toolbar Tool Buttons
    lateinit var freehandToolButton: Button
    lateinit var rectToolButton: Button
    lateinit var ellipseToolButton: Button
    lateinit var lineToolButton: Button
    lateinit var bezierToolButton: Button

    // Action Buttons
    lateinit var addSampleShapeButton: Button
    lateinit var undoButton: Button
    lateinit var redoButton: Button
    lateinit var clearButton: Button
    lateinit var exportImageButton: Button

    // Form Controls & Canvases
    lateinit var strokeWidthSlider: RangeSlider
    val strokeColorPicker: ColorPicker = ColorPicker(RgbaColor.fromHex("#38BDF8")).apply {
        id = "picker-stroke-color"
    }
    lateinit var sketchCanvas: Canvas2DExtSurface
    lateinit var curveEditor: CurveEditorExt

    // Status Badges & Labels
    lateinit var shapeCountBadge: Badge
    lateinit var activeToolBadge: Badge
    lateinit var strokeWidthBadge: Badge
    lateinit var vblankFrameBadge: Badge
    lateinit var statusLabel: Label

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("sketchpad-root")
        buildUi()
        syncStatusControls()
    }

    private fun buildUi() {
        val self = this
        build(clearFirst = true) {
            vstack(classes = "gap-2") {
                // Top Toolbar
                card(classes = "sketchpad-toolbar") {
                    hstack(classes = "gap-2 flex-wrap align-items-center") {
                        h5("Canvas2D Vector Sketchpad", classes = "mb-0 me-2")

                        self.freehandToolButton = button(
                            text = "Freehand",
                            type = BootstrapType.PRIMARY,
                            size = BootstrapSize.SM,
                            id = "btn-tool-freehand",
                            onClick = { self.selectTool(SketchShapeType.FREEHAND) }
                        )
                        self.rectToolButton = button(
                            text = "Rectangle",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-tool-rect",
                            onClick = { self.selectTool(SketchShapeType.RECTANGLE) }
                        )
                        self.ellipseToolButton = button(
                            text = "Ellipse",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-tool-ellipse",
                            onClick = { self.selectTool(SketchShapeType.ELLIPSE) }
                        )
                        self.lineToolButton = button(
                            text = "Line",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-tool-line",
                            onClick = { self.selectTool(SketchShapeType.LINE) }
                        )
                        self.bezierToolButton = button(
                            text = "Bezier Curve",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-tool-bezier",
                            onClick = { self.selectTool(SketchShapeType.BEZIER_CURVE) }
                        )

                        self.addSampleShapeButton = button(
                            text = "Add Sample Shape",
                            type = BootstrapType.SUCCESS,
                            size = BootstrapSize.SM,
                            id = "btn-add-shape",
                            onClick = { self.addSampleShape() }
                        )
                        self.undoButton = button(
                            text = "Undo",
                            type = BootstrapType.WARNING,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-undo",
                            onClick = { self.undo() }
                        )
                        self.redoButton = button(
                            text = "Redo",
                            type = BootstrapType.INFO,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-redo",
                            onClick = { self.redo() }
                        )
                        self.clearButton = button(
                            text = "Clear",
                            type = BootstrapType.DANGER,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-clear",
                            onClick = { self.clearCanvas() }
                        )
                        self.exportImageButton = button(
                            text = "Export PNG",
                            type = BootstrapType.LIGHT,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-export-png",
                            onClick = { self.promptExportCanvas() }
                        )
                    }
                }

                // Telemetry & Status Strip
                hstack(classes = "gap-2 align-items-center") {
                    self.activeToolBadge = badge(
                        text = "Tool: FREEHAND",
                        type = BootstrapType.PRIMARY,
                        pill = true,
                        id = "badge-active-tool"
                    )
                    self.shapeCountBadge = badge(
                        text = "Shapes: 0",
                        type = BootstrapType.INFO,
                        pill = true,
                        id = "badge-shape-count"
                    )
                    self.strokeWidthBadge = badge(
                        text = "Stroke: 4px",
                        type = BootstrapType.SECONDARY,
                        pill = true,
                        id = "badge-stroke-width"
                    )
                    self.vblankFrameBadge = badge(
                        text = "VBlank Frames: 0",
                        type = BootstrapType.DARK,
                        pill = true,
                        id = "badge-vblank-frames"
                    )
                    self.statusLabel = label(
                        text = "Ready to sketch on hardware-accelerated Canvas2D.",
                        id = "label-sketchpad-status"
                    )
                }

                // Main Workspace: Canvas2D Surface + Inspector Sidebar
                hstack(classes = "sketchpad-workspace gap-3 align-items-start") {
                    self.sketchCanvas = canvas2DExt(
                        width = 720,
                        height = 460,
                        id = "sketchpad-canvas-surface",
                        classes = "sketchpad-canvas",
                        onRender = { ctx, deltaMs, _, frameNum ->
                            self.renderedFrameCount = max(self.renderedFrameCount + 1L, frameNum)
                            self.lastFrameDeltaMsValue = deltaMs
                            self.vblankFrameBadge.text = "VBlank Frames: ${self.renderedFrameCount}"
                            self.renderSketchpadScene(ctx)
                        }
                    ) {
                        onPointerDown = { ev -> self.handlePointerDown(ev) }
                        onPointerMove = { ev -> self.handlePointerMove(ev) }
                        onPointerUp = { ev -> self.handlePointerUp(ev) }
                    }

                    // Right Inspector Sidebar
                    vstack(classes = "sketchpad-sidebar gap-2") {
                        label("Stroke Width (1px - 24px)")
                        self.strokeWidthSlider = rangeSlider(
                            minValue = 4.0,
                            maxValue = 24.0,
                            min = 1.0,
                            max = 24.0,
                            step = 1.0,
                            id = "slider-stroke-width"
                        ) {
                            onValueChanged = { updated ->
                                self.activeStrokeWidth = updated.toFloat().coerceIn(1f, 24f)
                                self.syncStatusControls()
                                self.sketchCanvas.requestRender()
                            }
                        }

                        label("Stroke Color Hex")
                        self.strokeColorPicker.onColorChanged = { chosen ->
                            self.activeColorHex = chosen.toHexString()
                            self.syncStatusControls()
                            self.sketchCanvas.requestRender()
                        }
                        self.strokeColorPicker.onValueChanged = { text ->
                            val parsed = RgbaColor.fromCss(text)
                            if (parsed != null) {
                                self.activeColorHex = parsed.toHexString()
                                self.syncStatusControls()
                                self.sketchCanvas.requestRender()
                            }
                        }
                        component(self.strokeColorPicker)

                        label("Cubic-Bezier Taper Curve (CurveEditorExt)")
                        self.curveEditor = curveEditorExt(
                            width = 220,
                            height = 180,
                            controlPoint1 = Point(0.35f, 0.15f),
                            controlPoint2 = Point(0.65f, 0.85f),
                            id = "curve-editor-bezier",
                            onCurveChanged = { cp1, cp2 ->
                                self.statusLabel.text = "Bezier curve updated: CP1(${cp1.x}, ${cp1.y}), CP2(${cp2.x}, ${cp2.y})"
                                self.sketchCanvas.requestRender()
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Switches the active drawing tool and updates toolbar button visual states.
     */
    fun selectTool(tool: SketchShapeType) {
        activeTool = tool
        val buttonMap = mapOf(
            SketchShapeType.FREEHAND to freehandToolButton,
            SketchShapeType.RECTANGLE to rectToolButton,
            SketchShapeType.ELLIPSE to ellipseToolButton,
            SketchShapeType.LINE to lineToolButton,
            SketchShapeType.BEZIER_CURVE to bezierToolButton
        )
        for ((shapeType, btn) in buttonMap) {
            val isSelected = shapeType == tool
            btn.type = if (isSelected) BootstrapType.PRIMARY else BootstrapType.SECONDARY
            btn.outline = !isSelected
        }
        statusLabel.text = "Selected tool: ${tool.displayName}"
        syncStatusControls()
    }

    /**
     * Appends a vector command, clears the redo stack, and requests a canvas VBlank render.
     */
    fun addVectorCommand(command: SketchVectorCommand) {
        _commands.add(command)
        _redoStack.clear()
        syncStatusControls()
        sketchCanvas.requestRender()
    }

    /**
     * Adds a deterministic sample shape using the currently selected [activeTool], [activeColorHex],
     * and [activeStrokeWidth].
     */
    fun addSampleShape(): SketchVectorCommand {
        val index = sampleCounter++
        val baseX = 70f + (index % 5) * 110f
        val baseY = 70f + ((index / 5) % 3) * 110f
        val palette = listOf("#38BDF8", "#A855F7", "#10B981", "#F59E0B", "#EC4899")
        val color = if (index == 0) activeColorHex else palette[index % palette.size]

        val points = when (activeTool) {
            SketchShapeType.FREEHAND -> listOf(
                baseX to baseY,
                (baseX + 25f) to (baseY - 18f),
                (baseX + 55f) to (baseY + 22f),
                (baseX + 85f) to (baseY - 10f)
            )
            SketchShapeType.RECTANGLE -> listOf(
                baseX to baseY,
                (baseX + 90f) to (baseY + 64f)
            )
            SketchShapeType.ELLIPSE -> listOf(
                (baseX + 45f) to (baseY + 35f),
                (baseX + 90f) to (baseY + 70f)
            )
            SketchShapeType.LINE -> listOf(
                baseX to baseY,
                (baseX + 90f) to (baseY + 65f)
            )
            SketchShapeType.BEZIER_CURVE -> listOf(
                baseX to (baseY + 50f),
                (baseX + 25f) to (baseY - 30f),
                (baseX + 65f) to (baseY + 90f),
                (baseX + 95f) to (baseY + 10f)
            )
        }
        val cmd = SketchVectorCommand(
            type = activeTool,
            colorHex = color,
            strokeWidth = activeStrokeWidth,
            points = points
        )
        addVectorCommand(cmd)
        statusLabel.text = "Added ${activeTool.displayName} (#${_commands.size})"
        return cmd
    }

    /**
     * Simulates a complete pointer down -> move -> up drawing gesture on the canvas.
     */
    fun simulateStrokeGesture(points: List<Pair<Float, Float>>) {
        if (points.isEmpty()) return
        val first = points.first()
        handlePointerDown(PointerEvent(x = first.first, y = first.second, buttonsMask = 1))
        for (i in 1 until points.size) {
            val pt = points[i]
            handlePointerMove(PointerEvent(x = pt.first, y = pt.second, buttonsMask = 1))
        }
        val last = points.last()
        handlePointerUp(PointerEvent(x = last.first, y = last.second, buttonsMask = 0))
    }

    /**
     * Undoes the most recent vector command and pushes it onto the redo stack.
     */
    fun undo(): Boolean {
        if (_commands.isEmpty()) return false
        val removed = _commands.removeAt(_commands.lastIndex)
        _redoStack.add(removed)
        statusLabel.text = "Undid ${removed.type.displayName} (${_commands.size} remaining)"
        syncStatusControls()
        sketchCanvas.requestRender()
        return true
    }

    /**
     * Redoes the most recently undone vector command.
     */
    fun redo(): Boolean {
        if (_redoStack.isEmpty()) return false
        val restored = _redoStack.removeAt(_redoStack.lastIndex)
        _commands.add(restored)
        statusLabel.text = "Redid ${restored.type.displayName} (${_commands.size} total)"
        syncStatusControls()
        sketchCanvas.requestRender()
        return true
    }

    /**
     * Clears all drawn vector shapes and resets the undo/redo stacks.
     */
    fun clearCanvas() {
        _commands.clear()
        _redoStack.clear()
        activeStrokePoints = null
        statusLabel.text = "Cleared canvas"
        syncStatusControls()
        sketchCanvas.requestRender()
    }

    /**
     * Captures the current [sketchCanvas] framebuffer as an [ImageBuffer].
     */
    fun captureSnapshotBuffer(): ImageBuffer {
        sketchCanvas.dispatchVBlankFrame(16.67)
        return sketchCanvas.toImageBuffer()
    }

    private fun handlePointerDown(event: PointerEvent) {
        activeStrokePoints = mutableListOf(event.x to event.y)
    }

    private fun handlePointerMove(event: PointerEvent) {
        val pts = activeStrokePoints ?: return
        pts.add(event.x to event.y)
        sketchCanvas.requestRender()
    }

    private fun handlePointerUp(event: PointerEvent) {
        val pts = activeStrokePoints ?: return
        pts.add(event.x to event.y)
        activeStrokePoints = null
        if (pts.size >= 2) {
            addVectorCommand(
                SketchVectorCommand(
                    type = activeTool,
                    colorHex = activeColorHex,
                    strokeWidth = activeStrokeWidth,
                    points = pts.toList()
                )
            )
        }
    }

    private fun promptExportCanvas() {
        Dialogs.saveFile(
            title = "Export Sketchpad PNG",
            suggestedFileName = "sketchpad-drawing.png",
            filters = listOf(FileTypeFilter("PNG Images", listOf("png"))),
            ownerWindow = attachedWindow,
            onSelected = { file ->
                statusLabel.text = "Exported canvas to ${file.name}"
            }
        )
    }

    private fun syncStatusControls() {
        if (!::shapeCountBadge.isInitialized) return
        shapeCountBadge.text = "Shapes: ${_commands.size}"
        activeToolBadge.text = "Tool: ${activeTool.name}"
        strokeWidthBadge.text = "Stroke: ${activeStrokeWidth.toInt()}px"
        undoButton.isEnabled = _commands.isNotEmpty()
        redoButton.isEnabled = _redoStack.isNotEmpty()
    }

    /**
     * Renders the background grid and all [SketchVectorCommand] shapes onto [ctx].
     */
    fun renderSketchpadScene(ctx: Canvas2DContext) {
        val w = ctx.width.toFloat()
        val h = ctx.height.toFloat()

        // Dark slate background
        ctx.setFillColor(RgbaColor(11, 17, 32, 255))
        ctx.fillRect(0f, 0f, w, h)

        // Subtle coordinate grid
        ctx.setStrokeColor(RgbaColor(30, 41, 59, 255))
        ctx.setLineWidth(1f)
        var gx = 40f
        while (gx < w) {
            ctx.beginPath()
            ctx.moveTo(gx, 0f)
            ctx.lineTo(gx, h)
            ctx.stroke()
            gx += 40f
        }
        var gy = 40f
        while (gy < h) {
            ctx.beginPath()
            ctx.moveTo(0f, gy)
            ctx.lineTo(w, gy)
            ctx.stroke()
            gy += 40f
        }

        val curveMidGain = if (::curveEditor.isInitialized) {
            curveEditor.sampleCurveY(0.5f).coerceIn(0.25f, 2.0f)
        } else {
            1.0f
        }

        for (cmd in _commands) {
            drawCommand(ctx, cmd, curveMidGain)
        }

        val inProgress = activeStrokePoints
        if (inProgress != null && inProgress.size >= 2) {
            drawCommand(
                ctx,
                SketchVectorCommand(
                    type = activeTool,
                    colorHex = activeColorHex,
                    strokeWidth = activeStrokeWidth,
                    points = inProgress
                ),
                curveMidGain
            )
        }
    }

    private fun drawCommand(ctx: Canvas2DContext, cmd: SketchVectorCommand, curveGain: Float) {
        if (cmd.points.isEmpty()) return
        val color = RgbaColor.fromHex(cmd.colorHex)
        val effectiveWidth = (cmd.strokeWidth * (0.5f + curveGain)).coerceAtLeast(1f)
        ctx.setStrokeColor(color)
        ctx.setFillColor(color.withAlpha(0.22f))
        ctx.setLineWidth(effectiveWidth)

        when (cmd.type) {
            SketchShapeType.FREEHAND -> {
                ctx.beginPath()
                val first = cmd.points.first()
                ctx.moveTo(first.first, first.second)
                for (i in 1 until cmd.points.size) {
                    val pt = cmd.points[i]
                    ctx.lineTo(pt.first, pt.second)
                }
                ctx.stroke()
            }
            SketchShapeType.LINE -> {
                val p0 = cmd.points.first()
                val p1 = cmd.points.last()
                ctx.beginPath()
                ctx.moveTo(p0.first, p0.second)
                ctx.lineTo(p1.first, p1.second)
                ctx.stroke()
            }
            SketchShapeType.RECTANGLE -> {
                val p0 = cmd.points.first()
                val p1 = cmd.points.last()
                val x = min(p0.first, p1.first)
                val y = min(p0.second, p1.second)
                val rw = abs(p1.first - p0.first).coerceAtLeast(4f)
                val rh = abs(p1.second - p0.second).coerceAtLeast(4f)
                ctx.fillRect(x, y, rw, rh)
                ctx.strokeRect(x, y, rw, rh)
            }
            SketchShapeType.ELLIPSE -> {
                val p0 = cmd.points.first()
                val p1 = cmd.points.last()
                val radius = hypot(p1.first - p0.first, p1.second - p0.second).coerceIn(8f, 120f)
                ctx.beginPath()
                ctx.arc(p0.first, p0.second, radius, 0f, (PI * 2.0).toFloat(), false)
                ctx.fill()
                ctx.stroke()
            }
            SketchShapeType.BEZIER_CURVE -> {
                val p0 = cmd.points.first()
                val p3 = cmd.points.last()
                val cp1 = if (cmd.points.size >= 3) {
                    cmd.points[1]
                } else {
                    (p0.first + (p3.first - p0.first) * curveEditor.controlPoint1.x) to
                        (p0.second - 60f * curveEditor.controlPoint1.y)
                }
                val cp2 = if (cmd.points.size >= 4) {
                    cmd.points[2]
                } else {
                    (p0.first + (p3.first - p0.first) * curveEditor.controlPoint2.x) to
                        (p3.second + 60f * curveEditor.controlPoint2.y)
                }
                ctx.beginPath()
                ctx.moveTo(p0.first, p0.second)
                ctx.bezierCurveTo(cp1.first, cp1.second, cp2.first, cp2.second, p3.first, p3.second)
                ctx.stroke()
            }
        }
    }
}
