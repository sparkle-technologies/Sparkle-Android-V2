package com.cyberflow.sparkle.login.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.cyberflow.base.util.dp2px
import com.cyberflow.sparkle.R

class ShadowImgButton : ConstraintLayout {

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
    private var src: Int = 0
    private fun attributes(attrs: AttributeSet?) {
        val mTypedArray = context.obtainStyledAttributes(
            attrs,
            com.cyberflow.base.resources.R.styleable.shadowImgButton
        )
        distance = dp2px(
            mTypedArray.getDimension(
                com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_shadow_distance,
                2.0f
            )
        )

        src = mTypedArray.getResourceId(
            com.cyberflow.base.resources.R.styleable.shadowImgButton_view_img_src,
            com.cyberflow.base.resources.R.drawable.login_ic_twitter
        )

        mTypedArray.recycle()
    }

    private var nextButtonShadow: ImageView? = null
    private var nextButtonShadowTextView: ImageView? = null
    private var nextButton: ImageButton? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.widget_shadow_img, this, true)
        nextButtonShadow = findViewById(R.id.nextButtonShadow)
        nextButtonShadowTextView = findViewById(R.id.nextButtonShadowTextView)
        nextButton = findViewById(R.id.nextButton)

        val layoutParams1 = nextButtonShadow?.layoutParams as LayoutParams
        layoutParams1.setMargins(distance, distance, 0, 0)
        nextButtonShadow?.layoutParams = layoutParams1

        val layoutParams2 = nextButton?.layoutParams as LayoutParams
        layoutParams2.setMargins(0, 0, distance, distance)
        nextButton?.layoutParams = layoutParams2

        nextButtonShadowTextView?.setImageResource(src)
        nextButton?.setImageResource(src)

        nextButton?.setOnClickListener {
            listener?.clicked()
        }

        nextButton?.setOnTouchListener { v, motionEvent ->
            staticButtonTouchAnim(
                motionEvent,
                nextButtonShadow,
                nextButtonShadowTextView,
                nextButton
            )
            false
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