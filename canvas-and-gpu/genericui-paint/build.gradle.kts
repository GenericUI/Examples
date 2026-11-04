plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.paint"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.paint"
        name = "GenericUIPaint"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Windows 11 Dark-Mode Paint clone built with GenericUI and Canvas2D."
    }
    window {
        title = "Untitled - GenericUIPaint"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#202024"
    }
    application {
        baseName = "GenericUIPaint"
        androidNamespace = "com.nexustools.genericui.examples.paint"
        themeColor = "#202024"
        desktop {
            appName = "GenericUIPaint"
            windowTitle = "Untitled - GenericUIPaint"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Windows 11 Dark-Mode Paint clone built with GenericUI and Canvas2D."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Untitled - GenericUIPaint"
            shortName = "GenericUIPaint"
            description = "Windows 11 Dark-Mode Paint clone built with GenericUI and Canvas2D."
            display = "standalone"
            backgroundColor = "#18181B"
            themeColor = "#202024"
            categories("graphics", "design", "productivity")
        }
    }
    backends {
        all()
    }
    extensions {
        canvas2d()
        imageProcessing()
        dsl()
    }
    sourceSets {
        common {
            implementation("com.nexustools.genericui.extensions.canvas2d:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.tests:common:0.1.0-SNAPSHOT")
        }
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
        kotlinPackageName = "com.nexustools.genericui.examples.paint.styles"
    }
}
