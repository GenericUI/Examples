package com.nexustools.genericui.examples.dataexplorer

/**
 * Immutable telemetry record representing one row or node inside the 10,000-item
 * virtualized dataset explorer.
 */
data class DatasetRecord(
    val id: Int,
    val title: String,
    val category: String,
    val latencyMs: Int,
    val status: String
)
