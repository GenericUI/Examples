plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.mediacatalog"
version = "1.0.0"

genericui {
    app {
        id = "com.nexustools.genericui.examples.mediacatalog"
        name = "Adaptive Media Catalog"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Responsive 3-posture AdaptiveSplitTabFragment media catalog for GenericUI."
    }
    window {
        title = "GenericUI — Adaptive Media Catalog"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#7C3AED"
    }
    application {
        baseName = "AdaptiveMediaCatalog"
        androidNamespace = "com.nexustools.genericui.examples.mediacatalog"
        themeColor = "#7C3AED"
        desktop {
            appName = "Adaptive Media Catalog"
            windowTitle = "GenericUI — Adaptive Media Catalog"
            windowWidth = 1280
            windowHeight = 800
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Responsive 3-posture AdaptiveSplitTabFragment media catalog for GenericUI."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Adaptive Media Catalog"
            shortName = "MediaCatalog"
            description = "Responsive Desktop Split, Phone Landscape Fullscreen, and Phone Portrait Tab catalog."
            themeColor = "#7C3AED"
            backgroundColor = "#111827"
            display = "standalone"
            categories("entertainment", "multimedia", "utilities")
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
        kotlinPackageName = "com.nexustools.genericui.examples.mediacatalog.styles"
    }
}
