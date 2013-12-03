package com.ellucian.mobile.android.schoolselector;

import java.util.ArrayList;
import java.util.Collections;

public class Institutions {

	private final ArrayList<Institution> institutions = new ArrayList<Institution>();

	public void add(Institution institution) {
		institutions.add(institution);
	}

	public ArrayList<Institution> getInstitutions() {
		Collections.sort(institutions);
		return institutions;
	}

}