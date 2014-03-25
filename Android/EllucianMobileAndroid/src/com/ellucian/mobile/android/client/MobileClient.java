package com.ellucian.mobile.android.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.client.configurationlist.ConfigurationListResponse;
import com.ellucian.mobile.android.client.courses.CoursesResponse;
import com.ellucian.mobile.android.client.courses.announcements.CourseAnnouncementsResponse;
import com.ellucian.mobile.android.client.courses.assignments.CourseAssignmentsResponse;
import com.ellucian.mobile.android.client.courses.daily.DailyScheduleResponse;
import com.ellucian.mobile.android.client.courses.events.CourseEventsResponse;
import com.ellucian.mobile.android.client.courses.overview.CourseRosterResponse;
import com.ellucian.mobile.android.client.directory.DirectoryResponse;
import com.ellucian.mobile.android.client.events.Event;
import com.ellucian.mobile.android.client.events.EventsResponse;
import com.ellucian.mobile.android.client.grades.GradesResponse;
import com.ellucian.mobile.android.client.maps.BuildingsResponse;
import com.ellucian.mobile.android.client.maps.MapsResponse;
import com.ellucian.mobile.android.client.news.NewsResponse;
import com.ellucian.mobile.android.client.notifications.NotificationsResponse;
import com.ellucian.mobile.android.client.numbers.NumbersResponse;
import com.ellucian.mobile.android.client.registration.CartResponse;
import com.ellucian.mobile.android.client.registration.EligibilityResponse;
import com.ellucian.mobile.android.client.registration.SearchResponse;
import com.ellucian.mobile.android.client.registration.TermsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class MobileClient {
	private static final String TAG = MobileClient.class.getSimpleName();
	private Gson jsonParser;
	private EllucianApplication application;
	
	public MobileClient(Application application) {
		GsonBuilder builder = new GsonBuilder();
		//builder.registerTypeAdapter(Date.class, new DateDeserializer());
		builder.setDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
		jsonParser = builder.create();
		if(application instanceof EllucianApplication) {
			this.application = (EllucianApplication)application;
		}
	}
	
	public void setDateFormat(String dateFormat) {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat(dateFormat);
		jsonParser = builder.create();
	}
	
	private String makeServerRequest(String requestUrl, boolean handleErrorCodes) {
		Log.d(TAG + ".makeServerRequest", "Making request at url: " + requestUrl );
		
		HttpURLConnection urlConnection = getConnection(requestUrl);
	
		if (urlConnection != null) {
			urlConnection.setConnectTimeout(20000);
			return handleResponse(urlConnection, handleErrorCodes);
		} else {
			return null;
		}
	}
	
	private String makeAuthenticatedServerRequest(String requestUrl, String username, String password, boolean handleErrorCodes) {
		Log.d(TAG + ".makeAuthenticatedServerRequest", "Making request at url: " + requestUrl );
		
		HttpURLConnection urlConnection = getConnection(requestUrl);
	
		if (urlConnection != null) {
			String auth = null;
			try {
				auth = Base64.encodeToString((username + ":" + password).getBytes("UTF-8"),
																 Base64.NO_WRAP);
				urlConnection.setRequestProperty("Authorization", "Basic " + auth);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException", e);
			}
			urlConnection.setConnectTimeout(20000);
			return handleResponse(urlConnection, handleErrorCodes);
		} else {
			return null;
		}
		
	}
	
	private String makeAuthenticatedServerPut(String requestUrl, String username, String password, boolean handleErrorCodes, String dataToBeWritten) {
		Log.d(TAG + ".makeAuthenticatedServerPut", "Making request at url: " + requestUrl );
		
		HttpURLConnection urlConnection = getConnection(requestUrl);
		
	
		if (urlConnection != null) {
			String auth = null;
			try {
				auth = Base64.encodeToString((username + ":" + password).getBytes("UTF-8"),
																 Base64.NO_WRAP);
				urlConnection.setRequestProperty("Authorization", "Basic " + auth);
				
				urlConnection.setRequestMethod("PUT");
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "application/json");
				urlConnection.setRequestProperty("Accept", "application/json");
		        OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
		        osw.write(dataToBeWritten);
		        osw.flush();
		        osw.close();
		        System.err.println(urlConnection.getResponseCode());
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException", e);
			} catch (ProtocolException e) {
				Log.e(TAG, "ProtocolException", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
			}
			urlConnection.setConnectTimeout(20000);	
			return handleResponse(urlConnection, handleErrorCodes);
		} else {
			return null;
		}
	}
	
	public String makeAuthenticatedServerPost(String requestUrl, String username, String password, boolean handleErrorCodes, String dataToBeWritten) {
		Log.d(TAG + ".makeAuthenticatedServerPost", "Making request at url: " + requestUrl );
		
		HttpURLConnection urlConnection = getConnection(requestUrl);
	
		if (urlConnection != null) {
			String auth = null;
			try {
				auth = Base64.encodeToString((username + ":" + password).getBytes("UTF-8"),
																 Base64.NO_WRAP);
				urlConnection.setRequestProperty("Authorization", "Basic " + auth);
				
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "application/json");
				urlConnection.setRequestProperty("Accept", "application/json");
		        OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
		        osw.write(dataToBeWritten);
		        osw.flush();
		        osw.close();
		        System.err.println(urlConnection.getResponseCode());
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException", e);
			} catch (ProtocolException e) {
				Log.e(TAG, "ProtocolException", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
			}
			urlConnection.setConnectTimeout(20000);	
			return handleResponse(urlConnection, handleErrorCodes);
		} else {
			return null;
		}
	}
	
	private HttpURLConnection getConnection(String requestUrl) {
		HttpURLConnection urlConnection = null;
		URL url = null;
		try {
			url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
		
		return urlConnection;
	}
	
	private String handleResponse(HttpURLConnection urlConnection, boolean handleErrorCodes) {
		String responseString = null;
	
		try {
			int statusCode = urlConnection.getResponseCode();
			Log.d(TAG, "Response code :" + statusCode);
			
			if (statusCode == HttpURLConnection.HTTP_OK) {
				
				storeCookies(urlConnection);
				
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				responseString = readStream(in);
			} else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				Log.e(TAG, "Unauthorized request.");
				if (handleErrorCodes) {
					responseString = "401";
				}
			} else if (statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
				Log.e(TAG, "Forbidden request.");
				if (handleErrorCodes) {
					responseString = "403";
				}
			} else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
				Log.e(TAG, "Resource Not Found.");
				if (handleErrorCodes) {
					responseString = "404";
				}
			} else {
				Log.e(TAG, "Unable to get the json response.");
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} finally {
            urlConnection.disconnect();
		}
		
		return responseString;
	}
	
	private void storeCookies(HttpURLConnection urlConnection) {
		CookieManager webCookieManager = CookieManager.getInstance();
		
		// Pull cookies from connection and insert them into manager
		List<String> cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
	    if (cookieList != null) {
	        for (String cookieString : cookieList) {
	        	
	        	String url = "";
	        	if (cookieString.contains("Domain")) {
					String[] domainSplit = cookieString.split("Domain=");
					String domainHalf = domainSplit[1]; 
					String[] valueSplit = domainHalf.split(";");
					url = valueSplit[0];
					
				} 

	        	Log.d(TAG, "Storing cookie...");
	        	Log.d(TAG, "url : "  + url);
	        	Log.d(TAG, "cookie : " + cookieString);

	            webCookieManager.setCookie(url , cookieString);
	        }
	        
	    }
	}

	private String readStream(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		StringBuilder builder = new StringBuilder();
        try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			builder = null;
		}
        
        return !TextUtils.isEmpty(builder) ? builder.toString() : null;
	}
	
	public <T extends ResponseObject<T>> T getResponseObject(Class<T> clazz, String requestUrl, boolean authenticated) {
		assert !TextUtils.isEmpty(requestUrl) : "requestUrl can not be empty or null";
		
		String jsonString;
		if (authenticated) {
			jsonString = makeAuthenticatedServerRequest(requestUrl, 
					application.getAppUserName(), application.getAppUserPassword(), false);
		} else {
			jsonString = makeServerRequest(requestUrl, false);
		}
		
		if (!TextUtils.isEmpty(jsonString)) {
			try {
				T response = (T) jsonParser.fromJson(jsonString, clazz);
				return response;
			} catch (JsonSyntaxException e) {
				Log.e(TAG + ".getResponse", "JsonSyntaxException", e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	
	/** Configuration, Login, Versions requests */
	
	public ConfigurationListResponse getConfigurationList(String requestUrl) {
		Log.d(TAG, "Retrieving Configuration List");
		return getResponseObject(ConfigurationListResponse.class, requestUrl, false);
	}
	
	public String getConfiguration(String requestUrl) {
		Log.d(TAG, "Retrieving Configuration");
		assert !TextUtils.isEmpty(requestUrl) : "serverUrl can not be empty or null"; 
		
		String responseString = makeServerRequest(requestUrl, true);		
		return responseString;
	}
	
	public String authenticateUser(String requestUrl, String username, String password) {
		Log.d(TAG, "Authenticating User");
		String authResponse = makeAuthenticatedServerRequest(requestUrl, username, password, false);
		
		return authResponse;
		
	}
	
	public String getServerVersion(String requestUrl)  {
		Log.d(TAG, "Retrieving Server Version");
		String aboutResponse = makeServerRequest(requestUrl, false);		
		String version = null;
		
		if (!TextUtils.isEmpty(aboutResponse)) {
			try {
				JSONObject versionJson = new JSONObject(aboutResponse);
				version = versionJson.getJSONObject("application").getString("version");
			} catch (JsonSyntaxException e) {
				Log.e(TAG + ".getServerVersion", "JsonSyntaxException", e);
			} catch (JSONException e) {
				Log.e(TAG + ".getServerVersion", "JSONException", e);
			}
		}
		return version;
	}
	
	/** Default requests */
	
	public JSONObject makeDefaultJsonRequest(String requestUrl) {
		String jsonString = makeServerRequest(requestUrl, false);
		JSONObject jsonObject = null;
		
		if (!TextUtils.isEmpty(jsonString)) {
			try {
				jsonObject = new JSONObject(jsonString);
			} catch (JSONException e) {
				Log.e(TAG + ".makeDefaultJsonRequest", "JsonSyntaxException", e);
			}
		}
		return jsonObject;
	}
	
	public JSONObject makeAuthenticatedJsonRequest(String requestUrl) {
		String jsonString = makeAuthenticatedServerRequest(requestUrl, application.getAppUserName(), 
				application.getAppUserPassword(), false);
		JSONObject jsonObject = null;
		
		if (!TextUtils.isEmpty(jsonString)) {
			try {
				jsonObject = new JSONObject(jsonString);
			} catch (JSONException e) {
				Log.e(TAG + ".makeAuthenticatedDefaultJsonRequest", "JsonSyntaxException", e);
			}
		}
		return jsonObject;
	}
	
	/** Course requests */
	
	public DailyScheduleResponse getDailySchedule(String requestUrl) {
		Log.d(TAG, "Retrieving Daily Schedule");
		return getResponseObject(DailyScheduleResponse.class, requestUrl,  true);
	}
	
	public CoursesResponse getFullSchedule(String requestUrl) {
		Log.d(TAG, "Retrieving Full Schedule");
		return getResponseObject(CoursesResponse.class, requestUrl,  true);
	}
	
	public CoursesResponse getCourseDetails(String requestUrl) {
		Log.d(TAG, "Retrieving Course Details");
		return getResponseObject(CoursesResponse.class, requestUrl,  true);
	}
	
	public CourseRosterResponse getCourseRoster(String requestUrl) {
		Log.d(TAG, "Retrieving Course Roster");
		return getResponseObject(CourseRosterResponse.class, requestUrl,  true);
	}
	
	public CourseAssignmentsResponse getCourseAssignments(String requestUrl) {
		Log.d(TAG, "Retrieving Course Assignments");
		return getResponseObject(CourseAssignmentsResponse.class, requestUrl,  true);
	}
	
	public CourseAnnouncementsResponse getCourseAnnouncements(String requestUrl) {
		Log.d(TAG, "Retrieving Course Announcements");
		return getResponseObject(CourseAnnouncementsResponse.class, requestUrl,  true);
	}
	
	public CourseEventsResponse getCourseEvents(String requestUrl) {
		Log.d(TAG, "Retrieving Course Events");
		return getResponseObject(CourseEventsResponse.class, requestUrl,  true);
	}
	
	/** Directory requests */
	
	public DirectoryResponse searchDirectory(String requestUrl) {
		Log.d(TAG, "Retrieving Directory");
		return getResponseObject(DirectoryResponse.class, requestUrl,  true);
	}
	
	/** Events requests */
	
	public EventsResponse getEvents(String requestUrl) {
		Log.d(TAG, "Retrieving Events");
		assert !TextUtils.isEmpty(requestUrl) : "requestUrl can not be empty or null"; 
		
		String jsonString = makeServerRequest(requestUrl, false);
		
		if (!TextUtils.isEmpty(jsonString)) {
			// Events json has a different date format. Didnt want to mess with the default parser.
			GsonBuilder builder = new GsonBuilder();
			builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			Gson parser = builder.create();
			
			EventsResponse response = null;
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(jsonString);
			} catch (JSONException e1) {
				Log.d(TAG, "json response from server was not parsed correctly");
				return null;
			}
			if (jsonObject != null) {
				JSONArray keys = jsonObject.names();
				JSONArray eventsForDate = null;
				JSONObject eventJson = null;
				
				if (keys != null) {
					response = new EventsResponse();
					for (int n = 0; n < keys.length(); n++) {
						try {
							String date = keys.getString(n);
							eventsForDate = jsonObject.getJSONArray(date);
							//Log.d("EVENT", eventsForDate.toString());
							
							for (int i = 0; i < eventsForDate.length(); i++) {
								eventJson = eventsForDate.getJSONObject(i);
								Event event = parser.fromJson(eventJson.toString(), Event.class);
								//dateEvents.events[i] = event;
								response.events.add(event);
							}				
						} catch (JSONException e) {
							Log.e(TAG + ".getEvents", "JsonSyntaxException", e);
						}
					}
				}
			}
			return response;
		} else {
			return null;
		}	
		
	}
	
	/** Grades requests */
	
	public GradesResponse getGrades(String requestUrl) {	
		Log.d(TAG, "Retrieving Grades");
		return getResponseObject(GradesResponse.class, requestUrl, true);
	}
	
	/** Map requests */
	
	public MapsResponse getMaps(String requestUrl) {
		Log.d(TAG, "Retrieving Maps");
		return getResponseObject(MapsResponse.class, requestUrl,  false);
	}
	
	public BuildingsResponse getBuildings(String requestUrl) {
		Log.d(TAG, "Retrieving Buildings");
		return getResponseObject(BuildingsResponse.class, requestUrl,  false);
	}
	
	/** News requests */
	
	public NewsResponse getNews(String requestUrl) {
		Log.d(TAG, "Retrieving News");
		return getResponseObject(NewsResponse.class, requestUrl, false);
	}
	
	/** Notifications requests */
	
	public NotificationsResponse getNotifications(String requestUrl) {
		Log.d(TAG, "Retrieving Notifications");
		return getResponseObject(NotificationsResponse.class, requestUrl,  true);
	}
		
	/** Numbers requests */
	
	public NumbersResponse getNumbers(String requestUrl) {
		Log.d(TAG, "Retrieving Numbers");
		return getResponseObject(NumbersResponse.class, requestUrl,  false);
	}
	
	/** Registration requests */
	
	public CartResponse getCartList(String requestUrl) {
		Log.d(TAG, "Retrieving Cart List");
		return getResponseObject(CartResponse.class, requestUrl,  true);
	}
	
	public String putCoursesToRegister(String requestUrl, String dataToBeWritten) {
		Log.d(TAG, "Registering Courses");
		return makeAuthenticatedServerPut(requestUrl, application.getAppUserName(), 
				application.getAppUserPassword(), false, dataToBeWritten);
	}
	
	public TermsResponse getOpenTerms(String requestUrl) {
		Log.d(TAG, "Retrieving terms");
		return getResponseObject(TermsResponse.class, requestUrl,  true);
	}
	
	public SearchResponse findSections(String requestUrl) {
		Log.d(TAG, "Searching for sections");
		return getResponseObject(SearchResponse.class, requestUrl,  true);
	}
	
	public EligibilityResponse getEligibility(String requestUrl) {
		Log.d(TAG, "Retrieveing Eligibility");
		return getResponseObject(EligibilityResponse.class, requestUrl,  true);
	}
	
	/** Url Utility Methods  */
	public String addTermAndSectionToUrl(String requestUrl, String termId, String courseId) {
		String modifiedUrl = requestUrl + "?term=" + termId + "&section=" + courseId;
		return modifiedUrl;
	}
	
	public String addUserToUrl(String requestUrl) {
		String modifiedUrl = requestUrl + "/" + application.getAppUserId();
		return modifiedUrl;
	}
}
