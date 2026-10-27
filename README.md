# GenericUI Examples

This directory contains **13 standalone Kotlin Multiplatform example applications** built with **GenericUI** (`id("com.nexustools.genericui")`). Each example is organized by category, links against the root `GenericUI` repository via Gradle composite builds (`includeBuild("../../..")`), targets all 8 platform backends (`windows`, `macos`, `gtk`, `qt`, `swing`, `android`, `ios`, `web`), and includes automated **`JestElectricRunner`** UI tests.

For full framework documentation, see the [Root README](../README.md) and the [Documentation Index](../docs/README.md).

---

## Single-`commonMain` Architecture Across All 13 Examples

Every example application uses a single shared `src/commonMain/kotlin` source set (`1 class per .kt file`) with annotation-driven fragment discovery (`@MainAppFragment`, `@AppFragment`, and `@AppApplication`) and zero per-backend `*Main` directories or manual `main()` / bootstrap boilerplate:

```text
<example-dir>/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/<pkg>/
    │   │   ├── <Example>Fragment.kt     # @MainAppFragment primary UI Fragment
    │   │   ├── <Example>App.kt          # @AppApplication FragmentApplication (TitleBar, MenuBar, Styles)
    │   │   └── ...                      # Domain models & helper @AppFragment classes (1 class per .kt file)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/<pkg>/
            └── <Example>JestTest.kt     # Cross-platform JestElectricRunner test suite
```

In `build.gradle.kts`, the `genericui { ... }` DSL configures application metadata (`app { ... }`), window defaults (`window { ... }`), theme (`theme { ... }`), all 8 backends (`backends { all() }`), optional extensions (`extensions { ... }`), SASS/CSS compilation (`styles { ... }`), and `JestElectricRunner` testing (`testing { ... }`):

```kotlin
plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

genericui {
    app {
        id = "com.nexustools.genericui.examples.counter"
        name = "Counter & Theme Switcher"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Hello GenericUI: Reactive counter, step selector, runtime theme switcher, and PreferenceStore persistence."
    }
    window {
        title = "Counter & Theme Switcher"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#2563EB"
    }
    backends {
        all()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

---

## Example Categories

### Category 1 — Basics, Reactive State, Forms, & Persistence (`basics/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **Counter & Theme Switcher** | [`basics/counter-and-theme-switcher`](./basics/counter-and-theme-switcher/README.md) | `@MainAppFragment` (`CounterFragment`), `@AppApplication` (`CounterApp`), `TitleBar`, `MenuBar`, `ThemeMode` (`LIGHT`, `DARK`, `AUTO`), runtime accent `themeColor` switching (`#2563EB`, `#10B981`, `#8B5CF6`), `getStyle()` & `getBootstrap()`, `Button`, `Badge`, `Select`, `Switch`, `Card`, `addStyleSheet(StylesPrecompiledStyleSheet)`, and `PreferenceStore` (`CounterSettings` with `PreferenceSerializable` & `PreferenceObjectCodec`). |
| **Validated Registration Wizard** | [`basics/validated-registration-wizard`](./basics/validated-registration-wizard/README.md) | `@MainAppFragment` (`RegistrationWizardFragment`), `@AppApplication` (`RegistrationWizardApp`), 3-step validated developer onboarding flow, `getStyle()` & `getBootstrap()`, `TextField`, `PasswordField`, `Select`, `RangeSlider`, `CheckBox`, `ProgressBar`, contextual `Alert`, `Badge`, and `PreferenceStore` persistence (`RegistrationProfile`). |
| **Markdown Notes & Preferences** | [`basics/markdown-notes-and-preferences`](./basics/markdown-notes-and-preferences/README.md) | `@MainAppFragment` (`NotesFragment`), `@AppApplication` (`NotesApp`), two-pane `SplitPane`, `ListGroup` / `ListGroupItem`, `SearchField`, `TextField`, `TextArea`, `CheckBox`, live `MarkdownView` preview, `getStyle()` & `getBootstrap()`, `PreferenceStore` (`putObject` / `getObject` with `NoteItem` and `PreferenceChangeListener`), and native `.md` import/export via `Dialogs.openFile` & `Dialogs.saveFile`. |

---

