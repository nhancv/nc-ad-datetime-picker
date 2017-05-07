package com.nhancv.picker.timeview;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import com.nhancv.picker.R;
import com.nhancv.picker.dateview.AnimatorListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NWheelPicker extends View implements IDebug, IWheelPicker, Runnable {
    public static final int SCROLL_STATE_IDLE = 0, SCROLL_STATE_DRAGGING = 1, SCROLL_STATE_SCROLLING = 2;
    public static final int ALIGN_CENTER = 0, ALIGN_LEFT = 1, ALIGN_RIGHT = 2;

    private static final String TAG = NWheelPicker.class.getSimpleName();

    private final Handler handler = new Handler();

    private Paint paint;
    private Typeface currentTypeFace, defaultTypeFace;
    private Scroller scroller;
    private VelocityTracker tracker;

    private OnItemSelectedListener onItemSelectedListener;
    private OnWheelChangeListener onWheelChangeListener;

    private Rect rectDrawn;
    private Rect rectIndicatorHead, rectIndicatorFoot;
    private Rect rectCurrentItem;

    private Camera camera;
    private Matrix matrixRotate, matrixDepth;

    private List data;

    private String maxWidthText;

    private int visibleItemCount, drawnItemCount;

    private int halfDrawnItemCount;

    private int textMaxWidth, textMaxHeight;

    private int itemTextColor, selectedItemTextColor;

    private int itemTextSize;

    private int indicatorSize;

    private int indicatorColor;

    private int curtainColor;

    private int itemSpace;

    private int itemAlign;

    private int itemHeight, halfItemHeight;

    private int halfWheelHeight;

    private int selectedItemPosition;

    private int currentItemPosition;

    private int minFlingY, maxFlingY;

    private int minimumVelocity = 50, maximumVelocity = 8000;

    private int wheelCenterX, wheelCenterY;

    private int drawnCenterX, drawnCenterY;

    private int scrollOffsetY;

    private int textMaxWidthPosition;

    private int lastPointY;

    private int downPointY;

    private int touchSlop = 8;

    private boolean hasSameWidth;

    private boolean hasIndicator;

    private boolean hasCurtain;

    private boolean hasAtmospheric;

    private boolean isCyclic;

    private boolean isCurved;

    private boolean isClick;

    private boolean isForceFinishScroll;

    private int curtainPadding;

    private boolean isDebug;

    private List<Region> listRegion;


    public NWheelPicker(Context context) {
        this(context, null);
    }

    public NWheelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NWheelPicker);
        int idData = a.getResourceId(R.styleable.NWheelPicker_wheel_data, 0);
        data = Arrays.asList(getResources()
                                      .getStringArray(idData == 0 ? R.array.WheelArrayDefault : idData));
        itemTextSize = a.getDimensionPixelSize(R.styleable.NWheelPicker_wheel_item_text_size,
                                               getResources().getDimensionPixelSize(R.dimen.WheelItemTextSize));
        visibleItemCount = a.getInt(R.styleable.NWheelPicker_wheel_visible_item_count, 7);
        selectedItemPosition = a.getInt(R.styleable.NWheelPicker_wheel_selected_item_position, 0);
        hasSameWidth = a.getBoolean(R.styleable.NWheelPicker_wheel_same_width, false);
        textMaxWidthPosition =
                a.getInt(R.styleable.NWheelPicker_wheel_maximum_width_text_position, -1);
        maxWidthText = a.getString(R.styleable.NWheelPicker_wheel_maximum_width_text);
        selectedItemTextColor = a.getColor
                (R.styleable.NWheelPicker_wheel_selected_item_text_color, -1);
        itemTextColor = a.getColor(R.styleable.NWheelPicker_wheel_item_text_color, 0xFF888888);
        itemSpace = a.getDimensionPixelSize(R.styleable.NWheelPicker_wheel_item_space,
                                            getResources().getDimensionPixelSize(R.dimen.WheelItemSpace));
        isCyclic = a.getBoolean(R.styleable.NWheelPicker_wheel_cyclic, false);
        hasIndicator = a.getBoolean(R.styleable.NWheelPicker_wheel_indicator, false);
        indicatorColor = a.getColor(R.styleable.NWheelPicker_wheel_indicator_color, 0xFFEE3333);
        indicatorSize = a.getDimensionPixelSize(R.styleable.NWheelPicker_wheel_indicator_size,
                                                getResources().getDimensionPixelSize(R.dimen.WheelIndicatorSize));
        hasCurtain = a.getBoolean(R.styleable.NWheelPicker_wheel_curtain, false);
        curtainColor = a.getColor(R.styleable.NWheelPicker_wheel_curtain_color, 0x88FFFFFF);
        hasAtmospheric = a.getBoolean(R.styleable.NWheelPicker_wheel_atmospheric, false);
        isCurved = a.getBoolean(R.styleable.NWheelPicker_wheel_curved, false);
        itemAlign = a.getInt(R.styleable.NWheelPicker_wheel_item_align, ALIGN_CENTER);
        curtainPadding = a.getDimensionPixelSize(R.styleable.NWheelPicker_wheel_curtain_padding, 0);
        a.recycle();

        // Update relevant parameters when the count of visible item changed
        updateVisibleItemCount();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setTextSize(itemTextSize);
        currentTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Bold.ttf");
        defaultTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Medium.ttf");
        paint.setTypeface(defaultTypeFace);

        // Update alignment of text
        updateItemTextAlign();

        // Correct sizes of text
        computeTextSize();

        scroller = new Scroller(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            ViewConfiguration conf = ViewConfiguration.get(getContext());
            minimumVelocity = conf.getScaledMinimumFlingVelocity();
            maximumVelocity = conf.getScaledMaximumFlingVelocity();
            touchSlop = conf.getScaledTouchSlop();
        }
        rectDrawn = new Rect();

        rectIndicatorHead = new Rect();
        rectIndicatorFoot = new Rect();

        rectCurrentItem = new Rect();

        camera = new Camera();

        matrixRotate = new Matrix();
        matrixDepth = new Matrix();
    }

    private void updateVisibleItemCount() {
        if (visibleItemCount < 2)
            throw new ArithmeticException("Wheel's visible item count can not be less than 2!");

        // Be sure count of visible item is odd number
        if (visibleItemCount % 2 == 0)
            visibleItemCount += 1;
        drawnItemCount = visibleItemCount + 2;
        halfDrawnItemCount = drawnItemCount / 2;
    }

    private void computeTextSize() {
        textMaxWidth = textMaxHeight = 0;
        if (hasSameWidth) {
            textMaxWidth = (int) paint.measureText(String.valueOf(data.get(0)));
        } else if (isPosInRang(textMaxWidthPosition)) {
            textMaxWidth = (int) paint.measureText
                    (String.valueOf(data.get(textMaxWidthPosition)));
        } else if (!TextUtils.isEmpty(maxWidthText)) {
            textMaxWidth = (int) paint.measureText(maxWidthText);
        } else {
            for (Object obj : data) {
                String text = String.valueOf(obj);
                int width = (int) paint.measureText(text);
                textMaxWidth = Math.max(textMaxWidth, width);
            }
        }
        Paint.FontMetrics metrics = paint.getFontMetrics();
        textMaxHeight = (int) (metrics.bottom - metrics.top);
    }

    private void updateItemTextAlign() {
        switch (itemAlign) {
            case ALIGN_LEFT:
                paint.setTextAlign(Paint.Align.LEFT);
                break;
            case ALIGN_RIGHT:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
            default:
                paint.setTextAlign(Paint.Align.CENTER);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Correct sizes of original content
        int resultWidth = textMaxWidth;
        int resultHeight = textMaxHeight * visibleItemCount + itemSpace * (visibleItemCount - 1);

        // Correct view sizes again if curved is enable
        if (isCurved) {
            resultHeight = (int) (2 * resultHeight / Math.PI);
        }
        if (isDebug)
            Log.i(TAG, "Wheel's content size is (" + resultWidth + ":" + resultHeight + ")");

        // Consideration padding influence the view sizes
        resultWidth += getPaddingLeft() + getPaddingRight();
        resultHeight += getPaddingTop() + getPaddingBottom();
        if (isDebug)
            Log.i(TAG, "Wheel's size is (" + resultWidth + ":" + resultHeight + ")");

        // Consideration sizes of parent can influence the view sizes
        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth);
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight);

        setMeasuredDimension(resultWidth, resultHeight);
    }

    private int measureSize(int mode, int sizeExpect, int sizeActual) {
        int realSize;
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect;
        } else {
            realSize = sizeActual;
            if (mode == MeasureSpec.AT_MOST)
                realSize = Math.min(realSize, sizeExpect);
        }
        return realSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // Set content region
        rectDrawn.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                       getHeight() - getPaddingBottom());

        if (isDebug)
            Log.i(TAG, "Wheel's drawn rect size is (" + rectDrawn.width() + ":" +
                       rectDrawn.height() + ") and location is (" + rectDrawn.left + ":" +
                       rectDrawn.top + ")");

        // Get the center coordinates of content region
        wheelCenterX = rectDrawn.centerX();
        wheelCenterY = rectDrawn.centerY();

        // Correct item drawn center
        computeDrawnCenter();

        halfWheelHeight = rectDrawn.height() / 2;

        itemHeight = rectDrawn.height() / visibleItemCount;
        halfItemHeight = itemHeight / 2 + curtainPadding;

        // Initialize fling max Y-coordinates
        computeFlingLimitY();

        // Correct region of indicator
        computeIndicatorRect();

        // Correct region of current select item
        computeCurrentItemRect();

        // Correct region for check touch
        computeListRegion();

    }

    private void computeListRegion() {
        try {
            listRegion = new ArrayList<>();
            //top
            for (int i = -drawnItemCount / 2; i < -1; i++) {
                int top = wheelCenterY + (i * itemHeight);
                int bottom = wheelCenterY + ((i + 1) * itemHeight);
                Region region = new Region(0, top, rectDrawn.width(), bottom);
                listRegion.add(region);
            }

            //mid
            listRegion.add(new Region(0, wheelCenterY + (-1 * itemHeight), rectDrawn.width(),
                                      wheelCenterY + itemHeight));

            //bottom
            for (int i = 1; i < drawnItemCount / 2; i++) {
                int top = wheelCenterY + (i * itemHeight);
                int bottom = wheelCenterY + ((i + 1) * itemHeight);
                Region region = new Region(0, top, rectDrawn.width(), bottom);
                listRegion.add(region);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void computeDrawnCenter() {
        switch (itemAlign) {
            case ALIGN_LEFT:
                drawnCenterX = rectDrawn.left;
                break;
            case ALIGN_RIGHT:
                drawnCenterX = rectDrawn.right;
                break;
            default:
                drawnCenterX = wheelCenterX;
                break;
        }
        drawnCenterY = (int) (wheelCenterY - ((paint.ascent() + paint.descent()) / 2));
    }

    private void computeFlingLimitY() {
        int currentItemOffset = selectedItemPosition * itemHeight;
        minFlingY = isCyclic ? Integer.MIN_VALUE :
                    -itemHeight * (data.size() - 1) + currentItemOffset;
        maxFlingY = isCyclic ? Integer.MAX_VALUE : currentItemOffset;
    }

    private void computeIndicatorRect() {
        if (!hasIndicator) return;
        int halfIndicatorSize = indicatorSize / 2;
        int indicatorHeadCenterY = wheelCenterY + halfItemHeight;
        int indicatorFootCenterY = wheelCenterY - halfItemHeight;
        rectIndicatorHead.set(rectDrawn.left, indicatorHeadCenterY - halfIndicatorSize,
                              rectDrawn.right, indicatorHeadCenterY + halfIndicatorSize);
        rectIndicatorFoot.set(rectDrawn.left, indicatorFootCenterY - halfIndicatorSize,
                              rectDrawn.right, indicatorFootCenterY + halfIndicatorSize);
    }

    private void computeCurrentItemRect() {
        if (!hasCurtain && selectedItemTextColor == -1) return;
        rectCurrentItem.set(rectDrawn.left, wheelCenterY - halfItemHeight, rectDrawn.right,
                            wheelCenterY + halfItemHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != onWheelChangeListener)
            onWheelChangeListener.onWheelScrolled(scrollOffsetY);

        // Need to draw curtain or not
        if (hasCurtain) {
            canvas.save();
            paint.setColor(curtainColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rectCurrentItem, paint);
            canvas.restore();
        }

        // Need to draw indicator or not
        if (hasIndicator) {
            paint.setColor(indicatorColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rectIndicatorHead, paint);
            canvas.drawRect(rectIndicatorFoot, paint);
        }
        if (isDebug) {
            paint.setColor(0x4433EE33);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, getPaddingLeft(), getHeight(), paint);
            canvas.drawRect(0, 0, getWidth(), getPaddingTop(), paint);
            canvas.drawRect(getWidth() - getPaddingRight(), 0, getWidth(), getHeight(), paint);
            canvas.drawRect(0, getHeight() - getPaddingBottom(), getWidth(), getHeight(), paint);
        }

        int drawnDataStartPos = -scrollOffsetY / itemHeight - halfDrawnItemCount;
        for (int drawnDataPos = drawnDataStartPos + selectedItemPosition,
             drawnOffsetPos = -halfDrawnItemCount;
             drawnDataPos < drawnDataStartPos + selectedItemPosition + drawnItemCount;
             drawnDataPos++, drawnOffsetPos++) {
            String data = "";
            if (isCyclic) {
                int actualPos = drawnDataPos % this.data.size();
                actualPos = actualPos < 0 ? (actualPos + this.data.size()) : actualPos;
                data = String.valueOf(this.data.get(actualPos));
            } else {
                if (isPosInRang(drawnDataPos))
                    data = String.valueOf(this.data.get(drawnDataPos));
            }
            paint.setColor(itemTextColor);
            paint.setStyle(Paint.Style.FILL);
            int mDrawnItemCenterY = drawnCenterY + (drawnOffsetPos * itemHeight) +
                                    scrollOffsetY % itemHeight;

            int distanceToCenter = 0;
            if (isCurved) {
                // Correct ratio of item's drawn center to wheel center
                float ratio = (drawnCenterY - Math.abs(drawnCenterY - mDrawnItemCenterY) -
                               rectDrawn.top) * 1.0F / (drawnCenterY - rectDrawn.top);

                // Correct unit
                int unit = 0;
                if (mDrawnItemCenterY > drawnCenterY)
                    unit = 1;
                else if (mDrawnItemCenterY < drawnCenterY)
                    unit = -1;

                float degree = (-(1 - ratio) * 90 * unit);
                if (degree < -90) degree = -90;
                if (degree > 90) degree = 90;
                distanceToCenter = computeSpace((int) degree);

                int transX = wheelCenterX;
                switch (itemAlign) {
                    case ALIGN_LEFT:
                        transX = rectDrawn.left;
                        break;
                    case ALIGN_RIGHT:
                        transX = rectDrawn.right;
                        break;
                }
                int transY = wheelCenterY - distanceToCenter;

                camera.save();
                camera.rotateX(degree);
                camera.getMatrix(matrixRotate);
                camera.restore();
                matrixRotate.preTranslate(-transX, -transY);
                matrixRotate.postTranslate(transX, transY);

                camera.save();
                camera.translate(0, 0, computeDepth((int) degree));
                camera.getMatrix(matrixDepth);
                camera.restore();
                matrixDepth.preTranslate(-transX, -transY);
                matrixDepth.postTranslate(transX, transY);

                matrixRotate.postConcat(matrixDepth);
            }
            if (hasAtmospheric) {
                int alpha = (int) ((drawnCenterY - Math.abs(drawnCenterY - mDrawnItemCenterY)) *
                                   1.0F / drawnCenterY * 255);
                alpha = alpha < 0 ? 0 : alpha;
                paint.setAlpha(alpha);
            }
            // Correct item's drawn centerY base on curved state
            int drawnCenterY = isCurved ? this.drawnCenterY - distanceToCenter : mDrawnItemCenterY;

            // Judges need to draw different color for current item or not
            if (selectedItemTextColor != -1) {
                canvas.save();
                if (isCurved) canvas.concat(matrixRotate);
                canvas.clipRect(rectCurrentItem, Region.Op.DIFFERENCE);
                canvas.drawText(data, drawnCenterX, drawnCenterY, paint);
                canvas.restore();

                paint.setColor(selectedItemTextColor);
                paint.setTypeface(currentTypeFace);
                paint.setTextSize(itemTextSize + 10);
                canvas.save();
                if (isCurved) canvas.concat(matrixRotate);
                canvas.clipRect(rectCurrentItem);
                canvas.drawText(data, drawnCenterX, drawnCenterY, paint);
                canvas.restore();
                paint.setColor(itemTextColor);
                paint.setTypeface(defaultTypeFace);
                paint.setTextSize(itemTextSize);


            } else {
                canvas.save();
                canvas.clipRect(rectDrawn);
                if (isCurved) canvas.concat(matrixRotate);
                canvas.drawText(data, drawnCenterX, drawnCenterY, paint);
                canvas.restore();
            }
            if (isDebug) {
                canvas.save();
                canvas.clipRect(rectDrawn);
                paint.setColor(0xFFEE3333);
                int lineCenterY = wheelCenterY + (drawnOffsetPos * itemHeight);
                canvas.drawLine(rectDrawn.left, lineCenterY, rectDrawn.right, lineCenterY,
                                paint);
                canvas.restore();
            }
        }

    }

    private boolean isPosInRang(int position) {
        return position >= 0 && position < data.size();
    }

    private int computeSpace(int degree) {
        return (int) (Math.sin(Math.toRadians(degree)) * halfWheelHeight);
    }

    private int computeDepth(int degree) {
        return (int) (halfWheelHeight - Math.cos(Math.toRadians(degree)) * halfWheelHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (null != getParent())
                    getParent().requestDisallowInterceptTouchEvent(true);
                if (null == tracker)
                    tracker = VelocityTracker.obtain();
                else
                    tracker.clear();
                tracker.addMovement(event);
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                    isForceFinishScroll = true;
                }
                downPointY = lastPointY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                //Stop when click outside
                if (event.getY() < getY() || event.getY() > (getY() + getHeight())) {
                    break;
                }

                if (Math.abs(downPointY - event.getY()) < touchSlop) {
                    isClick = true;
                    break;
                }
                isClick = false;
                tracker.addMovement(event);
                if (null != onWheelChangeListener)
                    onWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);

                // Scroll WheelPicker's content
                float move = event.getY() - lastPointY;
                if (Math.abs(move) < 1) break;
                scrollOffsetY += move;
                lastPointY = (int) event.getY();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                if (null != getParent())
                    getParent().requestDisallowInterceptTouchEvent(false);
                if (isClick) {
                    int indexSelected = -1;
                    for (int i = 0; i < listRegion.size(); i++) {
                        Region region = listRegion.get(i);
                        if (region.contains(0, downPointY)) {
                            indexSelected = i;
                            break;
                        }
                    }
                    int mid = visibleItemCount / 2;
                    int desPosition = currentItemPosition - (mid - indexSelected);
                    if (indexSelected != -1 && indexSelected != mid && desPosition >= 0 && desPosition < data.size()) {
                        smoothScrollToPosition(desPosition);
                    }

                    break;
                }
                tracker.addMovement(event);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT)
                    tracker.computeCurrentVelocity(1000, maximumVelocity);
                else
                    tracker.computeCurrentVelocity(1000);

                // Judges the WheelPicker is scroll or fling base on current velocity
                isForceFinishScroll = false;
                int velocity = (int) tracker.getYVelocity();
                if (Math.abs(velocity) > minimumVelocity) {
                    scroller.fling(0, scrollOffsetY, 0, velocity, 0, 0, minFlingY, maxFlingY);
                    scroller.setFinalY(scroller.getFinalY() +
                                       computeDistanceToEndPoint(scroller.getFinalY() % itemHeight));
                } else {
                    scroller.startScroll(0, scrollOffsetY, 0,
                                         computeDistanceToEndPoint(scrollOffsetY % itemHeight));
                }
                // Correct coordinates
                if (!isCyclic)
                    if (scroller.getFinalY() > maxFlingY)
                        scroller.setFinalY(maxFlingY);
                    else if (scroller.getFinalY() < minFlingY)
                        scroller.setFinalY(minFlingY);
                handler.post(this);
                if (null != tracker) {
                    tracker.recycle();
                    tracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (null != getParent())
                    getParent().requestDisallowInterceptTouchEvent(false);
                if (null != tracker) {
                    tracker.recycle();
                    tracker = null;
                }
                break;
        }
        return true;
    }

    private int computeDistanceToEndPoint(int remainder) {
        if (Math.abs(remainder) > halfItemHeight)
            if (scrollOffsetY < 0)
                return -itemHeight - remainder;
            else
                return itemHeight - remainder;
        else
            return -remainder;
    }

    public void smoothScrollToPosition(int position) {
        try {
            if (!isCyclic) {
                position %= data.size();
                position = position < 0 ? position + data.size() : position;

                int toPos = -(position - selectedItemPosition);
                int offset = Math.abs(position - currentItemPosition);
                int fromPos = (position > currentItemPosition) ? (toPos + offset) :
                              (toPos - offset);
                int from = fromPos * itemHeight;
                int target = toPos * itemHeight;

                ValueAnimator vAnimator = new ValueAnimator();
                vAnimator.setIntValues(from, target);
                vAnimator.setDuration(500);
                vAnimator.setEvaluator(new IntEvaluator());
                vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        scrollOffsetY = (int) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                vAnimator.addListener(new AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Correct coordinates
                        if (!isCyclic) {
                            if (scroller.getFinalY() > maxFlingY)
                                scroller.setFinalY(maxFlingY);
                            else if (scroller.getFinalY() < minFlingY)
                                scroller.setFinalY(minFlingY);
                            handler.post(NWheelPicker.this);
                        }
                    }
                });
                vAnimator.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scrollToPosition(int position) {
        try {
            if (!isCyclic) {
                position %= data.size();
                position = position < 0 ? position + data.size() : position;

                scrollOffsetY = -(position - selectedItemPosition) * itemHeight;
                scroller.startScroll(0, scrollOffsetY, 0,
                                     computeDistanceToEndPoint(scrollOffsetY % itemHeight));
                if (scroller.getFinalY() > maxFlingY)
                    scroller.setFinalY(maxFlingY);
                else if (scroller.getFinalY() < minFlingY)
                    scroller.setFinalY(minFlingY);
                handler.post(NWheelPicker.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (null == data || data.size() == 0) return;
        if (scroller.isFinished() && !isForceFinishScroll) {
            if (itemHeight == 0) return;
            int position = (-scrollOffsetY / itemHeight + selectedItemPosition) % data.size();
            position = position < 0 ? position + data.size() : position;
            if (isDebug)
                Log.i(TAG, position + ":" + data.get(position) + ":" + scrollOffsetY);
            currentItemPosition = position;
            if (null != onItemSelectedListener)
                onItemSelectedListener.onItemSelected(this, data.get(position), position);
            if (null != onWheelChangeListener) {
                onWheelChangeListener.onWheelSelected(position);
                onWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            }
        }
        if (scroller.computeScrollOffset()) {
            if (null != onWheelChangeListener)
                onWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
            scrollOffsetY = scroller.getCurrY();
            postInvalidate();
            handler.postDelayed(this, 16);
        }
    }

    @Override
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Override
    public int getVisibleItemCount() {
        return visibleItemCount;
    }

    @Override
    public void setVisibleItemCount(int count) {
        visibleItemCount = count;
        updateVisibleItemCount();
        requestLayout();
    }

    @Override
    public boolean isCyclic() {
        return isCyclic;
    }

    @Override
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
        computeFlingLimitY();
        invalidate();
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        onItemSelectedListener = listener;
    }

    @Override
    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    @Override
    public void setSelectedItemPosition(int position) {
        position = Math.min(position, data.size() - 1);
        position = Math.max(position, 0);
        selectedItemPosition = position;
        currentItemPosition = position;
        scrollOffsetY = 0;
        computeFlingLimitY();
        requestLayout();
        invalidate();
    }

    @Override
    public int getCurrentItemPosition() {
        return currentItemPosition;
    }

    @Override
    public List getData() {
        return data;
    }

    @Override
    public void setData(List data) {
        if (null == data)
            throw new NullPointerException("WheelPicker's data can not be null!");
        this.data = data;

        if (selectedItemPosition > data.size() - 1 || currentItemPosition > data.size() - 1) {
            selectedItemPosition = currentItemPosition = data.size() - 1;
        } else {
            selectedItemPosition = currentItemPosition;
        }
        scrollOffsetY = 0;
        computeTextSize();
        computeFlingLimitY();
        requestLayout();
        invalidate();
    }

    public void setSameWidth(boolean hasSameWidth) {
        this.hasSameWidth = hasSameWidth;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public boolean hasSameWidth() {
        return hasSameWidth;
    }

    @Override
    public void setOnWheelChangeListener(OnWheelChangeListener listener) {
        onWheelChangeListener = listener;
    }

    @Override
    public String getMaximumWidthText() {
        return maxWidthText;
    }

    @Override
    public void setMaximumWidthText(String text) {
        if (null == text)
            throw new NullPointerException("Maximum width text can not be null!");
        maxWidthText = text;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public int getMaximumWidthTextPosition() {
        return textMaxWidthPosition;
    }

    @Override
    public void setMaximumWidthTextPosition(int position) {
        if (!isPosInRang(position))
            throw new ArrayIndexOutOfBoundsException("Maximum width text Position must in [0, " +
                                                     data.size() + "), but current is " + position);
        textMaxWidthPosition = position;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public int getSelectedItemTextColor() {
        return selectedItemTextColor;
    }

    @Override
    public void setSelectedItemTextColor(int color) {
        selectedItemTextColor = color;
        computeCurrentItemRect();
        invalidate();
    }

    @Override
    public int getItemTextColor() {
        return itemTextColor;
    }

    @Override
    public void setItemTextColor(int color) {
        itemTextColor = color;
        invalidate();
    }

    @Override
    public int getItemTextSize() {
        return itemTextSize;
    }

    @Override
    public void setItemTextSize(int size) {
        itemTextSize = size;
        paint.setTextSize(itemTextSize);
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public int getItemSpace() {
        return itemSpace;
    }

    @Override
    public void setItemSpace(int space) {
        itemSpace = space;
        requestLayout();
        invalidate();
    }

    @Override
    public void setIndicator(boolean hasIndicator) {
        this.hasIndicator = hasIndicator;
        computeIndicatorRect();
        invalidate();
    }

    @Override
    public boolean hasIndicator() {
        return hasIndicator;
    }

    @Override
    public int getIndicatorSize() {
        return indicatorSize;
    }

    @Override
    public void setIndicatorSize(int size) {
        indicatorSize = size;
        computeIndicatorRect();
        invalidate();
    }

    @Override
    public int getIndicatorColor() {
        return indicatorColor;
    }

    @Override
    public void setIndicatorColor(int color) {
        indicatorColor = color;
        invalidate();
    }

    @Override
    public void setCurtain(boolean hasCurtain) {
        this.hasCurtain = hasCurtain;
        computeCurrentItemRect();
        invalidate();
    }

    @Override
    public boolean hasCurtain() {
        return hasCurtain;
    }

    @Override
    public int getCurtainColor() {
        return curtainColor;
    }

    @Override
    public void setCurtainColor(int color) {
        curtainColor = color;
        invalidate();
    }

    @Override
    public void setAtmospheric(boolean hasAtmospheric) {
        this.hasAtmospheric = hasAtmospheric;
        invalidate();
    }

    @Override
    public boolean hasAtmospheric() {
        return hasAtmospheric;
    }

    @Override
    public boolean isCurved() {
        return isCurved;
    }

    @Override
    public void setCurved(boolean isCurved) {
        this.isCurved = isCurved;
        requestLayout();
        invalidate();
    }

    @Override
    public int getItemAlign() {
        return itemAlign;
    }

    @Override
    public void setItemAlign(int align) {
        itemAlign = align;
        updateItemTextAlign();
        computeDrawnCenter();
        invalidate();
    }

    @Override
    public Typeface getTypeface() {
        if (null != paint)
            return paint.getTypeface();
        return null;
    }

    @Override
    public void setTypeface(Typeface tf) {
        if (null != paint)
            paint.setTypeface(tf);
        computeTextSize();
        requestLayout();
        invalidate();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(NWheelPicker picker, Object data, int position);
    }

    public interface OnWheelChangeListener {
        void onWheelScrolled(int offset);

        void onWheelSelected(int position);

        void onWheelScrollStateChanged(int state);
    }
}