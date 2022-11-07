/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
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
