package com.ellucian.mobile.android.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LocalMobileClient {
//	private static MobileClient instance;
	Gson jsonParser;
	Resources resources;
	
	public LocalMobileClient(Resources resources) {
		this.resources = resources;
		GsonBuilder builder = new GsonBuilder();
		//builder.registerTypeAdapter(Date.class, new DateDeserializer());
		builder.setDateFormat("yyyy-MM-dd HH:mm:ss.S");
		jsonParser = builder.create();
		
	}
	
//	public static MobileClient getInstance() {
//		if(instance == null) {
//			instance = new MobileClient();
//		} 
//		return instance;
//	}
	
	private String loadRawJson(int id) {
		InputStream is = resources.openRawResource(id);
		String json = "";
		try {
			json = IOUtils.toString(is);
		} catch (IOException e) {
			Log.e("MobileClient", "Unable to read raw resource: " + id);
		}
		return json;
	}
	
	public String getConfiguration(int id) {
		String configurationString = loadRawJson(id);
		return configurationString;
	}
	
//	public List<Term> getTermsWithGrades() {
//		Log.d("MobileClient", "Retrieving terms and grades");
//		List<Term> terms;
//		terms = getTerms();
//		for (Term term : terms) {
//			term.courses = getGrades(term.id).courses;
//		}
//		return terms;
//	}
	
//	public List<Term> getTerms() {
//		Log.d("MobileClient", "Retrieving terms");
//		List<Term> response;
//		// Stored data for 
//		// http://msdev.sghedu.com/banner-mobileserver/rest/term/50
//		//String termResponse = "[{\"id\":\"201210\",\"name\":\"Fall 2011 201210\",\"startDate\":\"2011-09-01 00:00:00.0\",\"endDate\":\"2011-09-01 00:00:00.0\"},{\"id\":\"201115\",\"name\":\"Fall-Winter 201115\",\"startDate\":\"2010-12-02 00:00:00.0\",\"endDate\":\"2010-12-02 00:00:00.0\"},{\"id\":\"201110\",\"name\":\"Fall 2010 201110\",\"startDate\":\"2010-09-01 00:00:00.0\",\"endDate\":\"2010-09-01 00:00:00.0\"},{\"id\":\"201030\",\"name\":\"Summer 2010 201030\",\"startDate\":\"2010-06-01 00:00:00.0\",\"endDate\":\"2010-06-01 00:00:00.0\"},{\"id\":\"201020\",\"name\":\"Spring 2010 201020\",\"startDate\":\"2010-01-01 00:00:00.0\",\"endDate\":\"2010-01-01 00:00:00.0\"}]";
//		//Resources.
//		
//		//String termResponse = "[{\"id\":\"201210\",\"name\":\"Fall 2011 201210\",\"startDate\":\"2011-09-01 00:00:00.0\",\"endDate\":\"2011-12-31 00:00:00.0\"},{\"id\":\"201115\",\"name\":\"Fall-Winter 201115\",\"startDate\":\"2010-12-02 00:00:00.0\",\"endDate\":\"2011-01-02 00:00:00.0\"},{\"id\":\"201110\",\"name\":\"Fall 2010 201110\",\"startDate\":\"2010-09-01 00:00:00.0\",\"endDate\":\"2010-12-01 00:00:00.0\"},{\"id\":\"201030\",\"name\":\"Summer 2010 201030\",\"startDate\":\"2010-06-01 00:00:00.0\",\"endDate\":\"2010-08-01 00:00:00.0\"},{\"id\":\"201020\",\"name\":\"Spring 2010 201020\",\"startDate\":\"2010-01-01 00:00:00.0\",\"endDate\":\"2010-05-01 00:00:00.0\"}]";
//		// parse the JSON from the server from the terms response
//		String termResponse = loadRawJson(R.raw.gradeterm);
//		
//		Term[] terms = jsonParser.fromJson(termResponse, Term[].class);
//		if(terms != null) {
//			Log.d("MobileClient", "Terms returned: " + terms.length);
//			response = Arrays.asList(terms);
//		} else {
//			Log.d("MobileClient", "No terms returned");
//			response = new ArrayList<Term>();
//		}
//		return response;
//	}
	
//	public GradesResponse getGrades(String termId) {
//		Log.d("MobileClient", "Retrieving grades for term: " + termId);
//		String gradesResponse = null;
//		if(termId.equals("2012/FA")) {
//			gradesResponse = loadRawJson(R.raw.gradefall);
//		} else if(termId.equals("2012/S1")) {
//			gradesResponse = loadRawJson(R.raw.gradesummer);
//		} else if(termId.equals("2012/SP")) {
//			gradesResponse = loadRawJson(R.raw.gradespring);
////		if(termId.equals("201210")) {
////			// http://msdev.sghedu.com/banner-mobileserver/rest/grade/50?termId=201210
////			// Need to add additional data for the student once services are running again
////			//gradesResponse = "{\"student\":{\"id\":\"50\",\"name\":null},\"term\":{\"id\":\"201210\",\"name\":\"Fall 2011 201210\",\"startDate\":\"2011-09-01 00:00:00.0\",\"endDate\":\"2011-09-01 00:00:00.0\"},\"grades\":[{\"courseId\":\"1016\",\"courseTitle\":\"ASTR 115\",\"courseDescription\":\"Basic Astronomy\",\"instructorName\":\"Whilomenia Dacton\",\"grade\":\"B\",\"creditHours\":\"4\",\"courseSection\":\"1016\",\"instructorId\":\"210392817\"},{\"courseId\":\"1019\",\"courseTitle\":\"C S 105\",\"courseDescription\":\"Intro to Computing - Business\",\"instructorName\":\"Karlotta Jesen\",\"grade\":\"B\",\"creditHours\":\"3\",\"courseSection\":\"1019\",\"instructorId\":\"082220005\"},{\"courseId\":\"1021\",\"courseTitle\":\"DANC 1000\",\"courseDescription\":\"Dancing with the StarZ\",\"instructorName\":\"Sharon Eide\",\"grade\":\"*C\",\"creditHours\":\"20\",\"courseSection\":\"1021\",\"instructorId\":\"LACCD1111\"},{\"courseId\":\"1022\",\"courseTitle\":\"ECON 111.3\",\"courseDescription\":\"Price Theory/Rsrce Allocation\",\"instructorName\":\"Joyce Pearson\",\"grade\":\"W\",\"creditHours\":\"3\",\"courseSection\":\"1022\",\"instructorId\":\"770499003\"}],\"previousTermId\":\"201115\",\"nextTermId\":null}";
////			//gradesResponse = "{\"student\":{\"id\":\"50\",\"name\":null},\"term\":{\"id\":\"201210\",\"name\":\"Fall 2011 201210\",\"startDate\":\"2011-09-01 00:00:00.0\",\"endDate\":\"2011-12-31 00:00:00.0\"},\"courses\":[{\"courseId\":\"1016\",\"courseTitle\":\"ASTR 115\",\"courseDescription\":\"Basic Astronomy\",\"grade\":\"B\",\"creditHours\":\"4\",\"courseSection\":\"1016\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1019\",\"courseTitle\":\"C S 105\",\"courseDescription\":\"Intro to Computing - Business\",\"grade\":\"B\",\"creditHours\":\"3\",\"courseSection\":\"1019\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1021\",\"courseTitle\":\"DANC 1000\",\"courseDescription\":\"Dancing with the StarZ\",\"grade\":\"*C\",\"creditHours\":\"20\",\"courseSection\":\"1021\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1022\",\"courseTitle\":\"ECON 111.3\",\"courseDescription\":\"Price Theory/Rsrce Allocation\",\"grade\":\"W\",\"creditHours\":\"3\",\"courseSection\":\"1022\",\"instructors\":[],\"midtermgrades\":[]}],\"previousTermId\":\"201115\",\"nextTermId\":null}";
////			gradesResponse = loadRawJson(R.raw.term_50_201210);
////		} else if(termId.equals("201115")) {
////			// http://msdev.sghedu.com/banner-mobileserver/rest/grade/50?termId=201115
////			//gradesResponse = "{\"student\":{\"id\":\"50\",\"name\":null},\"term\":{\"id\":\"201115\",\"name\":\"Fall-Winter 201115\",\"startDate\":\"2010-12-02 00:00:00.0\",\"endDate\":\"2011-01-02 00:00:00.0\"},\"courses\":[{\"courseId\":\"1001\",\"courseTitle\":\"ACCT 100\",\"courseDescription\":\"Introduction to Accounting\",\"grade\":\"A\",\"creditHours\":\"3\",\"courseSection\":\"1001\",\"instructors\":[],\"grades\":[{\"type\":\"FINAL\",\"name\":\"Final grade\",\"value\":\"A\"}]},{\"courseId\":\"1009\",\"courseTitle\":\"ECON 111.3\",\"courseDescription\":\"Price Theory/Rsrce Allocation\",\"grade\":\"A\",\"creditHours\":\"3\",\"courseSection\":\"1009\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1013\",\"courseTitle\":\"MATH 3333\",\"courseDescription\":\"Mathematics and the Galaxy ZZZ\",\"grade\":\"A\",\"creditHours\":\"4\",\"courseSection\":\"1013\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1018\",\"courseTitle\":\"ENGR 100\",\"courseDescription\":\"Intro to General Engineering\",\"grade\":\"B\",\"creditHours\":\"1\",\"courseSection\":\"1018\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1019\",\"courseTitle\":\"PSYC 100\",\"courseDescription\":\"Introduction to Psychology\",\"grade\":\"A\",\"creditHours\":\"3\",\"courseSection\":\"1019\",\"instructors\":[],\"midtermgrades\":[]}],\"previousTermId\":\"201110\",\"nextTermId\":\"201210\"}"; 
////			gradesResponse = loadRawJson(R.raw.term_50_201115);
////		} else if(termId.equals("201110")) {
////			// http://msdev.sghedu.com/banner-mobileserver/rest/grade/50?termId=201110
////			gradesResponse = "{\"student\":{\"id\":\"50\",\"name\":null},\"term\":{\"id\":\"201110\",\"name\":\"Fall 2010 201110\",\"startDate\":\"2010-09-01 00:00:00.0\",\"endDate\":\"2010-12-01 00:00:00.0\"},\"courses\":[{\"courseId\":\"1006\",\"courseTitle\":\"C S 105\",\"courseDescription\":\"Intro to Computing - Business\",\"grade\":\"D\",\"creditHours\":\"3\",\"courseSection\":\"1006\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1007\",\"courseTitle\":\"CHEM 1000\",\"courseDescription\":\"Chemisty Placement\",\"grade\":\"P\",\"creditHours\":\"4\",\"courseSection\":\"1007\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1010\",\"courseTitle\":\"FREN 300\",\"courseDescription\":\"Year Abroad\",\"grade\":\"I-LP\",\"creditHours\":\"120\",\"courseSection\":\"1010\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1011\",\"courseTitle\":\"GBUS 000\",\"courseDescription\":\"Business Orientation\",\"grade\":\"A\",\"creditHours\":\"1\",\"courseSection\":\"1011\",\"instructors\":[],\"midtermgrades\":[]}],\"previousTermId\":\"201030\",\"nextTermId\":\"201115\"}";
////			
////		} else if(termId.equals("201030")) {
////			// http://msdev.sghedu.com/banner-mobileserver/rest/grade/50?termId=201030
////			gradesResponse = "{\"student\":{\"id\":\"50\",\"name\":null},\"term\":{\"id\":\"201030\",\"name\":\"Summer 2010 201030\",\"startDate\":\"2010-06-01 00:00:00.0\",\"endDate\":\"2010-08-01 00:00:00.0\"},\"courses\":[{\"courseId\":\"1001\",\"courseTitle\":\"ACCT 100\",\"courseDescription\":\"Introduction to Accounting\",\"grade\":\"C\",\"creditHours\":\"3\",\"courseSection\":\"1001\",\"instructors\":[],\"midtermgrades\":[]}],\"previousTermId\":\"201020\",\"nextTermId\":\"201110\"}";
////		} else if(termId.equals("201020")) {
////			// http://msdev.sghedu.com/banner-mobileserver/rest/grade/50?termId=201020
////			gradesResponse = "{\"student\":{\"id\":\"50\",\"name\":null},\"term\":{\"id\":\"201020\",\"name\":\"Spring 2010 201020\",\"startDate\":\"2010-01-01 00:00:00.0\",\"endDate\":\"2010-05-01 00:00:00.0\"},\"courses\":[{\"courseId\":\"1001\",\"courseTitle\":\"ACCT 100\",\"courseDescription\":\"Introduction to Accounting\",\"grade\":\"A\",\"creditHours\":\"3\",\"courseSection\":\"1001\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1002\",\"courseTitle\":\"ASTR 115\",\"courseDescription\":\"Basic Astronomy\",\"grade\":\"D\",\"creditHours\":\"4\",\"courseSection\":\"1002\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1004\",\"courseTitle\":\"BIOL 100\",\"courseDescription\":\"Biology for Non-Science Majors\",\"grade\":\"P\",\"creditHours\":\"4\",\"courseSection\":\"1004\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1005\",\"courseTitle\":\"BIOL 100\",\"courseDescription\":\"Biology for Non-Science Majors\",\"grade\":\"C\",\"creditHours\":\"4\",\"courseSection\":\"1005\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1006\",\"courseTitle\":\"C S 105\",\"courseDescription\":\"Intro to Computing - Business\",\"grade\":\"A\",\"creditHours\":\"3\",\"courseSection\":\"1006\",\"instructors\":[],\"midtermgrades\":[]},{\"courseId\":\"1009\",\"courseTitle\":\"ECON 111.3\",\"courseDescription\":\"Price Theory/Rsrce Allocation\",\"grade\":\"C\",\"creditHours\":\"3\",\"courseSection\":\"1009\",\"instructors\":[],\"midtermgrades\":[]}],\"previousTermId\":null,\"nextTermId\":\"201030\"}";
//		} else {
//			return new GradesResponse();
//		}
//		GradesResponse response = jsonParser.fromJson(gradesResponse, GradesResponse.class);
//		return response;
//	}
//	
//	public MapsResponse getMaps() {
//		Log.d("MobileClient", "Retrieving maps");
//		String mapsResponse = loadRawJson(R.raw.maps);
//		MapsResponse response = jsonParser.fromJson(mapsResponse, MapsResponse.class);
//		return response;
//	}
//	
//	public AboutInfo getAboutInfo() {
//		Log.d("MobileClient", "Retrieving about information");
//		String aboutResponse = loadRawJson(R.raw.about);
//		AboutInfo aboutInfo = jsonParser.fromJson(aboutResponse, AboutInfo.class);
//		return aboutInfo;
//	}
//	
//	public NotificationsResponse getNotifications() {
//		Log.d("MobileClient", "Retrieving notifications");
//		String notificationsResponse = loadRawJson(R.raw.notifications);
//		NotificationsResponse response = jsonParser.fromJson(notificationsResponse, NotificationsResponse.class);
//		return response;
//	}
//	
//	public NumbersResponse getNumbers() {
//		Log.d("MobileClient", "Retrieving numbers");
//		String numbersResponse = loadRawJson(R.raw.important_numbers);
//		NumbersResponse response = jsonParser.fromJson(numbersResponse, NumbersResponse.class);
//		return response;
//	}
//	
//	public NewsResponse getNews() {
//		Log.d("MobileClient", "Retrieving news");
//		String newsResponse = loadRawJson(R.raw.feeds);
//		NewsResponse response = jsonParser.fromJson(newsResponse, NewsResponse.class);
//		return response;
//	}
//	
//	public CoursesResponse getCourses() {
//		GsonBuilder builder = new GsonBuilder();
//		//builder.registerTypeAdapter(Date.class, new DateDeserializer());
//		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//		Gson jsonParser = builder.create();
//		
//		Log.d("MobileClient", "Retrieving courses");
//		String coursesResponse = loadRawJson(R.raw.courses);
//		CoursesResponse response = jsonParser.fromJson(coursesResponse, CoursesResponse.class);
//		return response;
//	}
//	
//	public EventsResponse getEvents() {
//		// Events json has a different date format. Didnt want to mess with the default parser.
//		GsonBuilder builder = new GsonBuilder();
//		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
//		Gson parser = builder.create();
//		
//		Log.d("MobileClient", "Retrieving events");
//		String eventsResponse = loadRawJson(R.raw.events);
//		
//		EventsResponse response = null;
//		JSONObject jsonObject = null;
//		try {
//			jsonObject = new JSONObject(eventsResponse);
//		} catch (JSONException e1) {
//			Log.d("MobileClient", "Text from events.json not parsed correctly");
//		}
//		if (jsonObject != null) {
//			JSONArray keys = jsonObject.names();
//			JSONArray eventsForDate = null;
//			JSONObject eventJson = null;
//			response = new EventsResponse();
//			
//			for (int n = 0; n < keys.length(); n++) {
//				try {
//					String date = keys.getString(n);
//					eventsForDate = jsonObject.getJSONArray(date);
//					//Log.d("EVENT", eventsForDate.toString());
//					
//					for (int i = 0; i < eventsForDate.length(); i++) {
//						eventJson = eventsForDate.getJSONObject(i);
//						//Log.d("eventJson", eventJson.toString());
//						Event event = parser.fromJson(eventJson.toString(), Event.class);
//						//dateEvents.events[i] = event;
//						response.events.add(event);
//					}				
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return response;
//	}
	
//	private class DateDeserializer implements JsonDeserializer<Date> {
//		private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
//		      throws JsonParseException {
//			
//			Date date = null;
//			try {
//				date = format.parse(json.getAsJsonPrimitive().getAsString());
//			} catch (ParseException e) {
//				
//			} 
//			return date;	
//		}
//	}
}
