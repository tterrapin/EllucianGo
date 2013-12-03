package com.ellucian.mobile.android.home;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.CacheMonitor;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ImageLoader;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.Utils;
import com.ellucian.mobile.android.auth.LoginActivity;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.configuration.AbstractModule;
import com.ellucian.mobile.android.configuration.Configuration;
import com.ellucian.mobile.android.configuration.ConfigurationAdapter;
//import com.ellucian.mobile.android.configuration.ConfigurationHandler;
import com.ellucian.mobile.android.configuration.IAlertModule;
import com.ellucian.mobile.android.schoolselector.InstitutionSelectorActivity;

public class HomeActivity extends Activity {
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(modulesAdapter != null) {
				modulesAdapter.notifyDataSetChanged();
			}
		}
		
	};

	public class DownloadTask extends AsyncTask<String, Void, Configuration> {

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
					((EllucianApplication) getApplication())
							.setConfiguration(configuration);

					((EllucianApplication) getApplication()).getDataCache()
							.putCache(urls[0], contents, configuration);
					return configuration;
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "XML Pasing Exception = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Configuration configuration) {

			super.onPostExecute(configuration);
			if (configuration != null) {
				applyConfiguration(configuration);
			} else {
				Log.e(EllucianApplication.TAG,
						"Can't retrieve configuration from network");
				if (preferences.contains("configXml")) {
					Log.i(EllucianApplication.TAG, "Using stored preferences");
					final String contents = preferences.getString("configXml",
							"");
					configuration = parseConfiguration(contents);
					applyConfiguration(configuration);
				} else {
					Log.i(EllucianApplication.TAG, "Showing alert");
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							HomeActivity.this);
					builder.setMessage(
							"Unable to download current configuration")
							.setCancelable(false)
							.setNeutralButton("Choose new school",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											showInstitutionSelector();
											finish();
										}
									});
					final AlertDialog alertDialog = builder.create();
					alertDialog.show();
				}
			}
		}

	}

	//2.0 json version
	private Configuration parseConfiguration(String contents) {
		Configuration configuration = Configuration.parseConfiguration(contents);
		
		if(LoginUtil.isLoggedIn(getApplicationContext())) {
			((EllucianApplication)getApplication()).startBackgroundUpdateServices(false);
		}

		return configuration;
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

			final SharedPreferences.Editor editor = preferences.edit();
			editor.putString("configXml", contents);
			editor.commit();

			Configuration configuration = xmlHandler.getConfiguration();
			if(LoginUtil.isLoggedIn(getApplicationContext())) {
				((EllucianApplication)getApplication()).startBackgroundUpdateServices(false);
			}

			return configuration;
		} catch (final Exception e) {
			Log.e(EllucianApplication.TAG, e.toString());
		}
		return null;
	}
	*/

	

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mIntentReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mIntentReceiver, intentFilter);
	}



	public static final String CONFIGURATION = "configuration";
	private ConfigurationAdapter modulesAdapter;

	private SharedPreferences preferences;
	private IntentFilter intentFilter;

	private void applyConfiguration(Configuration configuration) {
		((EllucianApplication) getApplication()).setConfiguration(configuration);
		// set colors
		Configuration.Color color = configuration.getPrimaryColor();
		UICustomizer.setPrimaryColor(Color.rgb(color.getRed(),
				color.getGreen(), color.getBlue()));
		color = configuration.getSecondaryColor();
		UICustomizer.setSecondaryColor(Color.rgb(color.getRed(),
				color.getGreen(), color.getBlue()));
		color = configuration.getAccentColor();
		UICustomizer.setAccentColor(Color.rgb(color.getRed(), color.getGreen(),
				color.getBlue()));

		final ImageView logo = (ImageView) findViewById(R.id.schoolLogo);
		if (logo != null) {
		//	(findViewById(R.id.separatorBottom))
			//		.setBackgroundColor(UICustomizer.accentColor);

			if (configuration.getLogoUrl() != null) {

				Bitmap cachedImage = ((EllucianApplication) getApplication())
						.getImageLoader().loadImage(configuration.getLogoUrl(),
								new ImageLoader.ImageLoadedListener() {

									public void imageLoaded(Bitmap imageBitmap) {

										logo.setImageBitmap(imageBitmap);

									}

								});

				if (cachedImage != null) {

					logo.setImageBitmap(cachedImage);

				}
				UICustomizer.styleBackground(logo);
			} else {
				logo.setVisibility(View.GONE);
			}
		}

		this.setTitle(preferences.getString("displayName", "Home"));

		UICustomizer.style(this);
		UICustomizer.styleBackground(findViewById(R.id.homeLayout));

		final GridView view = (GridView) findViewById(R.id.homeLayout);
		modulesAdapter = new ConfigurationAdapter(this, configuration,
				((EllucianApplication) getApplication()).getImageLoader(), ((EllucianApplication) getApplication()).getDataCache());
		view.setAdapter(modulesAdapter);

		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(
				roundedCorners, null, null));
		pgDrawable.getPaint().setColor(UICustomizer.accentColor);
		view.setSelector(pgDrawable);

		view.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				final AbstractModule m = (AbstractModule) modulesAdapter
						.getItem(position);
				Intent intent = m.buildIntent(HomeActivity.this);
				if(Utils.isIntentAvailable(HomeActivity.this, intent)) {
					startActivity(intent);
				} else {
					String error = getResources().getString(R.string.webAppUnsupported);
					Log.e(EllucianApplication.TAG, error);
					
					Toast.makeText(HomeActivity.this, error, Toast.LENGTH_LONG).show();
				}

			}
		});

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		final ViewGroup layout = (ViewGroup) findViewById(R.id.mainLayout);
		layout.setBackgroundColor(UICustomizer.primaryColor);

		intentFilter = new IntentFilter(IAlertModule.ACTION);
		
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				CacheMonitor.class);
		startService(intent);

		configure();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reselect:
			showInstitutionSelector();
			break;
		case R.id.info:
			final Intent aboutIntent = new Intent(HomeActivity.this,
					InfoActivity.class);
			startActivity(aboutIntent);
			break;
		case R.id.menu_login:
			final Intent loginIntent = new Intent(HomeActivity.this,
					LoginActivity.class);
			startActivity(loginIntent);
			modulesAdapter.notifyDataSetChanged();
			break;
		case R.id.menu_logout:
			LoginUtil.logout(getApplication());
			modulesAdapter.notifyDataSetChanged();
			break;

		}
		return false;
	}

	protected void configure() {

		preferences = getSharedPreferences(CONFIGURATION, MODE_PRIVATE);

		if (!preferences.contains("configUrl")) {
			showInstitutionSelector();
			finish();
		} else {
			String configUrl = preferences.getString("configUrl", null);

			final DataCache cache = ((EllucianApplication) getApplication())
					.getDataCache();

			if (cache != null) {

				final boolean current = cache.isCurrentLongInterval(this,
						configUrl);

				String configurationXml = cache.getCache(this, configUrl);

				if (!current || configurationXml == null) {
					new DownloadTask().execute(configUrl);
				} else {
					applyConfiguration(parseConfiguration(configurationXml));
				}

			} else {
				// first time through
				new DownloadTask().execute(configUrl);
			}
		}
	}

	private void showInstitutionSelector() {
		final Intent intentSetup = new Intent(HomeActivity.this,
				InstitutionSelectorActivity.class);
		startActivity(intentSetup);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		final boolean allowLogin = LoginUtil.allowLogin(getApplication());
		menu.findItem(R.id.menu_login).setVisible(!loggedIn && allowLogin);
		menu.findItem(R.id.menu_logout).setVisible(loggedIn && allowLogin);
		return super.onPrepareOptionsMenu(menu);
	}

	public void onRestart() {
		super.onRestart();
		//modulesAdapter.notifyDataSetChanged();
		configure();
	}
}