import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version Versions.COMPOSE
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("com.android.library")
}

group = "io.github.jan.einkaufszettel"
version = "1.0"

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("io.ktor:ktor-client-okhttp:${Versions.KTOR}")
                api("io.ktor:ktor-client-content-negotiation:${Versions.KTOR}")
                api("io.ktor:ktor-serialization-kotlinx-json:${Versions.KTOR}")
                api("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATETIME}")
                api("com.soywiz.korlibs.klock:klock:${Versions.KORLIBS}")
                api("io.github.jan-tennert.supacompose:Supacompose-Postgrest:${Versions.SUPACOMPOSE}")
                api("io.github.jan-tennert.supacompose:Supacompose-Storage:${Versions.SUPACOMPOSE}")
                api("io.insert-koin:koin-core:${Versions.KOIN}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.4.1")
                api("androidx.core:core-ktx:1.7.0")
               // implementation("io.insert-koin:koin-android:${Versions.KOIN}")
                api("io.insert-koin:koin-androidx-compose:${Versions.KOIN}")
                //ksp("androidx.room:room-compiler:${Versions.ROOM}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(32)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(32)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}