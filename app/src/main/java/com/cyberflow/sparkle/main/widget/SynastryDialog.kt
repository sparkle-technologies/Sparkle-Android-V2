package com.cyberflow.sparkle.main.widget

import android.animation.ValueAnimator
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cyberflow.base.model.BondDetail
import com.cyberflow.base.model.User
import com.cyberflow.base.net.Api
import com.cyberflow.base.resources.R
import com.cyberflow.base.util.CacheUtil
import com.cyberflow.sparkle.DBComponent
import com.cyberflow.sparkle.widget.ShadowTxtButton
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withMain
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.loader.AssetStreamLoader

class SynastryDialog {

    private var mFragment: Fragment? = null
    private var mDialog: Dialog? = null
    private var friend: User? = null

    constructor(c: Fragment, user: User?,   callback: Callback) {
        if (c == null || callback == null) {
            return
        }

        mFragment = c
        friend = user
        mCallback = callback
        initView()
        initData()
    }

    private var tvTitle: TextView? = null
    private var ivLeft: ImageView? = null
    private var ivRight: ImageView? = null
    private var tvScore: TextView? = null
    private var ivAnima: ImageView? = null
    private var btnReveal: ShadowTxtButton? = null

    private fun initView() {
        mDialog = Dialog(mFragment?.requireContext()!!, R.style.forward_dialog)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(com.cyberflow.sparkle.R.layout.dialog_synastry)
        mDialog?.setCancelable(false)
        mDialog?.setCanceledOnTouchOutside(false)
        val window = mDialog?.window
        if (window != null) {
            val lp = window.attributes
            lp.gravity = Gravity.CENTER_VERTICAL
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
//            window.setWindowAnimations(R.style.BottomDialog_Animation)
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//            )
        }

        mDialog?.apply {

            tvTitle = findViewById<TextView>(com.cyberflow.sparkle.R.id.tv_name)
            ivLeft = findViewById<ImageView>(com.cyberflow.sparkle.R.id.iv_left)
            ivRight = findViewById<ImageView>(com.cyberflow.sparkle.R.id.iv_right)
            tvScore = findViewById<TextView>(com.cyberflow.sparkle.R.id.tv_score)
            ivAnima = findViewById<ImageView>(com.cyberflow.sparkle.R.id.iv_anima)
            btnReveal = findViewById<ShadowTxtButton>(com.cyberflow.sparkle.R.id.btn_reveal)

            btnReveal?.setClickListener(
                object : ShadowTxtButton.ShadowClickListener {
                    override fun clicked(disable: Boolean) {
                        if(disable){

                            return
                        }
                        mCallback?.onSelected(detail)
                    }
                })
        }
    }

    interface Callback {
        fun onSelected(select: BondDetail?)
    }

    private var mCallback: Callback? = null

    private fun initAnim() {
        val asset = AssetStreamLoader(mFragment?.requireActivity(), "synastry.png")
        APNGDrawable(asset).apply {
            setLoopLimit(-1)
            ivAnima?.setImageDrawable(this@apply)
            start()
        }
    }

    fun show() {
        initAnim()
        showData()
        mDialog?.show()
    }

    fun onDestroy() {
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }

    private fun showData(){

        CacheUtil.getUserInfo()?.user?.apply {
            ivLeft?.let { DBComponent.loadAvatar(it, avatar, gender) }
        }

        friend?.apply {
            ivRight?.let { DBComponent.loadAvatar(it, avatar, gender) }
        }

        requestBond()
    }

    private fun initData() {

    }

    private fun requestBond() {
        mFragment?.apply {
            scopeNetLife {
                val data = Post<String>(Api.BOND) {
                    json("open_uid" to friend?.open_uid.orEmpty())
                }.await()
                data?.let {
                    requestBondDetail()
                }
            }
        }
    }

    private fun requestBondDetail() {
        mFragment?.apply {
            scopeNetLife {
                val data = Post<BondDetail>(Api.BOND_DETAIL) {
                    json("open_uid" to friend?.open_uid.orEmpty())
                }.await()
                data?.let {
                    withMain {
                        showBondDetailInfo(it)
                    }
                }
            }
        }
    }

    private var detail: BondDetail? = null

    private fun showBondDetailInfo(bondDetail: BondDetail) {
        detail = bondDetail
        btnReveal?.disableBg(false)
        tvTitle?.text = bondDetail.title
        val dd = bondDetail.total_score
        ValueAnimator.ofInt(0, dd).apply {
            duration = (12 * dd).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener {
                tvScore?.text = it.animatedValue.toString()
            }
            start()
        }
    }
}
