package com.cyberflow.base.util

class ConstantGlobal {
    companion object{

        //replace im api key   1. module=chat / build.gradle
        const val PRO = false   // pro or test version  ↑↑↑↑↑↑


        // 1. for Cora open uid
        const val CORA_OPEN_UID_PRO = "d7566fe1-4a39-4484-93f2-8fec3489efbe"
        const val CORA_OPEN_UID_DEV = "eebe94a3-fb8d-403f-9696-6be1a9e43eb3"
        // https://master.sparkle.fun/traveler/57fcd670-d993-4e28-a0dc-81b1c308382a
        const val SHARE_BODY = "https://master.sparkle.fun/traveler/"
        fun getCoraOpenUid() = if(PRO) CORA_OPEN_UID_PRO else CORA_OPEN_UID_DEV


        // 2. for IM app key
        const val IM_APP_KEY_PRO = "1183231031159952#sparkle"
        const val IM_APP_KEY_DEV = "1111230615161307#sparkletest"
        fun IM_APP_KEY() = if(PRO) IM_APP_KEY_PRO else IM_APP_KEY_DEV
    }
}