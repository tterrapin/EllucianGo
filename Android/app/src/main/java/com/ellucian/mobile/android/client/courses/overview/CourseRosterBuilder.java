/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.overview;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseRoster;

public class CourseRosterBuilder extends ContentProviderOperationBuilder<CourseRosterResponse>{

		public CourseRosterBuilder(Context context) {
			super(context);
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseRosterResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			
			// delete current contents in database
			batch.add(ContentProviderOperation.newDelete(CourseRoster.CONTENT_URI).build());
			
			for (RosterStudent student : model.activeStudents) {
				batch.add(ContentProviderOperation
						.newInsert(CourseRoster.CONTENT_URI)
						.withValue(CourseRoster.ROSTER_STUDENT_ID, student.id)
						.withValue(CourseRoster.ROSTER_COURSE_ID, model.sectionId)
						.withValue(CourseRoster.ROSTER_FORMATTED_NAME, student.name)
						.withValue(CourseRoster.ROSTER_FIRST_NAME, student.firstName)
						.withValue(CourseRoster.ROSTER_MIDDLE_NAME, student.middleName)
						.withValue(CourseRoster.ROSTER_LAST_NAME, student.lastName)
						.withValue(CourseRoster.ROSTER_PHOTO, student.photo)
						.build());							
			}

			return batch;
			
		}
}
