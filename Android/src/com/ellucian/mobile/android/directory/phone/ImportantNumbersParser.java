package com.ellucian.mobile.android.directory.phone;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportantNumbersParser {

	public static List<ImportantNumbersCategory> parse(String jString)
			throws JSONException {

		final List<ImportantNumbersCategory> categories = new ArrayList<ImportantNumbersCategory>();
		final JSONObject jObject = new JSONObject(jString);

		final JSONArray contactsObj = jObject.getJSONArray("Contacts");
		for (int i = 0; i < contactsObj.length(); i++) {

			final ImportantNumbersCategory category = new ImportantNumbersCategory();
			categories.add(category);
			if (!contactsObj.getJSONObject(i).isNull("Category")) {
				category.setName(contactsObj.getJSONObject(i)
						.getString("Category"));
			}

			final JSONArray items = contactsObj.getJSONObject(i).getJSONArray(
					"Items");
			for (int j = 0; j < items.length(); j++) {
				final ImportantNumbersContact contact = new ImportantNumbersContact();
				category.add(contact);

				final JSONObject itemObj = items.getJSONObject(j);

				if (!itemObj.isNull("Name")) {
					contact.setName(itemObj.getString("Name"));
				}
				if (!itemObj.isNull("Phone")) {
					contact.setPhone(itemObj.getString("Phone"));
				}
				if (!itemObj.isNull("Email")) {
					contact.setEmail(itemObj.getString("Email"));
				}
				if (!itemObj.isNull("Label")) {
					contact.setLabel(itemObj.getString("Label"));
				}
				if (!itemObj.isNull("Address")) {
					contact.setAddress(itemObj.getString("Address"));
				}
				if (!itemObj.isNull("Latitude")) {
					contact.setLatitude(itemObj.getDouble("Latitude"));
				}
				if (!itemObj.isNull("Longitude")) {
					contact.setLongitude(itemObj.getDouble("Longitude"));
				}
			}

		}

		return categories;

	}
}