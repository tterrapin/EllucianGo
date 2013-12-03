package com.ellucian.mobile.android.courses;

import java.util.ArrayList;
import java.util.List;

public class Roster {
	private final List<RosterContact> faculty = new ArrayList<RosterContact>();
	private final List<RosterContact> students = new ArrayList<RosterContact>();

	public void addFaculty(RosterContact contact) {
		faculty.add(contact);
	}

	public void addStudent(RosterContact contact) {
		students.add(contact);
	}

	public List<RosterContact> getFaculty() {
		return faculty;
	}

	public List<RosterContact> getStudents() {
		return students;
	}
}
