package com.nexustools.genericui.examples.paint

/**
 * The 20 geometric, arrow, star, and callout shapes in the Windows 11 Paint `Shapes` ribbon gallery.
 */
enum class PaintShapeType(val label: String, val symbol: String) {
    LINE("Line", "/"),
    CURVE("Curve", "~"),
    OVAL("Oval", "O"),
    RECTANGLE("Rectangle", "[]"),
    ROUNDED_RECTANGLE("Rounded Rectangle", "()"),
    POLYGON("Polygon", "P5"),
    TRIANGLE("Triangle", "/\\"),
    RIGHT_TRIANGLE("Right Triangle", "|\\"),
    DIAMOND("Diamond", "<>"),
    PENTAGON("Pentagon", "P"),
    HEXAGON("Hexagon", "H"),
    RIGHT_ARROW("Right Arrow", "->"),
    LEFT_ARROW("Left Arrow", "<-"),
    UP_ARROW("Up Arrow", "^"),
    DOWN_ARROW("Down Arrow", "v"),
    FOUR_POINT_STAR("4-Point Star", "✦"),
    FIVE_POINT_STAR("5-Point Star", "★"),
    SIX_POINT_STAR("6-Point Star", "*"),
    SPEECH_BUBBLE("Speech Bubble", "\"\""),
    HEART("Heart", "<3")
}
