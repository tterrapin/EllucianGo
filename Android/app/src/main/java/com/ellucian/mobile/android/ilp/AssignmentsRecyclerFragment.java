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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.SectionedItemHolderRecyclerAdapter;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultRecyclerFragment;
import com.ellucian.mobile.android.client.services.CourseAssignmentsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class AssignmentsRecyclerFragment extends EllucianDefaultRecyclerFragment implements
	LoaderManager.LoaderCallbacks<Cursor>{

    public static final String ASSIGNMENTS_TYPE = "assignmentsType";
	public static final int VIEW_BY_DATE = 0;
	public static final int VIEW_BY_NO_DATE = 1;
	private IlpListActivity activity;
	private SectionedItemHolderRecyclerAdapter dateAdapter;
	private SectionedItemHolderRecyclerAdapter noDateAdapter;
	private int showTypeIndex;

    public AssignmentsRecyclerFragment(){
    }
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		activity = (IlpListActivity) getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
		
		showTypeIndex = 0;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, AssignmentsRecyclerFragment.this);

        String ilpUrl = getArguments().getString(Extra.COURSES_ILP_URL);
        Intent intent = new Intent(activity, CourseAssignmentsIntentService.class);
        intent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);

        activity.startService(intent);

    }

    @Override
    public Bundle buildDetailBundle(Object... objects) {
		IlpItemHolder infoHolder = (IlpItemHolder) objects[0];

    	Bundle bundle = new Bundle();
		
		bundle.putString(Extra.TITLE, infoHolder.title);
		bundle.putString(Extra.DATE, infoHolder.displayDate);
        bundle.putString(Extra.DATE_LABEL, getString(R.string.course_assignments_due));
		bundle.putString(Extra.CONTENT, infoHolder.content);
		bundle.putString(Extra.HEADER_SECTION_NAME, infoHolder.sectionName);
		bundle.putString(Extra.LINK, infoHolder.url);
        bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ASSIGNMENTS);
        
        // assignments need extra care for adding to native calendar
        if (!TextUtils.isEmpty(infoHolder.date)) {
            Date dateStart = CalendarUtils.parseFromUTC(infoHolder.date);
            Long longStart = dateStart.getTime();
            bundle.putLong(Extra.START, longStart);
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

		return new CursorLoader(activity, CourseAssignments.CONTENT_URI, null, null, null, CourseAssignments.DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		ArrayList<IlpItemHolder> assignmentsList = buildAssignmentsList(cursor);
		buildAdapters(assignmentsList);

        Bundle args = getArguments();
        if (args.containsKey(ASSIGNMENTS_TYPE)) {
            showTypeIndex = args.getInt(ASSIGNMENTS_TYPE);
            args.remove(ASSIGNMENTS_TYPE);
        }

		if (showTypeIndex == 1) {
			setAdapter(noDateAdapter);	
		} else {
			setAdapter(dateAdapter);	
		}
        if (dualPane) {

            if (getArguments().getBoolean(IlpListActivity.SHOW_DETAIL, false)) {
                getArguments().remove(IlpListActivity.SHOW_DETAIL);
                if (getArguments().containsKey(IlpListActivity.SELECTED_INDEX)) {
                    int position = getArguments().getInt(IlpListActivity.SELECTED_INDEX);
                    Object item = adapter.getItem(position + 1);
                    detailBundle = buildDetailBundle(item);
                    recyclerView.setSelectedIndex(position + 1);
                } else if (getArguments().containsKey(Extra.LINK)) {

                    String url = getArguments().getString(Extra.LINK);

                    for (int i = 1; i < adapter.getItemCount(); i++) {
                        if (adapter.getItemViewType(i) == SectionedItemHolderRecyclerAdapter.TYPE_SECTION_ITEM) {
                            IlpItemHolder itemHolder = (IlpItemHolder)adapter.getItem(i);
                            if (url.equals(itemHolder.url)) {
                                Object item = adapter.getItem(i);
                                detailBundle = buildDetailBundle(item);
                                recyclerView.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            }

            showCurrentSelected();
        }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		setAdapter(null);	
	}

	private void showType(int type) {

		if (type == VIEW_BY_NO_DATE) {
			showTypeIndex = VIEW_BY_NO_DATE;
			setAdapter(noDateAdapter);
		} else {
			showTypeIndex = VIEW_BY_DATE;
			setAdapter(dateAdapter);
		}
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
	
	private ArrayList<IlpItemHolder> buildAssignmentsList(Cursor cursor) {
		ArrayList<IlpItemHolder> assignmentsList = new ArrayList<>();
		
		if (cursor.moveToFirst()) {
			do {				
				String sectionId = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_ID));
                String sectionName = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_SECTION_NAME));
				String title = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_NAME));
				String content = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_DESCRIPTION));
				String dateString = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_DUE));
				String url = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_URL));
				
				String displayDate;
				if (!TextUtils.isEmpty(dateString)) {
					Date date = CalendarUtils.parseFromUTC(dateString);
					displayDate = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
				} else {
					displayDate = getString(R.string.course_assignments_none_assigned);
				}

				AssignmentItemHolder infoHolder = new AssignmentItemHolder(sectionId, sectionName, title, dateString,
                        displayDate, content, url);
				assignmentsList.add(infoHolder);
				
			} while (cursor.moveToNext());
		}
		
		return assignmentsList;		
	}
	
	private void buildAdapters(ArrayList<IlpItemHolder> assignmentsList) {
        ArrayList<IlpItemHolder> overdueItems = new ArrayList<IlpItemHolder>();
		ArrayList<IlpItemHolder> todayItems = new ArrayList<IlpItemHolder>();
		ArrayList<IlpItemHolder> tomorrowItems = new ArrayList<IlpItemHolder>();
		ArrayList<IlpItemHolder> noDateItems = new ArrayList<IlpItemHolder>();
		ArrayList<IlpItemHolder> laterItems = new ArrayList<IlpItemHolder>();

        Calendar now = (Calendar)Calendar.getInstance().clone();
        Calendar tomorrow = (Calendar)now.clone();
        // 24:00:000 'belongs' to the next day
        tomorrow.set(Calendar.HOUR_OF_DAY, 24);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        Calendar later = (Calendar)tomorrow.clone();
        later.roll(Calendar.DAY_OF_YEAR, 1);

		if (assignmentsList != null) {
			
			for (IlpItemHolder infoHolder : assignmentsList) {
				if (!TextUtils.isEmpty(infoHolder.date)) {

					Date assignmentDate = CalendarUtils.parseFromUTC(infoHolder.date);
					Calendar assignmentDue = (Calendar)Calendar.getInstance().clone();
                    assignmentDue.setTime(assignmentDate);
                    if (assignmentDue.before(now)) {
                        // overdue
                        overdueItems.add(infoHolder);
                    } else if (assignmentDue.before(tomorrow)) {
                        // today
                        todayItems.add(infoHolder);
                    } else if (assignmentDue.before(later)) {
                        // tomorrow
                        tomorrowItems.add(infoHolder);
                    } else {
                        // later
                        laterItems.add(infoHolder);
                    }
				} else {
					noDateItems.add(infoHolder);
				}	
			}
		}
		
		dateAdapter = new IlpSectionedRecyclerAdapter(activity);
        if (!overdueItems.isEmpty()) {
            Collections.sort(overdueItems);
            IlpHeaderHolder overdueHeaderHolder = new IlpHeaderHolder(getString(R.string.ilp_overdue), null);
            dateAdapter.addSection(overdueHeaderHolder, overdueItems);
        }
		if (!todayItems.isEmpty()) {
            Collections.sort(todayItems);
            String todayDateString = todayItems.get(0).date;
            Date todayDate = CalendarUtils.parseFromUTC(todayDateString);
            String todayDisplayDate = CalendarUtils.getDefaultDateString(activity, todayDate);
            IlpHeaderHolder todayHeaderHolder = new IlpHeaderHolder(getString(R.string.ilp_today), todayDisplayDate);
			dateAdapter.addSection(todayHeaderHolder, todayItems);
		}
		if (!tomorrowItems.isEmpty()) {
			Collections.sort(tomorrowItems);
            String tomorrowDateString = tomorrowItems.get(0).date;
            Date tomorrowDate = CalendarUtils.parseFromUTC(tomorrowDateString);
            String tomorrowDisplayDate = CalendarUtils.getDefaultDateString(activity, tomorrowDate);
            IlpHeaderHolder tomorrowHeaderHolder = new IlpHeaderHolder(getString(R.string.ilp_tomorrow), tomorrowDisplayDate);
			dateAdapter.addSection(tomorrowHeaderHolder, tomorrowItems);
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
				Calendar cal = (Calendar)Calendar.getInstance().clone();
				cal.setTime(date);
				
				// Reset new section if not empty and year and date do not match
				if (!dateItems.isEmpty() && (lastYear != cal.get(Calendar.YEAR) || 
						lastDayOfYear != cal.get(Calendar.DAY_OF_YEAR))) {
                    String dayOfWeekDisplay = CalendarUtils.getDayName(lastDayOfWeek);
                    IlpHeaderHolder dateHeaderHolder = new IlpHeaderHolder(dayOfWeekDisplay, lastDateDisplay);
					dateAdapter.addSection(dateHeaderHolder, dateItems);
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
                dateAdapter.addSection(dateHeaderHolder, dateItems);
			}
		}
		
		noDateAdapter = new IlpSectionedRecyclerAdapter(activity);
		if (!noDateItems.isEmpty()) {
            IlpHeaderHolder noDateHeaderHolder = new IlpHeaderHolder(getString(R.string.ilp_no_date), null);
			noDateAdapter.addSection(noDateHeaderHolder, noDateItems);
		}
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ilp_assignments, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_by_date:
                recyclerView.clearSelected();
                showType(VIEW_BY_DATE);
                if (dualPane) {
                    showCurrentSelected();
                }
                return true;
            case R.id.view_by_no_date:
                recyclerView.clearSelected();
                showType(VIEW_BY_NO_DATE);
                if (dualPane) {
                    showCurrentSelected();
                }
                return true;
            default: return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        sendView("ILP Assignments List", getEllucianActivity().moduleName);
    }

}
