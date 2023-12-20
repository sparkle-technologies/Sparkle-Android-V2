package com.cyberflow.base.util

class ConstantGlobal {
    companion object{

        // cora open uid
        // request host url
        // im api key   1. module=chat / build.gradle     2. DemoHelper.APP_KEY_TEST
        const val PRO = true   // pro or test version

        const val CORA_OPEN_UID_PRO = "d7566fe1-4a39-4484-93f2-8fec3489efbe"
        const val CORA_OPEN_UID_DEV = "eebe94a3-fb8d-403f-9696-6be1a9e43eb3"
        const val SHARE_BODY = "https://www.sparkle.fun/traveler/"
        fun getCoraOpenUid() = if(PRO) CORA_OPEN_UID_PRO else CORA_OPEN_UID_DEV
    }
}