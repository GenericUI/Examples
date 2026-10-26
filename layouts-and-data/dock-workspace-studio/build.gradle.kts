plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.dockstudio"
version = "1.0.0"

genericui {
    app {
        id = "com.nexustools.genericui.examples.dockstudio"
        name = "Dock Workspace Studio"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "GIMP-style Single/Multi-Window DockWorkspace and Overlay HUD Studio for GenericUI."
    }
    window {
        title = "GenericUI — Dock Workspace Studio"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#0EA5E9"
    }
    application {
        baseName = "DockWorkspaceStudio"
        androidNamespace = "com.nexustools.genericui.examples.dockstudio"
        themeColor = "#0EA5E9"
        desktop {
            appName = "Dock Workspace Studio"
            windowTitle = "GenericUI — Dock Workspace Studio"
            windowWidth = 1360
            windowHeight = 860
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "GIMP-style Single/Multi-Window DockWorkspace and Overlay HUD Studio for GenericUI."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Dock Workspace Studio"
            shortName = "DockStudio"
            description = "GIMP-style Single/Multi-Window DockWorkspace and Overlay HUD Studio."
            themeColor = "#0EA5E9"
            backgroundColor = "#0B1120"
            display = "standalone"
            categories("graphics", "productivity", "developer")
        }
    }
    backends {
        all()
    }
    extensions {
        dsl()
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
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        kotlinPackageName = "com.nexustools.genericui.examples.dockstudio.styles"
    }
}
