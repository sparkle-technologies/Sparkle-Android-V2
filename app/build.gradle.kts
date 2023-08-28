plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.cyberflow.sparkle"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.cyberflow.sparkle"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.compose.runtime:runtime:1.5.0")  // for compose


    implementation(project(mapOf("path" to ":base")))
    implementation(project(mapOf("path" to ":base_resources")))

    // auth for twitter/google social login
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // for web3Auth
    implementation("com.github.web3auth:single-factor-auth-android:0.0.2")
    implementation("org.torusresearch:fetch-node-details-java:3.1.0")
    implementation("com.auth0.android:jwtdecode:2.0.2")

    // for unipass - CA
    implementation("org.web3j:core:4.8.7-android")
    implementation("com.github.UniPassID:android-custom-auth-sdk:v0.0.1-alpha.9")

}