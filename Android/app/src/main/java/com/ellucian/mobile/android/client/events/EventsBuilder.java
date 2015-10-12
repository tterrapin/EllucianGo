/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.events;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.Events;
import com.ellucian.mobile.android.provider.EllucianContract.EventsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.EventsEventsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;

public class EventsBuilder extends ContentProviderOperationBuilder<EventsResponse> {
	private final String module;
	// TODO - Remove this when the events code gets fixed
	//private final SimpleDateFormat eventsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	public EventsBuilder(Context context, String module) {
		super(context);
		this.module = module;
	}
	
	@Override
	public ArrayList<ContentProviderOperation> buildOperations(EventsResponse model) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// delete current contents in database
		batch.add(ContentProviderOperation.newDelete(Events.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		batch.add(ContentProviderOperation.newDelete(EventsCategories.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());		
		batch.add(ContentProviderOperation.newDelete(EventsEventsCategories.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		
		final HashMap<String, String> categories = new HashMap<String, String>();
		
		String uniquePrefix = System.currentTimeMillis() + "_";
		long uid = 0;
			
		
		for (Event event : model.events) {			
			
			String uniqueEventId = uniquePrefix + ++uid;
			String uniqueCategoryId = uniquePrefix + "c" + ++uid;
			
			String categoriesString = "";
			int length = event.categories.length;
			for (int i = 0; i < length; i ++) {
				String currentCategory = event.categories[i].name;
				categoriesString += currentCategory;
				if (i != (length -1)) {
					categoriesString += ", ";
				}
				
				if (!categories.containsKey(currentCategory)) {
					categories.put(currentCategory, uniqueCategoryId);
					batch.add(ContentProviderOperation
							.newInsert(EventsCategories.CONTENT_URI)
							.withValue(Modules.MODULES_ID, module)
							.withValue(EventsCategories.EVENTS_CATEGORY_ID, uniqueCategoryId)
							.withValue(EventsCategories.EVENTS_CATEGORY_NAME, currentCategory)
							.build());
				}
				
				
			}
			
			//Log.d("event categories", categories);
			/*
			String categoryName = "";
			if (event.categories != null && event.categories.length > 0) {
				categoryName = event.categories[0].name;	
			}
			*/
			
			batch.add(ContentProviderOperation
					.newInsert(Events.CONTENT_URI)
					.withValue(EventsCategories.EVENTS_CATEGORY_ID, categories.get(event.categories[0].name))
					.withValue(Modules.MODULES_ID, module)
					.withValue(Events.EVENTS_ID, uniqueEventId)
					.withValue(Events.EVENTS_UID, event.uid)
					.withValue(Events.EVENTS_TITLE, event.summary)
					.withValue(Events.EVENTS_DESCRIPTION, event.description)
					.withValue(Events.EVENTS_LOCATION, event.location)
					.withValue(Events.EVENTS_DURATION, event.duration)
					.withValue(Events.EVENTS_CONTACT, event.contact)
					.withValue(Events.EVENTS_EMAIL, event.email)
					.withValue(Events.EVENTS_CATEGORIES, categoriesString)
					.withValue(Events.EVENTS_START, event.start)
					.withValue(Events.EVENTS_END, event.end)
					.withValue(Events.EVENTS_ALL_DAY, event.allDay ? 1 : 0)
					.build());
			
			// Add content provider for insert.
			for (Category category : event.categories) {
				batch.add(ContentProviderOperation
						.newInsert(EventsEventsCategories.CONTENT_URI)
						.withValue(Modules.MODULES_ID, module)
						.withValue(Events.EVENTS_ID, uniqueEventId)
						.withValue(EventsCategories.EVENTS_CATEGORY_ID, categories.get(category.name))
						.build());
			}
			
		}
			
		return batch;
	}
}
