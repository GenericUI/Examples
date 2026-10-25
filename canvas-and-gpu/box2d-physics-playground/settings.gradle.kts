pluginManagement {
    includeBuild("../../..")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "box2d-physics-playground"
includeBuild("../../..")
