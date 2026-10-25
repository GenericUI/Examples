package com.nexustools.genericui.examples.visualizer

/**
 * Rendering modes supported by the [AudioSpectrumVisualizerFragment] 2D canvas stage.
 */
enum class VisualizerDisplayMode(
    val displayName: String,
    val description: String
) {
    FFT_SPECTRUM_BARS(
        displayName = "64-Band FFT Spectrum Bars",
        description = "Logarithmic frequency spectrum bars with peak hold indicators"
    ),
    OSCILLOSCOPE_WAVEFORM(
        displayName = "Oscilloscope Time-Domain Waveform",
        description = "Continuous stereo phase-aligned PCM waveform ribbon"
    ),
    RADIAL_ENERGY_RINGS(
        displayName = "Radial Energy Rings",
        description = "Concentric Bass, Mid, and Treble audio-reactive pulse rings"
    );

    companion object {
        fun fromNameOrDefault(raw: String?): VisualizerDisplayMode =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
                ?: FFT_SPECTRUM_BARS
    }
}