### Category 2 — Virtualized Data, Adaptive Responsive Shells, & Dockable Workspaces (`layouts-and-data/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **Virtualized Data Explorer** | [`layouts-and-data/virtualized-data-explorer`](./layouts-and-data/virtualized-data-explorer/README.md) | `@MainAppFragment` (`VirtualizedDataExplorerFragment`), 10,000-row dataset (`DatasetRecord`) across `LazyList`, `LazyGrid`, and `TreeView` (`ExplorerViewMode`), true `[firstVisibleIndex .. lastVisibleIndex]` viewport virtualization (`LazyViewportWindow`), live `onLoadItem` / `onUnloadItem` recycling counters, `getStyle()` & `getBootstrap()`, and search/category filtering. |
| **Adaptive Media Catalog** | [`layouts-and-data/adaptive-media-catalog`](./layouts-and-data/adaptive-media-catalog/README.md) | `@MainAppFragment` (`AdaptiveMediaCatalogFragment : AdaptiveSplitTabFragment`), responsive 3-posture layout adaptation (`TABLET_DESKTOP_COLUMNS` at `1280x800`, `PHONE_LANDSCAPE_FULLSCREEN` at `800x390`, `PORTRAIT_SINGLE_FLOW_TABS` at `390x844`), `WindowConfiguration`, `TabContainer` (`Channels`, `Queue`, `Details`), `getStyle()` & `getBootstrap()`, and live transport/queue controls (`CatalogChannelItem`). |
| **Dock Workspace Studio** | [`layouts-and-data/dock-workspace-studio`](./layouts-and-data/dock-workspace-studio/README.md) | `@MainAppFragment` (`DockWorkspaceStudioFragment : OverlayDockWorkspaceFragment`), `LEFT` Toolbox, `RIGHT` Properties Inspector, and `BOTTOM` Studio Console `DockPanel`s with custom `TitleBar`s and `DockHeaderAction`s, `FLOATING` HUD overlay panel, central `TabContainer` with closable `StudioDocument` tabs, `getStyle()` & `getBootstrap()`, and `StudioWindowMode` (`SINGLE_WINDOW_MODE` vs. `MULTI_WINDOW_MODE`) switching. |

---

### Category 3 — 2D Vector Canvas, GPU Shader Filters, & Physics Simulation (`canvas-and-gpu/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **Canvas2D Vector Sketchpad** | [`canvas-and-gpu/canvas2d-vector-sketchpad`](./canvas-and-gpu/canvas2d-vector-sketchpad/README.md) | `@MainAppFragment` (`SketchpadFragment`), `:extensions:canvas2d` (`Canvas2DExtSurface`, `CurveEditorExt`) & `:extensions:dsl`, custom SVG tool cursors (`CssCustomCursor.fromSvg(...)` & `getStyle().setCursor(...)`), `getBootstrap()`, interactive 2D vector drawing (`FREEHAND`, `RECTANGLE`, `ELLIPSE`, `LINE`, `BEZIER_CURVE`), cubic-Bezier stroke taper modulation, `ColorPicker`, `RangeSlider`, undo/redo `SketchVectorCommand` stack, VBlank frame telemetry, and PNG export (`Dialogs.saveFile`). |
| **GPU Image Filter Lab** | [`canvas-and-gpu/gpu-image-filter-lab`](./canvas-and-gpu/gpu-image-filter-lab/README.md) | `@MainAppFragment` (`GpuImageFilterLabFragment`), `:extensions:gpu`, `:extensions:imageprocessing` & `:extensions:canvas2d` (`GpuImageProcessor`, `BrightnessContrastFilter`, `HueSaturationLightnessFilter`, `GaussianBlurFilter`, `VignetteFilter`, `ImageHistogramResult`), `getStyle()` & `getBootstrap()`, one-click presets (`Vivid`, `Noir`, `Warm Cinema`, `Default`), and live 256-bin luminance & RGB histogram rendering. |
| **Box2D Physics Playground** | [`canvas-and-gpu/box2d-physics-playground`](./canvas-and-gpu/box2d-physics-playground/README.md) | `@MainAppFragment` (`Box2DPhysicsPlaygroundFragment`), `:extensions:box2d` & `:extensions:canvas2d` (`World2D`, `Body2D`, `BodyDef2D`, `BodyType2D`, `PolygonShape2D`, `CircleShape2D`, `RevoluteJoint2D`, `Box2DDebugDrawer`), `getStyle()` & `getBootstrap()`, scene presets (`PYRAMID_STACK`, `PENDULUM_CRADLE`, `BOUNCY_CASCADE`), interactive dynamic box/circle spawning, and live gravity control. |

---

