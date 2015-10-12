/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.text.TextUtils;

import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;
import com.ellucian.mobile.android.util.CalendarUtils;

import java.util.Date;

public class IlpItemHolder implements EllucianRecyclerAdapter.ItemInfoHolder, Comparable<IlpItemHolder> {
	public String type;
	private String sectionId;
    public String sectionName;
	public String title;
	public String date;
	public String displayDate;
	public String content;
	public String location;
	public String url;
	
	IlpItemHolder() {
	}
	
	IlpItemHolder(String type, String sectionId, String sectionName, String title, String date, String displayDate, String content,
				  String location, String url) {
		this.type = type;
		this.sectionId = sectionId;
        this.sectionName = sectionName;
		this.title = title;
		this.date = date;
		this.displayDate = displayDate;
		this.content = content;
		this.location = location;
		this.url = url;
	}
		
	@Override
	public String getDefaultText() {
		return title;
	}

	@Override
	public int compareTo(IlpItemHolder other) {
        if (this == other) {
            return 0;
        }
		if (TextUtils.isEmpty(date)) {
			return 1;
		}
		if (other == null || TextUtils.isEmpty(other.date)) {
			return -1;
		}
		
		Date thisDate = CalendarUtils.parseFromUTC(date);
		Date otherDate = CalendarUtils.parseFromUTC(other.date);
		
		return thisDate.compareTo(otherDate);
	}

}
