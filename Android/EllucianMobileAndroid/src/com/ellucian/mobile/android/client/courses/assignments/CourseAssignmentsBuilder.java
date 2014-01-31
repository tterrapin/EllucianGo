package com.ellucian.mobile.android.client.courses.assignments;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;

public class CourseAssignmentsBuilder extends ContentProviderOperationBuilder<CourseAssignmentsResponse>{
		String courseId;

		public CourseAssignmentsBuilder(Context context, String courseId) {
			super(context);
			this.courseId = courseId;
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseAssignmentsResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			
			// delete current contents in database
			batch.add(ContentProviderOperation.newDelete(CourseAssignments.CONTENT_URI)
					.withSelection(CourseCourses.COURSE_ID + "=?", new String[] {courseId})
					.build());
			
			for (Assignment assignment : model.assignments) {
				batch.add(ContentProviderOperation
						.newInsert(CourseAssignments.CONTENT_URI)
						.withValue(CourseCourses.COURSE_ID, courseId)
						.withValue(CourseAssignments.ASSIGNMENT_NAME, assignment.name)
						.withValue(CourseAssignments.ASSIGNMENT_DUE, assignment.dueDate)
						.withValue(CourseAssignments.ASSIGNMENT_DESCRIPTION, assignment.description)
						.withValue(CourseAssignments.ASSIGNMENT_URL, assignment.url)
						.build());							
			}

			return batch;
			
		}
}
