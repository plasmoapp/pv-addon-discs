dependencies {
    compileOnly(libs.paper.v11605)
    compileOnly(libs.log4j.core)

    api(platform(libs.koin.bom))
    api(libs.koin.core)
    api(libs.reflectionremapper)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
