package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceElementKind
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.util.BinaryJson
import com.nexustools.genericui.util.BinaryPlist

/**
 * Multi-format document codec bridging UTF-8 JSON strings, [BinaryJson] (`.bson` / `.bjson`),
 * and Apple [BinaryPlist] (`bplist00` `.plist`), plus hex dump and byte-size comparison helpers.
 */
object DataDocumentCodec {

    /**
     * Formats a [PreferenceElement] tree as pretty-printed, indented JSON text.
     */
    fun formatPrettyJson(element: PreferenceElement, indentSpaces: Int = 2): String {
        val step = indentSpaces.coerceIn(1, 8)
        val sb = StringBuilder()
        writePrettyJson(sb, element, currentIndent = 0, indentStep = step)
        return sb.toString()
    }

    /**
     * Formats a [PreferenceElement] tree as compact single-line JSON text.
     */
    fun formatCompactJson(element: PreferenceElement): String =
        PreferenceObjectCodec.elementToJson(element)

    /**
     * Parses a JSON string into a [PreferenceElement] tree, throwing an [IllegalArgumentException]
     * if the input is blank or structurally malformed.
     */
    fun parseJsonString(jsonText: String): PreferenceElement {
        val trimmed = jsonText.trim()
        require(trimmed.isNotEmpty()) { "JSON string cannot be empty." }
        validateJsonStructure(trimmed)
        return PreferenceObjectCodec.fromJsonElement(trimmed)
    }

    /**
     * Encodes a [PreferenceElement] tree into [BinaryJson] (`.bson`) bytes.
     */
    fun encodeBinaryJson(element: PreferenceElement): ByteArray =
        BinaryJson.encodeElement(element)

    /**
     * Decodes [BinaryJson] (`.bson` / `.bjson`) bytes into a [PreferenceElement] tree.
     * Also detects standard little-endian BSON document headers so external `.bson` files decode cleanly.
     */
    fun decodeBinaryJson(bytes: ByteArray): PreferenceElement {
        if (bytes.isEmpty()) return PreferenceElement.Null
        if (isStandardLittleEndianBson(bytes)) {
            val decoded = runCatching { decodeStandardLittleEndianBson(bytes, 0).first }.getOrNull()
            if (decoded != null) return decoded
        }
        return BinaryJson.decodeElement(bytes)
    }

    /**
     * Encodes a [PreferenceElement] tree into Apple [BinaryPlist] (`bplist00`) bytes.
     */
    fun encodeBinaryPlist(element: PreferenceElement): ByteArray =
        BinaryPlist.encodeElement(element)

    /**
     * Decodes Apple [BinaryPlist] (`bplist00`) bytes into a [PreferenceElement] tree.
     */
    fun decodeBinaryPlist(bytes: ByteArray): PreferenceElement =
        BinaryPlist.decodeElement(bytes)

    /**
     * Encodes [element] into the specified target [format].
     */
    fun encodeToBytes(
        element: PreferenceElement,
        format: DataFormat,
        prettyJsonIndent: Int = 2
    ): ByteArray = when (format) {
        DataFormat.JSON -> {
            if (prettyJsonIndent > 0) {
                formatPrettyJson(element, prettyJsonIndent).encodeToByteArray()
            } else {
                formatCompactJson(element).encodeToByteArray()
            }
        }
        DataFormat.BINARY_JSON_BSON -> encodeBinaryJson(element)
        DataFormat.BINARY_PLIST -> encodeBinaryPlist(element)
    }

    /**
     * Auto-detects (or uses [explicitFormat]) and decodes [bytes] into a `(DataFormat, PreferenceElement)` pair.
     */
    fun decodeFromBytes(
        bytes: ByteArray,
        explicitFormat: DataFormat? = null,
        fileNameHint: String? = null
    ): Pair<DataFormat, PreferenceElement> {
        val detected = explicitFormat ?: detectFormat(fileNameHint, bytes)
        val element = when (detected) {
            DataFormat.BINARY_PLIST -> decodeBinaryPlist(bytes)
            DataFormat.BINARY_JSON_BSON -> decodeBinaryJson(bytes)
            DataFormat.JSON -> {
                val text = bytes.decodeToString()
                PreferenceObjectCodec.fromJsonElement(text)
            }
        }
        return detected to element
    }

