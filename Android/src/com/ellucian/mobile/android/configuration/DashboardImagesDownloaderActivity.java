package com.ellucian.mobile.android.configuration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.home.HomeActivity;
import com.ellucian.mobile.android.schoolselector.InstitutionSelectorActivity;

public class DashboardImagesDownloaderActivity extends Activity {

	public class ConfigurationDownloadingTask extends
			AsyncTask<String, Void, Configuration> {

		@Override
		protected Configuration doInBackground(String... urls) {

			try {

				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0]));
				final HttpResponse response = client.execute(request);

				final int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					final BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					final StringBuffer sb = new StringBuffer();
					String line = "";
					final String NL = System.getProperty("line.separator");
					while ((line = in.readLine()) != null) {
						sb.append(line + NL);
					}
					in.close();
					final String contents = sb.toString();

					Configuration configuration = parseConfiguration(contents);
					
					final SharedPreferences.Editor editor = preferences.edit();
					editor.putString("configXml", contents);
					editor.commit();
					
					return configuration;
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Configuration Parsing Exception = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Configuration configuration) {
			super.onPostExecute(configuration);
			if (configuration != null) {
				applyConfiguration(configuration);
			} else {
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						DashboardImagesDownloaderActivity.this);
				builder.setMessage("Unable to download current configuration")
						.setCancelable(false)
						.setNeutralButton("Choose new school",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										final Intent intentSetup = new Intent(
												DashboardImagesDownloaderActivity.this,
												InstitutionSelectorActivity.class);
										startActivity(intentSetup);
										finish();
									}
								});
				final AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}

		}
		
		//2.0 json version
		private Configuration parseConfiguration(String content) {
			return Configuration.parseConfiguration(content);
		}
/*
		private Configuration parseConfiguration(String contents) {
			final ConfigurationHandler xmlHandler = new ConfigurationHandler();
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			try {
				sp = spf.newSAXParser();

				final XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(xmlHandler);
				xr.parse(new InputSource(new ByteArrayInputStream(contents
						.getBytes())));

				
				return xmlHandler.getConfiguration();
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, e.getLocalizedMessage());
			}
			return null;
		}
		*/

	}

	private class DownloadImageTask extends AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... urls) {
			final int count = urls.length;

			for (int i = 0; i < count; i++) {
				if(urls[i] != null) {
					((EllucianApplication) DashboardImagesDownloaderActivity.this
						.getApplication()).getImageLoader().loadImage(urls[i],
						false, null);
					publishProgress((int) ((i + 1) / (float) count * 100));
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			final Intent intent = new Intent(
					DashboardImagesDownloaderActivity.this, HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

			startActivity(intent);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressHorizontal.setProgress(progress[0]);
		}

	}

	public static final String CONFIGURATION = "configuration";

	private SharedPreferences preferences;

	private ProgressBar progressHorizontal;

	private void applyConfiguration(Configuration configuration) {

		final Set<String> urls = new HashSet<String>();

		urls.add(configuration.getLogoUrl());

		for (final AbstractModule m : configuration.getModules()) {

			final String image = m.getImageUrl();
			if (image != null) {
				urls.add(image);
			}

		}

		new DownloadImageTask().execute(urls.toArray(new String[urls.size()]));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_images);
		progressHorizontal = (ProgressBar) findViewById(R.id.progress_horizontal);

		preferences = getSharedPreferences(CONFIGURATION, MODE_PRIVATE);

		final String url = preferences.getString("configUrl", null);

		new ConfigurationDownloadingTask().execute(url);
	}

}
