package com.nexustools.genericui.examples.sketchpad

/**
 * Supported 2D vector drawing tool types in the Canvas2D Vector Sketchpad.
 */
enum class SketchShapeType(val displayName: String) {
    FREEHAND("Freehand"),
    RECTANGLE("Rectangle"),
    ELLIPSE("Ellipse"),
    LINE("Line"),
    BEZIER_CURVE("Bezier Curve")
}
