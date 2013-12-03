package com.ellucian.mobile.android.news;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsFeed {
	public List<NewsItem> items = new ArrayList<NewsItem>();
	public Date lastUpdatedTime;
	public String title;

	public void add(NewsItem item) {
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