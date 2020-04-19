package org.jun1or.smallvideo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jun1or.smallvideo.listener.CaptureListener;
import org.jun1or.smallvideo.listener.ReturnListener;
import org.jun1or.smallvideo.listener.TypeListener;


public class CaptureLayout extends FrameLayout {

    private CaptureListener mCaptureLisenter;    //拍照按钮监听
    private TypeListener mTypeLisenter;          //拍照或录制后接结果按钮监听
    private ReturnListener mReturnListener;      //退出按钮监听

    public void setTypeLisenter(TypeListener typeLisenter) {
        this.mTypeLisenter = typeLisenter;
    }

    public void setCaptureLisenter(CaptureListener captureLisenter) {
        this.mCaptureLisenter = captureLisenter;
    }

    public void setReturnLisenter(ReturnListener returnListener) {
        this.mReturnListener = returnListener;
    }

    private CaptureButton mBtnCapture;      //拍照按钮
    private TypeButton mBtnConfirm;         //确认按钮
    private TypeButton mBtnCancel;          //取消按钮
    private ReturnButton mBtnReturn;        //返回按钮
    private TextView mTvTip;               //提示文本

    private int mLayoutWidth;
    private int mLayoutHeight;
    private int mBtnSize;

    private boolean mIsFirst = true;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutWidth = outMetrics.widthPixels;
        } else {
            mLayoutWidth = outMetrics.widthPixels / 2;
        }
        mBtnSize = (int) (mLayoutWidth / 4.5f);
        mLayoutHeight = mBtnSize + (mBtnSize / 5) * 2 + 100;

        initView();
        initEvent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mLayoutWidth, mLayoutHeight);
    }

    public void initEvent() {
        //默认Typebutton为隐藏
        mBtnCancel.setVisibility(GONE);
        mBtnConfirm.setVisibility(GONE);
    }

    public void startTypeBtnAnimator() {
        //拍照录制结果后的动画
        mBtnReturn.setVisibility(GONE);
        mBtnCapture.setVisibility(GONE);
        mBtnCancel.setVisibility(VISIBLE);
        mBtnConfirm.setVisibility(VISIBLE);
        mBtnCancel.setClickable(false);
        mBtnConfirm.setClickable(false);
        ObjectAnimator animator_cancel = ObjectAnimator.ofFloat(mBtnCancel, "translationX", mLayoutWidth / 4, 0);
        ObjectAnimator animator_confirm = ObjectAnimator.ofFloat(mBtnConfirm, "translationX", -mLayoutWidth / 4, 0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator_cancel, animator_confirm);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBtnCancel.setClickable(true);
                mBtnConfirm.setClickable(true);
            }
        });
        set.setDuration(200);
        set.start();
    }


    private void initView() {
        setWillNotDraw(false);
        //拍照按钮
        mBtnCapture = new CaptureButton(getContext(), mBtnSize);
        LayoutParams btn_capture_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btn_capture_param.gravity = Gravity.CENTER;
        mBtnCapture.setLayoutParams(btn_capture_param);
        mBtnCapture.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                if (mCaptureLisenter != null) {
                    mCaptureLisenter.takePictures();
                }
            }

            @Override
            public void recordShort(long time) {
                if (mCaptureLisenter != null) {
                    mCaptureLisenter.recordShort(time);
                }
                startAlphaAnimation();
            }

            @Override
            public void recordStart() {
                if (mCaptureLisenter != null) {
                    mCaptureLisenter.recordStart();
                }
                startAlphaAnimation();
            }

            @Override
            public void recordEnd(long time) {
                if (mCaptureLisenter != null) {
                    mCaptureLisenter.recordEnd(time);
                }
                startAlphaAnimation();
                startTypeBtnAnimator();
            }

            @Override
            public void recordZoom(float zoom) {
                if (mCaptureLisenter != null) {
                    mCaptureLisenter.recordZoom(zoom);
                }
            }

            @Override
            public void recordError() {
                if (mCaptureLisenter != null) {
                    mCaptureLisenter.recordError();
                }
            }
        });

        //取消按钮
        mBtnCancel = new TypeButton(getContext(), TypeButton.TYPE_CANCEL, mBtnSize);
        final LayoutParams btn_cancel_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL;
        btn_cancel_param.setMargins((mLayoutWidth / 4) - mBtnSize / 2, 0, 0, 0);
        mBtnCancel.setLayoutParams(btn_cancel_param);
        mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTypeLisenter != null) {
                    mTypeLisenter.cancel();
                }
                startAlphaAnimation();
            }
        });

        //确认按钮
        mBtnConfirm = new TypeButton(getContext(), TypeButton.TYPE_CONFIRM, mBtnSize);
        LayoutParams btn_confirm_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        btn_confirm_param.setMargins(0, 0, (mLayoutWidth / 4) - mBtnSize / 2, 0);
        mBtnConfirm.setLayoutParams(btn_confirm_param);
        mBtnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTypeLisenter != null) {
                    mTypeLisenter.confirm();
                }
                startAlphaAnimation();
