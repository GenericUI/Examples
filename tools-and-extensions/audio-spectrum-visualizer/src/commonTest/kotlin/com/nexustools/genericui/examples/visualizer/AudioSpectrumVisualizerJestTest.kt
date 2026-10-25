package com.nexustools.genericui.examples.visualizer

import com.nexustools.genericui.component.media.WebVttSrtParser
import com.nexustools.genericui.enums.media.AudioCodec
import com.nexustools.genericui.enums.media.MediaContainer
import com.nexustools.genericui.enums.media.VideoCodec
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test

/**
 * Cross-platform [JestElectricRunner] BDD & Robolectric UI test suite for `audio-spectrum-visualizer`.
 */
class AudioSpectrumVisualizerJestTest {

    @Test
    fun runAudioSpectrumVisualizerSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Audio Spectrum Visualizer JestElectric Suite",
            visualOnWindows = false
        ) {
            describe("MediaCapabilitiesReport, Transport Controls, FFT Energy Bands & WebVTT Subtitles") {

                it("populates MediaCapabilitiesReport hardware/software codec table on launch") {
                    val controller = launchApp(visualOnWindows = false) {
                        AudioSpectrumVisualizerApp()
                    }

                    val caps = controller.app.visualizerFragment.mediaCapabilities
                    expect(caps.isHardwareAccelerated(VideoCodec.H264)).toBe(true)
                    expect(caps.isHardwareAccelerated(VideoCodec.HEVC)).toBe(true)
                    expect(caps.supportsAudio(AudioCodec.AAC)).toBe(true)
                    expect(caps.supportsAudio(AudioCodec.OPUS)).toBe(true)
                    expect(caps.supportsContainer(MediaContainer.MKV)).toBe(true)
                    expect(caps.supportsHdr10).toBe(true)

                    expectNode(controller.findById("badge-codec-backend")).toContainText("Vulkan")
                    expect(controller.app.visualizerFragment.capabilitiesTable.rows.size).toBeGreaterThan(5)

                    controller.destroy()
                }

                it("synthesizes test chord audio frames and computes FFT spectrum bins & AudioEnergyBands") {
                    val controller = launchFragment {
                        AudioSpectrumVisualizerFragment()
                    }

                    // Initial state before synthesis has 0% energy
                    expectNode(controller.findById("badge-bass")).toHaveText("Bass: 0%")
                    expectNode(controller.findById("badge-mid")).toHaveText("Mid: 0%")
                    expectNode(controller.findById("badge-treble")).toHaveText("Treble: 0%")

                    // Click Synthesize Test Chord button
                    controller.findById("btn-synth-chord").performClick()
                    expect(controller.fragment.chordSynthesisCount).toBe(1)

                    val bands = controller.fragment.audioAnalyzer.computeEnergyBands()
                    expect(bands.bass).toBeGreaterThan(0.6f)
                    expect(bands.mid).toBeGreaterThan(0.5f)
                    expect(bands.treble).toBeGreaterThan(0.4f)
                    expect(bands.overallRms).toBeGreaterThan(0.2f)
                    expect(bands.peakBeatDetected).toBe(true)

                    expectNode(controller.findById("badge-bass")).toContainText("Bass: 76%")
                    expectNode(controller.findById("badge-mid")).toContainText("Mid: 62%")
                    expectNode(controller.findById("badge-treble")).toContainText("Treble: 50%")
                    expectNode(controller.findById("badge-rms")).toContainText("Beat: PEAK")

                    // Verify float and byte FFT / time-domain extraction from AudioAnalyzerExt
                    val floatFreq = FloatArray(64)
                    val byteTime = ByteArray(64)
                    controller.fragment.audioAnalyzer.getFloatFrequencyData(floatFreq)
                    controller.fragment.audioAnalyzer.getByteTimeDomainData(byteTime)
                    expect(floatFreq.any { it > -60f }).toBe(true)
                    expect(byteTime.any { it != 0.toByte() }).toBe(true)

                    controller.destroy()
                }

                it("controls transport Play/Pause/Seek and synchronizes WebVttSrtParser subtitle cues") {
                    val controller = launchFragment {
                        AudioSpectrumVisualizerFragment()
                    }

                    val parsedCues = WebVttSrtParser.parse(AudioSpectrumVisualizerFragment.SAMPLE_WEBVTT_SUBTITLES)
                    expect(parsedCues).toHaveLength(3)
                    expect(parsedCues[0].id).toBe("cue-intro")
                    expect(parsedCues[1].id).toBe("cue-fft")
                    expect(parsedCues[2].id).toBe("cue-gpu")

                    // Start playback via Play / Pause button -> seeks to 1.5s and activates cue-intro
                    controller.findById("btn-play-pause").performClick()
                    expect(controller.fragment.isTransportPlaying).toBe(true)
                    expectNode(controller.findById("badge-playback-state")).toHaveText("PLAYING")
                    expectNode(controller.findById("badge-subtitle-cue"))
                        .toContainText("Welcome to GenericUI Audio Spectrum Visualizer")

                    // Seek +5s -> 6.5s activates cue-fft
                    controller.findById("btn-seek-forward").performClick()
                    expectNode(controller.findById("badge-playback-time")).toHaveText("6.5s")
                    expectNode(controller.findById("badge-subtitle-cue"))
                        .toContainText("Real-time 64-band FFT & AudioEnergyBands decomposition")

                    // Seek +5s -> 11.5s activates cue-gpu
                    controller.findById("btn-seek-forward").performClick()
                    expectNode(controller.findById("badge-playback-time")).toHaveText("11.5s")
                    expectNode(controller.findById("badge-subtitle-cue"))
                        .toContainText("Hardware-accelerated Vulkan & OpenGL ES media pipeline")

                    // Pause playback
                    controller.findById("btn-play-pause").performClick()
                    expect(controller.fragment.isTransportPlaying).toBe(false)
                    expectNode(controller.findById("badge-playback-state")).toHaveText("PAUSED")

                    controller.destroy()
                }

                it("switches between FFT_SPECTRUM_BARS, OSCILLOSCOPE_WAVEFORM, and RADIAL_ENERGY_RINGS modes") {
                    val controller = launchFragment {
                        AudioSpectrumVisualizerFragment()
                    }

                    controller.findById("btn-synth-chord").performClick()
                    expect(controller.fragment.displayMode).toBe(VisualizerDisplayMode.FFT_SPECTRUM_BARS)

                    controller.findById("select-viz-mode")
                        .performSelectValue(VisualizerDisplayMode.OSCILLOSCOPE_WAVEFORM.name)
                    expect(controller.fragment.displayMode).toBe(VisualizerDisplayMode.OSCILLOSCOPE_WAVEFORM)
                    expectNode(controller.findById("badge-active-viz-mode"))
                        .toHaveText(VisualizerDisplayMode.OSCILLOSCOPE_WAVEFORM.displayName)

                    controller.findById("select-viz-mode")
                        .performSelectValue(VisualizerDisplayMode.RADIAL_ENERGY_RINGS.name)
                    expect(controller.fragment.displayMode).toBe(VisualizerDisplayMode.RADIAL_ENERGY_RINGS)
                    expectNode(controller.findById("badge-active-viz-mode"))
                        .toHaveText(VisualizerDisplayMode.RADIAL_ENERGY_RINGS.displayName)

                    controller.destroy()
                }
            }
        }
        report.assertAllPassed()
    }
}
