plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.filterlab"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.filterlab"
        name = "GPU Image Filter Lab"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Real-time offscreen GPU image processing pipeline and 256-bin R/G/B/Luminance histogram lab built with GenericUI."
    }
    window {
        title = "Offscreen GPU Image Filter & Histogram Lab"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#8B5CF6"
    }
    application {
        baseName = "GpuImageFilterLab"
        androidNamespace = "com.nexustools.genericui.examples.filterlab"
        themeColor = "#8B5CF6"
        desktop {
            appName = "GPU Image Filter Lab"
            windowTitle = "Offscreen GPU Image Filter & Histogram Lab"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Real-time offscreen GPU image processing pipeline and 256-bin R/G/B/Luminance histogram lab built with GenericUI."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Offscreen GPU Image Filter & Histogram Lab"
            shortName = "FilterLab"
            description = "Real-time offscreen GPU image processing pipeline and 256-bin R/G/B/Luminance histogram lab built with GenericUI."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#8B5CF6"
            categories("photo", "graphics", "utilities")
        }
    }
    backends {
        all()
    }
    extensions {
        gpu()
        imageProcessing()
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
        kotlinPackageName = "com.nexustools.genericui.examples.filterlab.styles"
    }
}
