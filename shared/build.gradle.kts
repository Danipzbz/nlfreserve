import org.jetbrains.compose.ComposeExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // --- CAMBIO AQUÍ: Usamos JS en lugar de Wasm ---
    js(IR) {
        browser()
        binaries.executable()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(libs.firebase.multiplatform.firestore)
            export(libs.firebase.multiplatform.common)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Firebase vuelve aquí para que no tengas errores de imports
            api(libs.firebase.multiplatform.firestore)
            api(libs.firebase.multiplatform.common)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation("dev.gitlive:firebase-firestore:1.13.0")
            implementation("dev.gitlive:firebase-common:1.13.0")
        }

        // Configuramos la carpeta de JS
        val jsMain by getting {
            dependsOn(commonMain.get())
        }
    }
}

android {
    namespace = "com.dpbprog.nlfreserve.shared"
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}