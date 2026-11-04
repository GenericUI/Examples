package com.nexustools.genericui.examples.paint

import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer
import com.nexustools.genericui.style.css.CssCustomCursor
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Cross-platform `JestElectricRunner` suite for `GenericUIPaint` (Windows 11 Dark-Mode Paint clone).
 */
class GenericUIPaintJestTest {

    @Test
    fun runGenericUIPaintJestElectricSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "GenericUIPaint Windows 11 Dark-Mode Paint JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Initial 131x128 Canvas, Ribbon Tools, & 20-Shape Gallery") {
                it("launches with 131x128px canvas, switches tools (PENCIL, FILL_BUCKET, ERASER, SHAPE), and selects shapes (RECTANGLE, OVAL, FIVE_POINT_STAR)") {
                    val controller = launchApp(visualOnWindows = false) { GenericUIPaintApp() }
                    val fragment = controller.app.paintFragment
                    fragment.getPreferenceStore().remove(PaintPreferences.PREFERENCE_KEY)
                    fragment.handleNewCanvas()

                    expect(fragment.canvasWidthPx).toBe(131)
                    expect(fragment.canvasHeightPx).toBe(128)
                    expectNode(controller.findById("badge-canvas-size")).toHaveText("131 × 128px")
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: BRUSH")
                    expectNode(controller.findById("btn-undo")).toBeDisabled()
                    expectNode(controller.findById("btn-redo")).toBeDisabled()

                    val initialCursor = fragment.paintCanvas.getStyle().getCursor() as? CssCustomCursor
                    assertNotNull(initialCursor)

                    // Select PENCIL tool
                    controller.findById("btn-tool-pencil").performClick()
                    expect(fragment.activeTool).toBe(PaintTool.PENCIL)
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: PENCIL")
                    expect(fragment.pencilToolButton.getBootstrap().type).toBe(BootstrapType.PRIMARY)

                    // Select FILL_BUCKET tool
                    controller.findById("btn-tool-fill").performClick()
                    expect(fragment.activeTool).toBe(PaintTool.FILL_BUCKET)
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: FILL_BUCKET")
                    expect(fragment.fillToolButton.getBootstrap().type).toBe(BootstrapType.PRIMARY)

                    // Select ERASER tool
                    controller.findById("btn-tool-eraser").performClick()
                    expect(fragment.activeTool).toBe(PaintTool.ERASER)
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: ERASER")
                    expect(fragment.eraserToolButton.getBootstrap().type).toBe(BootstrapType.PRIMARY)

                    // Select shapes from the 20-shape gallery: RECTANGLE, OVAL, FIVE_POINT_STAR
                    controller.findById("btn-shape-rectangle").performClick()
                    expect(fragment.activeTool).toBe(PaintTool.SHAPE)
                    expect(fragment.activeShapeType).toBe(PaintShapeType.RECTANGLE)
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: SHAPE")

                    controller.findById("btn-shape-oval").performClick()
                    expect(fragment.activeTool).toBe(PaintTool.SHAPE)
                    expect(fragment.activeShapeType).toBe(PaintShapeType.OVAL)

                    controller.findById("btn-shape-five_point_star").performClick()
                    expect(fragment.activeTool).toBe(PaintTool.SHAPE)
                    expect(fragment.activeShapeType).toBe(PaintShapeType.FIVE_POINT_STAR)
                    expect(PaintShapeType.entries.size).toBe(20)
                }

                it("draws strokes, shapes, and fills with multi-step Undo and Redo") {
                    val controller = launchApp(visualOnWindows = false) { GenericUIPaintApp() }
                    val fragment = controller.app.paintFragment
                    fragment.handleNewCanvas()

                    // Draw a Pencil stroke
                    controller.findById("btn-tool-pencil").performClick()
                    fragment.simulateStrokeGesture(
                        listOf(10f to 12f, 45f to 48f, 90f to 64f)
                    )
                    expect(fragment.totalStrokeCount).toBe(1)
                    expect(fragment.activeLayer.strokes.first().tool).toBe(PaintTool.PENCIL)
                    expectNode(controller.findById("btn-undo")).toBeEnabled()

                    // Draw a 5-Point Star shape
                    controller.findById("btn-shape-five_point_star").performClick()
                    fragment.simulateStrokeGesture(
                        listOf(20f to 20f, 100f to 100f)
                    )
                    expect(fragment.totalStrokeCount).toBe(2)
                    expect(fragment.activeLayer.strokes.last().shapeType).toBe(PaintShapeType.FIVE_POINT_STAR)

                    // Undo both commands and redo one
                    controller.findById("btn-undo").performClick()
                    expect(fragment.totalStrokeCount).toBe(1)
                    expect(fragment.redoCount).toBe(1)
                    expectNode(controller.findById("btn-redo")).toBeEnabled()

                    controller.findById("btn-undo").performClick()
                    expect(fragment.totalStrokeCount).toBe(0)
                    expect(fragment.redoCount).toBe(2)

                    controller.findById("btn-redo").performClick()
                    expect(fragment.totalStrokeCount).toBe(1)
                    expect(fragment.redoCount).toBe(1)

                    // Render VBlank frame and verify snapshot dimensions
                    val snapshot = fragment.captureCanvasSnapshot()
                    expect(snapshot.width).toBe(131)
                    expect(snapshot.height).toBe(128)
                }

                it("updates foreground/background palette colours, left vertical sliders, and Layers panel") {
                    val controller = launchApp(visualOnWindows = false) { GenericUIPaintApp() }
                    val fragment = controller.app.paintFragment
                    fragment.handleNewCanvas()

                    // Default foreground is #000000 and background is #FFFFFF
                    expect(fragment.foregroundHex).toBe("#000000")
                    expect(fragment.backgroundHex).toBe("#FFFFFF")

                    // Click palette swatch #3 (red #ED1C24) for Foreground
                    controller.findById("btn-palette-3").performClick()
                    expect(fragment.foregroundHex).toBe("#ED1C24")

                    // Switch to Background slot and click palette swatch #17 (sky blue #99D9EA)
                    controller.findById("btn-swatch-background").performClick()
                    expect(fragment.editingForegroundColor).toBe(false)
                    controller.findById("btn-palette-17").performClick()
                    expect(fragment.backgroundHex).toBe("#99D9EA")

                    // Adjust left vertical Brush Size (1..64px) and Opacity (1..100%) sliders
                    controller.findById("slider-brush-size").performSetSliderValue(24.0)
                    expect(fragment.brushSizePx).toBe(24)
                    expectNode(controller.findById("badge-brush-size")).toHaveText("24px")

                    controller.findById("slider-brush-opacity").performSetSliderValue(65.0)
                    expect(fragment.brushOpacityPercent).toBe(65)
                    expectNode(controller.findById("badge-brush-opacity")).toHaveText("65%")

                    // Toggle Layers panel and add/toggle/merge layers
                    expect(fragment.layersPanelOpen).toBe(false)
                    controller.findById("btn-toggle-layers").performClick()
                    expect(fragment.layersPanelOpen).toBe(true)

                    controller.findById("btn-layer-add").performClick()
                    expect(fragment.layers.size).toBe(2)
                    expect(fragment.activeLayerIndex).toBe(1)

                    // Draw on Layer 2
                    controller.findById("btn-shape-oval").performClick()
                    fragment.simulateStrokeGesture(listOf(15f to 15f, 80f to 75f))
                    expect(fragment.activeLayer.strokes.size).toBe(1)

                    // Toggle visibility on Layer 2
                    controller.findById("btn-layer-toggle-visibility").performClick()
                    expect(fragment.activeLayer.visible).toBe(false)
                    controller.findById("btn-layer-toggle-visibility").performClick()
                    expect(fragment.activeLayer.visible).toBe(true)

                    // Merge Layer 2 down into Layer 1
                    controller.findById("btn-layer-merge-down").performClick()
                    expect(fragment.layers.size).toBe(1)
                    expect(fragment.activeLayer.strokes.size).toBe(1)
                }

                it("round-trips multi-layer canvas projects via WritableByteBuffer and ReadableByteBuffer and persists PaintPreferences in getPreferenceStore()") {
                    val controller = launchApp(visualOnWindows = false) { GenericUIPaintApp() }
                    val fragment = controller.app.paintFragment
                    fragment.handleNewCanvas()

                    // Configure custom workspace & draw on two layers
                    fragment.resizeCanvas(240, 180)
                    fragment.setForegroundColor("#00A2E8")
                    fragment.setBackgroundColor("#EFE4B0")
                    fragment.updateBrushSize(14)
                    fragment.updateBrushOpacity(80)
                    fragment.updateZoomPercent(200)
                    fragment.toggleLayersPanel(open = true)

                    fragment.selectShape(PaintShapeType.RECTANGLE)
                    fragment.simulateStrokeGesture(listOf(12f to 16f, 120f to 96f))

                    fragment.addLayer("Overlay Layer")
                    fragment.selectShape(PaintShapeType.FIVE_POINT_STAR)
                    fragment.simulateStrokeGesture(listOf(40f to 30f, 160f to 140f))

                    expect(fragment.layers.size).toBe(2)
                    expect(fragment.totalStrokeCount).toBe(2)

                    // Export to WritableByteBuffer
                    val writable = WritableByteBuffer()
                    fragment.exportCanvasToWritable(writable)
                    val serializedBytes = writable.toByteArray()
                    assertTrue(serializedBytes.isNotEmpty())

                    // Reset canvas and import from ReadableByteBuffer
                    fragment.handleNewCanvas()
                    expect(fragment.canvasWidthPx).toBe(131)
                    expect(fragment.layers.size).toBe(1)
                    expect(fragment.totalStrokeCount).toBe(0)

                    val readable = ReadableByteBuffer(serializedBytes)
                    fragment.importCanvasFromReadable(readable)

                    assertEquals(240, fragment.canvasWidthPx)
                    assertEquals(180, fragment.canvasHeightPx)
                    assertEquals("#00A2E8", fragment.foregroundHex)
                    assertEquals("#EFE4B0", fragment.backgroundHex)
                    assertEquals(14, fragment.brushSizePx)
                    assertEquals(80, fragment.brushOpacityPercent)
                    assertEquals(200, fragment.zoomPercent)
                    assertEquals(true, fragment.layersPanelOpen)
                    assertEquals(2, fragment.layers.size)
                    assertEquals(2, fragment.totalStrokeCount)
                    assertEquals("Overlay Layer", fragment.layers[1].name)
                    assertEquals(PaintShapeType.FIVE_POINT_STAR, fragment.layers[1].strokes.first().shapeType)

                    // Verify PreferenceStore persistence round-trip
                    val savedPrefs = fragment.savePreferencesToStore()
                    assertEquals(240, savedPrefs.canvasWidthPx)
                    assertEquals(180, savedPrefs.canvasHeightPx)

                    fragment.resizeCanvas(131, 128)
                    val restoredPrefs = fragment.loadPreferencesFromStore()
                    assertEquals(240, restoredPrefs.canvasWidthPx)
                    assertEquals(180, restoredPrefs.canvasHeightPx)
                    assertEquals("#00A2E8", fragment.foregroundHex)
                    assertEquals(14, fragment.brushSizePx)

                    // Clean up PreferenceStore after test
                    fragment.getPreferenceStore().remove(PaintPreferences.PREFERENCE_KEY)
                }
            }
        }
        report.assertAllPassed()
    }
}
