package com.glgjing.recorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.Toast;

import com.huya.record.floatview.FloatWindow;
import com.huya.record.floatview.MoveType;
import com.utils.CameraUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecordService extends Service {
    private static final String ACTION_PREFIX = "nf_";
    private static final String ACTION_RECORD_START = "nf_action_record_start";
    private static final String ACTION_RECORD_STOP = "nf_action_record_stop";
    private static final String ACTION_RECORD_PAUSE = "nf_action_record_pause";
    private static final String ACTION_RECORD_RESUME = "nf_action_record_resume";
    private static final String ACTION_GOTO_VIDEO = "nf_action_goto_video";
    private static final String ACTION_RECORD_SETTING = "nf_action_record_setting";
    private static final String ACTION_SCREEN_CAPTURE = "nf_action_screen_capture";
    private static final String ACTION_RECORD_CLOSE = "nf_action_record_close";
    private static final int NOTIFICATION_REQUEST_CODE = 850115;
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;

    private int width = 720;
    private int height = 1080;
    private int dpi;
    private NotificationManager nfManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private int notifyId;
    private static RecordState recordState = RecordState.IDLE;
    private static List<RecordListener> recordListeners = new ArrayList<>();

    public interface RecordListener {
        void onRecordStart();
        void onRecordStop();
    }

    private enum RecordState {
        IDLE(0, "未录制"), RECORDING(1, "录制中");

        public String name;
        public int code;

        RecordState(int code, String name) {
            this.name = name;
            this.code = code;
        }
    }

    public static void addRecordListener(RecordListener listener) {
        if (listener != null) {
            recordListeners.add(listener);
        }
    }

    public static void removeRecordListener(RecordListener listener){
        recordListeners.remove(listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().startsWith(ACTION_PREFIX)) {
            precessIntent(intent);
        } else {
            setupNotification(startId);
        }
        return START_STICKY;
    }

    private void precessIntent(Intent intent) {
        if (intent == null || intent.getAction() == null || remoteViews == null) return;
        String action = intent.getAction();
        if (action.equals(ACTION_RECORD_START)) {
            remoteViews.setViewVisibility(R.id.nf_record_start, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_close, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_goto_app_video, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_record_stop, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.nf_record_pause, View.VISIBLE);
           /* Intent intent1 = new Intent(this, FloatEmptyActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);*/
            Intent intent2 = new Intent(getApplicationContext(), FloatEmptyActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent2);

        } else if (action.equals(ACTION_RECORD_STOP)) {
            remoteViews.setViewVisibility(R.id.nf_record_start, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.nf_close, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.nf_goto_app_video, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.nf_record_stop, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_record_pause, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_record_resume, View.GONE);

            stopRecord();
        } else if (action.equals(ACTION_RECORD_PAUSE)) {
            remoteViews.setViewVisibility(R.id.nf_record_pause, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_record_resume, View.VISIBLE);

            stopRecord();
        } else if (action.equals(ACTION_RECORD_RESUME)) {
            remoteViews.setViewVisibility(R.id.nf_record_resume, View.GONE);
            remoteViews.setViewVisibility(R.id.nf_record_pause, View.VISIBLE);

            Intent intent1 = new Intent(getApplicationContext(), FloatEmptyActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent1);
        } else if (action.equals(ACTION_RECORD_SETTING)) {
            showSettingDialog();
        } else if (action.equals(ACTION_SCREEN_CAPTURE)) {
            shotScreen();
        } else if (action.equals(ACTION_RECORD_CLOSE)) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        // 同步到通知栏
        nfManager.notify(notifyId, notification);
    }

    private void shotScreen() {
        Toast.makeText(getApplicationContext(), "shot screen", Toast.LENGTH_SHORT).show();
    }

    private void showSettingDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.record_setting_view, null);
        View record_setting_close = view.findViewById(R.id.record_setting_close);
        record_setting_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                wm.removeView((View) view.getParent());
            }
        });

        Switch record_camera_switch, record_show_touch_switch;
        record_camera_switch = view.findViewById(R.id.record_camera_switch);
        record_camera_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUtils.showCamera();
            }
        });
        record_show_touch_switch = view.findViewById(R.id.record_show_touch_switch);
        record_show_touch_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        FloatWindow
                .with(getApplicationContext())
                .setView(view)
                .setMoveType(MoveType.inactive)
                .setGravity(Gravity.CENTER)
                .setDesktopShow(true)
                .build();
        Toast.makeText(getApplicationContext(), "showSettingdialog", Toast.LENGTH_SHORT).show();
    }

    private void setupNotification(int notificationId) {
        notifyId = notificationId;
        //初始化通知管理者
        nfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext()); //获取一个Notification构造器
        builder.setContentTitle("");
        builder.setContentText("");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        remoteViews = new RemoteViews(getPackageName(), R.layout.record_remote_view);
        setupClick(R.id.nf_record_start, ACTION_RECORD_START);
        setupClick(R.id.nf_record_stop, ACTION_RECORD_STOP);
        setupClick(R.id.nf_record_resume, ACTION_RECORD_RESUME);
        setupClick(R.id.nf_record_pause, ACTION_RECORD_PAUSE);
        setupClick(R.id.nf_record_setting, ACTION_RECORD_SETTING);
//        setupClick(R.id.nf_goto_app_video, ACTION_GOTO_VIDEO);
        setupClick(R.id.nf_screen_capture, ACTION_SCREEN_CAPTURE);
        setupClick(R.id.nf_close, ACTION_RECORD_CLOSE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_GOTO_VIDEO);
        PendingIntent pIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.nf_goto_app_video, pIntent);

        builder.setContent(remoteViews);
        notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(notificationId, notification);
    }

    private void setupClick(int resId, String action) {
        Intent intent = new Intent(this, RecordService.class);
        intent.setAction(action);
        //不同控件的requestCode需要区分开 getActivity broadcoast同理
        PendingIntent pIntent = PendingIntent.getService(this, NOTIFICATION_REQUEST_CODE, intent, 0);
        remoteViews.setOnClickPendingIntent(resId, pIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        mediaRecorder = new MediaRecorder();
    }


    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }


    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public static boolean isRecording() {
        return recordState == RecordState.RECORDING;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public boolean startRecord() {
        Log.i("nadiee", "RecordService-startRecord");
        for (RecordListener listener : recordListeners) {
            listener.onRecordStart();
        }
        if (mediaProjection == null || recordState == RecordState.RECORDING) {
            return false;
        }
        recordState = RecordState.RECORDING;
        initRecorder();
        createVirtualDisplay();
        mediaRecorder.start();
        return true;
    }

    public boolean stopRecord() {
        Log.i("nadiee", "RecordService-stopRecord");
        for (RecordListener listener : recordListeners) {
            listener.onRecordStop();
        }
        if (recordState != RecordState.RECORDING) {
            return false;
        }
        recordState = RecordState.IDLE;
        mediaRecorder.stop();
        mediaRecorder.reset();
        virtualDisplay.release();
        mediaProjection.stop();
        Log.i("nadiee", "RecordService-stopRecord-end");
        return true;
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(getsaveDirectory() + System.currentTimeMillis() + ".mp4");
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
//        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getsaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "aScreenRecord" + "/";

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }

            return rootDir;
        } else {
            return null;
        }
    }

    public class RecordBinder extends Binder {
        public RecordService getRecordService() {
            return RecordService.this;
        }
    }
}