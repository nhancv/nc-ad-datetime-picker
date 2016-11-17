package com.nhancv.picker.dateview;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;

class AnimationHandler {

    public static final int HEIGHT_ANIM_DURATION_MILLIS = 650;
    public static final int INDICATOR_ANIM_DURATION_MILLIS = 600;
    private NCalendarController nCalendarController;
    private NCalendarView calendarView;

    AnimationHandler(NCalendarController nCalendarController, NCalendarView calendarView) {
        this.nCalendarController = nCalendarController;
        this.calendarView = calendarView;
    }

    void openCalendar() {
        Animation heightAnim = getCollapsingAnimation(true);
        heightAnim.setDuration(HEIGHT_ANIM_DURATION_MILLIS);
        heightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        nCalendarController.setAnimationStatus(NCalendarController.EXPAND_COLLAPSE_CALENDAR);
        calendarView.getLayoutParams().height = 0;
        calendarView.requestLayout();
        calendarView.startAnimation(heightAnim);
    }

    void closeCalendar() {
        Animation heightAnim = getCollapsingAnimation(false);
        heightAnim.setDuration(HEIGHT_ANIM_DURATION_MILLIS);
        heightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        nCalendarController.setAnimationStatus(NCalendarController.EXPAND_COLLAPSE_CALENDAR);
        calendarView.getLayoutParams().height = calendarView.getHeight();
        calendarView.requestLayout();
        calendarView.startAnimation(heightAnim);
    }

    void openCalendarWithAnimation() {
        final Animator indicatorAnim = getIndicatorAnimator(1f, nCalendarController.getDayIndicatorRadius());
        final Animation heightAnim = getExposeCollapsingAnimation(true);
        calendarView.getLayoutParams().height = 0;
        calendarView.requestLayout();
        setUpAnimationLisForExposeOpen(indicatorAnim, heightAnim);
        calendarView.startAnimation(heightAnim);
    }

    private void setUpAnimationLisForExposeOpen(final Animator indicatorAnim, Animation heightAnim) {
        heightAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                nCalendarController.setAnimationStatus(NCalendarController.EXPOSE_CALENDAR_ANIMATION);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                indicatorAnim.start();
            }
        });
        indicatorAnim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                nCalendarController.setAnimationStatus(NCalendarController.ANIMATE_INDICATORS);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                nCalendarController.setAnimationStatus(NCalendarController.IDLE);
            }
        });
    }

    void closeCalendarWithAnimation() {
        final Animator indicatorAnim = getIndicatorAnimator(nCalendarController.getDayIndicatorRadius(), 1f);
        final Animation heightAnim = getExposeCollapsingAnimation(false);
        calendarView.getLayoutParams().height = calendarView.getHeight();
        calendarView.requestLayout();
        setUpAnimationLisForExposeClose(indicatorAnim, heightAnim);
        calendarView.startAnimation(heightAnim);
    }

    private void setUpAnimationLisForExposeClose(final Animator indicatorAnim, Animation heightAnim) {
        heightAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                nCalendarController.setAnimationStatus(NCalendarController.EXPOSE_CALENDAR_ANIMATION);
                indicatorAnim.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                nCalendarController.setAnimationStatus(NCalendarController.IDLE);
            }
        });
        indicatorAnim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                nCalendarController.setAnimationStatus(NCalendarController.ANIMATE_INDICATORS);
            }
        });
    }

    @NonNull
    private Animation getExposeCollapsingAnimation(final boolean isCollapsing) {
        Animation heightAnim = getCollapsingAnimation(isCollapsing);
        heightAnim.setDuration(HEIGHT_ANIM_DURATION_MILLIS);
        heightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        return heightAnim;
    }

    @NonNull
    private Animation getCollapsingAnimation(boolean isCollapsing) {
        return new CollapsingAnimation(calendarView, nCalendarController, nCalendarController.getTargetHeight(), getTargetGrowRadius(), isCollapsing);
    }

    @NonNull
    private Animator getIndicatorAnimator(float from, float to) {
        ValueAnimator animIndicator = ValueAnimator.ofFloat(from, to);
        animIndicator.setDuration(INDICATOR_ANIM_DURATION_MILLIS);
        animIndicator.setInterpolator(new OvershootInterpolator());
        animIndicator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                nCalendarController.setGrowFactorIndicator((Float) animation.getAnimatedValue());
                calendarView.invalidate();
            }
        });
        return animIndicator;
    }

    private int getTargetGrowRadius() {
        int heightSq = nCalendarController.getTargetHeight() * nCalendarController.getTargetHeight();
        int widthSq = nCalendarController.getWidth() * nCalendarController.getWidth();
        return (int) (0.5 * Math.sqrt(heightSq + widthSq));
    }
}
