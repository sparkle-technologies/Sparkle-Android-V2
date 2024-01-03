package com.cyberflow.sparkle.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

class ShadowTxtButton : ConstraintLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attributes(attrs)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        attributes(attrs)
        initView()
    }

    private var distance: Int = 0     // shadow width/height
    private var txt: String = ""     // button text
    private var txt_color: Int = 0
    private var txt_size: Float = -1f
    private var txt_bold: Boolean = true
    private var txt_disable_color: Int = 0
    private var bg: Int = 0          // button background drawable, default is R.drawable.button_start
    private var bgDisable: Int = 0
    private var disable: Boolean = false
    private var viewClickEnable: Boolean = true
    private var drawableLeft: Int = -1

    private fun attributes(attrs: AttributeSet?) {
        val mTypedArray = context.obtainStyledAttributes(
            attrs,
            com.cyberflow.base.resources.R.styleable.shadowButton
        )
        distance = dp2px(
            context,
            mTypedArray.getDimension(
                com.cyberflow.base.resources.R.styleable.shadowButton_view_shadow_distance,
                2.0f
            )
        )
        txt = mTypedArray.getString(com.cyberflow.base.resources.R.styleable.shadowButton_view_text).orEmpty()

        txt_color = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_text_color, com.cyberflow.base.resources.R.color.almost_black)

        txt_size = mTypedArray.getDimension(com.cyberflow.base.resources.R.styleable.shadowButton_view_text_size, -1f)

        txt_bold = mTypedArray.getBoolean(com.cyberflow.base.resources.R.styleable.shadowButton_view_text_bold, true)

        txt_disable_color = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_text_disable_color, com.cyberflow.base.resources.R.color.color_7D7D80)

        bg = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_bg, com.cyberflow.base.resources.R.drawable.register_btn_next)

        bgDisable = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_disable_bg, com.cyberflow.base.resources.R.drawable.register_btn_next_disable)

        disable = mTypedArray.getBoolean(com.cyberflow.base.resources.R.styleable.shadowButton_view_disable, false)

        viewClickEnable = mTypedArray.getBoolean(com.cyberflow.base.resources.R.styleable.shadowButton_view_click_enable, true)

        drawableLeft = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_left_drawable, -1)

        mTypedArray.recycle()
    }

    private var ivBgShadow: ImageView? = null
    private var tvNormal: TextView? = null
    private var ivClicking: ImageButton? = null
    private var tvClicking: TextView? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.widget_shadow_button, this, true)
        ivBgShadow = findViewById(R.id.ivBgShadow)
        tvNormal = findViewById(R.id.tvNormal)
        ivClicking = findViewById(R.id.ivClicking)
        tvClicking = findViewById(R.id.tvClicking)

        val layoutParams1 = ivBgShadow?.layoutParams as LayoutParams
        layoutParams1.setMargins(0, distance, 0, 0)
        ivBgShadow?.layoutParams = layoutParams1

        val layoutParams2 = ivClicking?.layoutParams as LayoutParams
        layoutParams2.setMargins(0, 0, 0, distance)
        ivClicking?.layoutParams = layoutParams2

        disableBg(disable)

        tvNormal?.text = txt
        tvClicking?.text = txt

        if(drawableLeft > 0){
            tvNormal?.apply {
                setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0)
                compoundDrawablePadding = resources.getDimension(com.cyberflow.base.resources.R.dimen.dp_8).toInt()
            }

            tvClicking?.apply {
                setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0)
                compoundDrawablePadding = resources.getDimension(com.cyberflow.base.resources.R.dimen.dp_8).toInt()
            }
        }

        if(txt_size > 0){
//            Log.e("TAG", "============================= txt_size=$txt_size"  )
            tvNormal?.setTextSize(TypedValue.COMPLEX_UNIT_PX, txt_size)
            tvClicking?.setTextSize(TypedValue.COMPLEX_UNIT_PX, txt_size)
        }

        if(txt_bold){
            ResourcesCompat.getFont(context, com.cyberflow.base.resources.R.font.poppins_semibold).apply {
                tvNormal?.typeface = this
                tvClicking?.typeface = this
            }
        }

        if(viewClickEnable){
            ivClicking?.setOnClickListener {
                listener?.clicked(disable)
            }

            ivClicking?.setOnTouchListener { v, motionEvent ->
                staticButtonTouchAnim(
                    motionEvent,
                    ivClicking,
                    ivBgShadow,
                    tvClicking,
                    if(disable) bgDisable else bg,
                    com.cyberflow.base.resources.R.drawable.button_start_shadow
                )
                false
            }
        }
    }

    fun disableBg(disable: Boolean){
//        Log.e("TAG", "disableBg: disable=$disable", )
        this.disable = disable
        if(disable){
            ResourcesCompat.getColor(resources, txt_disable_color, null).apply {
                tvNormal?.setTextColor(this)
                tvClicking?.setTextColor(this)
            }
            ivClicking?.setImageResource(bgDisable)
        }else{
            ResourcesCompat.getColor(resources, txt_color, null).apply {
                tvNormal?.setTextColor(this)
                tvClicking?.setTextColor(this)
            }
            ivClicking?.setImageResource(bg)
        }
    }

    interface ShadowClickListener{
        fun clicked(disable: Boolean = false)
    }

    private var listener: ShadowClickListener? = null
    fun setClickListener(listener: ShadowClickListener){
        this.listener = listener
    }

    fun setViewTxt(txt: String){
        this.txt = txt
        tvNormal?.text = txt
        tvClicking?.text = txt
        invalidate()
    }

    private fun staticButtonTouchAnim(
        motionEvent: MotionEvent,
        button: ImageView?,
        buttonShadow: ImageView?,
        buttonText: TextView?,
        buttonBackground: Int,
        buttonShadowBackground: Int
    ) {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                button?.visibility = View.INVISIBLE
                buttonText?.visibility = View.INVISIBLE
                buttonShadow?.setImageResource(buttonBackground)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                button?.visibility = View.VISIBLE
                buttonText?.visibility = View.VISIBLE
                buttonShadow?.setImageResource(buttonShadowBackground)
            }
        }
    }
}

fun dp2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}