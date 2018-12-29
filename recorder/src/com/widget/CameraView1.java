/*
package com.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.camera.CameraController;
import com.example.cj.videoeditor.drawer.CameraDrawer;
import com.glgjing.recorder.RecordApplication;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.Context.WINDOW_SERVICE;

*/
/**
 * Created by cj on 2017/8/1.
 * desc
 *//*


public class CameraView1 extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private Context mContext;

    private CameraDrawer mCameraDrawer;
    private CameraController mCamera;

    private int dataWidth = 0, dataHeight = 0;

    private boolean isSetParm = false;

    private int cameraId;

    CameraOrientationListener mOrientationListener;

    public CameraView1(Context context) {
        this(context, null);
    }

    public CameraView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        */
/**初始化OpenGL的相关信息*//*

        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
        setCameraDistance(100);//相机距离

        */
/**初始化Camera的绘制类*//*

        mCameraDrawer = new CameraDrawer(getResources());
        */
/**初始化相机的管理类*//*

        mCamera = new CameraController();
        mOrientationListener = new CameraOrientationListener(mContext);
        mOrientationListener.enable();

    }

    private void open(int cameraId) {
        mCamera.close();
        mCamera.open(cameraId);
//        determineDisplayOrientation();
        mCameraDrawer.setCameraId(cameraId);
        final Point previewSize = mCamera.getPreviewSize();
        dataWidth = previewSize.x;
        dataHeight = previewSize.y;
        SurfaceTexture texture = mCameraDrawer.getTexture();
        texture.setOnFrameAvailableListener(this);
        mCamera.setPreviewTexture(texture);
        Display display = ((WindowManager) RecordApplication.getInstance().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        } else if(display.getRotation() == Surface.ROTATION_270) {
            mCamera.setDisplayOrientation(180);
        }
        mCamera.preview();
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.releaseCamera();
        }
        SurfaceTexture texture = mCameraDrawer.getTexture();
        if (texture != null) {
            texture.release();
        }
    }

    public void switchCamera() {
        cameraId = cameraId == 0 ? 1 : 0;
        open(cameraId);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraDrawer.onSurfaceCreated(gl, config);
        if (!isSetParm) {
            open(cameraId);
            stickerInit();
        }
        mCameraDrawer.setPreviewSize(dataWidth, dataHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraDrawer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (isSetParm) {
            mCameraDrawer.onDrawFrame(gl);
        }
    }

    */
/**
     * 每次Activity onResume时被调用,第一次不会打开相机
     *//*

    @Override
    public void onResume() {
        super.onResume();
        if (isSetParm) {
            open(cameraId);
        }
    }

    public void onDestroy() {
        if (mCamera != null) {
            mCamera.close();
        }
        mOrientationListener.disable();
    }

    */
/**
     * 摄像头聚焦
     *//*

    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
//        mCamera.onFocus(point,callback);
    }

    public int getCameraId() {
        return cameraId;
    }

    public int getBeautyLevel() {
        return mCameraDrawer.getBeautyLevel();
    }

    public void changeBeautyLevel(final int level) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.changeBeautyLevel(level);
            }
        });
    }


    private void stickerInit() {
        if (!isSetParm && dataWidth > 0 && dataHeight > 0) {
            isSetParm = true;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    */
/**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     *//*

    private void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(getCameraId(), cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        WindowManager wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: // This is display orientation
                degrees = 90; // This is camera orientation
                break;
            case Surface.ROTATION_90:
                degrees = 0;
                break;
            case Surface.ROTATION_180:
                degrees = 270;
                break;
            case Surface.ROTATION_270:
                degrees = 180;
                break;
            default:
                degrees = 90;
                break;
        }
        mCamera.setDisplayOrientation(degrees);

       */
/* int displayOrientation;

        // CameraInfo.Orientation is the angle relative to the natural position of the device
        // in clockwise rotation (angle that is rotated clockwise from the natural position)
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }
        Log.i("nadiee", "rotation:"+rotation+ ", degrees:"+degrees + ", displayOrientation:"+displayOrientation);
        if (rotation == 1) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(displayOrientation);
        }*//*

    }
    */
/**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     *//*

    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
            determineDisplayOrientation();
        }

        */
/**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         *//*

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }
    }
}
*/
