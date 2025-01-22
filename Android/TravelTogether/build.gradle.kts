import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

val localProperties = File(rootDir, "local.properties")
if (localProperties.exists()) {
    val properties = Properties()
    localProperties.inputStream().use { properties.load(it) }
    properties.forEach { key, value ->
        extra[key.toString()] = value.toString()
    }
}