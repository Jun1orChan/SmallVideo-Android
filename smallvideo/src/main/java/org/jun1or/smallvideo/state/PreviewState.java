package org.jun1or.smallvideo.state;

import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.jun1or.smallvideo.CameraInterface;
import org.jun1or.smallvideo.JCameraView;

class PreviewState implements State {
    public static final String TAG = "PreviewState";

    private CameraMachine mCameraMachine;

    PreviewState(CameraMachine machine) {
        this.mCameraMachine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
    }

    @Override
    public void stop() {
        CameraInterface.getInstance().doStopPreview();
    }


    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
        if (mCameraMachine.getView().handlerFoucs(x, y)) {
            CameraInterface.getInstance().handleFocus(mCameraMachine.getContext(), x, y, callback);
        }
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().switchCamera(holder, screenProp);
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
            @Override
            public void captureResult(Bitmap bitmap, boolean isVertical) {
                mCameraMachine.getView().showPicture(bitmap, isVertical);
                mCameraMachine.setState(mCameraMachine.getBorrowPictureState());
//                LogUtil.i("capture");
            }
        });
    }

    @Override
    public void record(Surface surface, float screenProp) {
        CameraInterface.getInstance().startRecord(surface, screenProp, null);
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        CameraInterface.getInstance().stopRecord(isShort, new CameraInterface.StopRecordCallback() {
            @Override
            public void recordResult(String url, Bitmap firstFrame) {
                if (isShort) {
                    mCameraMachine.getView().resetState(JCameraView.TYPE_SHORT);
                } else {
                    mCameraMachine.getView().playVideo(firstFrame, url);
                    mCameraMachine.setState(mCameraMachine.getBorrowVideoState());
                }
            }
        });
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {
//        LogUtil.i("浏览状态下,没有 cancle 事件");
    }

    @Override
    public void confirm() {
//        LogUtil.i("浏览状态下,没有 confirm 事件");
    }

    @Override
    public void zoom(float zoom, int type) {
//        LogUtil.i(TAG, "zoom");
        CameraInterface.getInstance().setZoom(zoom, type);
    }

    @Override
    public void flash(String mode) {
        CameraInterface.getInstance().setFlashMode(mode);
    }
}
