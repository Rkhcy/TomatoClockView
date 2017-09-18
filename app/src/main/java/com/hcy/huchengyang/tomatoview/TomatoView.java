package com.hcy.huchengyang.tomatoview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by huchengyang on 2017/9/18.
 */

public class TomatoView extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mColor = Color.parseColor("#D1D1D1");
    private int centerX;
    private int centerY;
    private int radius;
    private RectF mRectF = new RectF();
    public static final float START_ANGLE = -90;
    public static final int MAX_TIME = 60;
    private float sweepVelocity = 0;
    private String textTime = "00:00";
    //分钟
    private int time;
    //倒计时
    private int countdownTime;
    private float touchX;
    private float touchY;
    private float offsetX;
    private float offsetY;
    private boolean isStarted;

    public TomatoView(Context context) {
        super(context);
    }

    public TomatoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TomatoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public static float dpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * metrics.density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        centerX = width / 2;
        centerY = height / 2;
        radius = (int) dpToPixel(120);
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        //黑圆
        canvas.save();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dpToPixel(5));
        canvas.drawCircle(centerX, centerY, radius, mPaint);
        canvas.restore();
        //灰圆
        canvas.save();
        mPaint.setColor(mColor);
        canvas.drawArc(mRectF, START_ANGLE, 360 * sweepVelocity, false, mPaint);
        canvas.restore();
        //时间
        canvas.save();
        timePaint.setColor(Color.BLACK);
        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setTextSize(dpToPixel(40));
        canvas.drawText(textTime, centerX - timePaint.measureText(textTime) / 2,
                centerY - (timePaint.ascent() + timePaint.descent()) / 2, timePaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isStarted) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();
        boolean isContained = isContained(x, y);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isContained) {
                    touchX = x;
                    touchY = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isContained) {
                    offsetX = x - touchX;
                    offsetY = y - touchY;
                    time = (int) (offsetY / 2 / radius * MAX_TIME);
                    if (time <= 0) {
                        time = 0;
                    }
                    textTime = formatTime(time);
                    countdownTime = time * 60;
                    invalidate();
                }
                break;
        }
        return true;
    }

    private boolean isContained(float x, float y) {
        if (Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)) > radius) {
            return false;
        } else {
            return true;
        }
    }

    private String formatTime(int time) {
        StringBuilder sb = new StringBuilder();
        if (time < 10) {
            sb.append("0" + time + ":00");
        } else {
            sb.append(time + ":00");
        }
        return sb.toString();
    }

    private String formatCountdownTime(int countdownTime) {
        StringBuilder sb = new StringBuilder();
        int minute = countdownTime / 60;
        int second = countdownTime - 60 * minute;
        if (minute < 10) {
            sb.append("0" + minute + ":");
        } else {
            sb.append(minute + ":");
        }
        if (second < 10) {
            sb.append("0" + second);
        } else {
            sb.append(second);
        }
        return sb.toString();
    }

    public void start(){
        if (countdownTime == 0 || isStarted) {
            return;
        }
        isStarted = true;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1.0f);
        valueAnimator.setDuration(countdownTime * 1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sweepVelocity = (float) animation.getAnimatedValue();
                mColor = Color.parseColor("#D1D1D1");
                invalidate();
            }
        });
        valueAnimator.start();

        new CountDownTimer(countdownTime * 1000 + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownTime = (countdownTime * 1000 - 1000) / 1000;
                textTime = formatCountdownTime(countdownTime);
                invalidate();
            }

            @Override
            public void onFinish() {
                mColor = Color.BLACK;
                sweepVelocity = 0;
                isStarted = false;
                invalidate();
            }
        }.start();
    }



}
