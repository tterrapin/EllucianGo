/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.SectionedItemHolderRecyclerAdapter;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Utils;

import java.util.Date;

public class IlpSectionedRecyclerAdapter extends SectionedItemHolderRecyclerAdapter {
    private final boolean calendarAvailable;

	public IlpSectionedRecyclerAdapter(Context context) {
        super(context);
        Intent calIntent = new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);
        calendarAvailable = Utils.isIntentAvailable(context, calIntent);
	}

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(final ViewGroup parent, int viewType) {
        View v;

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ilp_header_row, parent, false);

        return new ItemViewHolder(this, v, null);
    }

    @SuppressLint("NewApi")
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        IlpHeaderHolder headerHolder = (IlpHeaderHolder) getItem(position);

        View headerContainer = ((ItemViewHolder)holder).itemView.findViewById(R.id.header_container);

        TextView dayView = (TextView) headerContainer.findViewById(R.id.day);
        dayView.setText(headerHolder.day);
        int build = Build.VERSION.SDK_INT;

        String overdueText = context.getResources().getString(R.string.ilp_overdue);
        if (headerHolder.day.equals(overdueText)) {
            if (build >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                dayView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_warning_black, 0, 0, 0);
            } else {
                dayView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_warning_black, 0, 0, 0);
            }
            dayView.setCompoundDrawablePadding(12);
        } else {
            // unset drawable in case recycled
            if (build >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (dayView.getCompoundDrawablesRelative()[0] != null) {
                    dayView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
                }
            } else {
                if (dayView.getCompoundDrawables()[0] != null) {
                    dayView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }
        }

        TextView dateView = (TextView) headerContainer.findViewById(R.id.date);
        dateView.setText(headerHolder.date);
    }


	@Override
	public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
		View v;
    	
    	v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ilp_item_row, parent, false);

        return new ItemViewHolder(this, v, onItemClickListener);
	}
	
	// Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    	 	
    	Object object = getItem(position);
        IlpItemHolder infoHolder = (IlpItemHolder) object;

    	TextView titleView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(R.id.title);
		titleView.setText(infoHolder.title);
		TextView sectionView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(R.id.section_name);
		sectionView.setText(infoHolder.sectionName);
		TextView dateView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(R.id.date);
        String displayDate = "";

        if (infoHolder.type.equals(AssignmentItemHolder.TYPE_ASSIGNMENT)) {
            if (!TextUtils.isEmpty(infoHolder.date)) {
                Date dueDate = CalendarUtils.parseFromUTC(infoHolder.date);
                Date now = new Date();
                if (dueDate.before(now)) {
                    displayDate = CalendarUtils.getDefaultDateTimeString(context, dueDate);
                    titleView.setTextColor(Utils.getColorHelper(context, R.color.warning_text_color));
                } else {
                    displayDate = CalendarUtils.getDefaultTimeString(context, dueDate);
                    // reset the color in case recycled
                    titleView.setTextColor(Color.BLACK);
                }

            }

            if (calendarAvailable) {
                TextView remindMeView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(R.id.remind_me);
                remindMeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendAddToCalendarIntent(position);
                    }
                });
                sectionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendAddToCalendarIntent(position);
                    }
                });
                remindMeView.setVisibility(View.VISIBLE);
            }
        } else if (infoHolder.type.equals(EventItemHolder.TYPE_EVENT)) {

            EventItemHolder eventHolder = (EventItemHolder) infoHolder;

            if (eventHolder.allDay) {
                displayDate = context.getString(R.string.all_day_event);
            } else if (!TextUtils.isEmpty(eventHolder.startDate)) {
                Date startDate = CalendarUtils.parseFromUTC(eventHolder.startDate);

                if (!TextUtils.isEmpty(eventHolder.endDate)) {
                    Date endDate = CalendarUtils.parseFromUTC(eventHolder.endDate);

                    if (CalendarUtils.getDefaultDateString(context, startDate)
                            .equals(CalendarUtils.getDefaultDateString(context, endDate))) {
                        // start/end today, short format output
                        displayDate = context.getString(R.string.date_time_to_time_format,
                                CalendarUtils.getDefaultDateString(context, startDate),
                                CalendarUtils.getDefaultTimeString(context, startDate),
                                CalendarUtils.getDefaultTimeString(context, endDate));
                    } else {
                        // start/end different days, long format output
                        displayDate = context.getString(R.string.date_time_to_date_time_format,
                                CalendarUtils.getDefaultDateString(context, startDate),
                                CalendarUtils.getDefaultTimeString(context, startDate),
                                CalendarUtils.getDefaultDateString(context, endDate),
                                CalendarUtils.getDefaultTimeString(context, endDate));
                    }
                } else {
                    displayDate = CalendarUtils.getDefaultTimeString(context, startDate);
                }

            } else {
                displayDate = context.getString(R.string.course_assignments_none_assigned);
            }
        } else {
            displayDate = infoHolder.displayDate;
        }

        dateView.setText(displayDate);
    }

    private void sendAddToCalendarIntent(int position) {

        IlpItemHolder itemHolder = (IlpItemHolder) getItem(position);

        // Creating intent for native Calendar App
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, itemHolder.title)
                .putExtra(CalendarContract.Events.DESCRIPTION, itemHolder.content);

        Date date = CalendarUtils.parseFromUTC(itemHolder.date);
        if (date != null) {
            long startTime = date.getTime();
            // Some 3rd Party calendar apps need end dates, so add 1 hour if there is no end date.
            long endTime = startTime+3600000;
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        }

        context.startActivity(intent);
    }

}
