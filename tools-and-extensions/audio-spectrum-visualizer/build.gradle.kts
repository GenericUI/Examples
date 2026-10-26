plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.visualizer"
version = "1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.visualizer"
        name = "Audio Spectrum Visualizer"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Cross-platform media player, real-time FFT spectrum & waveform visualizer, WebVTT/SRT subtitle inspector, and codec capability viewer across all 8 backends."
    }
    window {
        title = "GenericUI — Media Player, Codec Inspector & FFT Spectrum Visualizer"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        color = "#10B981"
    }
    application {
        baseName = "AudioSpectrumVisualizer"
        androidNamespace = "com.nexustools.genericui.examples.visualizer"
        themeColor = "#10B981"
        desktop {
            appName = "Audio Spectrum Visualizer"
            windowTitle = "GenericUI — Media Player, Codec Inspector & FFT Spectrum Visualizer"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Cross-platform media player, real-time FFT spectrum & waveform visualizer, WebVTT/SRT subtitle inspector, and codec capability viewer across all 8 backends."
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
            categories("multimedia", "music", "entertainment", "utilities")
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