    /**
     * Detects the [DataFormat] of a file from its [bytes] magic header and optional [fileName].
     */
    fun detectFormat(fileName: String?, bytes: ByteArray): DataFormat {
        if (BinaryPlist.isBinaryPlist(bytes)) {
            return DataFormat.BINARY_PLIST
        }
        if (bytes.isNotEmpty()) {
            val firstByte = bytes[0].toInt() and 0xFF
            if (firstByte in 0x00..0x0B && firstByte != 0x09 && firstByte != 0x0A) {
                return DataFormat.BINARY_JSON_BSON
            }
            if ((firstByte == 0x09 || firstByte == 0x0A || firstByte == 0x0B) && bytes.size >= 5) {
                // In BinaryJson, 0x09 (ARRAY), 0x0A (MAP), and 0x0B (OBJECT) are followed by a 4-byte BE count
                val b1 = bytes[1].toInt() and 0xFF
                val b2 = bytes[2].toInt() and 0xFF
                if (b1 == 0 && b2 == 0) {
                    return DataFormat.BINARY_JSON_BSON
                }
            }
            if (isStandardLittleEndianBson(bytes)) {
                return DataFormat.BINARY_JSON_BSON
            }
        }
        val byName = DataFormat.fromFileName(fileName)
        if (byName != null) {
            if (byName == DataFormat.BINARY_JSON_BSON && looksLikeUtf8JsonText(bytes)) {
                return DataFormat.JSON
            }
            return byName
        }
        return if (looksLikeUtf8JsonText(bytes)) {
            DataFormat.JSON
        } else {
            DataFormat.BINARY_JSON_BSON
        }
    }

    /**
     * Computes encoded byte sizes across all 3 formats for live comparison in the editor UI.
     */
    fun computeFormatSizes(
        element: PreferenceElement,
        prettyJsonIndent: Int = 2
    ): Map<DataFormat, Int> {
        val jsonBytes = encodeToBytes(element, DataFormat.JSON, prettyJsonIndent)
        val bsonBytes = encodeBinaryJson(element)
        val plistBytes = encodeBinaryPlist(element)
        return linkedMapOf(
            DataFormat.JSON to jsonBytes.size,
            DataFormat.BINARY_JSON_BSON to bsonBytes.size,
            DataFormat.BINARY_PLIST to plistBytes.size
        )
    }

    /**
     * Formats a concise summary comparing the document's byte size in JSON, BinaryJson (BSON), and BinaryPlist (`bplist00`).
     */
    fun formatSizeComparison(element: PreferenceElement, prettyJsonIndent: Int = 2): String {
        val prettySize = formatPrettyJson(element, prettyJsonIndent).encodeToByteArray().size
        val compactSize = formatCompactJson(element).encodeToByteArray().size
        val bsonSize = encodeBinaryJson(element).size
        val plistSize = encodeBinaryPlist(element).size
        return "JSON (pretty): $prettySize B | JSON (compact): $compactSize B | BinaryJson (BSON): $bsonSize B | BinaryPlist (bplist00): $plistSize B"
    }

