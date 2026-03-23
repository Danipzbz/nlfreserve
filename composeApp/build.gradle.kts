import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // --- CAMBIO A JS (IR) PARA MAYOR COMPATIBILIDAD ---
    js(IR) {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.projectDir.path + "/src/jsMain/resources")
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
                implementation(projects.shared)
                implementation(libs.kotlinx.datetime)
            }
        }


        val androidMain by getting {
            dependsOn(commonMain) // Android también depende de common
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation("androidx.core:core-splashscreen:1.0.1")
            }
        }

        // --- CONFIGURACIÓN DE JS MAIN ---
        val jsMain by getting {
            // Esto le dice a JS que use todo lo de commonMain
            dependsOn(commonMain)
            dependencies {
                implementation(compose.html.core)
                implementation(compose.runtime)
            }
        }
    }
}

android {
    namespace = "com.dpbprog.nlfreserve"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dpbprog.nlfreserve"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform(libs.firebase.android.platform))
    implementation(libs.firebase.android.analytics)
}