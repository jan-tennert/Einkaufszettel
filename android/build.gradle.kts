plugins {
    id("org.jetbrains.compose") version Versions.COMPOSE
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("com.google.devtools.ksp") version Versions.KSP
    id("kotlin-parcelize")
}

group = "io.github.jan.einkaufszettel"
version = "1.0"

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(project(":common"))
    //implementation("androidx.activity:activity-compose:1.4.0")
    implementation("io.github.raamcosta.compose-destinations:animations-core:${Versions.DESTINATIONS}")
    implementation("com.journeyapps:zxing-android-embedded:${Versions.ZXING}")
    // https://mvnrepository.com/artifact/com.google.accompanist/accompanist-swiperefresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.24.13-rc")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}")
    //  implementation("androidx.compose.material:material:${Versions.COMPOSE}")
   // implementation("androidx.compose.material:material-icons-extended:1.2.0-rc03")
    //  implementation("androidx.compose.ui:ui-tooling-preview:${Versions.COMPOSE}")
    //   implementation("androidx.compose.ui:ui-tooling:${Versions.COMPOSE}")
    //   implementation("androidx.compose.ui:ui:${Versions.COMPOSE}")
    //implementation("androidx.datastore:datastore-preferences:${Versions.DATASTORE}")
    //   implementation("androidx.core:core-ktx:${Versions.CORE}")
    implementation("androidx.navigation:navigation-compose:${Versions.NAVIGATION}")
    ksp("io.github.raamcosta.compose-destinations:ksp:${Versions.DESTINATIONS}")
    implementation("io.github.jan-tennert.dnsplugin:DnsPlugin:${Versions.DNSPLUGIN}")
    implementation("com.soywiz.korlibs.krypto:krypto:${Versions.KORLIBS}")
    implementation("io.coil-kt:coil:${Versions.COIL}")
    implementation("io.coil-kt:coil-compose:${Versions.COIL}")
    implementation("androidx.activity:activity-ktx:${Versions.ACTIVITY}")
    implementation("androidx.activity:activity-compose:${Versions.ACTIVITY}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-process:${Versions.LIFECYCLE}")
    implementation("androidx.camera:camera-core:${Versions.CAMERAX}")
    implementation("androidx.camera:camera-camera2:${Versions.CAMERAX}")
    implementation("androidx.camera:camera-lifecycle:${Versions.CAMERAX}")
    implementation("androidx.camera:camera-view:${Versions.CAMERAX}")
    implementation("com.google.accompanist:accompanist-permissions:${Versions.PERMISSIONS}")

    //Barcode
    implementation("com.google.mlkit:barcode-scanning:${Versions.BARCODE}")
    implementation("org.burnoutcrew.composereorderable:reorderable:${Versions.COMPOSE_REORDERABLE}")
}

android {
    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
    compileSdkVersion(32)
    defaultConfig {
        applicationId = "io.github.jan.shopping"
        minSdkVersion(24)
        targetSdkVersion(32)
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        debug {
          //  isMinifyEnabled = true
        }
    }
    lint {
        isAbortOnError = false
    }
}