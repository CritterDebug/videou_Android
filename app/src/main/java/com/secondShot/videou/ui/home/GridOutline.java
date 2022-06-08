package com.secondShot.videou.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;


public class GridOutline extends View {
    private int width = 0;
    private int height = 0;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GridOutline(Context context) {
        super(context);
        init(context);
    }

    public GridOutline(Context context, int w, int h) {
        super(context);
        init(context);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int topBarRemove = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getContext().getResources().getDisplayMetrics());
        canvas.drawLine(width/3, topBarRemove, width/3, height, mPaint);
        canvas.drawLine(width/3 + width/3, topBarRemove, width/3 + width/3, height, mPaint);
        canvas.drawLine(0, height - height/3, width, height - height/3, mPaint);
        canvas.drawLine(0, height - (height/3 + height/3), width, height - (height/3 + height/3), mPaint);
        invalidate();
    }

    public void init(Context context) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(1);
    }
}




