package com.cyberflow.sparkle.main.view

import com.drake.brv.item.ItemPosition

class OfficialModel(var name: String = "Cora-Official", override var itemPosition: Int = 0) :
    ItemPosition

class ContactModel(var name: String = "King-Official", override var itemPosition: Int = 0) :
    ItemPosition


class HoroscopeHeadItem( override var itemPosition: Int = 0) :
    ItemPosition

class EmptyItem( override var itemPosition: Int = 0) :
    ItemPosition

class HoroscopeItem(
    var name: String = "Pattern 1",
    var desc: String = "During this period of time, you will have great challenges no matter from the spiritual or material level change.",
    var line: Int = 0,
    override var itemPosition: Int = 0
) : ItemPosition