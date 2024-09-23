plugins {
    id("com.github.johnrengelman.shadow")
//    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(libs.paper.v11904)

//    paperweight.paperDevBundle(libs.versions.paper11904.get())
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}