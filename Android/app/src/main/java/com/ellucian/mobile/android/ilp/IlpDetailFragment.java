/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.webframe.WebframeActivity;

public class IlpDetailFragment extends EllucianDefaultDetailFragment{

    public static final String DETAIL_TYPE = "detailType";
    public static final String DETAIL_TYPE_ASSIGNMENTS = "Assignments";
    public static final String DETAIL_TYPE_ANNOUNCEMENTS = "Announcements";
    public static final String DETAIL_TYPE_EVENTS = "Events";

    private Activity activity;
    private boolean calendarAvailable;

    public IlpDetailFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Intent calIntent = new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);
        calendarAvailable = Utils.isIntentAvailable(activity, calIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View rootView = inflater.inflate(R.layout.fragment_default_detail, container, false);

        View headerLayout = rootView.findViewById(R.id.header_layout);
        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        TextView dateLabelView = (TextView) rootView.findViewById(R.id.date_label);
        TextView dateView = (TextView) rootView.findViewById(R.id.date);
        TextView contentView = (TextView) rootView.findViewById(R.id.content);
        TextView locationView = (TextView) rootView.findViewById(R.id.location);

        Activity activity = getActivity();

        String title = null;
        String dateLabel = null;
        String date = null;
        String content = null;
        // Only difference from EllucianDefaultDetailFragment onCreateView
        // is the addition of section name.
        String sectionName = null;

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(Extra.TITLE);
            dateLabel = args.getString(Extra.DATE_LABEL);
            date = args.getString(Extra.DATE);
            content = args.getString(Extra.CONTENT);
            sectionName = args.getString(Extra.HEADER_SECTION_NAME);
        }

        if (!TextUtils.isEmpty(title)) {
            titleView.setText(title);
        }
        if (!TextUtils.isEmpty(dateLabel)) {
            dateLabelView.setText(dateLabel);
        } else {
            dateLabelView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(date)) {
            dateView.setText(date);
        }
        if (!TextUtils.isEmpty(content)) {
            contentView.setAutoLinkMask(Utils.getAvailableLinkMasks(activity, Linkify.ALL));
            contentView.setText(content);
        }
        // reusing the header's location view to show section name in ILP
        if (!TextUtils.isEmpty(sectionName)) {
            locationView.setText(sectionName);
        } else {
            locationView.setVisibility(View.GONE);
        }
        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ilp_detail, menu);

        Bundle args = getArguments();
        String title = args.getString(Extra.TITLE);
        String content = args.getString(Extra.CONTENT);
        String date = args.getString(Extra.DATE);
        String location = args.getString(Extra.LOCATION);

        // Adding date and location to the body of the email
        String text = "";
        if (!TextUtils.isEmpty(date)) {
            text += getString(R.string.label_string_content_format,
                    getString(R.string.label_date),
                    date) + "\n\n";
        }
        if (!TextUtils.isEmpty(location)) {
            text += getString(R.string.label_string_content_format,
                    getString(R.string.label_location),
                    location) + "\n\n";
        }
        text += content;

        MenuItem sharedMenuItem = menu.findItem(R.id.share);

        /** Getting the actionprovider associated with the menu item whose id is share */
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(sharedMenuItem);
        shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {

            @Override
            public boolean onShareTargetSelected(ShareActionProvider source,
                                                 Intent intent) {
                String label = "Tap Share Icon - " + intent.getComponent().flattenToShortString();
                sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, label, null, getEllucianActivity().moduleName);
                return false;
            }
        });

        /** Getting the target intent */
        Intent shareIntent = getDefaultShareIntent(title, text);

        /** Setting a share intent */
        if(Utils.isIntentAvailable(getActivity(), shareIntent)) {
            shareActionProvider.setShareIntent(shareIntent);
        } else {
            sharedMenuItem.setVisible(false).setEnabled(false);
        }

        String link = null;
        if (args.containsKey(Extra.LINK)) { link = args.getString(Extra.LINK); }


        boolean hideViewMenuItem = true;
        boolean hideAddToCalMenuItem = true;
        boolean hideRemindMeMenuItem = true;

        // check tabIndex and detailType args independently to set
        // one-way switches to not hide menu items
        if (args.containsKey(IlpListActivity.TAB_INDEX)) {
            int idx = args.getInt(IlpListActivity.TAB_INDEX);
            if (idx == IlpListActivity.TAB_ASSIGNMENTS || idx == IlpListActivity.TAB_ANNOUNCEMENTS) {
                hideViewMenuItem = false;
            }
            if (idx == IlpListActivity.TAB_EVENTS) {
                hideAddToCalMenuItem = false;
            }
            if (idx == IlpListActivity.TAB_ASSIGNMENTS) {
                hideRemindMeMenuItem = false;
            }
        }
        if (args.containsKey(DETAIL_TYPE)) {
            String detailType = args.getString(DETAIL_TYPE);
            if (detailType.equals(DETAIL_TYPE_ASSIGNMENTS) || detailType.equals(DETAIL_TYPE_ANNOUNCEMENTS)) {
                hideViewMenuItem = false;
            }
            if (detailType.equals(DETAIL_TYPE_EVENTS)) {
                hideAddToCalMenuItem = false;
            }
            if (detailType.equals(DETAIL_TYPE_ASSIGNMENTS)) {
                hideRemindMeMenuItem = false;
            }
        }
        // if no link is present, or view is not applicable for content type
        if (link == null || hideViewMenuItem) {
            MenuItem viewMenuItem = menu.findItem(R.id.view_target);
            viewMenuItem.setVisible(false).setEnabled(false);
        }
        // if calendar is not available, or addToCal not applicable
        if (!calendarAvailable || hideAddToCalMenuItem) {
            MenuItem calendarItem = menu.findItem(R.id.event_detail_add_to_calendar);
            calendarItem.setVisible(false);
            calendarItem.setEnabled(false);
        }
        // if calendar is not available, or Remind Me not applicable
        if (!calendarAvailable || hideRemindMeMenuItem) {
            MenuItem remindMe = menu.findItem(R.id.assignment_detail_remind_me);
            remindMe.setVisible(false);
            remindMe.setEnabled(false);
        }
    }

    private Intent getDefaultShareIntent(String subject, String text){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.view_target) {
            String url = getArguments().getString(Extra.LINK);
            if (!TextUtils.isEmpty(url)) {
                sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_FOLLOW_WEB, "Open assignment in web frame", null, getEllucianActivity().moduleName);
                Intent intent = new Intent(getActivity(), WebframeActivity.class);
                intent.putExtra(Extra.REQUEST_URL, url);
                startActivity(intent);
                return true;
            }
        }
        if (itemId == R.id.event_detail_add_to_calendar) {
            sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
                    GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
                    "Add to Calendar", null, getEllucianActivity().moduleName);
            sendAddToCalendarIntent();
            return true;
        }
        if (itemId == R.id.assignment_detail_remind_me) {
            sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
                    GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
                    "Remind Me", null, getEllucianActivity().moduleName);
            sendAddToCalendarIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        String viewLabel;
        if (getArguments().containsKey(DETAIL_TYPE)) {
            viewLabel = "ILP " + getArguments().getString(DETAIL_TYPE) + " Detail";
        } else {
            viewLabel = "ILP Detail";
        }

        sendView(viewLabel, getEllucianActivity().moduleName);
    }

    protected void sendAddToCalendarIntent() {
        Bundle args = getArguments();
//        String type = args.getString(IlpDetailFragment.DETAIL_TYPE);
        long startTime = args.getLong(Extra.START, CalendarUtils.getNextHour());
        // Some 3rd Party calendar apps need end dates, so add 1 hour if there is no end date.
        long endTime = args.getLong(Extra.END, startTime+3600000);
        String title = args.getString(Extra.TITLE);
        String content = args.getString(Extra.CONTENT);
        String location = args.getString(Extra.LOCATION);

        // Creating intent for native Calendar App
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(Events.TITLE, title)
                .putExtra(Events.DESCRIPTION, content)
                .putExtra(Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        // same handling as EventsDetailFragment
        if (endTime == -1) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        } else {
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        }

        startActivity(intent);
    }


}
