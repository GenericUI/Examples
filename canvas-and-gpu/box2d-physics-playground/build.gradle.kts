plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.physics2d"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.physics2d"
        name = "Box2D Physics Playground"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Box2D rigid-body physics sandbox with revolute joints, collision impulse solver, and Canvas2D debug drawer."
    }
    window {
        title = "Box2D Rigid-Body Physics Playground"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#10B981"
    }
    application {
        baseName = "Box2DPhysicsPlayground"
        androidNamespace = "com.nexustools.genericui.examples.physics2d"
        themeColor = "#10B981"
        desktop {
            appName = "Box2D Physics Playground"
            windowTitle = "Box2D Rigid-Body Physics Playground"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Box2D rigid-body physics sandbox with revolute joints, collision impulse solver, and Canvas2D debug drawer."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Box2D Rigid-Body Physics Playground"
            shortName = "Physics2D"
            description = "Box2D rigid-body physics sandbox with revolute joints, collision impulse solver, and Canvas2D debug drawer."
            display = "standalone"
            backgroundColor = "#090D16"
            themeColor = "#10B981"
            categories("simulation", "games", "education")
        }
    }
    backends {
        all()
    }
    extensions {
        box2d()
        canvas2d()
        dsl()
    }
    sourceSets {
        common {
            implementation("com.nexustools.genericui.extensions.canvas2d:dsl:0.1.0-SNAPSHOT")
            implementation("com.nexustools.genericui.extensions.box2d:dsl:0.1.0-SNAPSHOT")
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
        kotlinPackageName = "com.nexustools.genericui.examples.physics2d.styles"
    }
}
