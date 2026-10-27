# Audio Spectrum Visualizer & Media Codec Inspector (`examples/tools-and-extensions/audio-spectrum-visualizer`)

A multiplatform media and visualization showcase built with `GenericUI`, `:extensions:media`, `:extensions:canvas2d`, `:extensions:gpu`, and `:extensions:dsl` across all 8 backends.

---

## Key `GenericUI` Features & Extensions Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `AudioSpectrumVisualizerFragment` (`@MainAppFragment`) and `AudioSpectrumVisualizerApp` (`@AppApplication` extending `FragmentApplication` with `TitleBar`, `MenuBar`, `setTheme(ThemeMode.DARK)`, and `addStyleSheet(StylesPrecompiledStyleSheet)`).
- **`getStyle()` & `getBootstrap()` Styling API**: Configures transport toolbar and spectrum canvas inline styles (`getStyle().apply { borderRadius = "8px"; setCursor(CssCursor.CROSSHAIR) }`) and updates transport button and subtitle/energy badge Bootstrap states via `getBootstrap()`.
- **Real-Time FFT Spectrum & `AudioEnergyBands` Telemetry (`AudioPlayerExt` & `AudioAnalyzerExt`)**: Synthesizes multi-harmonic test chords (`#btn-synth-chord`), extracts 64-band FFT frequency bins and time-domain waveforms, and drives live Bass (`#badge-bass`), Midrange (`#badge-mid`), Treble (`#badge-treble`), and RMS/Peak Beat (`#badge-rms`) meters.
- **3 Interactive `Canvas2DExtSurface` Visualizer Modes (`VisualizerDisplayMode`)**:
  - `FFT_SPECTRUM_BARS`: 64-band color-graded logarithmic frequency bars.
  - `OSCILLOSCOPE_WAVEFORM`: Continuous phase-aligned time-domain waveform ribbon.
  - `RADIAL_ENERGY_RINGS`: Concentric Bass, Mid, and Treble pulse rings.
- **WebVTT / SRT Subtitle Cue Synchronization (`WebVttSrtParser`)**: Parses timed `.vtt`/`.srt` subtitle streams and updates `#badge-subtitle-cue` live as transport playback (`#btn-play-pause`, `#btn-seek-forward`) progresses.
- **Hardware & Software Codec Capability Inspector (`MediaCapabilitiesReport`)**: Displays supported `VideoCodec` (`H264`, `HEVC`, `AV1`, `VP9`), `AudioCodec` (`AAC`, `OPUS`, `FLAC`, `MP3`, `EAC3`), and `MediaContainer` capabilities in `#table-media-capabilities`.
- **`JestElectricRunner` Test Suite**: Full BDD and UI test coverage in `AudioSpectrumVisualizerJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/tools-and-extensions/audio-spectrum-visualizer/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/visualizer/
    │   │   ├── AudioSpectrumVisualizerApp.kt        # @AppApplication FragmentApplication (TitleBar & MenuBar)
    │   │   ├── AudioSpectrumVisualizerFragment.kt   # @MainAppFragment Player + FFT + Canvas2D + Subtitle UI
    │   │   └── VisualizerDisplayMode.kt             # Enum of 2D canvas visualization modes
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/visualizer/
            └── AudioSpectrumVisualizerJestTest.kt   # Cross-platform JestElectricRunner test suite
```

---

## Phase 4 Single-`commonMain` Configuration & Code

### `build.gradle.kts`

```kotlin
plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

genericui {
    app {
        id = "com.nexustools.genericui.examples.visualizer"
        name = "Audio Spectrum Visualizer"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Cross-platform media player, real-time FFT spectrum & waveform visualizer, WebVTT/SRT subtitle inspector, and codec capability viewer across all 8 backends."
    }
    window {
        title = "GenericUI — Media Player, Codec Inspector & FFT Spectrum Visualizer"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#10B981"
    }
    backends {
        all()
    }
    extensions {
        media()
        canvas2d()
        gpu()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`AudioSpectrumVisualizerFragment.kt`)

```kotlin
@MainAppFragment
class AudioSpectrumVisualizerFragment : Fragment() {
    val audioAnalyzer: AudioAnalyzerExt = createBoundAudioAnalyzer()
    val audioPlayer: AudioPlayerExt = AudioPlayerExt("synth://harmonic-chord-progression.wav").apply {
        attachAudioAnalyzer(audioAnalyzer)
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        renderVisualizerFrame()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/audio-spectrum-visualizer`, invoke the root Gradle wrapper (`../../../gradlew`):

| Target Backend | Development Build Command | Production Build Command |
| :--- | :--- | :--- |
| **Windows (`:desktop:windows`)** | `../../../gradlew windowsDevelopment` | `../../../gradlew windowsProduction` |
| **macOS (`:desktop:macos`)** | `../../../gradlew macDevelopment` | `../../../gradlew macProduction` |
| **Linux GTK (`:desktop:gtk`)** | `../../../gradlew linuxDevelopmentGtk` | `../../../gradlew linuxProductionGtk` |
| **Linux Qt (`:desktop:qt`)** | `../../../gradlew linuxDevelopmentQt` | `../../../gradlew linuxProductionQt` |
| **JVM / Swing (`:desktop:swing`)** | `../../../gradlew swingDevelopment` | `../../../gradlew swingProduction` |
| **Android (`:mobile:android`)** | `../../../gradlew androidDevelopment` | `../../../gradlew androidProduction` |
| **iOS (`:mobile:ios`)** | `../../../gradlew iosDevelopment` | `../../../gradlew iosProduction` |
| **Web (`:web` WasmJS + JS + PWA)** | `../../../gradlew webDevelopment` | `../../../gradlew webProduction` |

---

## Running the `JestElectricRunner` Test Suite

```bash
# Headless multiplatform unit & UI tests
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Live Windows visual app tests
../../../gradlew windowsVisualTest
```
