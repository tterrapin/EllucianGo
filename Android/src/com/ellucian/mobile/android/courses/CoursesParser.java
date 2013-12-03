package com.ellucian.mobile.android.courses;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ellucian.mobile.android.Utils;
import com.ellucian.mobile.android.grades.Grade;

public class CoursesParser {
	public static List<Term> parse(String jString) throws JSONException {
		final List<Term> terms = new ArrayList<Term>();
		final JSONObject jObject = new JSONObject(jString);
		final JSONArray jTerms = jObject.getJSONArray("Terms");
		for (int i = 0; i < jTerms.length(); i++) {
			final Term term = new Term();
			terms.add(term);
			final JSONObject termObj = jTerms.getJSONObject(i);
			if (!termObj.isNull("Name")) {
				term.setName(termObj.getString("Name"));
			}
			if (!termObj.isNull("StartDate")) {
				term.setDate(Utils.convertJsonDate(termObj
						.getString("StartDate")));
			}
			final JSONArray jCourses = termObj.getJSONArray("Courses");
			for (int j = 0; j < jCourses.length(); j++) {
				final Course course = new Course();
				term.addCourse(course);
				final JSONObject courseObj = jCourses.getJSONObject(j);
				if (!courseObj.isNull("CourseDescription")) {
					course.setDescription(courseObj
							.getString("CourseDescription"));
				}
				if (!courseObj.isNull("CourseName")) {
					course.setName(courseObj.getString("CourseName"));
				}
				if (!courseObj.isNull("SectionTitle")) {
					course.setSectionTitle(courseObj.getString("SectionTitle"));
				}
				if (!courseObj.isNull("IsILP")) {
					course.setIsILP(courseObj.getBoolean("IsILP"));
				}
				if (!courseObj.isNull("LearningProviderId")) {
					course.setLearningProviderId(courseObj
							.getString("LearningProviderId"));
				}
				if (!courseObj.isNull("SectionId")) {
					course.setCourseId(courseObj.getString("SectionId"));
				}
				final JSONArray jFaculty = courseObj
						.getJSONArray("Instructors");
				for (int k = 0; k < jFaculty.length(); k++) {
					final Faculty faculty = new Faculty();
					course.addFaculty(faculty);
					final JSONObject facultyObj = jFaculty.getJSONObject(k);
					if (!facultyObj.isNull("FacultyDomain")) {
						faculty.setDomain(facultyObj.getString("FacultyDomain"));
					}
					if (!facultyObj.isNull("FacultyName")) {
						faculty.setName(facultyObj.getString("FacultyName"));
					}
					if (!facultyObj.isNull("FacultyUserName")) {
						faculty.setUsername(facultyObj
								.getString("FacultyUserName"));
					}
				}
				final JSONArray jSession = courseObj
						.getJSONArray("SectionMeetings");
				for (int k = 0; k < jSession.length(); k++) {
					final Session session = new Session();
					course.addSession(session);
					final JSONObject sessionObj = jSession.getJSONObject(k);
					if (!sessionObj.isNull("Days")) {
						session.setDays(sessionObj.getString("Days"));
					}
					if (!sessionObj.isNull("DateEnd")) {
						session.setEndDate(Utils.convertJsonDate(sessionObj
								.getString("DateEnd")));
					}
					if (!sessionObj.isNull("DateStart")) {
						session.setStartDate(Utils.convertJsonDate(sessionObj
								.getString("DateStart")));
					}
					if (!sessionObj.isNull("InstructionalMethod")) {
						session.setInstructionalMethod(sessionObj
								.getString("InstructionalMethod"));
					}
					if (!sessionObj.isNull("Pattern")) {
						session.setPattern(sessionObj.getString("Pattern"));
					}
					if (!sessionObj.isNull("TimeEnd")) {
						session.setTimeEnd(sessionObj.getString("TimeEnd"));
					}
					if (!sessionObj.isNull("TimeStart")) {
						session.setTimeStart(sessionObj.getString("TimeStart"));
					}
					if (!sessionObj.isNull("Location")) {
						final Location location = new Location();
						session.setLocation(location);
						final JSONObject locationObj = sessionObj
								.getJSONObject("Location");
						if (!locationObj.isNull("BuildingLabel")) {
							location.setBuildingLabel(locationObj
									.getString("BuildingLabel"));
						}
						if (!locationObj.isNull("Latitude")) {
							location.setLatitude(locationObj
									.getDouble("Latitude"));
						}
						if (!locationObj.isNull("Longitude")) {
							location.setLongitude(locationObj
									.getDouble("Longitude"));
						}
						if (!locationObj.isNull("Description")) {
							location.setDescription(locationObj
									.getString("Description"));
						}
						if (!locationObj.isNull("RoomId")) {
							location.setRoomId(locationObj.getString("RoomId"));
						}
						if (!locationObj.isNull("Image")) {
							location.setImage(locationObj.getString("Image"));
						}
						// if (!locationObj.isNull("Address")) {
						// location.setAddress(locationObj
						// .getString("Address"));
						// }
						if (!locationObj.isNull("BuildingName")) {
							location.setBuildingName(locationObj
									.getString("BuildingName"));
						}
						if (!locationObj.isNull("Label")) {
							location.setLabel(locationObj.getString("Label"));
						}
					}
				}
				if (!courseObj.isNull("FinalGrade")) {
					final Grade grade = new Grade();
					course.addGrade(grade);
					final JSONObject gradeObj = courseObj
							.getJSONObject("FinalGrade");
					if (!gradeObj.isNull("Label")) {
						grade.setLabel(gradeObj.getString("Label"));
					}
					if (!gradeObj.isNull("Date")) {
						grade.setUpdatedDate(Utils.convertJsonDate(gradeObj
								.getString("Date")));
					}
					if (!gradeObj.isNull("Value")) {
						grade.setGrade(gradeObj.getString("Value"));
					}
				}
				final JSONArray jGrade = courseObj
						.getJSONArray("MidtermGrades");
				for (int k = 0; k < jGrade.length(); k++) {
					final Grade grade = new Grade();
					course.addGrade(grade);
					final JSONObject gradeObj = jGrade.getJSONObject(k);
					if (!gradeObj.isNull("Label")) {
						grade.setLabel(gradeObj.getString("Label"));
					}
					if (!gradeObj.isNull("Date")) {
						grade.setUpdatedDate(Utils.convertJsonDate(gradeObj
								.getString("Date")));
					}
					if (!gradeObj.isNull("Value")) {
						grade.setGrade(gradeObj.getString("Value"));
					}
				}
			}
		}
		return terms;
	}
}
