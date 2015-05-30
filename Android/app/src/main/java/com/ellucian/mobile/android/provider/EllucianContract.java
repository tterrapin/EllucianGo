/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.provider;

import java.util.List;

import android.net.Uri;
import android.provider.BaseColumns;

import com.ellucian.elluciango.BuildConfig;

public class EllucianContract {
	
	public static final String CONTENT_AUTHORITY = BuildConfig.contentProvider;
	static final String PATH_SECURED = "secured";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	public static final Uri SECURED_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SECURED).build();
	
	static final String PATH_MODULES = "modules";
	private static final String PATH_MODULES_PROPERTIES = "properties";
	private static final String PATH_MODULES_ROLES = "roles";
	private static final String PATH_MODULES_URLS = "urls";
	static final String PATH_SEARCH = "search";
	
	interface ModulesColumns {
		String MODULES_ID = "module_id";
		String MODULES_ICON_URL = "module_icon_url";
		String MODULE_ORDER = "module_order";
		String MODULE_NAME = "module_name";
		String MODULE_SHOW_FOR_GUEST = "module_guest";
		String MODULE_TYPE = "module_type";
		String MODULE_SECURE ="module_secure";
		String MODULE_SUB_TYPE = "module_sub_type";
		String MODULE_LOCK = "module_lock"; //pseudo column not in database
		String MODULE_RIGHT_TEXT = "module_right_text"; //pseudo column not in database
	}
	
	interface ModulesPropertiesColumns {
		String MODULE_PROPERTIES_NAME = "moduleurl_name";
		String MODULE_PROPERTIES_VALUE = "moduleurl_url";
	}
	
	interface ModulesRolesColumns {
		String MODULE_ROLES_NAME = "moduleroles_name";
	}

	interface GradeTermsColumns {
		String TERM_ID = "gradeterms_id";
		String TERM_NAME = "gradeterms_name";
		String TERM_START_DATE = "gradeterms_start_date";
		String TERM_END_DATE = "gradeterms_end_date";	
	}
	
	interface GradeCoursesColumns {
		String COURSE_ID = "gradecourses_course_id"; 
		String COURSE_ERP_ID = "gradecourses_course_erp_id";
		String COURSE_TITLE = "gradecourses_course_title";
		String COURSE_DESCRIPTION = "gradecourses_course_description";
		String COURSE_CREDIT_HOURS = "gradecourses_credit_hours";
		String COURSE_SECTION = "gradecourses_course_section";	
	}
	
	interface GradesColumns {
		String GRADE_NAME = "grade_name";
		String GRADE_TYPE = "grade_type";
		String GRADE_VALUE = "grade_value";
		String GRADE_UPDATED = "grade_updated";
	}
	
	interface MapsCampusesColumns {
		String CAMPUS_ID = "mapscampuses_id";
		String CAMPUS_NAME = "mapscampuses_name";
		String CAMPUS_CENTER_LATITUDE = "mapscampuses_center_lat";
		String CAMPUS_CENTER_LONGITUDE = "mapscampuses_center_lng";
		String CAMPUS_NORTHWEST_LATITUDE = "mapscampuses_northwest_lat";
		String CAMPUS_NORTHWEST_LONGITUDE = "mapscampuses_northwest_lng";
		String CAMPUS_SOUTHEAST_LATITUDE = "mapscampuses_southeast_lat";
		String CAMPUS_SOUTHEAST_LONGITUDE = "mapscampuses_southeast_lng";
	}
	
	interface MapsBuildingsColumns {
		//String BUILDING_CATEGORY = "mapsbuildings_category";
		String BUILDING_BUILDING_ID = "mapsbuildings_building_id";
		String BUILDING_NAME = "mapsbuildings_name";
		String BUILDING_DESCRIPTION = "mapsbuildings_description";
		String BUILDING_IMAGE_URL = "mapsbuildings_image_url";
		String BUILDING_ADDRESS = "mapsbuildings_address";
		String BUILDING_LATITUDE = "mapsbuildings_lat";
		String BUILDING_LONGITUDE = "mapsbuildings_long";
		String BUILDING_ADDITIONAL_SERVICES = "mapsbuildings_additional_services";
		String BUILDING_CATEGORIES = "mapsbuildings_categories";
		
	}
	
	interface MapsBuildingsCategoriesColumns {
		String MAPS_BUILDINGS_CATEGORY_NAME = "maps_buildings_categories_name";
	}
	
	interface NotificationsColumns {
		String NOTIFICATIONS_ID = "notifications_id";
		String NOTIFICATIONS_TITLE = "notifications_title";
		String NOTIFICATIONS_DETAILS = "notifications_details";
		String NOTIFICATIONS_HYPERLINK = "notifications_hyperlink";
		String NOTIFICATIONS_LINK_LABEL = "notifications_link_label";
		String NOTIFICATIONS_DATE = "notifications_date";
		String NOTIFICATIONS_SOURCE = "notifications_source";
		String NOTIFICATIONS_DISPATCH_DATE = "notifications_dispatch_date";
		String NOTIFICATIONS_MOBILE_HEADLINE= "notifications_mobile_headline";
		String NOTIFICATIONS_EXPIRES = "notifications_expires";
		String NOTIFICATIONS_PUSH = "notifications_push";
		String NOTIFICATIONS_MODULE = "notifications_module";
		String NOTIFICATIONS_STICKY = "notifications_sticky";
		String NOTIFICATIONS_STATUSES = "notifications_statuses";
	}
	
	interface NumbersColumns {
		String NUMBERS_NAME = "numbers_name";
		String NUMBERS_ADDRESS = "numbers_address";
		String NUMBERS_EMAIL = "numbers_email";
		String NUMBERS_PHONE = "numbers_phone";
		String NUMBERS_LATITUDE = "numbers_lat";
		String NUMBERS_LONGITUDE = "numbers_long"; 
		String NUMBERS_BUILDING_ID = "numbers_building_id";
		String NUMBERS_CAMPUS_ID = "numbers_campus_id";
        String NUMBERS_EXTENSION = "numbers_extension";
	}
	
	interface NumbersCategoriesColumns {
		String NUMBERS_CATEGORY_NAME = "numberscategories_name";
	}
	
	interface NewsColumns {
		String NEWS_ENTRY_ID = "news_entry_id";
		String NEWS_FEED_NAME = "news_feed_name";
		String NEWS_TITLE = "news_title";
		String NEWS_CONTENT = "news_content";
		String NEWS_LIST_DESCRIPTION = "news_list_description";
		String NEWS_LINK = "news_link";
		String NEWS_LOGO = "news_logo";
		String NEWS_POST_DATE = "news_post_date";
	}
	
	interface NewsCategoriesColumns {
		String NEWS_CATEGORY_ID = "news_category_id";
		String NEWS_CATEGORY_NAME = "news_category_name";
	}
	
	interface CourseTermsColumns {
		String TERM_ID = "gradeterms_id";
		String TERM_NAME = "gradeterms_name";
		String TERM_START_DATE = "gradeterms_start_date";
		String TERM_END_DATE = "gradeterms_end_date";	
	}
	
	interface CourseCoursesColumns {
		String COURSE_ID = "coursecourses_id"; 
		String COURSE_NAME = "coursecourses_name";
		String COURSE_TITLE = "coursecourses_title";
		String COURSE_DESCRIPTION = "coursecourses_description";
		String COURSE_SECTION_NUMBER = "coursecourses_section_number";
		String COURSE_IS_INSTRUCTOR = "coursecourses_is_instructor";
		String COURSE_LEARNING_PROVIDER = "coursecourses_learning_provider";
		String COURSE_LEARNING_PROVIDER_SITE_ID = "coursecourses_learning_provider_site_id";
	}

	interface CourseInstructorsColumns {
		String INSTRUCTOR_ID = "courseinstructors_id"; 
		String INSTRUCTOR_FIRST_NAME = "courseinstructors_first_name";
		String INSTRUCTOR_MIDDLE_NAME = "courseinstructors_middle_name";
		String INSTRUCTOR_LAST_NAME = "courseinstructors_last_name";
		String INSTRUCTOR_FORMATTED_NAME = "courseinstructors_formatted_name";
		String INSTRUCTOR_PRIMARY = "courseinstructors_primary";
	}
	
	interface CoursePatternsColumns {
		String PATTERN_DAYS = "coursepatterns_days";
		String PATTERN_START_TIME = "corusepaterns_start_time";
		String PATTERN_END_TIME = "coursepatterns_end_time";
		String PATTERN_LOCATION = "coursepatterns_location";
		String PATTERN_ROOM = "coursepatterns_room";
		String PATTERN_INSTRUCTIONAL_METHOD = "coursepatterns_instructional_method";
	}
	
	interface CourseMeetingsColumns {
		String MEETING_SUMMARY = "meeting_summary";
		String MEETING_LOCATION = "meeting_location";
		String MEETING_START = "meeting_start";
		String MEETING_END = "meeting_end";
	}
	
	interface CourseRosterColumns {
		String ROSTER_STUDENT_ID = "roster_student_id";
		String ROSTER_COURSE_ID = "roster_course_id";
		String ROSTER_FIRST_NAME = "roster_first_name";
		String ROSTER_MIDDLE_NAME = "roster_middle_name";
		String ROSTER_LAST_NAME = "roster_last_name";
		String ROSTER_FORMATTED_NAME = "roster_formatted_name";
		String ROSTER_PHOTO = "roster_photo";
	}
	
	interface CourseAssignmentsColumns {
		String ASSIGNMENT_NAME = "assignment_name";
		String ASSIGNMENT_DESCRIPTION = "assignment_description";
		String ASSIGNMENT_DUE = "assignment_due";
		String ASSIGNMENT_URL= "assignment_url";
        String ASSIGNMENT_SECTION_NAME = "assignment_section_name";
	}
	
	interface CourseAnnouncementsColumns {
		String ANNOUNCEMENT_TITLE = "announcement_title";
		String ANNOUNCEMENT_CONTENT = "announcement_content";
		String ANNOUNCEMENT_DATE = "announcement_date";
		String ANNOUNCEMENT_URL= "announcement_url";
        String ANNOUNCEMENT_SECTION_NAME = "announcement_section_name";
	}
	
	interface CourseEventsColumns {
		String EVENT_TITLE = "event_title";
		String EVENT_DESCRIPTION = "event_description";
		String EVENT_LOCATION = "event_location";
		String EVENT_START = "event_start";
		String EVENT_END = "event_end";
		String EVENT_ALL_DAY= "event_all_day";
        String EVENT_SECTION_NAME = "event_section_name";
	}

	interface EventsColumns {
		String EVENTS_ID = "events_id";
		String EVENTS_UID = "events_uid";
		String EVENTS_TITLE = "events_title";
		String EVENTS_DESCRIPTION = "events_description";
		String EVENTS_LOCATION = "events_location";
		String EVENTS_CONTACT = "events_contact";
		String EVENTS_DURATION = "events_duration";
		String EVENTS_EMAIL = "events_email";
		String EVENTS_CATEGORIES = "events_categories";
		String EVENTS_START = "events_start";
		String EVENTS_END = "events_end";
		String EVENTS_ALL_DAY = "events_all_day";	
	}
	
	interface EventsCategoriesColumns {
		String EVENTS_CATEGORY_ID = "eventscategories_id";
		String EVENTS_CATEGORY_NAME = "eventscategories_name";	
	}
	
	interface RegistrationLocationsColumns {
		String REGISTRATION_LOCATIONS_NAME = "registrationlocations_name";
		String REGISTRATION_LOCATIONS_CODE = "registrationlocations_code";	
	}
	
	interface RegistrationLevelsColumns {
		String REGISTRATION_LEVELS_NAME = "registrationlevels_name";
		String REGISTRATION_LEVELS_CODE = "registrationlevels_code";	
	}
	
	public static class Modules implements ModulesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MODULES).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.module";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.module";
		
		public static final String DEFAULT_SORT = ModulesColumns.MODULE_ORDER + " ASC";

        public static Uri buildModuleUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
        public static Uri buildPropertiesUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).appendPath(PATH_MODULES_PROPERTIES).build();
        }
        
        public static Uri buildRolesUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).appendPath(PATH_MODULES_ROLES).build();
        }

		public static Uri buildUrlsUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).appendPath(PATH_MODULES_URLS).build();
		}
        
		public static String getModuleId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_MODULESPROPERTIES = "modules_urls";
	public static class ModulesProperties implements ModulesPropertiesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MODULESPROPERTIES).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.module_url";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.module_url";
		
		public static final String DEFAULT_SORT = ModulesPropertiesColumns.MODULE_PROPERTIES_NAME + " ASC";
		
        public static Uri buildPropertyUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
		public static String getPropertyId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_MODULESROLES = "modules_roles";
	public static class ModulesRoles implements ModulesRolesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MODULESROLES).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.module_roles";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.module_roles";
		
		public static final String DEFAULT_SORT = ModulesRolesColumns.MODULE_ROLES_NAME + " ASC";
		
        public static Uri buildRoleUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
		public static String getRoleId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}


	static final String PATH_GRADETERMS = "grade_terms";
	static final String PATH_GRADETERMS_COURSES = "grade_courses";
	public static class GradeTerms implements GradeTermsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRADETERMS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.grade_term";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.grade_term";
		
		public static final String DEFAULT_SORT = GradeTermsColumns.TERM_START_DATE + " DESC";
		
        public static Uri buildTermUri(String termId) {
            return CONTENT_URI.buildUpon().appendPath(termId).build();
        }
        
        public static Uri buildCoursesUri(String termId) {
        	return CONTENT_URI.buildUpon().appendPath(termId).appendPath(PATH_GRADETERMS_COURSES).build();
        }

		
		public static String getTermId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_GRADECOURSES = "grade_courses";
	static final String PATH_GRADESCOURSES_GRADES = "grades";
	public static class GradesCourses implements GradeCoursesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRADECOURSES).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.grade_courses";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.grade_courses";
		
		public static final String DEFAULT_SORT = GradeCoursesColumns.COURSE_DESCRIPTION + " ASC";
		
        public static Uri buildCourseUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
		
        public static Uri buildGradesUri(String id) {
        	return CONTENT_URI.buildUpon().appendPath(id).appendPath(PATH_GRADESCOURSES_GRADES).build();
        }
        
		public static String getCourseId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_GRADES = "grades";
	public static class Grades implements GradesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRADES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.grades";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.grades";
		
		public static final String DEFAULT_SORT = GradesColumns.GRADE_TYPE + " ASC";
		
        public static Uri buildGradeUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
		
		public static String getGradeId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_MAPS_CAMPUSES = "maps_campuses";
	static final String PATH_MAPS_CAMPUSES_BUILDINGS = "buildings";
	public static class MapsCampuses implements MapsCampusesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAPS_CAMPUSES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.maps_campuses";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.maps_campuses";
		
		public static final String DEFAULT_SORT = MapsCampusesColumns.CAMPUS_NAME + " ASC";
		
		public static Uri buildCampusUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static Uri buildBuildingsUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).appendPath(PATH_MAPS_CAMPUSES_BUILDINGS).build();
		}
		
		public static String getCampusId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_MAPS_BUILDINGS = "maps_buildings";
	public static class MapsBuildings implements MapsBuildingsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAPS_BUILDINGS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.maps_buildings";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.maps_buildings";
		
		public static final String DEFAULT_SORT = MapsBuildingsColumns.BUILDING_NAME + " ASC";
		
		public static Uri buildBuildingUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getBuildingId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_MAPS_BUILDINGS_CATEGORIES = "maps_buildings_categories";
	public static class MapsBuildingsCategories implements MapsBuildingsCategoriesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAPS_BUILDINGS_CATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.maps_buildings_categories";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.maps_buildings_categories";
		
		public static final String DEFAULT_SORT = MapsBuildingsCategoriesColumns.MAPS_BUILDINGS_CATEGORY_NAME + " ASC";
		
		public static Uri buildCategoryUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getCategoryId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_MAPS_BUILDINGS_BUILDINGSCATEGORIES = "maps_buildings_buildings_categories";
	public static class MapsBuildingsBuildingsCategories implements BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAPS_BUILDINGS_BUILDINGSCATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.maps_buildings_buildings_categories";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.maps_buildings_buildings_categories";
		
		public static final String DEFAULT_SORT = MapsBuildingsColumns.BUILDING_NAME + " ASC";
		
		public static Uri buildUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_NOTIFICATIONS = "notifications";
	public static class Notifications implements NotificationsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTIFICATIONS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.notifications";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.notifications";
		
		public static final String DEFAULT_SORT = NotificationsColumns.NOTIFICATIONS_STICKY + " DESC, " + NotificationsColumns.NOTIFICATIONS_DATE + " DESC";
		
		public static Uri buildNotificationsUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getNotificationsId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_NUMBERS = "numbers";
	public static class Numbers implements NumbersColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NUMBERS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.numbers";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.numbers";
		
		public static final String DEFAULT_SORT = NumbersColumns.NUMBERS_NAME + " ASC";
		
		public static Uri buildNumberUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getNumberId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_NUMBERS_CATEGORIES = "numbers_categories";
	public static class NumbersCategories implements NumbersCategoriesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NUMBERS_CATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.numbers_categories";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.numbers_categories";
		
		public static final String DEFAULT_SORT = NumbersCategoriesColumns.NUMBERS_CATEGORY_NAME + " ASC";
		
		public static Uri buildCategoryUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getCategoryId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_NEWS = "news";
	public static class News implements NewsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.news";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.news";
		
		public static final String DEFAULT_SORT = News.NEWS_POST_DATE + " DESC";
		
		public static final String SEARCH_SNIPPET = "search_snippet";
		
		public static Uri buildNewsUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getNewsId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
		
		public static Uri buildSearchUri(String query) {
			return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
		}
		
		public static boolean isSearchUri(Uri uri) {
			List<String> pathSegments = uri.getPathSegments();
			return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
		}
		
		public static String getSearchQuery(Uri uri) {
			return uri.getPathSegments().get(2);
		}
	}
	
	static final String PATH_NEWS_CATEGORIES = "news_categories";
	public static class NewsCategories implements NewsCategoriesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS_CATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.news_categories";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.news_categories";
		
		public static final String DEFAULT_SORT = NewsCategoriesColumns.NEWS_CATEGORY_NAME + " ASC";
		
		public static Uri buildCategoryUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getCategoryId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_COURSETERMS = "course_terms";
	public static class CourseTerms implements CourseTermsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSETERMS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_term";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_term";
		
		public static final String DEFAULT_SORT = CourseTermsColumns.TERM_START_DATE + " DESC";
		
        public static Uri buildTermUri(String termId) {
            return CONTENT_URI.buildUpon().appendPath(termId).build();
        }
        
		public static String getTermId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_COURSECOURSES = "course_courses";
	public static class CourseCourses implements CourseCoursesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSECOURSES).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_courses";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_courses";
		
		public static final String DEFAULT_SORT = CourseCoursesColumns.COURSE_NAME + " ASC";
		
        public static Uri buildCourseUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
		public static String getCourseId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	static final String PATH_COURSEINSTRUCTORS = "course_instructors";
	public static class CourseInstructors implements CourseInstructorsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEINSTRUCTORS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_instructors";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_instructors";
		
		public static final String DEFAULT_SORT = CourseInstructorsColumns.INSTRUCTOR_FORMATTED_NAME + " ASC";
		
        public static Uri buildInstructorUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
		public static String getInstructorId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	static final String PATH_COURSEPATTERNS = "course_patterns";
	public static class CoursePatterns implements CoursePatternsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEPATTERNS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_patterns";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_patterns";
		
		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";
		
        public static Uri buildPatternUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
		public static String getPatternId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	static final String PATH_COURSEMEETINGS = "course_meetings";
	public static class CourseMeetings implements CourseMeetingsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEMEETINGS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_meetings";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_meetings";
		
		public static final String DEFAULT_SORT = CourseMeetingsColumns.MEETING_START + " ASC";
		
        public static Uri buildMeetingUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
		public static String getMeetingId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_COURSEROSTER = "course_roster";
	public static class CourseRoster implements CourseRosterColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEROSTER).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_roster";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_roster";
		
		public static final String DEFAULT_SORT = CourseRosterColumns.ROSTER_LAST_NAME + " ASC";
		
		public static Uri buildRosterUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getRosterId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_COURSEASSIGNMENTS = "course_assignments";
	public static class CourseAssignments implements CourseAssignmentsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEASSIGNMENTS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_assignments";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_assignments";
		
		public static final String DEFAULT_SORT = CourseAssignmentsColumns.ASSIGNMENT_DUE + " DESC";
		
		public static Uri buildAssignmentUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getAssignmentId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_COURSEANNOUNCEMENTS = "course_announcements";
	public static class CourseAnnouncements implements CourseAnnouncementsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEANNOUNCEMENTS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_announcements";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_announcements";
		
		public static final String DEFAULT_SORT = CourseAnnouncementsColumns.ANNOUNCEMENT_DATE + " ASC";
		
		public static Uri buildAnnouncementUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getAnnouncementId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_COURSEEVENTS = "course_events";
	public static class CourseEvents implements CourseEventsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSEEVENTS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.course_events";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.course_events";
		
		public static final String DEFAULT_SORT = CourseEventsColumns.EVENT_START + " ASC";
		
		public static Uri buildEventUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getEventId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	static final String PATH_EVENTS = "events";
