plugins {
    kotlin("jvm") version(libs.versions.kotlin.get())
    alias(libs.plugins.pv) apply false
    alias(libs.plugins.pv.java.templates)
//    id("io.papermc.paperweight.userdev") version "1.7.2" apply false
}

allprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        mavenLocal()

        maven {
            name = "codemc-snapshots"
            url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        }

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

        compileOnly(rootProject.libs.protocollib)
        compileOnly(rootProject.libs.pv)
        compileOnly(rootProject.libs.pv.lavaplayer)
        compileOnly(rootProject.libs.slib)
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }
}
