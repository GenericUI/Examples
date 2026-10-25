package com.nexustools.genericui.examples.dockstudio

/**
 * Immutable model representing a document open inside the Dock Workspace Studio.
 */
data class StudioDocument(
    val id: String,
    val title: String,
    val layerCount: Int,
    val zoomPercent: Int,
    val notes: String
)
