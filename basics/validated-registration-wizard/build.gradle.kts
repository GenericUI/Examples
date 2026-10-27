plugins {
    id("com.android.kotlin.multiplatform.library") version "9.1.1"
    id("com.nexustools.genericui")
}

group = "com.nexustools.genericui.examples.wizard"
version = "0.1.0-SNAPSHOT"

genericui {
    app {
        id = "com.nexustools.genericui.examples.wizard"
        name = "Validated Registration Wizard"
        version = "1.0.0"
        vendor = "NexusTools"
        description = "Multi-step developer onboarding wizard with live field validation and PreferenceStore profile persistence."
    }
    window {
        title = "Validated Registration Wizard"
        icon = "src/commonMain/resources/icon.svg"
    }
    theme {
        mode = "auto"
        color = "#10B981"
    }
    application {
        baseName = "ValidatedRegistrationWizard"
        androidNamespace = "com.nexustools.genericui.examples.wizard"
        themeColor = "#10B981"
        desktop {
            appName = "Validated Registration Wizard"
            windowTitle = "Validated Registration Wizard"
            appVersion = "1.0.0"
            vendor = "NexusTools"
            description = "Multi-step developer onboarding wizard with live field validation and PreferenceStore profile persistence."
            icon = "src/commonMain/resources/icon.svg"
            webIcon = "src/commonMain/resources/icon.svg"
            linuxIcon = "src/commonMain/resources/icon.svg"
        }
        webManifest {
            enabled = true
            allowInstallAsWebApp = true
            name = "Validated Registration Wizard"
            shortName = "Reg Wizard"
            description = "Multi-step developer onboarding wizard with live field validation and PreferenceStore profile persistence."
            display = "standalone"
            backgroundColor = "#0F172A"
            themeColor = "#10B981"
            categories.addAll(listOf("productivity", "developer", "utilities"))
        }
    }

    backends {
        all()
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
}
