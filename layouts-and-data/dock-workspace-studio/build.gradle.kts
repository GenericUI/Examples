plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.dockstudio"
version = "1.0.0"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.dockstudio.main"
        mainClass = "com.nexustools.genericui.examples.dockstudio.DockWorkspaceStudioAppKt"
        baseName = "DockWorkspaceStudio"
        androidNamespace = "com.nexustools.genericui.examples.dockstudio"
        appName = "Dock Workspace Studio"
        appId = "com.nexustools.genericui.examples.dockstudio"
        description = "GIMP-style Single/Multi-Window DockWorkspace and Overlay HUD Studio for GenericUI."
        themeColor = "#0EA5E9"
        desktop {
            windowTitle = "GenericUI — Dock Workspace Studio"
            windowWidth = 1360
            windowHeight = 860
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
