package com.ellucian.mobile.android.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncementsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignmentsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCoursesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEventsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseInstructorsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseMeetingsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CoursePatternsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseRosterColumns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseTermsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.Events;
import com.ellucian.mobile.android.provider.EllucianContract.EventsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.EventsCategoriesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.EventsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.GradeCoursesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTerms;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTermsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.GradesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.GradesCourses;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsCategoriesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampusesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesPropertiesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesRolesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.News;
import com.ellucian.mobile.android.provider.EllucianContract.NewsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.NewsCategoriesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.NewsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.NotificationsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersCategories;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersCategoriesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersColumns;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLevelsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLocationsColumns;

public class EllucianDatabase extends SQLiteOpenHelper {
	
	// 7 = Ellucian Mobile 3.5
	// 8 = Ellucian Mobile 3.6
	// 9 = Ellucian Mobile 4.0
	private static final int DB_VERSION = 9;
	private static final String DB_NAME = "ellucian_mobile.db";

	public interface Tables {
		String MODULES = "modules";
		String MODULES_ROLES = "modules_roles";
		String MODULES_PROPERTIES = "modules_urls";

		String GRADE_TERMS = "grade_terms";
		String GRADE_COURSES = "grade_courses";
		String GRADES = "grades";

		String GRADE_TERMS_JOIN_GRADE_COURSES = GRADE_TERMS
				+ " LEFT OUTER JOIN " + GRADE_COURSES + " on " + GRADE_TERMS
				+ "." + GradeTerms.TERM_ID + "=" + GRADE_COURSES + "."
				+ GradeTerms.TERM_ID;

		String GRADES_COURSES_JOIN_GRADES = GRADE_COURSES + " LEFT OUTER JOIN "
				+ GRADES + " on " + GRADE_COURSES + "."
				+ GradesCourses.COURSE_ID + "=" + GRADES + "."
				+ GradesCourses.COURSE_ID;

		String MAPS_CAMPUSES = "maps_campuses";
		String MAPS_BUILDINGS = "maps_buildings";
		String MAPS_BUILDINGS_CATEGORIES = "maps_buildings_categories";
		String MAPS_BUILDINGS_BUILDINGSCATEGORIES = "maps_buildings_buildings_categories";
		String NOTIFICATIONS = "notifications";
		String NUMBERS = "numbers";
		String NUMBERS_CATEGORIES = "numbers_categories";
		String NEWS = "news";
		String NEWS_CATEGORIES = "news_categories";
		String NEWS_SEARCH = "news_search";
		String COURSE_TERMS = "course_terms";
		String COURSE_COURSES = "course_courses";
		String COURSE_INSTRUCTORS = "course_instructors";
		String COURSE_PATTERNS = "course_patterns";
		String COURSE_MEETINGS = "course_meetings";
		String COURSE_ROSTER = "course_roster";
		String COURSE_ASSIGNMENTS = "course_assignments";
		String COURSE_ANNOUNCEMENTS = "course_announcements";
		String COURSE_EVENTS = "course_events";
		String EVENTS = "events";
		String EVENTS_CATEGORIES = "events_categories";
		String EVENTS_EVENTS_CATEGORIES = "events_events_categories";
		String EVENTS_JOIN_CATEGORIES = EVENTS + " LEFT OUTER JOIN "
				+ EVENTS_EVENTS_CATEGORIES + " on " + EVENTS + "."
				+ Events.EVENTS_ID + "=" + EVENTS_EVENTS_CATEGORIES + "."
				+ Events.EVENTS_ID + " LEFT OUTER JOIN " + EVENTS_CATEGORIES
				+ " on " + EVENTS_CATEGORIES + "."
				+ EventsCategories.EVENTS_CATEGORY_ID + "="
				+ EVENTS_EVENTS_CATEGORIES + "."
				+ EventsCategories.EVENTS_CATEGORY_ID;
		String EVENTS_SEARCH = "events_search";

		String NEWS_SEARCH_JOIN_NEWS = Tables.NEWS_SEARCH + " LEFT OUTER JOIN "
				+ Tables.NEWS + " ON " + NewsSearchColumns.NEWS_ID + "="
				+ NewsColumns.NEWS_ENTRY_ID;
		String EVENTS_SEARCH_JOIN_EVENTS_JOIN_CATEGORIES = Tables.EVENTS_SEARCH + " LEFT OUTER JOIN "
				+ Tables.EVENTS + " ON " + Tables.EVENTS_SEARCH + "." + EventsSearchColumns.EVENTS_ID + "="
				+ EVENTS + "."
				+ EventsColumns.EVENTS_ID + " LEFT OUTER JOIN "
						+ EVENTS_EVENTS_CATEGORIES + " on " + EVENTS + "."
						+ Events.EVENTS_ID + "=" + EVENTS_EVENTS_CATEGORIES + "."
						+ Events.EVENTS_ID + " LEFT OUTER JOIN " + EVENTS_CATEGORIES
						+ " on " + EVENTS_CATEGORIES + "."
						+ EventsCategories.EVENTS_CATEGORY_ID + "="
						+ EVENTS_EVENTS_CATEGORIES + "."
						+ EventsCategories.EVENTS_CATEGORY_ID;
		String REGISTRATION_LOCATIONS = "registration_locations";
		String REGISTRATION_LEVELS = "registration_levels";
		
	}

