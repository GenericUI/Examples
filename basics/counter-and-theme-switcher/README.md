# Counter & Theme Switcher (`examples/basics/counter-and-theme-switcher`)

A beginner-friendly **GenericUI** multiplatform example demonstrating reactive UI controls, Bootstrap contextual styling, runtime theme mode (`LIGHT`, `DARK`, `SYSTEM`) and accent `themeColor` switching, and cross-platform object persistence via `PreferenceStore` across all 8 backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `CounterFragment` (`@MainAppFragment`) and `CounterApp` (`@AppApplication` extending `FragmentApplication`) with `TitleBar`, `MenuBar`, and zero manual `main()` or per-backend bootstrap boilerplate.
- **Core Widgets & Layouts**: `VStack`, `HStack`, `Card`, `Button`, `Badge`, `Select`, `SelectOption`, `Switch`, `Heading`, and `Text`.
- **Bootstrap Styling & Precompiled SASS**: Uses `BootstrapType` (`PRIMARY`, `SECONDARY`, `DANGER`, `SUCCESS`, `INFO`, `WARNING`), `BootstrapSize`, `SpacingScale`, and build-time `styles.scss` precompilation (`StylesPrecompiledStyleSheet`).
- **Runtime Theme & Accent Color Switching**: Updates `setThemeMode(...)` (`ThemeMode.LIGHT`, `ThemeMode.DARK`, `ThemeMode.SYSTEM`) and `setThemeColor(...)` (`#2563EB`, `#10B981`, `#8B5CF6`).
- **`PreferenceStore` Object Persistence**: Automatically serializes and restores `CounterSettings` (`PreferenceSerializable` + `PreferenceObjectCodec`) across all 8 backends.
- **`JestElectricRunner` Test Suite**: Full BDD (`describe`, `it`, `expect`, `expectNode`) and Robolectric-style (`launchApp`, `launchFragment`, `findById`, `performClick`, `performSelectValue`, `performSetChecked`) UI test coverage in `CounterJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/basics/counter-and-theme-switcher/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/counter/
    │   │   ├── CounterApp.kt          # @AppApplication FragmentApplication (TitleBar, MenuBar, Styles)
    │   │   ├── CounterFragment.kt     # @MainAppFragment primary counter & theme switcher UI
    │   │   └── CounterSettings.kt     # PreferenceStore-serializable settings model
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/counter/
            └── CounterJestTest.kt     # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.counter"
        name = "Counter & Theme Switcher"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Hello GenericUI: Reactive counter, step selector, runtime theme switcher, and PreferenceStore persistence."
    }
    window {
        title = "Counter & Theme Switcher"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#2563EB"
    }
    backends {
        all()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`CounterFragment.kt`)

```kotlin
@MainAppFragment
class CounterFragment(
    val preferenceStore: PreferenceStore = PreferenceStore(CounterSettings.DEFAULT_STORE_NAME),
    var onThemeUpdated: ((ThemeMode, String) -> Unit)? = null
) : Fragment() {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        // Builds reactive counter, step selector, theme mode/color controls, and PreferenceStore sync
    }
}
```

---

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

---

## Running `JestElectricRunner` Tests

```bash
# Run the headless JVM / Swing JestElectricRunner test suite
../../../gradlew jvmTest

# Compile Web (JS & WasmJS) and Native Windows targets
../../../gradlew compileKotlinJs compileKotlinWasmJs compileKotlinMingwX64

# Run live onscreen Windows visual UI tests
../../../gradlew windowsVisualTest
```
