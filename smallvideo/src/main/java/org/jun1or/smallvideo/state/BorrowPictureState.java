package org.jun1or.smallvideo.state;

import android.view.Surface;
import android.view.SurfaceHolder;

import org.jun1or.smallvideo.CameraInterface;
import org.jun1or.smallvideo.JCameraView;

public class BorrowPictureState implements State {
    private final String TAG = "BorrowPictureState";
    private CameraMachine mCameraMachine;

    public BorrowPictureState(CameraMachine machine) {
        this.mCameraMachine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
        mCameraMachine.setState(mCameraMachine.getPreviewState());
    }

    @Override
    public void stop() {

    }


    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {

    }

    @Override
    public void record(Surface surface, float screenProp) {

    }

    @Override
    public void stopRecord(boolean isShort, long time) {
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
        mCameraMachine.getView().resetState(JCameraView.TYPE_PICTURE);
        mCameraMachine.setState(mCameraMachine.getPreviewState());
    }

    @Override
    public void confirm() {
        mCameraMachine.setState(mCameraMachine.getPreviewState());
        mCameraMachine.getView().confirmState(JCameraView.TYPE_PICTURE);
    }

    @Override
    public void zoom(float zoom, int type) {
//        LogUtil.i(TAG, "zoom");
    }

    @Override
    public void flash(String mode) {

    }

}
