package com.ellucian.mobile.android;

import java.util.HashMap;

import com.ellucian.mobile.android.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UICustomizer {

	public static int accentColor;
	public static HashMap<String, Bitmap> images = new HashMap<String, Bitmap>();
	public static int primaryColor;

	public static int secondaryColor;

	public static void reset() {
		primaryColor = 0;
		secondaryColor = 0;
		accentColor = 0;
	}

	public static void setAccentColor(int rgb) {
		accentColor = rgb;

	}

	public static void setPrimaryColor(int rgb) {
		primaryColor = rgb;

	}

	public static void setProgressBarVisible(Activity activity,
			boolean visibility) {

		Log.v(EllucianApplication.TAG, "Set progress bar visible: "
				+ visibility);

		final ProgressBar progressBar = (ProgressBar) activity
				.findViewById(R.id.titlebar_progress_circular);
		progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);

	}

	public static void setSecondaryColor(int rgb) {
		secondaryColor = rgb;

	}

	public static void style(Activity activity) {

		final TextView title = (TextView) activity
				.findViewById(R.id.titlebar_title);
		final View separator = activity.findViewById(R.id.titlebar_separator);
		final ProgressBar progressBar = (ProgressBar) activity
				.findViewById(R.id.titlebar_progress_circular);
		final View container = activity.findViewById(R.id.titlebar_container);
		separator.setBackgroundColor(accentColor);

		title.setText(activity.getTitle());
		title.setTextColor(secondaryColor);

		progressBar.setDrawingCacheBackgroundColor(primaryColor);
		progressBar.setBackgroundColor(primaryColor);

		container.setBackgroundColor(primaryColor);

		separator.setBackgroundColor(accentColor);

	}

	public static void styleBackground(View view) {
		view.setBackgroundColor(primaryColor);
	}
}
