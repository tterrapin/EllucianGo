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
import com.ellucian.mobile.android.client.services.CourseEventsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class EventsRecyclerFragment extends EllucianDefaultRecyclerFragment implements
	LoaderManager.LoaderCallbacks<Cursor>{

	private IlpListActivity activity;
	private IlpSectionedRecyclerAdapter adapter;

    public EventsRecyclerFragment(){
    }

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		activity = (IlpListActivity) getActivity();
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, EventsRecyclerFragment.this);

        String ilpUrl = getArguments().getString(Extra.COURSES_ILP_URL);
        Intent intent = new Intent(activity, CourseEventsIntentService.class);
        intent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);

        activity.startService(intent);
    }

    @Override
    public Bundle buildDetailBundle(Object... objects) {
		EventItemHolder infoHolder = (EventItemHolder) objects[0];

    	Bundle bundle = new Bundle();
		
		bundle.putString(Extra.TITLE, infoHolder.title);
		bundle.putString(Extra.DATE, infoHolder.displayDate);
		bundle.putString(Extra.CONTENT, infoHolder.content);
		bundle.putString(Extra.LINK, infoHolder.url);
        bundle.putString(Extra.HEADER_SECTION_NAME, infoHolder.sectionName);
        bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_EVENTS);
        // events need extra care for adding to native calendar
        if (!TextUtils.isEmpty(infoHolder.startDate)) {
            Date dateStart = CalendarUtils.parseFromUTC(infoHolder.startDate);
            Long longStart = dateStart.getTime();
            bundle.putLong(Extra.START, longStart);
            bundle.putString(Extra.LOCATION, infoHolder.location);
            if (infoHolder.allDay || infoHolder.endDate == null) {
                // same handling as EventsListFragment
                bundle.putLong(Extra.END, -1L);
            } else {
                Date dateEnd = CalendarUtils.parseFromUTC(infoHolder.endDate);
                Long longEnd = dateEnd.getTime();
                bundle.putLong(Extra.END, longEnd);
            }
        }

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

		return new CursorLoader(activity, CourseEvents.CONTENT_URI, null, null, null, EllucianContract.CourseEvents.DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		ArrayList<IlpItemHolder> eventsList = buildEventsList(cursor);
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
	
	private ArrayList<IlpItemHolder> buildEventsList(Cursor cursor) {
		ArrayList<IlpItemHolder> eventsList = new ArrayList<IlpItemHolder>();
		
		if (cursor.moveToFirst()) {
			do {

                String sectionId = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_ID));
                String sectionName = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_SECTION_NAME));
                String title = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_DESCRIPTION));
                String location = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_LOCATION));
                String startDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_START));
                String endDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_END));
                String allDayString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_ALL_DAY));
                boolean allDay = Boolean.parseBoolean(allDayString);

                // Depending on the type of the event and what info is present the display will change
                String displayDate;
                if (!TextUtils.isEmpty(startDateString)) {
                    Date startDate = CalendarUtils.parseFromUTC(startDateString);

                    if (allDay) {
                        displayDate = getString(R.string.date_all_day_event_format,
                                CalendarUtils.getDefaultDateString(activity, startDate));
                    } else if (!TextUtils.isEmpty(endDateString)) {
                        Date endDate = CalendarUtils.parseFromUTC(endDateString);

                        if (CalendarUtils.getDefaultDateString(activity, startDate)
                                .equals(CalendarUtils.getDefaultDateString(activity, endDate))) {

                            displayDate = getString(R.string.date_time_to_time_format,
                                    CalendarUtils.getDefaultDateString(activity, startDate),
                                    CalendarUtils.getDefaultTimeString(activity, startDate),
                                    CalendarUtils.getDefaultTimeString(activity, endDate));
                        } else {
                            displayDate = getString(R.string.date_time_to_date_time_format,
                                    CalendarUtils.getDefaultDateString(activity, startDate),
                                    CalendarUtils.getDefaultTimeString(activity, startDate),
                                    CalendarUtils.getDefaultDateString(activity, endDate),
                                    CalendarUtils.getDefaultTimeString(activity, endDate));
                        }

                    } else {
                        displayDate = getString(R.string.date_time_format,
                                CalendarUtils.getDefaultDateString(activity, startDate),
                                CalendarUtils.getDefaultTimeString(activity, startDate));
                    }
                } else {
                    displayDate = getString(R.string.unavailable);
                }

                EventItemHolder infoHolder = new EventItemHolder(sectionId, sectionName,  title, displayDate, content,
                        location, startDateString, endDateString, allDay);
                eventsList.add(infoHolder);
				
			} while (cursor.moveToNext());
		}
		
		return eventsList;
	}
	
	private void buildAdapters(ArrayList<IlpItemHolder> eventsList) {
        ArrayList<IlpItemHolder> todayItems = new ArrayList<IlpItemHolder>();
        ArrayList<IlpItemHolder> tomorrowItems = new ArrayList<IlpItemHolder>();
        ArrayList<IlpItemHolder> noDateItems = new ArrayList<IlpItemHolder>();
        ArrayList<IlpItemHolder> laterItems = new ArrayList<IlpItemHolder>();

		if (eventsList != null) {

            Calendar now = Calendar.getInstance();
            Calendar tomorrow = (Calendar)now.clone();
            // 24:00:00 'belongs' to the next day
            tomorrow.set(Calendar.HOUR_OF_DAY, 24);
            tomorrow.set(Calendar.MINUTE, 0);
            tomorrow.set(Calendar.SECOND, 0);
            Calendar yesterday = (Calendar)tomorrow.clone();
            yesterday.roll(Calendar.DAY_OF_YEAR, -1);
            Calendar later = (Calendar)tomorrow.clone();
            later.roll(Calendar.DAY_OF_YEAR, 1);

            if (eventsList != null) {

                for (IlpItemHolder infoHolder : eventsList) {
                    EventItemHolder eventItem = (EventItemHolder)infoHolder;
                    if (!TextUtils.isEmpty(eventItem.startDate)) {
                        Date eventStartDate = CalendarUtils.parseFromUTC(eventItem.startDate);
                        Date eventEndDate = CalendarUtils.parseFromUTC(eventItem.endDate);
                        Calendar eventStart = (Calendar)now.clone();
                        Calendar eventEnd = (Calendar)now.clone();
                        eventStart.setTime(eventStartDate);
                        eventEnd.setTime(eventEndDate);

                        if (eventStart.after(yesterday) && eventStart.before(tomorrow)) {
                            todayItems.add(eventItem);
                        } else if (now.after(eventStart) && now.before(eventEnd)) {
                            todayItems.add(eventItem);
                        } else if (eventStart.after(tomorrow) && eventStart.before(later)) {
                            tomorrowItems.add(eventItem);
                        } else if (eventStart.after(later)) {
                            laterItems.add(eventItem);
                        }
                    } else {
                        noDateItems.add(eventItem);
                    }
                }
            }
        }

        adapter = new IlpSectionedRecyclerAdapter(activity);
        if (!todayItems.isEmpty()) {
            Collections.sort(todayItems);
            String todayDateString = todayItems.get(0).date;
            Date todayDate = CalendarUtils.parseFromUTC(todayDateString);
            String todayDisplayDate = CalendarUtils.getDefaultDateString(activity, todayDate);
            IlpHeaderHolder todayHeaderHolder = new IlpHeaderHolder(getString(R.string.ilp_today), todayDisplayDate);
            adapter.addSection(todayHeaderHolder, todayItems);
        }
        if (!tomorrowItems.isEmpty()) {
            Collections.sort(tomorrowItems);
            String tomorrowDateString = tomorrowItems.get(0).date;
            Date tomorrowDate = CalendarUtils.parseFromUTC(tomorrowDateString);
            String tomorrowDisplayDate = CalendarUtils.getDefaultDateString(activity, tomorrowDate);
            IlpHeaderHolder tomorrowHeaderHolder = new IlpHeaderHolder(getString(R.string.ilp_tomorrow), tomorrowDisplayDate);
            adapter.addSection(tomorrowHeaderHolder, tomorrowItems);
        }
        if (!laterItems.isEmpty()) {
            Collections.sort(laterItems);

            int lastYear = 0;
            int lastDayOfYear = 0;
            int lastDayOfWeek = 0;
            String lastDateDisplay = "";
            ArrayList<IlpItemHolder> dateItems = new ArrayList<IlpItemHolder>();
            for (IlpItemHolder infoHolder : laterItems) {
                Date date = CalendarUtils.parseFromUTC(infoHolder.date);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                // Reset new section if not empty and year and date do not match
                if (!dateItems.isEmpty() && (lastYear != cal.get(Calendar.YEAR) ||
                        lastDayOfYear != cal.get(Calendar.DAY_OF_YEAR))) {

                    String dayOfWeekDisplay = CalendarUtils.getDayName(lastDayOfWeek);
                    IlpHeaderHolder dateHeaderHolder = new IlpHeaderHolder(dayOfWeekDisplay, lastDateDisplay);
                    adapter.addSection(dateHeaderHolder, dateItems);
                    dateItems = new ArrayList<IlpItemHolder>();
                }

                dateItems.add(infoHolder);

                lastDateDisplay = CalendarUtils.getDefaultDateString(activity, cal.getTime());
                lastYear = cal.get(Calendar.YEAR);
                lastDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                lastDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            }

            // Add last section if not empty
            if (!dateItems.isEmpty()) {
                String dayOfWeekDisplay = CalendarUtils.getDayName(lastDayOfWeek);
                IlpHeaderHolder dateHeaderHolder = new IlpHeaderHolder(dayOfWeekDisplay, lastDateDisplay);
                adapter.addSection(dateHeaderHolder, dateItems);
            }
        }

	}

    @Override
    public void onStart() {
        super.onStart();
        sendView("ILP Events List", getEllucianActivity().moduleName);
    }
}