//                resetCaptureLayout();
            }
        });
        //返回按钮
        mBtnReturn = new ReturnButton(getContext(), (int) (mBtnSize / 2.5f));
        LayoutParams iv_custom_param_left = new LayoutParams((int) (mBtnSize / 2.5f), (int) (mBtnSize / 2.5f));
        iv_custom_param_left.gravity = Gravity.CENTER_VERTICAL;
        iv_custom_param_left.setMargins(mLayoutWidth / 6, 0, 0, 0);
        mBtnReturn.setLayoutParams(iv_custom_param_left);
        mBtnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mReturnListener != null)
                    mReturnListener.onReturn();
            }
        });

        mTvTip = new TextView(getContext());
        LayoutParams txt_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        txt_param.gravity = Gravity.CENTER_HORIZONTAL;
        txt_param.setMargins(0, 0, 0, 0);
        mTvTip.setText("轻触拍照，长按摄像");
        mTvTip.setTextColor(0xFFFFFFFF);
        mTvTip.setGravity(Gravity.CENTER);
        mTvTip.setLayoutParams(txt_param);

        this.addView(mBtnCapture);
        this.addView(mBtnCancel);
        this.addView(mBtnConfirm);
        this.addView(mBtnReturn);
        this.addView(mTvTip);

    }

    /**************************************************
     * 对外提供的API                      *
     **************************************************/
    public void resetCaptureLayout() {
        mBtnCapture.resetState();
        mBtnCancel.setVisibility(GONE);
        mBtnConfirm.setVisibility(GONE);
        mBtnCapture.setVisibility(VISIBLE);
        mBtnReturn.setVisibility(VISIBLE);
    }


    public void startAlphaAnimation() {
        if (mIsFirst) {
            ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(mTvTip, "alpha", 1f, 0f);
            animator_txt_tip.setDuration(300);
            animator_txt_tip.start();
            mIsFirst = false;
        }
    }

    public void setTextWithAnimation(String tip) {
        mTvTip.setText(tip);
        ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(mTvTip, "alpha", 0f, 1f, 1f, 0f);
        animator_txt_tip.setDuration(2500);
        animator_txt_tip.start();
    }

    public void setDuration(int duration) {
        mBtnCapture.setDuration(duration);
    }

    public void setButtonFeatures(int state) {
        mBtnCapture.setButtonFeatures(state);
        if (state != JCameraView.BUTTON_STATE_BOTH) {
            mTvTip.setText(null);
        }
    }

    public void setTip(String tip) {
        mTvTip.setText(tip);
    }

    public void showTip() {
        mTvTip.setVisibility(VISIBLE);
    }

    public void setReturnClickListener(ReturnListener returnClickListener) {
        this.mReturnListener = returnClickListener;
    }
}
