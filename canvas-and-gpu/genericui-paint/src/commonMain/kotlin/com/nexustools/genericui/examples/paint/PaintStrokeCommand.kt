package com.nexustools.genericui.examples.paint

import com.nexustools.genericui.io.ByteBuffer
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.Writable

/**
 * Immutable drawing command (freehand stroke, flood fill, text stamp, eraser pass, or geometric shape)
 * recorded on a [PaintLayer] for undo/redo history, `Canvas2D` rendering, and binary project serialization.
 */
data class PaintStrokeCommand(
    val tool: PaintTool,
    val brushStyle: PaintBrushStyle = PaintBrushStyle.BRUSH,
    val shapeType: PaintShapeType = PaintShapeType.RECTANGLE,
    val primaryColorHex: String = "#000000",
    val secondaryColorHex: String = "#FFFFFF",
    val strokeWidthPx: Float = 8f,
    val opacityPercent: Int = 100,
    val outlineEnabled: Boolean = true,
    val fillEnabled: Boolean = false,
    val textContent: String = "",
    val points: List<Pair<Float, Float>> = emptyList()
) {
    /**
     * Serializes this [PaintStrokeCommand] to a [Writable] binary stream.
     */
    fun writeTo(writable: Writable) {
        writable.writeInt(tool.ordinal)
        writable.writeInt(brushStyle.ordinal)
        writable.writeInt(shapeType.ordinal)
        writeLengthPrefixedString(writable, primaryColorHex)
        writeLengthPrefixedString(writable, secondaryColorHex)
        writable.writeFloat(strokeWidthPx)
        writable.writeInt(opacityPercent)
        writable.writeBoolean(outlineEnabled)
        writable.writeBoolean(fillEnabled)
        writeLengthPrefixedString(writable, textContent)
        writable.writeInt(points.size)
        for ((px, py) in points) {
            writable.writeFloat(px)
            writable.writeFloat(py)
        }
    }

    companion object {
        internal fun writeLengthPrefixedString(writable: Writable, text: String) {
            val encoded = ByteBuffer.fromString(text, "utf8").toByteArray()
            writable.writeInt(encoded.size)
            if (encoded.isNotEmpty()) {
                writable.writeBytes(encoded)
            }
        }

        internal fun readLengthPrefixedString(reader: ReadableByteBuffer): String {
            val len = reader.nextInt().coerceAtLeast(0)
            if (len == 0) return ""
            return reader.nextString(len, "utf8")
        }

        /**
         * Deserializes a [PaintStrokeCommand] from a [ReadableByteBuffer].
         */
        fun readFrom(reader: ReadableByteBuffer): PaintStrokeCommand {
            val toolOrdinal = reader.nextInt().coerceIn(0, PaintTool.entries.lastIndex)
            val brushOrdinal = reader.nextInt().coerceIn(0, PaintBrushStyle.entries.lastIndex)
            val shapeOrdinal = reader.nextInt().coerceIn(0, PaintShapeType.entries.lastIndex)
            val primaryHex = readLengthPrefixedString(reader)
            val secondaryHex = readLengthPrefixedString(reader)
            val strokeWidth = reader.nextFloat()
            val opacity = reader.nextInt().coerceIn(1, 100)
            val outline = reader.nextBoolean()
            val fill = reader.nextBoolean()
            val text = readLengthPrefixedString(reader)
            val pointCount = reader.nextInt().coerceIn(0, 100_000)
            val pts = ArrayList<Pair<Float, Float>>(pointCount)
            for (i in 0 until pointCount) {
                val x = reader.nextFloat()
                val y = reader.nextFloat()
                pts.add(x to y)
            }
            return PaintStrokeCommand(
                tool = PaintTool.entries[toolOrdinal],
                brushStyle = PaintBrushStyle.entries[brushOrdinal],
                shapeType = PaintShapeType.entries[shapeOrdinal],
                primaryColorHex = primaryHex.ifBlank { "#000000" },
                secondaryColorHex = secondaryHex.ifBlank { "#FFFFFF" },
                strokeWidthPx = strokeWidth,
                opacityPercent = opacity,
                outlineEnabled = outline,
                fillEnabled = fill,
                textContent = text,
                points = pts
            )
        }
    }
}
