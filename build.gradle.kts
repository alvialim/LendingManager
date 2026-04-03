import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
    alias(libs.plugins.room) apply false
}

// After all projects apply Kotlin/JS, allow yarn.lock to refresh when npm inputs change (avoids kotlinStoreYarnLock hard failure).
gradle.projectsEvaluated {
    rootProject.extensions.findByType(YarnRootExtension::class.java)?.yarnLockAutoReplace = true
}