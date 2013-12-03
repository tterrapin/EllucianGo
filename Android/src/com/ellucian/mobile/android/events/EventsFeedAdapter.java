package com.ellucian.mobile.android.events;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;

public class EventsFeedAdapter extends BaseAdapter {
	private final List<Event> items;
	private final LayoutInflater mInflater;
	private DateFormat dateFormat;
	private DateFormat timeFormat;
	private Context context;

	public EventsFeedAdapter(Context context, List<Event> items,
			java.text.DateFormat dateFormat, java.text.DateFormat timeFormat) {

		this.items = items;
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.dateFormat = dateFormat;
		this.timeFormat = timeFormat;
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.events_list_row, null);

			holder = new ViewHolder();
			holder.eventTitle = (TextView) convertView
					.findViewById(R.id.eventTitle);
			holder.eventStartDate = (TextView) convertView
					.findViewById(R.id.eventStartDate);
			holder.eventStartTime = (TextView) convertView
					.findViewById(R.id.eventStartTime);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Event item = items.get(position);

		holder.eventTitle.setText(item.getTitle());

		final Date date = item.getStartDate().getTime();

		holder.eventStartDate.setText(dateFormat.format(date));
		if (item.isAllDay()) {
			holder.eventStartTime.setText(context.getResources().getString(
					R.string.allDay));
		} else {
			holder.eventStartTime.setText(timeFormat.format(date));
		}

		Log.d(EllucianApplication.TAG,
				"Event detail: " + position + " : "
						+ (item.isAllDay() ? "ALL DAY " : "")
						+ holder.eventStartDate.getText() + " "
						+ holder.eventStartTime.getText() + " "
						+ holder.eventTitle.getText());
		return convertView;

	}

}

class ViewHolder {
	TextView eventTitle;
	TextView eventStartDate;
	TextView eventStartTime;
}
