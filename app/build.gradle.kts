plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("kotlinx-serialization")
    id("therouter")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.cyberflow.sparkle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cyberflow.sparkle"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk{
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
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

    lint {
        checkReleaseBuilds = false
        abortOnError = false
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
        buildConfig = true
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

    // auth for twitter/google social login
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.libraries.places:places:3.1.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx")   // crash
    implementation("com.google.firebase:firebase-analytics-ktx")

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
//    implementation("com.tbuonomo:dotsindicator:5.0")        // viewpager2 indicator
//    implementation("io.github.youth5201314:banner:2.2.2")

    implementation("jp.wasabeef:blurry:4.0.1")


//    kapt("cn.therouter:apt:1.2.0-rc1")
//    implementation("cn.therouter:router:1.2.0-rc1")

    // for IM
    implementation(project(mapOf("path" to ":chat")))

    kapt("cn.therouter:apt:1.2.0-rc1")
//    implementation("cn.therouter:router:1.2.0-rc1")

    val room_version = "2.5.1"
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    implementation(project(mapOf("path" to ":flutter")))

    // for publish aab to google play
    implementation("com.google.android.play:integrity:1.3.0")
}