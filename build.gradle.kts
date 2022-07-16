buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("com.android.tools.build:gradle:7.0.4")
    }
}

group = "io.github.jan.einkaufszettel"
version = "1.0"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}