package com.ellucian.mobile.android.directory;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileParser {

	public static Profile parse(String jString) throws JSONException {

		final JSONObject itemObj = new JSONObject(jString);

		final Profile contact = new Profile();

		if (!itemObj.isNull("PreferredName")) {
			contact.setPreferredName(itemObj.getString("PreferredName"));
		}
		if (!itemObj.isNull("OptOut")) {
			contact.setOptOut(itemObj.getBoolean("OptOut"));
		}
		if (!itemObj.isNull("LastName")) {
			contact.setLastName(itemObj.getString("LastName"));
		}
		if (!itemObj.isNull("FirstName")) {
			contact.setFirstName(itemObj.getString("FirstName"));
		}
		if (!itemObj.isNull("WorkPhone")) {
			contact.setWorkPhone(itemObj.getString("WorkPhone"));
		}
		if (!itemObj.isNull("MobilePhone")) {
			contact.setMobilePhone(itemObj.getString("MobilePhone"));
		}
		if (!itemObj.isNull("ImageUrl")) {
			contact.setImageUrl(itemObj.getString("ImageUrl"));
		}
		if (!itemObj.isNull("Department")) {
			contact.setDepartment(itemObj.getString("Department"));
		}
		if (!itemObj.isNull("Office")) {
			contact.setOffice(itemObj.getString("Office"));
		}
		if (!itemObj.isNull("Title")) {
			contact.setTitle(itemObj.getString("Title"));
		}
		if (!itemObj.isNull("Email")) {
			contact.setEmail(itemObj.getString("Email"));
		}

		return contact;
	}
}