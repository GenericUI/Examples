package com.nexustools.genericui.examples.sketchpad

import com.nexustools.genericui.geometry.Point
import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Visual test runner entry point for the Canvas2D Vector Sketchpad (`windowsVisualTest`).
 */
object SketchpadVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Canvas2D Vector Sketchpad Visual Suite",
            args = args
        ) {
            describe("Interactive 2D Vector Tools, Undo/Redo, and CurveEditorExt") {
                it("switches vector tools, adds shapes, adjusts stroke width, and verifies Undo/Redo/Clear") {
                    val controller = launchApp { SketchpadApp() }
                    val fragment = controller.app.sketchpadFragment

                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: FREEHAND")
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 0")

                    controller.findById("btn-add-shape").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 1")

                    controller.findById("btn-tool-rect").performClick()
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: RECTANGLE")
                    controller.findById("btn-add-shape").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 2")

                    controller.findById("btn-tool-ellipse").performClick()
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: ELLIPSE")
                    controller.findById("btn-add-shape").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 3")

                    controller.findById("btn-tool-bezier").performClick()
                    expectNode(controller.findById("badge-active-tool")).toHaveText("Tool: BEZIER_CURVE")
                    controller.findById("slider-stroke-width").performSetSliderValue(10.0)
                    expectNode(controller.findById("badge-stroke-width")).toHaveText("Stroke: 10px")

                    controller.findById("btn-add-shape").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 4")

                    fragment.curveEditor.controlPoint1 = Point(0.25f, 0.75f)
                    fragment.sketchCanvas.dispatchVBlankFrame(16.67)
                    expect(fragment.renderedFrameCount).toBeGreaterThan(0)

                    controller.findById("btn-undo").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 3")

                    controller.findById("btn-redo").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 4")

                    controller.findById("btn-clear").performClick()
                    expectNode(controller.findById("badge-shape-count")).toHaveText("Shapes: 0")
                }
            }
        }
}
