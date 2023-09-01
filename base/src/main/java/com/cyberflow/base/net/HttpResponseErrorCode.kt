package com.cyberflow.base.net


object HttpResponseErrorCode {

    const val ErrUnknown = 10000 // 未知错误
    const val ErrUserAuthNotExist = 10101 // 鉴权信息不存在
    const val ErrInvalidPassword = 10102 // 密码错误
    const val ErrInvalidPlatform = 10103 // 不支持的三方鉴权平台
    const val ErrNoSignIn = 10104 // 未登录
    const val ErrTokenExpired = 10105 // 授权已过期
    const val ErrAuthorize = 10106 // 获取授权信息失败

    const val ErrInvalidSignInType = 10201 // 不支持的登录方式

    const val ErrUserNotExist = 20101 // 用户不存在
    const val ErrIncompleteUserInfo = 20102 // 用户信息不完整

    const val ErrInvalidParam = 30101 // 请求参数错误
    const val ErrRecordNotExist = 30102 // 记录不存在
    const val ErrParseData = 30103 // 数据解析错误

    const val ErrDbError = 40101 // 数据库错误

    const val ErrThirdParty = 50001 // 第三方错误

    public fun handleErrorCode(code: Int): String {
        return when (code) {
            ErrUserAuthNotExist -> "user auth not exist" // "鉴权信息不存在"
            ErrInvalidPassword -> "invalid password" // ""密码错误"
            ErrInvalidPlatform -> "invalid platform" //  "不支持的三方鉴权平台"
            ErrNoSignIn -> "not sign in" // "未登录"
            ErrTokenExpired -> "token expired" // ""授权已过期"
            ErrAuthorize -> "fail get authorize information" // "获取授权信息失败"
            ErrInvalidSignInType -> "unsupported sign in type" // "不支持的登录方式"
            ErrUserNotExist -> "user not exist" // ""用户不存在"
            ErrIncompleteUserInfo -> "user information missing" // ""用户信息不完整"
            ErrInvalidParam -> "request params invalid" // ""请求参数错误"
            ErrRecordNotExist -> "record not exist" // ""记录不存在"
            ErrParseData -> "fail parse data" // ""数据解析错误"
            ErrDbError -> "database error" //  "数据库错误"
            ErrThirdParty -> "third party failed" //  ""第三方错误"
            ErrUnknown -> "unknown error" //  "未知错误"
            else -> ""
        }
    }
}

