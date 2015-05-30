/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.grades;

import android.database.Cursor;

public class CourseModel {
	private String title;
	private String id;
	private Cursor grades;
	
	/**
	 * Constructor for convenience
	 * @param id id of the course
	 * @param title of the course
	 * @param grades cursor for the grades
	 */
	CourseModel(String id, String title, Cursor grades) {
		this.id = id;
		this.title = title;
		this.grades = grades;
	}
	
	/**
	 * Gets the title
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the id
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the grades cursor
	 * @return
	 */
	public Cursor getGrades() {
		return grades;
	}
	
	/**
	 * Sets the grades cursor
	 * @param grades
	 */
	public void setGrades(Cursor grades) {
		this.grades = grades;
	}
}
