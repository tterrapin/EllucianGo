/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.overview;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.client.grades.Grade;
import com.ellucian.mobile.android.client.grades.GradesResponse;
import com.ellucian.mobile.android.client.grades.Section;
import com.ellucian.mobile.android.client.grades.Term;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTerms;
import com.ellucian.mobile.android.provider.EllucianContract.Grades;
import com.ellucian.mobile.android.provider.EllucianContract.GradesCourses;

public class CourseGradesBuilder extends ContentProviderOperationBuilder<GradesResponse> {
	private final Context context;
	
	public CourseGradesBuilder(Context context) {
		super(context);
		this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see com.ellucian.mobile.android.client.ContentProviderOperationBuilder#buildOperations(java.lang.Object)
	 */
	@Override
	public ArrayList<ContentProviderOperation> buildOperations(GradesResponse response) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		if (response.terms != null && response.terms.length > 0) {
			Term term = response.terms[0];
			if (term.sections != null && term.sections.length > 0) {
				Section section = term.sections[0];
				
				String uniqueCourseId = term.id + " - " + section.sectionId;
				
				String uniqueIdSelection = GradesCourses.COURSE_ID + " = ?";
				String[] uniqueIdArgs = new String[] { uniqueCourseId };
				
				// delete specific course in courses tables
				batch.add(ContentProviderOperation
						  .newDelete(Grades.CONTENT_URI)
						  .withSelection(uniqueIdSelection, uniqueIdArgs)
						  .build());
				batch.add(ContentProviderOperation
						  .newDelete(GradesCourses.CONTENT_URI)
						  .withSelection(uniqueIdSelection, uniqueIdArgs)
						  .build());
				
				// add update statements to the batch
				batch.add(ContentProviderOperation
						.newInsert(GradesCourses.CONTENT_URI)
						.withValue(GradesCourses.COURSE_ID, uniqueCourseId)
						.withValue(GradesCourses.COURSE_ERP_ID, section.sectionId)
						.withValue(GradesCourses.COURSE_DESCRIPTION, section.courseName)
						.withValue(GradesCourses.COURSE_CREDIT_HOURS, section.creditHours)
						.withValue(GradesCourses.COURSE_SECTION, section.courseSectionNumber)
						.withValue(GradesCourses.COURSE_TITLE, section.sectionTitle)
						.withValue(GradeTerms.TERM_ID, term.id)
						.build());
				// Now process grades for the course
				for(Grade grade : section.grades) {
					batch.add(ContentProviderOperation
							.newInsert(Grades.CONTENT_URI)
							.withValue(GradesCourses.COURSE_ID, uniqueCourseId)
							.withValue(Grades.GRADE_NAME, grade.name)
							.withValue(Grades.GRADE_TYPE, grade.type)
							.withValue(Grades.GRADE_UPDATED, grade.updated)
							.withValue(Grades.GRADE_VALUE, grade.value)
							.build());
				}
			}
		}
			
		return batch;
	}
	

}
