package com.ellucian.mobile.android.directory;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchResultsParser {

	public static List<ContactProxy> parse(String jString) throws JSONException {

		final List<ContactProxy> contacts = new ArrayList<ContactProxy>();
		final JSONObject jObject = new JSONObject(jString);

		final JSONArray jResults = jObject.getJSONArray("Items");
		for (int i = 0; i < jResults.length(); i++) {

			final ContactProxy contact = new ContactProxy();
			contacts.add(contact);

			final JSONObject itemObj = jResults.getJSONObject(i);

			if (!itemObj.isNull("UserDomain")) {
				contact.setUserDomain(itemObj.getString("UserDomain"));
			}
			
			if (!itemObj.isNull("PreferredName")) {
				contact.setPreferredName(itemObj.getString("PreferredName"));
			}
			if (!itemObj.isNull("UserName")) {
				contact.setUserName(itemObj.getString("UserName"));

			}
		}
		return contacts;
	}
}