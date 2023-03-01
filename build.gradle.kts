// version
val mavenGroup: String by rootProject
val mavenArtifactId: String by rootProject
val pluginVersion: String by rootProject

// java
val targetJavaVersion: String by rootProject

// libs
val kotlinVersion: String by rootProject
val paperVersion: String by rootProject
val lombokVersion: String by rootProject

// dev
val devDirs: String by rootProject
val slurper = groovy.json.JsonSlurper()

plugins {
    kotlin("jvm") version("1.8.10")
    kotlin("kapt") version "1.6.10"
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.0.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2" // Generates plugin.yml
    id("com.github.johnrengelman.shadow") version("7.0.0")
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

    maven {
        url = uri("https://repo.plo.su")
    }

    maven { url = uri("https://m2.dv8tion.net/releases") }

    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }

}

dependencies {
    compileOnly(kotlin("stdlib", kotlinVersion))
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    implementation(shadow("com.sedmelluq:lavaplayer:1.3.77")!!)

    compileOnly("su.plo.voice.api:server:2.0.0+ALPHA")
    compileOnly("su.plo.config:config:1.0.0")

    kapt("su.plo.voice.api:server:2.0.0+ALPHA")
}

tasks {
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
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
        minecraftVersion("1.19.2")
        systemProperty("-Dplasmovoice.alpha.disableversioncheck", "true")
    }

    build {
        dependsOn(shadowJar)
        doLast {
            val dirs = slurper.parse(devDirs.reader()) as ArrayList<String>
            for (dir in dirs) {
                val target = shadowJar.get().archiveFile.get().asFile
                val dest = File(dir).resolve(target.name)

                target.inputStream().use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output, 8 * 1024)
                    }
                }
            }
        }
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "su.plo.template.TestPlugin"
    apiVersion = "1.19"
    authors = listOf("Author")

    depend = listOf("kotlin-stdlib", "PlasmoVoice")
}
