package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityCompatibilityRelationBinding
import com.cyberflow.sparkle.databinding.ItemCompatibilityRelationBinding
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import kotlinx.serialization.Serializable

class CompatibilityRelationAct : BaseDBAct<BaseViewModel, ActivityCompatibilityRelationBinding>() {

    companion object {

        fun go(context: Context) {
            val intent = Intent(context, CompatibilityRelationAct::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mDataBinding.llBack.setOnClickListener {
            finish()
        }
        mDataBinding.rv.linear().divider {
            orientation = DividerOrientation.VERTICAL
            setDivider(16, true)
        }.setup {
            addType<RelationItem>(R.layout.item_compatibility_relation)
            onBind {
                getBinding<ItemCompatibilityRelationBinding>().apply {
                    item.setOnClickListener {
                        toastSuccess(getString(R.string.coming_soon))
                    }
                }
            }
        }.models = getFakeData()
    }

    private fun getFakeData(): List<RelationItem>? {
        val result = arrayListOf<RelationItem>()
        repeat(20) {
            val item = RelationItem()
            item.score = 80 + it
            item.relation = if (it % 2 == 0) "Fall in love" else "kill each other"
            result.add(item)
        }
        return result
    }

    override fun initData() {

    }
}

@Serializable
data class RelationItem(
    val myurl: String = "https://d3eazbkghql3cd.cloudfront.net/avatar/7b/7b091ed1-d45e-44ff-88dc-aa4802e434aa/avatar_native.png?versionId=0g457ODq_NUZnb_R5RIHyHAKCf555Bl0",
    val taurl: String = "https://d3eazbkghql3cd.cloudfront.net/avatar/7b/7b091ed1-d45e-44ff-88dc-aa4802e434aa/avatar_native.png?versionId=0g457ODq_NUZnb_R5RIHyHAKCf555Bl0",
    var name: String = "Tom and Jerry",
    var relation: String = "Fall in love and kill each other",
    var score: Int = 100,
    val overall: String = "overall"
)
