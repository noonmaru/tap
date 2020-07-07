/*
 * Copyright (c) $date.year Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.internal.jvm.Jvm
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        maven("https://repo.maven.apache.org/maven2/") //Maven Central
        maven("https://papermc.io/repo/repository/maven-public/") //Paper
        if (project.name != "api") {
            mavenLocal()
        }
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
    }

    group = requireNotNull(properties["pluginGroup"]) { "Group is undefined in properties" }
    version = requireNotNull(properties["pluginVersion"]) { "Version is undefined in properties" }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
        javadoc {
            options.encoding = "UTF-8"
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")

    dependencies {
        testImplementation(group = "junit", name = "junit", version = "4.12")

        if (project.name != "api") {
            implementation(project(":api"))
        }
    }

    tasks {
        create<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        if (project.name == "api") {
            processResources {
                filesMatching("**/*.yml") {
                    expand(project.properties)
                }
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("Tap") {
                val parent = parent!!
                artifactId = project.name.let { if (it == "api") parent.name else "${parent.name}-${project.name}" }
                from(components["java"])
                artifact(tasks["sourcesJar"])
            }
        }
    }

    if (project.name != "api") {
        tasks.forEach { task ->
            if (task.name != "clean") {
                task.onlyIf {
                    gradle.taskGraph.hasTask(":shadowJar") || parent!!.hasProperty("withNMS")
                }
            }
        }
    }
}

project(":api") {
    dependencies {
        compileOnly(files(Jvm.current().toolsJar))
        compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")
        compileOnlyUrl("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar", "ProtocolLib.jar")
        implementation("it.unimi.dsi:fastutil:8.3.1")
    }

    tasks {
        processResources {
            filesMatching("**.*.yml") {
                expand(project.properties)
            }
        }
    }
}

dependencies {
    subprojects {
        implementation(this)
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("dist")
    }
    create<Copy>("distJar") {
        from(shadowJar)
        into("W:\\Servers\\tap\\plugins")
    }
}

if (!hasProperty("debug")) {
    tasks {
        shadowJar {
            relocate("it.unimi.dsi", "com.github.noonmaru.tap.internal.it.unimi.dsi")
        }
    }
}

fun DependencyHandlerScope.compileOnlyUrl(url: String, name: String): Dependency? {
    File("libs").mkdir()
    val jar = File("libs", name)
    val date = File("libs", "${name}.log").apply {
        if (!exists()) {
            createNewFile()
        }
    }
    (URL(url).openConnection() as HttpURLConnection).run {
        val lastModified = getHeaderField("Last-Modified")
        if (lastModified != String(date.readBytes())) {
            inputStream.use {
                Files.copy(it, Paths.get(jar.toURI()), StandardCopyOption.REPLACE_EXISTING)
                Files.write(Paths.get(date.toURI()), lastModified.toByteArray(), StandardOpenOption.WRITE)
            }
        }
    }
    return compileOnly(files(jar.toURI()))
}