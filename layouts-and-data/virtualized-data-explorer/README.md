# Virtualized Data Explorer (`LazyList`, `LazyGrid` & `TreeView` — 10,000 Rows)

Demonstrates true viewport window virtualization across a **10,000-record dataset** in `GenericUI` using `LazyList`, `LazyGrid`, and `TreeView` with live `ViewHolderPool` recycling metrics, search and category filtering, and interactive selection telemetry across all 8 backends (`Windows`, `macOS`, `GTK`, `Qt`, `Swing`, `Android`, `iOS`, and `Web` WasmJS/JS + PWA).

## Key `GenericUI` APIs Demonstrated

- **Virtualized Containers**: `LazyList`, `LazyGrid`, `TreeView`, `TreeNode`, `LazyViewportTracker`, `LazyViewportWindow`, `ViewHolderPool`
- **Form & Filter Controls**: `TextField` (`id = "input-filter"`), `Select` (`id = "select-category"`), `SelectOption`, `Button`
- **Live Telemetry Badges**: `Badge` (`id = "badge-total-items"`, `id = "badge-active-window"`, `id = "badge-selected-item"`, `id = "badge-recycle-stats"`)
- **Application & Lifecycle**: `FragmentApplication`, `Fragment`, `JestElectricRunner`

## Project Layout (`1 Class Per File`)

```text
examples/layouts-and-data/virtualized-data-explorer/
├── README.md
├── settings.gradle.kts
├── gradle.properties
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dataexplorer/
    │   │   ├── DatasetRecord.kt
    │   │   ├── ExplorerViewMode.kt
    │   │   ├── VirtualizedDataExplorerFragment.kt
    │   │   ├── VirtualizedDataExplorerApp.kt
    │   │   └── VirtualizedDataExplorerVisualTestMain.kt
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/dataexplorer/
            └── VirtualizedDataExplorerJestTest.kt
```

## Running the Application Across All 8 Backends

From `examples/layouts-and-data/virtualized-data-explorer/`:

```bash
# Desktop — Windows (Win32 / Direct2D / ANGLE / Vulkan)
./gradlew windowsDevelopment

# Desktop — macOS (AppKit / CoreGraphics / MoltenVK)
./gradlew macDevelopment

# Desktop — Linux GTK & Qt
./gradlew linuxDevelopmentGtk
./gradlew linuxDevelopmentQt

# Desktop — JVM / AWT / Swing
./gradlew swingDevelopment

# Mobile — Android & iOS
./gradlew androidDevelopment
./gradlew iosDevelopment

# Web — WasmJS primary + JS fallback + PWA manifest.webmanifest
./gradlew webDevelopment
```

## Running the `JestElectricRunner` Test Suite

```bash
# Headless JVM / Swing test suite
./gradlew jvmTest

# Native Windows test suite
./gradlew mingwX64Test

# Live Visual Win32 Window Test Runner on Windows
./gradlew windowsVisualTest

# Web (JS / WasmJS) & Linux / macOS / Android / iOS tests
./gradlew jsTest wasmJsTest
```
