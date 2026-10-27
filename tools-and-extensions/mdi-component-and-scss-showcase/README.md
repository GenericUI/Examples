# MDI Component & Live SCSS Stylesheet Showcase (`examples/tools-and-extensions/mdi-component-and-scss-showcase`)

A multiplatform showcase and live theme studio built with `GenericUI`, `:extensions:codeeditor`, `:extensions:dsl`, and `:extensions:canvas2d` across all 8 backends.

---

## Key `GenericUI` Features & Extensions Demonstrated

- **Annotation-Driven Entry Point & Fragment Hierarchy (`@MainAppFragment`, `@AppFragment`, & `@AppApplication`)**:
  - `MdiComponentAndScssShowcaseFragment` (`@MainAppFragment`) hosts the main 2-tab MDI & SCSS studio.
  - `EmbeddedDockWorkspaceSubFragment` (`@AppFragment` extending `DockWorkspaceFragment`) is embedded inside MDI Window #6 and automatically gets its own generated `EmbeddedDockWorkspaceSubFragmentActivity` on Android.
  - `MdiComponentAndScssShowcaseApp` (`@AppApplication` extending `FragmentApplication`) configures the outer `TitleBar`, `MenuBar`, `ThemeMode.AUTO` (`setTheme` / `getTheme` / `getResolvedTheme`), and precompiled stylesheet (`addStyleSheet(StylesPrecompiledStyleSheet)` / `removeStyleSheet(...)`).
- **Precompiled `.scss` Stylesheets, `getStyle()`, & `getBootstrap()`**:
  - Compiles `src/commonMain/resources/styles.scss` at build time into `StylesPrecompiledStyleSheet` via `styles { compileSass = true; precompileToKotlin = true; generateWebCss = true; globalStyleSheet = "styles.scss" }`.
  - Uses `getStyle()` and `getBootstrap()` to manage inline CSS properties and Bootstrap button/badge states.
- **Multi-Document Interface (`MdiContainer` & `MdiWindow`)**: Interactive desktop workspace (`#showcase-mdi-container`) with cascade, tile grid, minimize, maximize, restore, and taskbar strip controls hosting **6 category `MdiWindow`s**:
  1. **HTML Compat & Typography (`#mdi-win-htmlcompat`)**: Semantic HTML5 wrapper components (`Header`, `Footer`, `Main`, `Section`, `Article`, `Aside`, `Div`, `Span`, `H1`..`H6`, `P`, `A`, `Strong`, `B`, `Em`, `I`, `Small`, `Sup`, `Sub`, `Pre`, `Code`, `Blockquote`, `Ul`, `Ol`, `Li`, `Figure`, `Figcaption`, `Details`, `Summary`, `Hr`, `Br`).
  2. **Actions, Buttons & Navigation (`#mdi-win-actions-nav`)**: Standard Bootstrap `<button>` (`Button.htmlTag == "button"`), `IconButton`, `ToggleButton`, `DropdownButton`, `SplitDropdownButton`, `FloatingActionButton`, `ButtonGroup`, `Toolbar`, `MenuBar` (`genericui:menubar`), `Navbar`, `Nav`, `Breadcrumb`, `Pagination`, `Stepper`, and `BottomNavigationBar`.
  3. **Forms, Inputs & Validation (`#mdi-win-forms`)**: `Form`, `Fieldset`, `FormGroup`, `InputGroup`, `TextField`, `PasswordField`, `NumberField`, `SearchField`, `TextArea`, `Select`, `ComboBox`, `CheckBox`, `RadioButton`, `RadioGroup`, `Switch`, `RangeSlider`, `ColorPicker`, `DatePicker`, `TimePicker`, `CompoundUnitInput`, and `FileDropZone`.
  4. **Surfaces, Cards, Tables & Feedback (`#mdi-win-surfaces-feedback`)**: `Card` (`genericui:card`), `CollapsiblePanel`, `Accordion`, `AccordionItem`, `Jumbotron`, `Well`, `ListGroup`, `Table`, `Thead`/`Tbody`/`Tfoot`/`Tr`/`Th`/`Td`, `Alert`, `Banner`, `Toast`, `ProgressBar`, `Spinner`, `SkeletonPlaceholder`, `Popover`, and `Tooltip`.
  5. **Virtualized Data & Layouts (`#mdi-win-lazy-layouts`)**: `Row` (`genericui:row`), `Col` (`genericui:col`), `FlexBox`, `SplitPane`, `LazyList` (`genericui:lazylist`), `LazyGrid` (`genericui:lazygrid`), and `TreeView` (`genericui:treeview`).
  6. **Dock Workspace Studio Fragment (`#mdi-win-dock-workspace`)**: Hosts a live `EmbeddedDockWorkspaceSubFragment` (`DockWorkspaceFragment`) with `LEFT`, `RIGHT`, and `BOTTOM` `DockPanel`s, custom `TitleBar`s, `ToolPalette`, `ColorSwatchBox`, `CategorizedTreeView`, central `CloseableTab`s, and `SINGLE_WINDOW_MODE` / `MULTI_WINDOW_MODE` toggling inside an MDI window.
