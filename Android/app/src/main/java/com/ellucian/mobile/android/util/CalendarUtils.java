/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.ellucian.elluciango.R;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarUtils {
    // indices follow the Calendar constants... example Calendar.SUNDAY

    private static final SimpleDateFormat UtcFormatPrototype;// = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        UtcFormatPrototype = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        UtcFormatPrototype.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String getDayShortName(int dayId) {
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        String[] dayNames = symbols.getShortWeekdays();
        return dayNames[dayId];
    }

    public static String getDayName(int dayId) {
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        String[] dayNames = symbols.getWeekdays();
        return dayNames[dayId];
    }

    public static String getDefaultDateTimeString(Context context, Date date) {
        String output;
        // get the short form of the date
        DateFormat dateFormatter = android.text.format.DateFormat.getDateFormat(context);
        DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        output = context.getString(R.string.date_time_format,
                dateFormatter.format(date),
                timeFormatter.format(date));
        return output;
    }

    public static String getMonthDateString(Context context, Date date) {
        String output;
//        DateFormat dateFormatter = android.text.format.DateFormat.getMediumDateFormat(context);
//        DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
//        output = context.getString(R.string.date_time_format,
//                dateFormatter.format(date),
//                timeFormatter.format(date));
//        return output;
        return DateUtils.getRelativeDateTimeString(context, date.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();

    }

    public static String getDefaultDateString(Context context, Date date) {
        String output;
        DateFormat dateFormatter = android.text.format.DateFormat.getDateFormat(context);
        output = dateFormatter.format(date);
        return output;
    }

    public static String getDefaultTimeString(Context context, Date date) {
        String output;
        DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        output = timeFormatter.format(date);
        return output;
    }

    public SimpleDateFormat getUTCFormat() {
        return (SimpleDateFormat)UtcFormatPrototype.clone();
    }

    public static Date parseFromUTC(String dateString) {
        Date date = null;
        if (!TextUtils.isEmpty(dateString)) {
            try {
                date = ((SimpleDateFormat)UtcFormatPrototype.clone()).parse(dateString);
            } catch (ParseException e) {
                Log.e("CalendarUtils.parseFromUTC", "parsing failed", e);
            }
        }
        return date;
    }

    public static String formatToUTC(Date date) {
        String dateString = null;
        if (date != null) {
            dateString = ((SimpleDateFormat)UtcFormatPrototype.clone()).format(date);
        }
        return dateString;
    }

    public static Long getNextHour() {
        Calendar rightNow = (Calendar)Calendar.getInstance().clone();
        rightNow.add(Calendar.HOUR, 1);
        rightNow.set(Calendar.MINUTE, 0);
        return rightNow.getTime().getTime();
    }
}
