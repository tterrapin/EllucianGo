package com.ellucian.mobile.android.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.Utils;

public class EventsParser {

	public static List<EventCalendar> parse(String jString)
			throws JSONException {

		final List<EventCalendar> calendars = new ArrayList<EventCalendar>();
		final JSONObject jObject = new JSONObject(jString);

		final JSONArray feedsObj = jObject.getJSONArray("Calendars");
		for (int i = 0; i < feedsObj.length(); i++) {

			final EventCalendar calendar = new EventCalendar();
			calendars.add(calendar);
			calendar.setTitle(feedsObj.getJSONObject(i).getString("Title"));
			final JSONArray items = feedsObj.getJSONObject(i).getJSONArray(
					"Events");
			for (int j = 0; j < items.length(); j++) {
				final Event event = new Event();
				calendar.add(event);

				final JSONObject itemObj = items.getJSONObject(j);
				
				if (!itemObj.isNull("Title")) {
					event.setTitle(itemObj.getString("Title"));
				}
				if (!itemObj.isNull("StartDate")) {
					event.setStartDate(Utils
							.convertJsonDate(itemObj.getString("StartDate")));
				}
				if (!itemObj.isNull("EndDate")) {
					Calendar end = Utils
							.convertJsonDate(itemObj.getString("EndDate"));
					event.setEndDate(end);
				}
				if (!itemObj.isNull("Description")) {
					event.setDescription(itemObj.getString("Description"));
				}
				if (!itemObj.isNull("Location")) {
					event.setLocation(itemObj.getString("Location"));
				}
				if (!itemObj.isNull("IsAllDay")) {
					event.setAllDay(itemObj.getBoolean("IsAllDay"));
				}
				
				Log.d(EllucianApplication.TAG, "EndDate: " + event.getTitle() + " " + event.getStartDate().getTime().toGMTString() + " " + event.getEndDate().getTime().toGMTString());
			}

		}

		return calendars;
	}
}