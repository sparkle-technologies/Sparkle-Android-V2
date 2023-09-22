package com.cyberflow.sparkle.main.view

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.cyberflow.base.fragment.BaseDBFragment
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.R
import com.cyberflow.sparkle.databinding.FragmentMainRightBinding
import com.cyberflow.sparkle.databinding.ItemFriendsFeedEmptyBinding
import com.cyberflow.sparkle.databinding.MainFriendsFeedBinding
import com.cyberflow.sparkle.databinding.MainOfficialBinding
import com.cyberflow.sparkle.login.widget.ShadowTxtButton
import com.cyberflow.sparkle.main.viewmodel.MainViewModel
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class MainRightFragment : BaseDBFragment<BaseViewModel, FragmentMainRightBinding>() {

    override fun initData() {

    }

    private var actVm: MainViewModel? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        actVm = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {


        initListView()
    }

    private fun initListView() {
        mDatabind.rv.linear().setup {
            addType<HeaderModel>(R.layout.item_hover_header)
            addType<OfficialModel>(R.layout.main_official)
            addType<FriendsModel>(R.layout.main_friends_feed)
            addType<FriendsEmptyModel>(R.layout.item_friends_feed_empty)
            onCreate {
                when (itemViewType) {
                    R.layout.main_official -> {
                        getBinding<MainOfficialBinding>().rv.divider {
                            orientation = DividerOrientation.VERTICAL
                            setDivider(10, true)
                        }.setup {
                            addType<String>(R.layout.item_official)
                            onBind {
                                if (layoutPosition == 0)
                                    findView<ImageView>(R.id.iv).setImageResource(R.drawable.pic_cora)
                                else
                                    findView<ImageView>(R.id.iv).setImageResource(R.drawable.pic_king)
                            }
                            onClick(R.id.root) {
                                Snackbar.make(
                                    this.itemView,
                                    "click official",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                when (this.layoutPosition) {
                                    0 -> {
                                        mDatabind.rv.models = getData(true)
                                    }

                                    1 -> {
                                        mDatabind.rv.models = getData(false)
                                    }
                                }
                            }
                        }
                    }

                    R.layout.main_friends_feed -> {
                        getBinding<MainFriendsFeedBinding>().rv.divider {
                            orientation = DividerOrientation.HORIZONTAL
                            setDivider(14, true)
                        }.divider {
                            orientation = DividerOrientation.VERTICAL
                            setDivider(10, true)
                        }.setup {
                            addType<String>(R.layout.item_friends_feed)
                            addType<FriendsAddModel>(R.layout.item_friends_feed_add)
                            onClick(R.id.bg_new_friend) {
                                Snackbar.make(
                                    itemView,
                                    "TODO -->  go IM add friend",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    R.layout.item_friends_feed_empty -> {
                        getBinding<ItemFriendsFeedEmptyBinding>().btnAddFriend.setClickListener(
                            object : ShadowTxtButton.ShadowClickListener {
                                override fun clicked(disable: Boolean) {
                                    Snackbar.make(
                                        itemView,
                                        "TODO -->  go IM add friend",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                }
            }

            onBind {
                when (itemViewType) {
                    R.layout.main_official -> {
                        val model = getModel<OfficialModel>()
                        getBinding<MainOfficialBinding>().rv.models = model.names
                    }

                    R.layout.main_friends_feed -> {
                        val model = getModel<FriendsModel>()
                        getBinding<MainFriendsFeedBinding>().rv.models = model.names
                    }
                }
            }
        }

        mDatabind.page.setEnableLoadMore(false)
        mDatabind.page.onRefresh {
            postDelayed({       // simulate request data for 2s
                mDatabind.rv.models = getData()
                mDatabind.page.finishRefresh()
            }, 2000)
        }
        refresh()
    }


    fun refresh() {
        mDatabind.page.autoRefresh()
    }

    private fun getData(empty: Boolean = false): List<Any> {
        val r = Random.nextInt(10)
        return listOf(
            HeaderModel(title = "Official"),
            OfficialModel(arrayListOf("Cora-Official", "King-Official")),
            HeaderModel(title = "Friends Feed"),
            if (empty) FriendsEmptyModel() else
                FriendsModel(
                    arrayListOf(
                        "Cora-$r",
                        "King-$r",
                        "Cora-$r",

                        "King-$r",
                        "Cora-$r",
                        "King-$r",

                        "Cora-$r",
                        "King-$r",
                        "Cora-$r",

                        "King-$r",
                        "Cora-$r",
                        "King-$r",

                        "Cora-$r",
                        "King-$r",
                        FriendsAddModel()
                    )
                )
        )
    }
}