package com.ellucian.mobile.android.grades;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ellucian.mobile.android.Utils;

public class GradesParser {
	// public static List<Term> parse(String jString) throws JSONException {
	// return parse(jString, null, null);
	// }
	public static GradesData parse(String newjString)
			throws JSONException {
		final GradesData gd = new GradesData();
		// GradesAsOf
		final JSONObject jObject = new JSONObject(newjString);
		gd.setGradesAsOf(jObject.getString("GradesAsOf"));
		List<Term> terms = parseJson(newjString);
//		if (cachedjString != null) {
//			List<Term> cachedTerms = parseJson(cachedjString);
//			for(Term t : terms) {
//				if(!cachedTerms.contains(t)) {
//					cached
//				}
//			}
//			gd.setTerms(cachedTerms);
//		} else {
			gd.setTerms(terms);
//		}
		return gd;
	}

	public static List<Term> parseJson(final String jString)
			throws JSONException {
		final JSONObject jObject = new JSONObject(jString);
		List<Term> terms = new ArrayList<Term>();
		final JSONArray jTerms = jObject.optJSONArray("Terms");
		if (jTerms != null) {
			
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
				final JSONArray jCourses = termObj.getJSONArray("Grades");
				for (int j = 0; j < jCourses.length(); j++) {
					final Course course = new Course();
					term.addCourse(course);
					final JSONObject courseObj = jCourses.getJSONObject(j);
					if (!courseObj.isNull("CourseName")) {
						course.setName(courseObj.getString("CourseName"));
					}
					if (!courseObj.isNull("SectionTitle")) {
						course.setSectionTitle(courseObj
								.getString("SectionTitle"));
					}
					if (!courseObj.isNull("SectionId")) {
						course.setCourseId(courseObj.getString("SectionId"));
					}
					if (!courseObj.isNull("FinalGrade")) {
						final Grade grade = new Grade();
						course.addFinalGrade(grade);
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
		}
		return terms;
	}
}
