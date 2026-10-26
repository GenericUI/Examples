# Canvas2D Vector & Bezier Sketchpad (`examples/canvas-and-gpu/canvas2d-vector-sketchpad`)

Interactive 2D vector drawing and cubic-Bezier curve sketchpad demonstrating `:extensions:canvas2d` (`Canvas2DExtSurface`, `CurveEditorExt`), `:extensions:dsl`, hardware VBlank rendering (`onRender`), undo/redo command history (`SketchVectorCommand`), and `JestElectricRunner` cross-platform UI testing across all 8 `GenericUI` backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `SketchpadFragment` (`@MainAppFragment`) and `SketchpadApp` (`@AppApplication` extending `FragmentApplication`).
- **`:extensions:canvas2d` (`Canvas2DExtSurface` & `CurveEditorExt`)**: Immediate-mode 2D vector drawing (`beginPath`, `moveTo`, `lineTo`, `bezierCurveTo`, `arc`, `fillRect`, `strokeRect`) and interactive cubic-Bezier curve editing (`sampleCurveY`).
- **Hardware VBlank Rendering**: Frame-accurate rendering loop tracking `deltaMs` and VBlank frame counters (`badge-vblank-frames`).
- **Form & Action Controls**: Tool switcher `Button`s (`Freehand`, `Rectangle`, `Ellipse`, `Line`, `Bezier Curve` via `SketchShapeType`), stroke width `RangeSlider` (`slider-stroke-width`), `ColorPicker` (`picker-stroke-color`), Undo/Redo history stack, and PNG export (`Dialogs.saveFile`).
- **`JestElectricRunner` Test Suite**: Automated vector drawing, undo/redo, and UI tests in `SketchpadJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/canvas-and-gpu/canvas2d-vector-sketchpad/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/sketchpad/
    │   │   ├── SketchShapeType.kt       # Shape tool enum (FREEHAND, RECTANGLE, ELLIPSE, LINE, BEZIER_CURVE)
    │   │   ├── SketchVectorCommand.kt   # Immutable vector drawing command model
    │   │   ├── SketchpadApp.kt          # @AppApplication FragmentApplication (TitleBar & Styles)
    │   │   └── SketchpadFragment.kt     # @MainAppFragment Canvas2DExtSurface & CurveEditorExt UI
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/sketchpad/
            └── SketchpadJestTest.kt     # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.sketchpad"
        name = "Canvas2D Vector Sketchpad"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Interactive 2D vector drawing and cubic-Bezier curve sketchpad built with GenericUI."
    }
    window {
        title = "Canvas2D Vector & Bezier Sketchpad"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#3B82F6"
    }
    backends {
        all()
    }
    extensions {
        canvas2d()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`SketchpadFragment.kt`)

```kotlin
@MainAppFragment
class SketchpadFragment : Fragment() {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("sketchpad-root")
        buildUi()
        syncStatusControls()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/canvas-and-gpu/canvas2d-vector-sketchpad`, invoke the root Gradle wrapper (`../../../gradlew`):

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
