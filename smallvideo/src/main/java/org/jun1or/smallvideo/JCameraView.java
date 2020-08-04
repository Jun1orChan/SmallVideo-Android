package org.jun1or.smallvideo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;


import androidx.annotation.RequiresApi;

import org.jun1or.smallvideo.listener.CaptureListener;
import org.jun1or.smallvideo.listener.JCameraListener;
import org.jun1or.smallvideo.listener.ReturnListener;
import org.jun1or.smallvideo.listener.TypeListener;
import org.jun1or.smallvideo.state.CameraMachine;
import org.jun1or.util.DisplayUtil;
import org.jun1or.util.FileUtil;


public class JCameraView extends FrameLayout implements CameraInterface.CameraOpenOverCallback, SurfaceHolder
        .Callback, CameraView {
    //Camera状态机
    private CameraMachine mCameraMachine;

    //闪关灯状态
    private static final int TYPE_FLASH_AUTO = 0x021;
    private static final int TYPE_FLASH_ON = 0x022;
    private static final int TYPE_FLASH_OFF = 0x023;
    private int type_flash = TYPE_FLASH_OFF;

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;
    public static final int TYPE_VIDEO = 0x002;
    public static final int TYPE_SHORT = 0x003;
    public static final int TYPE_DEFAULT = 0x004;

    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 24 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 20 * 100000;
    public static final int MEDIA_QUALITY_LOW = 16 * 100000;
    public static final int MEDIA_QUALITY_POOR = 12 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 8 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 4 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 2 * 80000;


    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;      //只能拍照
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;     //只能录像
    public static final int BUTTON_STATE_BOTH = 0x103;              //两者都可以


    //回调监听
    private JCameraListener mJCameraLisenter;

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private ImageView mFlashLamp;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    private MediaPlayer mMediaPlayer;

    private int mLayoutWidth;
    private float mScreenProp = 0f;

    private Bitmap mCaptureBitmap;   //捕获的图片
    private Bitmap mFirstFrame;      //第一帧图片
    private String mVideoUrl;        //视频URL


    //切换摄像头按钮的参数
    private int mDuration = 0;       //录制时间

    //缩放梯度
    private int mZoomGradient = 0;

    private boolean mFirstTouch = true;
    private float mFirstTouchLength = 0;

    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //get AttributeSet
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.smallvideo_JCameraView, defStyleAttr, 0);
        mDuration = a.getInteger(R.styleable.smallvideo_JCameraView_duration_max, 15 * 1000);       //没设置默认为15s
        a.recycle();
        initData();
        initView();
    }

    private void initData() {
        mLayoutWidth = DisplayUtil.getScreenWidth(mContext);
        //缩放梯度
        mZoomGradient = (int) (mLayoutWidth / 16f);
        mCameraMachine = new CameraMachine(getContext(), this, this);
    }

    private void initView() {
        setWillNotDraw(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.smallvideo_view, this);
        mVideoView = (VideoView) view.findViewById(R.id.video_preview);
        mPhoto = (ImageView) view.findViewById(R.id.image_photo);
        mSwitchCamera = (ImageView) view.findViewById(R.id.image_switch);
        mFlashLamp = (ImageView) view.findViewById(R.id.image_flash);
        setFlashRes();
        mFlashLamp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                type_flash++;
                if (type_flash > 0x023)
                    type_flash = TYPE_FLASH_AUTO;
                setFlashRes();
            }
        });
        mCaptureLayout = (CaptureLayout) view.findViewById(R.id.capture_layout);
        mCaptureLayout.setDuration(mDuration);
        mFoucsView = (FoucsView) view.findViewById(R.id.fouce_view);
        mVideoView.getHolder().addCallback(this);
        //切换摄像头
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraMachine.swtich(mVideoView.getHolder(), mScreenProp);
            }
        });
        //拍照 录像
        mCaptureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                mSwitchCamera.setVisibility(INVISIBLE);
                mFlashLamp.setVisibility(INVISIBLE);
                mCameraMachine.capture();
            }

            @Override
            public void recordStart() {
                mSwitchCamera.setVisibility(INVISIBLE);
                mFlashLamp.setVisibility(INVISIBLE);
                mCameraMachine.record(mVideoView.getHolder().getSurface(), mScreenProp);
            }

            @Override
            public void recordShort(final long time) {
                mCaptureLayout.setTextWithAnimation("录制时间过短");
                mSwitchCamera.setVisibility(VISIBLE);
                mFlashLamp.setVisibility(VISIBLE);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCameraMachine.stopRecord(true, time);
                    }
                }, 1000 - time);
            }

            @Override
            public void recordEnd(long time) {
                mCameraMachine.stopRecord(false, time);
            }

            @Override
            public void recordZoom(float zoom) {
                mCameraMachine.zoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            @Override
            public void recordError() {
            }
        });
        //确认 取消
        mCaptureLayout.setTypeLisenter(new TypeListener() {
            @Override
            public void cancel() {
                mCameraMachine.cancle(mVideoView.getHolder(), mScreenProp);
            }

            @Override
            public void confirm() {
                mCameraMachine.confirm();
            }
        });
        //退出
        mCaptureLayout.setReturnLisenter(new ReturnListener() {
            @Override
            public void onReturn() {
                if (mJCameraLisenter != null) {
                    mJCameraLisenter.onReturn();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = mVideoView.getMeasuredWidth();
        float heightSize = mVideoView.getMeasuredHeight();
        if (mScreenProp == 0) {
            mScreenProp = heightSize / widthSize;
        }
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), mScreenProp);
    }

    //生命周期onResume
    public void onResume() {
        resetState(TYPE_DEFAULT); //重置状态
        CameraInterface.getInstance().registerSensorManager(mContext);
        CameraInterface.getInstance().setSwitchView(mSwitchCamera, mFlashLamp);
        mCameraMachine.start(mVideoView.getHolder(), mScreenProp);
//        mCameraMachine.foucs(getWidth() / 2, getHeight() / 2, null);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraInterface.getInstance().handleFocus(mCameraMachine.getContext(), getWidth() / 2, getHeight() / 2, null);
            }
        }, 500);
    }

    //生命周期onPause
    public void onPause() {
        stopVideo();
        resetState(TYPE_PICTURE);
        CameraInterface.getInstance().isPreview(false);
        CameraInterface.getInstance().unregisterSensorManager(mContext);
    }

    //SurfaceView生命周期
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread() {
            @Override
            public void run() {
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraInterface.getInstance().doDestroyCamera();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                if (event.getPointerCount() == 2) {
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    mFirstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                            point_2_Y, 2));

                    if (mFirstTouch) {
                        mFirstTouchLength = result;
                        mFirstTouch = false;
                    }
                    if ((int) (result - mFirstTouchLength) / mZoomGradient != 0) {
                        mFirstTouch = true;
                        mCameraMachine.zoom(result - mFirstTouchLength, CameraInterface.TYPE_CAPTURE);
                    }
//                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                mFirstTouch = true;
                break;
        }
        return true;
    }

    //对焦框指示器动画
    private void setFocusViewWidthAnimation(float x, float y) {
        mCameraMachine.foucs(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });
    }

    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            mVideoView.setLayoutParams(videoViewParam);
        }
    }

    /**************************************************
     * 对外提供的API                     *
     **************************************************/

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }


    public void setJCameraLisenter(JCameraListener jCameraLisenter) {
        this.mJCameraLisenter = jCameraLisenter;
    }


    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        this.mCaptureLayout.setButtonFeatures(state);
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    @Override
    public void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                //初始化VideoView
                FileUtil.deleteFile(mVideoUrl);
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mCameraMachine.start(mVideoView.getHolder(), mScreenProp);
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
        }
        mSwitchCamera.setVisibility(VISIBLE);
        mFlashLamp.setVisibility(VISIBLE);
        mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mCameraMachine.start(mVideoView.getHolder(), mScreenProp);
                if (mJCameraLisenter != null) {
                    mJCameraLisenter.recordSuccess(mVideoUrl, mFirstFrame);
                }
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                if (mJCameraLisenter != null) {
                    mJCameraLisenter.captureSuccess(mCaptureBitmap);
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        if (isVertical) {
            mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        mCaptureBitmap = bitmap;
        mPhoto.setImageBitmap(bitmap);
        mPhoto.setVisibility(VISIBLE);
        mCaptureLayout.startAlphaAnimation();
        mCaptureLayout.startTypeBtnAnimator();
    }

    @Override
    public void playVideo(Bitmap firstFrame, final String url) {
        mVideoUrl = url;
        JCameraView.this.mFirstFrame = firstFrame;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void setTip(String tip) {
        mCaptureLayout.setTip(tip);
    }

    @Override
    public void startPreviewCallback() {
        handlerFoucs(mFoucsView.getWidth() / 2, mFoucsView.getHeight() / 2);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        if (y > mCaptureLayout.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > mLayoutWidth - mFoucsView.getWidth() / 2) {
            x = mLayoutWidth - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }

    private void setFlashRes() {
        switch (type_flash) {
            case TYPE_FLASH_AUTO:
                mFlashLamp.setImageResource(R.drawable.smallvideo_flash_auto);
                mCameraMachine.flash(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case TYPE_FLASH_ON:
                mFlashLamp.setImageResource(R.drawable.smallvideo_flash_on);
                mCameraMachine.flash(Camera.Parameters.FLASH_MODE_ON);
                break;
            case TYPE_FLASH_OFF:
                mFlashLamp.setImageResource(R.drawable.smallvideo_flash_off);
                mCameraMachine.flash(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
    }
}
