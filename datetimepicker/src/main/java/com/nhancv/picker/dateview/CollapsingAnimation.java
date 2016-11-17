package com.nhancv.picker.dateview;


import android.view.animation.Animation;
import android.view.animation.Transformation;

class CollapsingAnimation extends Animation {
    private final int targetHeight;
    private final NCalendarView view;
    private final boolean down;
    private int targetGrowRadius;
    private NCalendarController nCalendarController;

    public CollapsingAnimation(NCalendarView view, NCalendarController nCalendarController, int targetHeight, int targetGrowRadius, boolean down) {
        this.view = view;
        this.nCalendarController = nCalendarController;
        this.targetHeight = targetHeight;
        this.targetGrowRadius = targetGrowRadius;
        this.down = down;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float grow = 0;
        int newHeight;
        if (down) {
            newHeight = (int) (targetHeight * interpolatedTime);
            grow = (interpolatedTime * (targetGrowRadius * 2));
        } else {
            float progress = 1 - interpolatedTime;
            newHeight = (int) (targetHeight * progress);
            grow = (progress * (targetGrowRadius * 2));
        }
        nCalendarController.setGrowProgress(grow);
        view.getLayoutParams().height = newHeight;
        view.requestLayout();

    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}