plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.platformer2d"
version = "0.1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.platformer2d"
        name = "Skyline Courier 2D"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "2D SVG skeletal platformer powered by GenericUI, :extensions:game:engine2d, Box2D physics, Canvas2D parallax rendering, and unified gamepad input."
    }
    window {
        title = "GenericUI — Skyline Courier 2D (SVG Skeletal Platformer)"
        size(1200, 720)
        minSize(960, 580)
        aspectRatio(16, 9)
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        mode = "dark"
        color = "#38BDF8"
    }
    application {
        baseName = "SvgSkeletalPlatformer2D"
        androidNamespace = "com.nexustools.genericui.examples.platformer2d"
        themeColor = "#38BDF8"
        desktop {
            appName = "Skyline Courier 2D"
            windowTitle = "GenericUI — Skyline Courier 2D (SVG Skeletal Platformer)"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "2D SVG skeletal platformer with Box2D physics, Canvas2D parallax stage, TwoBoneIK aim, and multi-family gamepad support."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Skyline Courier 2D — SVG Skeletal Platformer"
            shortName = "Skyline 2D"
            description = "2D side-scrolling platformer with articulated SVG skeleton, IK aiming, and Box2D rigid-body physics."
            display = "standalone"
            backgroundColor = "#070B14"
            themeColor = "#38BDF8"
            categories.addAll(listOf("games", "action", "platformer"))
        }
    }
    backends {
        all()
    }
    extensions {
        engine2d()
        dsl()
    }
    testing {
        enabled = true
        visualOnWindows = true
    }
    sourceSets {
        commonMain {
            implementation("com.nexustools.genericui.extensions.game.engine2d:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.game.engine:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.game.gamepad:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.graphics.canvas2d:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.physics.box2d:dsl:0.1.0-SNAPSHOT")
        }
    }
    styles {
        compileSass = true
        generateWebCss = true
        precompileToKotlin = true
        sourceDir = "src/commonMain/resources"
        globalStyleSheet = "styles.scss"
        kotlinPackageName = "com.nexustools.genericui.examples.platformer2d.styles"
    }
}
