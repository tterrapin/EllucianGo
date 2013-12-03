package com.ellucian.mobile.android.configuration;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.ImageLoader;
import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginUtil;

public class ConfigurationAdapter extends BaseAdapter implements ListAdapter {

	private final Configuration configuration;
	private final ImageLoader imageLoader;

	private final LayoutInflater mInflater;
	private final DataCache dataCache;
	
	private List<AbstractModule> modules;
	
	private final Context context;

	public ConfigurationAdapter(Context context, Configuration configuration,
			ImageLoader imageLoader, DataCache cache) {
		this.configuration = configuration;
		this.imageLoader = imageLoader;
		this.context = context;

		mInflater = LayoutInflater.from(context);
		dataCache = cache;
		
		modules = new ArrayList<AbstractModule>(configuration.getModules());
		for(AbstractModule m : modules) {
			if(!(m.isShowForGuest() || m.getRoles().size() == 0) || m.getRoles().contains(LoginUtil.getRoles(context))) {
				modules.remove(m);
			}
		}
	}

	public int getCount() {
		return modules.size();
	}

	public Object getItem(int position) {

		return modules.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = mInflater.inflate(R.layout.button_layout, null);
		TextView text = (TextView) convertView.findViewById(R.id.textView1);
		final ImageView image = (ImageView) convertView
				.findViewById(R.id.imageView1);

		final AbstractModule m = configuration.getModules().get(position);
		if (m.getImageUrl() != null) {
			
			String imageUrl = m.getImageUrl();
			if(m instanceof Notifications) {
				Notifications notificationModule = ((Notifications)m);
				if(dataCache.hasNotifications(context, notificationModule.getUrl())) {
					imageUrl = notificationModule.getAlertImageUrl();
				}
			}

			Bitmap cachedImage = imageLoader.loadImage(imageUrl,
					new ImageLoader.ImageLoadedListener() {

						public void imageLoaded(Bitmap imageBitmap) {
							image.setImageBitmap(imageBitmap);
							notifyDataSetChanged();
						}
					});

			if (cachedImage != null) {
				image.setImageBitmap(cachedImage);
			}
		}

		text.setText(m.getName());
		text.setTextColor(UICustomizer.secondaryColor);

		return convertView;
	}
}
