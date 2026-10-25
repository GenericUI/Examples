# GenericUI Examples

This directory contains 13 standalone Kotlin Multiplatform example applications built with **GenericUI** (`id("com.nexustools.genericui")`). Each example is organized by category, links against the root `GenericUI` repository via Gradle composite builds (`includeBuild("../../..")`), targets all 8 platform backends (`windows`, `macos`, `gtk`, `qt`, `swing`, `android`, `ios`, `web`), and includes automated **`JestElectricRunner`** UI tests.

For full framework documentation, see the [Root README](../README.md) and the [Documentation Index](../docs/README.md).

---

## Example Categories

### Category 1 — Basics, Reactive State, Forms, & Persistence (`basics/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **Counter & Theme Switcher** | [`basics/counter-and-theme-switcher`](./basics/counter-and-theme-switcher/README.md) | `FragmentApplication`, `Fragment`, `MutableState<T>` / `State<T>`, `ThemeMode` (`LIGHT`, `DARK`, `SYSTEM`), `Button` (`htmlTag = "button"`), `Heading` (`h1`–`h6`), `Badge`, `Switch`, `Select`, `Card`, `ProgressBar`, `ToastManager`, and declarative `:extensions:dsl` (`buildUi`). |
| **Validated Registration Wizard** | [`basics/validated-registration-wizard`](./basics/validated-registration-wizard/README.md) | `PageHostFragment`, `ValidatedStepPageFragment`, `StepValidationRule` (`RequiredTextRule`, `EmailFormatRule`, `MinLengthRule`, `StepValidationRules.custom`), `TextField`, `Checkbox`, `RadioGroup`, `RangeSlider`, `Accordion`, and step progress navigation. |
| **Markdown Notes & Preferences** | [`basics/markdown-notes-and-preferences`](./basics/markdown-notes-and-preferences/README.md) | `SplitPane`, `ListView`, `SearchBox`, `TextArea`, `MarkdownText`, `TagEditor`, `PreferenceStore` (cross-platform key-value persistence with `PreferenceChangeListener`), and `Dialogs.confirm`. |

---

### Category 2 — Virtualized Data, Adaptive Responsive Shells, & Dockable Workspaces (`layouts-and-data/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **Virtualized Data Explorer** | [`layouts-and-data/virtualized-data-explorer`](./layouts-and-data/virtualized-data-explorer/README.md) | `Table` (`TableColumn`, multi-column sorting), `LazyList` (virtualized 10,000-row feed with recycled view holders), `Pagination`, `SearchBox`, `Breadcrumb`, and `Popover` / `ContextMenu`. |
| **Adaptive Media Catalog** | [`layouts-and-data/adaptive-media-catalog`](./layouts-and-data/adaptive-media-catalog/README.md) | `AdaptiveSplitTabFragment` (desktop side-by-side split view vs. mobile tabbed navigation), `LazyGrid`, `Carousel`, `ChipGroup`, `RatingBar`, `Avatar`, `BottomSheet`, and `Drawer`. |
| **Dock Workspace Studio** | [`layouts-and-data/dock-workspace-studio`](./layouts-and-data/dock-workspace-studio/README.md) | `DockWorkspaceFragment`, `DockWorkspaceMode` (`SINGLE_WINDOW` vs. `MULTI_WINDOW`), `DockArea` (`LEFT`, `RIGHT`, `TOP`, `BOTTOM`, `CENTER`, `FLOATING`), `DockPanel`, `TreeView`, `PropertyGrid`, `MenuBar`, and `StatusBar`. |

---

