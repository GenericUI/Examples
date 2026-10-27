# Virtualized Data Explorer (`examples/layouts-and-data/virtualized-data-explorer`)

Demonstrates true viewport window virtualization across a **10,000-record dataset** in `GenericUI` using `LazyList`, `LazyGrid`, and `TreeView` with live `ViewHolderPool` recycling metrics, search and category filtering, and interactive selection telemetry across all 8 backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `VirtualizedDataExplorerFragment` (`@MainAppFragment`) and `VirtualizedDataExplorerApp` (`@AppApplication` extending `FragmentApplication`) with `ThemeMode.AUTO` (`setTheme(ThemeMode.AUTO)`) and `StylesPrecompiledStyleSheet` (`addStyleSheet(...)`).
- **`ComponentBootstrap` (`bootstrap { ... }`) & `ComponentStyle` (`style { ... }`)**: Uses `:extensions:dsl` `bootstrap { ... }` and `style { ... }` builders (`BootstrapType`, `BootstrapSize`, `BorderRadiusStyle`, `ShadowElevation`, `SpacingScale`, `FontWeight`, `accentColor`) with strict custom CSS class isolation.
- **Virtualized Containers**: `LazyList`, `LazyGrid`, `TreeView`, `TreeNode`, `LazyViewportWindow` (`[firstVisibleIndex .. lastVisibleIndex]`), and `ViewHolderPool` (`onLoadItem` / `onUnloadItem`).
- **Form & Filter Controls**: `TextField` (`id = "input-filter"`), `Select` (`id = "select-category"`), `SelectOption`, and `Button` (`btn-mode-list`, `btn-mode-grid`, `btn-mode-tree`, `btn-scroll-top`, `btn-scroll-mid`, `btn-scroll-bottom`).
- **Live Telemetry Badges**: `Badge` (`id = "badge-total-items"`, `id = "badge-active-window"`, `id = "badge-selected-item"`, `id = "badge-recycle-stats"`).
- **`JestElectricRunner` Test Suite**: Full 10,000-row virtualization, scrolling, filtering, and mode-switching tests in `VirtualizedDataExplorerJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/layouts-and-data/virtualized-data-explorer/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dataexplorer/
    │   │   ├── DatasetRecord.kt                     # Immutable dataset record model
    │   │   ├── ExplorerViewMode.kt                  # Viewport mode enum (LIST, GRID, TREE)
    │   │   ├── VirtualizedDataExplorerApp.kt        # @AppApplication FragmentApplication
    │   │   └── VirtualizedDataExplorerFragment.kt   # @MainAppFragment 10,000-row LazyList/LazyGrid/TreeView UI
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/dataexplorer/
            └── VirtualizedDataExplorerJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.dataexplorer"
        name = "Virtualized Data Explorer"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "10,000-row LazyList, LazyGrid, and TreeView virtualized dataset explorer for GenericUI."
    }
    window {
        title = "GenericUI — Virtualized Data Explorer (10,000 Rows)"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        mode = "auto"
        color = "#2563EB"
    }
    backends {
        all()
    }
    extensions {
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`VirtualizedDataExplorerFragment.kt`)

```kotlin
@MainAppFragment
class VirtualizedDataExplorerFragment(
    val totalDatasetSize: Int = DEFAULT_RECORD_COUNT
) : Fragment() {
    val allRecords: List<DatasetRecord> = generateDataset(totalDatasetSize)

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("explorer-root")
        // Builds toolbar, telemetry badges, and LazyList / LazyGrid / TreeView viewport
        rebuildVirtualizedDataViews()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/layouts-and-data/virtualized-data-explorer`, invoke the root Gradle wrapper (`../../../gradlew`):

```bash
# Desktop — Windows (Win32 / Direct2D / ANGLE / Vulkan)
../../../gradlew windowsDevelopment

# Desktop — macOS (AppKit / CoreGraphics / MoltenVK)
../../../gradlew macDevelopment

# Desktop — Linux GTK & Qt
../../../gradlew linuxDevelopmentGtk
../../../gradlew linuxDevelopmentQt

# Desktop — JVM / AWT / Swing
../../../gradlew swingDevelopment

# Mobile — Android & iOS
../../../gradlew androidDevelopment
../../../gradlew iosDevelopment

# Web — WasmJS primary + JS fallback + PWA manifest.webmanifest
../../../gradlew webDevelopment
```

---

## Running the `JestElectricRunner` Test Suite

```bash
# Headless JVM / Swing test suite
../../../gradlew jvmTest

# Native Windows test suite
../../../gradlew mingwX64Test

# Live Visual Win32 Window Test Runner on Windows
../../../gradlew windowsVisualTest

# Web (JS / WasmJS) tests
../../../gradlew jsTest wasmJsTest
```
