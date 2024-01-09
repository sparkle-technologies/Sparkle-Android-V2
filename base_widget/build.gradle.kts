plugins {
//    id("com.android.application")
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cyberflow.sparkle.widget"
    compileSdk = 33

    defaultConfig {
//        applicationId = "com.cyberflow.sparkle.widget"
        minSdk = 24
        targetSdk = 33
//        versionCode = 1
//        versionName = "1.0"

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
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    api("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    api("androidx.recyclerview:recyclerview:1.3.2")
    api("com.geyifeng.immersionbar:immersionbar:3.2.2")

    api(project(mapOf("path" to ":base_resources")))
}