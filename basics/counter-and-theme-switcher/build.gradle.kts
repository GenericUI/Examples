plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.counter"
version = "0.1.0-SNAPSHOT"

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
        mode = "auto"
        color = "#2563EB"
    }
    application {
        baseName = "CounterAndThemeSwitcher"
        androidNamespace = "com.nexustools.genericui.examples.counter"
        themeColor = "#2563EB"
        desktop {
            appName = "Counter & Theme Switcher"
            windowTitle = "Counter & Theme Switcher"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Hello GenericUI: Reactive counter, step selector, runtime theme switcher, and PreferenceStore persistence."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Counter & Theme Switcher"
            shortName = "Counter & Theme"
            description = "Hello GenericUI: Reactive counter, step selector, runtime theme switcher, and PreferenceStore persistence."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#2563EB"
            categories.addAll(listOf("utilities", "developer", "productivity"))
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
