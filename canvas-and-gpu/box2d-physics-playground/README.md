# Box2D Physics Playground (`box2d-physics-playground`)

An interactive 2D rigid-body physics sandbox built with `:extensions:box2d` and `:extensions:canvas2d` across all 8 `GenericUI` backends.

## Key `GenericUI` Features & Extensions Demonstrated
- **Rigid-Body Dynamics (`:extensions:box2d` — `World2D`, `Body2D`, `FixtureDef2D`)**: Simulates static boundaries and dynamic polygon (`PolygonShape2D`) and circle (`CircleShape2D`) bodies with configurable density, friction, and restitution.
- **Revolute Joints (`RevoluteJoint2D`)**: Demonstrates constraints and pendulum motion in the `Pendulum Cradle` preset.
- **Canvas2D Debug Visualization (`Box2DDebugDrawer`)**: Renders world bodies, fixtures, orientation vectors, and joint anchors onto a `Canvas2DExtSurface` synchronized with `registerVBlankRender`.
- **Interactive Controls**: Spawn boxes and circles on demand, switch between `Pyramid Stack`, `Pendulum Cradle`, and `Bouncy Cascade` presets, adjust vertical gravity via `RangeSlider`, or single-step and pause the solver.
- **1-Class-Per-File Modular Architecture**:
  - `PhysicsScenePreset.kt` — `PhysicsScenePreset` enum (`PYRAMID_STACK`, `PENDULUM_CRADLE`, `BOUNCY_CASCADE`)
  - `Box2DPhysicsPlaygroundFragment.kt` — `Box2DPhysicsPlaygroundFragment` Box2D world & debug viewport controller
  - `Box2DPhysicsPlaygroundApp.kt` — `Box2DPhysicsPlaygroundApp` (`FragmentApplication`) and `main(args)` entry point
  - `Box2DPhysicsPlaygroundVisualTestMain.kt` — `Box2DPhysicsPlaygroundVisualTestMain` visual test entry point
  - `Box2DPhysicsPlaygroundJestTest.kt` — `Box2DPhysicsPlaygroundJestTest` `JestElectricRunner` automated test suite

## Running the Application Across All 8 Backends

From `examples/canvas-and-gpu/box2d-physics-playground`:

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

## Running the `JestElectricRunner` Test Suite

```bash
# Run headless multiplatform unit + UI tests (JVM, JS/Wasm, Native)
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Run live onscreen Windows visual tests
../../../gradlew windowsVisualTest
```
