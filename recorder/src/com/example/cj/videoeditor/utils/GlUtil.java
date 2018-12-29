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

package com.example.cj.videoeditor.utils;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.utils.LOG;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * Some OpenGL utility functions.
 */
public class GlUtil {

    /**
     * Identity matrix for general use.  Don't modify or life will get weird.
     */
    public static final float[] IDENTITY_MATRIX;

    static {
        IDENTITY_MATRIX = new float[16];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

    private static final int SIZEOF_FLOAT = 4;

    private GlUtil() {
    }     // do not instantiate

    /**
     * Creates a new program from the supplied vertex and fragment shaders.
     *
     * @return A handle to the program, or 0 on failure.
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();// create empty OpenGL ES Program
        checkGlError("glCreateProgram");
        if (program == 0) {
            LOG.logE("Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);// add the vertex shader to program
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);// add the fragment shader to program
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);// creates OpenGL ES program executables
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            LOG.logE("Could not link program: ");
            LOG.logE(GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * Compiles the provided shader source.
     *
     * @return A handle to the shader, or 0 on failure.
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            LOG.logE("Could not compile shader " + shaderType + ":");
            LOG.logE(" " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            LOG.logE(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     * <p>
     * Throws a RuntimeException if the location is invalid.
     */
    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    /**
     * Creates a texture from raw data.
     *
     * @param data   Image data, in a "direct" ByteBuffer.
     * @param width  Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    public static int createImageTexture(ByteBuffer data, int width, int height, int format) {
        int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        GlUtil.checkGlError("glGenTextures");

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("loadImageTexture");

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format,
                GLES20.GL_UNSIGNED_BYTE, data);
        GlUtil.checkGlError("loadImageTexture");

        return textureHandle;
    }

    public static int createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     * Writes GL version info to the log.
     */
    public static void logVersionInfo() {
        LOG.logE("vendor  : " + GLES20.glGetString(GLES20.GL_VENDOR));
        LOG.logE("renderer: " + GLES20.glGetString(GLES20.GL_RENDERER));
        LOG.logE("version : " + GLES20.glGetString(GLES20.GL_VERSION));

    }

    public enum  Transformation {
        FLIP_HORIZONTAL(1), FLIP_VERTICAL(2), FLIP_HORIZONTAL_VERTICAL(3), FLIP_NONE(4),
        SCALE_TYPE_FIT_XY(10), SCALE_TYPE_CENTER_CROP(11), SCALE_TYPE_CENTER_INSIDE(12);
        public int value;

        Transformation(int value) {
            this.value = value;
        }
    }

    public static void cropSquare(float[]textureCoord, float inputWidth, float inputHeight) {
        Log.i("nadiee", "GIUtil-cropSquare-inputWidth:"+inputWidth + ", inputHeight:"+inputHeight);
        if (inputWidth == inputHeight || inputHeight ==0 || inputWidth == 0) { // 已经是方形无需处理
            return;
        }
        float extraRate;
        if (inputWidth < inputHeight) {
            extraRate = (float) ((inputHeight - inputWidth)*0.9 / inputHeight);
            textureCoord[0] = extraRate;
            textureCoord[2] = 1 - extraRate;
            textureCoord[4] = extraRate;
            textureCoord[6] = 1 - extraRate;
        } else {
            extraRate = (inputWidth - inputHeight) / inputWidth;
            textureCoord[1] = extraRate/2;
            textureCoord[3] = extraRate/2;
            textureCoord[5] = 1 - extraRate/2;
            textureCoord[7] = 1 - extraRate/2;
        }
        Log.i("nadiee", "rate:"+extraRate);
    }

    public static void scale(float[]textureCoords, int inputWidth, int inputHeight, int outputWidth, int outputHeight, int scaleType) {
        if (scaleType == Transformation.SCALE_TYPE_FIT_XY.value) {
            // The default is FIT_XY
            return;
        }

        // Note: scale type need to be implemented by adjusting
        // the textureCoords (not textureCoords).
        if (inputWidth * outputHeight == inputHeight * outputWidth) {
            // Optional optimization: If input w/h aspect is the same as output's,
            // there is no need to adjust textureCoords at all.
            return;
        }

        float inputAspect = inputWidth / (float) inputHeight;
        float outputAspect = outputWidth / (float) outputHeight;

        if (scaleType == Transformation.SCALE_TYPE_CENTER_CROP.value) {
            if (inputAspect < outputAspect) {
                float heightRatio = outputAspect / inputAspect;
                textureCoords[1] *= heightRatio;
                textureCoords[3] *= heightRatio;
                textureCoords[5] *= heightRatio;
                textureCoords[7] *= heightRatio;
            } else {
                float widthRatio = inputAspect / outputAspect;
                textureCoords[0] *= widthRatio;
                textureCoords[2] *= widthRatio;
                textureCoords[4] *= widthRatio;
                textureCoords[6] *= widthRatio;
            }
        } else if (scaleType == Transformation.SCALE_TYPE_CENTER_INSIDE.value) {
            if (inputAspect < outputAspect) {
                float widthRatio = inputAspect / outputAspect;
                textureCoords[0] *= widthRatio;
                textureCoords[2] *= widthRatio;
                textureCoords[4] *= widthRatio;
                textureCoords[6] *= widthRatio;
            } else {
                float heightRatio = outputAspect / inputAspect;
                textureCoords[1] *= heightRatio;
                textureCoords[3] *= heightRatio;
                textureCoords[5] *= heightRatio;
                textureCoords[7] *= heightRatio;
            }
        }
    }

    public static void flip(float[] textureCoords, int flip) {
        if (flip == Transformation.FLIP_HORIZONTAL.value) {
            swap(textureCoords, 0, 2);
            swap(textureCoords, 4, 6);
        } else if (flip == Transformation.FLIP_VERTICAL.value) {
            swap(textureCoords, 1, 5);
            swap(textureCoords, 3, 7);
        } else if (flip == Transformation.FLIP_HORIZONTAL_VERTICAL.value) {
            swap(textureCoords, 0, 2);
            swap(textureCoords, 4, 6);

            swap(textureCoords, 1, 5);
            swap(textureCoords, 3, 7);
        }
    }

    public static float[] rotate(float[] textureCoords, int angle) {
        float x, y;
        switch (angle) {
            case 90:
                x = textureCoords[0];
                y = textureCoords[1];
                textureCoords[0] = textureCoords[4];
                textureCoords[1] = textureCoords[5];
                textureCoords[4] = textureCoords[6];
                textureCoords[5] = textureCoords[7];
                textureCoords[6] = textureCoords[2];
                textureCoords[7] = textureCoords[3];
                textureCoords[2] = x;
                textureCoords[3] = y;
                break;
            case 180:
                swap(textureCoords, 0, 6);
                swap(textureCoords, 1, 7);
                swap(textureCoords, 2, 4);
                swap(textureCoords, 3, 5);
                break;
            case 270:
                x = textureCoords[0];
                y = textureCoords[1];
                textureCoords[0] = textureCoords[2];
                textureCoords[1] = textureCoords[3];
                textureCoords[2] = textureCoords[6];
                textureCoords[3] = textureCoords[7];
                textureCoords[6] = textureCoords[4];
                textureCoords[7] = textureCoords[5];
                textureCoords[4] = x;
                textureCoords[5] = y;
                break;
            case Surface.ROTATION_0:
            default:
                break;
        }
        return textureCoords;
    }

    public static float[] swap(float[] arr, int index1, int index2) {
        float tmp = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = tmp;
        return arr;
    }
}
