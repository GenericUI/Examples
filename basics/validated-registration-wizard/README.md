# Validated Registration Wizard (`examples/basics/validated-registration-wizard`)

A multi-step developer onboarding wizard built with **GenericUI** demonstrating form controls, live per-step validation, progress tracking, and structured `RegistrationProfile` persistence via `PreferenceStore` across all 8 backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `RegistrationWizardFragment` (`@MainAppFragment`) and `RegistrationWizardApp` (`@AppApplication` extending `FragmentApplication`) with `TitleBar`, `MenuBar`, `ThemeMode.AUTO` (`setTheme(ThemeMode.AUTO)`), and `StylesPrecompiledStyleSheet` (`addStyleSheet(...)`).
- **`ComponentBootstrap` (`getBootstrap()`) & `ComponentStyle` (`getStyle()`)**: Uses strongly-typed `getBootstrap()` (`BootstrapType`, `BootstrapSize`, `SpacingScale`, `BorderRadiusStyle`, `ShadowElevation`, `FontWeight`) and W3C `getStyle()` (`accentColor` on `RangeSlider` and `CheckBox`) with strict user-added CSS class isolation.
- **3-Step Validated Wizard Flow**:
  - **Step 1 (Account Credentials)**: `TextField` (`input-username`, `input-email`) and `PasswordField` (`input-password`) with live validation rules.
  - **Step 2 (Developer Profile)**: `Select` (`select-role`) and `RangeSlider` (`slider-experience`) with live experience badge updates (`badge-experience-years`).
  - **Step 3 (Review & Terms)**: Summary overview (`summary-username`, `summary-email`, `summary-role`, `summary-experience`), `CheckBox` (`check-terms`), and `Button` (`btn-submit`).
- **Feedback & Navigation Controls**: `ProgressBar` (`wizard-progress`), contextual `Alert` (`validation-alert`), status `Badge` (`validation-message`, `badge-current-step`), and `Back` / `Next` navigation buttons (`btn-back`, `btn-next`).
- **`PreferenceStore` Object Persistence**: Persists the completed `RegistrationProfile` across all 8 GenericUI target backends.
- **`JestElectricRunner` Test Suite**: Tests blocked navigation on invalid inputs, typing via `performTypeText`, step progression, `performSelectValue`, `performSetSliderValue`, `performSetChecked(true)`, and `PreferenceStore` verification in `RegistrationWizardJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/basics/validated-registration-wizard/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/wizard/
    │   │   ├── RegistrationProfile.kt          # PreferenceStore-serializable onboarding profile model
    │   │   ├── RegistrationWizardApp.kt        # @AppApplication FragmentApplication (TitleBar & MenuBar)
    │   │   └── RegistrationWizardFragment.kt   # @MainAppFragment 3-step validated wizard UI
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/wizard/
            └── RegistrationWizardJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.wizard"
        name = "Validated Registration Wizard"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Multi-step developer onboarding wizard with live field validation and PreferenceStore profile persistence."
    }
    window {
        title = "Validated Registration Wizard"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        mode = "auto"
        color = "#10B981"
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

### `@MainAppFragment` (`RegistrationWizardFragment.kt`)

```kotlin
@MainAppFragment
class RegistrationWizardFragment(
    val preferenceStore: PreferenceStore = PreferenceStore(RegistrationProfile.DEFAULT_STORE_NAME)
) : Fragment() {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        // Builds 3-step onboarding panels, live validation alert/badges, and PreferenceStore submission
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/basics/validated-registration-wizard`, invoke the root Gradle wrapper (`../../../gradlew`):

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
