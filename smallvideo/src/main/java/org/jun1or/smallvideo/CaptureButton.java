package org.jun1or.smallvideo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import org.jun1or.smallvideo.listener.CaptureListener;

import static org.jun1or.smallvideo.JCameraView.BUTTON_STATE_BOTH;
import static org.jun1or.smallvideo.JCameraView.BUTTON_STATE_ONLY_CAPTURE;
import static org.jun1or.smallvideo.JCameraView.BUTTON_STATE_ONLY_RECORDER;


public class CaptureButton extends View {

    private int mState;              //当前按钮状态
    private int mBtnState;       //按钮可执行的功能状态（拍照,录制,两者）

    public static final int STATE_IDLE = 0x001;        //空闲状态
    public static final int STATE_PRESS = 0x002;       //按下状态
    public static final int STATE_LONG_PRESS = 0x003;  //长按状态
    public static final int STATE_RECORDERING = 0x004; //录制状态
    public static final int STATE_BAN = 0x005;         //禁止状态

    private int mProgressColor = 0xEE16AE16;            //进度条颜色
    private int mOutsideColor = 0xEEDCDCDC;             //外圆背景色
    private int mInsideColor = 0xFFFFFFFF;              //内圆背景色


    private float mEvent_Y;  //Touch_Event_Down时候记录的Y值


    private Paint mPaint;

    private float mStrokeWidth;          //进度条宽度
    private int mOutsideAddSize;       //长按外圆半径变大的Size
    private int mInsideReduceSize;     //长安内圆缩小的Size

    //中心坐标
    private float mCenter_X;
    private float mCenter_Y;

    private float mBtnRadius;            //按钮半径
    private float mBtnOutsideRadius;    //外圆半径
    private float mBtnInsideRadius;     //内圆半径
    private int mBtnSize;                //按钮大小

    private float mProgress;         //录制视频的进度
    private int mDuration;           //录制视频最大时间长度
    private int mMinDuration;       //最短录制时间限制
    private int mRecordedTime;      //记录当前录制的时间

    private static final int LONGPRESS_DELAY = 300;
    private static final int START_ANIM_DURATION = 100;

    private RectF mRectF;

    private LongPressRunnable mLongPressRunnable;    //长按后处理的逻辑Runnable
    private CaptureListener mCaptureLisenter;        //按钮回调接口
    private RecordCountDownTimer mTimer;             //计时器

    public CaptureButton(Context context) {
        super(context);
    }

