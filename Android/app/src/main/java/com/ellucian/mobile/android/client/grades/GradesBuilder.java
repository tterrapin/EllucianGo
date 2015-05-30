/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.grades;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTerms;
import com.ellucian.mobile.android.provider.EllucianContract.Grades;
import com.ellucian.mobile.android.provider.EllucianContract.GradesCourses;

public class GradesBuilder extends ContentProviderOperationBuilder<GradesResponse> {

	public GradesBuilder(Context context) {
		super(context);
	}
	
	/* (non-Javadoc)
	 * @see com.ellucian.mobile.android.client.ContentProviderOperationBuilder#buildOperations(java.lang.Object)
	 */
	@Override
	public ArrayList<ContentProviderOperation> buildOperations(GradesResponse response) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		// delete current contents in database
		batch.add(ContentProviderOperation.newDelete(GradeTerms.CONTENT_URI).build());
		batch.add(ContentProviderOperation.newDelete(GradesCourses.CONTENT_URI).build());
		batch.add(ContentProviderOperation.newDelete(Grades.CONTENT_URI).build());
		
		
		// add update statements to the batch
		for (Term term : response.terms) {
			batch.add(ContentProviderOperation
					.newInsert(GradeTerms.CONTENT_URI)
					.withValue(GradeTerms.TERM_ID, term.id)
					.withValue(GradeTerms.TERM_NAME, term.name)
					.withValue(GradeTerms.TERM_START_DATE, (term.startDate))
					.withValue(GradeTerms.TERM_END_DATE, (term.endDate))
					.build());
			for (Section course : term.sections) {
				String uniqueCourseId = term.id + " - " + course.sectionId;
				batch.add(ContentProviderOperation
						.newInsert(GradesCourses.CONTENT_URI)
						.withValue(GradesCourses.COURSE_ID, uniqueCourseId)
						.withValue(GradesCourses.COURSE_ERP_ID, course.sectionId)
						.withValue(GradesCourses.COURSE_DESCRIPTION, course.courseName)
						.withValue(GradesCourses.COURSE_CREDIT_HOURS, course.creditHours)
						.withValue(GradesCourses.COURSE_SECTION, course.courseSectionNumber)
						.withValue(GradesCourses.COURSE_TITLE, course.sectionTitle)
						.withValue(GradeTerms.TERM_ID, term.id)
						.build());
				// Now process grades for the course
				for(Grade grade : course.grades) {
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
