# Syntax-Highlighted CodeEditor & Regex Playground (`examples/tools-and-extensions/code-editor-and-regex-tester`)

A multiplatform developer tool built with `GenericUI`, `:extensions:codeeditor`, and `:extensions:dsl` across all 8 backends.

---

## Key `GenericUI` Features & Extensions Demonstrated

- **Annotation-Driven Entry Point (`@MainAppFragment` & `@AppApplication`)**: `CodeEditorRegexFragment` (`@MainAppFragment`) and `CodeEditorRegexApp` (`@AppApplication` extending `FragmentApplication` with `TitleBar` and `MenuBar`).
- **Multi-Language Syntax Highlighting (`IdeCodeEditor` & `CodeLanguage`)**: Live lexical syntax highlighting, line numbers, code folding (`{ ... }`), bracket matching, diagnostic squiggle underlines (`CodeDiagnostic`), and `CodeEditorTheme` switching (`DARK_IDE`, `DRACULA`, `MONOKAI`, `LIGHT_IDE`) across `KOTLIN`, `TYPESCRIPT`, `JAVASCRIPT`, `JSON`, `CSS`, `PYTHON`, `RUST`, `SQL`, and `MARKDOWN`.
- **Live Regular Expression Playground**: Interactive regex pattern input (`#input-regex-pattern`), case-insensitive (`#check-ignore-case`) and multiline (`#check-multiline`) flags, match count badge (`#badge-match-count`), inline editor match diagnostics, and a structured capture-group extraction table (`#table-capture-groups` with `RegexMatchRecord`).
- **Persistent Snippet Library (`PreferenceStore`)**: Built-in language presets plus custom code/regex snippet saving (`#btn-save-snippet`) and loading (`#btn-load-snippet`) backed by `PreferenceStore("CodeEditorRegexExample")` and `CodeSnippetPreset`.
- **`JestElectricRunner` Test Suite**: Full BDD and UI test coverage in `CodeEditorRegexJestTest.kt`.

---

## Project Layout (`1 Class Per .kt File`)

```text
examples/tools-and-extensions/code-editor-and-regex-tester/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   ├── kotlin/com/nexustools/genericui/examples/codeeditor/
    │   │   ├── CodeEditorRegexApp.kt          # @AppApplication FragmentApplication (TitleBar & MenuBar)
    │   │   ├── CodeEditorRegexFragment.kt     # @MainAppFragment IdeCodeEditor + Regex Playground UI
    │   │   ├── CodeSnippetPreset.kt           # Preset model + PreferenceStore object factory
    │   │   └── RegexMatchRecord.kt            # Match record model (index, fullMatch, rangeText, groups)
    │   └── resources/
    │       ├── icon.svg
    │       └── styles.scss
    └── commonTest/
        └── kotlin/com/nexustools/genericui/examples/codeeditor/
            └── CodeEditorRegexJestTest.kt     # Cross-platform JestElectricRunner test suite
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
        id = "com.nexustools.genericui.examples.codeeditor"
        name = "CodeEditor & Regex Playground"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Syntax-highlighted multi-language IDE code editor and live regular expression playground powered by GenericUI across all 8 backends."
    }
    window {
        title = "GenericUI — Syntax-Highlighted CodeEditor & Regex Playground"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#6366F1"
    }
    backends {
        all()
    }
    extensions {
        codeEditor()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
}
```

### `@MainAppFragment` (`CodeEditorRegexFragment.kt`)

```kotlin
@MainAppFragment
class CodeEditorRegexFragment(
    val preferenceStore: PreferenceStore = PreferenceStore("CodeEditorRegexExample")
) : Fragment() {
    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        evaluateRegex()
    }
}
```

---

## Building & Running Across All 8 Backends

From `examples/tools-and-extensions/code-editor-and-regex-tester`, invoke the root Gradle wrapper (`../../../gradlew`):

| Target Backend | Development Build Command | Production Build Command |
| :--- | :--- | :--- |
| **Windows (`:desktop:windows`)** | `../../../gradlew windowsDevelopment` | `../../../gradlew windowsProduction` |
| **macOS (`:desktop:macos`)** | `../../../gradlew macDevelopment` | `../../../gradlew macProduction` |
| **Linux GTK (`:desktop:gtk`)** | `../../../gradlew linuxDevelopmentGtk` | `../../../gradlew linuxProductionGtk` |
| **Linux Qt (`:desktop:qt`)** | `../../../gradlew linuxDevelopmentQt` | `../../../gradlew linuxProductionQt` |
| **JVM / Swing (`:desktop:swing`)** | `../../../gradlew swingDevelopment` | `../../../gradlew swingProduction` |
| **Android (`:mobile:android`)** | `../../../gradlew androidDevelopment` | `../../../gradlew androidProduction` |
| **iOS (`:mobile:ios`)** | `../../../gradlew iosDevelopment` | `../../../gradlew iosProduction` |
| **Web (`:web` WasmJS + JS + PWA)** | `../../../gradlew webDevelopment` | `../../../gradlew webProduction` |

---

## Running the `JestElectricRunner` Test Suite

```bash
# Headless multiplatform unit & UI tests
../../../gradlew jvmTest
../../../gradlew mingwX64Test

# Live Windows visual app tests
../../../gradlew windowsVisualTest
```
