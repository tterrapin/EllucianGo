/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.full;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.client.courses.CoursesResponse;
import com.ellucian.mobile.android.client.courses.Section;
import com.ellucian.mobile.android.client.courses.Term;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseInstructors;
import com.ellucian.mobile.android.provider.EllucianContract.CourseMeetings;
import com.ellucian.mobile.android.provider.EllucianContract.CoursePatterns;
import com.ellucian.mobile.android.provider.EllucianContract.CourseTerms;

public class FullScheduleBuilder extends ContentProviderOperationBuilder<CoursesResponse>{

		public FullScheduleBuilder(Context context) {
			super(context);
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CoursesResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			
			
			// delete current contents in database
			batch.add(ContentProviderOperation.newDelete(CourseTerms.CONTENT_URI).build());
			batch.add(ContentProviderOperation.newDelete(CourseCourses.CONTENT_URI).build());
			batch.add(ContentProviderOperation.newDelete(CourseInstructors.CONTENT_URI).build());
			batch.add(ContentProviderOperation.newDelete(CoursePatterns.CONTENT_URI).build());
			batch.add(ContentProviderOperation.newDelete(CourseMeetings.CONTENT_URI).build());
			if(model.terms != null) {
				for (Term term : model.terms) {
					batch.add(ContentProviderOperation
							.newInsert(CourseTerms.CONTENT_URI)
							.withValue(CourseTerms.TERM_ID, term.id)
							.withValue(CourseTerms.TERM_NAME, term.name)
							.withValue(CourseTerms.TERM_START_DATE, term.startDate)
							.withValue(CourseTerms.TERM_END_DATE, term.endDate)
							.build());
					for (Section section : term.sections) {
						batch.add(ContentProviderOperation
								.newInsert(CourseCourses.CONTENT_URI)
								.withValue(CourseCourses.COURSE_ID, section.sectionId)
								.withValue(CourseTerms.TERM_ID, term.id)
								.withValue(CourseCourses.COURSE_NAME, section.courseName)
								.withValue(CourseCourses.COURSE_TITLE, section.sectionTitle)
								.withValue(CourseCourses.COURSE_SECTION_NUMBER, section.courseSectionNumber)
								.withValue(CourseCourses.COURSE_IS_INSTRUCTOR, section.isInstructor ? 1 : 0)
								.build());
		
					}
				}
			}
				
			return batch;
			
		}
}
