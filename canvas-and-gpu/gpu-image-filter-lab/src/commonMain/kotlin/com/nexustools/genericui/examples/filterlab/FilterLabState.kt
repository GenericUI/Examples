package com.nexustools.genericui.examples.filterlab

/**
 * Immutable parameter state for the GPU Image Filter Lab pipeline.
 */
data class FilterLabState(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hueShiftDegrees: Float = 0f,
    val blurRadius: Int = 0,
    val vignetteStrength: Float = 0f,
    val activePresetName: String = "Default"
)
