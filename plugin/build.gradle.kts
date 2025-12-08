import java.net.URI

plugins {
    alias(libs.plugins.runpaper)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.crowdin)
    id("su.plo.voice.plugin.relocate-kotlin")
    id("su.plo.voice.plugin.java-templates")
}

base.archivesName = rootProject.name

dependencies {
    implementation(project(":core")) { isTransitive = false }
    implementation(project(":nms:1.19.4", "shadow")) { isTransitive = false }
    implementation(project(":nms:1.20.6", "shadow")) { isTransitive = false }
    implementation(project(":nms:1.21.3", "shadow")) { isTransitive = false }
    implementation(project(":nms:1.21.5", "shadow")) { isTransitive = false }
    compileOnly(libs.paper.v11605)

    implementation(platform(libs.koin.bom)) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.koin.core) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.reflectionremapper)
}

crowdin {
    url = URI.create("https://github.com/plasmoapp/plasmo-voice-crowdin/archive/refs/heads/addons.zip").toURL()
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

        listOf(
            "co.touchlab",
            "org.koin",
            "xyz.jpenilla",
            "net.fabricmc",
        ).forEach {
            relocate(it, "${project.group}.libraries.$it")
        }
    }

    runServer {
        javaLauncher = project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(21)
        }
        minecraftVersion("1.21.10")
        systemProperty("plasmovoice.alpha.disableversioncheck", "true")

        downloadPlugins {
            modrinth("plasmo-voice", "spigot-2.1.6")
            modrinth("pv-addon-lavaplayer-lib", "1.1.10")
            modrinth("packetevents", "2.10.1")
        }
    }

    build {
        dependsOn(shadowJar)
    }

    java {
        // 2.1.6 Plasmo Voice requires 17+ java
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "${group}.DiscsPlugin"
    name = rootProject.name
    apiVersion = "1.16"
    authors = listOf("KPidS", "Apehum")

    depend = listOf("PlasmoVoice", "packetevents", "pv-addon-lavaplayer-lib")

    foliaSupported = true

    commands {
        register("disc") {}
    }
}
