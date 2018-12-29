package com.glgjing.recorder;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.huya.record.floatview.IFloatWindowImpl;


public class FloatEmptyActivity extends Activity {
    private static final int RECORD_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE = 103;
    private static final int SETTING_REQUEST_CODE = 104;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_SETTINGS}, AUDIO_REQUEST_CODE);
        }

        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent captureIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    captureIntent = projectionManager.createScreenCaptureIntent();
                }
                startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
            }
        }, 50);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i("nadiee", "onActivityResult");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            }
            recordService.setMediaProject(mediaProjection);

            recordService.startRecord();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults == null || grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
