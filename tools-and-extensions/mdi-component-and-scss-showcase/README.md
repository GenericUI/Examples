# MDI Component & Live SCSS Stylesheet Showcase (`mdi-component-and-scss-showcase`)

A multiplatform showcase and live theme studio built with `GenericUI`, `:extensions:codeeditor`, `:extensions:dsl`, and `:extensions:canvas2d` demonstrating:

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

## Project Layout (`1 Class Per File`)

```text
examples/tools-and-extensions/mdi-component-and-scss-showcase/
├── README.md
├── settings.gradle.kts
├── gradle.properties
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/mdishowcase/
    │   │   ├── ScssThemePreset.kt                      # 5 SCSS theme presets (RAINBOW_PRISM default)
    │   │   ├── EmbeddedDockWorkspaceSubFragment.kt     # DockWorkspaceFragment hosted in MDI Window #6
    │   │   ├── MdiComponentAndScssShowcaseFragment.kt  # Main 2-tab Fragment (MDI Preview + SCSS Editor)
    │   │   ├── MdiComponentAndScssShowcaseApp.kt       # FragmentApplication + main(args) entry point
    │   │   └── MdiComponentAndScssShowcaseVisualTestMain.kt # Visual test entry point
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/mdishowcase/
            └── MdiComponentAndScssShowcaseJestTest.kt  # JestElectricRunner BDD + UI test suite
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/mdi-component-and-scss-showcase/`:

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