	private interface Triggers {
		String NEWS_SEARCH_INSERT = "news_search_insert";
		String NEWS_SEARCH_DELETE = "news_search_delete";
		String NEWS_SEARCH_UPDATE = "news_search_update";
		String EVENTS_SEARCH_INSERT = "events_search_insert";
		String EVENTS_SEARCH_DELETE = "events_search_delete";
		String EVENTS_SEARCH_UPDATE = "events_search_update";
	}

	private interface Subquery {
		String NEWS_BODY = "(new." + News.NEWS_TITLE + "||'; '||new."
				+ News.NEWS_CONTENT + ")";
		String EVENTS_BODY = "(new." + Events.EVENTS_TITLE + "||'; '||IfNull(new."
				+ Events.EVENTS_DESCRIPTION + ", '')||'; '||new." +
				Events.EVENTS_LOCATION + ")";
	}

	public EllucianDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	/** {@code REFERENCES} clauses. */
	private interface References {
		String MODULES_ID = "REFERENCES " + Tables.MODULES + "("
				+ ModulesColumns.MODULES_ID + ")";
		String TERM_ID = "REFERENCES " + Tables.GRADE_TERMS + "("
				+ GradeTermsColumns.TERM_ID + ")";
		String COURSE_ID = "REFERENCES " + Tables.GRADE_COURSES + "("
				+ GradesCourses.COURSE_ID + ")";
		String MAPS_CAMPUSES_ID = "REFERENCES " + Tables.MAPS_CAMPUSES + "("
				+ MapsCampuses.CAMPUS_ID + ")";
		String MAPS_CAMPUSES_NAME = "REFERENCES " + Tables.MAPS_CAMPUSES + "("
				+ MapsCampuses.CAMPUS_NAME + ")";
		String MAPS_BUILDINGS_CATEGORY_NAME = "REFERENCES " + Tables.MAPS_BUILDINGS_CATEGORIES
				+ "(" + MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME + ")";
		String NUMBERS_CATEGORY_NAME = "REFERENCES " + Tables.NUMBERS_CATEGORIES
				+ "(" + NumbersCategories.NUMBERS_CATEGORY_NAME + ")";
		String COURSE_TERMS_TERM_ID = "REFERENCES " + Tables.COURSE_TERMS + "("
				+ CourseTermsColumns.TERM_ID + ")";
		String COURSE_COURSES_COURSE_ID = "REFERENCES " + Tables.COURSE_COURSES
				+ "(" + CourseCoursesColumns.COURSE_ID + ")";
		String NEWS_ID = "REFERENCES " + Tables.NEWS + "(" + News.NEWS_ENTRY_ID
				+ ")";
		String NEWS_CATEGORY_ID = "REFERENCES " + Tables.NEWS_CATEGORIES + "("
				+ NewsCategories.NEWS_CATEGORY_ID + ")";
		String EVENTS_ID = "REFERENCES " + Tables.EVENTS + "("
				+ Events.EVENTS_ID + ")";
		String EVENTS_CATEGORY_ID = "REFERENCES " + Tables.EVENTS_CATEGORIES
				+ "(" + EventsCategories.EVENTS_CATEGORY_ID + ")";
		String BUILDING_BUILDING_ID = "REFERENCES " + Tables.MAPS_BUILDINGS + "("
				+ MapsBuildings.BUILDING_BUILDING_ID + ")";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.MODULES + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL, "
				+ ModulesColumns.MODULES_ICON_URL + " TEXT NOT NULL, "
				+ ModulesColumns.MODULE_ORDER + " INTEGER NOT NULL, "
				+ ModulesColumns.MODULE_NAME + " TEXT NOT NULL, "
				+ ModulesColumns.MODULE_SECURE + " TEXT, " 
				+ ModulesColumns.MODULE_SHOW_FOR_GUEST + " INTEGER NOT NULL DEFAULT 0, " 
				+ ModulesColumns.MODULE_TYPE + " TEXT NOT NULL, "
				+ ModulesColumns.MODULE_SUB_TYPE + " TEXT, "
				+ "UNIQUE (" + ModulesColumns.MODULES_ID
				+ ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.MODULES_PROPERTIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ ModulesPropertiesColumns.MODULE_PROPERTIES_NAME + " TEXT NOT NULL, "
				+ ModulesPropertiesColumns.MODULE_PROPERTIES_VALUE + " TEXT NOT NULL" + ")");
		
		db.execSQL("CREATE TABLE " + Tables.MODULES_ROLES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ ModulesRolesColumns.MODULE_ROLES_NAME + " TEXT NOT NULL " + ")");


