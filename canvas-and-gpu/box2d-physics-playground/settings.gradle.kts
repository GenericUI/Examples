pluginManagement {
    includeBuild("../../..")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    id("com.nexustools.genericui.settings")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "box2d-physics-playground"
includeBuild("../../..")
