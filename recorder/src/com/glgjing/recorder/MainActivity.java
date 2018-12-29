package com.glgjing.recorder;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import com.example.cj.videoeditor.utils.GlUtil;
import com.huya.record.floatview.FloatView;
import com.huya.record.floatview.FloatWindow;
import com.huya.record.floatview.MoveType;
import com.huya.record.floatview.Screen;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainActivity extends AppCompatActivity {
    boolean hasSettingPermission;
    android.os.Handler handler = new android.os.Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            showCamera();
        }
    };

    public int getImageId(){
        Log.i("nadiee", "getImageId");
        return R.drawable.durec_camera;
    }
    private void onVibrator() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            Vibrator localVibrator = (Vibrator) getApplicationContext()
                    .getSystemService(VIBRATOR_SERVICE);
            vibrator = localVibrator;
        }
        vibrator.vibrate(100L);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        findViewById(R.id.start_record_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordView();
                showCountDown();
                finish();
            }
        });

        startService(new Intent(getApplicationContext(), RecordService.class));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_SETTINGS}, 104);
        } else {
            hasSettingPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 104) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                hasSettingPermission = true;
            }
        }
    }
    private void showRecordView() {
        if (FloatWindow.isShowing()) {
            return;
        }
        final ImageView floatView = new ImageView(getApplicationContext());
        floatView.setBackgroundResource(R.drawable.float_record_start);

        FloatWindow
                .with(getApplicationContext())
                .setView(floatView)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (RecordService.isRecording()) {
                            if (v.getTag() != null && v.getTag() instanceof RecordService) {
                                RecordService recordService = (RecordService) v.getTag();
                                recordService.stopRecord();
                            }
                        } else {
                            v.getContext().startActivity(new Intent(getApplicationContext(), FloatEmptyActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }
                })
                .setViewLifeListener(new FloatView.ViewLifeListener() {
                    RecordService mRecordService;
                    int touchSwitch = 0;
                    ServiceConnection connection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName className, IBinder service) {
                            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                            DisplayMetrics metrics = new DisplayMetrics();
                            wm.getDefaultDisplay().getMetrics(metrics);
                            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
                            mRecordService = binder.getRecordService();
                            mRecordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
                            RecordService.addRecordListener(listener);
                            floatView.setTag(mRecordService);
                            floatView.setBackgroundResource(mRecordService.isRecording() ? R.drawable.float_record_stop : R.drawable.float_record_start);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName arg0) {
                        }
                    };
                    RecordService.RecordListener listener = new RecordService.RecordListener() {
                        @Override
                        public void onRecordStart() {
                            floatView.setBackgroundResource(R.drawable.float_record_stop);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (hasSettingPermission && Settings.System.canWrite(getApplicationContext())) {
                                    touchSwitch = Settings.System.getInt(getApplicationContext().getContentResolver(), "show_touches", 0);
                                    Settings.System.putInt(getApplicationContext().getContentResolver(),"show_touches", 1);
                                }
                            } else {
                                touchSwitch = Settings.System.getInt(getApplicationContext().getContentResolver(), "show_touches", 0);
                                Settings.System.putInt(getApplicationContext().getContentResolver(),"show_touches", 1);
                            }
                        }
                        @Override
                        public void onRecordStop() {
                            floatView.setBackgroundResource(R.drawable.float_record_start);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (hasSettingPermission && Settings.System.canWrite(getApplicationContext())) {
                                    Settings.System.putInt(getApplicationContext().getContentResolver(),"show_touches", touchSwitch);
                                }
                            } else {
                                Settings.System.putInt(getApplicationContext().getContentResolver(),"show_touches", touchSwitch);
                            }

                        }
                    };
                    ShakeListener shakeListener = new ShakeListener(getApplicationContext());//创建一个对象

                    public void onAttachedToWindow() {
                        // 需要解绑service
                        Intent intent = new Intent(getApplicationContext(), RecordService.class);
                        getApplicationContext().bindService(intent, connection, BIND_AUTO_CREATE);
                        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener(){//调用setOnShakeListener方法进行监听
                            public void onShake() {
                                //对手机摇晃后的处理（如换歌曲，换图片，震动……）
                                onVibrator();
                                if (RecordService.isRecording()) {
                                    if (floatView.getTag() != null && floatView.getTag() instanceof RecordService) {
                                        RecordService recordService = (RecordService) floatView.getTag();
                                        recordService.stopRecord();
                                    }
                                } else {
                                    floatView.getContext().startActivity(new Intent(getApplicationContext(), FloatEmptyActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                }
                            }
                        });
                        shakeListener.start();
                    }
                    public void onDetachedFromWindow() {
                        getApplicationContext().unbindService(connection);
                        shakeListener.stop();
                    }

                    @Override
                    public void onOritationChange() {

                    }
                })
                .setWidth(100)
                .setHeight(100)
                .setX(Screen.width - 100)
                .setY(Screen.height / 2)
                .setMoveType(MoveType.active)
                .setMoveStyle(500, new BounceInterpolator())
                .setDesktopShow(true)
                .build();

        // 创建一个surfaceview窗口
        /*final CameraView cameraView = new CameraView(getApplicationContext());
        cameraView.setBackgroundColor(0xff00ff00);
        View cv = cameraView.getView();
        FloatWindow
                .with(getApplicationContext())
                .setView(cameraView)
                .setTag("surfaceview")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       cameraView.onClick();
                    }
                })
                .setViewLifeListener(new FloatView.ViewLifeListener() {
                    public void onAttachedToWindow() {
                    }
                    public void onDetachedFromWindow() {
                    }
                    @Override
                    public void onOritationChange() {
                        cameraView.onOritationChange();
                    }
                })
                .setWidth(300)
                .setHeight(400)
                .setX(0)
                .setY(0)
                .setMoveType(MoveType.active)
                .setMoveStyle(500, new BounceInterpolator())
                .setDesktopShow(true)
                .build();
        showCamera();*/
//        new android.os.Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            }
//        }, 1000);
    }
    private void showCountDown() {
        CountDownView view = new CountDownView(getApplicationContext());
        view.setMainHandler(handler);
        FloatWindow
                .with(getApplicationContext())
                .setView(view)
                .setTag("countdown")
                .setGravity(Gravity.CENTER)
                .setMoveType(MoveType.inactive)
                .setDesktopShow(true)
                .build();
    }
    private void showCamera() {
        // 创建一个surfaceview窗口
        final CameraWrapper cameraView = new CameraWrapper(getApplicationContext());
        FloatWindow
                .with(getApplicationContext())
                .setView(cameraView)
                .setTag("surfaceview")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraView.onClick(v);
                    }
                })
                .setViewLifeListener(new FloatView.ViewLifeListener() {
                    public void onAttachedToWindow() {

                    }
                    public void onDetachedFromWindow() {
                    }
                    @Override
                    public void onOritationChange() {
//                        cameraView.onOritationChange();
                    }
                })
                .setWidth(400)
                .setHeight(400)
                .setGravity(Gravity.TOP|Gravity.LEFT)
                .setMoveType(MoveType.active)
                .setMoveStyle(500, new BounceInterpolator())
                .setDesktopShow(true)
                .build();
    }
}
