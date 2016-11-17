package com.nhancv.picker.dateview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;

import com.nhancv.picker.dateview.domain.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NCalendarView extends View {

    public static final int FILL_LARGE_INDICATOR = 1;
    public static final int NO_FILL_LARGE_INDICATOR = 2;
    public static final int SMALL_INDICATOR = 3;

    private final AnimationHandler animationHandler;
    private NCalendarController nCalendarController;
    private GestureDetectorCompat gestureDetector;
    private boolean shouldScroll = true;
    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            nCalendarController.onSingleTapConfirmed(e);
            invalidate();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (shouldScroll) {
                if (Math.abs(distanceX) > 0) {
                    getParent().requestDisallowInterceptTouchEvent(true);

                    nCalendarController.onScroll(e1, e2, distanceX, distanceY);
                    invalidate();
                    return true;
                }
            }

            return false;
        }
    };

    public NCalendarView(Context context) {
        this(context, null);
    }

    public NCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        nCalendarController = new NCalendarController(new Paint(), new OverScroller(getContext()),
                new Rect(), attrs, getContext(), Color.argb(255, 233, 84, 81),
                Color.argb(255, 64, 64, 64), Color.argb(255, 219, 219, 219), VelocityTracker.obtain(),
                Color.argb(255, 100, 68, 65), new EventsContainer(Calendar.getInstance()),
                Locale.getDefault(), TimeZone.getDefault());
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
        animationHandler = new AnimationHandler(nCalendarController, this);
    }

    /*
    Use a custom locale for compact calendar and reinitialise the view.
     */
    public void setLocale(TimeZone timeZone, Locale locale) {
        nCalendarController.setLocale(timeZone, locale);
        invalidate();
    }

    /*
    Compact calendar will use the locale to determine the abbreviation to use as the day column names.
    The default is to use the default locale and to abbreviate the day names to one character.
    Setting this to true will displace the short weekday string provided by java.
     */
    public void setUseThreeLetterAbbreviation(boolean useThreeLetterAbbreviation) {
        nCalendarController.setUseWeekDayAbbreviation(useThreeLetterAbbreviation);
        invalidate();
    }

    public void setCalendarBackgroundColor(final int calenderBackgroundColor) {
        nCalendarController.setCalenderBackgroundColor(calenderBackgroundColor);
        invalidate();
    }

    /*
    Sets the name for each day of the week. No attempt is made to adjust width or text size based on the length of each day name.
    Works best with 3-4 characters for each day.
     */
    public void setDayColumnNames(String[] dayColumnNames) {
        nCalendarController.setDayColumnNames(dayColumnNames);
    }

    public void setShouldShowMondayAsFirstDay(boolean shouldShowMondayAsFirstDay) {
        nCalendarController.setShouldShowMondayAsFirstDay(shouldShowMondayAsFirstDay);
        invalidate();
    }

    public void setCurrentSelectedDayBackgroundColor(int currentSelectedDayBackgroundColor) {
        nCalendarController.setCurrentSelectedDayBackgroundColor(currentSelectedDayBackgroundColor);
        invalidate();
    }

    public void setCurrentDayBackgroundColor(int currentDayBackgroundColor) {
        nCalendarController.setCurrentDayBackgroundColor(currentDayBackgroundColor);
        invalidate();
    }

    public int getHeightPerDay() {
        return nCalendarController.getHeightPerDay();
    }

    public void setListener(CompactCalendarViewListener listener) {
        nCalendarController.setListener(listener);
    }

    public Date getFirstDayOfCurrentMonth() {
        return nCalendarController.getFirstDayOfCurrentMonth();
    }

    public void shouldDrawIndicatorsBelowSelectedDays(boolean shouldDrawIndicatorsBelowSelectedDays) {
        nCalendarController.shouldDrawIndicatorsBelowSelectedDays(shouldDrawIndicatorsBelowSelectedDays);
    }

    public void setCurrentDate(Date dateTimeMonth) {
        nCalendarController.setCurrentDate(dateTimeMonth);
        invalidate();
    }

    public int getWeekNumberForCurrentMonth() {
        return nCalendarController.getWeekNumberForCurrentMonth();
    }

    public void setShouldDrawDaysHeader(boolean shouldDrawDaysHeader) {
        nCalendarController.setShouldDrawDaysHeader(shouldDrawDaysHeader);
    }

    /**
     * see {@link #addEvent(Event, boolean)} when adding single events
     * or {@link #addEvents(java.util.List)}  when adding multiple events
     *
     * @param event
     */
    @Deprecated
    public void addEvent(Event event) {
        addEvent(event, false);
    }

    /**
     * Adds an event to be drawn as an indicator in the calendar.
     * If adding multiple events see {@link #addEvents(List)}} method.
     *
     * @param event            to be added to the calendar
     * @param shouldInvalidate true if the view should invalidate
     */
    public void addEvent(Event event, boolean shouldInvalidate) {
        nCalendarController.addEvent(event);
        if (shouldInvalidate) {
            invalidate();
        }
    }

    /**
     * Adds multiple events to the calendar and invalidates the view once all events are added.
     */
    public void addEvents(List<Event> events) {
        nCalendarController.addEvents(events);
        invalidate();
    }

    /**
     * Fetches the events for the date passed in
     *
     * @param date
     * @return
     */
    public List<Event> getEvents(Date date) {
        return nCalendarController.getCalendarEventsFor(date.getTime());
    }

    /**
     * Fetches the events for the epochMillis passed in
     *
     * @param epochMillis
     * @return
     */
    public List<Event> getEvents(long epochMillis) {
        return nCalendarController.getCalendarEventsFor(epochMillis);
    }

    /**
     * Fetches the events for the month of the epochMillis passed in and returns a sorted list of events
     *
     * @param epochMillis
     * @return
     */
    public List<Event> getEventsForMonth(long epochMillis) {
        return nCalendarController.getCalendarEventsForMonth(epochMillis);
    }

    /**
     * Fetches the events for the month of the date passed in and returns a sorted list of events
     *
     * @param date
     * @return
     */
    public List<Event> getEventsForMonth(Date date) {
        return nCalendarController.getCalendarEventsForMonth(date.getTime());
    }

    /**
     * Remove the event associated with the Date passed in
     *
     * @param date
     */
    public void removeEvents(Date date) {
        nCalendarController.removeEventsFor(date.getTime());
    }

    public void removeEvents(long epochMillis) {
        nCalendarController.removeEventsFor(epochMillis);
    }

    /**
     * see {@link #removeEvent(Event, boolean)} when removing single events
     * or {@link #removeEvents(java.util.List)} (java.util.List)}  when removing multiple events
     *
     * @param event
     */
    @Deprecated
    public void removeEvent(Event event) {
        removeEvent(event, false);
    }

    /**
     * Removes an event from the calendar.
     * If removing multiple events see {@link #removeEvents(List)}
     *
     * @param event            event to remove from the calendar
     * @param shouldInvalidate true if the view should invalidate
     */
    public void removeEvent(Event event, boolean shouldInvalidate) {
        nCalendarController.removeEvent(event);
        if (shouldInvalidate) {
            invalidate();
        }
    }

    /**
     * Removes multiple events from the calendar and invalidates the view once all events are added.
     */
    public void removeEvents(List<Event> events) {
        nCalendarController.removeEvents(events);
        invalidate();
    }

    /**
     * Clears all Events from the calendar.
     */
    public void removeAllEvents() {
        nCalendarController.removeAllEvents();
        invalidate();
    }

    public void setCurrentSelectedDayIndicatorStyle(final int currentSelectedDayIndicatorStyle) {
        nCalendarController.setCurrentSelectedDayIndicatorStyle(currentSelectedDayIndicatorStyle);
        invalidate();
    }

    public void setCurrentDayIndicatorStyle(final int currentDayIndicatorStyle) {
        nCalendarController.setCurrentDayIndicatorStyle(currentDayIndicatorStyle);
        invalidate();
    }

    public void setEventIndicatorStyle(final int eventIndicatorStyle) {
        nCalendarController.setEventIndicatorStyle(eventIndicatorStyle);
        invalidate();
    }

    private void checkTargetHeight() {
        if (nCalendarController.getTargetHeight() <= 0) {
            throw new IllegalStateException("Target height must be set in xml properties in order to expand/collapse CompactCalendar.");
        }
    }

    public void setTargetHeight(int targetHeight) {
        nCalendarController.setTargetHeight(targetHeight);
        checkTargetHeight();
    }

    public void showCalendar() {
        checkTargetHeight();
        animationHandler.openCalendar();
    }

    public void hideCalendar() {
        checkTargetHeight();
        animationHandler.closeCalendar();
    }

    public void showCalendarWithAnimation() {
        checkTargetHeight();
        animationHandler.openCalendarWithAnimation();
    }

    public void hideCalendarWithAnimation() {
        checkTargetHeight();
        animationHandler.closeCalendarWithAnimation();
    }

    public void showNextMonth() {
        nCalendarController.showNextMonth();
        invalidate();
    }

    public void showPreviousMonth() {
        nCalendarController.showPreviousMonth();
        invalidate();
    }

    @Override
    protected void onMeasure(int parentWidth, int parentHeight) {
        super.onMeasure(parentWidth, parentHeight);
        int width = MeasureSpec.getSize(parentWidth);
        int height = MeasureSpec.getSize(parentHeight);
        if (width > 0 && height > 0) {
            nCalendarController.onMeasure(width, height, getPaddingRight(), getPaddingLeft());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        nCalendarController.onDraw(canvas);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (nCalendarController.computeScroll()) {
            invalidate();
        }
    }

    public void shouldScrollMonth(boolean shouldDisableScroll) {
        this.shouldScroll = shouldDisableScroll;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (shouldScroll) {
            nCalendarController.onTouch(event);
            invalidate();
        }

        // on touch action finished (CANCEL or UP), we re-allow the parent container to intercept touch events (scroll inside ViewPager + RecyclerView issue #82)
        if ((event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) && shouldScroll) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        // always allow gestureDetector to detect onSingleTap and scroll events
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        // Prevents ViewPager from scrolling horizontally by announcing that (issue #82)
        return true;
    }

    public interface CompactCalendarViewListener {
        void onDayClick(Date dateClicked);

        void onMonthScroll(Date firstDayOfNewMonth);
    }

}
