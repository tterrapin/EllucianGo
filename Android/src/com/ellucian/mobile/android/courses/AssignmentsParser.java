package com.ellucian.mobile.android.courses;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ellucian.mobile.android.Utils;

public class AssignmentsParser {
	public static List<Assignment> parse(String jString) throws JSONException {
		final List<Assignment> assignments = new ArrayList<Assignment>();
		final JSONObject jObject = new JSONObject(jString);
		final JSONArray jAssignments = jObject
				.getJSONArray("Assignments");
		for (int i = 0; i < jAssignments.length(); i++) {
			final Assignment assignment = new Assignment();
			assignments.add(assignment);
			final JSONObject itemObj = jAssignments.getJSONObject(i);
			if (!itemObj.isNull("Description")) {
				assignment.setDescription(itemObj.getString("Description"));
			}
			if (!itemObj.isNull("DueDate")) {
				assignment.setDueDate(Utils
						.convertJsonDate(itemObj.getString("DueDate")));
			}
			if (!itemObj.isNull("Name")) {
				assignment.setName(itemObj.getString("Name"));
			}
			if (!itemObj.isNull("Url")) {
				assignment.setUrl(itemObj.getString("Url"));
			}

		}
		return assignments;
	}
}