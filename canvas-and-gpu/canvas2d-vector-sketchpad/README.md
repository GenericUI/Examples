# Canvas2D Vector & Bezier Sketchpad (`canvas2d-vector-sketchpad`)

Interactive 2D vector drawing and cubic-Bezier curve sketchpad demonstrating `:extensions:canvas2d` (`Canvas2DExtSurface`, `CurveEditorExt`), hardware VBlank rendering (`registerVBlankRender`), undo/redo command history, and `JestElectricRunner` cross-platform UI testing across all 8 `GenericUI` backends.

## Key `GenericUI` APIs Demonstrated

- **`:extensions:canvas2d` (`Canvas2DExtSurface` & `CurveEditorExt`)**: Immediate-mode 2D vector drawing (`beginPath`, `moveTo`, `lineTo`, `bezierCurveTo`, `arc`, `fillRect`, `strokeRect`) and interactive cubic-Bezier curve editing (`sampleCurveY`, `sampleLut`).
- **Hardware VBlank Rendering (`AbstractCanvas.registerVBlankRender`)**: Frame-accurate rendering loop tracking `deltaMs` and VBlank frame counters.
- **Form & Action Controls**: Tool switcher `Button`s (`Freehand`, `Rectangle`, `Ellipse`, `Line`, `Bezier Curve`), stroke width `RangeSlider`, `ColorPicker`, Undo/Redo history stack, and `Dialogs.saveFile`.
- **1-Class-Per-File Modular Architecture**:
  - `SketchShapeType.kt` — `SketchShapeType` enum (`FREEHAND`, `RECTANGLE`, `ELLIPSE`, `LINE`, `BEZIER_CURVE`)
  - `SketchVectorCommand.kt` — `SketchVectorCommand` immutable drawing command model
  - `SketchpadFragment.kt` — `SketchpadFragment` primary UI and canvas rendering controller
  - `SketchpadApp.kt` — `SketchpadApp` (`FragmentApplication`) and `main(args)` entry point
  - `SketchpadVisualTestMain.kt` — `SketchpadVisualTestMain` visual test entry point
  - `SketchpadJestTest.kt` — `SketchpadJestTest` `JestElectricRunner` automated test suite

## Running the Application Across All 8 Backends

From `examples/canvas-and-gpu/canvas2d-vector-sketchpad`:

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
