# Adaptive Media Catalog (`examples/layouts-and-data/adaptive-media-catalog`)

Demonstrates responsive 3-posture layout adaptation in `GenericUI` using `AdaptiveSplitTabFragment`, seamlessly re-parenting the primary media viewport and secondary tab container across **Desktop/Tablet Multi-Column Split** (`TABLET_DESKTOP_COLUMNS`), **Phone Landscape Immersive Fullscreen** (`PHONE_LANDSCAPE_FULLSCREEN`), and **Phone Portrait Single-Flow Tabs** (`PORTRAIT_SINGLE_FLOW_TABS`) without interrupting playback state across all 8 backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `AdaptiveMediaCatalogFragment` (`@MainAppFragment` extending `AdaptiveSplitTabFragment`) and `AdaptiveMediaCatalogApp` (`@AppApplication` extending `FragmentApplication`).
- **Adaptive Helper Fragment**: `AdaptiveSplitTabFragment`, `AdaptiveSplitTabFragment.LayoutMode`, `WindowConfiguration`, `setPrimaryView`, and `addTab`.
- **Navigation & Surfaces**: `TabContainer` (`Channels`, `Queue`, `Details`), `Card`, `Badge`, `Button`, `Heading`, `Label`, `HStack`, and `VStack`.
- **Posture Simulation & Testing**: Live switching between Desktop (`1280x800`), Phone Landscape (`800x390`), and Phone Portrait (`390x844`) postures verified with `JestElectricRunner` (`AdaptiveMediaCatalogJestTest.kt`).

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/layouts-and-data/adaptive-media-catalog/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/mediacatalog/
    │   │   ├── AdaptiveMediaCatalogApp.kt        # @AppApplication FragmentApplication
    │   │   ├── AdaptiveMediaCatalogFragment.kt   # @MainAppFragment AdaptiveSplitTabFragment media catalog
    │   │   └── CatalogChannelItem.kt             # Channel metadata model
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/mediacatalog/
            └── AdaptiveMediaCatalogJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.mediacatalog"
        name = "Adaptive Media Catalog"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Responsive 3-posture AdaptiveSplitTabFragment media catalog for GenericUI."
    }
    window {
        title = "GenericUI — Adaptive Media Catalog"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#7C3AED"
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

### `@MainAppFragment` (`AdaptiveMediaCatalogFragment.kt`)

```kotlin
@MainAppFragment
class AdaptiveMediaCatalogFragment : AdaptiveSplitTabFragment(
    phoneLandscapeFullscreen = true,
    primaryColumnWeight = 1.8f,
    secondaryColumnWeight = 1.0f
) {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        setPrimaryView(buildPrimaryViewportStage())
        addTab("Channels", buildChannelsTabPane())
        addTab("Queue", buildQueueTabPane())
        addTab("Details", buildDetailsTabPane())
        refreshNowPlayingAndTabs()
        syncPostureBadges()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/layouts-and-data/adaptive-media-catalog`, invoke the root Gradle wrapper (`../../../gradlew`):

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
