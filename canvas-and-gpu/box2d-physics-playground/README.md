# Box2D Physics Playground (`examples/canvas-and-gpu/box2d-physics-playground`)

An interactive 2D rigid-body physics sandbox built with `:extensions:box2d`, `:extensions:canvas2d`, and `:extensions:dsl` across all 8 `GenericUI` backends.

---

## Key `GenericUI` Features & Extensions Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `Box2DPhysicsPlaygroundFragment` (`@MainAppFragment`) and `Box2DPhysicsPlaygroundApp` (`@AppApplication` extending `FragmentApplication`).
- **Rigid-Body Dynamics (`:extensions:box2d` — `World2D`, `Body2D`, `BodyDef2D`, `BodyType2D`, `FixtureDef2D`)**: Simulates static boundaries and dynamic polygon (`PolygonShape2D`) and circle (`CircleShape2D`) bodies with configurable density, friction, and restitution.
- **Revolute Joints (`RevoluteJoint2D`)**: Demonstrates constraints and pendulum motion in the `Pendulum Cradle` preset (`PhysicsScenePreset.PENDULUM_CRADLE`).
- **Canvas2D Debug Visualization (`Box2DDebugDrawer` & `Canvas2DExtSurface`)**: Renders world bodies, fixtures, orientation vectors, and joint anchors onto a `Canvas2DExtSurface` (`canvas2DExt`).
- **Interactive Controls**: Spawn boxes (`btn-spawn-box`) and circles (`btn-spawn-circle`) on demand, switch between `Pyramid Stack`, `Pendulum Cradle`, and `Bouncy Cascade` presets (`PhysicsScenePreset`), adjust vertical gravity via `RangeSlider` (`slider-gravity`), or single-step and pause the solver.
- **`JestElectricRunner` Test Suite**: Automated simulation and UI tests in `Box2DPhysicsPlaygroundJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/canvas-and-gpu/box2d-physics-playground/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/physics2d/
    │   │   ├── Box2DPhysicsPlaygroundApp.kt        # @AppApplication FragmentApplication (TitleBar & Styles)
    │   │   ├── Box2DPhysicsPlaygroundFragment.kt   # @MainAppFragment Box2D World2D & Canvas2D viewport
    │   │   └── PhysicsScenePreset.kt               # Scene preset enum (PYRAMID_STACK, PENDULUM_CRADLE, BOUNCY_CASCADE)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/physics2d/
            └── Box2DPhysicsPlaygroundJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.physics2d"
        name = "Box2D Physics Playground"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Box2D rigid-body physics sandbox with revolute joints, collision impulse solver, and Canvas2D debug drawer."
    }
    window {
        title = "Box2D Rigid-Body Physics Playground"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#10B981"
    }
    backends {
        all()
    }
    extensions {
        box2d()
        canvas2d()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`Box2DPhysicsPlaygroundFragment.kt`)

```kotlin
@MainAppFragment
class Box2DPhysicsPlaygroundFragment : Fragment() {
    var world: World2D = World2D(gravity = Vec2(0f, -9.81f))
        private set

    val debugDrawer: Box2DDebugDrawer = Box2DDebugDrawer(
        pixelsPerMeter = 46f,
        offsetX = 280f,
        offsetY = 190f,
        flipY = true
    )

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("physics-shell")
        buildUi()
        loadScenePreset(PhysicsScenePreset.PYRAMID_STACK)
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/canvas-and-gpu/box2d-physics-playground`, invoke the root Gradle wrapper (`../../../gradlew`):

```bash
# Desktop JVM (AWT/Swing + optional LWJGL)
../../../gradlew swingDevelopment

# Desktop Windows (Win32 / Direct2D / ANGLE / Vulkan)
../../../gradlew windowsDevelopment

# Desktop macOS (AppKit / CoreGraphics / MoltenVK)
../../../gradlew macDevelopment

# Desktop Linux GTK & Qt
../../../gradlew linuxDevelopmentGtk
../../../gradlew linuxDevelopmentQt

# Web (WasmJS primary + JS IR fallback + PWA manifest.webmanifest)
../../../gradlew webDevelopment

# Mobile (Android & iOS)
../../../gradlew androidDevelopment
../../../gradlew iosDevelopment
```

---

## Running the `JestElectricRunner` Test Suite

```bash
# Run headless multiplatform unit + UI tests (JVM, JS/Wasm, Native)
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Run live onscreen Windows visual tests
../../../gradlew windowsVisualTest
```
