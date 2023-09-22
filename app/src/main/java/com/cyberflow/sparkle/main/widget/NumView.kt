package com.cyberflow.sparkle.main.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.cyberflow.base.util.sp2px
import kotlin.math.min

class NumView constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int, defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    var color = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var strokeColor = 0
    var strokeWidth = 0f
    var textSize = 0f
    var textColor = 0
    var num = ""
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, 0
    )

    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0, 0)

    constructor(context: Context) : this(context, null, 0, 0)

    init {
        val attrArray = context.obtainStyledAttributes(
            attrs,
            com.cyberflow.base.resources.R.styleable.NumView
        )
        color = attrArray.getColor(
            com.cyberflow.base.resources.R.styleable.NumView_num_view_color,
            Color.WHITE
        )
        strokeColor = attrArray.getColor(
            com.cyberflow.base.resources.R.styleable.NumView_num_view_strokeColor,
            Color.WHITE
        )
        strokeWidth = attrArray.getDimension(
            com.cyberflow.base.resources.R.styleable.NumView_num_view_strokeWidth,
            10.0f
        )
        textSize = attrArray.getDimension(
            com.cyberflow.base.resources.R.styleable.NumView_num_text_size,
            16.0f
        )

        textSize = sp2px(textSize).toFloat()

        textColor = attrArray.getColor(
            com.cyberflow.base.resources.R.styleable.NumView_num_text_color,
            Color.BLACK
        )

        attrArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (num.isEmpty() || num == "0") {
            visibility = INVISIBLE
            return
        } else {
            visibility = VISIBLE
        }

        // 1. 画大圈  实心
        paint.color = color
        paint.style = Paint.Style.FILL
        val radius: Float = (min(width, height) / 2).toFloat() - strokeWidth
        canvas?.drawCircle(width.toFloat() / 2, height.toFloat() / 2, radius, paint)

        //2. 画外面的
        paint.color = strokeColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        val strokeRadius = radius
        canvas?.drawCircle(width.toFloat() / 2, height.toFloat() / 2, strokeRadius, paint)

        // 3. 画里面的数字
        paint.color = textColor
        paint.textSize = textSize
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        val rect = Rect(0, 0, width, height)
        val metrics = paint.fontMetrics
        val distance = (metrics.bottom - metrics.top) / 2 - metrics.bottom
        val baseLine = rect.centerY() + distance
        canvas?.drawText(num, rect.centerX().toFloat(), baseLine, paint)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec))
    }

    private fun measureSize(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return (if (specMode == MeasureSpec.EXACTLY) specSize
        else (context.resources.displayMetrics.density * 50).toInt())
    }

    fun setNum(i: Int) {
        num = i.toString()
        invalidate()
    }
}