### Category 3 — 2D Vector Canvas, GPU Shader Filters, & Physics Simulation (`canvas-and-gpu/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **Canvas2D Vector Sketchpad** | [`canvas-and-gpu/canvas2d-vector-sketchpad`](./canvas-and-gpu/canvas2d-vector-sketchpad/README.md) | `:extensions:canvas2d` (`Canvas2DExtSurface`, `CurveEditorExt`), interactive vector drawing (`strokeShape`, `fillShape`, `LinearGradient`, `RadialGradient`), `ColorPicker`, `Toolbar`, `ToggleGroup`, and undo/redo history. |
| **GPU Image Filter Lab** | [`canvas-and-gpu/gpu-image-filter-lab`](./canvas-and-gpu/gpu-image-filter-lab/README.md) | `:extensions:gpu` & `:extensions:imageprocessing` (`GpuImageProcessor`, `GpuImagePipelineBuilder`, `BrightnessContrastFilter`, `HueSaturationLightnessFilter`, `GaussianBlurFilter`, `VignetteFilter`, `ColorMatrixFilter`, and real-time RGBA histogram computation). |
| **Box2D Physics Playground** | [`canvas-and-gpu/box2d-physics-playground`](./canvas-and-gpu/box2d-physics-playground/README.md) | `:extensions:box2d` & `:extensions:canvas2d` (`World2D`, `BodyDef2D`, `BodyType2D`, `PolygonShape2D`, `CircleShape2D`, `RevoluteJoint2D`, `Box2DDebugDrawer`, VBlank simulation loop, and interactive rigid-body spawning). |

---

### Category 4 — Developer Tools, Code Editors, MDI & Live SCSS, Serialization, & Media (`tools-and-extensions/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **BSON / JSON / Binary Plist Visual Editor** | [`tools-and-extensions/bson-json-plist-editor`](./tools-and-extensions/bson-json-plist-editor/README.md) | `:common` `Json`, `BinaryJson` (BSON), and `BinaryPlist` (`bplist00`) codecs, `MenuBar`, `SplitPane`, `TabLayout` with `CloseableTab`s, `TreeView`, `Table`, and lossless cross-format conversion. Also available at [`bson-json-plist-editor`](./bson-json-plist-editor/README.md). |
| **Code Editor & Regex Tester** | [`tools-and-extensions/code-editor-and-regex-tester`](./tools-and-extensions/code-editor-and-regex-tester/README.md) | `:extensions:codeeditor` (`IdeCodeEditor`, `CodeLanguage` syntax highlighting, line numbers, `CodeDiagnostic` markers), live regular expression pattern & flags testing, match table, and replacement preview. |
| **Audio Spectrum Visualizer** | [`tools-and-extensions/audio-spectrum-visualizer`](./tools-and-extensions/audio-spectrum-visualizer/README.md) | `:extensions:media` & `:extensions:canvas2d` (`AudioPlayerExt`, `AudioAnalyzerExt`, `AudioEnergyBands`, real-time FFT spectrum bar and waveform oscilloscope rendering, playlist management, and transport controls). |
| **MDI Component & Live SCSS Showcase** | [`tools-and-extensions/mdi-component-and-scss-showcase`](./tools-and-extensions/mdi-component-and-scss-showcase/README.md) | `MdiContainer` & `MdiWindow` (`genericui:mdicontainer` / `genericui:mdiwindow`), every major `GenericUI` component & `com.nexustools.genericui.component.htmlcompat` tag class (`H1`–`H6`, `Header`, `Span`, `Div`, etc.), embedded `DockWorkspaceFragment`, `:extensions:codeeditor` `CodeLanguage.SCSS`, and `LiveScssStyleEngine` preloaded with a scoped Rainbow Prism SCSS theme. |

---

## Running & Testing Examples

Navigate into any example directory and invoke the root Gradle wrapper (`../../../gradlew` for categorized examples):

```bash
cd examples/tools-and-extensions/mdi-component-and-scss-showcase

# Print resolved GenericUI backends, extensions, and style pipeline configuration
../../../gradlew printGenericUiConfig

# Run headless JestElectricRunner UI tests on JVM / Swing
../../../gradlew jvmTest

# Run live onscreen Windows visual UI tests via JestElectricRunner
../../../gradlew windowsVisualTest

# Build and launch for specific GenericUI targets
../../../gradlew swingDevelopment
../../../gradlew windowsDevelopment
../../../gradlew webDevelopment
```
