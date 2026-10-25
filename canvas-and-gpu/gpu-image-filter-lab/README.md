# GPU Image Filter Lab (`gpu-image-filter-lab`)

An interactive multi-stage image filter pipeline and live 256-bin RGB/Luminance histogram analyzer built with `:extensions:imageprocessing` and `:extensions:canvas2d` across all 8 `GenericUI` backends.

## Key `GenericUI` Features & Extensions Demonstrated
- **Multi-Stage Filter Pipeline (`:extensions:imageprocessing`)**: Chains `BrightnessContrastFilter`, `HueSaturationLightnessFilter`, `GaussianBlurFilter`, and `VignetteFilter` via `GpuImageProcessor.process(sourceImage) { ... }`.
- **256-Bin Histogram & Mean Luminance (`ImageHistogramResult`)**: Computes real-time R/G/B and luminance histograms (`GpuImageProcessor.computeHistogram`) and renders the luminance distribution onto a dedicated `Canvas2DExtSurface`.
- **One-Click Filter Presets & Sliders**: Switch between `Vivid`, `Noir`, `Warm Cinema`, and `Default` presets or fine-tune parameters via interactive `RangeSlider` controls.
- **1-Class-Per-File Modular Architecture**:
  - `FilterLabState.kt` — `FilterLabState` immutable filter parameter state
  - `GpuImageFilterLabFragment.kt` — `GpuImageFilterLabFragment` filter pipeline & histogram UI controller
  - `GpuImageFilterLabApp.kt` — `GpuImageFilterLabApp` (`FragmentApplication`) and `main(args)` entry point
  - `GpuImageFilterLabVisualTestMain.kt` — `GpuImageFilterLabVisualTestMain` visual test entry point
  - `GpuImageFilterLabJestTest.kt` — `GpuImageFilterLabJestTest` `JestElectricRunner` automated test suite

## Running the Application Across All 8 Backends

From `examples/canvas-and-gpu/gpu-image-filter-lab`:

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

## Running the `JestElectricRunner` Test Suite

```bash
# Run headless multiplatform unit + UI tests (JVM, JS/Wasm, Native)
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Run live onscreen Windows visual tests
../../../gradlew windowsVisualTest
```
