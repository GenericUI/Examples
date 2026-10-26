pluginManagement {
    includeBuild("../../..")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.nexustools.genericui.settings")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "validated-registration-wizard"

includeBuild("../../..")
