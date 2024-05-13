plugins {
    kotlin("jvm") version(libs.versions.kotlin.get())
    `maven-publish`
    alias(libs.plugins.runpaper)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.crowdin)
    alias(libs.plugins.pv.kotlin.relocate)
    alias(libs.plugins.pv.java.templates)
}

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
    compileOnly(libs.kotlinx.coroutines.core)
    compileOnly(libs.kotlinx.coroutines.jdk8)

    compileOnly(libs.paper)
    compileOnly(libs.protocollib)

    compileOnly(libs.pv)
    compileOnly(libs.pv.lavaplayer)
    compileOnly(libs.slib)
}

crowdin {
    projectId = "plasmo-voice-addons"
    sourceFileName = "server/discs.toml"
    resourceDir = "discs/languages"
    createList = true
}

tasks {
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveAppendix.set("")
        configurations = listOf(project.configurations.shadow.get())
    }

    runServer {
        minecraftVersion("1.20.4")
        systemProperty("plasmovoice.alpha.disableversioncheck", "true")
    }

    build {
        dependsOn(shadowJar)
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(16)) // lavaplayer supports only java 16+
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "su.plo.voice.discs.DiscsPlugin"
    apiVersion = "1.16"
    authors = listOf("KPidS", "Apehum")

    depend = listOf("PlasmoVoice", "ProtocolLib", "pv-addon-lavaplayer-lib")

    foliaSupported = true

    commands {
        register("disc") {}
    }
}
