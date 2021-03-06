import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("org.jetbrains.compose") version Versions.COMPOSE
}

group = "io.github.jan.einkaufszettel"
version = "1.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
                // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
                implementation("ch.qos.logback:logback-classic:1.3.0-alpha16")
                implementation("io.github.g0dkar:qrcode-kotlin-jvm:3.1.0")
                implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Einkaufszettel"
            packageVersion = "1.0.1"

            val iconsRoot = project.file("src/jvmMain/resources")
            windows {
                iconFile.set(iconsRoot.resolve("orders.ico"))
                shortcut = true
                perUserInstall = true
            }
            linux {
                shortcut = true
                iconFile.set(iconsRoot.resolve("orders.png"))
            }
        }
    }
}