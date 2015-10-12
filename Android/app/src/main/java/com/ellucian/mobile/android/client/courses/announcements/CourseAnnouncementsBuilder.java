/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.announcements;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;

import java.util.ArrayList;

public class CourseAnnouncementsBuilder extends ContentProviderOperationBuilder<CourseAnnouncementsResponse>{
		private final String courseId;

		public CourseAnnouncementsBuilder(Context context, String courseId) {
			super(context);
			this.courseId = courseId;
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseAnnouncementsResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

            // delete current contents in database
            if (!TextUtils.isEmpty(courseId)) {
                batch.add(ContentProviderOperation.newDelete(CourseAnnouncements.CONTENT_URI)
                        .withSelection(CourseCourses.COURSE_ID + "=?", new String[] {courseId})
                        .build());
            } else {
                batch.add(ContentProviderOperation.newDelete(CourseAnnouncements.CONTENT_URI).build());
            }
			
			for (Item item : model.items) {
                String sectionName = "";
                if (!TextUtils.isEmpty(item.courseName)) {
                    sectionName += item.courseName;

                    if (!TextUtils.isEmpty(item.courseSectionNumber)) {
                        sectionName += "-" + item.courseSectionNumber;
                    }
                }
				String sectionId = this.courseId;
				if(item.sectionId != null) {
					sectionId = item.sectionId;
				}
				batch.add(ContentProviderOperation
						.newInsert(CourseAnnouncements.CONTENT_URI)
						.withValue(CourseCourses.COURSE_ID, sectionId)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_TITLE, item.title)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_CONTENT, item.content)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_DATE, item.date)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_URL, item.website)
                        .withValue(CourseAnnouncements.ANNOUNCEMENT_SECTION_NAME, sectionName)
						.build());							
			}

			return batch;
			
		}
}
