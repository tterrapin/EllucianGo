package com.ellucian.mobile.android.grades;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

public class Grade implements Parcelable {
	public static final Parcelable.Creator<Grade> CREATOR = new Parcelable.Creator<Grade>() {
		public Grade createFromParcel(Parcel in) {
			return new Grade(in);
		}

		public Grade[] newArray(int size) {
			return new Grade[size];
		}
	};
	private String grade;
	private String label;
	private Calendar updatedDate;

	public Grade() {
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Grade)) return false;
		Grade g = (Grade)o;
		return g.getGrade().equals(grade) && g.getLabel().equals(label) && g.getUpdatedDate().equals(updatedDate);
	}

	public Grade(Parcel in) {
		label = in.readString();
		grade = in.readString();
		final long temp = in.readLong();
		if (temp != 0) {
			updatedDate = Calendar.getInstance();
			updatedDate.setTimeInMillis(temp);
		}
	}

	public int describeContents() {
		return 0;
	}

	public String getGrade() {
		return grade;
	}

	public String getLabel() {
		return label;
	}

	public Calendar getUpdatedDate() {
		return updatedDate;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setUpdatedDate(Calendar updatedDate) {
		this.updatedDate = updatedDate;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(label);
		dest.writeString(grade);
		dest.writeLong(updatedDate != null ? updatedDate.getTimeInMillis() : 0);
	}
}
