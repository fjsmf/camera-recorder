package com.huya.record.floatview;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

class FloatPhone extends FloatView {

    private final Context mContext;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private MyFrameLayout mViewWrapper;
    private View mView;
    private int mX, mY;
    private String mTag;

    class MyFrameLayout extends FrameLayout {
        private ViewLifeListener mViewLifeListener;
        int orientation = 1;
        @Override
        protected void onAttachedToWindow() {
            if (mViewLifeListener != null) {
                mViewLifeListener.onAttachedToWindow();
            }
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            if (mViewLifeListener != null) {
                mViewLifeListener.onDetachedFromWindow();
            }
            FloatWindow.destroy(mTag);
            super.onDetachedFromWindow();
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            if (newConfig != null && newConfig.orientation != orientation) {
                if (mViewLifeListener != null) {
                    mViewLifeListener.onOritationChange();
                }
            }
            orientation = newConfig.orientation;
        }
        public MyFrameLayout(Context context) {
            super(context);
        }

        public MyFrameLayout(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void setViewLifeListener(ViewLifeListener lifeListener) {
            mViewLifeListener = lifeListener;
        }
    }

    FloatPhone(Context applicationContext, String tag) {
        mContext = applicationContext;
        mTag = tag;
        mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity  = Gravity.CENTER;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN   // 可在全屏幕布局, 不受状态栏影响
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;  // 最初不可获取焦点, 这样不影响底层应用接收触摸事件
        mLayoutParams.windowAnimations = 0;
    }

    @Override
    public void setSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
    }

    @Override
    public void setView(View view, int type) {
        mView = view;
        mType = type;
        if (mType == TYPE_OPERATOR) {
            mViewWrapper = new MyFrameLayout(view.getContext());
            mViewWrapper.addView(view);
        }
    }

    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mLayoutParams.gravity = gravity;
        if (xOffset > 0) {
            mLayoutParams.x = mX = xOffset;
        }
        if (yOffset > 0) {
            mLayoutParams.y = mY = yOffset;
        }
    }


    @Override
    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            req();
        } else if (Miui.rom()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                req();
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                Miui.req(mContext, new PermissionListener() {
                    @Override
                    public void onSuccess() {
                        if (TYPE_OPERATOR == mType) {
                            mWindowManager.addView(mViewWrapper, mLayoutParams);
                        } else {
                            mWindowManager.addView(mView, mLayoutParams);
                        }
//                        mWindowManager.addView(mView, mLayoutParams);
                    }

                    @Override
                    public void onFail() {
                    }
                });
            }
        } else {
            try {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                if (TYPE_OPERATOR == mType) {
                    mWindowManager.addView(mViewWrapper, mLayoutParams);
                } else {
                    mWindowManager.addView(mView, mLayoutParams);
                }
//                mWindowManager.addView(mView, mLayoutParams);
            } catch (Exception e) {
                if (TYPE_OPERATOR == mType) {
                    mWindowManager.removeView(mViewWrapper);
                } else {
                    mWindowManager.removeView(mView);
                }
                mWindowManager.removeView(mView);
                LogUtil.e("TYPE_TOAST 失败");
                req();
            }
        }
    }

    @Override
    public void setViewLifeListener(ViewLifeListener lifeListener) {
        if (mViewWrapper != null) {
            mViewWrapper.setViewLifeListener(lifeListener);
        }
    }

    private void req() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        FloatPermissionActivity.request(mContext, new PermissionListener() {
            @Override
            public void onSuccess() {
                if (TYPE_OPERATOR == mType) {
                    mWindowManager.addView(mViewWrapper, mLayoutParams);
                } else {
                    mWindowManager.addView(mView, mLayoutParams);
                }
            }

            @Override
            public void onFail() {
            }
        });
    }

    @Override
    public void dismiss() {
        if (TYPE_OPERATOR == mType) {
            mWindowManager.removeView(mViewWrapper);
        } else {
            mWindowManager.removeView(mView);
        }
//        mWindowManager.removeView(mView);
    }

    @Override
    public void updateXY(int x, int y) {
        mLayoutParams.x = mX = x;
        mLayoutParams.y = mY = y;
        if (TYPE_OPERATOR == mType) {
            mWindowManager.updateViewLayout(mViewWrapper, mLayoutParams);
        } else {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
//        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    void updateX(int x) {
        mLayoutParams.x = mX = x;
        if (TYPE_OPERATOR == mType) {
            mWindowManager.updateViewLayout(mViewWrapper, mLayoutParams);
        } else {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
//        mWindowManager.updateViewLayout(mView, mLayoutParams);

    }

    @Override
    void updateY(int y) {
        mLayoutParams.y = mY = y;
        if (TYPE_OPERATOR == mType) {
            mWindowManager.updateViewLayout(mViewWrapper, mLayoutParams);
        } else {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
//        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    void updateSize(int width, int height){
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        if (TYPE_OPERATOR == mType) {
            mWindowManager.updateViewLayout(mViewWrapper, mLayoutParams);
        } else {
            mWindowManager.updateViewLayout(mView, mLayoutParams);
        }
    }

    @Override
    int getX() {
        return mX;
    }

    @Override
    int getY() {
        return mY;
    }


}
