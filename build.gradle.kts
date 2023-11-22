// libs
val paperVersion: String by rootProject
val foliaVersion: String by rootProject
val plasmoVoiceVersion: String by rootProject
val lavaplayerLibVersion: String by rootProject

plugins {
    kotlin("jvm") version("1.8.22")
    `maven-publish`
    id("xyz.jpenilla.run-paper") version("2.0.1")
    id("net.minecrell.plugin-yml.bukkit") version("0.6.0")
    id("com.github.johnrengelman.shadow") version("7.0.0")
    id("su.plo.crowdin.plugin") version("1.0.2-SNAPSHOT")
    id("su.plo.voice.plugin.relocate-kotlin") version("1.0.2-SNAPSHOT")
}

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }

    maven("https://repo.plasmoverse.com/snapshots")
    maven("https://repo.plo.su")

    maven("https://m2.dv8tion.net/releases")

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")

    maven("https://repo.dmulloy2.net/repository/public/")

    maven("https://jitpack.io/")

}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")

    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

    compileOnly("su.plo:pv-addon-lavaplayer-lib:$lavaplayerLibVersion")
    compileOnly("su.plo.voice.api:server:$plasmoVoiceVersion")
    compileOnly("su.plo.slib:spigot:1.0.0-SNAPSHOT")
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
        minecraftVersion("1.20.1")
        systemProperty("plasmovoice.alpha.disableversioncheck", "true")
    }

    build {
        dependsOn(shadowJar)
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
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
