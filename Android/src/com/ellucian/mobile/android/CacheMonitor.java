package com.ellucian.mobile.android;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ellucian.mobile.android.configuration.AbstractModule;
import com.ellucian.mobile.android.configuration.Configuration;

/**
 * Service to monitor the "fake" cache and drop images created awhile ago, but
 * keeping the images used for the home screen buttons
 */
public class CacheMonitor extends Service {

	private static final String TIMER_NAME = "CacheMonitor";
	protected static final int EXPIRATION_TIME = 24 * 60 * 60 * 1000; // one day
	private static final long INTERVAL = 60 * 60 * 1000L; // one hour

	private Timer timer;

	private final TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			Log.i(EllucianApplication.TAG, "Cache Monitor checking cache");
			try {
				final Set<String> keepUrls = new HashSet<String>();
				final Configuration configuration = ((EllucianApplication) getApplication())
						.getConfiguration();
				if(configuration == null) return;
				
				for (final AbstractModule m : configuration.getModules()) {
					if(m.getImageUrl() != null) {
						keepUrls.add(m.getImageUrl());
					}
				}
				final ImageLoader imageLoader = ((EllucianApplication) getApplication())
						.getImageLoader();
				imageLoader.expireCache(EXPIRATION_TIME, keepUrls);

			} catch (final Throwable t) {
				Log.e(EllucianApplication.TAG,
						"Cache monitor failed to clean up cache", t);
			}
		}
	};

	/**
	 * This is a background service, not for activities to bind to
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		timer = new Timer(TIMER_NAME);
		timer.schedule(updateTask, INTERVAL, INTERVAL);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(EllucianApplication.TAG, "Cache Monitor checking cache");

		timer.cancel();
		timer = null;
	}

}