### Category 4 — Developer Tools, Code Editors, MDI & Live SCSS, Serialization, & Media (`tools-and-extensions/`)

| Example | Directory | Key Features Demonstrated |
| :--- | :--- | :--- |
| **BSON / JSON / Binary Plist Visual Editor** | [`tools-and-extensions/bson-json-plist-editor`](./tools-and-extensions/bson-json-plist-editor/README.md) | `@MainAppFragment` (`DataEditorFragment`), `@AppApplication` (`DataEditorApp`), purely visual 4-column hierarchical `LazyGridTree` editor supporting all 12 `DataNodeValueType`s (`OBJECT`, `MAP`, `ARRAY`, `STRING`, `BYTE`..`DOUBLE`, `BOOLEAN`, `NULL`), `getStyle()` & `getBootstrap()`, multi-tab `TabContainer`, lossless conversion and live byte-size comparison across JSON (`.json`), `BinaryJson` / BSON (`.bson`), and Apple `BinaryPlist` (`bplist00` `.plist`), and `PreferenceStore`. |
| **Code Editor & Regex Tester** | [`tools-and-extensions/code-editor-and-regex-tester`](./tools-and-extensions/code-editor-and-regex-tester/README.md) | `@MainAppFragment` (`CodeEditorRegexFragment`), `:extensions:codeeditor` (`IdeCodeEditor`, 9 `CodeLanguage` syntax highlighters, 4 `CodeEditorTheme`s, inline `CodeDiagnostic` match highlights), `getStyle()` & `getBootstrap()`, live Regular Expression pattern & flags evaluation (`RegexMatchRecord`), capture-group `Table`, multi-buffer `TabContainer` (`CloseableTab`), and `PreferenceStore` snippet library (`CodeSnippetPreset`). |
| **Audio Spectrum Visualizer** | [`tools-and-extensions/audio-spectrum-visualizer`](./tools-and-extensions/audio-spectrum-visualizer/README.md) | `@MainAppFragment` (`AudioSpectrumVisualizerFragment`), `:extensions:media`, `:extensions:canvas2d` & `:extensions:gpu` (`AudioPlayerExt`, `AudioAnalyzerExt`, `AudioEnergyBands`, `WebVttSrtParser`, `MediaCapabilitiesReport`), `getStyle()` & `getBootstrap()`, 3 real-time `Canvas2DExtSurface` visualizer modes (`FFT_SPECTRUM_BARS`, `OSCILLOSCOPE_WAVEFORM`, `RADIAL_ENERGY_RINGS`), synchronized subtitle cues, and platform codec capability inspector `Table`. |
| **MDI Component & Live SCSS Showcase** | [`tools-and-extensions/mdi-component-and-scss-showcase`](./tools-and-extensions/mdi-component-and-scss-showcase/README.md) | `@MainAppFragment` (`MdiComponentAndScssShowcaseFragment`), `@AppFragment` (`EmbeddedDockWorkspaceSubFragment`), precompiled `.scss` (`styles { compileSass = true; precompileToKotlin = true; globalStyleSheet = "styles.scss" }`), `IApplication.addStyleSheet` / `removeStyleSheet`, `ThemeMode.AUTO` / `DARK` / `LIGHT`, `getStyle()` & `getBootstrap()`, `MdiContainer` & 6 category `MdiWindow`s showcasing every major GenericUI component & `com.nexustools.genericui.component.htmlcompat` tag, embedded `DockWorkspaceFragment`, and `IdeCodeEditor` (`CodeLanguage.SCSS`) + `LiveScssStyleEngine` preloaded with scoped `ScssThemePreset.RAINBOW_PRISM`. |

---

## Running & Testing Examples

Navigate into any example directory and invoke the root Gradle wrapper (`../../../gradlew`):

```bash
cd examples/tools-and-extensions/mdi-component-and-scss-showcase

# Print resolved GenericUI backends, extensions, and style pipeline configuration
../../../gradlew printGenericUiConfig

# Run headless JestElectricRunner UI tests on JVM / Swing and Native Windows
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Run live onscreen Windows visual UI tests via JestElectricRunner
../../../gradlew windowsVisualTest

# Build and launch for specific GenericUI targets
../../../gradlew swingDevelopment
../../../gradlew windowsDevelopment
../../../gradlew macDevelopment
../../../gradlew linuxDevelopmentGtk
../../../gradlew linuxDevelopmentQt
../../../gradlew androidDevelopment
../../../gradlew iosDevelopment
../../../gradlew webDevelopment
```
