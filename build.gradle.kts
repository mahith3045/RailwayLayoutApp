import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform) // Kotlin Multiplatform
    alias(libs.plugins.composeMultiplatform) // Compose Multiplatform
    kotlin("plugin.serialization") version "1.9.21" // Serialization Plugin
    kotlin("plugin.compose") // ✅ Required for Kotlin 2.0.0+
}

kotlin {
    jvm("desktop") // Target JVM for Desktop

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Dependencies
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3) // Material 3 UI Components
                implementation(compose.ui)
                implementation(compose.components.resources) // Resource handling

                // Serialization Library
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs) // Desktop Support
                implementation(libs.kotlinx.coroutines.swing) // Coroutines for Swing
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.mahith.desktopapp.MainKt" // Entry Point
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb) // macOS, Windows, Linux
            packageName = "RailwayControlPanel"
            packageVersion = "1.0.0"
        }
    }
}