- **Scoped Live Rainbow SCSS Stylesheet Editor (`IdeCodeEditor` + `LiveScssStyleEngine`)**:
  - Preloaded with **`ScssThemePreset.RAINBOW_PRISM`**, styling all components inside `genericui:mdicontainer` with full-spectrum rainbow gradients and colors.
  - Strictly bound to `mdiContainer` (`liveScssEngine.bindRootComponent(mdiContainer)`), ensuring the outer `IdeCodeEditor`, top-level `MenuBar`, and outer shell tabs are never affected by the MDI stylesheet.
  - Includes SCSS syntax highlighting (`$variables`, `@mixin`, `@include`, `genericui:*` tags, hex colors, CSS units, and pseudo-classes), context-aware autocomplete (`ScssLanguageSupport`), and live syntax error squiggles (`CodeDiagnostic`).

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/tools-and-extensions/mdi-component-and-scss-showcase/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/mdishowcase/
    │   │   ├── EmbeddedDockWorkspaceSubFragment.kt     # @AppFragment DockWorkspaceFragment hosted in MDI Window #6
    │   │   ├── MdiComponentAndScssShowcaseApp.kt       # @AppApplication FragmentApplication
    │   │   ├── MdiComponentAndScssShowcaseFragment.kt  # @MainAppFragment main 2-tab Fragment (MDI Preview + SCSS Editor)
    │   │   └── ScssThemePreset.kt                      # 5 SCSS theme presets (RAINBOW_PRISM default)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/mdishowcase/
            └── MdiComponentAndScssShowcaseJestTest.kt  # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.mdishowcase"
        name = "MDI Component & Live SCSS Showcase"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Interactive Multi-Document Interface (MDI) component gallery and live SCSS stylesheet editor powered by GenericUI across all 8 backends."
    }
    window {
        title = "GenericUI — MDI Component & Live SCSS Stylesheet Showcase"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#FF2A6D"
    }
    backends {
        all()
    }
    extensions {
        dsl()
        codeEditor()
        canvas2d()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` & `@AppFragment`

```kotlin
@MainAppFragment
class MdiComponentAndScssShowcaseFragment : Fragment() {
    val liveScssEngine: LiveScssStyleEngine = LiveScssStyleEngine(ScssThemePreset.RAINBOW_PRISM.scssSource)
    val dockSubFragment: EmbeddedDockWorkspaceSubFragment = EmbeddedDockWorkspaceSubFragment()

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        liveScssEngine.reapplyToBoundComponents()
    }
}

@AppFragment
class EmbeddedDockWorkspaceSubFragment : DockWorkspaceFragment(
    initialMode = StudioWindowMode.SINGLE_WINDOW_MODE
)
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/mdi-component-and-scss-showcase`, invoke the root Gradle wrapper (`../../../gradlew`):

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
