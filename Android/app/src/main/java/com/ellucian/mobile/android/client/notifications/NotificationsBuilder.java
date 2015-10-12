/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.notifications;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;

import java.util.ArrayList;

public class NotificationsBuilder extends ContentProviderOperationBuilder<NotificationsResponse> {

	public NotificationsBuilder(Context context) {
		super(context);
	}
	
	@Override
	public ArrayList<ContentProviderOperation> buildOperations(NotificationsResponse model) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<>();
		
		// delete current contents in database
		batch.add(ContentProviderOperation.newDelete(Notifications.CONTENT_URI).build());
		
		//String uniquePrefix = System.currentTimeMillis() + "_";
		//long uid = 0;
		
		for (Notification notification : model.notifications) {
			
			String id;
			if (!TextUtils.isEmpty(notification.id)) {
				id = notification.id;
			} else {
				id = notification.title;
			}
			String statuses = null;
			if (notification.statuses != null && notification.statuses.length > 0) {
				statuses = TextUtils.join(",", notification.statuses);
			}
			
			batch.add(ContentProviderOperation
					.newInsert(Notifications.CONTENT_URI)
					.withValue(Notifications.NOTIFICATIONS_ID, id)
					.withValue(Notifications.NOTIFICATIONS_TITLE, notification.title)
					.withValue(Notifications.NOTIFICATIONS_DETAILS, notification.description)
					.withValue(Notifications.NOTIFICATIONS_HYPERLINK, notification.hyperlink)
					.withValue(Notifications.NOTIFICATIONS_LINK_LABEL, notification.linkLabel)
					.withValue(Notifications.NOTIFICATIONS_DATE, notification.noticeDate)
					.withValue(Notifications.NOTIFICATIONS_SOURCE, notification.source)
					.withValue(Notifications.NOTIFICATIONS_DISPATCH_DATE, notification.dispatchDate)
					.withValue(Notifications.NOTIFICATIONS_MOBILE_HEADLINE, notification.mobileHeadline)
					.withValue(Notifications.NOTIFICATIONS_EXPIRES, notification.expires)
					.withValue(Notifications.NOTIFICATIONS_PUSH, notification.push ? 1 : 0)
					.withValue(Notifications.NOTIFICATIONS_MODULE, notification.module ? 1 : 0)
					.withValue(Notifications.NOTIFICATIONS_STICKY, notification.sticky ? 1 : 0)
					.withValue(Notifications.NOTIFICATIONS_STATUSES, statuses)
					.build());
		}
			
		return batch;
	}
}
