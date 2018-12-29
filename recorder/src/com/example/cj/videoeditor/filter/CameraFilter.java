package com.example.cj.videoeditor.filter;

import android.content.res.Resources;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.example.cj.videoeditor.utils.GlUtil;
import com.filter.OesFilter;
import com.glgjing.recorder.RecordApplication;

import static android.content.Context.WINDOW_SERVICE;


/**
 * Description:
 */
public class CameraFilter extends OesFilter {

    private float previewWidth;
    private float previewHeight;
    float[] coord;

    public CameraFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    public void setFlag(int flag) {
        super.setFlag(flag);
        coord = new float[]{
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };

        Display display = ((WindowManager) RecordApplication.getInstance().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        Log.i("nadiee", "CameraFilter-display rotation:"+display.getRotation());
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
//                GlUtil.rotate(coord, 90);
//                GlUtil.cropSquare(coord, previewWidth, previewHeight);
                GlUtil.flip(coord, GlUtil.Transformation.FLIP_VERTICAL.value);
                needcrop =false;
                break;
            case Surface.ROTATION_90:
                GlUtil.cropSquare(coord, previewWidth, previewHeight);
                GlUtil.flip(coord, GlUtil.Transformation.FLIP_HORIZONTAL.value);
                GlUtil.rotate(coord, 90);

                break;
            case Surface.ROTATION_180:
                break;
            case Surface.ROTATION_270:
                GlUtil.cropSquare(coord, previewWidth, previewHeight);
                GlUtil.flip(coord, GlUtil.Transformation.FLIP_HORIZONTAL.value);
                GlUtil.rotate(coord, 270);
                break;
        }
        mTexBuffer.clear();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);
    }
    boolean needcrop = false;
    @Override
    protected void onSizeChanged(int width, int height) {
        Log.i("nadiee", "CameraFilter-onSizeChanged-width:"+width + ", height:"+height);
        previewWidth = width;
        previewHeight = height;
        needcrop = true;
        setFlag(0);
        needcrop = false;


    }
}
