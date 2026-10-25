package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.dialog.FileTypeFilter

/**
 * Supported document serialization formats in the BSON / JSON / Binary Plist Editor.
 */
enum class DataFormat(
    val displayName: String,
    val shortBadge: String,
    val defaultExtension: String,
    val cleanExtension: String,
    val mimeType: String,
    val description: String
) {
    JSON(
        displayName = "JSON (UTF-8 Text)",
        shortBadge = "JSON",
        defaultExtension = ".json",
        cleanExtension = "json",
        mimeType = "application/json",
        description = "Human-readable UTF-8 JavaScript Object Notation (.json)"
    ),
    BINARY_JSON_BSON(
        displayName = "Binary JSON / BSON",
        shortBadge = "BSON",
        defaultExtension = ".bson",
        cleanExtension = "bson",
        mimeType = "application/bson",
        description = "Compact tag-typed BinaryJson / BSON binary representation (.bson, .bjson)"
    ),
    BINARY_PLIST(
        displayName = "Binary Plist (bplist00)",
        shortBadge = "PLIST",
        defaultExtension = ".plist",
        cleanExtension = "plist",
        mimeType = "application/x-plist",
        description = "Apple Binary Property List bplist00 with object & offset tables (.plist)"
    );

    fun toFileTypeFilter(): FileTypeFilter = when (this) {
        JSON -> FileTypeFilter(
            description = "JSON Documents (*.json)",
            extensions = listOf("json"),
            mimeTypes = listOf("application/json", "text/json", "text/plain"),
            macOsUniformTypeIdentifiers = listOf("public.json")
        )
        BINARY_JSON_BSON -> FileTypeFilter(
            description = "Binary JSON / BSON Files (*.bson, *.bjson)",
            extensions = listOf("bson", "bjson"),
            mimeTypes = listOf("application/bson", "application/octet-stream"),
            macOsUniformTypeIdentifiers = listOf("public.data")
        )
        BINARY_PLIST -> FileTypeFilter(
            description = "Apple Binary Property Lists (*.plist)",
            extensions = listOf("plist"),
            mimeTypes = listOf("application/x-plist", "application/octet-stream"),
            macOsUniformTypeIdentifiers = listOf("com.apple.property-list")
        )
    }

    companion object {
        fun allFileTypeFilters(): List<FileTypeFilter> = listOf(
            FileTypeFilter(
                description = "All Data Documents (*.json, *.bson, *.bjson, *.plist)",
                extensions = listOf("json", "bson", "bjson", "plist"),
                mimeTypes = listOf("application/json", "application/bson", "application/x-plist", "application/octet-stream"),
                macOsUniformTypeIdentifiers = listOf("public.json", "com.apple.property-list", "public.data")
            ),
            JSON.toFileTypeFilter(),
            BINARY_JSON_BSON.toFileTypeFilter(),
            BINARY_PLIST.toFileTypeFilter(),
            FileTypeFilter.ALL_FILES
        )

        fun fromFileName(fileName: String?): DataFormat? {
            if (fileName.isNullOrBlank()) return null
            val lower = fileName.trim().lowercase()
            return when {
                lower.endsWith(".plist") -> BINARY_PLIST
                lower.endsWith(".bson") || lower.endsWith(".bjson") -> BINARY_JSON_BSON
                lower.endsWith(".json") -> JSON
                else -> null
            }
        }

        fun fromSelectValue(value: String?): DataFormat {
            if (value.isNullOrBlank()) return JSON
            return entries.firstOrNull {
                it.name.equals(value.trim(), ignoreCase = true) ||
                    it.shortBadge.equals(value.trim(), ignoreCase = true) ||
                    it.cleanExtension.equals(value.trim().trimStart('.'), ignoreCase = true)
            } ?: JSON
        }
    }
}
