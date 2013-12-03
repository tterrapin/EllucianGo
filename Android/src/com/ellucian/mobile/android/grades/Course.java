package com.ellucian.mobile.android.grades;

import java.util.ArrayList;
import java.util.List;

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
	private List<Grade> grades = new ArrayList<Grade>();
	private Grade finalGrade;

	public void setGrades(List<Grade> grades) {
		this.grades = grades;
	}

	private String name;
	private String sectionTitle;


	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Course)) return false; 
		Course c = (Course)o;
		
		return c.getCourseId().equals(courseId) && c.getName().equals(name) && c.getSectionTitle().equals(sectionTitle);
	}

	public Course() {
		;
	}

	@SuppressWarnings("unchecked")
	public Course(Parcel in) {
		description = in.readString();
		name = in.readString();
		sectionTitle = in.readString();
		courseId = in.readString();
		grades = in.readArrayList(Grade.class.getClassLoader());
		finalGrade = in.readParcelable(Grade.class.getClassLoader());
	}

	public void addFinalGrade(Grade grade) {
		finalGrade = grade;
		addGrade(grade);
	}

	public void addGrade(Grade grade) {
		grades.add(grade);
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

	public List<Grade> getGrades() {
		return grades;
	}

	public String getName() {
		return name;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}


	public void setCourseId(String id) {
		this.courseId = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Grade getFinalGrade() {
		return finalGrade;
	}

	public void setFinalGrade(Grade finalGrade) {
		this.finalGrade = finalGrade;
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
		dest.writeString(courseId);
		dest.writeList(grades);
		dest.writeParcelable(finalGrade, flags);
	}
}
