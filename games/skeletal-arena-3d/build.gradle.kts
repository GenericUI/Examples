plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.arena3d"
version = "0.1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.arena3d"
        name = "Vanguard Arena 3D"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "3D skeletal arena action game built with GenericUI, :extensions:game:engine3d, Three.js Canvas3D, Box3D physics, and dual-stick/gyro/haptic gamepad support."
    }
    window {
        title = "GenericUI — Vanguard Arena 3D (Skeletal Combat & Box3D)"
        size(1240, 740)
        minSize(960, 600)
        aspectRatio(16, 9)
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        mode = "dark"
        color = "#EC4899"
    }
    application {
        baseName = "SkeletalArena3D"
        androidNamespace = "com.nexustools.genericui.examples.arena3d"
        themeColor = "#EC4899"
        desktop {
            appName = "Vanguard Arena 3D"
            windowTitle = "GenericUI — Vanguard Arena 3D (Skeletal Combat & Box3D)"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "3D skeletal arena action game with ProceduralHumanoidRig3D, Box3D projectiles, Follow/Orbit cameras, and DualSense/Switch/Xbox haptics."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Vanguard Arena 3D — GenericUI Skeletal Action"
            shortName = "Arena 3D"
            description = "3D arena action showcase powered by engine3d, Box3D physics, and multi-family gamepad haptics."
            display = "standalone"
            backgroundColor = "#090714"
            themeColor = "#EC4899"
            categories.addAll(listOf("games", "action", "3d"))
        }
    }
    backends {
        all()
    }
    extensions {
        engine3d()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
    sourceSets {
        commonMain {
            implementation("com.nexustools.genericui.extensions.game.engine3d:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.game.engine:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.game.gamepad:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.graphics.threejs:dsl:0.1.0-SNAPSHOT")
        }
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        globalStyleSheet = "styles.scss"
        kotlinPackageName = "com.nexustools.genericui.examples.arena3d.styles"
    }
}
