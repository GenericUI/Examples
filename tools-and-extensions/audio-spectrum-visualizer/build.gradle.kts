plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.visualizer"
version = "1.0-SNAPSHOT"

genericui {
    application {
        entryPoint = "com.nexustools.genericui.examples.visualizer.main"
        baseName = "AudioSpectrumVisualizer"
        androidNamespace = "com.nexustools.genericui.examples.visualizer"
        themeColor = "#10B981"
        desktop {
            appName = "Audio Spectrum Visualizer"
            windowTitle = "GenericUI — Media Player, Codec Inspector & FFT Spectrum Visualizer"
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "GenericUI Audio Spectrum Visualizer"
            shortName = "SpectrumViz"
            description = "Cross-platform media player, real-time FFT spectrum & waveform visualizer, WebVTT/SRT subtitle inspector, and codec capability viewer across all 8 backends."
            display = "standalone"
            backgroundColor = "#090D16"
            themeColor = "#10B981"
            categories = listOf("multimedia", "music", "entertainment", "utilities")
        }
    }
    backends {
        all()
    }
    extensions {
        media()
        canvas2d()
        gpu()
        dsl()
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
        kotlinPackageName = "com.nexustools.genericui.examples.visualizer.styles"
    }
}
