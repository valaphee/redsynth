plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.palantir.git-version") version "0.12.3"
    id("io.papermc.paperweight.userdev") version "1.3.8"
    kotlin("jvm") version "1.7.20"
}

group = "com.valaphee"
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = "${details.lastTag}.${details.commitDistance}"

dependencies {
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib"))
}

tasks {
    compileJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    compileKotlin { kotlinOptions { jvmTarget = "17" } }

    processResources { filesMatching("/plugin.yml") { expand("project" to project) } }

    assemble { dependsOn(reobfJar) }

    shadowJar { archiveName = "redsynth.jar" }

    build { dependsOn(shadowJar) }
}
