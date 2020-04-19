package org.jun1or.smallvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class ReturnButton extends View {

    private int mSize;

    private int mCenter_X;
    private int mCenter_Y;
    private float mStrokeWidth;

    private Paint mPaint;
    private Path mPath;

    public ReturnButton(Context context, int size) {
        this(context);
        this.mSize = size;
        mCenter_X = size / 2;
        mCenter_Y = size / 2;

        mStrokeWidth = size / 15f;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);

        mPath = new Path();
    }

    public ReturnButton(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.moveTo(mStrokeWidth, mStrokeWidth / 2 + mSize / 4);
        mPath.lineTo(mCenter_X, mCenter_Y + mSize / 4 - mStrokeWidth / 2);
        mPath.lineTo(mSize - mStrokeWidth, mStrokeWidth / 2 + mSize / 4);
        canvas.drawPath(mPath, mPaint);
    }
}
