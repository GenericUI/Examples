# Audio Spectrum Visualizer & Media Codec Inspector (`audio-spectrum-visualizer`)

A multiplatform media and visualization showcase built with `GenericUI`, `:extensions:media`, `:extensions:canvas2d`, and `:extensions:gpu` demonstrating:

- **Real-Time FFT Spectrum & `AudioEnergyBands` Telemetry (`AudioAnalyzerExt`)**: Synthesizes multi-harmonic test chords (`#btn-synth-chord`), extracts 64-band FFT frequency bins and time-domain waveforms, and drives live Bass (`#badge-bass`), Midrange (`#badge-mid`), Treble (`#badge-treble`), and RMS/Peak Beat (`#badge-rms`) meters.
- **3 Interactive `Canvas2DExtSurface` Visualizer Modes (`VisualizerDisplayMode`)**:
  - `FFT_SPECTRUM_BARS`: 64-band color-graded logarithmic frequency bars.
  - `OSCILLOSCOPE_WAVEFORM`: Continuous phase-aligned time-domain waveform ribbon.
  - `RADIAL_ENERGY_RINGS`: Concentric Bass, Mid, and Treble pulse rings.
- **WebVTT / SRT Subtitle Cue Synchronization (`WebVttSrtParser`)**: Parses timed `.vtt`/`.srt` subtitle streams and updates `#badge-subtitle-cue` live as transport playback (`#btn-play-pause`, `#btn-seek-forward`) progresses.
- **Hardware & Software Codec Capability Inspector (`MediaCapabilitiesReport`)**: Displays supported `VideoCodec` (`H264`, `HEVC`, `AV1`, `VP9`), `AudioCodec` (`AAC`, `OPUS`, `FLAC`, `MP3`, `EAC3`), and `MediaContainer` capabilities in `#table-media-capabilities`.

---

## Project Layout (`1 Class Per File`)

```text
examples/tools-and-extensions/audio-spectrum-visualizer/
├── README.md
├── settings.gradle.kts
├── gradle.properties
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/visualizer/
    │   │   ├── VisualizerDisplayMode.kt             # Enum of 2D canvas visualization modes
    │   │   ├── AudioSpectrumVisualizerFragment.kt   # Primary UI Fragment (Player + FFT + Canvas2D)
    │   │   ├── AudioSpectrumVisualizerApp.kt        # FragmentApplication + main(args) entry point
    │   │   └── AudioSpectrumVisualizerVisualTestMain.kt # Visual test entry point (runVisualAppTestMain)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/visualizer/
            └── AudioSpectrumVisualizerJestTest.kt   # JestElectricRunner BDD + Robolectric UI test suite
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/audio-spectrum-visualizer/`:

| Target Backend | Development Build Command | Production Build Command |
| :--- | :--- | :--- |
| **Windows (Win32)** | `./gradlew windowsDevelopment` | `./gradlew windowsProduction` |
| **macOS (AppKit)** | `./gradlew macDevelopment` | `./gradlew macProduction` |
| **Linux GTK** | `./gradlew linuxDevelopmentGtk` | `./gradlew linuxProductionGtk` |
| **Linux Qt** | `./gradlew linuxDevelopmentQt` | `./gradlew linuxProductionQt` |
| **JVM / Swing** | `./gradlew swingDevelopment` | `./gradlew swingProduction` |
| **Android** | `./gradlew androidDevelopment` | `./gradlew androidProduction` |
| **iOS** | `./gradlew iosDevelopment` | `./gradlew iosProduction` |
| **Web (WasmJS + JS + PWA)** | `./gradlew webDevelopment` | `./gradlew webProduction` |

---

## Running the `JestElectricRunner` Test Suite

### Headless Multiplatform Unit & UI Tests
```bash
./gradlew jvmTest
```

### Live Windows Visual App Tests (`Win32NativeWindowHost`)
```bash
./gradlew windowsVisualTest
```
