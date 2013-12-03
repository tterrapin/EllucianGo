package com.ellucian.mobile.android.directory.phone;

import java.util.ArrayList;
import java.util.List;

public class ImportantNumbersCategory {

	private final List<ImportantNumbersContact> contacts = new ArrayList<ImportantNumbersContact>();
	private String name;

	public void add(ImportantNumbersContact contact) {
		contacts.add(contact);

	}

	public List<ImportantNumbersContact> getContacts() {
		return contacts;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;

	}

	@Override
	public String toString() {
		return name;
	}
}
