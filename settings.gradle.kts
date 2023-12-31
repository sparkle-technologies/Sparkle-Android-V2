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

        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }

        maven { url = uri("https://developer.huawei.com/repo/") }

        maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
    }
}

rootProject.name = "Sparkle"
include(":app")
include(":base")
include(":base_resources")
include(":base_widget")

// IM
include(":chat")
include(":ease-im-kit")
include(":selector")

project(":chat").projectDir = File("im/chat")
project(":ease-im-kit").projectDir = File("im/ease-im-kit")
project(":selector").projectDir = File("im/selector")

// wallet-connector
include(":core")
include(":sdkdapp")
include(":dapp")

project(":core").projectDir = File("wallet_connector/core")
project(":sdkdapp").projectDir = File("wallet_connector/sdkdapp")
project(":dapp").projectDir = File("wallet_connector/dapp")

// for flutter import
apply(from = "flutter_settings.gradle")

/**
 * 开发规范
 *  所有资源 图片 字体大小 颜色 尺寸都放 base_resources   一句话 所有静态资源都放这
 *  所有工具类  架构  数据实体  都放进 base              一句话 所有处理数据都逻辑都放这
 *  app 里面只会有具体的业务逻辑 不再分包 widget/自定义view和具体的业务放在一起
 *      例如 login/{viewmodel, view, widget }         一句话 所有UI业务逻辑都放这
 */


