package com.ellucian.mobile.android.view;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;
import android.widget.Button;

import com.ellucian.mobile.android.util.Utils;

/**
 * Custom view that represents a {@link Blocks#BLOCK_ID} instance, including its
 * title and time span that it occupies. Usually organized automatically by
 * {@link BlocksLayout} to match up against a {@link TimeRulerView} instance.
 */
public class BlockView extends Button {
    private static final int TIME_STRING_FLAGS = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY |
            DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_TIME;

    private String mBlockId;
    private String mTitle;
    private long mStartTime;
    private long mEndTime;
    private int mColumn;

    public static final TimeZone CONFERENCE_TIME_ZONE = Calendar.getInstance().getTimeZone();

    public BlockView(Context context) {
    	super(context);
    }

    public BlockView(Context context, String blockId, String title, long startTime,
            long endTime, int column) {
        super(context);

        mBlockId = blockId;
        mTitle = title;
        mStartTime = startTime;
        mEndTime = endTime;
        mColumn = column;
        
        setText(mTitle);
        setTextAppearance(context, android.R.style.TextAppearance_Small);
        setTextColor(Utils.getSubheaderTextColor(context));
    }

    public String getBlockId() {
        return mBlockId;
    }

    public String getBlockTimeString() {
        TimeZone.setDefault(CONFERENCE_TIME_ZONE);
        return DateUtils.formatDateTime(getContext(), mStartTime, TIME_STRING_FLAGS);
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public int getColumn() {
        return mColumn;
    }
}
