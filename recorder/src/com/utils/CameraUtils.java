/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.utils;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.glgjing.recorder.CameraWrapper;
import com.glgjing.recorder.RecordApplication;
import com.huya.record.floatview.FloatView;
import com.huya.record.floatview.FloatWindow;
import com.huya.record.floatview.MoveType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.glgjing.recorder.CameraWrapper.TAG;

/**
 * Camera-related utility functions.
 */
public class CameraUtils {
    /**
     * Attempts to find a preview size that matches the provided width and height (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size for video.
     * <p>
     * TODO: should do a best-fit match, e.g.
     * https://github.com/commonsguy/cwac-camera/blob/master/camera/src/com/commonsware/cwac/camera/CameraUtils.java
     */
    public static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        //for (Camera.Size size : parms.getSupportedPreviewSizes()) {
        //    Log.d(TAG, "supported: " + size.width + "x" + size.height);
        //}

        /*for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                return;
            }
        }*/
        Camera.Size s = null;
        float minRate = 100, rate;
        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
//            Log.i("nadiee", String.format("size(%d, %d)", size.width, size.height));
            /*if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                return;
            }*/
            rate = (float)size.width/size.height;
            if (rate < minRate) {
                minRate = rate;
                s = size;
            }
        }
        if (minRate > 0) {
            parms.setPreviewSize(s.width,  s.height);
            return;
        }
        Log.w(TAG, "Unable to set preview size to " + width + "x" + height);
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            Log.d(TAG, "Camera preferred preview size for video is " +
                    ppsfv.width + "x" + ppsfv.height);
        }
        if (ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height);
        }
        // else use whatever the default size is
    }

    /**
     * Attempts to find a fixed preview frame rate that matches the desired frame rate.
     * <p>
     * It doesn't seem like there's a great deal of flexibility here.
     * <p>
     * TODO: follow the recipe from http://stackoverflow.com/questions/22639336/#22645327
     *
     * @return The expected frame rate, in thousands of frames per second.
     */
    public static int chooseFixedPreviewFps(Camera.Parameters parms, int desiredThousandFps) {
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            //Log.d(TAG, "entry: " + entry[0] + " - " + entry[1]);
            if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }

        int[] tmp = new int[2];
        parms.getPreviewFpsRange(tmp);
        int guess;
        if (tmp[0] == tmp[1]) {
            guess = tmp[0];
        } else {
            guess = tmp[1] / 2;     // shrug
        }

        Log.d(TAG, "Couldn't find match for " + desiredThousandFps + ", using " + guess);
        return guess;
    }


    public static void showCamera() {
        // 创建一个surfaceview窗口
        final CameraWrapper cameraView = new CameraWrapper(RecordApplication.getInstance());
        FloatWindow
                .with(RecordApplication.getInstance())
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
