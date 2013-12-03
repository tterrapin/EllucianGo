package com.ellucian.mobile.android.courses;

import java.util.ArrayList;
import java.util.List;

import com.ellucian.mobile.android.grades.Grade;

import android.os.Parcel;
import android.os.Parcelable;

public class Course implements Parcelable {
	public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
		public Course createFromParcel(Parcel in) {
			return new Course(in);
		}

		public Course[] newArray(int size) {
			return new Course[size];
		}
	};
	private String courseId;
	private String description;
	private List<Faculty> faculty = new ArrayList<Faculty>();
	private ArrayList<Grade> grades = new ArrayList<Grade>();
	private boolean isILP;
	private String learningProviderId;
	private String name;
	private String sectionTitle;
	private List<Session> sessions = new ArrayList<Session>();
	private String term;

	public Course() {
		;
	}

	@SuppressWarnings("unchecked")
	public Course(Parcel in) {
		description = in.readString();
		name = in.readString();
		sectionTitle = in.readString();
		faculty = in.readArrayList(Faculty.class.getClassLoader());
		sessions = in.readArrayList(Session.class.getClassLoader());
		isILP = in.readInt() == 1;
		learningProviderId = in.readString();
		courseId = in.readString();
		grades = in.readArrayList(Grade.class.getClassLoader());
	}

	public void addFaculty(Faculty faculty) {
		this.faculty.add(faculty);
	}

	public void addGrade(Grade grade) {
		grades.add(grade);
	}

	public void addSession(Session session) {
		this.sessions.add(session);
	}

	public boolean allowsAnnouncements() {
		return learningProviderId != null && learningProviderId.length() > 0;
	}

	public boolean allowsAssignments() {
		return isILP();
	}

	public boolean allowsEvents() {
		return learningProviderId != null && learningProviderId.length() > 0;
	}

	public int describeContents() {
		return 0;
	}

	public String getCourseId() {
		return courseId;
	}

	public String getDescription() {
		return description;
	}

	public List<Faculty> getFaculty() {
		return faculty;
	}

	public ArrayList<Grade> getGrades() {
		return grades;
	}

	public String getIdForUrl() {
		if (isILP()) {
			return learningProviderId;
		} else if (learningProviderId != null) {
			return learningProviderId;
		} else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public String getTerm() {
		return term;
	}

	public String getType() {
		if (isILP()) {
			return "ilp";
		} else if (learningProviderId != null) {
			return "portal";
		} else {
			return null;
		}
	}

	public boolean isILP() {
		return isILP;
	}

	public void setCourseId(String id) {
		this.courseId = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIsILP(boolean isILP) {
		this.isILP = isILP;
	}

	public void setLearningProviderId(String learningProviderId) {
		this.learningProviderId = learningProviderId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(description);
		dest.writeString(name);
		dest.writeString(sectionTitle);
		dest.writeList(faculty);
		dest.writeList(sessions);
		dest.writeInt(isILP ? 1 : 0);
		dest.writeString(learningProviderId);
		dest.writeString(courseId);
		dest.writeList(grades);
	}
}