    public CaptureButton(Context context, int size) {
        super(context);
        this.mBtnSize = size;
        mBtnRadius = size / 2.0f;

        mBtnOutsideRadius = mBtnRadius;
        mBtnInsideRadius = mBtnRadius * 0.75f;

        mStrokeWidth = size / 15;
        mOutsideAddSize = size / 5;
        mInsideReduceSize = size / 8;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mProgress = 0;
        mLongPressRunnable = new LongPressRunnable();

        mState = STATE_IDLE;                //初始化为空闲状态
        mBtnState = BUTTON_STATE_BOTH;  //初始化按钮为可录制可拍照
        mDuration = 15 * 1000 + 1000;              //默认最长录制时间为15s（设置为15s，录制出来的时间为14S，故加一秒，原因未知）
        mMinDuration = 2 * 1000;              //默认最短录制时间为2s

        mCenter_X = (mBtnSize + mOutsideAddSize * 2) / 2;
        mCenter_Y = (mBtnSize + mOutsideAddSize * 2) / 2;

        mRectF = new RectF(
                mCenter_X - (mBtnRadius + mOutsideAddSize - mStrokeWidth / 2),
                mCenter_Y - (mBtnRadius + mOutsideAddSize - mStrokeWidth / 2),
                mCenter_X + (mBtnRadius + mOutsideAddSize - mStrokeWidth / 2),
                mCenter_Y + (mBtnRadius + mOutsideAddSize - mStrokeWidth / 2));

        mTimer = new RecordCountDownTimer(mDuration, mDuration / 360);    //录制定时器
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mBtnSize + mOutsideAddSize * 2, mBtnSize + mOutsideAddSize * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setColor(mOutsideColor); //外圆（半透明灰色）
        canvas.drawCircle(mCenter_X, mCenter_Y, mBtnOutsideRadius, mPaint);

        mPaint.setColor(mInsideColor);  //内圆（白色）
        canvas.drawCircle(mCenter_X, mCenter_Y, mBtnInsideRadius, mPaint);

        //如果状态为录制状态，则绘制录制进度条
        if (mState == STATE_RECORDERING) {
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            canvas.drawArc(mRectF, -90, mProgress, false, mPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() > 1 || mState != STATE_IDLE)
                    break;
                mEvent_Y = event.getY();     //记录Y值
                mState = STATE_PRESS;        //修改当前状态为点击按下
                //判断按钮状态是否为可录制状态
                if ((mBtnState == BUTTON_STATE_ONLY_RECORDER || mBtnState == BUTTON_STATE_BOTH))
                    postDelayed(mLongPressRunnable, LONGPRESS_DELAY);    //同时延长300启动长按后处理的逻辑Runnable
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCaptureLisenter != null
                        && mState == STATE_RECORDERING
                        && (mBtnState == BUTTON_STATE_ONLY_RECORDER || mBtnState == BUTTON_STATE_BOTH)) {
                    //记录当前Y值与按下时候Y值的差值，调用缩放回调接口
                    mCaptureLisenter.recordZoom(mEvent_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                //根据当前按钮的状态进行相应的处理
                handlerUnpressByState();
                break;
        }
        return true;
    }

    //当手指松开按钮时候处理的逻辑
    private void handlerUnpressByState() {
        removeCallbacks(mLongPressRunnable); //移除长按逻辑的Runnable
        //根据当前状态处理
        switch (mState) {
            //当前是点击按下
            case STATE_PRESS:
                if (mCaptureLisenter != null && (mBtnState == BUTTON_STATE_ONLY_CAPTURE || mBtnState ==
                        BUTTON_STATE_BOTH)) {
                    startCaptureAnimation(mBtnInsideRadius);
                } else {
                    mState = STATE_IDLE;
                }
                break;
            //当前是长按状态
            case STATE_RECORDERING:
                mTimer.cancel(); //停止计时器
                recordEnd();    //录制结束
                break;
        }
    }

    //录制结束
    private void recordEnd() {
        if (mCaptureLisenter != null) {
            if (mRecordedTime < mMinDuration)
                mCaptureLisenter.recordShort(mRecordedTime);//回调录制时间过短
            else
                mCaptureLisenter.recordEnd(mRecordedTime);  //回调录制结束
        }
        resetRecordAnim();  //重制按钮状态
    }

    //重制状态
    private void resetRecordAnim() {
        mState = STATE_BAN;
        mProgress = 0;       //重制进度
        invalidate();
        //还原按钮初始状态动画
        startRecordAnimation(
                mBtnOutsideRadius,
                mBtnRadius,
                mBtnInsideRadius,
                mBtnRadius * 0.75f
        );
    }

    //内圆动画
    private void startCaptureAnimation(float inside_start) {
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_start * 0.75f, inside_start);
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBtnInsideRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        inside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //回调拍照接口
                mCaptureLisenter.takePictures();
                mState = STATE_BAN;
            }
        });
        inside_anim.setDuration(START_ANIM_DURATION);
        inside_anim.start();
    }

    //内外圆动画
    private void startRecordAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {
        ValueAnimator outside_anim = ValueAnimator.ofFloat(outside_start, outside_end);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_end);
        //外圆动画监听
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBtnOutsideRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        //内圆动画监听
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBtnInsideRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        AnimatorSet set = new AnimatorSet();
        //当动画结束后启动录像Runnable并且回调录像开始接口
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //设置为录制状态
                if (mState == STATE_LONG_PRESS) {
                    if (mCaptureLisenter != null)
                        mCaptureLisenter.recordStart();
                    mState = STATE_RECORDERING;
                    mTimer.start();
                }
            }
        });
        set.playTogether(outside_anim, inside_anim);
        set.setDuration(START_ANIM_DURATION);
        set.start();
    }


    //更新进度条
    private void updateProgress(long millisUntilFinished) {
        mRecordedTime = (int) (mDuration - millisUntilFinished);
        mProgress = 360f - millisUntilFinished / (float) mDuration * 360f;
        invalidate();
    }

    //录制视频计时器
    private class RecordCountDownTimer extends CountDownTimer {
        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            updateProgress(0);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    recordEnd();
                }
            }, 200);
        }
    }

    //长按线程
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            mState = STATE_LONG_PRESS;   //如果按下后经过300毫秒则会修改当前状态为长按状态
            //启动按钮动画，外圆变大，内圆缩小
            startRecordAnimation(
                    mBtnOutsideRadius,
                    mBtnOutsideRadius + mOutsideAddSize,
                    mBtnInsideRadius,
                    mBtnInsideRadius - mInsideReduceSize
            );
        }
    }

    /**************************************************
     * 对外提供的API                     *
     **************************************************/

    //设置最长录制时间
    public void setDuration(int duration) {
        this.mDuration = duration + 1000;
        mTimer = new RecordCountDownTimer(mDuration, mDuration / 360);    //录制定时器
    }

    //设置最短录制时间
    public void setMinDuration(int duration) {
        this.mMinDuration = duration;
    }

    //设置回调接口
    public void setCaptureLisenter(CaptureListener captureLisenter) {
        this.mCaptureLisenter = captureLisenter;
    }

    //设置按钮功能（拍照和录像）
    public void setButtonFeatures(int state) {
        this.mBtnState = state;
    }

    //是否空闲状态
    public boolean isIdle() {
        return mState == STATE_IDLE ? true : false;
    }

    //设置状态
    public void resetState() {
        mState = STATE_IDLE;
    }
}
