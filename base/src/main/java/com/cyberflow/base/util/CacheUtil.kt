package com.cyberflow.base.util

import android.content.Context
import android.text.TextUtils
import com.cyberflow.base.bean.LoginResponseData
import com.cyberflow.base.bean.UserAvatarImages
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.bean.UserInfo
import com.tencent.mmkv.MMKV


object CacheUtil {

    private const val ID = "i_love_sparkle_v1"
    private const val USERINFO = "sparkle_user_info"
    private const val NATIVE_MIX_IMGS = "sparkle_native_mix_imgs"
    const val WALLET_NAME = "sparkle_wallet_name"


    fun getUserInfo(): LoginResponseData? {
        val kv = getMMKV()
        val userStr = kv.decodeString(USERINFO)
        return if (TextUtils.isEmpty(userStr)) {
            null
        } else {
            GsonConverter.gson.fromJson<LoginResponseData>(userStr, LoginResponseData::class.java)
        }
    }

    fun getSimpleUserInfo(): UserInfo {
        val result = UserInfo()
        getUserInfo()?.apply {
            result.gender = user.gender
            result.birthdate = user.birthdate
            result.birth_time = user.birth_time
            result.nick = user.nick
            result.signature = user.signature
            result.birthplace_info = user.birthplace_info
            result.location_info = user.location_info
            return result
        }
        return result
    }

    fun setUserInfo(obj: LoginResponseData?) {
        val kv = getMMKV()
        if (obj == null) {
            kv.encode(USERINFO, "")
        } else {
            val json = GsonConverter.gson.toJson(obj)
            kv.encode(USERINFO, json)
        }
    }

    fun isLoggedInAndHasUserInfoCompleted(): Boolean {
        getUserInfo()?.apply {
            val necessary1 = token?.isNotEmpty()
            val necessary2 = user.open_uid?.isNotEmpty()
            val necessary3 = user.nft_list?.any { it.url.isNotEmpty() }
            return necessary1 == true && necessary2 == true && necessary3 == true
        }
        return false
    }

    fun getNativeImgs(): UserAvatarImages? {
        val kv = getMMKV()
        val userStr = kv.decodeString(NATIVE_MIX_IMGS)
        return if (TextUtils.isEmpty(userStr)) {
            null
        } else {
            GsonConverter.gson.fromJson<UserAvatarImages>(userStr, UserAvatarImages::class.java)
        }
    }

    fun setNativeImgs(obj: UserAvatarImages?) {
        val kv = getMMKV()
        if (obj == null) {
            kv.encode(NATIVE_MIX_IMGS, "")
        } else {
            val json = GsonConverter.gson.toJson(obj)
            kv.encode(NATIVE_MIX_IMGS, json)
        }
    }


    fun init(context: Context) {
        MMKV.initialize(context)
    }

    private fun getMMKV(): MMKV {
        return MMKV.mmkvWithID(ID)
    }

    fun savaString(key: String, value: String): Boolean {
        return getMMKV().encode(key, value)
    }

    fun getString(key: String, defaultValue: String = ""): String? {
        return getMMKV().decodeString(key, defaultValue)
    }

    fun saveInt(key: String, value: Int): Boolean {
        return getMMKV().encode(key, value)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getMMKV().getInt(key, defaultValue)
    }


    fun saveBool(key: String, value: Boolean): Boolean {
        return getMMKV().encode(key, value)
    }

    fun getBool(key: String, defaultValue: Boolean = false): Boolean {
        return getMMKV().decodeBool(key, defaultValue)
    }

    fun saveFloat(key: String, value: Float): Boolean {
        return getMMKV().encode(key, value)
    }

    fun getFloat(key: String, defaultValue: Float = 0.0f): Float {
        return getMMKV().decodeFloat(key, defaultValue)
    }

    fun logout() {
        getMMKV().removeValuesForKeys(arrayOf(USERINFO, NATIVE_MIX_IMGS, WALLET_NAME))
    }
}