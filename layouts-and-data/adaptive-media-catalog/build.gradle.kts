plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.mediacatalog"
version = "1.0.0"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.mediacatalog.main"
        mainClass = "com.nexustools.genericui.examples.mediacatalog.AdaptiveMediaCatalogAppKt"
        baseName = "AdaptiveMediaCatalog"
        androidNamespace = "com.nexustools.genericui.examples.mediacatalog"
        appName = "Adaptive Media Catalog"
        appId = "com.nexustools.genericui.examples.mediacatalog"
        description = "Responsive 3-posture AdaptiveSplitTabFragment media catalog for GenericUI."
        themeColor = "#7C3AED"
        desktop {
            windowTitle = "GenericUI — Adaptive Media Catalog"
            windowWidth = 1280
            windowHeight = 800
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
