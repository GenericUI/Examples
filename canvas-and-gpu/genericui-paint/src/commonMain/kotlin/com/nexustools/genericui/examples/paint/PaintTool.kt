package com.nexustools.genericui.examples.paint

/**
 * Interactive tools in the Windows 11 Dark-Mode Paint ribbon (`Selection`, `Tools`, `Brushes`, `Shapes`).
 */
enum class PaintTool(val label: String, val iconSymbol: String) {
    SELECTION("Selection", "[]"),
    PENCIL("Pencil", "/"),
    FILL_BUCKET("Fill", "#"),
    TEXT("Text", "A"),
    ERASER("Eraser", "X"),
    COLOR_PICKER("Color Picker", "+"),
    MAGNIFIER("Magnifier", "@"),
    BRUSH("Brush", "*"),
    SHAPE("Shape", "<>")
}
