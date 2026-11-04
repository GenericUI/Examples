package com.nexustools.genericui.examples.paint

/**
 * Represents a single compositable drawing layer in the Windows 11 Paint `Layers` sidebar.
 */
data class PaintLayer(
    val layerId: String,
    var name: String,
    var visible: Boolean = true,
    var opacityPercent: Int = 100,
    val strokes: MutableList<PaintStrokeCommand> = mutableListOf()
)
