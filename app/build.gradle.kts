plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.cyberflow.sparkle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cyberflow.sparkle"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation("com.google.android.libraries.places:places:3.1.0")

    // for wallet-connector, it integrates with metamask, trust wallet, coinbase wallet etc
    implementation(project(mapOf("path" to ":dapp")))

    // for web3Auth
    implementation("com.github.web3auth:single-factor-auth-android:0.0.2")
    implementation("org.torusresearch:fetch-node-details-java:3.1.0")
    implementation("com.auth0.android:jwtdecode:2.0.2")

    // for unipass - CA
    implementation("org.web3j:core:4.8.7-android")
    implementation("com.github.UniPassID:android-custom-auth-sdk:v0.0.1-alpha.9")

    // for UI
    implementation("com.tbuonomo:dotsindicator:5.0")     // viewpager2 indicator

}