/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.news;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.News;
import com.ellucian.mobile.android.provider.EllucianContract.NewsCategories;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.util.ArrayList;
import java.util.HashMap;
public class NewsBuilder extends ContentProviderOperationBuilder<NewsResponse> {
	private final String module;
	
	public NewsBuilder(Context context, String module) {
		super(context);
		this.module = module;
	}
	
	@Override
	public ArrayList<ContentProviderOperation> buildOperations(NewsResponse model) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<>();
		
		// TODO: Only delete the data specific to a module
		batch.add(ContentProviderOperation.newDelete(News.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		batch.add(ContentProviderOperation.newDelete(NewsCategories.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		
		String uniquePrefix = System.currentTimeMillis() + "_";
		long uid = 0;
		
		final HashMap<String, String> categories = new HashMap<String, String>();
		
		for (Entry entry : model.entries) {
			String uniqueCategoryId = uniquePrefix + ++uid;
			if (!categories.containsKey(entry.feedName)) {
				categories.put(entry.feedName, uniqueCategoryId);
				batch.add(ContentProviderOperation
						.newInsert(NewsCategories.CONTENT_URI)
						.withValue(NewsCategories.NEWS_CATEGORY_ID, uniqueCategoryId)
						.withValue(NewsCategories.NEWS_CATEGORY_NAME, entry.feedName)
						.withValue(Modules.MODULES_ID, module)
						.build());
			}
			
			String cleanContent = android.text.TextUtils.isEmpty(entry.content) ? entry.content : Jsoup.clean(entry.content, Whitelist.none());
			String link = entry.link.length > 0 ? entry.link[0] : null;
			batch.add(ContentProviderOperation
					.newInsert(News.CONTENT_URI)
					.withValue(NewsCategories.NEWS_CATEGORY_ID, categories.get(entry.feedName))
					.withValue(News.NEWS_ENTRY_ID, entry.entryId)
					.withValue(News.NEWS_FEED_NAME, entry.feedName)
					.withValue(News.NEWS_TITLE, entry.title)
					.withValue(News.NEWS_CONTENT, entry.content)
					.withValue(News.NEWS_LIST_DESCRIPTION, cleanContent)
					.withValue(News.NEWS_LINK, link)
					.withValue(News.NEWS_LOGO, entry.logo)
					.withValue(News.NEWS_POST_DATE, entry.postDate)
					.withValue(Modules.MODULES_ID, module)
					.build());
		}
			
		return batch;
	}
}
