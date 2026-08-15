// Firebase (Crashlytics + Analytics) needs a real google-services.json from your own Firebase
// console project (Project Settings > Your apps > google-services.json) dropped in app/.
// The plugins that process it are applied conditionally so the build keeps working before
// that file exists; once it's present, Firebase wiring turns on automatically.
val hasFirebaseConfig = file("google-services.json").exists()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.application)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    kotlin("kapt")
}

if (hasFirebaseConfig) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "com.charles.warmwords.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.charles.warmwords.app"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("boolean", "FIREBASE_ENABLED", hasFirebaseConfig.toString())
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.material.icon.extended)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.splashscreen)

    // AI
    implementation(libs.litertlm)

    // Utilities
    implementation(libs.gson)
    implementation(libs.material3.views)

    // Firebase (Crashlytics + Analytics). SDK is always on the classpath so the app compiles
    // with or without a real google-services.json; actual init/usage is guarded at runtime by
    // BuildConfig.FIREBASE_ENABLED, which only turns on once that file is present (see above).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Hilt DI
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.hilt.android.testing)
}

kapt {
    javacOptions {
        // Room's DatabaseVerifier (kapt annotation processing) spins up sqlite-jdbc inside a
        // separately forked JDK-toolchain worker JVM that gets a minimal/clean environment on
        // this machine, so java.io.tmpdir resolves to C:\WINDOWS (not writable) -> crash.
        // -J forwards these as real JVM args to that forked worker process.
        option("-J-Djava.io.tmpdir=${System.getProperty("java.io.tmpdir")}")
        option("-J-Dorg.sqlite.tmpdir=${System.getProperty("java.io.tmpdir")}")
    }
}
