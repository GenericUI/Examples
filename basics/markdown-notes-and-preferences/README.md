# Markdown Notes & Preferences (`examples/basics/markdown-notes-and-preferences`)

A two-pane Markdown note manager demonstrating structured object persistence in `PreferenceStore`, live `PreferenceChangeListener` notifications, live search filtering, `MarkdownView` rendering, and `.md` file import/export across all 8 `GenericUI` backends.

## Key `GenericUI` APIs Demonstrated

- **Two-Pane Shell & Surface Components**: `SplitPane`, `Card`, `ListGroup`, `ListGroupItem`
- **Form & Typography Controls**: `SearchField`, `TextField`, `TextArea`, `CheckBox`, `Badge`, `Heading`, `MarkdownView`
- **File Dialogs**: `Dialogs.openFile`, `Dialogs.saveFile`, `FileTypeFilter`
- **Reactive Structured Persistence**: `PreferenceStore`, `PreferenceSerializable`, `PreferenceObjectCodec`, `PreferenceChangeListener`
- **Testing**: `JestElectricRunner` (`launchApp`, `launchFragment`, `performClick`, `performTypeText`, `performClearText`, `performSetChecked`, `expectNode`)

## Project Layout (`1 Class Per File`)

```text
examples/basics/markdown-notes-and-preferences/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/notes/
    │   │   ├── NoteItem.kt                 # Structured note model stored in PreferenceStore
    │   │   ├── NotesApp.kt                 # FragmentApplication + main(args) entry point
    │   │   ├── NotesFragment.kt            # Two-pane SplitPane notes editor & preview fragment
    │   │   ├── NotesPlatformBootstrap.kt   # expect/actual native backend bootstrap
    │   │   └── NotesVisualTestMain.kt      # Onscreen Windows visual test runner entry point
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/notes/
            └── NotesJestTest.kt            # Cross-platform JestElectricRunner test suite
```

## Running the Application

From `examples/basics/markdown-notes-and-preferences`:

```bash
# Run on JVM / Swing desktop
../../../gradlew runSwing

# Run native Windows (Win32 / WinUI) executable
../../../gradlew runReleaseExecutableMingwX64

# Run in Web browser (WasmJS / JS with PWA manifest)
../../../gradlew wasmJsBrowserDevelopmentRun
```

## Running the `JestElectricRunner` Test Suite

```bash
# Headless cross-platform test verification + multi-backend compilation check
../../../gradlew jvmTest compileKotlinJs compileKotlinWasmJs compileKotlinMingwX64

# Onscreen visual test execution on Windows
../../../gradlew windowsVisualTest
```
