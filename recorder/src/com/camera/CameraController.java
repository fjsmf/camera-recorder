package com.camera;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


import com.glgjing.recorder.RecordApplication;
import com.utils.CameraUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by cj on 2017/8/2.
 * desc 相机的管理类 主要是Camera的一些设置
 * 包括预览和录制尺寸、闪光灯、曝光、聚焦、摄像头切换等
 */

public class CameraController implements ICamera {
    /**
     * 相机的宽高及比例配置
     */
    private ICamera.Config mConfig;
    /**
     * 相机实体
     */
    private Camera mCamera;
    /**
     * 预览的尺寸
     */
    private Camera.Size preSize;
    /**
     * 实际的尺寸
     */
    private Camera.Size picSize;

    private Point mPreSize;
    private Point mPicSize;


    public CameraController() {
        /**初始化一个默认的格式大小*/
        mConfig = new ICamera.Config();
        mConfig.minPreviewWidth = 400;
        mConfig.minPictureWidth = 400;
        mConfig.rate = 1.33f;
    }

    public void open(int cameraId) {
        mCamera = Camera.open(cameraId);
        if (mCamera != null) {
            Display display = ((WindowManager) RecordApplication.getInstance().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            int angle = 0;
            switch (display.getRotation()) {
                case Surface.ROTATION_0: // This is display orientation
                    angle = 90; // This is camera orientation
                    break;
/*                case Surface.ROTATION_90:
                    angle = 0;
                    break;
                case Surface.ROTATION_180:
                    angle = 270;
                    break;*/
                case Surface.ROTATION_270:
                    angle = 180;
                    break;
/*                default:
                    angle = 90;
                    break;*/
            }
//            mCamera.setDisplayOrientation(angle);
            /**选择当前设备允许的预览尺寸*/
            Camera.Parameters param = mCamera.getParameters();
            preSize = getPropPreviewSize(param.getSupportedPreviewSizes(), mConfig.rate,
                    mConfig.minPreviewWidth);
            picSize = getPropPictureSize(param.getSupportedPictureSizes(), mConfig.rate,
                    mConfig.minPictureWidth);
            param.setPictureSize(picSize.width, picSize.height);
            param.setPreviewSize(preSize.width, preSize.height);
            mCamera.setParameters(param);
            Camera.Size pre = param.getPreviewSize();
            Camera.Size pic = param.getPictureSize();
            mPicSize = new Point(pic.height, pic.width);
            mPreSize = new Point(pre.height, pre.width);
        }
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                Log.e("hero", "----setPreviewTexture");
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setConfig(Config config) {
        this.mConfig = config;
    }

    @Override
    public void setOnPreviewFrameCallback(final PreviewFrameCallback callback) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    callback.onPreviewFrame(data, mPreSize.x, mPreSize.y);
                }
            });
        }
    }

    @Override
    public void preview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    @Override
    public Point getPreviewSize() {
        return mPreSize;
    }

    @Override
    public Point getPictureSize() {
        return mPicSize;
    }

    @Override
    public boolean close() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    private Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);

       /* int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }*/
        Camera.Size s = null;
        float minRate = 100, rate;
        for (Camera.Size size : list) {
            if (size.width < minWidth) {
                continue;
            }
            rate = (float)size.width/size.height;
            if (rate < minRate) {
                minRate = rate;
                s = size;
            }
        }
        if (minRate < 100) {
            return s;
        }
        return list.get(0);
    }

    private static boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d("nadiee", "releaseCamera -- done");
        }
    }

    protected void setDisplayOrientation(Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]{angle});
        } catch (Exception e1) {

        }
    }
}
