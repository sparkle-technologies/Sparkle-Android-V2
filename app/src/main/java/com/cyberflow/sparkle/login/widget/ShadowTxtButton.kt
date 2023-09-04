package com.cyberflow.sparkle.login.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.R

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
    private var txt_disable_color: Int = 0
    private var bg: Int = 0          // button background drawable, default is R.drawable.button_start
    private var bgDisable: Int = 0
    private var disable: Boolean = false
    private fun attributes(attrs: AttributeSet?) {
        val mTypedArray = context.obtainStyledAttributes(
            attrs,
            com.cyberflow.base.resources.R.styleable.shadowButton
        )
        distance = dp2px(
            mTypedArray.getDimension(
                com.cyberflow.base.resources.R.styleable.shadowButton_view_shadow_distance,
                2.0f
            )
        )
        txt = mTypedArray.getString(com.cyberflow.base.resources.R.styleable.shadowButton_view_text).orEmpty()

        txt_color = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_text_color, com.cyberflow.base.resources.R.color.almost_black)

        txt_disable_color = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_text_disable_color, com.cyberflow.base.resources.R.color.color_7D7D80)

        bg = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_bg, com.cyberflow.base.resources.R.drawable.register_btn_next)

        bgDisable = mTypedArray.getResourceId(com.cyberflow.base.resources.R.styleable.shadowButton_view_disable_bg, com.cyberflow.base.resources.R.drawable.register_btn_next_disable)

        disable = mTypedArray.getBoolean(com.cyberflow.base.resources.R.styleable.shadowButton_view_disable, false)

        mTypedArray.recycle()
    }

    private var nextButtonShadow: ImageView? = null
    private var nextButtonShadowTextView: TextView? = null
    private var nextButton: ImageButton? = null
    private var nextButtonTextView: TextView? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.widget_shadow_button, this, true)
        nextButtonShadow = findViewById(R.id.nextButtonShadow)
        nextButtonShadowTextView = findViewById(R.id.nextButtonShadowTextView)
        nextButton = findViewById(R.id.nextButton)
        nextButtonTextView = findViewById(R.id.nextButtonTextView)

        val layoutParams1 = nextButtonShadow?.layoutParams as LayoutParams
        layoutParams1.setMargins(distance, distance, 0, 0)
        nextButtonShadow?.layoutParams = layoutParams1

        val layoutParams2 = nextButton?.layoutParams as LayoutParams
        layoutParams2.setMargins(0, 0, distance, distance)
        nextButton?.layoutParams = layoutParams2

        disableBg(disable)

        nextButtonShadowTextView?.text = txt
        nextButtonTextView?.text = txt

        nextButton?.setOnClickListener {
            listener?.clicked(disable)
        }

        nextButton?.setOnTouchListener { v, motionEvent ->
            staticButtonTouchAnim(
                motionEvent,
                nextButton,
                nextButtonShadow,
                nextButtonTextView,
                if(disable) bgDisable else bg,
                com.cyberflow.base.resources.R.drawable.button_start_shadow
            )
            false
        }
    }

    fun disableBg(disable: Boolean){
        this.disable = disable
        if(disable){
            txt_disable_color.apply {
                nextButtonShadowTextView?.setTextColor(this)
                nextButtonTextView?.setTextColor(this)
            }
            nextButton?.setImageResource(bgDisable)
        }else{
            txt_color.apply {
                nextButtonShadowTextView?.setTextColor(this)
                nextButtonTextView?.setTextColor(this)
            }
            nextButton?.setImageResource(bg)
        }
    }

    interface ShadowClickListener{
        fun clicked(disable: Boolean = false)
    }

    private var listener: ShadowClickListener? = null
    fun setClickListener(listener: ShadowClickListener){
        this.listener = listener
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