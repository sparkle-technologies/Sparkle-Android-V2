plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.cyberflow.sparkle.chat"
    compileSdk = 33

    defaultConfig {
//        applicationId = "com.cyberflow.sparkle.chat"
        minSdk = 24

//        versionCode = 1
//        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
                arguments["room.expandProjection"] = "true"
            }
        }

        buildConfigField("String", "APP_SERVER_PROTOCOL", "\"https\"")
        buildConfigField("String", "APP_SERVER_DOMAIN", "\"a1.easemob.com\"")
        buildConfigField("String", "APP_BASE_USER", "\"/inside/app/user/\"")
        buildConfigField("String", "APP_SERVER_LOGIN", "\"login/V2\"")
        buildConfigField("String", "APP_SERVER_REGISTER", "\"register\"")
        buildConfigField("String", "APP_SERVE_CHECK_RESET", "\"reset/password\"")
        buildConfigField("String", "APP_SERVE_CHANGE_PWD", "\"/password\"")
        buildConfigField("String", "APP_SEND_SMS_FROM_SERVER", "\"/inside/app/sms/send\"")
        buildConfigField("String", "APP_VERIFICATION_CODE", "\"/inside/app/image/\"")

        manifestPlaceholders["EASEMOB_APPKEY"] = "1111230615161307#demo"
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(project(mapOf("path" to ":base")))
    implementation(project(mapOf("path" to ":base_resources")))
    api(project(mapOf("path" to ":ease-im-kit")))

    api("io.hyphenate:hyphenate-chat:4.0.3")
    implementation("androidx.room:room-runtime:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")

    implementation("com.parse:parse-android:1.13.1")

    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    implementation("pub.devrel:easypermissions:3.0.0")

    kapt("cn.therouter:apt:1.2.0-rc1")
    implementation("cn.therouter:router:1.2.0-rc1")

}