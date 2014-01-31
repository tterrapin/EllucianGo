package com.ellucian.mobile.android.notifications;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class NotificationsDetailFragment extends EllucianDefaultDetailFragment {

	private View rootView;
	
	public NotificationsDetailFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_notifications_detail, container, false);


		Bundle args = getArguments();
		// bundle should contain:
		// TITLE, CONTENT, LINK_LABEL, LINK, DATE attributes
		// which matches to 
		// title, details, hyperlink, linkLabel, notificationDate
		
		String title = null;
		String details = null;
		String linkLabel = null;
		String hyperlink = null;
		String notificationDate = null;
		
		if (args != null) {
			title = args.getString(Extra.TITLE);
			details = args.getString(Extra.CONTENT);
			linkLabel = args.getString(Extra.LINK_LABEL);
			hyperlink = args.getString(Extra.LINK);
			notificationDate = args.getString(Extra.DATE);
			
	        View outerHeader = (View) rootView.findViewById(R.id.header_layout);
	        outerHeader.setBackgroundColor(Utils.getAccentColor(getActivity()));

			if (!TextUtils.isEmpty(title)) {
				TextView titleView = (TextView) rootView.findViewById(R.id.title);
				titleView.setText(title);
			}
			if (!TextUtils.isEmpty(notificationDate)) {
				TextView dateView = (TextView) rootView.findViewById(R.id.notificationDate);
				dateView.setText(notificationDate);
			}
			if (!TextUtils.isEmpty(details)) {
				TextView contentView = (TextView) rootView.findViewById(R.id.details);
				contentView.setAutoLinkMask(Utils.getAvailableLinkMasks(getActivity(), Linkify.ALL));
				contentView.setText(details);
			}
			Button button = (Button) rootView.findViewById(R.id.actionButton);
			if (!TextUtils.isEmpty(hyperlink)) {
				if (hyperlink.startsWith("HTTPS")) {
					hyperlink = "https" + hyperlink.substring(5);
				} else {
					hyperlink = "http" + hyperlink.substring(4);
				}
				final Intent websiteIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(hyperlink));
				boolean intentAvailable = Utils.isIntentAvailable(getActivity(),  websiteIntent);
				if (intentAvailable) {
					button.setText(linkLabel);
					button.setOnClickListener(new View.OnClickListener() {
						public void onClick(View arg0) {
							sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_FOLLOW_WEB, "Open notification in web frame", null, getEllucianActivity().moduleName);
							startActivity(websiteIntent);
						}
					});
				} else {
					button.setVisibility(View.GONE);
				}
			} else {
				button.setVisibility(View.GONE);
			}
		}

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
	}
}
