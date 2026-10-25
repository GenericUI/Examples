package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceElement

/**
 * State container for a single open document tab in the multi-tab BSON / JSON / Binary Plist Editor.
 *
 * Supports loading from a JSON string, providing a formatted or compact JSON string,
 * converting between `.json`, `.bson` (`BinaryJson`), and `.plist` (`BinaryPlist` `bplist00`),
 * and editing structured tree nodes in real time via [com.nexustools.genericui.component.lazy.LazyGridTree].
 */
class DataDocumentTabState(
    val tabId: String,
    var title: String,
    var filePath: String? = null,
    var format: DataFormat = DataFormat.JSON,
    var rootNode: EditableDataNode = EditableDataNode(
        keyOrIndex = "root",
        valueType = DataNodeValueType.OBJECT
    ),
    var selectedNodePath: String = "root",
    var isDirty: Boolean = false,
    var lastJsonError: String? = null
) {

    /**
     * Tracks which branch paths (e.g. `"root"`, `"root.user"`, `"root.roles"`) are expanded
     * in the [com.nexustools.genericui.component.lazy.LazyGridTree] for this tab.
     */
    val expandedBranchPaths: MutableSet<String> = linkedSetOf("root")

    init {
        seedExpandedBranchPaths()
    }

    /**
     * Populates [expandedBranchPaths] with all branch container paths in [rootNode].
     */
    fun seedExpandedBranchPaths() {
        expandedBranchPaths.clear()
        expandedBranchPaths.add("root")
        for ((path, node) in rootNode.flattenWithPaths("root")) {
            if (node.isContainer) {
                expandedBranchPaths.add(path)
            }
        }
    }

    /**
     * Returns the display title for the tab header including format badge and dirty indicator.
     */
    fun displayTabTitle(): String {
        val baseTitle = title.trim().ifEmpty { "untitled${format.defaultExtension}" }
        return "[${format.shortBadge}] $baseTitle"
    }

    /**
     * Converts the current mutable [rootNode] tree into a [PreferenceElement] AST.
     */
    fun toPreferenceElement(): PreferenceElement =
        rootNode.toPreferenceElement()

    /**
     * Generates and provides the UTF-8 JSON string representation of this tab's document.
     */
    fun toJsonString(pretty: Boolean = true, indentSpaces: Int = 2): String {
        val element = toPreferenceElement()
        return if (pretty) {
            DataDocumentCodec.formatPrettyJson(element, indentSpaces)
        } else {
            DataDocumentCodec.formatCompactJson(element)
        }
    }

    /**
     * Parses [jsonText] and replaces this tab's [rootNode] tree if valid.
     * Sets [lastJsonError] and returns `false` if [jsonText] is invalid.
     */
    fun loadFromJsonString(jsonText: String, markDirty: Boolean = true): Boolean {
        return try {
            val parsed = DataDocumentCodec.parseJsonString(jsonText)
            rootNode = EditableDataNode.fromPreferenceElement(parsed, keyOrIndex = "root")
            selectedNodePath = "root"
            seedExpandedBranchPaths()
            lastJsonError = null
            if (markDirty) {
                isDirty = true
            }
            true
        } catch (t: Throwable) {
            lastJsonError = t.message ?: "Invalid JSON syntax."
            false
        }
    }

    /**
     * Loads this tab's document from raw file [bytes], auto-detecting the [DataFormat] unless
     * [explicitFormat] is provided.
     */
    fun loadFromBytes(
        bytes: ByteArray,
        explicitFormat: DataFormat? = null,
        fileNameHint: String? = title,
        markDirty: Boolean = false
    ): Boolean {
        return try {
            val (detectedFormat, element) = DataDocumentCodec.decodeFromBytes(
                bytes = bytes,
                explicitFormat = explicitFormat,
                fileNameHint = fileNameHint
            )
            format = detectedFormat
            rootNode = EditableDataNode.fromPreferenceElement(element, keyOrIndex = "root")
            selectedNodePath = "root"
            seedExpandedBranchPaths()
            lastJsonError = null
            isDirty = markDirty
            true
        } catch (t: Throwable) {
            lastJsonError = t.message ?: "Failed to decode document bytes."
            false
        }
    }

    /**
     * Serializes this tab's document into raw bytes for [targetFormat] (`.json`, `.bson`, or `.plist`).
     */
    fun toFileBytes(
        targetFormat: DataFormat = format,
        prettyJsonIndent: Int = 2
    ): ByteArray = DataDocumentCodec.encodeToBytes(
        element = toPreferenceElement(),
        format = targetFormat,
        prettyJsonIndent = prettyJsonIndent
    )

    /**
     * Converts this tab's active target format to [newFormat] and updates the filename extension in [title].
     */
    fun convertFormat(newFormat: DataFormat) {
        if (format == newFormat) return
        format = newFormat
        val stem = title.substringBeforeLast('.', title).ifBlank { "document" }
        title = "$stem${newFormat.defaultExtension}"
        isDirty = true
    }

    /**
     * Returns the currently selected [EditableDataNode], falling back to [rootNode] if the path no longer exists.
     */
    fun selectedNode(): EditableDataNode {
        val found = rootNode.findByPath(selectedNodePath)
        if (found != null) return found
        selectedNodePath = "root"
        return rootNode
    }

    /**
     * Renames the key of the node at [path] (when inside an `OBJECT` or `MAP`) and returns its updated path.
     */
    fun renameNodeAtPath(path: String, newKey: String): String? {
        val oldNormalized = path.trim()
        val newPath = rootNode.renameKeyByPath(oldNormalized, newKey) ?: return null
        if (newPath != oldNormalized) {
            remapPathPrefix(oldNormalized, newPath)
            isDirty = true
            lastJsonError = null
        }
        return newPath
    }

    /**
     * Updates the primitive value of the leaf node at [path].
     */
    fun updateNodeValueAtPath(path: String, newPrimitiveValue: String): Boolean {
        val node = rootNode.findByPath(path) ?: return false
        if (node.isContainer) return false
        if (node.primitiveText != newPrimitiveValue) {
            node.primitiveText = newPrimitiveValue
            isDirty = true
            lastJsonError = null
        }
        return true
    }

    /**
     * Updates the [DataNodeValueType] of the node at [path] (converting between any of the 12 supported types).
     */
    fun updateNodeTypeAtPath(
        path: String,
        newType: DataNodeValueType,
        initialPrimitiveText: String? = null
    ): Boolean {
        val normalized = path.trim()
        val node = rootNode.findByPath(normalized) ?: return false
        if (node.valueType == newType && initialPrimitiveText == null) return true
        val resolvedPrimitive = initialPrimitiveText ?: if (!newType.isContainer && !node.isContainer && node.primitiveText.isNotEmpty()) {
            when (newType) {
                DataNodeValueType.BOOLEAN -> if (node.primitiveText.equals("true", ignoreCase = true) || node.primitiveText == "1") "true" else "false"
                DataNodeValueType.NULL -> "null"
                DataNodeValueType.BYTE, DataNodeValueType.SHORT, DataNodeValueType.INT, DataNodeValueType.LONG ->
                    node.primitiveText.toLongOrNull()?.toString() ?: newType.defaultPrimitiveText()
                DataNodeValueType.FLOAT, DataNodeValueType.DOUBLE ->
                    node.primitiveText.toDoubleOrNull()?.toString() ?: newType.defaultPrimitiveText()
                else -> node.primitiveText
            }
        } else {
            newType.defaultPrimitiveText()
        }
        node.updateValueAndType(newType, resolvedPrimitive)
        if (newType.isContainer) {
            expandedBranchPaths.add(normalized)
        } else {
            expandedBranchPaths.remove(normalized)
        }
        selectedNodePath = normalized
        isDirty = true
        lastJsonError = null
        return true
    }

    /**
     * Moves the node at [path] up one position inside its parent container and returns the updated path.
     */
    fun moveNodeUpAtPath(path: String): String? {
        val oldNormalized = path.trim()
        val newPath = rootNode.moveUpByPath(oldNormalized) ?: return null
        selectedNodePath = newPath
        ensureContainerPathsExpanded()
        isDirty = true
        lastJsonError = null
        return newPath
    }

    /**
     * Moves the node at [path] down one position inside its parent container and returns the updated path.
     */
    fun moveNodeDownAtPath(path: String): String? {
        val oldNormalized = path.trim()
        val newPath = rootNode.moveDownByPath(oldNormalized) ?: return null
        selectedNodePath = newPath
        ensureContainerPathsExpanded()
        isDirty = true
        lastJsonError = null
        return newPath
    }

    /**
     * Adds a new leaf child to the branch at [branchPath] and returns the created leaf's path.
     */
    fun addLeafToBranchAtPath(
        branchPath: String,
        key: String = "newField",
        type: DataNodeValueType = DataNodeValueType.STRING,
        value: String = type.defaultPrimitiveText()
    ): String {
        val normalizedBranch = branchPath.trim().ifEmpty { "root" }
        val branchNode = rootNode.findByPath(normalizedBranch) ?: rootNode
        val resolvedBranchPath = if (rootNode.findByPath(normalizedBranch) != null) normalizedBranch else "root"
        val leafType = if (type.isContainer) DataNodeValueType.STRING else type
        val added = branchNode.addChildNode(key, leafType, value)
        expandedBranchPaths.add(resolvedBranchPath)
        val newPath = if (branchNode.valueType == DataNodeValueType.ARRAY) {
            "$resolvedBranchPath${added.keyOrIndex}"
        } else {
            "$resolvedBranchPath.${added.keyOrIndex}"
        }
        selectedNodePath = newPath
        isDirty = true
        lastJsonError = null
        return newPath
    }

    /**
     * Adds a nested branch (`OBJECT`, `MAP`, or `ARRAY`) to the branch at [branchPath] and returns the created branch's path.
     */
    fun addBranchToBranchAtPath(
        branchPath: String,
        key: String = "newObject",
        branchType: DataNodeValueType = DataNodeValueType.OBJECT
    ): String {
        val normalizedBranch = branchPath.trim().ifEmpty { "root" }
        val branchNode = rootNode.findByPath(normalizedBranch) ?: rootNode
        val resolvedBranchPath = if (rootNode.findByPath(normalizedBranch) != null) normalizedBranch else "root"
        val added = branchNode.addBranchNode(key, branchType)
        expandedBranchPaths.add(resolvedBranchPath)
        val newPath = if (branchNode.valueType == DataNodeValueType.ARRAY) {
            "$resolvedBranchPath${added.keyOrIndex}"
        } else {
            "$resolvedBranchPath.${added.keyOrIndex}"
        }
        expandedBranchPaths.add(newPath)
        selectedNodePath = newPath
        isDirty = true
        lastJsonError = null
        return newPath
    }

    /**
     * Duplicates the node at [path] inside its parent container and returns the duplicated node's path.
     */
    fun duplicateNodeAtPath(path: String): String? {
        val normalized = path.trim()
        if (normalized == "root" || normalized.isEmpty()) return null
        val dupPath = rootNode.duplicateByPath(normalized) ?: return null
        val dupNode = rootNode.findByPath(dupPath)
        if (dupNode != null && dupNode.isContainer) {
            for ((subPath, subNode) in dupNode.flattenWithPaths(dupPath)) {
                if (subNode.isContainer) {
                    expandedBranchPaths.add(subPath)
                }
            }
        }
        selectedNodePath = dupPath
        isDirty = true
        lastJsonError = null
        return dupPath
    }

    /**
     * Deletes the node at [path] from its parent container (cannot delete `"root"`).
     */
    fun deleteNodeAtPath(path: String): Boolean {
        val normalized = path.trim()
        if (normalized == "root" || normalized.isEmpty()) return false
        val parentPath = EditableDataNode.parentPathOf(normalized) ?: "root"
        val removed = rootNode.removeByPath(normalized)
        if (removed) {
            expandedBranchPaths.removeAll { it == normalized || it.startsWith("$normalized.") || it.startsWith("$normalized[") }
            if (selectedNodePath == normalized || selectedNodePath.startsWith("$normalized.") || selectedNodePath.startsWith("$normalized[")) {
                selectedNodePath = parentPath
            }
            isDirty = true
            lastJsonError = null
        }
        return removed
    }

    /**
     * Updates the selected node's key name (if it is inside an `OBJECT` or `MAP`), value type, and primitive value.
     */
    fun updateSelectedNode(
        newKeyName: String?,
        newType: DataNodeValueType,
        newPrimitiveValue: String
    ): Boolean {
        val node = selectedNode()
        val parent = rootNode.findParentOfPath(selectedNodePath)
        var updatedPath = selectedNodePath

        if (parent != null && (parent.valueType == DataNodeValueType.OBJECT || parent.valueType == DataNodeValueType.MAP)) {
            val cleanKey = newKeyName?.trim().orEmpty()
            if (cleanKey.isNotEmpty() && cleanKey != node.keyOrIndex) {
                val renamedPath = rootNode.renameKeyByPath(selectedNodePath, cleanKey)
                if (renamedPath != null) {
                    remapPathPrefix(selectedNodePath, renamedPath)
                    updatedPath = renamedPath
                }
            }
        }

        node.updateValueAndType(newType, newPrimitiveValue)
        if (newType.isContainer) {
            expandedBranchPaths.add(updatedPath)
        }
        selectedNodePath = updatedPath
        isDirty = true
        lastJsonError = null
        return true
    }

    /**
     * Adds a child node to the currently selected container node (or to its parent container if a primitive node is selected).
     */
    fun addChildToSelectedOrParent(
        childKey: String,
        childType: DataNodeValueType,
        childValue: String = childType.defaultPrimitiveText()
    ): String {
        val current = selectedNode()
        val targetContainerPath: String
        val targetContainer: EditableDataNode
        if (current.isContainer) {
            targetContainerPath = selectedNodePath
            targetContainer = current
        } else {
            val parent = rootNode.findParentOfPath(selectedNodePath)
            if (parent != null && parent.isContainer) {
                targetContainerPath = EditableDataNode.parentPathOf(selectedNodePath) ?: "root"
                targetContainer = parent
            } else {
                targetContainerPath = selectedNodePath
                targetContainer = current
            }
        }

        val added = targetContainer.addChildNode(childKey, childType, childValue)
        expandedBranchPaths.add(targetContainerPath)
        val newPath = if (targetContainer.valueType == DataNodeValueType.ARRAY) {
            "$targetContainerPath${added.keyOrIndex}"
        } else {
            "$targetContainerPath.${added.keyOrIndex}"
        }
        if (added.isContainer) {
            expandedBranchPaths.add(newPath)
        }
        selectedNodePath = newPath
        isDirty = true
        lastJsonError = null
        return newPath
    }

    /**
     * Duplicates the currently selected node inside its parent container.
     */
    fun duplicateSelectedNode(): String? =
        duplicateNodeAtPath(selectedNodePath)

    /**
     * Removes the currently selected node from its parent container (cannot delete `"root"`).
     */
    fun deleteSelectedNode(): Boolean =
        deleteNodeAtPath(selectedNodePath)

    private fun remapPathPrefix(oldPrefix: String, newPrefix: String) {
        if (selectedNodePath == oldPrefix) {
            selectedNodePath = newPrefix
        } else if (selectedNodePath.startsWith("$oldPrefix.") || selectedNodePath.startsWith("$oldPrefix[")) {
            selectedNodePath = newPrefix + selectedNodePath.removePrefix(oldPrefix)
        }
        val updatedExpanded = linkedSetOf<String>()
        for (p in expandedBranchPaths) {
            when {
                p == oldPrefix -> updatedExpanded.add(newPrefix)
                p.startsWith("$oldPrefix.") || p.startsWith("$oldPrefix[") ->
                    updatedExpanded.add(newPrefix + p.removePrefix(oldPrefix))
                else -> updatedExpanded.add(p)
            }
        }
        expandedBranchPaths.clear()
        expandedBranchPaths.addAll(updatedExpanded)
    }

    private fun ensureContainerPathsExpanded() {
        for ((path, node) in rootNode.flattenWithPaths("root")) {
            if (node.isContainer && path == "root") {
                expandedBranchPaths.add(path)
            }
        }
    }
}
