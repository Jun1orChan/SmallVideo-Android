package org.jun1or.smallvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class TypeButton extends View {
    public static final int TYPE_CANCEL = 0x001;
    public static final int TYPE_CONFIRM = 0x002;
    private int mBtnType;
    private int mBtnSize;

    private float mCenter_X;
    private float mCenter_Y;
    private float mBtnRadius;

    private Paint mPaint;
    private Path mPath;
    private float mStrokeWidth;

    private float mIndex;
    private RectF mRectF;

    public TypeButton(Context context) {
        super(context);
    }

    public TypeButton(Context context, int type, int size) {
        super(context);
        this.mBtnType = type;
        mBtnSize = size;
        mBtnRadius = size / 2.0f;
        mCenter_X = size / 2.0f;
        mCenter_Y = size / 2.0f;

        mPaint = new Paint();
        mPath = new Path();
        mStrokeWidth = size / 50f;
        mIndex = mBtnSize / 12f;
        mRectF = new RectF(mCenter_X, mCenter_Y - mIndex, mCenter_X + mIndex * 2, mCenter_Y + mIndex);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mBtnSize, mBtnSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //如果类型为取消，则绘制内部为返回箭头
        if (mBtnType == TYPE_CANCEL) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xEEDCDCDC);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mCenter_X, mCenter_Y, mBtnRadius, mPaint);

            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);

            mPath.moveTo(mCenter_X - mIndex / 7, mCenter_Y + mIndex);
            mPath.lineTo(mCenter_X + mIndex, mCenter_Y + mIndex);

            mPath.arcTo(mRectF, 90, -180);
            mPath.lineTo(mCenter_X - mIndex, mCenter_Y - mIndex);
            canvas.drawPath(mPath, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            mPath.reset();
            mPath.moveTo(mCenter_X - mIndex, (float) (mCenter_Y - mIndex * 1.5));
            mPath.lineTo(mCenter_X - mIndex, (float) (mCenter_Y - mIndex / 2.3));
            mPath.lineTo((float) (mCenter_X - mIndex * 1.6), mCenter_Y - mIndex);
            mPath.close();
            canvas.drawPath(mPath, mPaint);

        }
        //如果类型为确认，则绘制绿色勾
        if (mBtnType == TYPE_CONFIRM) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xFFFFFFFF);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mCenter_X, mCenter_Y, mBtnRadius, mPaint);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0xFF00CC00);
            mPaint.setStrokeWidth(mStrokeWidth);

            mPath.moveTo(mCenter_X - mBtnSize / 6f, mCenter_Y);
            mPath.lineTo(mCenter_X - mBtnSize / 21.2f, mCenter_Y + mBtnSize / 7.7f);
            mPath.lineTo(mCenter_X + mBtnSize / 4.0f, mCenter_Y - mBtnSize / 8.5f);
            mPath.lineTo(mCenter_X - mBtnSize / 21.2f, mCenter_Y + mBtnSize / 9.4f);
            mPath.close();
            canvas.drawPath(mPath, mPaint);
        }
    }
}
