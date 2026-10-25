package com.nexustools.genericui.examples.sketchpad

/**
 * Immutable vector shape or brush stroke command rendered onto the [SketchpadFragment] canvas.
 */
data class SketchVectorCommand(
    val type: SketchShapeType,
    val colorHex: String,
    val strokeWidth: Float,
    val points: List<Pair<Float, Float>>
)
