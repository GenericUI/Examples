# Abyssal Crypt — UI Dungeon Crawler (`examples/games/ui-dungeon-crawler`)

A complete turn-based roguelike dungeon crawler built directly with **GenericUI** components, `:extensions:game:engine`, and `:extensions:game:gamepad` across **all 9 GenericUI target backends** (`windows`, `macos`, `gtk`, `qt`, `swing`, `android`, `ios`, `ipados`, and `web`).

---

## Key `GenericUI`, Engine & Gamepad APIs Demonstrated

- **`GameScene`, `UiStageHost` & `GameLoopScheduler` (`:extensions:game:engine`)**:
  - Hosts the UI-stage dungeon simulation, registers unified `gamepadBindings { ... }` (`move`, `wait_turn`, `use_potion`, `toggle_pause`, `toggle_gyro`, `cycle_profile`), and drives background heraldry `Skeleton2D` (`HumanoidSkeleton2DFactory`) animations over VBlank.
- **Unified Multi-Family Controller Support (`:extensions:game:gamepad`)**:
  - Dynamically renders hardware-accurate prompts via `GamepadFrameSnapshot.resolveButtonLabel(...)` across **Xbox** (`A/B/X/Y/LB/RB`), **PlayStation DualSense** (`Cross/Circle/Square/Triangle/L1/R1`), and **Nintendo Switch Pro** (positional `B/A/Y/X/L/R`).
  - Fires tailored controller feedback on strikes, traps, healing, and floor descent: `setRumble`, DualSense `ContinuousResistance` / `SectionResistance` adaptive triggers & health-reactive lightbar RGB, Switch Pro `SwitchHdRumbleCommand` + player/home LED patterns, and Xbox `XboxImpulseRumbleCommand`.
- **Zero-Config & Custom `GamepadUiNavigator` (`DefaultSpatialGamepadUiNavigationStrategy` & `DungeonFocusNavigationStrategy`)**:
  - Toggle live between GenericUI's built-in spatial `DefaultSpatialGamepadUiNavigationStrategy` and the custom reimplemented `DungeonFocusNavigationStrategy` (adding `.dungeon-gamepad-focus` CSS ring styling, ring-wrap fallback, shoulder-button zone jumping, `X`-to-inspect, and pause cancel handling).
- **Six-Axis Gyro Aim Cursor Toggle**:
  - Enables six-axis `motion.gyroRadPerSecY/X` + Right Stick steering of an inspection cursor across the `11×9` crypt grid.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/games/ui-dungeon-crawler/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/dungeoncrawler/
    │   │   ├── DungeonActor.kt
    │   │   ├── DungeonCrawlerApp.kt
    │   │   ├── DungeonCrawlerFragment.kt
    │   │   ├── DungeonEngineController.kt
    │   │   ├── DungeonEntityKind.kt
    │   │   ├── DungeonFloorGenerator.kt
    │   │   ├── DungeonFocusMode.kt
    │   │   ├── DungeonFocusNavigationStrategy.kt
    │   │   ├── DungeonStateSnapshot.kt
    │   │   ├── DungeonTileType.kt
    │   │   └── GeneratedDungeonFloor.kt
    │   └── resources/
    │       ├── icon.svg        # Automatically squircle-wrapped on Apple bundles
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/dungeoncrawler/
            └── DungeonCrawlerJestTest.kt
```

---

## Building & Testing

From `examples/games/ui-dungeon-crawler`, invoke the root Gradle wrapper (`../../../gradlew`):

```bash
# Run multiplatform tests & UI verification
../../../gradlew jvmTest
../../../gradlew macosArm64Test

# Run development builds across all 9 backends
../../../gradlew developmentWindows
../../../gradlew developmentMac
../../../gradlew developmentLinuxGtk
../../../gradlew developmentLinuxQt
../../../gradlew developmentSwing
../../../gradlew developmentAndroid
../../../gradlew developmentIos
../../../gradlew developmentIpados
../../../gradlew developmentWeb
```
