import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.oss.licenses)
}

android {
    namespace = "com.ayogeshwaran.workoutlogger"
    compileSdk = 37

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val hasKeystore = keystorePropertiesFile.exists()

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                val properties = Properties().apply {
                    keystorePropertiesFile.inputStream().use { load(it) }
                }
                storeFile = file(properties.getProperty("storeFile"))
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.ayogeshwaran.workoutlogger"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "2026.6.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    lint {
        abortOnError = true
        error += "HardcodedText"
    }
}

ksp {
    arg("appfunctions:aggregateAppFunctions", "true")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // OSS Licenses
    implementation(libs.play.services.oss.licenses)

    implementation(libs.androidx.core.ktx)

    // AppFunctions
    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)
    ksp(libs.androidx.appfunctions.compiler)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.register<Copy>("renameReleaseBundle") {
    from(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
    into(layout.buildDirectory.dir("outputs/renamed-bundle"))
    rename { "flinklog-${android.defaultConfig.versionName}.aab" }
}

afterEvaluate {
    tasks.named("bundleRelease") {
        finalizedBy("renameReleaseBundle")
    }
}
