package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceElementKind

/**
 * Value types supported by [EditableDataNode] with lossless conversion to and from [PreferenceElement].
 */
enum class DataNodeValueType(
    val displayName: String,
    val badgeLabel: String,
    val isContainer: Boolean,
    val isNumber: Boolean
) {
    OBJECT("Object", "OBJ", isContainer = true, isNumber = false),
    MAP("Map", "MAP", isContainer = true, isNumber = false),
    ARRAY("Array", "ARR", isContainer = true, isNumber = false),
    STRING("String", "STR", isContainer = false, isNumber = false),
    BYTE("Byte (Int8)", "I8", isContainer = false, isNumber = true),
    SHORT("Short (Int16)", "I16", isContainer = false, isNumber = true),
    INT("Int (Int32)", "I32", isContainer = false, isNumber = true),
    LONG("Long (Int64)", "I64", isContainer = false, isNumber = true),
    FLOAT("Float (FP32)", "F32", isContainer = false, isNumber = true),
    DOUBLE("Double (FP64)", "F64", isContainer = false, isNumber = true),
    BOOLEAN("Boolean", "BOOL", isContainer = false, isNumber = false),
    NULL("Null", "NULL", isContainer = false, isNumber = false);

    fun defaultPrimitiveText(): String = when (this) {
        OBJECT, MAP, ARRAY -> ""
        STRING -> ""
        BYTE, SHORT, INT, LONG -> "0"
        FLOAT, DOUBLE -> "0.0"
        BOOLEAN -> "false"
        NULL -> "null"
    }

    fun toPreferenceElement(
        primitiveText: String,
        childrenEntries: Map<String, PreferenceElement> = emptyMap(),
        childrenList: List<PreferenceElement> = emptyList()
    ): PreferenceElement {
        val trimmed = primitiveText.trim()
        return when (this) {
            OBJECT -> PreferenceElement.ofObject(childrenEntries)
            MAP -> PreferenceElement.ofMap(childrenEntries)
            ARRAY -> PreferenceElement.ofArray(childrenList)
            STRING -> PreferenceElement.ofString(primitiveText)
            BYTE -> PreferenceElement.ofByte(
                trimmed.toByteOrNull()
                    ?: trimmed.toIntOrNull()?.coerceIn(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt())?.toByte()
                    ?: 0.toByte()
            )
            SHORT -> PreferenceElement.ofShort(
                trimmed.toShortOrNull()
                    ?: trimmed.toIntOrNull()?.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())?.toShort()
                    ?: 0.toShort()
            )
            INT -> PreferenceElement.ofInt(
                trimmed.toIntOrNull()
                    ?: trimmed.toDoubleOrNull()?.toInt()
                    ?: 0
            )
            LONG -> PreferenceElement.ofLong(
                trimmed.toLongOrNull()
                    ?: trimmed.toDoubleOrNull()?.toLong()
                    ?: 0L
            )
            FLOAT -> PreferenceElement.ofFloat(
                trimmed.toFloatOrNull() ?: 0f
            )
            DOUBLE -> PreferenceElement.ofDouble(
                trimmed.toDoubleOrNull() ?: 0.0
            )
            BOOLEAN -> {
                val boolVal = when (trimmed.lowercase()) {
                    "true", "1", "yes", "on" -> true
                    else -> false
                }
                PreferenceElement.ofBoolean(boolVal)
            }
            NULL -> PreferenceElement.Null
        }
    }

    companion object {
        fun fromPreferenceElement(element: PreferenceElement): DataNodeValueType = when (element.kind) {
            PreferenceElementKind.OBJECT -> OBJECT
            PreferenceElementKind.MAP -> MAP
            PreferenceElementKind.ARRAY -> ARRAY
            PreferenceElementKind.STRING -> STRING
            PreferenceElementKind.BOOLEAN -> BOOLEAN
            PreferenceElementKind.NULL -> NULL
            PreferenceElementKind.NUMBER -> when (element.numberTypeTag) {
                PreferenceElement.NUMBER_TAG_BYTE -> BYTE
                PreferenceElement.NUMBER_TAG_SHORT -> SHORT
                PreferenceElement.NUMBER_TAG_INT -> INT
                PreferenceElement.NUMBER_TAG_LONG -> LONG
                PreferenceElement.NUMBER_TAG_FLOAT -> FLOAT
                PreferenceElement.NUMBER_TAG_DOUBLE -> DOUBLE
                else -> DOUBLE
            }
        }

        fun extractPrimitiveText(element: PreferenceElement): String = when (element.kind) {
            PreferenceElementKind.NULL -> "null"
            PreferenceElementKind.BOOLEAN -> (element.booleanValue ?: false).toString()
            PreferenceElementKind.NUMBER -> (element.numberValue ?: 0).toString()
            PreferenceElementKind.STRING -> element.stringValue ?: ""
            PreferenceElementKind.ARRAY, PreferenceElementKind.MAP, PreferenceElementKind.OBJECT -> ""
        }

        fun fromNameOrDefault(name: String?, default: DataNodeValueType = STRING): DataNodeValueType {
            if (name.isNullOrBlank()) return default
            return entries.firstOrNull {
                it.name.equals(name.trim(), ignoreCase = true) ||
                    it.badgeLabel.equals(name.trim(), ignoreCase = true)
            } ?: default
        }
    }
}
