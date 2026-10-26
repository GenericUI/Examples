# Dock Workspace Studio (`examples/layouts-and-data/dock-workspace-studio`)

Demonstrates a modular creative studio workspace in `GenericUI` built with `OverlayDockWorkspaceFragment` and `DockWorkspaceFragment`, featuring `LEFT` Toolbox, `RIGHT` Properties, and `BOTTOM` Console `DockPanel`s with custom `TitleBar`s, `CENTER` `TabContainer` with `CloseableTab` document tabs, dynamic switching between `StudioWindowMode.SINGLE_WINDOW_MODE` and `StudioWindowMode.MULTI_WINDOW_MODE` (with `PanelWindow` detachment), and a floating HUD overlay panel across all 8 backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `DockWorkspaceStudioFragment` (`@MainAppFragment` extending `OverlayDockWorkspaceFragment`) and `DockWorkspaceStudioApp` (`@AppApplication` extending `FragmentApplication`).
- **Docking & Workspace Fragments**: `OverlayDockWorkspaceFragment`, `DockWorkspaceFragment`, `DockWorkspace`, `DockArea`, `DockPanel`, `PanelWindow`, `DockPosition`, and `StudioWindowMode` (`SINGLE_WINDOW_MODE` vs. `MULTI_WINDOW_MODE`).
- **Panel & Window TitleBars**: `DockPanel.setTitleBar(TitleBar)`, `TitleBar.addTrailing(...)`, and `DockHeaderAction`.
- **Document Tabs**: `TabContainer`, `CloseableTab`, `StudioDocument`, `openDocument`, `closeDocument`, and `activateDocument`.
- **`JestElectricRunner` Test Suite**: Full studio workspace, document tab lifecycle, window mode, and HUD overlay tests in `DockWorkspaceStudioJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/layouts-and-data/dock-workspace-studio/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dockstudio/
    │   │   ├── DockWorkspaceStudioApp.kt        # @AppApplication FragmentApplication
    │   │   ├── DockWorkspaceStudioFragment.kt   # @MainAppFragment OverlayDockWorkspaceFragment studio
    │   │   └── StudioDocument.kt                # Studio document tab state model
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/dockstudio/
            └── DockWorkspaceStudioJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.dockstudio"
        name = "Dock Workspace Studio"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "GIMP-style Single/Multi-Window DockWorkspace and Overlay HUD Studio for GenericUI."
    }
    window {
        title = "GenericUI — Dock Workspace Studio"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#0EA5E9"
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

### `@MainAppFragment` (`DockWorkspaceStudioFragment.kt`)

```kotlin
@MainAppFragment
class DockWorkspaceStudioFragment : OverlayDockWorkspaceFragment(
    backgroundComponent = Container("studio-canvas-backdrop"),
    initialMode = StudioWindowMode.SINGLE_WINDOW_MODE
) {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addDockPanel(toolboxPanel, DockPosition.LEFT)
        addDockPanel(propertiesPanel, DockPosition.RIGHT)
        addDockPanel(consolePanel, DockPosition.BOTTOM)
        registerOverlayPanel(hudOverlayPanel, DockPosition.FLOATING, xOffsetPx = 280, yOffsetPx = 64)
        // Opens initial StudioDocument tabs and wires Single/Multi-Window mode switching
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/layouts-and-data/dock-workspace-studio`, invoke the root Gradle wrapper (`../../../gradlew`):

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
