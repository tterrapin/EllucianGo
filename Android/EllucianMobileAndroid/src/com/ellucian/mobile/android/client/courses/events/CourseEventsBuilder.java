package com.ellucian.mobile.android.client.courses.events;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;

public class CourseEventsBuilder extends ContentProviderOperationBuilder<CourseEventsResponse>{
		String courseId;

		public CourseEventsBuilder(Context context, String courseId) {
			super(context);
			this.courseId = courseId;
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseEventsResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			
			// delete current contents in database
			batch.add(ContentProviderOperation.newDelete(CourseEvents.CONTENT_URI)
					.withSelection(CourseCourses.COURSE_ID + "=?", new String[] {courseId})
					.build());
			
			for (Event event : model.events) {
				batch.add(ContentProviderOperation
						.newInsert(CourseEvents.CONTENT_URI)
						.withValue(CourseCourses.COURSE_ID, courseId)
						.withValue(CourseEvents.EVENT_TITLE, event.title)
						.withValue(CourseEvents.EVENT_DESCRIPTION, event.description)
						.withValue(CourseEvents.EVENT_START, event.startDate)
						.withValue(CourseEvents.EVENT_END, event.endDate)
						.withValue(CourseEvents.EVENT_LOCATION, event.location)
						.withValue(CourseEvents.EVENT_ALL_DAY, event.isAllDay)
						.build());							
			}

			return batch;
			
		}
}
