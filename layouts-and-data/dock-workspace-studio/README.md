# Dock Workspace Studio (`OverlayDockWorkspaceFragment` & `DockWorkspaceFragment`)

Demonstrates a modular creative studio workspace in `GenericUI` built with `OverlayDockWorkspaceFragment` and `DockWorkspaceFragment`, featuring `LEFT` Toolbox, `RIGHT` Properties, and `BOTTOM` Console `DockPanel`s with custom `TitleBar`s, `CENTER` `TabContainer` with `CloseableTab` document tabs, dynamic switching between `StudioWindowMode.SINGLE_WINDOW_MODE` and `StudioWindowMode.MULTI_WINDOW_MODE` (with `PanelWindow` detachment), and a floating HUD overlay panel across all 8 backends (`Windows`, `macOS`, `GTK`, `Qt`, `Swing`, `Android`, `iOS`, and `Web` WasmJS/JS + PWA).

## Key `GenericUI` APIs Demonstrated

- **Docking & Workspace Fragments**: `OverlayDockWorkspaceFragment`, `DockWorkspaceFragment`, `DockWorkspace`, `DockArea`, `DockPanel`, `PanelWindow`, `DockPosition`, `StudioWindowMode`
- **Panel & Window TitleBars**: `DockPanel.setTitleBar(TitleBar)`, `DockPanel.getTitleBar()`, `DockHeaderAction`
- **Document Tabs**: `TabContainer`, `CloseableTab`, `StudioDocument`

## Project Layout (`1 Class Per File`)

```text
examples/layouts-and-data/dock-workspace-studio/
├── README.md
├── settings.gradle.kts
├── gradle.properties
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dockstudio/
    │   │   ├── StudioDocument.kt
    │   │   ├── DockWorkspaceStudioFragment.kt
    │   │   ├── DockWorkspaceStudioApp.kt
    │   │   └── DockWorkspaceStudioVisualTestMain.kt
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/dockstudio/
            └── DockWorkspaceStudioJestTest.kt
```

## Running the Application Across All 8 Backends

From `examples/layouts-and-data/dock-workspace-studio/`:

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
