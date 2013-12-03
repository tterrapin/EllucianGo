package com.ellucian.mobile.android.courses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Term implements Parcelable {
	public static final Parcelable.Creator<Term> CREATOR = new Parcelable.Creator<Term>() {
		public Term createFromParcel(Parcel in) {
			return new Term(in);
		}

		public Term[] newArray(int size) {
			return new Term[size];
		}
	};
	private List<Course> courses = new ArrayList<Course>();
	private Calendar date;
	private String name;

	public Term() {
	}

	@SuppressWarnings("unchecked")
	public Term(Parcel in) {
		name = in.readString();
		date = Calendar.getInstance();
		date.setTimeInMillis(in.readLong());
		courses = in.readArrayList(Term.class.getClassLoader());
	}

	public void addCourse(Course course) {
		courses.add(course);
	}

	public int describeContents() {
		return 0;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public Calendar getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeLong(date.getTimeInMillis());
		dest.writeList(courses);
	}
}
