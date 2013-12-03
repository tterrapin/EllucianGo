package com.ellucian.mobile.android.news;

import java.util.Calendar;

public class NewsItem {

	private String content;
	private Calendar date;
	private String image;
	private String title;
	private String website;

	public String getContent() {
		return content;
	}

	public Calendar getDate() {
		return date;
	}

	public String getImage() {
		return image;
	}

	public String getTitle() {
		return title;
	}

	public String getWebsite() {
		return website;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setWebsite(String website) {
		this.website = website;
	}
}
