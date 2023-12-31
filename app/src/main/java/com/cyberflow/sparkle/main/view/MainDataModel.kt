package com.cyberflow.sparkle.main.view

import com.drake.brv.item.ItemHover
import com.drake.brv.item.ItemPosition

class HeaderModel(val title: String, override var itemHover: Boolean = true) : ItemHover

class OfficialModel(var names: List<String>, override var itemPosition: Int = 0) : ItemPosition

class OfficialCoraModel(var names: String, override var itemPosition: Int = 0) : ItemPosition

class FriendsModel(var names: List<Any>, override var itemPosition: Int = 0) : ItemPosition

class FriendMessageInfo(var avatar: String = "", var imageUrl: String = "", var nickname: String = "", var open_uid :String = "",  var bgColor: String = "#ffffff", var num: Int = 0, var gender: Int = 1, override var itemPosition: Int = 0): ItemPosition

class FriendsAddModel(override var itemPosition: Int = 0) : ItemPosition

class FriendsEmptyModel(override var itemPosition: Int = 0) : ItemPosition

class HoroscopeHeadItem(override var itemPosition: Int = 0) : ItemPosition

class EmptyItem(override var itemPosition: Int = 0) : ItemPosition

class HoroscopeItem(
    var name: String = "Pattern 1",
    var desc: String = "During this period of time, you will have great challenges no matter from the spiritual or material level change.",
    var line: Int = 0,
    override var itemPosition: Int = 0
) : ItemPosition