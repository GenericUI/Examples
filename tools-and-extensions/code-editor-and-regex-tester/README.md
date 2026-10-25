# Syntax-Highlighted CodeEditor & Regex Playground (`code-editor-and-regex-tester`)

A multiplatform developer tool built with `GenericUI` and `:extensions:codeeditor` demonstrating:

- **Multi-Language Syntax Highlighting (`IdeCodeEditor` & `CodeLanguage`)**: Live lexical syntax highlighting, line numbers, code folding (`{ ... }`), bracket matching, diagnostic squiggle underlines (`CodeDiagnostic`), and synchronized `CodeMinimap` across `KOTLIN`, `TYPESCRIPT`, `JAVASCRIPT`, `JSON`, `CSS`, `PYTHON`, `RUST`, `SQL`, and `MARKDOWN`.
- **Live Regular Expression Playground**: Interactive regex pattern input (`#input-regex-pattern`), case-insensitive (`#check-ignore-case`) and multiline (`#check-multiline`) flags, match count badge (`#badge-match-count`), inline editor match diagnostics, and a structured capture-group extraction table (`#table-capture-groups`).
- **Persistent Snippet Library (`PreferenceStore`)**: Built-in language presets plus custom code/regex snippet saving (`#btn-save-snippet`) and loading (`#btn-load-snippet`) backed by `PreferenceStore("CodeEditorRegexExample")`.

---

## Project Layout (`1 Class Per File`)

```text
examples/tools-and-extensions/code-editor-and-regex-tester/
├── README.md
├── settings.gradle.kts
├── gradle.properties
├── build.gradle.kts
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/codeeditor/
    │   │   ├── RegexMatchRecord.kt            # Match record model (index, fullMatch, rangeText, groups)
    │   │   ├── CodeSnippetPreset.kt           # Preset model + PreferenceStore object factory
    │   │   ├── CodeEditorRegexFragment.kt     # Primary UI Fragment (IdeCodeEditor + Regex Playground)
    │   │   ├── CodeEditorRegexApp.kt          # FragmentApplication + main(args) entry point
    │   │   └── CodeEditorRegexVisualTestMain.kt # Visual test entry point (runVisualAppTestMain)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/codeeditor/
            └── CodeEditorRegexJestTest.kt     # JestElectricRunner BDD + Robolectric UI test suite
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/code-editor-and-regex-tester/`:

| Target Backend | Development Build Command | Production Build Command |
| :--- | :--- | :--- |
| **Windows (Win32)** | `./gradlew windowsDevelopment` | `./gradlew windowsProduction` |
| **macOS (AppKit)** | `./gradlew macDevelopment` | `./gradlew macProduction` |
| **Linux GTK** | `./gradlew linuxDevelopmentGtk` | `./gradlew linuxProductionGtk` |
| **Linux Qt** | `./gradlew linuxDevelopmentQt` | `./gradlew linuxProductionQt` |
| **JVM / Swing** | `./gradlew swingDevelopment` | `./gradlew swingProduction` |
| **Android** | `./gradlew androidDevelopment` | `./gradlew androidProduction` |
| **iOS** | `./gradlew iosDevelopment` | `./gradlew iosProduction` |
| **Web (WasmJS + JS + PWA)** | `./gradlew webDevelopment` | `./gradlew webProduction` |

---

## Running the `JestElectricRunner` Test Suite

### Headless Multiplatform Unit & UI Tests
```bash
./gradlew jvmTest
```

### Live Windows Visual App Tests (`Win32NativeWindowHost`)
```bash
./gradlew windowsVisualTest
```
