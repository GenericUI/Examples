# Adaptive Media Catalog (`AdaptiveSplitTabFragment`)

Demonstrates responsive 3-posture layout adaptation in `GenericUI` using `AdaptiveSplitTabFragment`, seamlessly re-parenting the primary media viewport and secondary tab container across **Desktop/Tablet Multi-Column Split**, **Phone Landscape Immersive Fullscreen**, and **Phone Portrait Single-Flow Tabs** without interrupting playback state across all 8 backends (`Windows`, `macOS`, `GTK`, `Qt`, `Swing`, `Android`, `iOS`, and `Web` WasmJS/JS + PWA).

## Key `GenericUI` APIs Demonstrated

- **Adaptive Helper Fragment**: `AdaptiveSplitTabFragment`, `AdaptiveSplitTabFragment.LayoutMode`, `WindowConfiguration`
- **Navigation & Surfaces**: `TabContainer`, `Card`, `Badge`, `Button`, `HStack`, `VStack`
- **Posture Simulation & Testing**: Live switching between Desktop (`1280x800`), Phone Landscape (`800x390`), and Phone Portrait (`390x844`) postures verified with `JestElectricRunner`

## Project Layout (`1 Class Per File`)

```text
examples/layouts-and-data/adaptive-media-catalog/
├── README.md
├── settings.gradle.kts
├── gradle.properties
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/mediacatalog/
    │   │   ├── CatalogChannelItem.kt
    │   │   ├── AdaptiveMediaCatalogFragment.kt
    │   │   ├── AdaptiveMediaCatalogApp.kt
    │   │   └── AdaptiveMediaCatalogVisualTestMain.kt
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/mediacatalog/
            └── AdaptiveMediaCatalogJestTest.kt
```

## Running the Application Across All 8 Backends

From `examples/layouts-and-data/adaptive-media-catalog/`:

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
