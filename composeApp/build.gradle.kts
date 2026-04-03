import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleGmsGoogleServices)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // IR + stable webpack module name (must match composeApp.js in index.html).
    js(KotlinJsCompilerType.IR) {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                // Must match <script src> in jsMain/resources/index.html
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    jvm("desktop") {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }

    sourceSets {
        val commonMain by getting

        commonMain.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.serialization.json)
        }

        val mobileMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.compose.icons.extended)

                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation(libs.androidx.room.runtime)
                implementation(libs.sqlite.bundled)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.websockets)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.firebase.database)
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.common)
            }
        }

        val androidMain by getting {
            dependsOn(mobileMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.google.firebase.database)
                implementation(libs.google.firebase.firestore)
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }
        jsMain.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.firebase.app)
            implementation(libs.firebase.database)
            implementation(libs.firebase.common)
        }

        val desktopMain by getting {
            dependsOn(mobileMain)
        }
        desktopMain.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.java)
            implementation(libs.firebase.app)
            implementation(libs.firebase.java.sdk)
        }

        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.haftabook.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.haftabook.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SUPABASE_URL", "\"\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"\"")
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        // androidTarget is also a KotlinJvmTarget. Compose would otherwise call `from` on the *first* JVM
        // target (Android) and/or disable configuration — desktopMain would never run. Turn off the default
        // wiring, then bind explicitly to the `desktop` JVM target.
        disableDefaultConfiguration()
        from(kotlin.targets.getByName("desktop"))
        mainClass = "com.haftabook.app.MainKt"
        nativeDistributions {
            // Windows: build .exe / .msi on Windows only (no cross-compile). macOS: .dmg, Linux: .deb
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "com.haftabook.app"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    "kspAndroid"(libs.androidx.room.compiler)
    "kspDesktop"(libs.androidx.room.compiler)
}

// Some Gradle/KMP setups do not put kotlin-stdlib on :jsCompileClasspath from sourceSet.dependencies alone,
// which breaks compileKotlinJs (unresolved String, kotlin.js, etc.). Force stdlib onto JS + common compile graphs.
val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}"
afterEvaluate {
    listOf(
        "jsCompileClasspath",
        "jsMainCompileClasspath",
        "jsMainCompilationCompileClasspath",
        "commonMainCompileClasspath",
        "metadataCommonMainCompileClasspath",
        "metadataCompilationClasspath",
        "desktopCompileClasspath",
        "desktopMainCompileClasspath",
    ).forEach { name ->
        configurations.findByName(name)?.dependencies?.add(dependencies.create(kotlinStdlib))
    }
}
