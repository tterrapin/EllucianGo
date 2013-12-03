package com.ellucian.mobile.android.news;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.ImageLoader;

public class NewsFeedAdapter extends BaseAdapter {
	private final List<NewsItem> items;
	private final LayoutInflater mInflater;
	private Context context;
	private ImageLoader imageLoader;

	public NewsFeedAdapter(Context context, List<NewsItem> items, ImageLoader imageLoader) {

		this.items = items;
		this.context = context;
		this.imageLoader = imageLoader;
		mInflater = LayoutInflater.from(context);
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

		final NewsItem item = items.get(position);
		final String url = item.getImage();

		convertView = mInflater.inflate(R.layout.news_list_row, null);

		final TextView newsTitle = (TextView) convertView
				.findViewById(R.id.newsTitle);
		final TextView newsTeaser = (TextView) convertView
				.findViewById(R.id.newsTeaser);
		final TextView newsDate = (TextView) convertView
				.findViewById(R.id.newsDate);
		final ImageView newsImage = (ImageView) convertView
				.findViewById(R.id.newsImage);

		newsTitle.setText(item.getTitle());
		final String content = item.getContent();
		if (content != null) {
			newsTeaser.setText(item.getContent());
		} else {
			newsTeaser.setVisibility(View.GONE);
		}
		final Date date = item.getDate().getTime();
		final java.text.DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);
		final java.text.DateFormat timeFormat = android.text.format.DateFormat
				.getTimeFormat(context);
		newsDate.setText(dateFormat.format(date) + " "
				+ timeFormat.format(date));

		if (url == null) {
			newsImage.setVisibility(View.GONE);

		} else {
			final Bitmap cachedImage = imageLoader.loadImage(item.getImage(),
							new ImageLoader.ImageLoadedListener() {

								public void imageLoaded(Bitmap imageBitmap) {
									newsImage.setImageBitmap(imageBitmap);
									notifyDataSetChanged();
								}
							});
			if (cachedImage != null) {
				newsImage.setImageBitmap(cachedImage);
			}
		}
		return convertView;

	}

}