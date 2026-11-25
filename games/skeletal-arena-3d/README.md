# Vanguard Arena 3D — Skeletal Combat & Box3D (`examples/games/skeletal-arena-3d`)

A 3D third-person / orbital arena action game built with `:extensions:game:engine3d` (`engine3d()`, automatically enabling `:extensions:game:engine`, `:extensions:graphics:canvas3d`, `:extensions:graphics:threejs`, `:extensions:physics:box3d`, and `:extensions:game:gamepad`) across **all 9 GenericUI target backends**.

---

## Features & Engine APIs Demonstrated

- **15-Bone Rigged 3D Humanoid + Procedural Bone Overrides (`ProceduralHumanoidRig3D` & `SkeletalModel3DModule`)**:
  - Spawns hero and patrolling enemy Sentinel rigs with `walk`, `run`, `attack` Three.js `AnimationClip` tracks, plus additive `animateBoneLambda("chest", ...)` breathing and live VBlank pitch/yaw overrides on `"upperArmR"`, `"forearmR"`, and `"head"`.
- **Box3D Physics, `CharacterController3DModule`, Projectiles & Raycast Sightline**:
  - Drives arena locomotion with kinematic `CharacterController3DModule`, collides dynamic `SphereGeometry` plasma bolts against static pillars/walls and patrolling enemies, and evaluates real-time sightline intersections via `World3D.raycastClosest(Ray3D(...))`.
- **Dual Camera System & Dynamic Lighting**:
  - Toggle seamlessly between `FollowCamera3DModule` (third-person chase cam) and `OrbitCamera3DModule` (tactical orbit cam), lit by `AmbientLight`, `DirectionalLight3DModule`, and twin colored `PointLight3DModule` rim lights.
- **Dual-Stick, Six-Axis Gyro Aim, Adaptive Triggers, HD Rumble & Pause UI**:
  - Combines Right Stick + 6-axis gyro aim, DualSense `VibrationPulse`/`ContinuousResistance` adaptive triggers & lightbar RGB, Switch Pro `SwitchHdRumbleCommand` & pulsing Home LED, Xbox `XboxImpulseRumbleCommand`, and `GamepadUiNavigator` pause modal navigation.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/games/skeletal-arena-3d/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/arena3d/
    │   │   ├── ArenaApp.kt
    │   │   ├── ArenaCameraMode.kt
    │   │   ├── ArenaEnemyUnit.kt
    │   │   ├── ArenaEngineController.kt
    │   │   ├── ArenaFragment.kt
    │   │   ├── ArenaProjectile.kt
    │   │   └── ArenaStateSnapshot.kt
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/arena3d/
            └── ArenaJestTest.kt
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
