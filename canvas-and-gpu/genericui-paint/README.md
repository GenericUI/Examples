# GenericUIPaint — Windows 11 Dark-Mode Paint Clone (`examples/canvas-and-gpu/genericui-paint`)

Complete standalone Kotlin Multiplatform clone of **Windows 11 Dark-Mode Paint** (`Untitled - Paint`) built with `:extensions:canvas2d` (`Canvas2D`), `:extensions:imageprocessing` (`ImageBuffer2DContext`), `:extensions:dsl`, binary project serialization (`WritableByteBuffer` / `ReadableByteBuffer`), and `getPreferenceStore()` persistence across all 8 `GenericUI` backends.

---

## Key `GenericUI` APIs Demonstrated

- **Windows 11 Dark-Mode Paint Chrome & Ribbon UI**:
  - **Top Command Row (`#18181B`)**: `File`, `Edit`, `View` menu buttons + `💾 Save`, `📤 Export`, `↶ Undo`, `↷ Redo` on the left; `📣 Feedback`, `👤 Profile`, `⚙ Settings` on the right.
  - **Grouped Ribbon Bar (`#222226`)**:
    - **Selection**: Dashed rectangular selection tool (`Selection ▾`).
    - **Image**: `Crop`, `Rotate ▾`, `Flip ▾`, `Remove BG` canvas transforms.
    - **Tools**: 2×3 tool grid (`Pencil`, `Fill Bucket`, `Text 'A'`, `Eraser`, `Color Picker`, `Magnifier`).
    - **Brushes**: Highlighted `Brush ▾` tool button paired with a `PaintBrushStyle` `Select` dropdown (`Brush`, `Calligraphy Pen`, `Airbrush`, `Oil Brush`, `Watercolor`, `Marker`, `Crayon`).
    - **Shapes**: Scrollable 20-shape gallery (`Line`, `Curve`, `Oval`, `Rectangle`, `Rounded Rectangle`, `Polygon`, `Triangle`, `Right Triangle`, `Diamond`, `Pentagon`, `Hexagon`, `Right/Left/Up/Down Arrow`, `4/5/6-Point Star`, `Speech Bubble`, `Heart`) + `Outline ▾` and `Fill ▾` toggles.
    - **Colours**: Active Foreground (`#000000`) & Background (`#FFFFFF`) circular swatches, 20 default Windows 11 Paint palette circles (2 rows of 10), 10 custom swatch slots, and rainbow `ColorPicker` wheel button.
    - **Copilot & Layers**: `✨ Copilot ▾` AI-assisted vector generator and `⧉ Layers` button toggling the collapsible right-side multi-layer panel (`+ Add`, `👁 Visibility`, `🗑 Delete`, `⤵ Merge`).
  - **Main Workspace Area (`#141416`)**:
    - Floating left vertical pill sliders for **Brush Size** (`1..64 px`) and **Brush Opacity** (`1..100%`).
    - Centered white drawing canvas (`default 131 × 128px`, matching the Windows 11 Paint screenshot) with 8 square perimeter resize handles and hardware-accelerated `Canvas2D` (`com.nexustools.genericui.component.media.Canvas2D`).
  - **Bottom Status Bar (`#1C1C20`)**:
    - Live cursor coordinates (`x, ypx`), selection box dimensions, canvas size badge (`131 × 128px`), fit-to-window (`[⊙]`), zoom percentage selector (`100% ▾`), and zoom `- / slider / +` controls.
- **Binary Project Serialization (`Writable` & `Readable`)**:
  - `exportCanvasToWritable(writable: Writable)` and `importCanvasFromReadable(readable: Readable)` serialize and restore the entire multi-layer project using `WritableByteBuffer` and `ReadableByteBuffer`.
- **Cross-Platform Preference Persistence (`getPreferenceStore()`)**:
  - `PaintPreferences` implements `PreferenceSerializable` to persist canvas dimensions, active foreground/background colours, brush size/opacity, zoom level, and layers panel state.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/canvas-and-gpu/genericui-paint/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/paint/
    │   │   ├── PaintTool.kt                 # 9 Paint tools (SELECTION, PENCIL, FILL_BUCKET, TEXT, ERASER, COLOR_PICKER, MAGNIFIER, BRUSH, SHAPE)
    │   │   ├── PaintBrushStyle.kt           # 7 brush presets (BRUSH, CALLIGRAPHY_PEN, AIRBRUSH, OIL_BRUSH, WATERCOLOR, MARKER, CRAYON)
    │   │   ├── PaintShapeType.kt            # 20 Windows 11 Paint shapes (lines, curves, polygons, arrows, stars, callouts, heart)
    │   │   ├── PaintLayer.kt                # Multi-layer model (id, name, visible, opacity, strokes)
    │   │   ├── PaintStrokeCommand.kt        # Stroke/shape/fill command + binary Writable/ReadableByteBuffer codec
    │   │   ├── PaintPreferences.kt          # PreferenceSerializable workspace settings for getPreferenceStore()
    │   │   ├── GenericUIPaintFragment.kt    # @MainAppFragment Windows 11 Dark-Mode Paint UI & Canvas2D renderer
    │   │   └── GenericUIPaintApp.kt         # @AppApplication FragmentApplication (TitleBar, MenuBar, Styles, PreferenceStore)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/paint/
            └── GenericUIPaintJestTest.kt    # Cross-platform JestElectricRunner test suite
```

---

## Building & Running Across All 8 Backends

From `examples/canvas-and-gpu/genericui-paint`, invoke the root Gradle wrapper (`../../../gradlew`):

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
