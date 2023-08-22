package com.cyberflow.base.bean

class LoginResponseData {

    var token: String? = null
    val id_token: String? = null
    val user = User()

    class LocationDetails {
        var longitude: Double? = 0.0
        var latitude: Double? = 0.0
        var location: String? = null
    }


    class Star_sign {
        var PlanetEnglish: String? = null
        var PlanetChinese: String? = null
        var SignEnglish: String? = null
        var SignChinese: String? = null
        var Desc: String? = null
        val Labels: List<String>? = null
        var seq = 0
    }

    class Nft_list {
        var url: String = ""
        var sequence: Int? = null
    }

    class Bind_list
    class User {
        var nick: String? = null
        var gender = 0
        var birthdate: String? = null
        var birth_time: String? = null
        var birthplace_info: LocationDetails? = null
        var avatar: String? = null
        var code: String? = null
        var signature: String? = null
        var star_sign: List<Star_sign>? = null
        var nft_list: List<Nft_list>? = null
        var nft_url_list: List<String>? = null
        var default_nft_list: List<Nft_list>? = null
        var open_uid: String? = null
        var location_info: LocationDetails? = null
        var bind_list: List<String>? = null
        var label_list: List<String>? = null
        var wallet_address: String? =
            null     // 0x0c42ad43beedace4927e1065c10c776f2c604b5c    signIn 接口才会有
        var wallet: String? =
            null            //  0x0c4...4b5c                                  share  接口才会有
        var task_completed: Boolean = false
    }


    companion object {
        fun getNftListString(response: LoginResponseData): String {
            return response.user?.nft_list.orEmpty().joinToString(",") {
                it.url
            }
        }

        //0x0c42ad43beedace4927e1065c10c776f2c604b5c   0x0c4...4b5c
        fun getAddressShort(wallet_address: String?): String {
            return wallet_address.orEmpty()?.let {
                if (it.length > 40) {
                    it.substring(0, 5).plus("...").plus(it.substring(it.length - 4))
                } else {
                    ""
                }
            }.orEmpty()
        }
    }
}
