plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.dataexplorer"
version = "1.0.0"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.dataexplorer.main"
        mainClass = "com.nexustools.genericui.examples.dataexplorer.VirtualizedDataExplorerAppKt"
        baseName = "VirtualizedDataExplorer"
        androidNamespace = "com.nexustools.genericui.examples.dataexplorer"
        appName = "Virtualized Data Explorer"
        appId = "com.nexustools.genericui.examples.dataexplorer"
        description = "10,000-row LazyList, LazyGrid, and TreeView virtualized dataset explorer for GenericUI."
        themeColor = "#2563EB"
        desktop {
            windowTitle = "GenericUI — Virtualized Data Explorer (10,000 Rows)"
            windowWidth = 1280
            windowHeight = 820
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Virtualized Data Explorer"
            shortName = "DataExplorer"
            description = "10,000-row virtualized LazyList, LazyGrid, and TreeView explorer."
            themeColor = "#2563EB"
            backgroundColor = "#0F172A"
            display = "standalone"
            categories("developer", "utilities", "productivity")
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
        kotlinPackageName = "com.nexustools.genericui.examples.dataexplorer.styles"
    }
}
