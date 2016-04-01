/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;
import com.ellucian.mobile.android.provider.EllucianContract.CourseInstructors;
import com.ellucian.mobile.android.provider.EllucianContract.CourseMeetings;
import com.ellucian.mobile.android.provider.EllucianContract.CoursePatterns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseRoster;
import com.ellucian.mobile.android.provider.EllucianContract.CourseTerms;
import com.ellucian.mobile.android.provider.EllucianContract.Events;
import com.ellucian.mobile.android.provider.EllucianContract.EventsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.EventsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.EventsEventsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.GradeCoursesColumns;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTerms;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTermsColumns;
import com.ellucian.mobile.android.provider.EllucianContract.Grades;
import com.ellucian.mobile.android.provider.EllucianContract.GradesCourses;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsBuildingsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesProperties;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesRoles;
import com.ellucian.mobile.android.provider.EllucianContract.News;
import com.ellucian.mobile.android.provider.EllucianContract.NewsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.provider.EllucianContract.Numbers;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersCategories;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLevels;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLocations;
import com.ellucian.mobile.android.provider.EllucianDatabase.EventsSearchColumns;
import com.ellucian.mobile.android.provider.EllucianDatabase.NewsSearchColumns;
import com.ellucian.mobile.android.provider.EllucianDatabase.Tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class EllucianProvider extends ContentProvider {

	private EllucianDatabase mOpenHelper;
	/** Stores list of batch notifications to fire once batch operations are complete */
	private final HashMap<String, Uri> batchChangeNotifications = new HashMap<String, Uri>();
	/** internal state on whether the provider is in batch mode */
	private	boolean isBatch;
	
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	
	private static final int MODULES = 100;
	private static final int MODULESPROPERTIES = 110;
	private static final int MODULESPROPERTIES_ID = 111;
	private static final int MODULESROLES = 120;
	private static final int MODULESROLES_ID = 121;
	private static final int GRADETERMS = 200;
	private static final int GRADETERMS_ID = 201;
	private static final int GRADETERMS_ID_GRADECOURSES = 202;
	private static final int GRADECOURSES = 300;
	private static final int GRADECOURSES_ID = 301;
	private static final int GRADECOURSES_ID_GRADES = 302;
	private static final int GRADES = 400;
	private static final int GRADES_ID = 401;
	private static final int MAPSCAMPUSES = 500;
	private static final int MAPSCAMPUSES_ID = 501;
	private static final int MAPSCAMPUSES_ID_BUILDINGS = 502;
	private static final int MAPS_BUILDINGS = 600;
	private static final int MAPS_BUILDINGS_ID = 601;
	private static final int MAPS_BUILDINGS_CATEGORIES = 602;
	private static final int MAPS_BUILDINGS_CATEGORIES_NAME = 603;
	private static final int MAPS_BUILDINGS_BUILDINGSCATEGORIES = 604;
	private static final int MAPS_BUILDINGS_BUILDINGSCATEGORIES_ID = 605;
	private static final int NOTIFICATIONS = 700;
	private static final int NOTIFICATIONS_ID = 701;
	private static final int NUMBERS = 800;
	private static final int NUMBERS_ID = 801;
	private static final int NUMBERSCATEGORIES = 900;
	private static final int NUMBERSCATEGORIES_ID = 901;
	private static final int NEWS = 1000;
	private static final int NEWS_ID = 1001;
	private static final int NEWS_SEARCH = 1002;
	private static final int COURSETERMS = 1100;
	private static final int COURSETERMS_ID = 1101;
	private static final int COURSECOURSES = 1200;
	private static final int COURSECOURSES_ID = 1201;
	private static final int COURSEINSTRUCTORS = 1300;
	private static final int COURSEINSTRUCTORS_ID = 1301;
	private static final int COURSEPATTERNS = 1400;
	private static final int COURSEPATTERNS_ID = 1401;
	private static final int COURSEMEETINGS = 1500;
	private static final int COURSEMEETINGS_ID = 1501;
	private static final int COURSEROSTER = 1600;
	private static final int COURSEROSTER_ID = 1601;
	private static final int EVENTS = 1700;
	private static final int EVENTS_ID = 1701;
	private static final int EVENTS_SEARCH = 1703;
	private static final int EVENTSCATEGORIES = 1800;
	private static final int EVENTSCATEGORIES_ID = 1801;
	private static final int EVENTS_EVENTSCATEGORIES = 1802;
	private static final int EVENTS_EVENTSCATEGORIES_ID = 1803;
	private static final int NEWSCATEGORIES = 1900;
	private static final int NEWSCATEGORIES_ID = 1901;
	private static final int COURSEASSIGNMENTS = 2000;
	private static final int COURSEASSIGNMENTS_ID = 2001;
	private static final int COURSEANNOUNCEMENTS = 2100;
	private static final int COURSEANNOUNCEMENTS_ID = 2101;
	private static final int COURSEEVENTS = 2200;
	private static final int COURSEEVENTS_ID = 2201;
	private static final int REGISTRATIONLOCATIONS = 2300;
	private static final int REGISTRATIONLOCATIONS_ID = 2301;
	private static final int REGISTRATIONLEVELS = 2302;
	private static final int REGISTRATIONLEVELS_ID = 2303;
    private static final int DIRECTORIES = 2400;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = EllucianContract.CONTENT_AUTHORITY;
		matcher.addURI(authority, EllucianContract.PATH_MODULES, MODULES);
		matcher.addURI(authority, EllucianContract.PATH_MODULESPROPERTIES, MODULESPROPERTIES);
		matcher.addURI(authority, EllucianContract.PATH_MODULESPROPERTIES+"/*", MODULESPROPERTIES_ID);
		matcher.addURI(authority, EllucianContract.PATH_MODULESROLES, MODULESROLES);
		matcher.addURI(authority, EllucianContract.PATH_MODULESROLES+"/*", MODULESROLES_ID);
		matcher.addURI(authority, EllucianContract.PATH_GRADETERMS, GRADETERMS);
		matcher.addURI(authority, EllucianContract.PATH_GRADETERMS+"/*", GRADETERMS_ID);
		matcher.addURI(authority, 
				EllucianContract.PATH_GRADETERMS+"/*/"+EllucianContract.PATH_GRADETERMS_COURSES, 
				GRADETERMS_ID_GRADECOURSES);
		matcher.addURI(authority, EllucianContract.PATH_GRADECOURSES, GRADECOURSES);
		matcher.addURI(authority, EllucianContract.PATH_GRADECOURSES+"/*", GRADECOURSES_ID);
		matcher.addURI(authority, 
				EllucianContract.PATH_GRADECOURSES+"/*/"+EllucianContract.PATH_GRADESCOURSES_GRADES, 
				GRADECOURSES_ID_GRADES);
		matcher.addURI(authority, EllucianContract.PATH_GRADES, GRADES);
		matcher.addURI(authority, EllucianContract.PATH_GRADES+"/*", GRADES_ID);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_CAMPUSES, MAPSCAMPUSES);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_CAMPUSES+"/*", MAPSCAMPUSES_ID);
		matcher.addURI(authority, 
				EllucianContract.PATH_MAPS_CAMPUSES+"/*/"+EllucianContract.PATH_MAPS_CAMPUSES_BUILDINGS, 
				MAPSCAMPUSES_ID_BUILDINGS);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_BUILDINGS, MAPS_BUILDINGS);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_BUILDINGS + "/*", MAPS_BUILDINGS_ID);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_BUILDINGS_CATEGORIES, MAPS_BUILDINGS_CATEGORIES);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_BUILDINGS_CATEGORIES+"/*", MAPS_BUILDINGS_CATEGORIES_NAME);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_BUILDINGS_BUILDINGSCATEGORIES, MAPS_BUILDINGS_BUILDINGSCATEGORIES);
		matcher.addURI(authority, EllucianContract.PATH_MAPS_BUILDINGS_BUILDINGSCATEGORIES+"/*", MAPS_BUILDINGS_BUILDINGSCATEGORIES_ID);
		matcher.addURI(authority, EllucianContract.PATH_NOTIFICATIONS, NOTIFICATIONS);
		matcher.addURI(authority, EllucianContract.PATH_NOTIFICATIONS+"/*", NOTIFICATIONS_ID);	
		matcher.addURI(authority, EllucianContract.PATH_NUMBERS, NUMBERS);
		matcher.addURI(authority, EllucianContract.PATH_NUMBERS+"/*", NUMBERS_ID);
		matcher.addURI(authority, EllucianContract.PATH_NUMBERS_CATEGORIES, NUMBERSCATEGORIES);
		matcher.addURI(authority, EllucianContract.PATH_NUMBERS_CATEGORIES+"/*", NUMBERSCATEGORIES_ID);
		matcher.addURI(authority, EllucianContract.PATH_NEWS, NEWS);
		matcher.addURI(authority, EllucianContract.PATH_NEWS+"/"+EllucianContract.PATH_SEARCH+"/*", NEWS_SEARCH);
		matcher.addURI(authority, EllucianContract.PATH_NEWS+"/*", NEWS_ID);
		matcher.addURI(authority, EllucianContract.PATH_NEWS_CATEGORIES, NEWSCATEGORIES);
		matcher.addURI(authority, EllucianContract.PATH_NEWS_CATEGORIES+"/*", NEWSCATEGORIES_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSETERMS, COURSETERMS);
		matcher.addURI(authority, EllucianContract.PATH_COURSETERMS+"/*", COURSETERMS_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSECOURSES, COURSECOURSES);
		matcher.addURI(authority, EllucianContract.PATH_COURSECOURSES+"/*", COURSECOURSES_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEINSTRUCTORS, COURSEINSTRUCTORS);
		matcher.addURI(authority, EllucianContract.PATH_COURSEINSTRUCTORS+"/*", COURSEINSTRUCTORS_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEPATTERNS, COURSEPATTERNS);
		matcher.addURI(authority, EllucianContract.PATH_COURSEPATTERNS+"/*", COURSEPATTERNS_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEMEETINGS, COURSEMEETINGS);
		matcher.addURI(authority, EllucianContract.PATH_COURSEMEETINGS+"/*", COURSEMEETINGS_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEROSTER, COURSEROSTER);
		matcher.addURI(authority, EllucianContract.PATH_COURSEROSTER+"/*", COURSEROSTER_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEASSIGNMENTS, COURSEASSIGNMENTS);
		matcher.addURI(authority, EllucianContract.PATH_COURSEASSIGNMENTS+"/*", COURSEASSIGNMENTS_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEANNOUNCEMENTS, COURSEANNOUNCEMENTS);
		matcher.addURI(authority, EllucianContract.PATH_COURSEANNOUNCEMENTS+"/*", COURSEANNOUNCEMENTS_ID);
		matcher.addURI(authority, EllucianContract.PATH_COURSEEVENTS, COURSEEVENTS);
		matcher.addURI(authority, EllucianContract.PATH_COURSEEVENTS+"/*", COURSEEVENTS_ID);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS, EVENTS);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS+"/"+EllucianContract.PATH_SEARCH+"/*", EVENTS_SEARCH);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS+"/*", EVENTS_ID);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS_CATEGORIES, EVENTSCATEGORIES);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS_CATEGORIES+"/*", EVENTSCATEGORIES_ID);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS_EVENTSCATEGORIES, EVENTS_EVENTSCATEGORIES);
		matcher.addURI(authority, EllucianContract.PATH_EVENTS_EVENTSCATEGORIES+"/*", EVENTS_EVENTSCATEGORIES_ID);
		matcher.addURI(authority, EllucianContract.PATH_REGISTRATION_LOCATIONS, REGISTRATIONLOCATIONS);
		matcher.addURI(authority, EllucianContract.PATH_REGISTRATION_LOCATIONS+"/*", REGISTRATIONLOCATIONS_ID);
		matcher.addURI(authority, EllucianContract.PATH_REGISTRATION_LEVELS, REGISTRATIONLEVELS);
		matcher.addURI(authority, EllucianContract.PATH_REGISTRATION_LEVELS+"/*", REGISTRATIONLEVELS_ID);
        matcher.addURI(authority, EllucianContract.PATH_DIRECTORIES, DIRECTORIES);
		
		return matcher;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.v(this.getClass().getSimpleName(), "delete(uri=" + uri + ")");
	
		if(uri.equals( EllucianContract.BASE_CONTENT_URI) ) {
			deleteDatabase();
			notifyChange(uri);
			return 1;
		}
		if(uri.equals( EllucianContract.SECURED_CONTENT_URI )) {
			deleteSecuredRecordsFromDatabase();
			notifyChange(uri);
			return 1;
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).delete(db);
		notifyChange(uri);
		return retVal;
	}

	private void deleteDatabase() {
		mOpenHelper.close();
		Context context = getContext();
		EllucianDatabase.deleteDatabase(context);
		mOpenHelper = new EllucianDatabase(getContext());
	}
	
	/**
	 * This method should delete any of the records that should be deleted when
	 * the secured data is removed from the database, such as a user signing
	 * out.
	 */
	private void deleteSecuredRecordsFromDatabase() {
		delete(Grades.CONTENT_URI);
		delete(GradesCourses.CONTENT_URI);
		delete(GradeTerms.CONTENT_URI);
		delete(CourseCourses.CONTENT_URI);
		delete(CourseInstructors.CONTENT_URI);
		delete(CourseMeetings.CONTENT_URI);
		delete(Notifications.CONTENT_URI);
	}

	private void delete(Uri contentUri) {
		delete(contentUri, null, null);
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch(match) {
			case MODULES:
				return Modules.CONTENT_TYPE;
			case MODULESPROPERTIES:
				return ModulesProperties.CONTENT_TYPE;
			case MODULESPROPERTIES_ID:
				return ModulesProperties.CONTENT_ITEM_TYPE;
			case MODULESROLES:
				return ModulesRoles.CONTENT_TYPE;
			case MODULESROLES_ID:
				return ModulesRoles.CONTENT_ITEM_TYPE;
			case GRADETERMS:
				return GradeTerms.CONTENT_TYPE;	
			case GRADETERMS_ID:
				return GradeTerms.CONTENT_ITEM_TYPE;
			case GRADETERMS_ID_GRADECOURSES:
				return GradesCourses.CONTENT_TYPE;
			case GRADECOURSES:
				return GradesCourses.CONTENT_TYPE;
			case GRADECOURSES_ID:
				return GradesCourses.CONTENT_ITEM_TYPE;
			case GRADECOURSES_ID_GRADES:
				return Grades.CONTENT_TYPE;
			case GRADES:
				return Grades.CONTENT_TYPE;
			case GRADES_ID:
				return Grades.CONTENT_ITEM_TYPE;
			case MAPSCAMPUSES:
				return MapsCampuses.CONTENT_TYPE;
			case MAPSCAMPUSES_ID:
				return MapsCampuses.CONTENT_ITEM_TYPE;
			case MAPSCAMPUSES_ID_BUILDINGS:
			case MAPS_BUILDINGS:
				return MapsBuildings.CONTENT_TYPE;
			case MAPS_BUILDINGS_ID:
				return MapsBuildings.CONTENT_ITEM_TYPE;
			case MAPS_BUILDINGS_CATEGORIES_NAME:
				return MapsBuildingsCategories.CONTENT_ITEM_TYPE;
			case MAPS_BUILDINGS_BUILDINGSCATEGORIES:
				return MapsBuildingsBuildingsCategories.CONTENT_TYPE;
			case MAPS_BUILDINGS_BUILDINGSCATEGORIES_ID:
				return MapsBuildingsBuildingsCategories.CONTENT_ITEM_TYPE;
			case NOTIFICATIONS:
				return Notifications.CONTENT_TYPE;
			case NOTIFICATIONS_ID:
				return Notifications.CONTENT_ITEM_TYPE;
			case NUMBERS:
				return Numbers.CONTENT_TYPE;
			case NUMBERS_ID:
				return Numbers.CONTENT_ITEM_TYPE;
			case NUMBERSCATEGORIES:
				return NumbersCategories.CONTENT_TYPE;
			case NUMBERSCATEGORIES_ID:
				return NumbersCategories.CONTENT_ITEM_TYPE;
			case NEWS:
				return News.CONTENT_TYPE;
			case NEWS_ID:
				return News.CONTENT_ITEM_TYPE;
			case NEWS_SEARCH:
				return News.CONTENT_TYPE;
			case NEWSCATEGORIES:
				return NewsCategories.CONTENT_TYPE;
			case NEWSCATEGORIES_ID:
				return NewsCategories.CONTENT_ITEM_TYPE;
			case COURSETERMS:
				return CourseTerms.CONTENT_TYPE;
			case COURSETERMS_ID:
				return CourseTerms.CONTENT_ITEM_TYPE;
			case COURSECOURSES:
				return CourseCourses.CONTENT_TYPE;
			case COURSECOURSES_ID:
				return CourseCourses.CONTENT_ITEM_TYPE;
			case COURSEINSTRUCTORS:
				return CourseInstructors.CONTENT_TYPE;
			case COURSEINSTRUCTORS_ID:
				return CourseInstructors.CONTENT_ITEM_TYPE;
			case COURSEPATTERNS:
				return CoursePatterns.CONTENT_TYPE;
			case COURSEPATTERNS_ID:
				return CoursePatterns.CONTENT_ITEM_TYPE;
			case COURSEMEETINGS:
				return CourseMeetings.CONTENT_TYPE;
			case COURSEMEETINGS_ID:
				return CourseMeetings.CONTENT_ITEM_TYPE;
			case COURSEROSTER:
				return CourseRoster.CONTENT_TYPE;
			case COURSEROSTER_ID:
				return CourseRoster.CONTENT_ITEM_TYPE;
			case COURSEASSIGNMENTS:
				return CourseAssignments.CONTENT_TYPE;
			case COURSEASSIGNMENTS_ID:
				return CourseAssignments.CONTENT_ITEM_TYPE;
			case COURSEANNOUNCEMENTS:
				return CourseAnnouncements.CONTENT_TYPE;
			case COURSEANNOUNCEMENTS_ID:
				return CourseAnnouncements.CONTENT_ITEM_TYPE;
			case COURSEEVENTS:
				return CourseEvents.CONTENT_TYPE;
			case COURSEEVENTS_ID:
				return CourseEvents.CONTENT_ITEM_TYPE;
			case EVENTS:
				return Events.CONTENT_TYPE;
			case EVENTS_ID:
				return Events.CONTENT_ITEM_TYPE;
			case EVENTS_SEARCH:
				return Events.CONTENT_TYPE;
			case EVENTSCATEGORIES:
				return EventsCategories.CONTENT_TYPE;
			case EVENTSCATEGORIES_ID:
				return EventsCategories.CONTENT_ITEM_TYPE;
			case EVENTS_EVENTSCATEGORIES:
				return EventsEventsCategories.CONTENT_TYPE;
			case EVENTS_EVENTSCATEGORIES_ID:
				return EventsEventsCategories.CONTENT_ITEM_TYPE;
			case REGISTRATIONLOCATIONS:
				return RegistrationLocations.CONTENT_TYPE;
			case REGISTRATIONLOCATIONS_ID:
				return RegistrationLocations.CONTENT_ITEM_TYPE;
			case REGISTRATIONLEVELS:
				return RegistrationLevels.CONTENT_TYPE;
			case REGISTRATIONLEVELS_ID:
				return RegistrationLevels.CONTENT_ITEM_TYPE;
            case DIRECTORIES:
                return EllucianContract.Directories.CONTENT_ITEM_TYPE;
				
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.v(this.getClass().getSimpleName(), "insert(uri=" + uri + ", values=" + values.toString() + ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		switch(match) {
			case MODULES:
				db.insertOrThrow(Tables.MODULES, null, values);
				notifyChange(uri);
				return Modules.buildModuleUri(values.getAsString(Modules.MODULES_ID));
			case MODULESPROPERTIES: {
				long id = db.insertOrThrow(Tables.MODULES_PROPERTIES, null, values);
				notifyChange(uri);
				return ModulesProperties.buildPropertyUri(values.getAsString(Long.toString(id)));
			}
			case MODULESROLES: {
				long id = db.insertOrThrow(Tables.MODULES_ROLES, null, values);
				notifyChange(uri);
				return ModulesRoles.buildRoleUri(values.getAsString(Long.toString(id)));
			}
			case GRADETERMS:
				db.insertOrThrow(Tables.GRADE_TERMS, null, values);
				notifyChange(uri);
				return GradeTerms.buildTermUri(values.getAsString(GradeTerms.TERM_ID));
			case GRADETERMS_ID_GRADECOURSES:
			case GRADECOURSES: {
				db.insertOrThrow(Tables.GRADE_COURSES, null, values);
				notifyChange(uri);
				return GradesCourses.buildCourseUri(values.getAsString(GradesCourses.COURSE_ID));
			}
			case GRADECOURSES_ID_GRADES:
			case GRADES: {
				long id = db.insertOrThrow(Tables.GRADES, null, values);
				notifyChange(uri);
				return Grades.buildGradeUri(Long.toString(id));
			}
			case MAPSCAMPUSES:
				db.insertOrThrow(Tables.MAPS_CAMPUSES, null, values);
				notifyChange(uri);
				return MapsCampuses.buildCampusUri(values.getAsString(MapsCampuses.CAMPUS_ID));
			case MAPSCAMPUSES_ID_BUILDINGS:
			case MAPS_BUILDINGS: {
				long id = db.insertOrThrow(Tables.MAPS_BUILDINGS, null, values);
				notifyChange(uri);
				return MapsBuildings.buildBuildingUri(Long.toString(id));	
			}
			case MAPS_BUILDINGS_CATEGORIES: {
				db.insertOrThrow(Tables.MAPS_BUILDINGS_CATEGORIES,null, values);
				notifyChange(uri);
				return MapsBuildingsCategories.buildCategoryUri(values.getAsString(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME));
			}
			case MAPS_BUILDINGS_BUILDINGSCATEGORIES: {
				long id = db.insertOrThrow(Tables.MAPS_BUILDINGS_BUILDINGSCATEGORIES, null, values);
				notifyChange(uri);
				return MapsBuildingsBuildingsCategories.buildUri(Long.toString(id));
			}	
			case NOTIFICATIONS: {
				db.insertOrThrow(Tables.NOTIFICATIONS, null, values);
				notifyChange(uri);
				return Notifications.buildNotificationsUri(values.getAsString(Notifications.NOTIFICATIONS_ID));
			}
			case NUMBERS: {
				
				long id = db.insertOrThrow(Tables.NUMBERS, null, values);
//				if(!values.containsKey(NumbersCategories.NUMBERS_CATEGORY_ID)) {
//					db.execSQL("UPDATE " + Tables.NUMBERS + " SET " + NumbersCategories.NUMBERS_CATEGORY_ID + "=(SELECT seq from sqlite_sequence where name = '"+ Tables.NUMBERS_CATEGORIES + "')");
//				}
				notifyChange(uri);
				return Numbers.buildNumberUri(Long.toString(id));
			}
			case NUMBERSCATEGORIES: {
				long id = db.insertOrThrow(Tables.NUMBERS_CATEGORIES, null, values);
				notifyChange(uri);
				return NumbersCategories.buildCategoryUri(values.getAsString(Long.toString(id)));
			}
			case NEWS: {
				long id = db.insertOrThrow(Tables.NEWS, null, values);
				notifyChange(uri);
				return News.buildNewsUri(Long.toString(id));
			}
			case NEWSCATEGORIES: {
				db.insertOrThrow(Tables.NEWS_CATEGORIES,null, values);
				notifyChange(uri);
				return NewsCategories.buildCategoryUri(values.getAsString(NewsCategories.NEWS_CATEGORY_ID));
			}
			case COURSETERMS:
				db.insertOrThrow(Tables.COURSE_TERMS, null, values);
				notifyChange(uri);
				return CourseTerms.buildTermUri(values.getAsString(CourseTerms.TERM_ID));
			case COURSECOURSES:
				db.insertOrThrow(Tables.COURSE_COURSES, null, values);
				notifyChange(uri);
				return CourseCourses.buildCourseUri(values.getAsString(CourseCourses.COURSE_ID));	
			case COURSEINSTRUCTORS: {
				long id = db.insertOrThrow(Tables.COURSE_INSTRUCTORS, null, values);
				notifyChange(uri);
				return CourseInstructors.buildInstructorUri(Long.toString(id));
			}
			case COURSEPATTERNS: {
				long id = db.insertOrThrow(Tables.COURSE_PATTERNS, null, values);
				notifyChange(uri);
				return CoursePatterns.buildPatternUri(Long.toString(id));
			}
			case COURSEMEETINGS: {
				long id = db.insertOrThrow(Tables.COURSE_MEETINGS, null, values);
				notifyChange(uri);
				return CourseMeetings.buildMeetingUri(Long.toString(id));
			}
			case COURSEROSTER: {
				long id = db.insertOrThrow(Tables.COURSE_ROSTER, null, values);
				notifyChange(uri);
				return CourseRoster.buildRosterUri(Long.toString(id));
			}
			case COURSEASSIGNMENTS: {
				long id = db.insertOrThrow(Tables.COURSE_ASSIGNMENTS, null, values);
				notifyChange(uri);
				return CourseAssignments.buildAssignmentUri(Long.toString(id));
			}
			case COURSEANNOUNCEMENTS: {
				long id = db.insertOrThrow(Tables.COURSE_ANNOUNCEMENTS, null, values);
				notifyChange(uri);
				return CourseAnnouncements.buildAnnouncementUri(Long.toString(id));
			}
			case COURSEEVENTS: {
				long id = db.insertOrThrow(Tables.COURSE_EVENTS, null, values);
				notifyChange(uri);
				return CourseEvents.buildEventUri(Long.toString(id));
			}
			case EVENTS: {				
				db.insertOrThrow(Tables.EVENTS, null, values);
				notifyChange(uri);
				return Events.buildEventsUri(values.getAsString(Events.EVENTS_ID));
			}
			case EVENTSCATEGORIES: {
				db.insertOrThrow(Tables.EVENTS_CATEGORIES,null, values);
				notifyChange(uri);
				return EventsCategories.buildCategoryUri(values.getAsString(EventsCategories.EVENTS_CATEGORY_ID));
			}
			case EVENTS_EVENTSCATEGORIES: {
				long id = db.insertOrThrow(Tables.EVENTS_EVENTS_CATEGORIES, null, values);
				notifyChange(uri);
				return EventsEventsCategories.buildUri(Long.toString(id));
			}
			case REGISTRATIONLOCATIONS: {
				long id = db.insertOrThrow(Tables.REGISTRATION_LOCATIONS, null, values);
				notifyChange(uri);
				return RegistrationLocations.buildUri(Long.toString(id));
			}
			case REGISTRATIONLEVELS: {
				long id = db.insertOrThrow(Tables.REGISTRATION_LEVELS, null, values);
				notifyChange(uri);
				return RegistrationLevels.buildUri(Long.toString(id));
			}
            case DIRECTORIES: {
                long id = db.insertOrThrow(Tables.DIRECTORIES, null, values);
                notifyChange(uri);
                return EllucianContract.Directories.buildUri(Long.toString(id));
            }
			default:
				throw new UnsupportedOperationException("Unknown uri: "+ uri);
		}
		
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new EllucianDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		Log.v(this.getClass().getSimpleName(), "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		
		final int match = sUriMatcher.match(uri);
		switch(match) {
			case EVENTS: {
				final SelectionBuilder builder = buildExpandedSelection(uri, match);
				cursor = builder.where(selection,  selectionArgs).query(db, true, projection, null, null, sortOrder, null);
				break;
			}
			default: {
				final SelectionBuilder builder = buildExpandedSelection(uri, match);
				cursor = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
			}
		}
		if(cursor != null) {
			Log.v(this.getClass().getSimpleName(), "Returning " + cursor.getCount() + " rows. Setting cursor notification change uri for: " + uri);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.v(this.getClass().getSimpleName(), "update(uri=" + uri + ", values=" + values.toString() + ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).update(db, values);
		notifyChange(uri);
		return retVal;
	}
	
	public synchronized ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		boolean success = false;
		db.beginTransaction();
		try {
			isBatch = true;
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
				Log.v(this.getClass().getSimpleName(), "ContentProviderResult["+i+"] = " + results[i]);
			}
			db.setTransactionSuccessful();
			success = true;
			return results;
		} finally {
			db.endTransaction();
			isBatch = false;
			if(success) {
			Log.v(this.getClass().getSimpleName(), "Firing batch notifications");
				for (Uri uri : batchChangeNotifications.values()) {
					getContext().getContentResolver().notifyChange(uri, null);
					Log.v(this.getClass().getSimpleName(), "Firing batch notification: " + uri);
				}
			}
			batchChangeNotifications.clear();
		}
	}
	
	private SelectionBuilder buildSimpleSelection(Uri uri) {
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch(match) {
			case MODULES: 
				return builder.table(Tables.MODULES);
			case MODULESPROPERTIES:
				return builder.table(Tables.MODULES_PROPERTIES);
			case MODULESPROPERTIES_ID: {
				final String id = ModulesProperties.getPropertyId(uri);
				return builder.table(Tables.MODULES_PROPERTIES)
						.where(ModulesProperties._ID + "=?", id);
			}
			case MODULESROLES:
				return builder.table(Tables.MODULES_ROLES);
			case MODULESROLES_ID: {
				final String id = ModulesRoles.getRoleId(uri);
				return builder.table(Tables.MODULES_ROLES)
						.where(ModulesRoles._ID + "=?", id);
			}
			case GRADETERMS:
				return builder.table(Tables.GRADE_TERMS);
			case GRADETERMS_ID: {
				final String termId = GradeTerms.getTermId(uri);
				return builder.table(Tables.GRADE_TERMS)
						.where(GradeTerms.TERM_ID + "=?", termId);
			}
			case GRADETERMS_ID_GRADECOURSES: {
				final String termId = GradeTerms.getTermId(uri);
				return builder.table(Tables.GRADE_COURSES)
						.where(GradeTerms.TERM_ID + "=?", termId);
			}
			case GRADECOURSES:
				return builder.table(Tables.GRADE_COURSES);
			case GRADECOURSES_ID: {
				final String courseId = GradesCourses.getCourseId(uri);
				return builder.table(Tables.GRADE_COURSES)
						.where(GradesCourses.COURSE_ID + "=?", courseId);
			}
			case GRADES:
				return builder.table(Tables.GRADES);
			case GRADES_ID: {
				final String gradeId = Grades.getGradeId(uri);
				return builder.table(Tables.GRADES)
						.where(Grades._ID + "=?", gradeId);
			}
			case GRADECOURSES_ID_GRADES: {
				final String courseId = GradesCourses.getCourseId(uri);
				return builder.table(Tables.GRADES)
						.where(GradesCourses.COURSE_ID + "=?", courseId);
			}
			case MAPSCAMPUSES:
				return builder.table(Tables.MAPS_CAMPUSES);
			case MAPSCAMPUSES_ID: {
				final String campusId = MapsCampuses.getCampusId(uri);
				return builder.table(Tables.MAPS_CAMPUSES)
						.where(MapsCampuses.CAMPUS_ID + "=?", campusId);
			}
			case MAPSCAMPUSES_ID_BUILDINGS: {
				final String campusId = MapsCampuses.getCampusId(uri);
				return builder.table(Tables.MAPS_BUILDINGS)
						.where(MapsCampuses.CAMPUS_ID + "=?", campusId);
			}
			case MAPS_BUILDINGS:
				return builder.table(Tables.MAPS_BUILDINGS);
			case MAPS_BUILDINGS_ID: {
					final String buildingId = MapsBuildings.getBuildingId(uri);
					return builder.table(Tables.MAPS_BUILDINGS)
							.where(MapsBuildings._ID + "=?", buildingId);
			}
			case MAPS_BUILDINGS_CATEGORIES:
				return builder.table(Tables.MAPS_BUILDINGS_CATEGORIES);
			case MAPS_BUILDINGS_CATEGORIES_NAME: {
					final String categoryId = MapsBuildingsCategories.getCategoryId(uri);
					return builder.table(Tables.MAPS_BUILDINGS_CATEGORIES)
							.where(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME + "=?", categoryId);
			}
			case MAPS_BUILDINGS_BUILDINGSCATEGORIES:
				return builder.table(Tables.MAPS_BUILDINGS_BUILDINGSCATEGORIES);
			case MAPS_BUILDINGS_BUILDINGSCATEGORIES_ID: {
					final String id = MapsBuildingsBuildingsCategories.getId(uri);
					return builder.table(Tables.MAPS_BUILDINGS_BUILDINGSCATEGORIES)
							.where(MapsBuildingsBuildingsCategories._ID + "=?", id);
			}
			case NOTIFICATIONS:
				return builder.table(Tables.NOTIFICATIONS);
			case NOTIFICATIONS_ID: {
					final String notificationsId = Notifications.getNotificationsId(uri);
					return builder.table(Tables.NOTIFICATIONS)
							.where(Notifications.NOTIFICATIONS_ID + "=?", notificationsId);
			}
			case NUMBERS:
				return builder.table(Tables.NUMBERS);
			case NUMBERS_ID: {
					final String numbersId = Numbers.getNumberId(uri);
					return builder.table(Tables.NUMBERS)
							.where(Numbers._ID + "=?", numbersId);
			}
			case NUMBERSCATEGORIES:
				return builder.table(Tables.NUMBERS_CATEGORIES);
			case NUMBERSCATEGORIES_ID: {
					final String categoryId = NumbersCategories.getCategoryId(uri);
					return builder.table(Tables.NUMBERS_CATEGORIES)
							.where(NumbersCategories._ID + "=?", categoryId);
			}
			case NEWS:
				return builder.table(Tables.NEWS);
			case NEWS_ID: {
					final String newsId = News.getNewsId(uri);
					return builder.table(Tables.NEWS)
							.where(News._ID + "=?", newsId);
			}
			case NEWSCATEGORIES:
				return builder.table(Tables.NEWS_CATEGORIES);
			case NEWSCATEGORIES_ID: {
					final String categoryId = NewsCategories.getCategoryId(uri);
					return builder.table(Tables.NEWS_CATEGORIES)
							.where(NewsCategories.NEWS_CATEGORY_ID + "=?", categoryId);
			}
			case COURSETERMS:
				return builder.table(Tables.COURSE_TERMS);
			case COURSETERMS_ID: {
				final String termId = CourseTerms.getTermId(uri);
				return builder.table(Tables.COURSE_TERMS)
						.where(CourseTerms.TERM_ID + "=?", termId);
			}
			case COURSECOURSES:
				return builder.table(Tables.COURSE_COURSES);
			case COURSECOURSES_ID: {
				final String courseId = CourseCourses.getCourseId(uri);
				return builder.table(Tables.COURSE_COURSES)
						.where(CourseCourses.COURSE_ID + "=?", courseId);
			}
			case COURSEINSTRUCTORS:
				return builder.table(Tables.COURSE_INSTRUCTORS);
			case COURSEINSTRUCTORS_ID: {
				final String instructorId = CourseInstructors.getInstructorId(uri);
				return builder.table(Tables.COURSE_INSTRUCTORS)
						.where(CourseInstructors._ID + "=?", instructorId);
			}
			case COURSEPATTERNS:
				return builder.table(Tables.COURSE_PATTERNS);
			case COURSEPATTERNS_ID: {
				final String patternId = CoursePatterns.getPatternId(uri);
				return builder.table(Tables.COURSE_PATTERNS)
						.where(CoursePatterns._ID + "=?", patternId);
			}
			case COURSEMEETINGS:
				return builder.table(Tables.COURSE_MEETINGS);
			case COURSEMEETINGS_ID: {
				final String meetingId = CourseMeetings.getMeetingId(uri);
				return builder.table(Tables.COURSE_MEETINGS)
						.where(CourseMeetings._ID + "=?", meetingId);
			}
			case COURSEROSTER:
				return builder.table(Tables.COURSE_ROSTER);
			case COURSEROSTER_ID: {
					final String rosterId = CourseRoster.getRosterId(uri);
					return builder.table(Tables.COURSE_ROSTER)
							.where(CourseRoster._ID + "=?", rosterId);
			}
			case COURSEASSIGNMENTS:
				return builder.table(Tables.COURSE_ASSIGNMENTS);
			case COURSEASSIGNMENTS_ID: {
					final String assignmentId = CourseAssignments.getAssignmentId(uri);
					return builder.table(Tables.COURSE_ASSIGNMENTS)
							.where(CourseAssignments._ID + "=?", assignmentId);
			}
			case COURSEANNOUNCEMENTS:
				return builder.table(Tables.COURSE_ANNOUNCEMENTS);
			case COURSEANNOUNCEMENTS_ID: {
					final String announcementId = CourseAnnouncements.getAnnouncementId(uri);
					return builder.table(Tables.COURSE_ANNOUNCEMENTS)
							.where(CourseAnnouncements._ID + "=?", announcementId);
			}
			case COURSEEVENTS:
				return builder.table(Tables.COURSE_EVENTS);
			case COURSEEVENTS_ID: {
					final String eventId = CourseEvents.getEventId(uri);
					return builder.table(Tables.COURSE_EVENTS)
							.where(CourseEvents._ID + "=?", eventId);
			}
			case EVENTS:
				return builder.table(Tables.EVENTS);
			case EVENTS_ID: {
					final String eventsId = Events.getEventsId(uri);
					return builder.table(Tables.EVENTS)
							.where(Events.EVENTS_ID + "=?", eventsId);
			}
			case EVENTSCATEGORIES:
				return builder.table(Tables.EVENTS_CATEGORIES);
			case EVENTSCATEGORIES_ID: {
					final String categoryId = EventsCategories.getCategoryId(uri);
					return builder.table(Tables.EVENTS_CATEGORIES)
							.where(EventsCategories.EVENTS_CATEGORY_ID + "=?", categoryId);
			}
			case EVENTS_EVENTSCATEGORIES:
				return builder.table(Tables.EVENTS_EVENTS_CATEGORIES);
			case EVENTS_EVENTSCATEGORIES_ID: {
					final String id = EventsEventsCategories.getId(uri);
					return builder.table(Tables.EVENTS_EVENTS_CATEGORIES)
							.where(EventsEventsCategories._ID + "=?", id);
			}
			case REGISTRATIONLOCATIONS:
				return builder.table(Tables.REGISTRATION_LOCATIONS);
			case REGISTRATIONLOCATIONS_ID: {
					final String id = RegistrationLocations.getId(uri);
					return builder.table(Tables.REGISTRATION_LOCATIONS)
							.where(RegistrationLocations._ID + "=?", id);
			}
			case REGISTRATIONLEVELS:
				return builder.table(Tables.REGISTRATION_LEVELS);
			case REGISTRATIONLEVELS_ID: {
					final String id = RegistrationLevels.getId(uri);
					return builder.table(Tables.REGISTRATION_LEVELS)
							.where(RegistrationLevels._ID + "=?", id);
			}
            case DIRECTORIES: {
                return builder.table(Tables.DIRECTORIES);
            }
            default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}
	
	private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
		final SelectionBuilder builder = new SelectionBuilder();
		switch(match) {
		case MODULES:
			return builder.table(Tables.MODULES);
		case MODULESPROPERTIES:
			return builder.table(Tables.MODULES_PROPERTIES);
		case MODULESPROPERTIES_ID: {
			final String id = ModulesProperties.getPropertyId(uri);
			return builder.table(Tables.MODULES_PROPERTIES)
					.where(ModulesProperties._ID + "=?", id);
		}	
		case MODULESROLES:
			return builder.table(Tables.MODULES_ROLES);
		case MODULESROLES_ID: {
			final String id = ModulesRoles.getRoleId(uri);
			return builder.table(Tables.MODULES_ROLES)
					.where(ModulesRoles._ID + "=?", id);
		}
		case GRADETERMS:
			return builder.table(Tables.GRADE_TERMS);
		case GRADETERMS_ID:
		case GRADETERMS_ID_GRADECOURSES: {
			final String termId = GradeTerms.getTermId(uri);
			return builder.table(Tables.GRADE_TERMS_JOIN_GRADE_COURSES)
					.mapToTable(GradeTermsColumns.TERM_ID, Tables.GRADE_TERMS)
					.where(Qualified.GRADE_TERMS_TERMS_ID + "=?", termId);
		}
		case GRADECOURSES:
			return builder.table(Tables.GRADE_COURSES);
		case GRADECOURSES_ID: {
			final String gradeId = GradesCourses.getCourseId(uri);
			return builder.table(Tables.GRADE_COURSES)
					.where(GradesCourses.COURSE_ID + "=?", gradeId);
		}
		case GRADES:
			return builder.table(Tables.GRADES);
		case GRADES_ID: {
			final String gradeId = Grades.getGradeId(uri);
			return builder.table(Tables.GRADES)
					.where(Grades._ID + "=?", gradeId);
		}
		case GRADECOURSES_ID_GRADES: {
			final String courseId = GradesCourses.getCourseId(uri);
			return builder.table(Tables.GRADES_COURSES_JOIN_GRADES)
					.where(Qualified.GRADE_COURSES_COURSE_ID + "=?", courseId);
		}
		case MAPSCAMPUSES:
			return builder.table(Tables.MAPS_CAMPUSES);
		case MAPSCAMPUSES_ID: {
			final String campusId = MapsCampuses.getCampusId(uri);
			return builder.table(Tables.MAPS_CAMPUSES)
					.where(MapsCampuses.CAMPUS_NAME + "=?", campusId);
		}
		case MAPSCAMPUSES_ID_BUILDINGS: {
			final String campusId = MapsCampuses.getCampusId(uri);
			return builder.table(Tables.MAPS_BUILDINGS)
					.where(MapsCampuses.CAMPUS_NAME + "=?", campusId);
		}
		case MAPS_BUILDINGS:
			return builder.table(Tables.MAPS_BUILDINGS);
		case MAPS_BUILDINGS_ID: {
				final String buildingId = MapsBuildings.getBuildingId(uri);
				return builder.table(Tables.MAPS_BUILDINGS)
						.where(MapsBuildings._ID + "=?", buildingId);
		}
		case MAPS_BUILDINGS_CATEGORIES:
			return builder.table(Tables.MAPS_BUILDINGS_CATEGORIES);
		case MAPS_BUILDINGS_CATEGORIES_NAME: {
				final String categoryId = NumbersCategories.getCategoryId(uri);
				return builder.table(Tables.MAPS_BUILDINGS_CATEGORIES)
						.where(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME + "=?", categoryId);
		}
		case MAPS_BUILDINGS_BUILDINGSCATEGORIES:
			return builder.table(Tables.MAPS_BUILDINGS_BUILDINGSCATEGORIES);
		case MAPS_BUILDINGS_BUILDINGSCATEGORIES_ID: {
				final String id = EventsEventsCategories.getId(uri);
				return builder.table(Tables.MAPS_BUILDINGS_BUILDINGSCATEGORIES)
						.where(MapsBuildingsBuildingsCategories._ID + "=?", id);
		}
		case NOTIFICATIONS:
			return builder.table(Tables.NOTIFICATIONS);
		case NOTIFICATIONS_ID: {
				final String notificationsId = Notifications.getNotificationsId(uri);
				return builder.table(Tables.NOTIFICATIONS)
						.where(Notifications.NOTIFICATIONS_ID + "=?", notificationsId);
		}
		case NUMBERS:
			return builder.table(Tables.NUMBERS);
		case NUMBERS_ID: {
				final String numbersId = Numbers.getNumberId(uri);
				return builder.table(Tables.NUMBERS)
						.where(Numbers._ID + "=?", numbersId);
		}
		case NUMBERSCATEGORIES:
			return builder.table(Tables.NUMBERS_CATEGORIES);
		case NUMBERSCATEGORIES_ID: {
				final String categoryId = NumbersCategories.getCategoryId(uri);
				return builder.table(Tables.NUMBERS_CATEGORIES)
						.where(NumbersCategories._ID + "=?", categoryId);
		}
		case NEWS:
			return builder.table(Tables.NEWS);
		case NEWS_ID: {
				final String newsId = News.getNewsId(uri);
				return builder.table(Tables.NEWS)
						.where(News._ID + "=?", newsId);
		}
		case NEWS_SEARCH: {
			final String query = News.getSearchQuery(uri);
			return builder.table(Tables.NEWS_SEARCH_JOIN_NEWS)
					.map(News.SEARCH_SNIPPET,  Subquery.NEWS_SNIPPET)
					.mapToTable(News._ID, Tables.NEWS)
					.mapToTable(News.NEWS_CONTENT, Tables.NEWS)
					.mapToTable(News.NEWS_ENTRY_ID, Tables.NEWS)
					.mapToTable(News.NEWS_FEED_NAME, Tables.NEWS)
					.mapToTable(News.NEWS_LINK, Tables.NEWS)
					.mapToTable(News.NEWS_LIST_DESCRIPTION, Tables.NEWS)
					.mapToTable(News.NEWS_LOGO, Tables.NEWS)
					.mapToTable(News.NEWS_POST_DATE, Tables.NEWS)
					.mapToTable(News.NEWS_TITLE, Tables.NEWS)
					.mapToTable(Modules.MODULES_ID, Tables.MODULES)
					.mapToTable(NewsCategories.NEWS_CATEGORY_ID, Tables.NEWS_CATEGORIES)
					.where(NewsSearchColumns.BODY + " MATCH ?", query);
		}
		case NEWSCATEGORIES:
			return builder.table(Tables.NEWS_CATEGORIES);
		case NEWSCATEGORIES_ID: {
				final String categoryId = NewsCategories.getCategoryId(uri);
				return builder.table(Tables.NEWS_CATEGORIES)
						.where(NewsCategories.NEWS_CATEGORY_ID + "=?", categoryId);
		}
		case COURSETERMS:
			return builder.table(Tables.COURSE_TERMS);
		case COURSETERMS_ID: {
			final String termId = CourseTerms.getTermId(uri);
			return builder.table(Tables.COURSE_TERMS)
					.where(CourseTerms.TERM_ID + "=?", termId);
		}
		case COURSECOURSES:
			return builder.table(Tables.COURSE_COURSES);
		case COURSECOURSES_ID: {
			final String courseId = CourseCourses.getCourseId(uri);
			return builder.table(Tables.COURSE_COURSES)
					.where(CourseCourses.COURSE_ID + "=?", courseId);
		}
		case COURSEINSTRUCTORS:
			return builder.table(Tables.COURSE_INSTRUCTORS);
		case COURSEINSTRUCTORS_ID: {
			final String instructorId = CourseInstructors.getInstructorId(uri);
			return builder.table(Tables.COURSE_INSTRUCTORS)
					.where(CourseInstructors._ID + "=?", instructorId);
		}
		case COURSEPATTERNS:
			return builder.table(Tables.COURSE_PATTERNS);
		case COURSEPATTERNS_ID: {
			final String patternId = CoursePatterns.getPatternId(uri);
			return builder.table(Tables.COURSE_PATTERNS)
					.where(CoursePatterns._ID + "=?", patternId);
		}
		case COURSEMEETINGS:
			return builder.table(Tables.COURSE_MEETINGS);
		case COURSEMEETINGS_ID: {
			final String meetingId = CourseMeetings.getMeetingId(uri);
			return builder.table(Tables.COURSE_MEETINGS)
					.where(CourseMeetings._ID + "=?", meetingId);
		}
		case COURSEROSTER:
			return builder.table(Tables.COURSE_ROSTER);
		case COURSEROSTER_ID: {
				final String rosterId = CourseRoster.getRosterId(uri);
				return builder.table(Tables.COURSE_ROSTER)
						.where(CourseRoster._ID + "=?", rosterId);
		}
		case COURSEASSIGNMENTS:
			return builder.table(Tables.COURSE_ASSIGNMENTS);
		case COURSEASSIGNMENTS_ID: {
				final String assignmentId = CourseAssignments.getAssignmentId(uri);
				return builder.table(Tables.COURSE_ASSIGNMENTS)
						.where(CourseAssignments._ID + "=?", assignmentId);
		}
		case COURSEANNOUNCEMENTS:
			return builder.table(Tables.COURSE_ANNOUNCEMENTS);
		case COURSEANNOUNCEMENTS_ID: {
				final String announcementId = CourseAnnouncements.getAnnouncementId(uri);
				return builder.table(Tables.COURSE_ANNOUNCEMENTS)
						.where(CourseAnnouncements._ID + "=?", announcementId);
		}
		case COURSEEVENTS:
			return builder.table(Tables.COURSE_EVENTS);
		case COURSEEVENTS_ID: {
				final String eventId = CourseEvents.getEventId(uri);
				return builder.table(Tables.COURSE_EVENTS)
						.where(CourseEvents._ID + "=?", eventId);
		}
		case EVENTS:
			//return builder.table(Tables.EVENTS);
			return builder.table(Tables.EVENTS_JOIN_CATEGORIES)
					.mapToTable(Events.EVENTS_ID, Tables.EVENTS)
					.mapToTable(EventsCategories.EVENTS_CATEGORY_ID, Tables.EVENTS_CATEGORIES)
					.mapToTable(Events._ID, Tables.EVENTS)
					.mapToTable(Modules.MODULES_ID, Tables.EVENTS);
		case EVENTS_ID: {
				final String eventsId = Events.getEventsId(uri);
//				return builder.table(Tables.EVENTS)
//						.where(Events._ID + "=?", eventsId);
				return builder.table(Tables.EVENTS_JOIN_CATEGORIES)
						.where(Qualified.EVENTS_ID + "=?", eventsId)
						.mapToTable(Events.EVENTS_ID, Tables.EVENTS)
						.mapToTable(EventsCategories.EVENTS_CATEGORY_ID, Tables.EVENTS_CATEGORIES)
						.mapToTable(Events._ID, Tables.EVENTS);
		}
		case EVENTS_SEARCH: {
			final String query = Events.getSearchQuery(uri);
			return builder.table(Tables.EVENTS_SEARCH_JOIN_EVENTS_JOIN_CATEGORIES)
					.map(Events.SEARCH_SNIPPET,  Subquery.EVENTS_SNIPPET)
					.mapToTable(Events._ID, Tables.EVENTS)
					.mapToTable(Events.EVENTS_ALL_DAY, Tables.EVENTS)
					.mapToTable(Events.EVENTS_CATEGORIES, Tables.EVENTS)
					.mapToTable(Events.EVENTS_CONTACT, Tables.EVENTS)
					.mapToTable(Events.EVENTS_DESCRIPTION, Tables.EVENTS)
					.mapToTable(Events.EVENTS_DURATION, Tables.EVENTS)
					.mapToTable(Events.EVENTS_EMAIL, Tables.EVENTS)
					.mapToTable(Events.EVENTS_END, Tables.EVENTS)
					.mapToTable(Events.EVENTS_ID, Tables.EVENTS)
					.mapToTable(Events.EVENTS_LOCATION, Tables.EVENTS)
					.mapToTable(Events.EVENTS_START, Tables.EVENTS)
					.mapToTable(Events.EVENTS_TITLE, Tables.EVENTS)
					.mapToTable(Events.EVENTS_UID, Tables.EVENTS)
					.mapToTable(Modules.MODULES_ID, Tables.MODULES)
					.mapToTable(EventsCategories.EVENTS_CATEGORY_ID, Tables.EVENTS_CATEGORIES)
					.where(EventsSearchColumns.BODY + " MATCH ?", query);
		}
		case EVENTSCATEGORIES:
			return builder.table(Tables.EVENTS_CATEGORIES);
		case EVENTSCATEGORIES_ID: {
				final String categoryId = EventsCategories.getCategoryId(uri);
				return builder.table(Tables.EVENTS_CATEGORIES)
						.where(EventsCategories.EVENTS_CATEGORY_ID + "=?", categoryId);
		}
		case EVENTS_EVENTSCATEGORIES:
			return builder.table(Tables.EVENTS_EVENTS_CATEGORIES);
		case EVENTS_EVENTSCATEGORIES_ID: {
				final String id = EventsEventsCategories.getId(uri);
				return builder.table(Tables.EVENTS_EVENTS_CATEGORIES)
						.where(EventsEventsCategories._ID + "=?", id);
		}
		case REGISTRATIONLOCATIONS:
			return builder.table(Tables.REGISTRATION_LOCATIONS);
		case REGISTRATIONLOCATIONS_ID: {
				final String id = RegistrationLocations.getId(uri);
				return builder.table(Tables.REGISTRATION_LOCATIONS)
						.where(RegistrationLocations._ID + "=?", id);
		}
		case REGISTRATIONLEVELS:
			return builder.table(Tables.REGISTRATION_LEVELS);
		case REGISTRATIONLEVELS_ID: {
				final String id = RegistrationLevels.getId(uri);
				return builder.table(Tables.REGISTRATION_LEVELS)
						.where(RegistrationLevels._ID + "=?", id);
		}
        case DIRECTORIES: {
            return builder.table(Tables.DIRECTORIES);
        }
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}
	
	private void notifyChange(Uri uri) {
		if(isBatch) {
			Log.v(this.getClass().getSimpleName(), "Batching notification change for: " + uri);
			batchChangeNotifications.put(uri.toString(), uri);
		} else {
			Log.v(this.getClass().getSimpleName(), "Notification change for: " + uri);
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}
	
	private interface Subquery {
		String NEWS_SNIPPET = "snippet(" + Tables.NEWS_SEARCH + ",'{','}','\u2026')";
		String EVENTS_SNIPPET = "snippet(" + Tables.EVENTS_SEARCH + ",'{','}','\u2026')";
	}
	
	/**
     * {@link EllucianContract} fields that are fully qualified with a specific
     * parent {@link Tables}. Used when needed to work around SQL ambiguity.
     */
	private interface Qualified {
		String GRADE_TERMS_TERMS_ID = Tables.GRADE_TERMS + "." + GradeTermsColumns.TERM_ID;
		String GRADE_COURSES_COURSE_ID = Tables.GRADE_COURSES + "." + GradeCoursesColumns.COURSE_ID;
		String EVENTS_ID = Tables.EVENTS + "." + EventsColumns.EVENTS_ID;
	}
}
