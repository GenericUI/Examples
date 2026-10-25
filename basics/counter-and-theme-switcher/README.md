# Counter & Theme Switcher (`examples/basics/counter-and-theme-switcher`)

A beginner-friendly **GenericUI** multiplatform example demonstrating reactive UI controls, Bootstrap contextual styling, runtime theme mode (`LIGHT`, `DARK`, `SYSTEM`) and accent `themeColor` switching, and cross-platform object persistence via `PreferenceStore`.

## Key `GenericUI` APIs Demonstrated

- **`FragmentApplication` & `Fragment`**: Structured application shell with `TitleBar`, `MenuBar`, and lifecycle-managed fragment views.
- **Core Widgets & Layouts**: `VStack`, `HStack`, `Card`, `Button`, `Badge`, `Select`, `Switch`, `Heading`, and `Text`.
- **Bootstrap Styling & Precompiled SASS**: Uses `BootstrapType` (`PRIMARY`, `SECONDARY`, `DANGER`, `SUCCESS`, `INFO`, `WARNING`), `SpacingScale`, and build-time `styles.scss` precompilation (`StylesPrecompiledStyleSheet`).
- **Runtime Theme & Accent Color Switching**: Updates `Application.setThemeMode(...)` (`ThemeMode.LIGHT`, `ThemeMode.DARK`, `ThemeMode.SYSTEM`) and `Application.setThemeColor(...)` (`#2563EB`, `#10B981`, `#8B5CF6`).
- **`PreferenceStore` Object Persistence**: Automatically serializes and restores `CounterSettings` (`PreferenceSerializable` + `PreferenceObjectCodec`) across all 8 backends.
- **`JestElectricRunner` Test Suite**: Full BDD (`describe`, `it`, `expect`, `expectNode`) and Robolectric-style (`launchApp`, `findById`, `performClick`, `performSelectValue`, `performSetChecked`) UI test coverage.

## Project Layout

```text
examples/basics/counter-and-theme-switcher/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/counter/
    │   │   ├── CounterApp.kt
    │   │   ├── CounterFragment.kt
    │   │   ├── CounterPlatformBootstrap.kt
    │   │   ├── CounterSettings.kt
    │   │   └── CounterVisualTestMain.kt
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/counter/
            └── CounterJestTest.kt
```

## Building & Running Across All 8 Backends

From `examples/basics/counter-and-theme-switcher`, invoke the root Gradle wrapper (`../../../gradlew`):

```bash
# Inspect resolved GenericUI backends, extensions, and SASS configuration
../../../gradlew printGenericUiConfig

# Build / Run Desktop & Web targets
../../../gradlew swingDevelopment
../../../gradlew windowsDevelopment
../../../gradlew macDevelopment
../../../gradlew linuxDevelopmentGtk
../../../gradlew linuxDevelopmentQt
../../../gradlew webDevelopment

# Build Mobile targets
../../../gradlew androidDevelopment
../../../gradlew iosDevelopment
```

## Running `JestElectricRunner` Tests

```bash
# Run the headless JVM / Swing JestElectricRunner test suite
../../../gradlew jvmTest

# Compile Web (JS & WasmJS) and Native Windows targets
../../../gradlew compileKotlinJs compileKotlinWasmJs compileKotlinMingwX64

# Run live onscreen Windows visual UI tests
../../../gradlew windowsVisualTest
```
