package com.ellucian.mobile.android.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class CalendarUtils {
	// indices follow the Calendar constants... example Calendar.SUNDAY
	
	private static final DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
	private static SimpleDateFormat UTCFormat;// = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static {
		UTCFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		UTCFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public static String getDayShortName(int dayId) {
		String[] dayNames = symbols.getShortWeekdays();
		return dayNames[dayId];
	}
	
	public static String getDayName(int dayId) {
		String[] dayNames = symbols.getWeekdays();
		return dayNames[dayId];
	}
	
	public static String getDefaultDateTimeString(Context context, Date date) {
		String output;
		// get the short form of the date
		DateFormat dateFormatter = android.text.format.DateFormat.getDateFormat(context);
		output = dateFormatter.format(date);
		// now append on the time portion
		DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
		output += " " + timeFormatter.format(date); 
		return output;
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
	
	public static SimpleDateFormat getUTCFormat() {
		return UTCFormat;
	}
	
	public static Date parseFromUTC(String dateString) {
		Date date = null;
		if (!TextUtils.isEmpty(dateString)) {
			try {
				date = UTCFormat.parse(dateString);
			} catch (ParseException e) {
				Log.e("CalendarUtils.parseFromUTC", "parsing failed", e);
			}
		}
		return date;
	}
	
	public static String formatToUTC(Date date) {
		String dateString = null;
		if (date != null) {
			dateString = UTCFormat.format(date);
		}
		return dateString;
		
	}
}
