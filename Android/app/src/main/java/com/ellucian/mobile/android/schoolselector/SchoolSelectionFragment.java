/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.schoolselector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.FilteredAdapter;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.configurationlist.Configuration;
import com.ellucian.mobile.android.client.configurationlist.ConfigurationListResponse;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SchoolSelectionFragment extends EllucianFragment {

	private static final String PREFERENCES_CLOUD_URL = "cloudUrl";
	private static final String PREFERENCES_FILENAME = "SchoolSelection";
	
	// live configurations
	private String configurationListUrl = "";

	private FilteredAdapter<Configuration> adapter = null;
	private ArrayList<Configuration> configurationList;
	private SchoolSelectionActivity activity;
	private String query;
	private View rootView;
	private ListView listView;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof SchoolSelectionActivity)) {
			throw new IllegalStateException("Activity must implement SchoolSelectionActivity");
		}
		this.activity = (SchoolSelectionActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_school_selection,  container, false);
		listView = (ListView)rootView.findViewById(R.id.school_selection_list_view);
		listView.setEmptyView(rootView.findViewById(R.id.school_selection_empty_view));
		return rootView;
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    // Setting fields from the configuration file. See res/xml/configuration_properties.xml
	    configurationListUrl = getConfigurationProperties().configurationListUrl;
	   
	    if (savedInstanceState != null) {
		    // do we have anything to restore?
	    }

        String cloudUrl = null;
        Uri data = this.activity.getIntent().getData();
        if(data != null) {
        	List<String> segments = data.getPathSegments();
        	cloudUrl = TextUtils.join("/", segments.subList(2, segments.size()));
        	cloudUrl = cloudUrl.replaceFirst("/", "://");
        	Utils.addStringToPreferences(this.activity, PREFERENCES_FILENAME, PREFERENCES_CLOUD_URL, cloudUrl);
        } else {
        	cloudUrl = Utils.getStringFromPreferences(this.activity, PREFERENCES_FILENAME, PREFERENCES_CLOUD_URL, configurationListUrl);
        }

		if (configurationList != null) {
			adapter = getAdapter(this.activity, android.R.layout.simple_list_item_1, configurationList);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> list, View v, int position, long id) {					
					getEllucianActivity().sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Choose Institution", null, null);

					final Configuration configuration = (Configuration) listView.getAdapter().getItem(position);
					Intent intent = new Intent(getActivity(), ConfigurationLoadingActivity.class);
					intent.putExtra(Utils.CONFIGURATION_URL,  configuration.configurationUrl);
					intent.putExtra(Utils.CONFIGURATION_NAME,  configuration.name);
					intent.putExtra(Utils.ID, configuration.id);
					startActivity(intent);
				}
			});

		} else {
			new DownloadTask().execute(cloudUrl);
		}
    }
   
	public class DownloadTask extends AsyncTask< String, Void, ArrayList<Configuration> > {
		
		private boolean outdated = false;
		private boolean upgradeAvailable = false;
		
		@Override
		protected ArrayList<Configuration> doInBackground(String... urls) {
			
			MobileClient client = new MobileClient(activity);
			ConfigurationListResponse response = client.getConfigurationList(urls[0]);
			
			//Version checking
			if (getConfigurationProperties().enableVersionChecking) {
				if(response != null && response.versions != null) {
					try {
	
						ArrayList<String> supportedVersions = new ArrayList<String>(Arrays.asList(response.versions.android));  
	
						String appVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
					
						String[] appVersionComponents = appVersion.split("\\.");
						String appVersionWithoutBuildNumber = appVersionComponents[0] + "." + appVersionComponents[1] + "." + appVersionComponents[2];
						String latestSupportedVersion = supportedVersions.get(supportedVersions.size()-1);
						String[] latestSupportedVersionComponents = latestSupportedVersion.split("\\.");
	
						outdated = true;
						if(latestSupportedVersion.equals(appVersionWithoutBuildNumber)) {
							//current
							outdated = false;
						} else if(supportedVersions.contains(appVersionWithoutBuildNumber)) {
							//supported
							//only alert the user once
							if(!latestSupportedVersion.equals(ConfigurationUpdateService.latestVersionToCauseAlert)) {
								upgradeAvailable = true;
								ConfigurationUpdateService.latestVersionToCauseAlert = latestSupportedVersion;
							}
							outdated = false;
						} else if(appVersionComponents.length > 0 && latestSupportedVersionComponents.length > 0 && Integer.parseInt(appVersionComponents[0]) > Integer.parseInt(latestSupportedVersionComponents[0])) {
						    //app newer than what server returns
							outdated = false;
						} else if(appVersionComponents.length > 0 && latestSupportedVersionComponents.length > 0 && Integer.parseInt(appVersionComponents[0]) == Integer.parseInt(latestSupportedVersionComponents[0])) {
							if(appVersionComponents.length > 1 && latestSupportedVersionComponents.length > 1 && Integer.parseInt(appVersionComponents[1]) > Integer.parseInt(latestSupportedVersionComponents[1])) {
								//app newer than what server returns
								outdated = false;
							} else if(appVersionComponents.length > 1 && latestSupportedVersionComponents.length > 1 && Integer.parseInt(appVersionComponents[1]) == Integer.parseInt(latestSupportedVersionComponents[1])) {
								if(appVersionComponents.length > 2 && latestSupportedVersionComponents.length > 2 && Integer.parseInt(appVersionComponents[2]) > Integer.parseInt(latestSupportedVersionComponents[2])) {
									//app newer than what server returns
									outdated = false;
					            }
					        }
					    }
					} catch (NameNotFoundException e) {
						Log.e("SchoolSelectionActivity", "Unable to get versionName");
					}
				}
			}
			
			//Capture Google Analytics id
			if(response != null && response.analytics != null) {
				Utils.addStringToPreferences(activity.getBaseContext(), Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1, response.analytics.ellucian);
			}

			ArrayList<Configuration> configurationList = null;
			if(response != null && !outdated) {
				configurationList = response.getConfigurationList(null);
			}
			return configurationList;
		}

		@Override
		protected void onPostExecute(ArrayList<Configuration> configurationList) {

			super.onPostExecute(configurationList);
			
			//Google Analytics - need to wait for the tracker id to use before logging, which is why its here and not onStart
            getEllucianActivity().sendViewToTracker1("Show Institution List", null);

			if (configurationList != null && !configurationList.isEmpty()) {
				adapter = getAdapter(activity, android.R.layout.simple_list_item_1, configurationList);
				listView.setAdapter(adapter);
				filterSearch();
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> list, View v, int position, long id) {
						//super.onItemClick(list, v, position, id);
						final Configuration configuration = (Configuration) listView.getAdapter().getItem(position);
						Intent intent = new Intent(getActivity(), ConfigurationLoadingActivity.class);
						intent.putExtra(Utils.CONFIGURATION_URL,  configuration.configurationUrl);
						intent.putExtra(Utils.CONFIGURATION_NAME,  configuration.name);
						intent.putExtra(Utils.ID, configuration.id);
						startActivity(intent);
					}
				});
			} else {
				listView.setAdapter(null);
			}
			
			if(outdated) {
				outdated();
			}
			if(upgradeAvailable) {
				upgradeAvailable();
			}
		}
		
		private void upgradeAvailable() {
			new AlertDialog.Builder(SchoolSelectionFragment.this.activity)
			.setTitle(R.string.version_outdated)
			.setMessage(R.string.version_outdated_message)
			.setNegativeButton(android.R.string.cancel,null)
			.setPositiveButton(R.string.version_upgrade,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0,
								int arg1) {
							Utils.sendMarketIntent(SchoolSelectionFragment.this.activity, false);
						}

					}).create().show();
		}
		
		private void outdated() {
			new AlertDialog.Builder(SchoolSelectionFragment.this.activity)
			.setTitle(R.string.version_upgrade)
			.setMessage(R.string.version_force_upgrade_message)
			.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0,
								int arg1) {
							Log.v("SchoolSelectionActivity", "User cancelled force upgrade");
							SchoolSelectionFragment.this.activity.finish();
							android.os.Process.killProcess(android.os.Process.myPid());
						}

					})
			.setPositiveButton(R.string.version_upgrade,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0,
								int arg1) {
							Utils.sendMarketIntent(SchoolSelectionFragment.this.activity, true);
						}

					}).create().show();
		}
	}

	private FilteredAdapter<Configuration> getAdapter(Context context, int textViewResourceId, List<Configuration> objects) {
		return new FilteredAdapter<Configuration>(context, textViewResourceId,
				objects) {

			@Override
			public CharSequence getText(Configuration obj) {
				return obj.name;
			}

			private boolean containsQueryString(String queryString,
					String valueText) {
				valueText = valueText.toLowerCase(Locale.getDefault());
				if (valueText.contains(queryString)) {
					return true;
				} else {
					final String[] words = valueText.split(" ");
					final int wordCount = words.length;

					for (int k = 0; k < wordCount; k++) {
						if (words[k].contains(queryString)) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public boolean matchesFilter(Configuration config,
					String queryString, int position) {
				if (containsQueryString(queryString, config.name)) {
					return true;
				} else {
					for (int k = 0; k < config.keywords.length; k++) {
						if (containsQueryString(queryString, config.keywords[k])) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}
	
	public boolean doQuery(String query) {
		this.query = query;
		filterSearch();
		return true;
	}
	
	private void filterSearch() {
		if (adapter != null && query != null) {
			adapter.getFilter().filter(query);
		}
	}

}
