/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.news;

import android.database.Cursor;
import android.os.Bundle;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.News;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;

public class NewsListFragment extends EllucianDefaultListFragment {
	
	public NewsListFragment() {		
	}
	
	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		Bundle bundle = new Bundle();
		
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);
		
		String title = cursor.getString(cursor.getColumnIndex(News.NEWS_TITLE));
		String content = cursor.getString(cursor.getColumnIndex(News.NEWS_CONTENT));
		String strippedContent = cursor.getString(cursor.getColumnIndex(News.NEWS_LIST_DESCRIPTION));
		String link = cursor.getString(cursor.getColumnIndex(News.NEWS_LINK));
		String logo = cursor.getString(cursor.getColumnIndex(News.NEWS_LOGO));
		String date = cursor.getString(cursor.getColumnIndex(News.NEWS_POST_DATE));
		
		bundle.putString(Extra.TITLE, title);
		bundle.putString(Extra.CONTENT, content);
		
		
		if (strippedContent != null) {
			bundle.putString(Extra.LIST_DESCRIPTION, strippedContent);
		}
		if (link != null) {
			bundle.putString(Extra.LINK, link);
		}
		if (logo != null) {
			bundle.putString(Extra.LOGO, logo);
		}
		if(date != null) {
			Date convDate = CalendarUtils.parseFromUTC(date);

			String dateString = CalendarUtils.getMonthDateString(getActivity(), convDate);

			bundle.putString(Extra.DATE, dateString);					
		}
	
		
		return bundle;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return NewsDetailFragment.class;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return NewsDetailActivity.class;	
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("News List", getEllucianActivity().moduleName);
	}

}
