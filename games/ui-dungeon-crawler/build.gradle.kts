plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.dungeoncrawler"
version = "0.1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.dungeoncrawler"
        name = "Abyssal Crypt"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Turn-based UI dungeon crawler built with GenericUI, :extensions:game:engine, and :extensions:game:gamepad across all 9 backends."
    }
    window {
        title = "GenericUI — Abyssal Crypt (UI Dungeon Crawler)"
        size(1160, 760)
        minSize(900, 620)
        aspectRatio(16, 10)
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        mode = "dark"
        color = "#F59E0B"
    }
    application {
        baseName = "UiDungeonCrawler"
        androidNamespace = "com.nexustools.genericui.examples.dungeoncrawler"
        themeColor = "#F59E0B"
        desktop {
            appName = "Abyssal Crypt"
            windowTitle = "GenericUI — Abyssal Crypt (UI Dungeon Crawler)"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Turn-based UI dungeon crawler built with GenericUI, :extensions:game:engine, and :extensions:game:gamepad across all 9 backends."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Abyssal Crypt — GenericUI Dungeon Crawler"
            shortName = "Abyssal Crypt"
            description = "Turn-based dungeon crawler with unified Xbox / DualSense / Switch Pro controller navigation."
            display = "standalone"
            backgroundColor = "#0B0F19"
            themeColor = "#F59E0B"
            categories.addAll(listOf("games", "rpg", "entertainment"))
        }
    }
    backends {
        all()
    }
    extensions {
        engine()
        gamepad()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
    sourceSets {
        commonMain {
            implementation("com.nexustools.genericui.extensions.game.engine:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.game.gamepad:dsl:0.1.0-SNAPSHOT")
        }
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        globalStyleSheet = "styles.scss"
        kotlinPackageName = "com.nexustools.genericui.examples.dungeoncrawler.styles"
    }
}
