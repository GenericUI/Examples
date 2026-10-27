# GPU Image Filter Lab (`examples/canvas-and-gpu/gpu-image-filter-lab`)

An interactive multi-stage image filter pipeline and live 256-bin RGB/Luminance histogram analyzer built with `:extensions:gpu`, `:extensions:imageprocessing`, `:extensions:canvas2d`, and `:extensions:dsl` across all 8 `GenericUI` backends.

---

## Key `GenericUI` Features & Extensions Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `GpuImageFilterLabFragment` (`@MainAppFragment`) and `GpuImageFilterLabApp` (`@AppApplication` extending `FragmentApplication` with `setTheme(ThemeMode.DARK)` and `addStyleSheet(StylesPrecompiledStyleSheet)`).
- **`getStyle()` & `getBootstrap()` Styling API**: Configures preview and histogram canvas inline styles via `getStyle().apply { borderRadius = "8px"; setCursor(CssCursor.CROSSHAIR) }` and updates preset badge/button visual state via `getBootstrap()`.
- **Multi-Stage Filter Pipeline (`:extensions:imageprocessing`)**: Chains `BrightnessContrastFilter`, `HueSaturationLightnessFilter`, `GaussianBlurFilter`, and `VignetteFilter` via `GpuImageProcessor.process(sourceImage) { ... }`.
- **256-Bin Histogram & Mean Luminance (`ImageHistogramResult`)**: Computes real-time R/G/B and luminance histograms (`GpuImageProcessor.computeHistogram`) and renders the luminance distribution onto a dedicated `Canvas2DExtSurface` (`histogram-canvas`).
- **One-Click Filter Presets & Sliders**: Switch between `Vivid`, `Noir`, `Warm Cinema`, and `Default` presets (`FilterLabState`) or fine-tune parameters via interactive `RangeSlider` controls (`slider-brightness`, `slider-contrast`, `slider-saturation`, `slider-blur`).
- **`JestElectricRunner` Test Suite**: Automated filter pipeline, histogram, and UI tests in `GpuImageFilterLabJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/canvas-and-gpu/gpu-image-filter-lab/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/filterlab/
    │   │   ├── FilterLabState.kt              # Immutable filter parameter state model
    │   │   ├── GpuImageFilterLabApp.kt        # @AppApplication FragmentApplication (TitleBar & Styles)
    │   │   └── GpuImageFilterLabFragment.kt   # @MainAppFragment GPU filter pipeline & histogram UI
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/filterlab/
            └── GpuImageFilterLabJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.filterlab"
        name = "GPU Image Filter Lab"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Real-time offscreen GPU image processing pipeline and 256-bin R/G/B/Luminance histogram lab built with GenericUI."
    }
    window {
        title = "Offscreen GPU Image Filter & Histogram Lab"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#8B5CF6"
    }
    backends {
        all()
    }
    extensions {
        gpu()
        imageProcessing()
        canvas2d()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`GpuImageFilterLabFragment.kt`)

```kotlin
@MainAppFragment
class GpuImageFilterLabFragment : Fragment() {
    val sourceImage: ImageBuffer = createProceduralTestImage(width = 160, height = 120)
    var filterState: FilterLabState = FilterLabState()
        private set

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("filter-lab-root")
        buildUi()
        recomputePipeline()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/canvas-and-gpu/gpu-image-filter-lab`, invoke the root Gradle wrapper (`../../../gradlew`):

```bash
# Desktop JVM (AWT/Swing + optional LWJGL)
../../../gradlew swingDevelopment

# Desktop Windows (Win32 / Direct2D / ANGLE / Vulkan)
../../../gradlew windowsDevelopment

# Desktop macOS (AppKit / CoreGraphics / MoltenVK)
../../../gradlew macDevelopment

# Desktop Linux GTK & Qt
../../../gradlew linuxDevelopmentGtk
../../../gradlew linuxDevelopmentQt

# Web (WasmJS primary + JS IR fallback + PWA manifest.webmanifest)
../../../gradlew webDevelopment

# Mobile (Android & iOS)
../../../gradlew androidDevelopment
../../../gradlew iosDevelopment
```

---

## Running the `JestElectricRunner` Test Suite

```bash
# Run headless multiplatform unit + UI tests (JVM, JS/Wasm, Native)
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Run live onscreen Windows visual tests
../../../gradlew windowsVisualTest
```
