package com.nexustools.genericui.examples.sketchpad

import com.nexustools.genericui.geometry.Point
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Cross-platform `JestElectricRunner` test suite for the Canvas2D Vector Sketchpad example.
 */
class SketchpadJestTest {

    @Test
    fun runSketchpadJestElectricSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Canvas2D Vector Sketchpad JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("Toolbar Tool Selection & Vector Shape Commands") {
                it("switches between Freehand, Rectangle, Ellipse, Line, and Bezier Curve tools and adds shapes") {
                    val controller = launchApp(visualOnWindows = false) { SketchpadApp() }
                    val fragment = controller.app.sketchpadFragment

                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: FREEHAND")
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 0")
                    expectNode(controller.findById("btn-undo")).toBeDisabled()
                    expectNode(controller.findById("btn-redo")).toBeDisabled()

                    // Add a Freehand shape
                    controller.findById("btn-add-shape").performClick()
                    expect(fragment.commands.size).toBe(1)
                    expect(fragment.commands.last().type).toBe(SketchShapeType.FREEHAND)
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 1")
                    expectNode(controller.findById("btn-undo")).toBeEnabled()

                    // Switch to Rectangle tool and add shape
                    controller.findById("btn-tool-rect").performClick()
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: RECTANGLE")
                    controller.findById("btn-add-shape").performClick()
                    expect(fragment.commands.size).toBe(2)
                    expect(fragment.commands.last().type).toBe(SketchShapeType.RECTANGLE)
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 2")

                    // Switch to Ellipse tool and add shape
                    controller.findById("btn-tool-ellipse").performClick()
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: ELLIPSE")
                    controller.findById("btn-add-shape").performClick()
                    expect(fragment.commands.size).toBe(3)
                    expect(fragment.commands.last().type).toBe(SketchShapeType.ELLIPSE)

                    // Switch to Bezier Curve tool and add shape
                    controller.findById("btn-tool-bezier").performClick()
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: BEZIER_CURVE")
                    controller.findById("btn-add-shape").performClick()
                    expect(fragment.commands.size).toBe(4)
                    expect(fragment.commands.last().type).toBe(SketchShapeType.BEZIER_CURVE)
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 4")
                }

                it("adjusts stroke width via RangeSlider and records pointer stroke gestures") {
                    val controller = launchApp(visualOnWindows = false) { SketchpadApp() }
                    val fragment = controller.app.sketchpadFragment

                    controller.findById("slider-stroke-width").performSetSliderValue(12.0)
                    expect(fragment.activeStrokeWidth).toBe(12f)
                    expectNode(controller.findById("badge-stroke-width")).toHaveText("Stroke: 12px")

                    controller.findById("btn-tool-line").performClick()
                    fragment.simulateStrokeGesture(
                        listOf(
                            40f to 40f,
                            180f to 160f
                        )
                    )
                    expect(fragment.commands.size).toBe(1)
                    expect(fragment.commands.first().type).toBe(SketchShapeType.LINE)
                    expect(fragment.commands.first().strokeWidth).toBe(12f)
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 1")
                }

                it("supports multi-step Undo, Redo, Clear, CurveEditorExt LUT sampling, and VBlank rendering") {
                    val controller = launchApp(visualOnWindows = false) { SketchpadApp() }
                    val fragment = controller.app.sketchpadFragment

                    controller.findById("btn-add-shape").performClick()
                    controller.findById("btn-tool-rect").performClick()
                    controller.findById("btn-add-shape").performClick()
                    controller.findById("btn-tool-bezier").performClick()
                    controller.findById("btn-add-shape").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 3")

                    // Undo twice
                    controller.findById("btn-undo").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 2")
                    expectNode(controller.findById("btn-redo")).toBeEnabled()

                    controller.findById("btn-undo").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 1")
                    expect(fragment.redoCount).toBe(2)

                    // Redo once
                    controller.findById("btn-redo").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 2")
                    expect(fragment.redoCount).toBe(1)

                    // Update cubic-Bezier curve control points and verify LUT evaluation
                    fragment.curveEditor.controlPoint1 = Point(0.2f, 0.8f)
                    fragment.curveEditor.controlPoint2 = Point(0.8f, 0.9f)
                    val lut = fragment.curveEditor.sampleLut(64)
                    expect(lut.size).toBe(64)
                    expect(fragment.curveEditor.sampleCurveY(0.5f) > 0.5f).toBe(true)

                    // Step VBlank frames and verify framebuffer extraction
                    fragment.sketchCanvas.dispatchVBlankFrame(16.67)
                    fragment.sketchCanvas.dispatchVBlankFrame(33.34)
                    pumpMainLooper(32L)
                    expect(fragment.renderedFrameCount).toBeGreaterThan(1)
                    val snapshot = fragment.captureSnapshotBuffer()
                    expect(snapshot.width).toBe(720)
                    expect(snapshot.height).toBe(460)

                    // Clear canvas
                    controller.findById("btn-clear").performClick()
                    expect(fragment.commands.size).toBe(0)
                    expect(fragment.redoCount).toBe(0)
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 0")
                    expectNode(controller.findById("btn-undo")).toBeDisabled()
                    expectNode(controller.findById("btn-redo")).toBeDisabled()
                }
            }
        }
        report.assertAllPassed()
    }
}
