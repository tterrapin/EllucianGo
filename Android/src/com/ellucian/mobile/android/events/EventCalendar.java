package com.ellucian.mobile.android.events;

import java.util.ArrayList;
import java.util.List;

public class EventCalendar {
	public List<Event> items = new ArrayList<Event>();

	public String title;

	public void add(Event item) {
		items.add(item);

	}

	public void setTitle(String title) {
		this.title = title;

	}

	@Override
	public String toString() {
		return title;
	}

}