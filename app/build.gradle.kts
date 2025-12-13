plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // ATUALIZADO: Versão 2.0.21 para combinar com o libs.versions.toml
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.etheralltda.ozem"
    compileSdk = 34 // Mantém 34 para estabilidade do Google Sign-In

    defaultConfig {
        applicationId = "com.etheralltda.ozem"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Gráficos e JSON
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // --- GOOGLE SIGN IN ---
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // --- SUPABASE DEPENDENCIES ---
    // Supabase 3.0.0 (Compatível com Kotlin 2.0)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-android:3.0.0")
    // Serialização 1.7.3 (Compatível com Kotlin 2.0)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}