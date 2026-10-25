package com.nexustools.genericui.examples.mediacatalog

/**
 * Immutable model representing a streaming channel or episode entry in the adaptive media catalog.
 */
data class CatalogChannelItem(
    val id: String,
    val title: String,
    val genre: String,
    val durationLabel: String,
    val description: String
)
