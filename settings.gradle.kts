pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
    }
}

rootProject.name = "Sparkle"
include(":app")
include(":base")

// wallet-connector
include(":core")
include(":sdkdapp")
include(":dapp")

project(":core").projectDir = File("wallet_connector/core")
project(":sdkdapp").projectDir = File("wallet_connector/sdkdapp")
project(":dapp").projectDir = File("wallet_connector/dapp")
