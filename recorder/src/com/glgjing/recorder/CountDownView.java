package com.glgjing.recorder;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huya.record.floatview.FloatWindow;
import com.widget.CameraView;


public class CountDownView extends RelativeLayout {

    public static final String TAG = "nadiee";

    private Context mContext;
    private TextView txt;
    private android.os.Handler mainHandler;
    private int countdown = 3;
    private android.os.Handler handler = new android.os.Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            if (--countdown < 1) {
                if (mainHandler != null) {
                    mainHandler.sendEmptyMessage(0);
                    mainHandler = null;
                    WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null) {
                        windowManager.removeView((View) getParent());
                    }
                    FloatWindow.destroy("countdown");
                }
            } else {
                txt.setText(String.valueOf(countdown));
                handler.sendEmptyMessageDelayed(1,1000);
            }
        }
    };

    public CountDownView(Context context) {
        super(context);
        init(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.count_down_view, null);
        txt = view.findViewById(R.id.txt);
        addView(view);
        handler.sendEmptyMessageDelayed(1, 1200);
    }

    public void setMainHandler(Handler handler) {
        mainHandler = handler;
    }
}
