package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.component.dialog.Dialogs
import com.nexustools.genericui.component.dialog.SelectedFile
import com.nexustools.genericui.component.shell.Window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Cross-platform file open/save helper bridging [Dialogs.openFile], [Dialogs.saveFile],
 * and [SelectedFile] with in-memory fallback caching so opening and saving `.json`, `.bson`,
 * and `.plist` documents works seamlessly across Desktop, Mobile, Swing, and Web.
 */
object DataEditorFileIoHelper {

    private val ioScope = CoroutineScope(Dispatchers.Default)
    private val inMemoryVirtualFiles = linkedMapOf<String, ByteArray>()

    /**
     * Stores or updates a virtual file in memory (used in headless tests or when a platform
     * `SelectedFile` does not provide a direct filesystem reader/writer).
     */
    fun putVirtualFile(fileNameOrPath: String, bytes: ByteArray) {
        inMemoryVirtualFiles[fileNameOrPath] = bytes.copyOf()
    }

    /**
     * Reads a virtual file previously saved in memory, or `null` if not found.
     */
    fun getVirtualFile(fileNameOrPath: String): ByteArray? =
        inMemoryVirtualFiles[fileNameOrPath]?.copyOf()

    /**
     * Opens the native multi-format file picker (`*.json`, `*.bson`, `*.bjson`, `*.plist`)
     * and decodes the selected file into a new [DataDocumentTabState].
     */
    fun openDocumentWithDialog(
        ownerWindow: Window?,
        nextTabId: String,
        onLoaded: (DataDocumentTabState, SelectedFile) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        Dialogs.openFile(
            title = "Open JSON, BSON, or Binary Plist Document",
            filters = DataFormat.allFileTypeFilters(),
            ownerWindow = ownerWindow,
            onSelected = { selectedFile ->
                readSelectedFile(
                    selectedFile = selectedFile,
                    nextTabId = nextTabId,
                    onLoaded = { tabState -> onLoaded(tabState, selectedFile) },
                    onError = onError
                )
            },
            onCancelled = onCancelled
        )
    }

    /**
     * Reads [selectedFile] asynchronously and constructs a [DataDocumentTabState].
     */
    fun readSelectedFile(
        selectedFile: SelectedFile,
        nextTabId: String,
        onLoaded: (DataDocumentTabState) -> Unit,
        onError: (String) -> Unit
    ) {
        ioScope.launch {
            try {
                val rawBytes = selectedFile.readBytes().let { bytes ->
                    if (bytes.isNotEmpty()) {
                        bytes
                    } else {
                        inMemoryVirtualFiles[selectedFile.pathOrUri]
                            ?: inMemoryVirtualFiles[selectedFile.name]
                            ?: ByteArray(0)
                    }
                }
                if (rawBytes.isEmpty()) {
                    onError("Selected file '${selectedFile.name}' is empty or could not be read.")
                    return@launch
                }
                val fileName = selectedFile.name.ifBlank {
                    selectedFile.pathOrUri.substringAfterLast('/').substringAfterLast('\\')
                }.ifBlank { "document.json" }

                val tabState = DataDocumentTabState(
                    tabId = nextTabId,
                    title = fileName,
                    filePath = selectedFile.pathOrUri
                )
                val ok = tabState.loadFromBytes(
                    bytes = rawBytes,
                    explicitFormat = null,
                    fileNameHint = fileName,
                    markDirty = false
                )
                if (ok) {
                    onLoaded(tabState)
                } else {
                    onError(tabState.lastJsonError ?: "Failed to decode '${selectedFile.name}'.")
                }
            } catch (t: Throwable) {
                onError(t.message ?: "Error reading '${selectedFile.name}'.")
            }
        }
    }

    /**
     * Opens the native save file dialog for [tabState] (defaulting to [targetFormat]'s extension)
     * and writes the encoded document bytes.
     */
    fun saveDocumentWithDialog(
        ownerWindow: Window?,
        tabState: DataDocumentTabState,
        targetFormat: DataFormat = tabState.format,
        prettyJsonIndent: Int = 2,
        onSaved: (SelectedFile, ByteArray) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val baseStem = tabState.title.substringBeforeLast('.', tabState.title).ifBlank { "document" }
        val suggestedName = "$baseStem${targetFormat.defaultExtension}"
        val filters = listOf(
            targetFormat.toFileTypeFilter(),
            DataFormat.JSON.toFileTypeFilter(),
            DataFormat.BINARY_JSON_BSON.toFileTypeFilter(),
            DataFormat.BINARY_PLIST.toFileTypeFilter()
        ).distinctBy { it.description }

        Dialogs.saveFile(
            title = "Save as ${targetFormat.displayName}",
            suggestedFileName = suggestedName,
            filters = filters,
            ownerWindow = ownerWindow,
            onSelected = { selectedFile ->
                writeSelectedFile(
                    selectedFile = selectedFile,
                    tabState = tabState,
                    fallbackFormat = targetFormat,
                    prettyJsonIndent = prettyJsonIndent,
                    onSaved = onSaved,
                    onError = onError
                )
            },
            onCancelled = onCancelled
        )
    }

    /**
     * Encodes [tabState] and writes the bytes to [selectedFile].
     */
    fun writeSelectedFile(
        selectedFile: SelectedFile,
        tabState: DataDocumentTabState,
        fallbackFormat: DataFormat = tabState.format,
        prettyJsonIndent: Int = 2,
        onSaved: (SelectedFile, ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        ioScope.launch {
            try {
                val resolvedFormat = DataFormat.fromFileName(selectedFile.name) ?: fallbackFormat
                tabState.format = resolvedFormat
                if (selectedFile.name.isNotBlank()) {
                    tabState.title = selectedFile.name
                }
                tabState.filePath = selectedFile.pathOrUri
                val bytes = tabState.toFileBytes(
                    targetFormat = resolvedFormat,
                    prettyJsonIndent = prettyJsonIndent
                )
                inMemoryVirtualFiles[selectedFile.pathOrUri] = bytes
                inMemoryVirtualFiles[selectedFile.name] = bytes
                selectedFile.writeBytes(bytes)
                tabState.isDirty = false
                onSaved(selectedFile, bytes)
            } catch (t: Throwable) {
                onError(t.message ?: "Failed to save '${selectedFile.name}'.")
            }
        }
    }
}
