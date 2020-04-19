package org.jun1or.smallvideo.state;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.jun1or.smallvideo.CameraInterface;
import org.jun1or.smallvideo.CameraView;


public class CameraMachine implements State {


    private Context mContext;
    private State mState;
    private CameraView mCameraView;
//    private CameraInterface.CameraOpenOverCallback cameraOpenOverCallback;

    private State mPreviewState;       //浏览状态(空闲)
    private State mBorrowPictureState; //浏览图片
    private State mBorrowVideoState;   //浏览视频

    public CameraMachine(Context context, CameraView view, CameraInterface.CameraOpenOverCallback
            cameraOpenOverCallback) {
        this.mContext = context;
        mPreviewState = new PreviewState(this);
        mBorrowPictureState = new BorrowPictureState(this);
        mBorrowVideoState = new BorrowVideoState(this);
        //默认设置为空闲状态
        this.mState = mPreviewState;
//        this.cameraOpenOverCallback = cameraOpenOverCallback;
        this.mCameraView = view;
    }

    public CameraView getView() {
        return mCameraView;
    }

    public Context getContext() {
        return mContext;
    }

    public void setState(State state) {
        this.mState = state;
    }

    //获取浏览图片状态
    State getBorrowPictureState() {
        return mBorrowPictureState;
    }

    //获取浏览视频状态
    State getBorrowVideoState() {
        return mBorrowVideoState;
    }

    //获取空闲状态
    State getPreviewState() {
        return mPreviewState;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        mState.start(holder, screenProp);
    }

    @Override
    public void stop() {
        mState.stop();
    }

    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
        mState.foucs(x, y, callback);
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {
        mState.swtich(holder, screenProp);
    }

    @Override
    public void restart() {
        mState.restart();
    }

    @Override
    public void capture() {
        mState.capture();
    }

    @Override
    public void record(Surface surface, float screenProp) {
        mState.record(surface, screenProp);
    }

    @Override
    public void stopRecord(boolean isShort, long time) {
        mState.stopRecord(isShort, time);
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {
        mState.cancle(holder, screenProp);
    }

    @Override
    public void confirm() {
        mState.confirm();
    }


    @Override
    public void zoom(float zoom, int type) {
        mState.zoom(zoom, type);
    }

    @Override
    public void flash(String mode) {
        mState.flash(mode);
    }

    public State getState() {
        return this.mState;
    }
}
