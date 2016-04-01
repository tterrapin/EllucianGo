/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.util.Utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Custom view that draws a vertical time "ruler" representing the chronological
 * progression of a single day. Usually shown along with {@link BlockView}
 * instances to give a spatial sense of time.
 */
public class TimeRulerView extends View {

    public final int mHeaderWidth = 200;
    private static final int mHourHeight = 180;
    private final int mLabelTextSize = 35;
    private final int mLabelPaddingLeft = 20;
    private final int mHourTextColor = Utils.getColorHelper(getContext(), R.color.list_title_text_color);
    private final int mDividerLineColor = Utils.getColorHelper(getContext(), R.color.list_title_text_color);
    private final int mStartHour = 0;
    private final int mEndHour = 24;
    public static final int mHourPadding = 30;
    
    private static final TimeZone CONFERENCE_TIME_ZONE = Calendar.getInstance().getTimeZone();


    public TimeRulerView(Context context) {
        this(context, null);
    }

    public TimeRulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeRulerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Return the vertical offset (in pixels) for a requested time (in
     * milliseconds since epoch).
     */
    public int getTimeVerticalOffset(long timeMillis) {
        Calendar calendar = GregorianCalendar.getInstance(CONFERENCE_TIME_ZONE);
        calendar.setTimeInMillis(timeMillis);

        final int minutes = ((calendar.get(Calendar.HOUR_OF_DAY) - mStartHour) * 60) + calendar.get(Calendar.MINUTE);
        return (minutes * mHourHeight) / 60;
    }

    /**
     *
     * Round down to the previous hour, given a y-axis position.
     * The logic is reverse of getTimeVerticalOffset.
     */
    public static int getPreviousHourOffset(int yPosition) {
        int previousHour = (int) Math.floor((yPosition-mHourPadding)/mHourHeight);
        return previousHour * mHourHeight;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int hours = mEndHour - mStartHour;

        int width = mHeaderWidth;
        int height = (mHourHeight * hours) + (mHourPadding * 2);

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    private final Paint mHourDivider = new Paint();
    private final Paint mHalfHourDivider = new Paint();
    private final Paint mLabelPaint = new Paint();

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int hourHeight = mHourHeight;

        final Paint hourDivider = mHourDivider;
        hourDivider.setColor(mDividerLineColor);
        hourDivider.setStrokeWidth(2);
        hourDivider.setStyle(Paint.Style.FILL);

        final Paint halfHourDivider = mHalfHourDivider;
        halfHourDivider.setColor(mDividerLineColor);

        final Paint labelPaint = mLabelPaint;
        labelPaint.setColor(mHourTextColor);
        labelPaint.setTextSize(mLabelTextSize);
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        labelPaint.setAntiAlias(true);

        final FontMetricsInt metrics = labelPaint.getFontMetricsInt();
        final int labelHeight = Math.abs(metrics.ascent);
        final int labelOffset = labelHeight - mLabelPaddingLeft;

        final int right = getRight();

        // Walk left side of canvas drawing timestamps
        final int hours = mEndHour - mStartHour;
        DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        for (int i = 0; i < hours; i++) {
            final int dividerY = (hourHeight * i) + mHourPadding;
            canvas.drawLine(mHeaderWidth, dividerY, right, dividerY, hourDivider);
            canvas.drawLine(mHeaderWidth, dividerY+(hourHeight/2), right, dividerY+(hourHeight/2), halfHourDivider);
            
            final int hour = mStartHour + i;
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            String label;
            label = df.format(calendar.getTime());

            final float labelWidth = labelPaint.measureText(label);

            canvas.drawText(label, 0, label.length(), mHeaderWidth - labelWidth
                    - mLabelPaddingLeft, dividerY + labelOffset, labelPaint);
        }
    }

    public int getHeaderWidth() {
        return mHeaderWidth;
    }
}
