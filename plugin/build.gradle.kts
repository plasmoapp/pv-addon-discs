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
    implementation(project(":v1_19_4")) { isTransitive = false }
    implementation(project(":v1_20_6")) { isTransitive = false }
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
        minecraftVersion("1.21.1")
        systemProperty("plasmovoice.alpha.disableversioncheck", "true")
    }

    build {
        dependsOn(shadowJar)
    }

    java {
        // todo: set to 16, 21 is for runServer
        toolchain.languageVersion.set(JavaLanguageVersion.of(16)) // lavaplayer supports only java 16+
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "${group}.DiscsPlugin"
    name = rootProject.name
    apiVersion = "1.16"
    authors = listOf("KPidS", "Apehum")

    depend = listOf("PlasmoVoice", "ProtocolLib", "pv-addon-lavaplayer-lib")

    foliaSupported = true

    commands {
        register("disc") {}
    }
}