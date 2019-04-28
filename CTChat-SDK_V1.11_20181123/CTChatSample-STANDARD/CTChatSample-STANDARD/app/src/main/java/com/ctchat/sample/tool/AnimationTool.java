package com.ctchat.sample.tool;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public enum AnimationTool {
    INSTANCE;

    private int duration = 200;
    private int depth = 200;
    private int centerX;
    private int centerY;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void startSelectAnimation(ImageView view, int resId) {
        Rotate3dAnimation openRotateAnimation = initOpenAnim(view, resId);
        view.startAnimation(openRotateAnimation);
    }

    public void startCancleAnimation(ImageView view, int resId) {
        Rotate3dAnimation openRotateAnimation = initCloseAnim(view, resId);
        view.startAnimation(openRotateAnimation);
    }

    public Rotate3dAnimation initOpenAnim(final ImageView img, final int resId) {
        centerX = img.getWidth()/2;
        centerY = img.getHeight()/2;
        Rotate3dAnimation openRotateAnimation = new Rotate3dAnimation(0, 90, centerX, centerY, depth, true);
        openRotateAnimation.setDuration(duration);
        openRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        openRotateAnimation.setFillAfter(false);
        openRotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img.setImageResource(resId);
                Rotate3dAnimation endAnim = new Rotate3dAnimation(270, 360, centerX, centerY, depth, false);
                endAnim.setDuration(duration);
                endAnim.setInterpolator(new DecelerateInterpolator());
                endAnim.setFillAfter(true);
                img.startAnimation(endAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return openRotateAnimation;
    }

    public Rotate3dAnimation initCloseAnim(final ImageView img, final int resId) {
        centerX = img.getWidth()/2;
        centerY = img.getHeight()/2;
        Rotate3dAnimation closeRorateAnimation = new Rotate3dAnimation(360, 270, centerX, centerY, depth, true);
        closeRorateAnimation.setDuration(duration);
        closeRorateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        closeRorateAnimation.setFillAfter(false);
        closeRorateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img.setImageResource(resId);
                Rotate3dAnimation endAnim = new Rotate3dAnimation(90, 0, centerX, centerY, depth, false);
                endAnim.setDuration(duration);
                endAnim.setInterpolator(new DecelerateInterpolator());
                endAnim.setFillAfter(true);
                img.startAnimation(endAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        return closeRorateAnimation;
    }
}
