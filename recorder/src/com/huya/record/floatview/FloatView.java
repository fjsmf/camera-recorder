package com.huya.record.floatview;

import android.view.View;


public abstract class FloatView {
    public static final int TYPE_NOR = 1;
    public static final int TYPE_OPERATOR = 2;
    public int mType;

    public interface ViewLifeListener {
        void onAttachedToWindow();
        void onDetachedFromWindow();
        void onOritationChange();
    }
    abstract void setSize(int width, int height);

    abstract void setView(View view, int type);
    abstract void setViewLifeListener(ViewLifeListener lifeListener);

    abstract void setGravity(int gravity, int xOffset, int yOffset);

    abstract void init();

    abstract void dismiss();

    void updateXY(int x, int y) {
    }

    void updateX(int x) {
    }

    void updateY(int y) {
    }

    void updateSize(int width, int height) {

    }

    int getX() {
        return 0;
    }

    int getY() {
        return 0;
    }
}
