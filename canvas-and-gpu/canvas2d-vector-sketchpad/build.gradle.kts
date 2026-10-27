plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.sketchpad"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.sketchpad"
        name = "Canvas2D Vector Sketchpad"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Interactive 2D vector drawing and cubic-Bezier curve sketchpad built with GenericUI."
    }
    window {
        title = "Canvas2D Vector & Bezier Sketchpad"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#3B82F6"
    }
    application {
        baseName = "Canvas2DVectorSketchpad"
        androidNamespace = "com.nexustools.genericui.examples.sketchpad"
        themeColor = "#3B82F6"
        desktop {
            appName = "Canvas2D Vector Sketchpad"
            windowTitle = "Canvas2D Vector & Bezier Sketchpad"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Interactive 2D vector drawing and cubic-Bezier curve sketchpad built with GenericUI."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Canvas2D Vector & Bezier Sketchpad"
            shortName = "VectorSketch"
            description = "Interactive 2D vector drawing and cubic-Bezier curve sketchpad built with GenericUI."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#3B82F6"
            categories("graphics", "design", "productivity")
        }
    }
    backends {
        all()
    }
    extensions {
        canvas2d()
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
        kotlinPackageName = "com.nexustools.genericui.examples.sketchpad.styles"
    }
}
