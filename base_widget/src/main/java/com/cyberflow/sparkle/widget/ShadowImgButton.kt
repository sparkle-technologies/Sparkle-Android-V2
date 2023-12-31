package com.cyberflow.sparkle.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

class ShadowImgButton : ConstraintLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attributes(attrs)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attributes(attrs)
        initView()
    }

    private var distance: Int = 0     // shadow width/height

    private var src: Int = 0
    private var bg: Int = 0

    private var disable: Boolean = false

    private var srcDisable: Int = 0
    private var bgDisable: Int = 0

    private fun attributes(attrs: AttributeSet?) {
        val mTypedArray = context.obtainStyledAttributes(attrs, com.cyberflow.base.resources.R.styleable.shadowImgButton)
        distance = dp2px(context, mTypedArray.getDimension(com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_shadow_distance, 2.0f))

        src = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_src, 0)
        bg = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowImgButton_view_bg_src, 0)

        srcDisable = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_disable_src, 0)
        bgDisable = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_disable_bg, 0)

        disable = mTypedArray.getBoolean(com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_disable, false)
        mTypedArray.recycle()
    }

    private var ivBgShadow: ImageView? = null
    private var ivNormal: ImageView? = null
    private var ivClicking: ImageButton? = null


    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.widget_shadow_img, this, true)
        ivBgShadow = findViewById(R.id.ivBgShadow)
        ivNormal = findViewById(R.id.ivNormal)
        ivClicking = findViewById(R.id.ivClicking)

        val layoutParams1 = ivBgShadow?.layoutParams as LayoutParams
        layoutParams1.setMargins(0, distance, 0, 0)
        ivBgShadow?.layoutParams = layoutParams1

        val layoutParams2 = ivClicking?.layoutParams as LayoutParams
        layoutParams2.setMargins(0, 0, 0, distance)
        ivClicking?.layoutParams = layoutParams2

        if(disable){
            if(bgDisable != 0){
                ivBgShadow?.setImageResource(bgDisable)
            }
            updateSrc(srcDisable)
        }else{
            if (bg != 0) {
                ivBgShadow?.setImageResource(bg)
            }
            updateSrc(src)
        }

        ivClicking?.setOnClickListener {
            listener?.clicked()
        }

        ivClicking?.setOnTouchListener { v, motionEvent ->
            if(disable){
                true
            }else{
                staticButtonTouchAnim(
                    motionEvent,
                    ivBgShadow,
                    ivNormal,
                    ivClicking
                )
                false
            }
        }
    }

    fun disableBg(disable: Boolean){
        this.disable = disable
        if(disable){
            if(bgDisable != 0){
                ivBgShadow?.setImageResource(bgDisable)
            }
            updateSrc(srcDisable)
        }else{
            if (bg != 0) {
                ivBgShadow?.setImageResource(bg)
            }
            updateSrc(src)
        }
    }

    fun updateSrc(pic: Int) {
        if (pic != 0) {
            if(disable){
                this.srcDisable = pic
            }else{
                this.src = pic
            }
            ivNormal?.setImageResource(pic)
            ivClicking?.setImageResource(pic)
            invalidate()
        }
    }

    interface ShadowClickListener {
        fun clicked()
    }

    private var listener: ShadowClickListener? = null
    fun setClickListener(listener: ShadowClickListener) {
        this.listener = listener
    }

    private fun staticButtonTouchAnim(
        motionEvent: MotionEvent,
        shadow: ImageView?,
        shadowBtn: ImageView?,
        btn: ImageButton?,
    ) {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                shadow?.visibility = View.INVISIBLE
                shadowBtn?.visibility = View.VISIBLE
                btn?.visibility = View.INVISIBLE
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                shadow?.visibility = View.VISIBLE
                shadowBtn?.visibility = View.INVISIBLE
                btn?.visibility = View.VISIBLE
            }
        }
    }
}