package com.ellucian.mobile.android.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This activity is for a sample custom module.
 * 
 * @author jhigley
 *
 */

// Activities should extend EllucianActivity for all default Navigation Drawer Menu setup, Actionbar colors and icons.
public class SampleFlickrActivity extends EllucianActivity {
	
	public static final String TAG = SampleFlickrActivity.class.getSimpleName();	
	private static final String API_KEY = "apiKey";
	private static final String USER_ID = "userId";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the layout of your activity.
		// Make sure your import to the right resource file.
		// In this case we want the import to com.ellucian.elluciango.R and not android.R
		setContentView(R.layout.activity_sample_flickr);
		
		// You can set the title of the module to display in the toolbar either here programmatically or
		// in the AndroidManifest using android:label property.
		// moduleName is a preset variable in the parent EllucianActivity class
		setTitle(moduleName);
		
		// Get intent-extras out of intent
		// These are set in either configuration xml or the Mobile Cloud Application
		Intent intent = getIntent();
		// Pull out the apiKey that you set in the configuration xml
		String apiKey = intent.getStringExtra(API_KEY);
		// Pull out the userId that you set in the Cloud Application
		String userId = intent.getStringExtra(USER_ID);
		
		// This is the url for the request to the flickr api with String.format placeholders
		String urlWithFormatPlaceholders = "https://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos" +
				"&api_key=%1s&per_page=1&format=json&nojsoncallback=1&user_id=%2s&extras=description,date_taken,url_m";
		
		
		String requestUrl = String.format(urlWithFormatPlaceholders, apiKey, userId);
		Log.d(TAG, "Making request to the Flickr api: " + requestUrl);
		
		// Create a task object to perform a asynchronous call to the flickr api to retrieve image meta-data
		// See class below
		RetrieveFlickrImageAndInfoTask task = new RetrieveFlickrImageAndInfoTask();
		task.execute(requestUrl);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// In order to track usage of this page in Google Analytics you call the sendView method
		// For more info about the Google Analytics usage see the EllucianActivity and 
		// view the Google Analytics api at https://developers.google.com/analytics/devguides/collection/
		sendView("Sample flickr Page", moduleName);
	}
	
	// Create an class that extends AsyncTask to retrieve your image and data.
	// Questions on how the AsyncTask works please read the Android Developers documents at http://developer.android.com/	
	private class RetrieveFlickrImageAndInfoTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			// Pull the requestUrl from the params
			String requestUrl = params[0];
			// Create a MobileClient object to make requests
			MobileClient client = new MobileClient(getApplication());
			// Get back a response in the form of a JSONObject. Please read JSONObject in the Android Developers documents
			JSONObject responseObject = client.makeDefaultJsonRequest(requestUrl);
				
			return responseObject;
		}
		
		@Override
		protected void onPostExecute(JSONObject resultObject) {
			if (resultObject != null) {
				
				try {
					// Parse the JSONObject to get the correct info you need
					// In this case we are looking for a image url to download our image from,
					// the description to display below the image,
					// and the time stamp of when the picture was taken.
					JSONObject photosObject = resultObject.getJSONObject("photos");
					JSONArray photoArray = photosObject.getJSONArray("photo");
					JSONObject firstImageObject = photoArray.getJSONObject(0);
					String imageUrl = firstImageObject.getString("url_m");
					JSONObject descriptionObejct = firstImageObject.getJSONObject("description");
					String content = descriptionObejct.getString("_content");
					String dateTaken = firstImageObject.getString("datetaken");
					
					if (!TextUtils.isEmpty(imageUrl)) {
						
						displayImage(imageUrl);					
					} else {
						Log.e(TAG, "imageUrl is missing");
					}
					
					if (!TextUtils.isEmpty(content)) {
						TextView descriptionView = (TextView) findViewById(R.id.descriptionView);
						
						// Using the Utils class to get the colors that were set in the Mobile Cloud Application.
						// The Utils class has many convenience methods to access cloud set info. 
						descriptionView.setTextColor(Utils.getAccentColor(SampleFlickrActivity.this));
						descriptionView.setText(content);					
					} else {
						Log.e(TAG, "content is missing");
					}
					
					if (!TextUtils.isEmpty(dateTaken)) {
						TextView dateView = (TextView) findViewById(R.id.dateView);
						
						// Using the Utils class to get the colors that were set in the Mobile Cloud Application.
						// The Utils class has many convenience methods to access cloud set info. 
						dateView.setTextColor(Utils.getAccentColor(SampleFlickrActivity.this));
						dateView.setText(dateTaken);					
					} else {
						Log.e(TAG, "dateTaken is missing");
					}
										
				} catch (JSONException e) {
					Log.e(TAG, "JSONException: ", e);
				}
							
			} else {
				Log.e(TAG, "response object is null");
			}
			
		}
		
		private void displayImage(String imageUrl) {
			
			// Using the Android Query library for image loading, see the Android Query web site for more info
			// https://code.google.com/p/android-query/
			// Create a AQuery object 
			AQuery aq = new AQuery(SampleFlickrActivity.this);
			
			// Check to see if the image is already in cache before downloading.
			Bitmap bit = aq.getCachedImage(imageUrl);
			if (bit != null) {
				Log.d(TAG, "Image found in cache");
				Drawable draw = new BitmapDrawable(getResources(), bit);
				aq.id(R.id.imageView).image(draw);
			} else {
				Log.d(TAG, "Image not found in cache, downloading");
				// Using AQuery object target the correct ImageView and give it a url of the image to download
				// This will asynchronously download and display the image in the ImageView on its own.
				aq.id(R.id.imageView).image(imageUrl);
			}
			
		}
	}

}
