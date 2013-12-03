package com.ellucian.mobile.android.courses;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RosterParser {
	private static void parse(JSONArray items, List<RosterContact> contacts)
			throws JSONException {
		for (int j = 0; j < items.length(); j++) {
			final RosterContact contact = new RosterContact();
			contacts.add(contact);
			final JSONObject itemObj = items.getJSONObject(j);
			if (!itemObj.isNull("DisplayName")) {
				contact.setName(itemObj.getString("DisplayName"));
			}
			if (!itemObj.isNull("Domain")) {
				contact.setDomain(itemObj.getString("Domain"));
			}
			if (!itemObj.isNull("UserName")) {
				contact.setUsername(itemObj.getString("UserName"));
			}
		}
	}

	public static Roster parse(String jString) throws JSONException {
		final Roster roster = new Roster();
		final JSONObject jObject = new JSONObject(jString);
		parse(jObject.getJSONArray("Faculty"), roster.getFaculty());
		parse(jObject.getJSONArray("Students"), roster.getStudents());
		return roster;
	}
}