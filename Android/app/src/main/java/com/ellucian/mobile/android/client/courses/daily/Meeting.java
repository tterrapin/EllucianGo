/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.daily;

import android.text.TextUtils;

import com.ellucian.mobile.android.util.CalendarUtils;

import java.io.Serializable;
import java.util.Date;

public class Meeting implements Serializable, Comparable<Meeting> {
	public String termId;
	public String sectionId;
	public String sectionTitle;
	public String courseName;
	public String courseSectionNumber;
	public String[] categories;
	public String start;
	public String end;
	public String building;
	public String buildingId;
	public String campusId;
	public String room;
	public boolean isInstructor;

    @Override
    public int compareTo(Meeting other) {
        if (this == other) {
            return 0;
        }

        if (TextUtils.isEmpty(start)) {
            return 1;
        }
        if (other == null || TextUtils.isEmpty(other.start)) {
            return -1;
        }

        Date thisStart = CalendarUtils.parseFromUTC(start);
        Date otherStart = CalendarUtils.parseFromUTC(other.start);

        return thisStart.compareTo(otherStart);

    }
}
