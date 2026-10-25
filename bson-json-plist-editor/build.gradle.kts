plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.dataeditor"
version = "1.0-SNAPSHOT"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.dataeditor.main"
        baseName = "BsonJsonPlistEditor"
        androidNamespace = "com.nexustools.genericui.examples.dataeditor"
        themeColor = "#1E293B"
        desktop {
            appName = "BSON JSON Plist Editor"
            windowTitle = "BSON / JSON / Binary Plist Visual Editor"
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "BSON / JSON / Binary Plist Visual Editor"
            shortName = "DataEditor"
            description = "Visual multi-tab JSON, Binary JSON (BSON), and Binary Plist (bplist00) editor powered by GenericUI across all 8 backends."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#1E293B"
            categories.addAll(listOf("developer", "utilities", "productivity"))
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
        kotlinPackageName = "com.nexustools.genericui.examples.dataeditor.styles"
    }
}
