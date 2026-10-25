# Validated Registration Wizard (`examples/basics/validated-registration-wizard`)

A multi-step onboarding wizard built with **GenericUI** demonstrating form controls, live per-step validation, progress tracking, and structured `RegistrationProfile` persistence via `PreferenceStore`.

## Key `GenericUI` APIs Demonstrated

- **3-Step Validated Wizard Flow**:
  - **Step 1 (Account Credentials)**: `TextField` (`input-username`, `input-email`) and `PasswordField` (`input-password`) with live validation rules.
  - **Step 2 (Developer Profile)**: `Select` (`select-role`) and `RangeSlider` (`slider-experience`) with live experience badge updates.
  - **Step 3 (Review & Terms)**: Summary overview, `CheckBox` (`check-terms`), and `Button` (`btn-submit`).
- **Feedback & Navigation Controls**: `ProgressBar` (`wizard-progress`), contextual `Alert` (`validation-alert`), status `Badge` (`validation-message`), and `Back` / `Next` navigation buttons.
- **`PreferenceStore` Object Persistence**: Persists the completed `RegistrationProfile` across all 8 GenericUI target backends.
- **`JestElectricRunner` Test Suite**: Tests blocked navigation on invalid inputs, typing via `performTypeText`, step progression, `performSelectValue`, `performSetSliderValue`, `performSetChecked(true)`, and `PreferenceStore` verification.

## Project Layout

```text
examples/basics/validated-registration-wizard/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/wizard/
    │   │   ├── RegistrationProfile.kt
    │   │   ├── RegistrationWizardApp.kt
    │   │   ├── RegistrationWizardFragment.kt
    │   │   ├── RegistrationWizardPlatformBootstrap.kt
    │   │   └── RegistrationWizardVisualTestMain.kt
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/wizard/
            └── RegistrationWizardJestTest.kt
```

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

## Running `JestElectricRunner` Tests

```bash
# Run the headless JVM / Swing JestElectricRunner test suite
../../../gradlew jvmTest

# Compile Web (JS & WasmJS) and Native Windows targets
../../../gradlew compileKotlinJs compileKotlinWasmJs compileKotlinMingwX64

# Run live onscreen Windows visual UI tests
../../../gradlew windowsVisualTest
```
