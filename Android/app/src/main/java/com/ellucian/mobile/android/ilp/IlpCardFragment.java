/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.client.services.CourseAnnouncementsIntentService;
import com.ellucian.mobile.android.client.services.CourseAssignmentsIntentService;
import com.ellucian.mobile.android.client.services.CourseEventsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class IlpCardFragment extends EllucianFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int ASSIGNMENTS_LOADER = 0;
	private static final int EVENTS_LOADER = 1;
	private static final int ANNOUNCEMENTS_LOADER = 2;

	private IlpCardActivity activity;
	private View rootView;
	private CardView assignmentsCard;
	private CardView eventsCard;
	private CardView announcementsCard;
	private List<AssignmentItemHolder> assignmentsToday = new ArrayList<AssignmentItemHolder>();
    private List<AssignmentItemHolder> assignmentsOverdue = new ArrayList<AssignmentItemHolder>();
	private List<EventItemHolder> events = new ArrayList<EventItemHolder>();
	private List<AnnouncementItemHolder> announcements = new ArrayList<AnnouncementItemHolder>();
	private Bundle detailBundle;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (IlpCardActivity) activity;
	}

	public IlpCardFragment(){
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (rootView == null || ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                        Configuration.SCREENLAYOUT_SIZE_LARGE)) {
			rootView = inflater.inflate(R.layout.fragment_ilp_card, container, false);
			
			assignmentsCard = (CardView) rootView.findViewById(R.id.assignments_card);
			Toolbar assignmentsToolbar = (Toolbar) assignmentsCard.findViewById(R.id.assignments_toolbar);
			assignmentsToolbar.inflateMenu(R.menu.ilp_assignments_card_toolbar);
			assignmentsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.view_by_date) {
                        openIlpActivity(IlpListActivity.TAB_ASSIGNMENTS, AssignmentsRecyclerFragment.VIEW_BY_DATE);
					} else {
                        openIlpActivity(IlpListActivity.TAB_ASSIGNMENTS, AssignmentsRecyclerFragment.VIEW_BY_NO_DATE);
					}
					return true;
				}
			});
			
			eventsCard = (CardView) rootView.findViewById(R.id.events_card);
			Toolbar eventsToolbar = (Toolbar) eventsCard.findViewById(R.id.events_toolbar);
			eventsToolbar.inflateMenu(R.menu.ilp_default_card_toolbar);
			eventsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if (item.getItemId() == R.id.view_all) {
                        openIlpActivity(IlpListActivity.TAB_EVENTS, 0);
					}
					return true;
				}
			});
			
			announcementsCard = (CardView) rootView.findViewById(R.id.announcements_card);
			Toolbar announcementsToolbar = (Toolbar) announcementsCard.findViewById(R.id.announcements_toolbar);
			announcementsToolbar.inflateMenu(R.menu.ilp_default_card_toolbar);
			announcementsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if (item.getItemId() == R.id.view_all) {
                        openIlpActivity(IlpListActivity.TAB_ANNOUNCEMENTS, 0);
					}
					return true;
				}
			});
		}

		return rootView;
	}

    private void openIlpActivity(int tabIndex, int type) {
        Intent intent = new Intent();
        intent.setClass(activity, IlpListActivity.class);
        intent.putExtras(activity.getIntent().getExtras());
        intent.putExtra(IlpListActivity.TAB_INDEX, tabIndex);
        if (tabIndex == IlpListActivity.TAB_ASSIGNMENTS ) {
            intent.putExtra(AssignmentsRecyclerFragment.ASSIGNMENTS_TYPE, type);
        }
        startActivity(intent);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(ASSIGNMENTS_LOADER, null, IlpCardFragment.this);
        getLoaderManager().initLoader(EVENTS_LOADER, null, IlpCardFragment.this);
        getLoaderManager().initLoader(ANNOUNCEMENTS_LOADER, null, IlpCardFragment.this);

        String ilpUrl = getArguments().getString(Extra.COURSES_ILP_URL);

        Intent assignmentsIntent = new Intent(activity, CourseAssignmentsIntentService.class);
        assignmentsIntent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);
        activity.startService(assignmentsIntent);

        Intent eventsIntent = new Intent(activity, CourseEventsIntentService.class);
        eventsIntent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);
        activity.startService(eventsIntent);

        Intent announcementsIntent = new Intent(activity, CourseAnnouncementsIntentService.class);
        announcementsIntent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);
        activity.startService(announcementsIntent);
	}	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		

		switch (id) {
		case EVENTS_LOADER: 		return new CursorLoader(activity, CourseEvents.CONTENT_URI,
											null, null, null, CourseEvents.DEFAULT_SORT);
							
		case ANNOUNCEMENTS_LOADER:  return new CursorLoader(activity, CourseAnnouncements.CONTENT_URI,
											null, null, null, CourseAnnouncements.DEFAULT_SORT);
									
		default: 	                return new CursorLoader(activity, CourseAssignments.CONTENT_URI,
											null, null, null, CourseAssignments.DEFAULT_SORT);
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		
		switch (loader.getId()) {
		case ASSIGNMENTS_LOADER:    buildAssignmentsList(cursor);
                                    buildAssignmentsCard(assignmentsToday);
									break;
									 
		case EVENTS_LOADER: 		buildEventsList(cursor);
									buildEventsCard(events);
									eventsCard.invalidate();
									break;
								
		case ANNOUNCEMENTS_LOADER:  buildAnnouncementsList(cursor);
									buildAnnouncementsCard(announcements);
									announcementsCard.invalidate();
									break;
		default:
		}

	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

    private void buildAssignmentsList(Cursor cursor) {
        AssignmentListsHolder assignmentLists = new AssignmentListsHolder(activity, cursor);
        assignmentsToday = assignmentLists.getAssignmentsToday();
        assignmentsOverdue = assignmentLists.getAssignmentsOverdue();
    }
	
	private void buildAssignmentsCard(final List<AssignmentItemHolder> assignmentList) {
        LayoutInflater inflater = activity.getLayoutInflater();
        LinearLayout assignmentsLayout = (LinearLayout) assignmentsCard.findViewById(R.id.assignments_layout);
        assignmentsLayout.removeAllViews();

        boolean showTodaySubheader = false;
        if (assignmentsOverdue != null && assignmentsOverdue.size() > 0) {
            TextView overdueSubheader = (TextView) inflater.inflate(R.layout.ilp_card_sub_header_row, assignmentsLayout, false);
            overdueSubheader.setTextColor(getResources().getColor(R.color.warning_text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                overdueSubheader.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_warning_red, 0, 0, 0);
            } else {
                overdueSubheader.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_warning_red, 0, 0, 0);
            }

            overdueSubheader.setCompoundDrawablePadding(12);
            overdueSubheader.setText(R.string.ilp_overdue);
            assignmentsLayout.addView(overdueSubheader);
            showTodaySubheader = true;

            for (int i = 0; i < assignmentsOverdue.size(); i++) {
                AssignmentItemHolder infoHolder = assignmentsOverdue.get(i);
                View cardRow = inflater.inflate(R.layout.ilp_card_row, assignmentsLayout, false);
                cardRow.setTag(Integer.valueOf(i));
                cardRow.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Integer index = (Integer) v.getTag();
                        AssignmentItemHolder infoHolder = assignmentsOverdue.get(index);
                        detailBundle = buildDetailBundle(infoHolder);
                        detailBundle.putInt(IlpListActivity.TAB_INDEX, IlpListActivity.TAB_ASSIGNMENTS);
                        detailBundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ASSIGNMENTS);
                        detailBundle.putInt(AssignmentsRecyclerFragment.ASSIGNMENTS_TYPE, AssignmentsRecyclerFragment.VIEW_BY_DATE);
                        showDetails(index);
                    }
                });

                TextView titleView = (TextView) cardRow.findViewById(R.id.title);
                titleView.setTextColor(getResources().getColor(R.color.warning_text_color));
                titleView.setText(infoHolder.title);

                TextView sectionView = (TextView) cardRow.findViewById(R.id.section_name);
                String displaySection = "";
                if (!TextUtils.isEmpty(infoHolder.sectionName)) {
                    displaySection = infoHolder.sectionName;
                }
                sectionView.setText(displaySection);

                TextView dateView = (TextView) cardRow.findViewById(R.id.date);
                String displayDate = "";
                if (!TextUtils.isEmpty(infoHolder.displayDate)) {
                    displayDate = infoHolder.displayDate;
                }
                dateView.setText(displayDate);

                if (i > 0) {
                    View separator = inflater.inflate(R.layout.separator, assignmentsLayout, false);
                    assignmentsLayout.addView(separator);
                }
                assignmentsLayout.addView(cardRow);
            }
        }

        if (assignmentList != null) {
            if (showTodaySubheader && !assignmentList.isEmpty()) {
                TextView todaySubheader = (TextView) inflater.inflate(R.layout.ilp_card_sub_header_row, assignmentsLayout, false);
                todaySubheader.setTextColor(getResources().getColor(R.color.due_today_green));
                todaySubheader.setText(R.string.ilp_due_today);
                assignmentsLayout.addView(todaySubheader);
            }

            for (int i = 0; i < assignmentList.size(); i++) {
                AssignmentItemHolder infoHolder = assignmentList.get(i);
                View cardRow = inflater.inflate(R.layout.ilp_card_row, assignmentsLayout, false);
                cardRow.setTag(Integer.valueOf(i));
                cardRow.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Integer index = (Integer) v.getTag();
                        AssignmentItemHolder infoHolder = assignmentList.get(index);
                        detailBundle = buildDetailBundle(infoHolder);
                        detailBundle.putInt(IlpListActivity.TAB_INDEX, IlpListActivity.TAB_ASSIGNMENTS);
                        detailBundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ASSIGNMENTS);
                        detailBundle.putInt(AssignmentsRecyclerFragment.ASSIGNMENTS_TYPE, AssignmentsRecyclerFragment.VIEW_BY_DATE);
                        int overdueCount = 0;
                        if (!assignmentsOverdue.isEmpty()) {
                            // have to add one for the today header to match the same index on larger list
                            overdueCount = assignmentsOverdue.size() + 1;
                        }
                        showDetails(index + overdueCount);
                    }
                });

                TextView titleView = (TextView) cardRow.findViewById(R.id.title);
                titleView.setText(infoHolder.title);

                TextView sectionView = (TextView) cardRow.findViewById(R.id.section_name);
                String displaySection = "";
                if (!TextUtils.isEmpty(infoHolder.sectionName)) {
                    displaySection = infoHolder.sectionName;
                }
                sectionView.setText(displaySection);

                TextView dateView = (TextView) cardRow.findViewById(R.id.date);
                String displayDate = "";
                if (!TextUtils.isEmpty(infoHolder.date)) {
                    Date date = CalendarUtils.parseFromUTC(infoHolder.date);
                    displayDate = CalendarUtils.getDefaultTimeString(activity, date);
                }
                dateView.setText(displayDate);

                if (i > 0) {
                    View separator = inflater.inflate(R.layout.separator, assignmentsLayout, false);
                    assignmentsLayout.addView(separator);
                }
                assignmentsLayout.addView(cardRow);
            }
        }

        TextView noAssignmentsView = (TextView) assignmentsCard.findViewById(R.id.assignments_none_today);
        if (assignmentList.isEmpty() && assignmentsOverdue.isEmpty()) {
            noAssignmentsView.setVisibility(View.VISIBLE);
        } else {
            noAssignmentsView.setVisibility(View.GONE);
        }
    }
	
	private void buildEventsList(Cursor cursor) {
		events = new ArrayList<EventItemHolder>();
		
		Calendar todayCal = Calendar.getInstance();
		if (cursor.moveToFirst()) {
			do {
				String startDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_START));
                String endDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_END));

				if (!TextUtils.isEmpty(startDateString)) {

					Date startDate = CalendarUtils.parseFromUTC(startDateString);
					Calendar eventStartCal = Calendar.getInstance();
					eventStartCal.setTime(startDate);

                    Date endDate = null;
                    Calendar eventEndCal = null;
                    if (!TextUtils.isEmpty(endDateString)) {
                        endDate = CalendarUtils.parseFromUTC(endDateString);
                        eventEndCal = Calendar.getInstance();
                        eventEndCal.setTime(endDate);
                    }

					// Make sure that the event startDate is today, or that
                    // today is between startDate and endDate
					if ((eventStartCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) &&
					 		eventStartCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)) ||
                            (todayCal.after(eventStartCal) && todayCal.before(eventEndCal))) {

						String sectionId = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_ID));
                        String sectionName = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_SECTION_NAME));
						String title = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_TITLE));
						String content = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_DESCRIPTION));
						String location = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_LOCATION));
						String allDayString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_ALL_DAY));
						boolean allDay = Boolean.parseBoolean(allDayString);

						// default displayDate will be the date and time
						String displayDate = "";

                        if (allDay) {
                            displayDate = getString(R.string.date_all_day_event_format,
                                        CalendarUtils.getDefaultDateString(activity, startDate));
                        } else if (!TextUtils.isEmpty(endDateString)) {

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

						EventItemHolder infoHolder = new EventItemHolder(sectionId, sectionName, title, displayDate,
                                content, location, startDateString, endDateString, allDay);
						events.add(infoHolder);
					}
				}

			} while (cursor.moveToNext());

			Collections.sort(events);
		}
	}
	
	private void buildEventsCard(final List<EventItemHolder> eventsList) {
		LayoutInflater inflater = activity.getLayoutInflater();
		LinearLayout eventsLayout = (LinearLayout) eventsCard.findViewById(R.id.events_layout);
		eventsLayout.removeAllViews();


        if (eventsList != null) {
            for (int i = 0; i < eventsList.size(); i++) {
                EventItemHolder infoHolder = eventsList.get(i);
                View cardRow = inflater.inflate(R.layout.ilp_card_row, eventsLayout, false);
                cardRow.setTag(Integer.valueOf(i));
                cardRow.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Integer index = (Integer) v.getTag();
                        EventItemHolder infoHolder = eventsList.get(index);
                        detailBundle = buildDetailBundle(infoHolder);
                        detailBundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_EVENTS);
                        detailBundle.putInt(IlpListActivity.TAB_INDEX, IlpListActivity.TAB_EVENTS);
                        showDetails(index);
                    }
                });

                TextView titleView = (TextView) cardRow.findViewById(R.id.title);
                titleView.setText(infoHolder.title);

                TextView sectionView = (TextView) cardRow.findViewById(R.id.section_name);
                String displaySection = "";
                if (!TextUtils.isEmpty(infoHolder.sectionName)) {
                    displaySection = infoHolder.sectionName;
                }
                sectionView.setText(displaySection);

                TextView dateView = (TextView) cardRow.findViewById(R.id.date);
                dateView.setText(infoHolder.displayDate);

                if (i > 0) {
                    View separator = inflater.inflate(R.layout.separator, eventsLayout, false);
                    eventsLayout.addView(separator);
                }

                eventsLayout.addView(cardRow);
            }
        }

        TextView noEventsView = (TextView) eventsCard.findViewById(R.id.events_none_today);
        if (eventsList.isEmpty()) {
            noEventsView.setVisibility(View.VISIBLE);
        } else {
            noEventsView.setVisibility(View.GONE);
        }
    }
	
	private void buildAnnouncementsList(Cursor cursor) {
		announcements = new ArrayList<AnnouncementItemHolder>();

        Calendar todayCal = Calendar.getInstance();
		if (cursor.moveToFirst()) {
			do {

                String sectionId = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_ID));
                String sectionName = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_SECTION_NAME));
                String title = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_TITLE));
                String dateString = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_DATE));
                String content = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_CONTENT));
                String url = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_URL));

                String displayDate = "";
                Date announcementDate;

                if (!TextUtils.isEmpty(dateString)) {
                    announcementDate = CalendarUtils.parseFromUTC(dateString);
                } else {  // Announcements with null dates get treated as Today
                    announcementDate = new Date();
                }
                displayDate = CalendarUtils.getDefaultDateTimeString(getActivity(), announcementDate);

                Calendar announcementCal = Calendar.getInstance();
                announcementCal.setTime(announcementDate);

                // Make sure that the announcement has the current date, if not it is ignored
                if (announcementCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) &&
                        announcementCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)) {

                    AnnouncementItemHolder infoHolder = new AnnouncementItemHolder(sectionId, sectionName, title,
                            dateString, displayDate, content, url);

                    announcements.add(infoHolder);
                }

            } while (cursor.moveToNext());

			Collections.sort(announcements);
			Collections.reverse(announcements);
		}
	}
	
	private void buildAnnouncementsCard(final List<AnnouncementItemHolder> announcementsList) {
		LayoutInflater inflater = activity.getLayoutInflater();
		LinearLayout announcementsLayout = (LinearLayout) announcementsCard.findViewById(R.id.announcements_layout);
		announcementsLayout.removeAllViews();

        if (announcementsList != null) {
            for (int i = 0; i < announcementsList.size(); i++) {
                AnnouncementItemHolder infoHolder = announcementsList.get(i);
                View cardRow = inflater.inflate(R.layout.ilp_card_row, announcementsLayout, false);
                cardRow.setTag(Integer.valueOf(i));
                cardRow.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Integer index = (Integer) v.getTag();
                        AnnouncementItemHolder infoHolder = announcementsList.get(index);
                        detailBundle = buildDetailBundle(infoHolder);
                        detailBundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ANNOUNCEMENTS);
                        // Make sure the right tab will be selected on dual pane
                        detailBundle.putInt(IlpListActivity.TAB_INDEX, IlpListActivity.TAB_ANNOUNCEMENTS);
                        showDetails(index);
                    }
                });

                TextView titleView = (TextView) cardRow.findViewById(R.id.title);
                titleView.setText(infoHolder.title);

                TextView sectionView = (TextView) cardRow.findViewById(R.id.section_name);
                String displaySection = "";
                if (!TextUtils.isEmpty(infoHolder.sectionName)) {
                    displaySection = infoHolder.sectionName;
                }
                sectionView.setText(displaySection);

                if (i > 0) {
                    View separator = inflater.inflate(R.layout.separator, announcementsLayout, false);
                    announcementsLayout.addView(separator);
                }

                announcementsLayout.addView(cardRow);
            }
        }

        TextView noAnnouncementsView = (TextView) announcementsCard.findViewById(R.id.announcements_none_today);
        if (announcementsList.isEmpty()) {
            noAnnouncementsView.setVisibility(View.VISIBLE);
        }
        else {
            noAnnouncementsView.setVisibility(View.GONE);
        }
	}
	
    private Bundle buildDetailBundle(Object... objects) {
    	IlpItemHolder infoHolder = (IlpItemHolder) objects[0];

    	Bundle bundle = new Bundle();
		
		bundle.putString(Extra.TITLE, infoHolder.title);
		bundle.putString(Extra.DATE, infoHolder.displayDate);
		bundle.putString(Extra.CONTENT, infoHolder.content);
		bundle.putString(Extra.LINK, infoHolder.url);
        bundle.putString(Extra.HEADER_SECTION_NAME, infoHolder.sectionName);

        // events need extra care for adding to native calendar
        if (infoHolder.type.equals(EventItemHolder.TYPE_EVENT)) {
            EventItemHolder eventItem = (EventItemHolder)infoHolder;
            Date dateStart = CalendarUtils.parseFromUTC(eventItem.startDate);
            Long longStart = dateStart.getTime();
            bundle.putLong(Extra.START, longStart);
            bundle.putString(Extra.LOCATION, eventItem.location);
            if (eventItem.allDay || eventItem.endDate == null) {
                // same handling as EventsListFragment
                bundle.putLong(Extra.END, -1L);
            } else {
                Date dateEnd = CalendarUtils.parseFromUTC(eventItem.endDate);
                Long longEnd = dateEnd.getTime();
                bundle.putLong(Extra.END, longEnd);
            }
        }
        // assignments need extra care for adding to native calendar
        if (infoHolder.type.equals(AssignmentItemHolder.TYPE_ASSIGNMENT) && !TextUtils.isEmpty(infoHolder.date)) {
            Date dateStart = CalendarUtils.parseFromUTC(infoHolder.date);
            Long longStart = dateStart.getTime();
            bundle.putLong(Extra.START, longStart);
        }
		return bundle;
	}
	
	private void showDetails(int index) {
		
		Intent intent = new Intent();
        intent.setClass(activity, IlpListActivity.class);
        intent.putExtras(activity.getIntent().getExtras());
        intent.putExtras(detailBundle); 
        intent.putExtra(IlpListActivity.SELECTED_INDEX, index);
        intent.putExtra(IlpListActivity.SHOW_DETAIL, true);
        startActivity(intent);

    }

    @Override
    public void onStart() {
        super.onStart();
        sendView("ILP Today Summary", getEllucianActivity().moduleName);
    }

}
