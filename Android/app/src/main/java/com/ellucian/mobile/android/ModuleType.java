/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android;

public interface ModuleType {

	public static final String MODULE = "mapp";
	public static final String ABOUT = "about";
	public static final String AUDIO = "audio";
	public static final String COURSES = "courses";
	public static final String DIRECTORY = "directory";
	public static final String EVENTS = "events";
    public static final String STUDENT_FINANCIALS = "studentFinancials";
	public static final String FEED = "feed";
	public static final String GRADES = "grades";
	public static final String HEADER = "header";
    public static final String ILP = "ilp";
	public static final String MAPS = "maps";
	public static final String NOTIFICATIONS = "notifications";
	public static final String NUMBERS = "numbers";
	public static final String REGISTRATION = "registration";
	public static final String VIDEO = "video";
	public static final String WEB = "web";
	public static final String CUSTOM = "custom";
	
	// internal
		public static final String _HOME = "_home";
		public static final String _ABOUT = "_about";
		public static final String _SWITCH_SCHOOLS = "_switch_schools";
		public static final String _SIGN_IN = "_sign_in";

	public static final String[] AUTHENTICATION_NEEDED = new String[] {
            ILP, COURSES, DIRECTORY, STUDENT_FINANCIALS, GRADES, NOTIFICATIONS, REGISTRATION, CUSTOM };

	public static final String[] ALL = new String[] { ABOUT, ILP, AUDIO, COURSES,
			DIRECTORY, EVENTS, STUDENT_FINANCIALS, FEED, GRADES, HEADER, MAPS, NOTIFICATIONS,
			NUMBERS, REGISTRATION, VIDEO, WEB, CUSTOM };
	
	public static final String[] ALL_WITH_INTERNAL = new String[] { ABOUT, ILP, AUDIO,
		COURSES, DIRECTORY, EVENTS, STUDENT_FINANCIALS, FEED, GRADES, HEADER, MAPS, NOTIFICATIONS, NUMBERS,
		REGISTRATION, VIDEO, WEB, CUSTOM, _HOME, _ABOUT, _SWITCH_SCHOOLS, _SIGN_IN };
	
}