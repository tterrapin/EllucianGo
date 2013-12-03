package com.ellucian.mobile.android.courses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ImageLoader;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.Utils;

public class CourseAnnouncementContentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_announcement_detail);

		UICustomizer.style(this);

		final Intent intent = getIntent();
		((TextView) findViewById(R.id.newsTitle)).setText(getIntent()
				.getStringExtra("title"));
		((TextView) findViewById(R.id.newsContent)).setText(getIntent()
				.getStringExtra("content"));
		final ImageView img = (ImageView) findViewById(R.id.newsImage);

		final String imageUrl = intent.getStringExtra("image");
		if (imageUrl == null) {
			img.setVisibility(View.GONE);
		} else {
			final Bitmap cachedImage = ((EllucianApplication) getApplication())
					.getImageLoader().loadImage(imageUrl,
							new ImageLoader.ImageLoadedListener() {
								public void imageLoaded(Bitmap imageBitmap) {
									img.setImageBitmap(imageBitmap);
								}
							});
			if (cachedImage != null) {
				img.setImageBitmap(cachedImage);
			}
		}

		final String website = intent.getStringExtra("website");
		final Button button = (Button) findViewById(R.id.showWebsiteButton);
		if (website == null) {
			button.setVisibility(View.GONE);
		} else {
			final Uri data = Uri.parse(website);
			final Intent websiteIntent = new Intent(
					Intent.ACTION_VIEW, data);
			if (Utils.isIntentAvailable(this, websiteIntent)) {
				button.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						
						startActivity(websiteIntent);
					}
				});
			} else {
				button.setVisibility(View.GONE);
			}
		}

	}
}
