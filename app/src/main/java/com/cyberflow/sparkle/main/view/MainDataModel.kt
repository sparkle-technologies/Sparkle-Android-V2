package com.cyberflow.sparkle.main.view

import com.drake.brv.item.ItemPosition

class HeaderModel(val title: String, override var itemPosition: Int = 0) : ItemPosition

class OfficialModel(var names: List<String>, override var itemPosition: Int = 0) : ItemPosition

class FriendsModel(var names: List<String>, override var itemPosition: Int = 0) : ItemPosition

class FriendsEmptyModel(override var itemPosition: Int = 0) : ItemPosition

class HoroscopeHeadItem(override var itemPosition: Int = 0) : ItemPosition

class EmptyItem(override var itemPosition: Int = 0) : ItemPosition

class HoroscopeItem(
    var name: String = "Pattern 1",
    var desc: String = "During this period of time, you will have great challenges no matter from the spiritual or material level change.",
    var line: Int = 0,
    override var itemPosition: Int = 0
) : ItemPosition