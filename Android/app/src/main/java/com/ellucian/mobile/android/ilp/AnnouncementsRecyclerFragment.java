/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultRecyclerFragment;
import com.ellucian.mobile.android.client.services.CourseAnnouncementsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class AnnouncementsRecyclerFragment extends EllucianDefaultRecyclerFragment implements
	LoaderManager.LoaderCallbacks<Cursor>{

	private IlpListActivity activity;
	private IlpSectionedRecyclerAdapter adapter;

    public AnnouncementsRecyclerFragment(){
    }

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		activity = (IlpListActivity) getActivity();
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, AnnouncementsRecyclerFragment.this);

        String ilpUrl = getArguments().getString(Extra.COURSES_ILP_URL);
        Intent intent = new Intent(activity, CourseAnnouncementsIntentService.class);
        intent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);

        activity.startService(intent);
    }

    @Override
    public Bundle buildDetailBundle(Object... objects) {
		IlpItemHolder infoHolder = (IlpItemHolder) objects[0];

    	Bundle bundle = new Bundle();

		bundle.putString(Extra.TITLE, infoHolder.title);
		bundle.putString(Extra.DATE, infoHolder.displayDate);
		bundle.putString(Extra.CONTENT, infoHolder.content);
        bundle.putString(Extra.HEADER_SECTION_NAME, infoHolder.sectionName);
		bundle.putString(Extra.LINK, infoHolder.url);
        bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ANNOUNCEMENTS);

		return bundle;
	}

	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return IlpDetailFragment.class;
	}

	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return IlpDetailActivity.class;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		return new CursorLoader(activity, CourseAnnouncements.CONTENT_URI, null, null, null, CourseAnnouncements.DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		ArrayList<IlpItemHolder> eventsList = buildAnnouncementsList(cursor);
		buildAdapters(eventsList);

        setAdapter(adapter);

        if (dualPane) {

            if (getArguments().getBoolean(IlpListActivity.SHOW_DETAIL, false)) {
                getArguments().remove(IlpListActivity.SHOW_DETAIL);
                int position = getArguments().getInt(IlpListActivity.SELECTED_INDEX);
                Object item = adapter.getItem(position + 1);
                detailBundle = buildDetailBundle(item);
                recyclerView.setSelectedIndex(position + 1);
            }

            showCurrentSelected();
        }
			
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
        setAdapter(null);
	}

    private void showCurrentSelected() {
        if (recyclerView.getSelectedIndex() == -1 && adapter != null && adapter.getItemCount() > 0) {
            Object itemHolder = adapter.getItem(1);
            detailBundle = buildDetailBundle(itemHolder);
            recyclerView.setSelectedIndex(1);
        }

        if (adapter != null) {
            adapter.setSelectedIndex(recyclerView.getSelectedIndex());
        }

        if (detailBundle != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    showDetails(recyclerView.getSelectedIndex());
                    recyclerView.smoothScrollToPosition(recyclerView.getSelectedIndex());
                }
            });
        }
    }
	
	private ArrayList<IlpItemHolder> buildAnnouncementsList(Cursor cursor) {
		ArrayList<IlpItemHolder> announcementsList = new ArrayList<>();
		
		if (cursor.moveToFirst()) {
			do {

                String sectionId = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_ID));
                String sectionName = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_SECTION_NAME));
                String title = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_TITLE));
                String dateString = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_DATE));
                String content = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_CONTENT));
                String url = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_URL));

                String displayDate;
                if (!TextUtils.isEmpty(dateString)) {
                    Date date = CalendarUtils.parseFromUTC(dateString);
                    displayDate = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
                } else {
                    displayDate = "";
                }

                AnnouncementItemHolder infoHolder = new AnnouncementItemHolder(sectionId, sectionName, title,
                        dateString, displayDate, content, url);

                announcementsList.add(infoHolder);
				
			} while (cursor.moveToNext());
		}

        Collections.sort(announcementsList);
        Collections.reverse(announcementsList);
		
		return announcementsList;
	}
	
	private void buildAdapters(ArrayList<IlpItemHolder> announcementsList) {
        adapter = new IlpSectionedRecyclerAdapter(activity);
		if (!announcementsList.isEmpty()) {
            IlpHeaderHolder headerHolder = new IlpHeaderHolder(getString(R.string.ilp_announcements), null);
            adapter.addSection(headerHolder, announcementsList);
        }
	}

    @Override
    public void onStart() {
        super.onStart();
        sendView("ILP Announcements List", getEllucianActivity().moduleName);
    }
}
