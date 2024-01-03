package com.cyberflow.sparkle.profile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.cyberflow.base.act.BaseDBAct
import com.cyberflow.base.model.User
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.ActivityCompatibilityRelationBinding
import com.cyberflow.sparkle.databinding.ItemCompatibilityRelationBinding
import com.cyberflow.sparkle.profile.viewmodel.CompatibilityViewModel
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import kotlinx.serialization.Serializable

class CompatibilityRelationAct : BaseDBAct<CompatibilityViewModel, ActivityCompatibilityRelationBinding>() {

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
            animationRepeat = true
            setAnimation(AnimationType.SCALE)

            addType<RelationItem>(R.layout.item_compatibility_relation)
            onBind {
                getBinding<ItemCompatibilityRelationBinding>().apply {
                    item.setOnClickListener {
                        toastSuccess(getString(R.string.coming_soon))
                    }
                }
            }
        }
    }

    private var user: User?=null
    override fun initData() {
        user = CacheUtil.getUserInfo()?.user
        viewModel.bondData?.observe(this){
            if(it.bond_list.isNullOrEmpty()){
                mDataBinding.state.showEmpty()
            }else{
                mDataBinding.state.showContent()

                mDataBinding.rv.models = it.bond_list.map {
                    RelationItem(
                        myurl = user?.avatar.orEmpty(),
                        taurl = it.avatar,
                        name = "${user?.nick} & ${it.nick}",
                        relation = it.title,
                        score = it.score,
                        overall = getString(R.string.overall)
                    )
                }
            }
        }
        mDataBinding.state.apply {
            onError {
                findViewById<ShadowTxtButton>(R.id.btn).setClickListener(object :
                    ShadowTxtButton.ShadowClickListener {
                    override fun clicked(disable: Boolean) {
                        getData()
                    }
                })
            }
        }
        getData()
    }

    private fun getData(){
        mDataBinding.state.showLoading()
        mDataBinding.state.scope {
            viewModel.bondData.value = Post<Bond>(Api.BOND_LIST) {}.await()
        }
    }
}

@Serializable
data class Bond(
    val bond_list: List<BondListItem>? = null
)

@Serializable
data class BondListItem(
    val to_open_uid: String = "",
    val avatar: String = "",
    val nick: String = "",
    var title: String = "",
    var sun_sign: String = "",
    var score: Int = 0,
    val overall: String = "",
    var ranking: Int = 0,
    var from_ticket: Int = 0,
    var to_ticket: Int = 0
)

@Serializable
data class RelationItem(
    val myurl: String = "",
    val taurl: String = "",
    var name: String = "",
    var relation: String = "",
    var score: Int = 0,
    val overall: String = "overall"
)
