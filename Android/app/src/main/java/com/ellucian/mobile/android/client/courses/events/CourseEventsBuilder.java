/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.events;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;

import java.util.ArrayList;

public class CourseEventsBuilder extends ContentProviderOperationBuilder<CourseEventsResponse>{
		private final String courseId;

		public CourseEventsBuilder(Context context, String courseId) {
			super(context);
			this.courseId = courseId;
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseEventsResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

            // delete current contents in database
            if (!TextUtils.isEmpty(courseId)) {
                batch.add(ContentProviderOperation.newDelete(CourseEvents.CONTENT_URI)
                        .withSelection(CourseCourses.COURSE_ID + "=?", new String[] {courseId})
                        .build());
            } else {
                batch.add(ContentProviderOperation.newDelete(CourseEvents.CONTENT_URI).build());
            }
			
			for (Event event : model.events) {
                String sectionName = "";
                if (!TextUtils.isEmpty(event.courseName)) {
                    sectionName += event.courseName;

                    if (!TextUtils.isEmpty(event.courseSectionNumber)) {
                        sectionName += "-" + event.courseSectionNumber;
                    }
                }
				String sectionId = this.courseId;
				if(event.sectionId != null) {
					sectionId = event.sectionId;
				}
				batch.add(ContentProviderOperation
						.newInsert(CourseEvents.CONTENT_URI)
						.withValue(CourseCourses.COURSE_ID, sectionId)
						.withValue(CourseEvents.EVENT_TITLE, event.title)
						.withValue(CourseEvents.EVENT_DESCRIPTION, event.description)
						.withValue(CourseEvents.EVENT_START, event.startDate)
						.withValue(CourseEvents.EVENT_END, event.endDate)
						.withValue(CourseEvents.EVENT_LOCATION, event.location)
						.withValue(CourseEvents.EVENT_ALL_DAY, event.isAllDay)
                        .withValue(CourseEvents.EVENT_SECTION_NAME, sectionName)
						.build());							
			}

			return batch;
			
		}
}
