plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.cyberflow.base"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    api("androidx.core:core-ktx:1.10.1")
    api("androidx.appcompat:appcompat:1.6.1")
    api("com.google.android.material:material:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // baisc
    api("androidx.core:core-ktx:1.3.2")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.31")

    //UI & layout
    api("androidx.appcompat:appcompat:1.3.1")
    api("com.google.android.material:material:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.1.0")
    api("com.kingja.loadsir:loadsir:1.3.8")
    api("com.github.JessYanCoding:AndroidAutoSize:v1.2.1")

    // MVVM
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    api("androidx.lifecycle:lifecycle-common-java8:2.3.1")
//    api("androidx.lifecycle:lifecycle-extensions:2.3.1")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
//    api("androidx.collection:collection-ktx:1.2.0")

    // network
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    api("com.squareup.okhttp3:okhttp:4.11.0")
    api("com.github.liangjingkanji:Net:3.6.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    api("com.google.code.gson:gson:2.10.1")

    // listview
    api("com.github.liangjingkanji:BRV:1.5.0")

    // network request logging
    debugImplementation("com.github.chuckerteam.chucker:library:3.5.2")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:3.5.2")

    //image
    api("com.github.bumptech.glide:glide:4.15.1")

    // cache mmkv
    api("com.tencent:mmkv:1.3.0")

}