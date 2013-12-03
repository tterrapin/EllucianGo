package com.ellucian.mobile.android;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.ellucian.mobile.android.configuration.AbstractModule;
import com.ellucian.mobile.android.configuration.Configuration;
//import com.ellucian.mobile.android.configuration.ConfigurationHandler;
import com.ellucian.mobile.android.configuration.DashboardImagesDownloaderActivity;
import com.ellucian.mobile.android.configuration.Grades;
import com.ellucian.mobile.android.home.HomeActivity;

public class EllucianApplication extends Application {
	public static final String TAG = "Ellucian Mobile";
	private Configuration configuration;
	private DataCache dataCache;
	private ImageLoader imageLoader;
	private long currentNotificationInterval = 0;
	private long currentGradeNotificationInterval = 0;

	public Configuration getConfiguration() {
		if (configuration == null) {
			final SharedPreferences preferences = getSharedPreferences(
					DashboardImagesDownloaderActivity.CONFIGURATION,
					MODE_PRIVATE);
			final String xml = preferences.getString("configXml", null);
			if (xml != null) {
				configuration = Configuration.parseConfiguration(xml);
//				final ConfigurationHandler xmlHandler = new ConfigurationHandler();
//				final SAXParserFactory spf = SAXParserFactory.newInstance();
//				SAXParser sp;
//				try {
//					sp = spf.newSAXParser();
//					final XMLReader xr = sp.getXMLReader();
//					xr.setContentHandler(xmlHandler);
//					xr.parse(new InputSource(new ByteArrayInputStream(xml
//							.getBytes())));
//					configuration = xmlHandler.getConfiguration();
//				} catch (final ParserConfigurationException e) {
//					Log.e(TAG, "Pase Configuration Error: " + e);
//				} catch (final SAXException e) {
//					Log.e(TAG, "Pase Configuration Error: " + e);
//				} catch (final IOException e) {
//					Log.e(TAG, "Pase Configuration Error: " + e);
//				}
			}
		}
		return configuration;
	}

	public DataCache getDataCache() {
		final Configuration configuration = getConfiguration();
		if (dataCache == null && configuration != null) {
			dataCache = new DataCache(this, configuration.getShortInterval(),
					configuration.getLongInterval());
		}
		return dataCache;
	}

	public ImageLoader getImageLoader() {
		if (imageLoader == null) {
			imageLoader = new ImageLoader(this);
		}
		return imageLoader;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d(TAG, "Low memory");
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		dataCache = null;
	}

	public void startBackgroundUpdateServices(boolean forceStart) {
		final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		final Configuration configuration = getConfiguration();

		long newGradesNotifiationInterval = configuration
				.getGradesNotificationInterval();
		long newNotificatioInterval = configuration.getNotificationInterval();

		if (forceStart || newGradesNotifiationInterval != currentGradeNotificationInterval
				|| newNotificatioInterval != currentNotificationInterval) {

			for (final AbstractModule module : configuration.getModules()) {
				final Intent service = module.buildService(this);
				if (service != null) {
					final SharedPreferences preferences = getSharedPreferences(
							HomeActivity.CONFIGURATION, MODE_PRIVATE);
					service.putExtra("appName",
							preferences.getString("displayName", ""));
					final PendingIntent alarmSender = PendingIntent.getService(
							getApplicationContext(), 0, service, 0);
					if (module instanceof Grades
							&& configuration.getGradesNotificationInterval() > 0) {
						am.setRepeating(AlarmManager.ELAPSED_REALTIME,
								SystemClock.elapsedRealtime(),
								configuration.getGradesNotificationInterval(),
								alarmSender);

					} else if (configuration.getNotificationInterval() > 0) {
						am.setRepeating(AlarmManager.ELAPSED_REALTIME,
								SystemClock.elapsedRealtime(),
								configuration.getNotificationInterval(),
								alarmSender);

					}
				}
			}
		}
		currentGradeNotificationInterval = currentNotificationInterval = configuration
				.getNotificationInterval();
	}
}
