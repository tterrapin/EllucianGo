/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.grades;

import android.database.Cursor;

@SuppressWarnings("JavaDoc")
class CourseModel {
    private String id;
    private String label;
    private String title;
	private Cursor grades;

	/**
	 * Constructor for convenience
	 * @param id id of the course
     * @param label courseName + section number
     * @param title title of the course
	 * @param grades cursor for the grades
	 */
	CourseModel(String id, String label, String title, Cursor grades) {
		this.id = id;
        this.label = label;
		this.title = title;
		this.grades = grades;
	}

    /**
     * Gets the label
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
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
