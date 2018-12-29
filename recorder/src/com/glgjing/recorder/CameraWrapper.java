package com.glgjing.recorder;


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.huya.record.floatview.FloatWindow;
import com.huya.record.floatview.IFloatWindow;
import com.widget.*;
import com.widget.CameraView;

import java.io.IOException;
import java.util.List;

public class CameraWrapper extends RelativeLayout implements View.OnClickListener {

    public static final String TAG = "nadiee";

    private com.widget.CameraView mCameraView;
    private Context mContext;
    private View cameraSwitch, close, view;
    private boolean showOper;
    private static android.os.Handler handler = new Handler();
    private FrameLayout fl;

    public CameraWrapper(Context context) {
        super(context);
        init(context);
    }

    public CameraWrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraWrapper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CameraWrapper(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.squarecamera_fragment_camera2, null);
        addView(view);

        cameraSwitch = view.findViewById(R.id.iv_change_camera);
        cameraSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.switchCamera();
                if (mCameraView.getCameraId() == 1) {
                    //前置摄像头 使用美颜
                    mCameraView.changeBeautyLevel(5);
                } else {
                    //后置摄像头不使用美颜
                    mCameraView.changeBeautyLevel(0);
                }
            }
        });

        close = view.findViewById(R.id.iv_close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                wm.removeView((View) getParent());
            }
        });
    }

    private void initCameraView() {
        Log.i("nadiee", "CameraWrapper-initCameraView()");
        fl = view.findViewById(R.id.fl);
        mCameraView = view.findViewById(R.id.camera_preview_view);
        mCameraView.onResume();
        if (mCameraView.getCameraId() == 1) {
            //前置摄像头 使用美颜
            mCameraView.changeBeautyLevel(5);
        } else {
            //后置摄像头不使用美颜
            mCameraView.changeBeautyLevel(0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initCameraView();
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.i("nadiee", "CameraWrapper-onDetachedFromWindow()");
        mCameraView.onDestroy();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("nadiee", "CameraWrapper-onConfigurationChanged()");
       /* if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation;
            mCameraView.releaseCamera();
            fl.removeView(mCameraView);
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initCameraView();
                }
            }, 20);
        }*/
    }

    @Override
    public void onClick(View v) {
        if (showOper) {
            showOper = false;
            close.setVisibility(GONE);
            cameraSwitch.setVisibility(GONE);
        } else {
            showOper = true;
            close.setVisibility(VISIBLE);
            cameraSwitch.setVisibility(VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    close.setVisibility(GONE);
                    cameraSwitch.setVisibility(GONE);
                    showOper = false;
                }
            }, 3000);
        }
    }

}
