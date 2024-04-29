// version
val mavenGroup: String by rootProject
val pluginVersion: String by rootProject

// libs
val paperVersion: String by rootProject
val foliaVersion: String by rootProject
val plasmoVoiceVersion: String by rootProject
val lavaplayerLibVersion: String by rootProject

plugins {
    kotlin("jvm") version("1.6.10")
    `maven-publish`
    id("xyz.jpenilla.run-paper") version("2.0.1")
    id("net.minecrell.plugin-yml.bukkit") version("0.6.0")
    id("com.github.johnrengelman.shadow") version("7.0.0")
    id("su.plo.crowdin.plugin") version("1.0.0")
    id("su.plo.voice.relocate") version("1.0.1")
}

group = mavenGroup
version = pluginVersion

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }

    maven("https://repo.plo.su")
    maven("https://repo.plasmoverse.com/releases")

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

    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly("dev.folia:folia-api:$foliaVersion")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

    compileOnly("su.plo:pv-addon-lavaplayer-lib:$lavaplayerLibVersion")
    compileOnly("su.plo.voice.api:server:$plasmoVoiceVersion")
    compileOnly("su.plo.config:config:1.0.0")
}

plasmoCrowdin {
    projectId = "plasmo-voice-addons"
    sourceFileName = "server/discs.toml"
    resourceDir = "discs/languages"
    createList = true
}

tasks {
    processResources {
        dependsOn(plasmoCrowdinDownload)
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
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "su.plo.voice.discs.DiscsPlugin"
    apiVersion = "1.19"
    authors = listOf("KPidS", "Apehum")

    depend = listOf("PlasmoVoice", "ProtocolLib", "pv-addon-lavaplayer-lib")

    foliaSupported = true

    commands {
        register("disc") {}
    }
}