    /**
     * Formats a structured offset + hex + ASCII dump preview of [bytes] up to [maxBytes].
     */
    fun formatHexPreview(bytes: ByteArray, maxBytes: Int = 256): String {
        if (bytes.isEmpty()) return "(0 bytes)"
        val limit = bytes.size.coerceAtMost(maxBytes.coerceAtLeast(16))
        val sb = StringBuilder()
        var offset = 0
        while (offset < limit) {
            val rowEnd = (offset + 16).coerceAtMost(limit)
            sb.append(offset.toString(16).uppercase().padStart(4, '0')).append("  ")
            for (i in 0 until 16) {
                val idx = offset + i
                if (idx < rowEnd) {
                    val hex = (bytes[idx].toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
                    sb.append(hex).append(' ')
                } else {
                    sb.append("   ")
                }
                if (i == 7) sb.append(' ')
            }
            sb.append(" |")
            for (idx in offset until rowEnd) {
                val b = bytes[idx].toInt() and 0xFF
                val ch = if (b in 0x20..0x7E) b.toChar() else '.'
                sb.append(ch)
            }
            sb.append('|')
            if (rowEnd < limit) {
                sb.append('\n')
            }
            offset += 16
        }
        if (bytes.size > limit) {
            sb.append("\n... (${bytes.size - limit} more bytes, total ${bytes.size} bytes)")
        }
        return sb.toString()
    }

    private fun looksLikeUtf8JsonText(bytes: ByteArray): Boolean {
        for (b in bytes) {
            val c = b.toInt() and 0xFF
            if (c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0D) continue
            return c == '{'.code || c == '['.code || c == '"'.code ||
                c in '0'.code..'9'.code || c == '-'.code ||
                c == 't'.code || c == 'f'.code || c == 'n'.code
        }
        return false
    }

    private fun validateJsonStructure(trimmed: String) {
        var depthBrace = 0
        var depthBracket = 0
        var inString = false
        var escaped = false
        for (i in trimmed.indices) {
            val c = trimmed[i]
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (c == '\\') {
                    escaped = true
                } else if (c == '"') {
                    inString = false
                }
            } else {
                when (c) {
                    '"' -> inString = true
                    '{' -> depthBrace++
                    '}' -> {
                        depthBrace--
                        require(depthBrace >= 0) { "Unexpected closing '}' at character $i." }
                    }
                    '[' -> depthBracket++
                    ']' -> {
                        depthBracket--
                        require(depthBracket >= 0) { "Unexpected closing ']' at character $i." }
                    }
                }
            }
        }
        require(!inString) { "Unterminated string literal in JSON." }
        require(depthBrace == 0) { "Unbalanced '{' / '}' braces in JSON." }
        require(depthBracket == 0) { "Unbalanced '[' / ']' brackets in JSON." }
    }

    private fun writePrettyJson(
        sb: StringBuilder,
        element: PreferenceElement,
        currentIndent: Int,
        indentStep: Int
    ) {
        when (element.kind) {
            PreferenceElementKind.NULL -> sb.append("null")
            PreferenceElementKind.BOOLEAN -> sb.append(if (element.booleanValue == true) "true" else "false")
            PreferenceElementKind.NUMBER -> {
                val n = element.numberValue
                val d = n?.toDouble() ?: 0.0
                if (d.isNaN() || d.isInfinite()) {
                    sb.append("0")
                } else {
                    sb.append(n.toString())
                }
            }
            PreferenceElementKind.STRING -> appendEscapedJsonString(sb, element.stringValue ?: "")
            PreferenceElementKind.ARRAY -> {
                val items = element.arrayElements
                if (items.isEmpty()) {
                    sb.append("[]")
                    return
                }
                sb.append("[\n")
                val childIndent = currentIndent + indentStep
                items.forEachIndexed { idx, item ->
                    sb.append(" ".repeat(childIndent))
                    writePrettyJson(sb, item, childIndent, indentStep)
                    if (idx < items.lastIndex) sb.append(',')
                    sb.append('\n')
                }
                sb.append(" ".repeat(currentIndent)).append(']')
            }
            PreferenceElementKind.MAP, PreferenceElementKind.OBJECT -> {
                val entries = element.mapEntries.entries.toList()
                if (entries.isEmpty()) {
                    sb.append("{}")
                    return
                }
                sb.append("{\n")
                val childIndent = currentIndent + indentStep
                entries.forEachIndexed { idx, (key, value) ->
                    sb.append(" ".repeat(childIndent))
                    appendEscapedJsonString(sb, key)
                    sb.append(": ")
                    writePrettyJson(sb, value, childIndent, indentStep)
                    if (idx < entries.lastIndex) sb.append(',')
                    sb.append('\n')
                }
                sb.append(" ".repeat(currentIndent)).append('}')
            }
        }
    }

