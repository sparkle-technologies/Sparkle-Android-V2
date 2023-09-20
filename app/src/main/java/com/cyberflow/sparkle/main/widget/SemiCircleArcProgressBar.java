package com.cyberflow.sparkle.main.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;



import java.util.Timer;
import java.util.TimerTask;

public class SemiCircleArcProgressBar extends View {

    private final static String TAG = "SemiCircleArcProgressBar";

    private int padding = 25;

    private int progressPlaceHolderColor;
    private int progressBarColor;
    private int progressPlaceHolderWidth;
    private int progressBarWidth;
    private int percent;

    private int top = 0;
    private int left = 0;
    private int right = 0;
    private int bottom = 0;

    //Constructors
    public SemiCircleArcProgressBar(Context context) {
        super(context);
    }

    public SemiCircleArcProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setAttrs(context, attrs);
    }

    public SemiCircleArcProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrs(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SemiCircleArcProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setAttrs(context, attrs);
    }

    private void initPaint() {
        linePaint = getPaint(Color.BLACK, 3);
        strokePaint = getPaint(progressPlaceHolderColor, progressPlaceHolderWidth);
        paint = getPaint(progressBarColor, progressBarWidth);
    }

    private void initLength(){
        padding = progressBarWidth > progressPlaceHolderWidth ? progressBarWidth + 5 : progressPlaceHolderWidth + 5;
        top = padding;
        left = padding;
        right = getMeasuredWidth();
        bottom = getMeasuredHeight() * 2;

        progressAmount = percent * (float) 1.8;

        Log.e(TAG, "init: progressAmount=" + progressAmount);
        int padd = progressPlaceHolderWidth / 5;
        rec = getProgressBarRectF();

        float _readisW = rec.width() / 2;
        float _readisH = rec.height() / 2;

        Log.e(TAG, "initLength: readisW=" + _readisW + "\t readisH=" + _readisH + "\t padd=" + padd);

        float centerX = rec.left + rec.width() / 2;
        float centerY = rec.top + rec.height() / 2;

        float radisWUp = _readisW + padd;
        float radisHUp = _readisH + padd;

        float radisWDown = _readisW - padd;
        float radisHDown = _readisH - padd;

        recUp = new RectF(centerX - radisWUp, centerY - radisHUp, centerX + radisWUp, centerY + radisHUp);
        recDown = new RectF(centerX - radisWDown, centerY - radisHDown, centerX + radisWDown, centerY + radisHDown);
    }

    private float progressAmount = 0f;
    private RectF recUp, recDown, rec;
    private Paint linePaint, strokePaint, paint;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initLength();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // step 1
        canvas.drawArc(recUp, 170, 200, false, linePaint);
        canvas.drawArc(recDown, 170, 200, false, linePaint);

        // step 2
        canvas.drawArc(getProgressBarRectF(), 170, progressAmount + 20, false, strokePaint);
        canvas.drawArc(getProgressBarRectF(), 170, progressAmount + 20, false, paint);
    }


    //Private Methods
    private void setAttrs(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, com.cyberflow.base.resources.R.styleable.SemiCircleArcProgressBar, 0, 0);
        try {
            progressPlaceHolderColor = typedArray.getColor(com.cyberflow.base.resources.R.styleable.SemiCircleArcProgressBar_progressPlaceHolderColor, Color.GRAY);
            progressBarColor = typedArray.getColor(com.cyberflow.base.resources.R.styleable.SemiCircleArcProgressBar_progressBarColor, Color.WHITE);
            progressPlaceHolderWidth = typedArray.getInt(com.cyberflow.base.resources.R.styleable.SemiCircleArcProgressBar_progressPlaceHolderWidth, 25);
            progressBarWidth = typedArray.getInt(com.cyberflow.base.resources.R.styleable.SemiCircleArcProgressBar_progressBarWidth, 10);
            percent = typedArray.getInt(com.cyberflow.base.resources.R.styleable.SemiCircleArcProgressBar_percent, 76);
        } finally {
            typedArray.recycle();
        }

        initPaint();
    }

    private Paint getPaint(int color, int strokeWidth) {
        Paint paint = new Paint();
        paint.setColor(color);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private RectF getProgressBarRectF() {
        return new RectF(left, top, right - padding, bottom - (padding * 2));
    }

    public void setPercent(int percent) {
        this.percent = percent;
        progressAmount = percent * (float) 1.8;
        postInvalidate();
    }

    //Custom Setter
    public void setPercentWithAnimation(final int percent) {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            int step = 0;

            @Override
            public void run() {
                if (step <= percent) setPercent(step++);
            }

        }, 0, 12);
    }
}