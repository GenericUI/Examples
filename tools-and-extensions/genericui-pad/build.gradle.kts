plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.pad"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.pad"
        name = "GenericUIPad"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Windows 11 Dark-Mode Tabbed Notepad & Rich Markdown Editor clone powered by GenericUI across all 8 backends."
    }
    window {
        title = "GenericUIPad"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#1E1E1E"
    }
    application {
        baseName = "GenericUIPad"
        androidNamespace = "com.nexustools.genericui.examples.pad"
        themeColor = "#1E1E1E"
        desktop {
            appName = "GenericUIPad"
            windowTitle = "GenericUIPad"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Windows 11 Dark-Mode Tabbed Notepad & Rich Markdown Editor clone powered by GenericUI across all 8 backends."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "GenericUIPad"
            shortName = "GenericUIPad"
            description = "Windows 11 Dark-Mode Tabbed Notepad & Rich Markdown Editor clone powered by GenericUI across all 8 backends."
            display = "standalone"
            backgroundColor = "#18181B"
            themeColor = "#1E1E1E"
            categories.addAll(listOf("productivity", "utilities", "developer"))
        }
    }
    backends {
        all()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        globalStyleSheet = "styles.scss"
        kotlinPackageName = "com.nexustools.genericui.examples.pad.styles"
    }
}