    private fun appendEscapedJsonString(sb: StringBuilder, value: String) {
        sb.append('"')
        for (ch in value) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (ch.code < 0x20) {
                        sb.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                    } else {
                        sb.append(ch)
                    }
                }
            }
        }
        sb.append('"')
    }

    private fun isStandardLittleEndianBson(bytes: ByteArray): Boolean {
        if (bytes.size < 5) return false
        if (bytes.last() != 0.toByte()) return false
        val declaredLen = readIntLE(bytes, 0)
        return declaredLen == bytes.size
    }

    private fun decodeStandardLittleEndianBson(bytes: ByteArray, startOffset: Int): Pair<PreferenceElement, Int> {
        val docSize = readIntLE(bytes, startOffset)
        val endOffset = (startOffset + docSize).coerceAtMost(bytes.size)
        var pos = startOffset + 4
        val fields = LinkedHashMap<String, PreferenceElement>()
        while (pos < endOffset - 1) {
            val typeTag = bytes[pos++].toInt() and 0xFF
            if (typeTag == 0x00) break
            val keyEnd = indexOfZeroByte(bytes, pos, endOffset)
            val key = bytes.decodeToString(pos, keyEnd)
            pos = keyEnd + 1
            val (elem, nextPos) = when (typeTag) {
                0x01 -> {
                    val bits = readLongLE(bytes, pos)
                    PreferenceElement.ofDouble(Double.fromBits(bits)) to (pos + 8)
                }
                0x02 -> {
                    val strLen = readIntLE(bytes, pos)
                    val strStart = pos + 4
                    val contentLen = (strLen - 1).coerceAtLeast(0)
                    val s = bytes.decodeToString(strStart, (strStart + contentLen).coerceAtMost(endOffset))
                    PreferenceElement.ofString(s) to (strStart + strLen)
                }
                0x03 -> decodeStandardLittleEndianBson(bytes, pos)
                0x04 -> {
                    val (arrObj, next) = decodeStandardLittleEndianBson(bytes, pos)
                    PreferenceElement.ofArray(arrObj.mapEntries.values.toList()) to next
                }
                0x08 -> {
                    val b = bytes[pos] != 0.toByte()
                    PreferenceElement.ofBoolean(b) to (pos + 1)
                }
                0x0A -> PreferenceElement.Null to pos
                0x10 -> PreferenceElement.ofInt(readIntLE(bytes, pos)) to (pos + 4)
                0x12 -> PreferenceElement.ofLong(readLongLE(bytes, pos)) to (pos + 8)
                else -> PreferenceElement.Null to endOffset
            }
            fields[key] = elem
            pos = nextPos
        }
        return PreferenceElement.ofObject(fields) to endOffset
    }

    private fun indexOfZeroByte(bytes: ByteArray, start: Int, limit: Int): Int {
        var i = start
        while (i < limit && bytes[i] != 0.toByte()) {
            i++
        }
        return i
    }

    private fun readIntLE(bytes: ByteArray, offset: Int): Int {
        val b0 = bytes.getOrElse(offset) { 0 }.toInt() and 0xFF
        val b1 = bytes.getOrElse(offset + 1) { 0 }.toInt() and 0xFF
        val b2 = bytes.getOrElse(offset + 2) { 0 }.toInt() and 0xFF
        val b3 = bytes.getOrElse(offset + 3) { 0 }.toInt() and 0xFF
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    private fun readLongLE(bytes: ByteArray, offset: Int): Long {
        val lo = readIntLE(bytes, offset).toLong() and 0xFFFFFFFFL
        val hi = readIntLE(bytes, offset + 4).toLong() and 0xFFFFFFFFL
        return lo or (hi shl 32)
    }
}
