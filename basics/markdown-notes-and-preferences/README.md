# Markdown Notes & Preferences (`examples/basics/markdown-notes-and-preferences`)

A two-pane Markdown note manager demonstrating structured object persistence in `PreferenceStore`, live `PreferenceChangeListener` notifications, live search filtering, `MarkdownView` rendering, and `.md` file import/export across all 8 `GenericUI` backends.

---

## Key `GenericUI` APIs Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `NotesFragment` (`@MainAppFragment`) and `NotesApp` (`@AppApplication` extending `FragmentApplication`) in a single shared `src/commonMain/kotlin` source set.
- **Two-Pane Shell & Surface Components**: `SplitPane` (`Orientation.HORIZONTAL`), `Card`, `ListGroup`, and `ListGroupItem`.
- **Form & Typography Controls**: `SearchField`, `TextField`, `TextArea`, `CheckBox`, `Badge`, `Heading`, `Text`, and `MarkdownView`.
- **Native File Browser Dialogs**: `Dialogs.openFile` and `Dialogs.saveFile` with `FileTypeFilter`.
- **Reactive Structured Persistence**: `PreferenceStore` (`putObject`, `getObject`, `putStringList`, `getStringList`), `PreferenceSerializable`, `PreferenceObjectCodec`, and live `PreferenceChangeListener` telemetry (`#badge-last-change`).
- **`JestElectricRunner` Test Suite**: Full UI test coverage in `NotesJestTest.kt` using `launchApp`, `launchFragment`, `performClick`, `performTypeText`, `performClearText`, `performSetChecked`, and `expectNode`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/basics/markdown-notes-and-preferences/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/notes/
    │   │   ├── NoteItem.kt        # Structured note model stored in PreferenceStore
    │   │   ├── NotesApp.kt        # @AppApplication FragmentApplication (TitleBar & Styles)
    │   │   └── NotesFragment.kt   # @MainAppFragment two-pane SplitPane notes editor & Markdown preview
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/notes/
            └── NotesJestTest.kt   # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.notes"
        name = "Markdown Notes & Preferences"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Two-pane Markdown note manager storing structured objects in PreferenceStore with live PreferenceChangeListener updates."
    }
    window {
        title = "Markdown Notes & Preferences"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#8B5CF6"
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

### `@MainAppFragment` (`NotesFragment.kt`)

```kotlin
@MainAppFragment
class NotesFragment(
    val preferenceStore: PreferenceStore = PreferenceStore(NoteItem.PREFERENCE_NAMESPACE)
) : Fragment() {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        preferenceStore.addChangeListener(preferenceListener)
        // Builds SplitPane with saved notes list, editor fields, and live MarkdownView preview
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/basics/markdown-notes-and-preferences`, invoke the root Gradle wrapper (`../../../gradlew`):

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
# Headless cross-platform test verification + multi-backend compilation check
../../../gradlew jvmTest compileKotlinJs compileKotlinWasmJs compileKotlinMingwX64

# Onscreen visual test execution on Windows
../../../gradlew windowsVisualTest
```
