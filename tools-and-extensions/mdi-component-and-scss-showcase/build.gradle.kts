plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.mdishowcase"
version = "1.0-SNAPSHOT"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.mdishowcase.main"
        baseName = "MdiComponentAndScssShowcase"
        androidNamespace = "com.nexustools.genericui.examples.mdishowcase"
        themeColor = "#FF2A6D"
        desktop {
            appName = "MDI Component & Live SCSS Showcase"
            windowTitle = "GenericUI — MDI Component & Live SCSS Stylesheet Showcase"
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "GenericUI MDI Component & Live SCSS Showcase"
            shortName = "MdiScssLab"
            description = "Interactive Multi-Document Interface (MDI) component gallery and live SCSS stylesheet editor powered by GenericUI across all 8 backends."
            display = "standalone"
            backgroundColor = "#0B0F19"
            themeColor = "#FF2A6D"
            categories("developer", "design", "utilities", "productivity")
        }
    }
    backends {
        all()
    }
    extensions {
        dsl()
        codeEditor()
        canvas2d()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
    sourceSets {
        common {
            implementation("com.nexustools.genericui.tests:common:0.1.0-SNAPSHOT")
        }
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        kotlinPackageName = "com.nexustools.genericui.examples.mdishowcase.styles"
    }
}
