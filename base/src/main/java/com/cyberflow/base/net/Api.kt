package com.cyberflow.base.net

object Api {

    const val HOST_PRO = "https://api.sparkle.fun/"
    const val HOST = "https://api-test.sparkle.fun/"
    const val SIGN_IN = "stars-cipher/signIn"
    const val COMPLETE_INFO = "stars-cipher/user/complete_info"
    const val DAILY_HOROSCOPE = "stars-cipher/user/daily_horoscope"
    const val WEEKLY_HOROSCOPE = "stars-cipher/user/weekly_horoscope"
    const val MONTHLY_HOROSCOPE = "stars-cipher/user/monthly_horoscope"
    const val YEARLY_HOROSCOPE = "stars-cipher/user/yearly_horoscope"

    const val IM_USER_SEARCH = "stars-cipher/search"
    const val IM_QUESTIONS = "sparkle-llm/tarot/get-question-list"
    const val IM_CHAT = "sparkle-llm/tarot/chat"


    const val GET_IMAGE_URLS = "stars-cipher/get_user_image_url"
    const val USER_DETAIL = "stars-cipher/user/detail"
    const val LOGIN_UNBIND = "stars-cipher/user/unbinding"
    const val LOGIN_BIND = "stars-cipher/user/binding"

    const val BOND = "ticket/user/synastry"
    const val BOND_DETAIL = "ticket/user/synastry_detail"
    const val RECOMMAND_FRIEND = "ticket/user/friends_recommend"

    // for IM friends relationship
    const val RELATIONSHIP_FRIEND_REQUEST = "relationship/user/send_friend_request"
    const val RELATIONSHIP_FRIEND_REJECT = "relationship/user/reject_friend_request"
    const val RELATIONSHIP_FRIEND_ACCEPT = "relationship/user/accept_friend_request"
    const val RELATIONSHIP_FRIEND_REQUEST_LIST = "relationship/user/get_friend_request_list"
    const val RELATIONSHIP_FRIEND_LIST = "relationship/user/get_friend_list"

    const val USER_COMPATIBILITY = "stars-cipher/user/compatibility"
    const val BOND_LIST = "stars-cipher/user/bond_list"


    const val SITE_MESSAGE = "sparkle-msg-notify/site_message"

}