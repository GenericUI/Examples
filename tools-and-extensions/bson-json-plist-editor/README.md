# BSON / JSON / Binary Plist Visual Editor (`examples/tools-and-extensions/bson-json-plist-editor`)

A purely **visual, multi-tab structured document editor** for **JSON (`.json`)**, **Binary JSON / BSON (`.bson`, `.bjson`)**, and **Apple Binary Property List (`.plist` `bplist00`)** files, powered by **`LazyGridTree`** across **all 8 GenericUI target backends** (`windows`, `macos`, `gtk`, `qt`, `swing`, `android`, `ios`, and `web`).

---

## Key `GenericUI` Features Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `DataEditorFragment` (`@MainAppFragment`) and `DataEditorApp` (`@AppApplication` extending `FragmentApplication` with `TitleBar`, `MenuBar`, and `StylesPrecompiledStyleSheet`).
- **Purely Visual `LazyGridTree` Editor (No Text-Based Editor)**:
  - **4-Column Hierarchical `LazyGridTree` (`Key / Hierarchy`, `Value`, `Type (12 DataTypes)`, `Node Actions`)**:
    - **Leaf Nodes**: Each leaf row provides an inline editable `Key` `TextField` (for object/map entries; `[index]` badge for array items), an inline editable `Value` control (`TextField` for strings/numbers, `CheckBox` switch for `BOOLEAN`, `"null"` badge for `NULL`), a `Type` `Select` dropdown supporting all 12 `DataNodeValueType`s, and inline `↑` / `↓` buttons to move the leaf up or down within its parent container, plus `Dup` and `Del` buttons.
    - **Branch Nodes (`OBJECT`, `MAP`, `ARRAY`)**: Each branch row provides an expand/collapse toggle (`▼` / `▶`), an inline editable `Key` `TextField` (for non-root branches), a child count summary (`{ N fields }` or `[ N items ]`), a `Type` `Select` dropdown, and inline `+ Leaf`, `+ Branch`, `↑`, `↓`, `Dup`, and `Del` buttons to create child leaves or nested branches, reorder branches, duplicate subtrees, or delete branches visually.
  - **Supports All 12 DataTypes**:
    - `OBJECT`, `MAP`, `ARRAY`, `STRING`, `BYTE`, `SHORT`, `INT`, `LONG`, `FLOAT`, `DOUBLE`, `BOOLEAN`, and `NULL`.
- **Multi-Tab Document Workspace (`TabContainer` + `CloseableTab`)**:
  - Open, edit, switch between, and close multiple `.json`, `.bson`, and `.plist` documents simultaneously, with per-tab format badges (`[JSON]`, `[BSON]`, `[PLIST]`) and dirty indicators (`*`).
- **3-Format Lossless Conversion & Binary Size Comparison**:
  - Convert any open tab between **UTF-8 JSON (`.json`)**, **`BinaryJson` / BSON (`.bson`)**, and **Apple `BinaryPlist` (`bplist00` `.plist`)** with live `ProgressBar` byte-size comparisons across all three encodings.
- **Cross-Platform Persistence (`PreferenceStore`)**:
  - Persists user settings and recent file history via `PreferenceStore`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/tools-and-extensions/bson-json-plist-editor/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dataeditor/
    │   │   ├── DataDocumentCodec.kt          # JSON, BinaryJson (.bson), and BinaryPlist (bplist00) codec
    │   │   ├── DataDocumentTabState.kt       # Single document tab state & LazyGridTree node mutation controller
    │   │   ├── DataEditorApp.kt              # @AppApplication FragmentApplication, TitleBar, and MenuBar
    │   │   ├── DataEditorFileIoHelper.kt     # Cross-platform Dialogs.openFile / saveFile helper
    │   │   ├── DataEditorFragment.kt         # @MainAppFragment visual multi-tab LazyGridTree editor Fragment
    │   │   ├── DataEditorPreferences.kt      # PreferenceStore-backed settings & recent files model
    │   │   ├── DataEditorSamplePresets.kt    # Built-in sample documents (User Profile, Manifest, Game Save)
    │   │   ├── DataFormat.kt                 # Enum: JSON, BINARY_JSON_BSON, BINARY_PLIST
    │   │   ├── DataNodeValueType.kt          # Enum: OBJECT, MAP, ARRAY, STRING, BYTE..DOUBLE, BOOLEAN, NULL
    │   │   └── EditableDataNode.kt           # Mutable tree node AST model with sibling reordering & editing
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/dataeditor/
            ├── DataEditorExampleTest.kt      # Multi-tab LazyGridTree visual editing & 12-datatype unit tests
            └── DataEditorJestTest.kt         # JestElectricRunner LazyGridTree visual suite
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
        id = "com.nexustools.genericui.examples.dataeditor"
        name = "BSON JSON Plist Editor"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Visual multi-tab JSON, Binary JSON (BSON), and Binary Plist (bplist00) editor powered by GenericUI across all 8 backends."
    }
    window {
        title = "BSON / JSON / Binary Plist Visual Editor"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#1E293B"
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

### `@MainAppFragment` (`DataEditorFragment.kt`)

```kotlin
@MainAppFragment
class DataEditorFragment(
    val preferenceStore: PreferenceStore = PreferenceStore.open("BsonJsonPlistEditor")
) : Fragment() {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        if (openTabs.isEmpty()) {
            ensureInitialDefaultTab()
        }
        refreshActiveTabWorkspace()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/bson-json-plist-editor`, invoke the root Gradle wrapper (`../../../gradlew`):

| Target / Task | Command |
| :--- | :--- |
| **Inspect Configuration** | `../../../gradlew printGenericUiConfig` |
| **Run Unit Tests (JVM/Swing)** | `../../../gradlew jvmTest` |
| **Run Unit Tests (Windows Native)** | `../../../gradlew mingwX64Test` |
| **Run Visual Tests (Windows)** | `../../../gradlew windowsVisualTest` |
| **Swing Desktop (`:desktop:swing`)** | `../../../gradlew swingDevelopment` / `../../../gradlew swingProduction` |
| **Windows Native (`:desktop:windows`)** | `../../../gradlew windowsDevelopment` / `../../../gradlew windowsProduction` |
| **macOS Native (`:desktop:macos`)** | `../../../gradlew macDevelopment` / `../../../gradlew macProduction` |
| **Linux GTK (`:desktop:gtk`)** | `../../../gradlew linuxDevelopmentGtk` / `../../../gradlew linuxProductionGtk` |
| **Linux Qt (`:desktop:qt`)** | `../../../gradlew linuxDevelopmentQt` / `../../../gradlew linuxProductionQt` |
| **Android (`:mobile:android`)** | `../../../gradlew androidDevelopment` / `../../../gradlew androidProduction` |
| **iOS (`:mobile:ios`)** | `../../../gradlew iosDevelopment` / `../../../gradlew iosProduction` |
| **Web PWA (`:web` Wasm + JS)** | `../../../gradlew webDevelopment` / `../../../gradlew webProduction` |
