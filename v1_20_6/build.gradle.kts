plugins {
    id("com.github.johnrengelman.shadow")
//    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(libs.paper.v12006)

//    paperweight.paperDevBundle(libs.versions.paper12006.get())
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}