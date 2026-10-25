package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.system.PreferenceElement

/**
 * Mutable tree node model representing an editable branch (`OBJECT`, `MAP`, `ARRAY`) or leaf
 * (`STRING`, `BYTE`, `SHORT`, `INT`, `LONG`, `FLOAT`, `DOUBLE`, `BOOLEAN`, `NULL`) node in a
 * JSON, BinaryJson (BSON), or BinaryPlist (`bplist00`) document.
 */
class EditableDataNode(
    val id: String = "node_${nextNodeCounter()}",
    var keyOrIndex: String,
    var valueType: DataNodeValueType,
    var primitiveText: String = valueType.defaultPrimitiveText(),
    val children: MutableList<EditableDataNode> = mutableListOf()
) {

    val isContainer: Boolean
        get() = valueType.isContainer

    val childCount: Int
        get() = children.size

    /**
     * Converts this mutable node tree losslessly into a [PreferenceElement] AST.
     */
    fun toPreferenceElement(): PreferenceElement = when (valueType) {
        DataNodeValueType.OBJECT -> {
            val map = LinkedHashMap<String, PreferenceElement>(children.size)
            for (child in children) {
                map[child.keyOrIndex] = child.toPreferenceElement()
            }
            PreferenceElement.ofObject(map)
        }
        DataNodeValueType.MAP -> {
            val map = LinkedHashMap<String, PreferenceElement>(children.size)
            for (child in children) {
                map[child.keyOrIndex] = child.toPreferenceElement()
            }
            PreferenceElement.ofMap(map)
        }
        DataNodeValueType.ARRAY -> {
            val list = ArrayList<PreferenceElement>(children.size)
            for (child in children) {
                list.add(child.toPreferenceElement())
            }
            PreferenceElement.ofArray(list)
        }
        else -> valueType.toPreferenceElement(primitiveText)
    }

    /**
     * Flattens this node tree into `(path, node)` pairs for display in the structured node tree view.
     */
    fun flattenWithPaths(prefix: String = "root"): List<Pair<String, EditableDataNode>> {
        val result = ArrayList<Pair<String, EditableDataNode>>()
        collectPaths(this, prefix, result)
        return result
    }

    private fun collectPaths(
        node: EditableDataNode,
        currentPath: String,
        out: MutableList<Pair<String, EditableDataNode>>
    ) {
        out.add(currentPath to node)
        when (node.valueType) {
            DataNodeValueType.ARRAY -> {
                node.children.forEachIndexed { idx, child ->
                    child.keyOrIndex = "[$idx]"
                    collectPaths(child, "$currentPath[$idx]", out)
                }
            }
            DataNodeValueType.OBJECT, DataNodeValueType.MAP -> {
                for (child in node.children) {
                    val childPath = "$currentPath.${child.keyOrIndex}"
                    collectPaths(child, childPath, out)
                }
            }
            else -> Unit
        }
    }

    /**
     * Looks up a node by its dot/bracket path (e.g. `"root"`, `"root.user.name"`, `"root.items[0]"`).
     */
    fun findByPath(path: String): EditableDataNode? {
        val normalized = path.trim().ifEmpty { "root" }
        return flattenWithPaths("root").firstOrNull { it.first == normalized }?.second
    }

    /**
     * Finds the parent container node of [targetPath], or `null` if [targetPath] is `"root"`.
     */
    fun findParentOfPath(targetPath: String): EditableDataNode? {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return null
        val parentPath = parentPathOf(normalized) ?: return null
        return findByPath(parentPath)
    }

    /**
     * Returns the zero-based index of [targetPath] within its parent's [children] list, or `-1` if root/not found.
     */
    fun siblingIndexOfPath(targetPath: String): Int {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return -1
        val parent = findParentOfPath(normalized) ?: return -1
        val targetNode = findByPath(normalized) ?: return -1
        return parent.children.indexOfFirst { it.id == targetNode.id }
    }

    /**
     * Returns the total number of siblings in the parent container of [targetPath].
     */
    fun siblingCountOfPath(targetPath: String): Int {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return 1
        val parent = findParentOfPath(normalized) ?: return 0
        return parent.children.size
    }

    /**
     * Returns `true` if the node at [targetPath] can be moved up one position within its parent container.
     */
    fun canMoveUpByPath(targetPath: String): Boolean =
        siblingIndexOfPath(targetPath) > 0

    /**
     * Returns `true` if the node at [targetPath] can be moved down one position within its parent container.
     */
    fun canMoveDownByPath(targetPath: String): Boolean {
        val idx = siblingIndexOfPath(targetPath)
        val count = siblingCountOfPath(targetPath)
        return idx >= 0 && idx < count - 1
    }

    /**
     * Moves the node at [targetPath] up by one index in its parent's [children] list and returns its updated path.
     */
    fun moveUpByPath(targetPath: String): String? {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return null
        val parent = findParentOfPath(normalized) ?: return null
        val targetNode = findByPath(normalized) ?: return null
        val parentPath = parentPathOf(normalized) ?: return null
        val idx = parent.children.indexOfFirst { it.id == targetNode.id }
        if (idx <= 0) return null

        val prev = parent.children[idx - 1]
        parent.children[idx - 1] = targetNode
        parent.children[idx] = prev

        return if (parent.valueType == DataNodeValueType.ARRAY) {
            parent.reindexArrayChildren()
            "$parentPath[${idx - 1}]"
        } else {
            "$parentPath.${targetNode.keyOrIndex}"
        }
    }

    /**
     * Moves the node at [targetPath] down by one index in its parent's [children] list and returns its updated path.
     */
    fun moveDownByPath(targetPath: String): String? {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return null
        val parent = findParentOfPath(normalized) ?: return null
        val targetNode = findByPath(normalized) ?: return null
        val parentPath = parentPathOf(normalized) ?: return null
        val idx = parent.children.indexOfFirst { it.id == targetNode.id }
        if (idx < 0 || idx >= parent.children.lastIndex) return null

        val next = parent.children[idx + 1]
        parent.children[idx + 1] = targetNode
        parent.children[idx] = next

        return if (parent.valueType == DataNodeValueType.ARRAY) {
            parent.reindexArrayChildren()
            "$parentPath[${idx + 1}]"
        } else {
            "$parentPath.${targetNode.keyOrIndex}"
        }
    }

    /**
     * Renames the key of the node at [targetPath] (when inside an `OBJECT` or `MAP`) and returns its new path.
     */
    fun renameKeyByPath(targetPath: String, newKeyName: String): String? {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return null
        val parent = findParentOfPath(normalized) ?: return null
        if (parent.valueType != DataNodeValueType.OBJECT && parent.valueType != DataNodeValueType.MAP) {
            return null
        }
        val targetNode = findByPath(normalized) ?: return null
        val parentPath = parentPathOf(normalized) ?: return null
        val clean = newKeyName.trim()
        if (clean.isEmpty()) return null
        if (clean == targetNode.keyOrIndex) return normalized

        val existingKeys = parent.children.filter { it.id != targetNode.id }.map { it.keyOrIndex }.toSet()
        var uniqueKey = clean
        var suffix = 2
        while (uniqueKey in existingKeys) {
            uniqueKey = "${clean}_$suffix"
            suffix++
        }
        targetNode.keyOrIndex = uniqueKey
        return "$parentPath.$uniqueKey"
    }

    /**
     * Updates this node's [valueType] and [primitiveText]. When switching from a container to a
     * primitive type, child nodes are cleared; when switching to a container type, [primitiveText] is cleared.
     */
    fun updateValueAndType(newType: DataNodeValueType, newPrimitiveText: String = newType.defaultPrimitiveText()) {
        val wasContainer = valueType.isContainer
        valueType = newType
        if (newType.isContainer) {
            primitiveText = ""
            if (newType == DataNodeValueType.ARRAY) {
                reindexArrayChildren()
            } else if (!wasContainer) {
                children.clear()
            } else {
                // Ensure children have valid object/map keys when switching from ARRAY to OBJECT/MAP
                children.forEachIndexed { idx, child ->
                    if (child.keyOrIndex.startsWith("[") && child.keyOrIndex.endsWith("]")) {
                        child.keyOrIndex = "field_$idx"
                    }
                }
            }
        } else {
            children.clear()
            primitiveText = newPrimitiveText
        }
    }

    /**
     * Adds a child node to this container (`OBJECT`, `MAP`, or `ARRAY`).
     * If this node is not currently a container, it is promoted to `OBJECT`.
     */
    fun addChildNode(
        key: String,
        type: DataNodeValueType,
        primitiveValue: String = type.defaultPrimitiveText()
    ): EditableDataNode {
        if (!valueType.isContainer) {
            valueType = DataNodeValueType.OBJECT
            primitiveText = ""
        }
        val resolvedKey = if (valueType == DataNodeValueType.ARRAY) {
            "[${children.size}]"
        } else {
            val baseKey = key.trim().ifEmpty { "newField" }
            ensureUniqueChildKey(baseKey)
        }
        val child = EditableDataNode(
            keyOrIndex = resolvedKey,
            valueType = type,
            primitiveText = if (type.isContainer) "" else primitiveValue
        )
        if (type == DataNodeValueType.OBJECT) {
            child.children.add(
                EditableDataNode(
                    keyOrIndex = "key",
                    valueType = DataNodeValueType.STRING,
                    primitiveText = "value"
                )
            )
        }
        children.add(child)
        if (valueType == DataNodeValueType.ARRAY) {
            reindexArrayChildren()
        }
        return child
    }

    /**
     * Adds a nested branch container (`OBJECT`, `MAP`, or `ARRAY`) inside this branch.
     */
    fun addBranchNode(
        key: String = "newObject",
        branchType: DataNodeValueType = DataNodeValueType.OBJECT
    ): EditableDataNode {
        val resolvedType = if (branchType.isContainer) branchType else DataNodeValueType.OBJECT
        return addChildNode(
            key = key,
            type = resolvedType,
            primitiveValue = ""
        )
    }

    /**
     * Removes the node at [targetPath] from this tree. Cannot remove `"root"`.
     */
    fun removeByPath(targetPath: String): Boolean {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return false
        val parent = findParentOfPath(normalized) ?: return false
        val targetNode = findByPath(normalized) ?: return false
        val removed = parent.children.removeAll { it.id == targetNode.id }
        if (removed && parent.valueType == DataNodeValueType.ARRAY) {
            parent.reindexArrayChildren()
        }
        return removed
    }

    /**
     * Duplicates the node at [targetPath] inside its parent container and returns the new sibling's path.
     */
    fun duplicateByPath(targetPath: String): String? {
        val normalized = targetPath.trim()
        if (normalized == "root" || normalized.isEmpty()) return null
        val parent = findParentOfPath(normalized) ?: return null
        val targetNode = findByPath(normalized) ?: return null
        val parentPath = parentPathOf(normalized) ?: return null
        val sourceIdx = parent.children.indexOfFirst { it.id == targetNode.id }
        if (sourceIdx < 0) return null

        return if (parent.valueType == DataNodeValueType.ARRAY) {
            val copy = targetNode.deepCopy("[${parent.children.size}]")
            parent.children.add(sourceIdx + 1, copy)
            parent.reindexArrayChildren()
            "$parentPath[${sourceIdx + 1}]"
        } else {
            val copyKey = parent.ensureUniqueChildKey("${targetNode.keyOrIndex}_copy")
            val copy = targetNode.deepCopy(copyKey)
            parent.children.add(sourceIdx + 1, copy)
            "$parentPath.$copyKey"
        }
    }

    /**
     * Creates a deep copy of this node subtree with a new [id].
     */
    fun deepCopy(newKeyOrIndex: String = keyOrIndex): EditableDataNode {
        val copy = EditableDataNode(
            keyOrIndex = newKeyOrIndex,
            valueType = valueType,
            primitiveText = primitiveText
        )
        for (child in children) {
            copy.children.add(child.deepCopy(child.keyOrIndex))
        }
        return copy
    }

    /**
     * Re-indexes child keys as `[0]`, `[1]`, ... when this node is an `ARRAY`.
     */
    fun reindexArrayChildren() {
        if (valueType != DataNodeValueType.ARRAY) return
        children.forEachIndexed { idx, child ->
            child.keyOrIndex = "[$idx]"
        }
    }

    /**
     * Returns a human-readable single-line summary for tree list labels.
     */
    fun summaryText(): String = when (valueType) {
        DataNodeValueType.OBJECT, DataNodeValueType.MAP -> "{ ${children.size} ${if (children.size == 1) "field" else "fields"} }"
        DataNodeValueType.ARRAY -> "[ ${children.size} ${if (children.size == 1) "item" else "items"} ]"
        DataNodeValueType.STRING -> "\"$primitiveText\""
        DataNodeValueType.NULL -> "null"
        else -> primitiveText
    }

    private fun ensureUniqueChildKey(baseKey: String): String {
        val existing = children.map { it.keyOrIndex }.toSet()
        if (baseKey !in existing) return baseKey
        var suffix = 2
        while ("${baseKey}_$suffix" in existing) {
            suffix++
        }
        return "${baseKey}_$suffix"
    }

    companion object {
        private var nodeCounter: Long = 0L

        internal fun nextNodeCounter(): Long = ++nodeCounter

        /**
         * Builds an [EditableDataNode] tree from a [PreferenceElement] AST.
         */
        fun fromPreferenceElement(
            element: PreferenceElement,
            keyOrIndex: String = "root"
        ): EditableDataNode {
            val type = DataNodeValueType.fromPreferenceElement(element)
            val primitive = DataNodeValueType.extractPrimitiveText(element)
            val node = EditableDataNode(
                keyOrIndex = keyOrIndex,
                valueType = type,
                primitiveText = primitive
            )
            when (type) {
                DataNodeValueType.OBJECT, DataNodeValueType.MAP -> {
                    for ((k, v) in element.mapEntries) {
                        node.children.add(fromPreferenceElement(v, keyOrIndex = k))
                    }
                }
                DataNodeValueType.ARRAY -> {
                    element.arrayElements.forEachIndexed { idx, item ->
                        node.children.add(fromPreferenceElement(item, keyOrIndex = "[$idx]"))
                    }
                }
                else -> Unit
            }
            return node
        }

        /**
         * Extracts the parent path of a dot/bracket node path (e.g. `"root.items[0]"` -> `"root.items"`).
         */
        fun parentPathOf(path: String): String? {
            val normalized = path.trim()
            if (normalized == "root" || normalized.isEmpty()) return null
            val lastBracket = normalized.lastIndexOf('[')
            val lastDot = normalized.lastIndexOf('.')
            return when {
                lastBracket > lastDot && lastBracket > 0 -> normalized.substring(0, lastBracket)
                lastDot > 0 -> normalized.substring(0, lastDot)
                else -> null
            }
        }
    }
}