//	static final String PATH_EVENTS_WITH_CATEGORIES = "events_with_categories";
	public static class Events implements EventsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();
//		public static final Uri EVENTS_WITH_CATEGORIES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS_WITH_CATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.events";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.events";
		
		public static final String DEFAULT_SORT = EventsColumns.EVENTS_START + " ASC";
	
		public static final String SEARCH_SNIPPET = "search_snippet";
	
		public static Uri buildEventsUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getEventsId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
		
		public static Uri buildSearchUri(String query) {
			return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
		}
		
		public static boolean isSearchUri(Uri uri) {
			List<String> pathSegments = uri.getPathSegments();
			return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
		}
		
		public static String getSearchQuery(Uri uri) {
			return uri.getPathSegments().get(2);
		}
	}
	
	static final String PATH_EVENTS_CATEGORIES = "events_categories";
	public static class EventsCategories implements EventsCategoriesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS_CATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.events_categories";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.events_categories";
		
		public static final String DEFAULT_SORT = EventsCategoriesColumns.EVENTS_CATEGORY_NAME + " ASC";
		
		public static Uri buildCategoryUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getCategoryId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_EVENTS_EVENTSCATEGORIES = "events_events_categories";
	public static class EventsEventsCategories implements BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS_EVENTSCATEGORIES).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.events_events_categories";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.events_events_categories";
		
		public static final String DEFAULT_SORT = Events.EVENTS_ID + " ASC";
		
		public static Uri buildUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_REGISTRATION_LOCATIONS = "registration_locations";
	public static class RegistrationLocations implements RegistrationLocationsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REGISTRATION_LOCATIONS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.registration_locations";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.registration_locations";
		
		public static final String DEFAULT_SORT = RegistrationLocationsColumns.REGISTRATION_LOCATIONS_NAME + " ASC";
		
		public static Uri buildUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	static final String PATH_REGISTRATION_LEVELS = "registration_levels";
	public static class RegistrationLevels implements RegistrationLevelsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REGISTRATION_LEVELS).build();
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ellucian.registration_levels";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ellucian.registration_levels";
		
		public static final String DEFAULT_SORT = RegistrationLevelsColumns.REGISTRATION_LEVELS_NAME + " ASC";
		
		public static Uri buildUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}
		
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
	
	private EllucianContract() {
		
	}
}
