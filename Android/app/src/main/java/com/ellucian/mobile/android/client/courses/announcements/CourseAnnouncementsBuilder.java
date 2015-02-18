package com.ellucian.mobile.android.client.courses.announcements;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;

public class CourseAnnouncementsBuilder extends ContentProviderOperationBuilder<CourseAnnouncementsResponse>{
		String courseId;

		public CourseAnnouncementsBuilder(Context context, String courseId) {
			super(context);
			this.courseId = courseId;
		}
		
		@Override
		public ArrayList<ContentProviderOperation> buildOperations(CourseAnnouncementsResponse model) {
			final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			
			// delete current contents in database
			batch.add(ContentProviderOperation.newDelete(CourseAnnouncements.CONTENT_URI)
					.withSelection(CourseCourses.COURSE_ID + "=?", new String[] {courseId})
					.build());
			
			for (Item item : model.items) {
				batch.add(ContentProviderOperation
						.newInsert(CourseAnnouncements.CONTENT_URI)
						.withValue(CourseCourses.COURSE_ID, courseId)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_TITLE, item.title)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_CONTENT, item.content)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_DATE, item.date)
						.withValue(CourseAnnouncements.ANNOUNCEMENT_URL, item.website)
						.build());							
			}

			return batch;
			
		}
}
