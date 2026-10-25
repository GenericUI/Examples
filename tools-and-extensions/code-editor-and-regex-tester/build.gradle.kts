plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.codeeditor"
version = "1.0-SNAPSHOT"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.codeeditor.main"
        baseName = "CodeEditorAndRegexTester"
        androidNamespace = "com.nexustools.genericui.examples.codeeditor"
        themeColor = "#6366F1"
        desktop {
            appName = "CodeEditor & Regex Playground"
            windowTitle = "GenericUI — Syntax-Highlighted CodeEditor & Regex Playground"
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "GenericUI CodeEditor & Regex Playground"
            shortName = "RegexLab"
            description = "Syntax-highlighted multi-language IDE code editor and live regular expression playground powered by GenericUI across all 8 backends."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#6366F1"
            categories = listOf("developer", "utilities", "productivity")
        }
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
    sourceSets {
        common {
            implementation("com.nexustools.genericui.tests:common:0.1.0-SNAPSHOT")
        }
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        kotlinPackageName = "com.nexustools.genericui.examples.codeeditor.styles"
    }
}
