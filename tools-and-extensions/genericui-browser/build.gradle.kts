plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.browser"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.browser"
        name = "GenericUIBrowser"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Google Chrome Desktop and Mobile multi-tab browser clone powered by GenericUI TabbedFragment and WebView across all 8 backends."
    }
    window {
        title = "GenericUIBrowser"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#1F2937"
    }
    application {
        baseName = "GenericUIBrowser"
        androidNamespace = "com.nexustools.genericui.examples.browser"
        themeColor = "#1F2937"
        desktop {
            appName = "GenericUIBrowser"
            windowTitle = "GenericUIBrowser"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Google Chrome Desktop and Mobile multi-tab browser clone powered by GenericUI TabbedFragment and WebView across all 8 backends."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "GenericUIBrowser"
            shortName = "Browser"
            description = "Google Chrome Desktop and Mobile multi-tab browser clone powered by GenericUI TabbedFragment and WebView across all 8 backends."
            display = "standalone"
            backgroundColor = "#111827"
            themeColor = "#1F2937"
            categories.addAll(listOf("productivity", "utilities", "developer"))
        }
    }
    backends {
        all()
    }
    extensions {
        webView()
        dsl()
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
        kotlinPackageName = "com.nexustools.genericui.examples.browser.styles"
    }
}
