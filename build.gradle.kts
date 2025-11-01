plugins {
    kotlin("jvm") version(libs.versions.kotlin.get())
    alias(libs.plugins.pv) apply false
    alias(libs.plugins.pv.java.templates)
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
}

allprojects {
    if (project == project(":nms")) return@allprojects

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://repo.codemc.io/repository/maven-snapshots/")

        maven("https://repo.plasmoverse.com/snapshots")
        maven("https://repo.plasmoverse.com/releases")
        maven("https://repo.plo.su")

        maven("https://m2.dv8tion.net/releases")

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-public/")

        maven("https://repo.dmulloy2.net/repository/public/")

        maven("https://jitpack.io/")
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly(rootProject.libs.kotlinx.coroutines.core)
        compileOnly(rootProject.libs.kotlinx.coroutines.jdk8)

        compileOnly(rootProject.libs.packetevents)
        compileOnly(rootProject.libs.pv)
        compileOnly(rootProject.libs.pv.lavaplayer)
        compileOnly(rootProject.libs.slib)
    }
}
