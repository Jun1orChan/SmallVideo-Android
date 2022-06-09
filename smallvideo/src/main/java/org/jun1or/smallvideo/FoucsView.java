package org.jun1or.smallvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import org.jun1or.smallvideo.util.DisplayUtil;


public class FoucsView extends View {
    private int mSize;
    private int mCenter_x;
    private int mCenter_y;
    private int mLength;
    private Paint mPaint;

    public FoucsView(Context context) {
        this(context, null);
    }

    public FoucsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoucsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mSize = DisplayUtil.getScreenWidth(context) / 3;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xEE16AE16);
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCenter_x = (int) (mSize / 2.0);
        mCenter_y = (int) (mSize / 2.0);
        mLength = (int) (mSize / 2.0) - 2;
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mCenter_x - mLength, mCenter_y - mLength, mCenter_x + mLength, mCenter_y + mLength, mPaint);
        canvas.drawLine(2, getHeight() / 2, mSize / 10, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() - 2, getHeight() / 2, getWidth() - mSize / 10, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() / 2, 2, getWidth() / 2, mSize / 10, mPaint);
        canvas.drawLine(getWidth() / 2, getHeight() - 2, getWidth() / 2, getHeight() - mSize / 10, mPaint);
    }
}
