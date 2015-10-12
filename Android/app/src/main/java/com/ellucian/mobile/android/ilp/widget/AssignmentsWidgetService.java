// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.ilp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.ilp.AssignmentItemHolder;
import com.ellucian.mobile.android.ilp.AssignmentListsHolder;
import com.ellucian.mobile.android.ilp.IlpDetailFragment;
import com.ellucian.mobile.android.ilp.IlpListActivity;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AssignmentsWidgetService extends RemoteViewsService {

    private static final String TAG = "AppWidgetService";
    public static final String LAUNCHED_FROM_APPWIDGET = "LAUNCHED_FROM_APPWIDGET";

    @Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new AssignmentsViewsFactory(this.getApplicationContext(),
				intent));
	}

	class AssignmentsViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private List<AssignmentItemHolder> assignmentsToday = new ArrayList<>();
        private Context context = null;
		@SuppressWarnings("unused")
		private final int appWidgetId;

		public AssignmentsViewsFactory(Context context, Intent intent) {
            this.context = context;
			appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
        }

		public void onCreate() {
            Log.d(TAG, "WIDGET service create");
//			// We reload the cursor in onDataSetChanged() which gets
//			// called immediately after onCreate().
        }

		public void onDestroy() {
		}

		public int getCount() {
            return assignmentsToday.size();
		}

		@Override
		public RemoteViews getViewAt(int position) {

            Log.v(TAG, "getViewAt() position: " + position);
            AssignmentItemHolder assignment;
            assignment = assignmentsToday.get(position);
            if (assignment != null) {
                RemoteViews row = new RemoteViews(context.getPackageName(),
                        R.layout.assignments_widget_row);

                row.setTextViewText(R.id.assignment_widget_title, assignment.title);
                row.setTextViewText(R.id.assignment_widget_section, assignment.sectionName);

                Intent i = new Intent();
                Bundle bundle = new Bundle();
                String dateLabel = getString(R.string.course_assignments_due);

                bundle.putString(Extra.TITLE, assignment.title);
                bundle.putString(Extra.DATE, assignment.displayDate);
                bundle.putString(Extra.CONTENT, assignment.content);
                bundle.putString(Extra.LINK, assignment.url);
                bundle.putString(Extra.DATE_LABEL, dateLabel);
                bundle.putString(Extra.HEADER_SECTION_NAME, assignment.sectionName);
                bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ASSIGNMENTS);

                // assignments need extra care for adding to native calendar
                if (!TextUtils.isEmpty(assignment.date)) {
                    Date dateStart = CalendarUtils.parseFromUTC(assignment.date);
                    Long longStart = dateStart.getTime();
                    bundle.putLong(Extra.START, longStart);
                }

                bundle.putBoolean(IlpListActivity.SHOW_DETAIL, true);
                bundle.putBoolean(LAUNCHED_FROM_APPWIDGET, true);
                String ilpUrl = Utils.getStringFromPreferences(context, Utils.CONFIGURATION, Utils.ILP_URL, "");
                bundle.putString(Extra.COURSES_ILP_URL, ilpUrl);
                i.putExtras(bundle);
                row.setOnClickFillInIntent(R.id.assignment_widget_row, i);
                return (row);
            } else {
                return null;

            }
		}

		@Override
		public RemoteViews getLoadingView() {
            return null;
		}

		@Override
		public int getViewTypeCount() {
            return 1;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void onDataSetChanged() {
			long identityToken = Binder.clearCallingIdentity();

            Cursor cursor;
            // Only fetch assignments with a due date.
            cursor = getContentResolver().query(
                    CourseAssignments.CONTENT_URI, null,
                    null,
                    null,
                    CourseAssignments.ASSIGNMENT_DUE + " asc");

            AssignmentListsHolder assignmentLists = new AssignmentListsHolder(context, cursor);
            assignmentsToday = assignmentLists.getWidgetAssignments();
            cursor.close();
            Binder.restoreCallingIdentity(identityToken);
        }

	}
}