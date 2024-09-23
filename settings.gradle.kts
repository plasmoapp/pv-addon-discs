pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io/")
        maven("https://repo.plasmoverse.com/snapshots")
        maven("https://repo.plasmoverse.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "pv-addon-discs"

include("core")
include("v1_19_4")
include("v1_20_6")
include("plugin")