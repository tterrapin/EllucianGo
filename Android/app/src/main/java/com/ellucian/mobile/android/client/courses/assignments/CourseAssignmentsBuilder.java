/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.assignments;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;

public class CourseAssignmentsBuilder extends ContentProviderOperationBuilder<CourseAssignmentsResponse>{
		private final String courseId;

		public CourseAssignmentsBuilder(Context context, String courseId) {
			super(context);
			this.courseId = courseId;
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseAssignmentsResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			
			// delete current contents in database
			if (!TextUtils.isEmpty(courseId)) {
				batch.add(ContentProviderOperation.newDelete(CourseAssignments.CONTENT_URI)
						.withSelection(CourseCourses.COURSE_ID + "=?", new String[] {courseId})
						.build());
			} else {
				batch.add(ContentProviderOperation.newDelete(CourseAssignments.CONTENT_URI).build());
			}
			
			for (Assignment assignment : model.assignments) {
                String sectionName = "";
                if (!TextUtils.isEmpty(assignment.courseName)) {
                    sectionName += assignment.courseName;

                    if (!TextUtils.isEmpty(assignment.courseSectionNumber)) {
                        sectionName += "-" + assignment.courseSectionNumber;
                    }
                }
				String sectionId = this.courseId;
				if(assignment.sectionId != null) {
					sectionId = assignment.sectionId;
				}
				batch.add(ContentProviderOperation
						.newInsert(CourseAssignments.CONTENT_URI)
						.withValue(CourseCourses.COURSE_ID, sectionId)
						.withValue(CourseAssignments.ASSIGNMENT_NAME, assignment.name)
						.withValue(CourseAssignments.ASSIGNMENT_DUE, assignment.dueDate)
						.withValue(CourseAssignments.ASSIGNMENT_DESCRIPTION, assignment.description)
						.withValue(CourseAssignments.ASSIGNMENT_URL, assignment.url)
                        .withValue(CourseAssignments.ASSIGNMENT_SECTION_NAME, sectionName)
						.build());							
			}

			return batch;
			
		}
}