		db.execSQL("CREATE TABLE " + Tables.GRADE_TERMS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ GradeTermsColumns.TERM_ID + " TEXT NOT NULL, "
				+ GradeTermsColumns.TERM_NAME + " TEXT NOT NULL, "
				+ GradeTermsColumns.TERM_START_DATE + " TEXT, "
				+ GradeTermsColumns.TERM_END_DATE + " TEXT, " + "UNIQUE ("
				+ GradeTermsColumns.TERM_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.GRADE_COURSES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ GradeCoursesColumns.COURSE_ID + " TEXT NOT NULL,"
				+ GradeTermsColumns.TERM_ID + " TEXT NOT NULL "
				+ References.TERM_ID + ", " + GradeCoursesColumns.COURSE_ERP_ID
				+ " TEXT, " + GradeCoursesColumns.COURSE_TITLE + " TEXT, "
				+ GradeCoursesColumns.COURSE_DESCRIPTION + " TEXT, "
				+ GradeCoursesColumns.COURSE_CREDIT_HOURS + " TEXT, "
				+ GradeCoursesColumns.COURSE_SECTION + " TEXT,"
				+ "UNIQUE (" + GradeCoursesColumns.COURSE_ID
				+ ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.GRADES + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ GradeCoursesColumns.COURSE_ID + " TEXT NOT NULL "
				+ References.COURSE_ID + ", " + GradesColumns.GRADE_NAME
				+ " TEXT, " + GradesColumns.GRADE_TYPE + " TEXT, "
				+ GradesColumns.GRADE_UPDATED + " TEXT, "
				+ GradesColumns.GRADE_VALUE + " TEXT " + ")");
		
		db.execSQL("CREATE TABLE " + Tables.MAPS_CAMPUSES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MapsCampusesColumns.CAMPUS_ID + " TEXT NOT NULL, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ MapsCampusesColumns.CAMPUS_NAME + " TEXT, "
				+ MapsCampusesColumns.CAMPUS_CENTER_LATITUDE + " INTEGER, "
				+ MapsCampusesColumns.CAMPUS_CENTER_LONGITUDE + " INTEGER, "
				+ MapsCampusesColumns.CAMPUS_NORTHWEST_LATITUDE + " REAL, "
				+ MapsCampusesColumns.CAMPUS_NORTHWEST_LONGITUDE + " REAL, "
				+ MapsCampusesColumns.CAMPUS_SOUTHEAST_LATITUDE + " REAL, "
				+ MapsCampusesColumns.CAMPUS_SOUTHEAST_LONGITUDE + " REAL, "
				+ "UNIQUE (" + MapsCampusesColumns.CAMPUS_ID
				+ ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.MAPS_BUILDINGS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MapsBuildingsColumns.BUILDING_BUILDING_ID + " TEXT, "
				+ MapsCampusesColumns.CAMPUS_ID + " TEXT "
				+ References.MAPS_CAMPUSES_ID + ", "
				+ MapsCampusesColumns.CAMPUS_NAME + " TEXT "
				+ References.MAPS_CAMPUSES_NAME + ", "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ MapsBuildingsColumns.BUILDING_CATEGORIES + " TEXT,"
				+ MapsBuildingsColumns.BUILDING_NAME + " TEXT,"
				+ MapsBuildingsColumns.BUILDING_DESCRIPTION + " TEXT,"
				+ MapsBuildingsColumns.BUILDING_ADDITIONAL_SERVICES + " TEXT,"
				+ MapsBuildingsColumns.BUILDING_IMAGE_URL + " TEXT,"
				+ MapsBuildingsColumns.BUILDING_ADDRESS + " TEXT,"
				+ MapsBuildingsColumns.BUILDING_LATITUDE + " REAL,"
				+ MapsBuildingsColumns.BUILDING_LONGITUDE + " REAL" + ")");
		
