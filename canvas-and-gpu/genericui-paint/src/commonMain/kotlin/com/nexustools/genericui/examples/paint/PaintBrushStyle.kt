package com.nexustools.genericui.examples.paint

/**
 * Brush tip presets available from the `Brushes` ribbon dropdown in Windows 11 Paint.
 */
enum class PaintBrushStyle(
    val label: String,
    val hardness: Float,
    val opacityScale: Float
) {
    BRUSH("Brush", hardness = 0.85f, opacityScale = 1.0f),
    CALLIGRAPHY_PEN("Calligraphy Pen", hardness = 0.98f, opacityScale = 1.0f),
    AIRBRUSH("Airbrush", hardness = 0.25f, opacityScale = 0.55f),
    OIL_BRUSH("Oil Brush", hardness = 0.75f, opacityScale = 0.92f),
    WATERCOLOR("Watercolor", hardness = 0.35f, opacityScale = 0.45f),
    MARKER("Marker", hardness = 0.90f, opacityScale = 0.70f),
    CRAYON("Crayon", hardness = 0.65f, opacityScale = 0.80f)
}
