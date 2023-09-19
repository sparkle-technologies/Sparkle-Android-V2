package com.cyberflow.base.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.cyberflow.base.model.DailyHoroScopeData
import com.cyberflow.base.model.LoginResponseData
import com.cyberflow.base.model.UserAvatarImages
import com.cyberflow.base.net.GsonConverter
import com.cyberflow.base.model.UserInfo
import com.tencent.mmkv.MMKV


object CacheUtil {

    private const val ID = "i_love_sparkle_v1"
    private const val USERINFO = "sparkle_user_info"
    private const val NATIVE_MIX_IMGS = "sparkle_native_mix_imgs"
    const val WALLET_NAME = "sparkle_wallet_name"
    private const val DAILY_HOROSCOPE_INFO = "sparkle_daily_horoscope_info"

    const val UNIPASS_PUBK = "sparkle_unipass_pubk"
    const val UNIPASS_PRIK = "sparkle_unipass_prik"

    const val LOGIN_METHOD = "sparkle_login_method"


    fun getUserInfo(): LoginResponseData? {
        val kv = getMMKV()
        val userStr = kv.decodeString(USERINFO)
        return if (TextUtils.isEmpty(userStr)) {
            null
        } else {
            GsonConverter.gson.fromJson<LoginResponseData>(userStr, LoginResponseData::class.java)
        }
    }

    fun getSimpleUserInfo(): UserInfo? {
        val result = UserInfo()
        getUserInfo()?.apply {
            if (user == null) {
                return null
            }
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
            Log.e("TAG", "setUserInfo:obj is null")
            kv.encode(USERINFO, "")
        } else {
            val json = GsonConverter.gson.toJson(obj)
            Log.e("TAG", "setUserInfo: $obj")
            kv.encode(USERINFO, json)
        }
    }

    /*fun setHoroscopeData(obj: DailyHoroScopeData?) {
        val kv = getMMKV()
        if (obj == null) {
            Log.e("TAG", "setHoroscopeData:obj is null" )
            kv.encode(DAILY_HOROSCOPE_INFO, "")
        } else {
            val json = GsonConverter.gson.toJson(obj)
            Log.e("TAG", "setHoroscopeData: $obj" )
            kv.encode(DAILY_HOROSCOPE_INFO, json)
        }
    }

    fun getHoroscopeData(): DailyHoroScopeData? {
        val kv = getMMKV()
        val str = kv.decodeString(DAILY_HOROSCOPE_INFO)
        return if (TextUtils.isEmpty(str)) {
            null
        } else {
            GsonConverter.gson.fromJson<DailyHoroScopeData>(str, DailyHoroScopeData::class.java)
        }
    }*/


    fun isLoggedInAndHasUserInfoCompleted(): Boolean {
        getUserInfo()?.apply {
            if (user == null) {
                return false
            }
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