		db.execSQL("CREATE TABLE " + Tables.MAPS_BUILDINGS_CATEGORIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ MapsBuildingsCategoriesColumns.MAPS_BUILDINGS_CATEGORY_NAME + " TEXT, "
				+ "UNIQUE (" + MapsBuildingsCategoriesColumns.MAPS_BUILDINGS_CATEGORY_NAME
				+ ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.MAPS_BUILDINGS_BUILDINGSCATEGORIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "	+ References.MODULES_ID + ", " 
				+ MapsBuildingsColumns.BUILDING_BUILDING_ID + " TEXT NOT NULL " + References.BUILDING_BUILDING_ID + ", "
				+ MapsCampusesColumns.CAMPUS_ID + " TEXT NOT NULL " + References.MAPS_CAMPUSES_ID + ", "
				+ MapsBuildingsColumns.BUILDING_NAME + " TEXT NOT NULL REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_NAME + "), "
				+ MapsBuildingsColumns.BUILDING_CATEGORIES + " TEXT REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_CATEGORIES + "), "
				+ MapsBuildingsColumns.BUILDING_DESCRIPTION + " TEXT REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_DESCRIPTION + "), "
				+ MapsBuildingsColumns.BUILDING_ADDITIONAL_SERVICES + " TEXT REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_ADDITIONAL_SERVICES + "), "
				+ MapsBuildingsColumns.BUILDING_IMAGE_URL + " TEXT REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_IMAGE_URL + "), "
				+ MapsBuildingsColumns.BUILDING_ADDRESS + " TEXT REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_ADDRESS + "), "
				+ MapsBuildingsColumns.BUILDING_LATITUDE + " REAL REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_LATITUDE + "), "
				+ MapsBuildingsColumns.BUILDING_LONGITUDE + " REAL REFERENCES " + Tables.MAPS_BUILDINGS+ "(" + MapsBuildings.BUILDING_LONGITUDE + "), "
				+ MapsBuildingsCategoriesColumns.MAPS_BUILDINGS_CATEGORY_NAME
				+ " TEXT NOT NULL " + References.MAPS_BUILDINGS_CATEGORY_NAME + ")");
				
		db.execSQL("CREATE TABLE " + Tables.NOTIFICATIONS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ NotificationsColumns.NOTIFICATIONS_ID + " TEXT NOT NULL, "
				+ NotificationsColumns.NOTIFICATIONS_TITLE + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_DETAILS + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_HYPERLINK + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_LINK_LABEL + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_DATE + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_SOURCE + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_DISPATCH_DATE + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_MOBILE_HEADLINE + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_EXPIRES + " TEXT, "
				+ NotificationsColumns.NOTIFICATIONS_PUSH + " INTEGER NOT NULL DEFAULT 0, " 
				+ NotificationsColumns.NOTIFICATIONS_MODULE + " INTEGER NOT NULL DEFAULT 0, " 
				+ NotificationsColumns.NOTIFICATIONS_STICKY + " INTEGER NOT NULL DEFAULT 1, "
				+ NotificationsColumns.NOTIFICATIONS_STATUSES + " TEXT, " 
				+ "UNIQUE (" + NotificationsColumns.NOTIFICATIONS_ID
				+ ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.NUMBERS + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ NumbersCategoriesColumns.NUMBERS_CATEGORY_NAME
				+ " TEXT NOT NULL " + References.NUMBERS_CATEGORY_NAME + ", "
				+ ModulesColumns.MODULES_ID + " TEXT " + References.MODULES_ID + ", "
				+ NumbersColumns.NUMBERS_NAME + " TEXT, "
				+ NumbersColumns.NUMBERS_ADDRESS + " TEXT, "
				+ NumbersColumns.NUMBERS_EMAIL + " TEXT, "
				+ NumbersColumns.NUMBERS_PHONE + " TEXT, "
				+ NumbersColumns.NUMBERS_LATITUDE + " REAL, "
				+ NumbersColumns.NUMBERS_LONGITUDE + " REAL, "
				+ NumbersColumns.NUMBERS_BUILDING_ID + " TEXT, "
				+ NumbersColumns.NUMBERS_CAMPUS_ID + " TEXT "
				+ ")");

		db.execSQL("CREATE TABLE " + Tables.NUMBERS_CATEGORIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ NumbersCategoriesColumns.NUMBERS_CATEGORY_NAME + " TEXT, "
				+ ModulesColumns.MODULES_ID + " TEXT " + References.MODULES_ID + ", "
				+ "UNIQUE (" + NumbersCategoriesColumns.NUMBERS_CATEGORY_NAME
				+ ") ON CONFLICT REPLACE)"
		);

		db.execSQL("CREATE TABLE " + Tables.NEWS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ NewsCategoriesColumns.NEWS_CATEGORY_ID + " TEXT NOT NULL "
				+ References.NEWS_CATEGORY_ID + ", "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", " 
				+ NewsColumns.NEWS_ENTRY_ID + " TEXT, " 
				+ NewsColumns.NEWS_FEED_NAME + " TEXT, "
				+ NewsColumns.NEWS_TITLE + " TEXT, " 
				+ NewsColumns.NEWS_CONTENT + " TEXT, " 
				+ NewsColumns.NEWS_LIST_DESCRIPTION + " TEXT, "
				+ NewsColumns.NEWS_LINK + " TEXT, " 
				+ NewsColumns.NEWS_LOGO + " TEXT, " 
				+ NewsColumns.NEWS_POST_DATE + " TEXT) ");

		db.execSQL("CREATE TABLE " + Tables.NEWS_CATEGORIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ NewsCategoriesColumns.NEWS_CATEGORY_ID + " TEXT, "
				+ NewsCategoriesColumns.NEWS_CATEGORY_NAME + " TEXT, "
				+ "UNIQUE (" + NewsCategoriesColumns.NEWS_CATEGORY_ID
				+ ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.COURSE_TERMS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseTermsColumns.TERM_ID + " TEXT NOT NULL, "
				+ CourseTermsColumns.TERM_NAME + " TEXT NOT NULL, "
				+ CourseTermsColumns.TERM_START_DATE + " TEXT, "
				+ CourseTermsColumns.TERM_END_DATE + " TEXT, " + "UNIQUE ("
				+ CourseTermsColumns.TERM_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.COURSE_COURSES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL,"
				+ CourseTermsColumns.TERM_ID + " TEXT NOT NULL "
				+ References.COURSE_TERMS_TERM_ID + ", "
				+ CourseCoursesColumns.COURSE_NAME + " TEXT, "
				+ CourseCoursesColumns.COURSE_TITLE + " TEXT, "
				+ CourseCoursesColumns.COURSE_DESCRIPTION + " TEXT, "
				+ CourseCoursesColumns.COURSE_SECTION_NUMBER + " TEXT, "
				+ CourseCoursesColumns.COURSE_IS_INSTRUCTOR + " INTEGER, "
				+ CourseCoursesColumns.COURSE_LEARNING_PROVIDER + " TEXT, "
				+ CourseCoursesColumns.COURSE_LEARNING_PROVIDER_SITE_ID + " TEXT "
				+ ")");

		db.execSQL("CREATE TABLE " + Tables.COURSE_INSTRUCTORS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL "
				+ References.COURSE_COURSES_COURSE_ID + ", "
				+ CourseInstructorsColumns.INSTRUCTOR_ID + " TEXT NOT NULL, "
				+ CourseInstructorsColumns.INSTRUCTOR_FIRST_NAME + " TEXT, "
				+ CourseInstructorsColumns.INSTRUCTOR_MIDDLE_NAME + " TEXT, "
				+ CourseInstructorsColumns.INSTRUCTOR_LAST_NAME + " TEXT, "
				+ CourseInstructorsColumns.INSTRUCTOR_FORMATTED_NAME + " TEXT, "
				+ CourseInstructorsColumns.INSTRUCTOR_PRIMARY + " INTEGER NOT NULL"
				+ ")");

		db.execSQL("CREATE TABLE " + Tables.COURSE_PATTERNS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL "
				+ References.COURSE_COURSES_COURSE_ID + ", "
				+ CoursePatternsColumns.PATTERN_DAYS + " TEXT, "
				+ CoursePatternsColumns.PATTERN_LOCATION + " TEXT, "
				+ CoursePatternsColumns.PATTERN_START_TIME + " TEXT, "
				+ CoursePatternsColumns.PATTERN_END_TIME + " TEXT, "
				+ CoursePatternsColumns.PATTERN_ROOM + " TEXT, "
				+ MapsBuildingsColumns.BUILDING_BUILDING_ID + " TEXT, "
				+ MapsCampusesColumns.CAMPUS_ID + " TEXT, "
				+ CoursePatternsColumns.PATTERN_INSTRUCTIONAL_METHOD + " TEXT "
				+ ")");

		db.execSQL("CREATE TABLE " + Tables.COURSE_MEETINGS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL "
				+ References.COURSE_COURSES_COURSE_ID + ", "
				+ CourseMeetingsColumns.MEETING_LOCATION + " TEXT, "
				+ CourseMeetingsColumns.MEETING_SUMMARY + " TEXT, "
				+ CourseMeetingsColumns.MEETING_START + " TEXT, "
				+ CourseMeetingsColumns.MEETING_END + " TEXT " 
				+ ")");
		
		db.execSQL("CREATE TABLE " + Tables.COURSE_ROSTER + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseRosterColumns.ROSTER_STUDENT_ID + " TEXT, "
				+ CourseRosterColumns.ROSTER_COURSE_ID + " TEXT NOT NULL, "
				+ CourseRosterColumns.ROSTER_FORMATTED_NAME + " TEXT, "
				+ CourseRosterColumns.ROSTER_FIRST_NAME + " TEXT, "
				+ CourseRosterColumns.ROSTER_MIDDLE_NAME + " TEXT, "
				+ CourseRosterColumns.ROSTER_LAST_NAME + " TEXT NOT NULL, "
				+ CourseRosterColumns.ROSTER_PHOTO + " TEXT "
				+ ")");
		
		db.execSQL("CREATE TABLE " + Tables.COURSE_ASSIGNMENTS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL, "
				+ CourseAssignmentsColumns.ASSIGNMENT_NAME + " TEXT NOT NULL, "
				+ CourseAssignmentsColumns.ASSIGNMENT_DESCRIPTION + " TEXT, "
				+ CourseAssignmentsColumns.ASSIGNMENT_DUE + " TEXT, "
				+ CourseAssignmentsColumns.ASSIGNMENT_URL + " TEXT "
				+ ")");
		
		db.execSQL("CREATE TABLE " + Tables.COURSE_ANNOUNCEMENTS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL, "
				+ CourseAnnouncementsColumns.ANNOUNCEMENT_TITLE + " TEXT NOT NULL, "
				+ CourseAnnouncementsColumns.ANNOUNCEMENT_CONTENT + " TEXT, "
				+ CourseAnnouncementsColumns.ANNOUNCEMENT_DATE + " TEXT, "
				+ CourseAnnouncementsColumns.ANNOUNCEMENT_URL + " TEXT "
				+ ")");
		
		db.execSQL("CREATE TABLE " + Tables.COURSE_EVENTS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL, "
				+ CourseEventsColumns.EVENT_TITLE + " TEXT NOT NULL, "
				+ CourseEventsColumns.EVENT_DESCRIPTION + " TEXT, "
				+ CourseEventsColumns.EVENT_START + " TEXT, "
				+ CourseEventsColumns.EVENT_END + " TEXT, "
				+ CourseEventsColumns.EVENT_LOCATION + " TEXT, "
				+ CourseEventsColumns.EVENT_ALL_DAY + " TEXT "
				+ ")");
		
		
		db.execSQL("CREATE TABLE " + Tables.EVENTS + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ EventsCategoriesColumns.EVENTS_CATEGORY_ID
				+ " TEXT NOT NULL " + References.EVENTS_CATEGORY_ID + ", "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", " + EventsColumns.EVENTS_ID
				+ " TEXT NOT NULL, " + EventsColumns.EVENTS_UID + " TEXT, "
				+ EventsColumns.EVENTS_TITLE + " TEXT, "
				+ EventsColumns.EVENTS_DESCRIPTION + " TEXT, "
				+ EventsColumns.EVENTS_LOCATION + " TEXT, "
				+ EventsColumns.EVENTS_DURATION + " TEXT, "
				+ EventsColumns.EVENTS_CONTACT + " TEXT, "
				+ EventsColumns.EVENTS_EMAIL + " TEXT, "
				+ EventsColumns.EVENTS_CATEGORIES + " TEXT, "
				+ EventsColumns.EVENTS_START + " TEXT, "
				+ EventsColumns.EVENTS_END + " TEXT, "
				+ EventsColumns.EVENTS_ALL_DAY
				+ " INTEGER NOT NULL DEFAULT 0, " + "UNIQUE ("
				+ EventsColumns.EVENTS_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.EVENTS_CATEGORIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", "
				+ EventsCategoriesColumns.EVENTS_CATEGORY_ID + " TEXT, "
				+ EventsCategoriesColumns.EVENTS_CATEGORY_NAME + " TEXT, "
				+ "UNIQUE (" + EventsCategoriesColumns.EVENTS_CATEGORY_ID
				+ ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.EVENTS_EVENTS_CATEGORIES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
				+ References.MODULES_ID + ", " + EventsColumns.EVENTS_ID
				+ " TEXT NOT NULL " + References.EVENTS_ID + ", "
				+ EventsCategoriesColumns.EVENTS_CATEGORY_ID
				+ " TEXT NOT NULL " + References.EVENTS_CATEGORY_ID + ")");
		
		db.execSQL("CREATE TABLE " + Tables.REGISTRATION_LOCATIONS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL " + References.MODULES_ID + ", " 
				+ RegistrationLocationsColumns.REGISTRATION_LOCATIONS_NAME + " TEXT NOT NULL, " 
				+ RegistrationLocationsColumns.REGISTRATION_LOCATIONS_CODE + " TEXT NOT NULL "
				+ ")");
		
		db.execSQL("CREATE TABLE " + Tables.REGISTRATION_LEVELS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ModulesColumns.MODULES_ID + " TEXT NOT NULL " + References.MODULES_ID + ", " 
				+ RegistrationLevelsColumns.REGISTRATION_LEVELS_NAME + " TEXT NOT NULL, " 
				+ RegistrationLevelsColumns.REGISTRATION_LEVELS_CODE + " TEXT NOT NULL "
				+ ")");
				
		createNewsSearch(db);
		createEventsSearch(db);

	}

	private static void createNewsSearch(SQLiteDatabase db) {
		db.execSQL("CREATE VIRTUAL TABLE " + Tables.NEWS_SEARCH
				+ " USING fts3(" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ NewsSearchColumns.BODY + " TEXT NOT NULL,"
				+ NewsSearchColumns.NEWS_ID + " TEXT NOT NULL "
				+ References.NEWS_ID + "," + "UNIQUE (" + NewsSearchColumns.NEWS_ID 
				+ ") ON CONFLICT REPLACE,"
				+ "tokenizer=porter)");
		db.execSQL("CREATE TRIGGER " + Triggers.NEWS_SEARCH_INSERT
				+ " AFTER INSERT ON " + Tables.NEWS + " BEGIN INSERT INTO "
				+ Qualified.NEWS_SEARCH + " " + " VALUES(new."
				+ News.NEWS_ENTRY_ID + ", " + Subquery.NEWS_BODY + ");"
				+ " END;");
		db.execSQL("CREATE TRIGGER " + Triggers.NEWS_SEARCH_DELETE
				+ " AFTER DELETE ON " + Tables.NEWS + " BEGIN DELETE FROM "
				+ Tables.NEWS_SEARCH + " " + " WHERE "
				+ Qualified.NEWS_SEARCH_NEWS_ID + "=old." + News.NEWS_ENTRY_ID
				+ ";" + " END;");
		db.execSQL("CREATE TRIGGER " + Triggers.NEWS_SEARCH_UPDATE
				+ " AFTER UPDATE ON " + Tables.NEWS + " BEGIN UPDATE "
				+ Tables.NEWS_SEARCH + " SET " + NewsSearchColumns.BODY + " = "
				+ Subquery.NEWS_BODY + " WHERE " + NewsColumns.NEWS_ENTRY_ID
				+ " = " + "old." + News.NEWS_ENTRY_ID + "; END;");
	}
	
	private static void createEventsSearch(SQLiteDatabase db) {
		db.execSQL("CREATE VIRTUAL TABLE " + Tables.EVENTS_SEARCH
				+ " USING fts3(" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ EventsSearchColumns.BODY + " TEXT NOT NULL,"
				+ EventsSearchColumns.EVENTS_ID + " TEXT NOT NULL "
				+ References.EVENTS_ID + "," + "UNIQUE (" + EventsSearchColumns.EVENTS_ID 
				+ ") ON CONFLICT REPLACE,"
				+ "tokenizer=porter)");
		db.execSQL("CREATE TRIGGER " + Triggers.EVENTS_SEARCH_INSERT
				+ " AFTER INSERT ON " + Tables.EVENTS + " BEGIN INSERT INTO "
				+ Qualified.EVENTS_SEARCH + " " + " VALUES(new."
				+ Events.EVENTS_ID + ", " + Subquery.EVENTS_BODY + ");"
				+ " END;");
		db.execSQL("CREATE TRIGGER " + Triggers.EVENTS_SEARCH_DELETE
				+ " AFTER DELETE ON " + Tables.EVENTS + " BEGIN DELETE FROM "
				+ Tables.EVENTS_SEARCH + " " + " WHERE "
				+ Qualified.EVENTS_SEARCH_EVENTS_ID + "=old." + Events.EVENTS_ID
				+ ";" + " END;");
		db.execSQL("CREATE TRIGGER " + Triggers.EVENTS_SEARCH_UPDATE
				+ " AFTER UPDATE ON " + Tables.EVENTS + " BEGIN UPDATE "
				+ Tables.EVENTS_SEARCH + " SET " + EventsSearchColumns.BODY + " = "
				+ Subquery.EVENTS_BODY + " WHERE " + EventsColumns.EVENTS_ID
				+ " = " + "old." + Events.EVENTS_ID + "; END;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		int version = oldVersion;
		Log.d(this.getClass().getSimpleName(), "onUpdate() from " + oldVersion
				+ " to " + newVersion);
		// here do anything that needs to be done to get the database scheme to
		// the current structure (alter columns, add tables).
		// Do not use break, fall through for each version.
		// Then set version to be the version of the scheme it upgraded to be
		switch (version) {
		case 1:
			db.execSQL("ALTER TABLE " + Tables.NUMBERS
					+ " ADD COLUMN "
					+ ModulesColumns.MODULES_ID + " TEXT " + References.MODULES_ID 
					);
			db.execSQL("ALTER TABLE " + Tables.NUMBERS_CATEGORIES
					+ " ADD COLUMN "
					+ ModulesColumns.MODULES_ID + " TEXT " + References.MODULES_ID 
					);
		case 2:
			db.execSQL("DROP TABLE IF EXISTS " + Tables.GRADE_COURSES);
			db.execSQL("CREATE TABLE " + Tables.GRADE_COURSES + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ GradeCoursesColumns.COURSE_ID + " TEXT NOT NULL,"
					+ GradeTermsColumns.TERM_ID + " TEXT NOT NULL "
					+ References.TERM_ID + ", " + GradeCoursesColumns.COURSE_ERP_ID
					+ " TEXT, " + GradeCoursesColumns.COURSE_TITLE + " TEXT, "
					+ GradeCoursesColumns.COURSE_DESCRIPTION + " TEXT, "
					+ GradeCoursesColumns.COURSE_CREDIT_HOURS + " TEXT, "
					+ GradeCoursesColumns.COURSE_SECTION + " TEXT,"
					+ "UNIQUE (" + GradeCoursesColumns.COURSE_ID
					+ ") ON CONFLICT REPLACE)");
		case 3:
			db.execSQL("ALTER TABLE " + Tables.MODULES
					+ " ADD COLUMN "
					+ ModulesColumns.MODULE_SECURE + " TEXT " 
					);
			db.execSQL("ALTER TABLE " + Tables.COURSE_COURSES
					+ " ADD COLUMN "
					+ CourseCoursesColumns.COURSE_LEARNING_PROVIDER + " TEXT "
					);
			db.execSQL("ALTER TABLE " + Tables.COURSE_COURSES
					+ " ADD COLUMN "
					+ CourseCoursesColumns.COURSE_LEARNING_PROVIDER_SITE_ID + " TEXT "
					);
			db.execSQL("CREATE TABLE " + Tables.COURSE_ASSIGNMENTS + " (" 
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL, "
					+ CourseAssignmentsColumns.ASSIGNMENT_NAME + " TEXT NOT NULL, "
					+ CourseAssignmentsColumns.ASSIGNMENT_DESCRIPTION + " TEXT, "
					+ CourseAssignmentsColumns.ASSIGNMENT_DUE + " TEXT, "
					+ CourseAssignmentsColumns.ASSIGNMENT_URL + " TEXT "
					+ ")");
			db.execSQL("CREATE TABLE " + Tables.COURSE_ANNOUNCEMENTS + " (" 
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL, "
					+ CourseAnnouncementsColumns.ANNOUNCEMENT_TITLE + " TEXT NOT NULL, "
					+ CourseAnnouncementsColumns.ANNOUNCEMENT_CONTENT + " TEXT, "
					+ CourseAnnouncementsColumns.ANNOUNCEMENT_DATE + " TEXT, "
					+ CourseAnnouncementsColumns.ANNOUNCEMENT_URL + " TEXT "
					+ ")");
			db.execSQL("CREATE TABLE " + Tables.COURSE_EVENTS + " (" 
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ CourseCoursesColumns.COURSE_ID + " TEXT NOT NULL, "
					+ CourseEventsColumns.EVENT_TITLE + " TEXT NOT NULL, "
					+ CourseEventsColumns.EVENT_DESCRIPTION + " TEXT, "
					+ CourseEventsColumns.EVENT_START + " TEXT, "
					+ CourseEventsColumns.EVENT_END + " TEXT, "
					+ CourseEventsColumns.EVENT_LOCATION + " TEXT, "
					+ CourseEventsColumns.EVENT_ALL_DAY + " TEXT "
					+ ")");
		case 4:
			db.execSQL("ALTER TABLE " + Tables.MODULES
					+ " ADD COLUMN "
					+ ModulesColumns.MODULE_SUB_TYPE + " TEXT " 
					);
		case 5:
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS					
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_SOURCE + " TEXT ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS	
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_DISPATCH_DATE + " TEXT ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_MOBILE_HEADLINE + " TEXT ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_EXPIRES + " TEXT ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_PUSH + " INTEGER NOT NULL DEFAULT 0 ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_MODULE + " INTEGER NOT NULL DEFAULT 0 ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_STICKY + " INTEGER NOT NULL DEFAULT 1 ");
			db.execSQL("ALTER TABLE " + Tables.NOTIFICATIONS
					+ " ADD COLUMN " + NotificationsColumns.NOTIFICATIONS_STATUSES + " TEXT ");
		case 6: //3.0
			db.execSQL("DROP TRIGGER " + Triggers.EVENTS_SEARCH_INSERT);
			db.execSQL("CREATE TRIGGER " + Triggers.EVENTS_SEARCH_INSERT
					+ " AFTER INSERT ON " + Tables.EVENTS + " BEGIN INSERT INTO "
					+ Qualified.EVENTS_SEARCH + " " + " VALUES(new."
					+ Events.EVENTS_ID + ", " + Subquery.EVENTS_BODY + ");"
					+ " END;");
			db.execSQL("CREATE TABLE " + Tables.MODULES_ROLES + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ModulesColumns.MODULES_ID + " TEXT NOT NULL "
					+ References.MODULES_ID + ", "
					+ ModulesRolesColumns.MODULE_ROLES_NAME + " TEXT NOT NULL " + ")");
		case 7: //3.5
			db.execSQL("ALTER TABLE " + Tables.COURSE_PATTERNS					
					+ " ADD COLUMN " + CoursePatternsColumns.PATTERN_INSTRUCTIONAL_METHOD + " TEXT ");
		case 8: //4.0
			db.execSQL("CREATE TABLE " + Tables.REGISTRATION_LOCATIONS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ModulesColumns.MODULES_ID + " TEXT NOT NULL " + References.MODULES_ID + ", " 
					+ RegistrationLocationsColumns.REGISTRATION_LOCATIONS_NAME + " TEXT NOT NULL, " 
					+ RegistrationLocationsColumns.REGISTRATION_LOCATIONS_CODE + " TEXT NOT NULL "
					+ ")");
			
			db.execSQL("CREATE TABLE " + Tables.REGISTRATION_LEVELS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ModulesColumns.MODULES_ID + " TEXT NOT NULL " + References.MODULES_ID + ", " 
					+ RegistrationLevelsColumns.REGISTRATION_LEVELS_NAME + " TEXT NOT NULL, " 
					+ RegistrationLevelsColumns.REGISTRATION_LEVELS_CODE + " TEXT NOT NULL "
					+ ")");
		}

		Log.d(this.getClass().getSimpleName(),
				"after upgrade logic, at version " + version);
		if (version != DB_VERSION) {

		}

	}

	public static void deleteDatabase(Context context) {
		context.deleteDatabase(DB_NAME);
	}

	interface NewsSearchColumns {
		String NEWS_ID = "news_id";
		String BODY = "body";
	}
	
	interface EventsSearchColumns {
		String EVENTS_ID = "events_id";
		String BODY = "body";
	}

	/** Fully-qualified field names. */
	private interface Qualified {

		String NEWS_SEARCH = Tables.NEWS_SEARCH + "("
				+ NewsSearchColumns.NEWS_ID + "," + NewsSearchColumns.BODY
				+ ")";
		String NEWS_SEARCH_NEWS_ID = Tables.NEWS_SEARCH + "." + NewsSearchColumns.NEWS_ID;

		String EVENTS_SEARCH = Tables.EVENTS_SEARCH + "("
				+ EventsSearchColumns.EVENTS_ID + "," + EventsSearchColumns.BODY
				+ ")";
		String EVENTS_SEARCH_EVENTS_ID = Tables.EVENTS_SEARCH + "." + EventsSearchColumns.EVENTS_ID;

	}
}
