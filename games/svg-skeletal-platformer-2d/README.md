# Skyline Courier 2D — SVG Skeletal Platformer (`examples/games/svg-skeletal-platformer-2d`)

A 2D side-scrolling platformer built with `:extensions:game:engine2d` (`engine2d()`, which bundles `:extensions:game:engine`, `:extensions:graphics:canvas2d`, `:extensions:graphics:imageprocessing`, `:extensions:physics:box2d`, and `:extensions:game:gamepad`) across **all 9 GenericUI target backends**.

> **Note on SVG & Physics modules:** In GenericUI's consolidated architecture, articulated 2D SVG skeletal rigs (`Skeleton2D`, `Bone2D`, `TwoBoneIkSolver2D`, `SvgPathPrimitive`, `SvgPolygonPrimitive`) live in `:extensions:game:engine`, Canvas2D SVG rendering & Box2D physics bridges live in `:extensions:game:engine2d` + `:extensions:physics:box2d`, and both are automatically enabled by `engine2d()`.

---

## Features & Engine APIs Demonstrated

- **Articulated 2D SVG Skeletal Rig & Procedural Clips (`PlatformerRigFactory`)**:
  - Builds an 11-bone `Skeleton2D` dressed with vector `SvgPathPrimitive` flowing scarf/visor/gauntlet, `SvgPolygonPrimitive` jetpack fin & Quantum Core gems, `SvgRectPrimitive`, and `SvgCirclePrimitive` parts (plus standalone reference [`hero-rig.svg`](./src/commonMain/resources/hero-rig.svg)).
  - Blends `Idle`, `Run`, `Jump`, and `Dash` `SkeletalAnimationClip2D` clips and applies real-time `TwoBoneIkSolver2D.applyToBones(...)` onto `"upperArmR"` / `"forearmR"` driven by Right Stick / six-axis gyro aim.
- **Box2D Rigid-Body Physics & Canvas2D Parallax Stage (`Game2DCanvasHost`)**:
  - `PhysicsWorld2DSubsystem` steps static rooftop colliders (`Collider2DModule.box`) and dynamic hero body (`RigidBody2DModule`) in lockstep with `GameLoopScheduler`'s 60 Hz `FixedStepAccumulator`.
  - Renders 2 multi-depth parallax skyline layers (`Tilemap2DModule` at `zOrder = -30` and `-20`), level platforms (`zOrder = -10`), floating SVG Quantum Cores, jetpack/dash `ParticleEmitter2DModule` bursts, and smooth follow + screen-shake `Camera2DModule`.
- **Unified Multi-Family Gamepad & Pause Navigation (`GamepadUiNavigator`)**:
  - Left Stick / D-Pad runs, `A`/`Cross`/`B` jumps, `RB`/`R1`/`X` fires Jet Dash with rumble/adaptive-trigger/HD-rumble/impulse-trigger feedback, and Pause Menu uses `GamepadUiNavigator`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/games/svg-skeletal-platformer-2d/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/platformer2d/
    │   │   ├── PlatformerAnimationState.kt
    │   │   ├── PlatformerApp.kt
    │   │   ├── PlatformerCollectible.kt
    │   │   ├── PlatformerEngineController.kt
    │   │   ├── PlatformerFragment.kt
    │   │   ├── PlatformerRigFactory.kt
    │   │   └── PlatformerSnapshot.kt
    │   └── resources/
    │       ├── hero-rig.svg
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/platformer2d/
            └── PlatformerJestTest.kt
```

---

## Building & Testing

```bash
../../../gradlew jvmTest
../../../gradlew macosArm64Test

# Run development builds across all 9 backends
../../../gradlew developmentWindows
../../../gradlew developmentMac
../../../gradlew developmentLinuxGtk
../../../gradlew developmentLinuxKde
../../../gradlew developmentSwing
../../../gradlew developmentAndroid
../../../gradlew developmentIos
../../../gradlew developmentIpados
../../../gradlew developmentWeb
```
