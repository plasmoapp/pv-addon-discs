dependencies {
    compileOnly(libs.paper.v11605)

    api(platform(libs.koin.bom))
    api(libs.koin.core)
    api(libs.reflectionremapper)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}