plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Firebase Plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.itismob.grpfive.mco"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.itismob.grpfive.mco"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)


    // CameraX dependencies for barcode scanning
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.guava:guava:31.0.1-android")


    // Firebase Implementations
    implementation(platform("com.google.firebase:firebase-bom:33.4.0")) // Separate ktx modules in this version compared sa 35.4.0
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // GSON for save state
    implementation("com.google.code.gson:gson:2.10.1")

    // MPAndroidChart for charts, from https://github.com/PhilJay/MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.androidx.viewpager2)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}