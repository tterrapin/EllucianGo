/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android;

public interface ModuleType {

	String MODULE = "mapp";
	String ABOUT = "about";
	String AUDIO = "audio";
	String COURSES = "courses";
	String DIRECTORY = "directory";
	String EVENTS = "events";
    String STUDENT_FINANCIALS = "studentFinancials";
	String FEED = "feed";
	String GRADES = "grades";
	String HEADER = "header";
    String ILP = "ilp";
	String MAPS = "maps";
	String NOTIFICATIONS = "notifications";
	String NUMBERS = "numbers";
	String REGISTRATION = "registration";
	String VIDEO = "video";
	String WEB = "web";
	String CUSTOM = "custom";
	
	// internal
	String _HOME = "_home";
		String _ABOUT = "_about";
		String _SWITCH_SCHOOLS = "_switch_schools";
		String _SIGN_IN = "_sign_in";

	String[] AUTHENTICATION_NEEDED = new String[] {
            ILP, COURSES, DIRECTORY, STUDENT_FINANCIALS, GRADES, NOTIFICATIONS, REGISTRATION, CUSTOM };

	String[] ALL = new String[] { ABOUT, ILP, AUDIO, COURSES,
			DIRECTORY, EVENTS, STUDENT_FINANCIALS, FEED, GRADES, HEADER, MAPS, NOTIFICATIONS,
			NUMBERS, REGISTRATION, VIDEO, WEB, CUSTOM };
	
	String[] ALL_WITH_INTERNAL = new String[] { ABOUT, ILP, AUDIO,
		COURSES, DIRECTORY, EVENTS, STUDENT_FINANCIALS, FEED, GRADES, HEADER, MAPS, NOTIFICATIONS, NUMBERS,
		REGISTRATION, VIDEO, WEB, CUSTOM, _HOME, _ABOUT, _SWITCH_SCHOOLS, _SIGN_IN };
	
}