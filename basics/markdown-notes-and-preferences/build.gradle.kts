plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.notes"
version = "0.1.0-SNAPSHOT"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.notes.main"
        mainClass = "com.nexustools.genericui.examples.notes.NotesAppKt"
        baseName = "MarkdownNotesAndPreferences"
        androidNamespace = "com.nexustools.genericui.examples.notes"
        themeColor = "#8B5CF6"
        desktop {
            appName = "Markdown Notes & Preferences"
            windowTitle = "Markdown Notes & Preferences"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Two-pane Markdown note manager storing structured objects in PreferenceStore with live PreferenceChangeListener updates."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Markdown Notes & Preferences"
            shortName = "Notes & Prefs"
            description = "Two-pane Markdown note manager storing structured objects in PreferenceStore with live PreferenceChangeListener updates."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#8B5CF6"
            categories.addAll(listOf("productivity", "utilities", "education"))
        }
    }

    backends {
        all()
    }

    testing {
        enabled = true
        visualOnWindows = true
    }

    sourceSets {
        commonMain {
            implementation("com.nexustools.genericui.tests:common:0.1.0-SNAPSHOT")
        }
    }
}
