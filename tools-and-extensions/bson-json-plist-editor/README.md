# BSON / JSON / Binary Plist Visual Editor (`bson-json-plist-editor`)

A purely **visual, multi-tab structured document editor** for **JSON (`.json`)**, **Binary JSON / BSON (`.bson`, `.bjson`)**, and **Apple Binary Property List (`.plist` `bplist00`)** files, powered by **`LazyGridTree`** across **all 8 GenericUI target backends** (`windows`, `macos`, `gtk`, `qt`, `swing`, `android`, `ios`, and `web`).

---

## Key Features

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
- **Programmatic JSON String Loading & Exporting**:
  - While the UI is strictly a visual `LazyGridTree` editor, `DataDocumentTabState` and `DataEditorFragment` provide programmatic APIs (`loadFromJsonString`, `toJsonString`, `loadJsonStringIntoNewTab`, `loadJsonStringIntoActiveTab`, `provideActiveTabJsonString`) for loading from or exporting to UTF-8 JSON strings.
- **Cross-Platform Persistence (`PreferenceStore`)**:
  - Persists user settings and recent file history via `PreferenceStore` (`.bson` on Windows/Linux, `bplist00` `.plist` on macOS/iOS, `SharedPreferences` on Android, and `localStorage` on Web).

---

## All 8 GenericUI Backends (`backends { all() }`)

In `build.gradle.kts`, `backends { all() }` enables all 8 GenericUI target platforms from a single shared codebase:

```kotlin
genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.dataeditor.main"
        baseName = "BsonJsonPlistEditor"
        androidNamespace = "com.nexustools.genericui.examples.dataeditor"
        themeColor = "#1E293B"
        desktop {
            appName = "BSON JSON Plist Editor"
            windowTitle = "BSON / JSON / Binary Plist Visual Editor"
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "BSON / JSON / Binary Plist Visual Editor"
            shortName = "DataEditor"
            description = "Visual multi-tab JSON, Binary JSON (BSON), and Binary Plist (bplist00) editor powered by GenericUI across all 8 backends."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#1E293B"
            categories.addAll(listOf("developer", "utilities", "productivity"))
        }
    }
    backends {
        all()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        kotlinPackageName = "com.nexustools.genericui.examples.dataeditor.styles"
    }
}
```

---

## Project Layout (1 Class Per `.kt` File)

```text
examples/tools-and-extensions/bson-json-plist-editor/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dataeditor/
    │   │   ├── DataDocumentCodec.kt          # JSON, BinaryJson (.bson), and BinaryPlist (bplist00) codec
    │   │   ├── DataDocumentTabState.kt       # Single document tab state & LazyGridTree node mutation controller
    │   │   ├── DataEditorApp.kt              # FragmentApplication entry point, TitleBar, and MenuBar
    │   │   ├── DataEditorFileIoHelper.kt     # Cross-platform Dialogs.openFile / saveFile helper
    │   │   ├── DataEditorFragment.kt         # Visual multi-tab LazyGridTree editor Fragment
    │   │   ├── DataEditorPlatformBootstrap.kt# expect object for platform NativeFactory initialization
    │   │   ├── DataEditorPreferences.kt      # PreferenceStore-backed settings & recent files model
    │   │   ├── DataEditorSamplePresets.kt    # Built-in sample documents (User Profile, Manifest, Game Save)
    │   │   ├── DataFormat.kt                 # Enum: JSON, BINARY_JSON_BSON, BINARY_PLIST
    │   │   ├── DataNodeValueType.kt          # Enum: OBJECT, MAP, ARRAY, STRING, BYTE..DOUBLE, BOOLEAN, NULL
    │   │   └── EditableDataNode.kt           # Mutable tree node AST model with sibling reordering & editing
    │   └── resources/
    │       ├── icon.svg                      # Vector application & Web PWA icon
    │       └── styles.scss                   # SASS stylesheet compiled to CSS and Kotlin PrecompiledStyleSheet
    ├── windowsMain/kotlin/.../DataEditorPlatformBootstrap.kt
    ├── macMain/kotlin/.../DataEditorPlatformBootstrap.kt
    ├── linuxMain/kotlin/.../DataEditorPlatformBootstrap.kt
    ├── swingMain/kotlin/.../DataEditorPlatformBootstrap.kt
    ├── androidMain/kotlin/.../DataEditorPlatformBootstrap.kt
    ├── iosMain/kotlin/.../DataEditorPlatformBootstrap.kt
    ├── webMain/kotlin/.../DataEditorPlatformBootstrap.kt
    └── commonTest/kotlin/com/nexustools/genericui/examples/dataeditor/
        ├── DataEditorExampleTest.kt          # Multi-tab LazyGridTree visual editing & 12-datatype unit tests
        └── DataEditorJestTest.kt             # Jest-Electric LazyGridTree visual suite
```

---

## Gradle Build & Test Commands

Run commands from `examples/tools-and-extensions/bson-json-plist-editor` using `../../../gradlew`:

| Target / Task | Command |
| :--- | :--- |
| **Inspect Configuration** | `../../../gradlew printGenericUiConfig` |
| **Run Unit Tests (JVM/Swing)** | `../../../gradlew jvmTest` |
| **Run Unit Tests (Windows Native)** | `../../../gradlew mingwX64Test` |
| **Swing Desktop (`:desktop:swing`)** | `../../../gradlew swingDevelopment` / `../../../gradlew swingProduction` |
| **Windows Native (`:desktop:windows`)** | `../../../gradlew windowsDevelopment` / `../../../gradlew windowsProduction` |
| **macOS Native (`:desktop:macos`)** | `../../../gradlew macDevelopment` / `../../../gradlew macProduction` |
| **Linux GTK (`:desktop:gtk`)** | `../../../gradlew linuxDevelopmentGtk` / `../../../gradlew linuxProductionGtk` |
| **Linux Qt (`:desktop:qt`)** | `../../../gradlew linuxDevelopmentQt` / `../../../gradlew linuxProductionQt` |
| **Android (`:mobile:android`)** | `../../../gradlew androidDevelopment` / `../../../gradlew androidProduction` |
| **iOS (`:mobile:ios`)** | `../../../gradlew iosDevelopment` / `../../../gradlew iosProduction` |
| **Web PWA (`:web` Wasm + JS)** | `../../../gradlew webDevelopment` / `../../../gradlew webProduction` |
