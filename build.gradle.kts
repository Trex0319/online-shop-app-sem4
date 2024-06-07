// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
        classpath("com.google.gms:google-services:4.4.0")
    }
}
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false

    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false
}