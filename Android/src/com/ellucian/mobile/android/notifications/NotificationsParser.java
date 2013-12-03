package com.ellucian.mobile.android.notifications;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ellucian.mobile.android.Utils;

public class NotificationsParser {

	public static List<ColleagueNotification> parse(String jString) throws JSONException {

		final List<ColleagueNotification> notifications = new ArrayList<ColleagueNotification>();
		final JSONObject jObject = new JSONObject(jString);

		final JSONArray jNotifications = jObject
				.getJSONArray("NotificationList");
		for (int i = 0; i < jNotifications.length(); i++) {

			final ColleagueNotification notification = new ColleagueNotification();
			notifications.add(notification);

			final JSONObject itemObj = jNotifications.getJSONObject(i);

			if (!itemObj.isNull("Description")) {
				notification.setDescription(itemObj.getString("Description"));
			}

			if (!itemObj.isNull("DescriptionDetails")) {
				notification.setDescriptionDetails(itemObj
						.getString("DescriptionDetails"));
			}
			if (!itemObj.isNull("Hyperlink")) {
				String url = itemObj.getString("Hyperlink");
				url = url.substring(0, url.indexOf(':')).toLowerCase() + url.substring(url.indexOf(':'));
				notification.setHyperlink(url);
			}
			if (!itemObj.isNull("LinkLabel")) {
				notification.setLinkLabel(itemObj.getString("LinkLabel"));
			}
			if (!itemObj.isNull("Restriction")) {
				notification.setRestriction(itemObj.getInt("Restriction"));
			}
			if (!itemObj.isNull("StartDate")) {
				notification.setStartDate(Utils.convertJsonDate(itemObj
						.getString("StartDate")));
			}

		}

		return notifications;

	}

}