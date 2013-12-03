package com.ellucian.mobile.android;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class Utils {

	private Utils() {}
	
	public static boolean isIntentAvailable(Context context, Intent intent) {
		if(intent == null) return false;
	    final PackageManager packageManager = context.getPackageManager();
	    List<ResolveInfo> resolveInfo =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	   if (resolveInfo.size() > 0) {
	   		return true;
	   	}
	   return false;
	}
	
	public static Calendar convertJsonDate(String dateStringFromJSON) {

		// Remove prefix and suffix extra string information
		final String dateString = dateStringFromJSON.replace("/Date(", "")
				.replace(")/", "");

		// Split date and timezone parts
		final String[] dateParts = dateString.split("[+-]");

		// The date must be in milliseconds since January 1, 1970 00:00:00 UTC
		// We want to be sure that it is a valid date and time, aka the use of
		// Calendar
		final Calendar calendar = Calendar.getInstance();
		if (dateParts.length > 0) {
			try {
				calendar.setTimeInMillis(Long.parseLong(dateParts[0]));
			} catch (final NumberFormatException e) {

			}
		}

		if (dateParts.length > 1) {
			// If you want to play with time zone:
			calendar.setTimeZone(TimeZone.getTimeZone(dateParts[1]));
		}

		return calendar;

	}



}
