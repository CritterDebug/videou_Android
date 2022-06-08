package com.secondShot.videou.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.concurrent.TimeUnit;


public class CircleOutline extends View {
    private float x = 0;
    private float y = 0;
    private int r = 0;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alphaNum = 100;

    public CircleOutline(Context context) {
        super(context);
        init(context);
    }

    public CircleOutline(Context context, float x, float y, int r) {
        super(context);
        init(context);
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public void startAnimating() {
        new Thread(new Runnable() {
            public void run() {
                while (r > 110) {
                    invalidate();
                    try {
                        TimeUnit.MILLISECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    r--;
                }
                mPaint.setAlpha(alphaNum);
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                while ( alphaNum > 50 ) {
                    invalidate();
                    try {
                        TimeUnit.MILLISECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    alphaNum--;
                    mPaint.setAlpha(alphaNum);
                }
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setAlpha(255);
                invalidate();
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, r, mPaint);
    }

    public void init(Context context) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);
        startAnimating();
    }
}




