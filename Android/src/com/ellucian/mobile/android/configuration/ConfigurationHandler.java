//package com.ellucian.mobile.android.configuration;
//
//import org.xml.sax.Attributes;
//import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;
//
//import android.util.Log;
//
//import com.ellucian.mobile.android.EllucianApplication;
//import com.ellucian.mobile.android.configuration.Configuration.Color;
//
//public class ConfigurationHandler extends DefaultHandler {
//
//	private class SearchScope {
//		private String name;
//		private String searchUrl;
//		private String profileUrl;
//	}
//
//	private Color color;
//	private final Configuration configuration = new Configuration();
//	private boolean currentElement;
//	private String currentValue;
//	private boolean inImages;
//	private boolean inAlertImages;
//	private boolean inUrls;
//	private SearchScope searchScope;
//	private AbstractModule module;
//
//	@Override
//	public void characters(char[] ch, int start, int length)
//			throws SAXException {
//		if (currentElement) {
//			currentValue += new String(ch, start, length);
//		}
//	}
//
//	@Override
//	public void endElement(String uri, String localName, String qName)
//			throws SAXException {
//		trimCurrentValue();
//		
//		currentElement = false;
//		if (localName.equalsIgnoreCase("red")) {
//			color.setRed(currentValue);
//		} else if (localName.equalsIgnoreCase("green")) {
//			color.setGreen(currentValue);
//		} else if (localName.equalsIgnoreCase("blue")) {
//			color.setBlue(currentValue);
//		} else if (localName.equalsIgnoreCase("logo")) {
//			configuration.setLogoUrl(currentValue);
//		} else if (localName.equalsIgnoreCase("authentication")) {
//			configuration.setAuthenticationUrl(currentValue);
//		} else if (localName.equalsIgnoreCase("images")) {
//			inImages = false;
//		} else if (inImages && localName.equalsIgnoreCase("android")) {
//			module.setImageUrl(currentValue);
//		} else if (module instanceof IAlertModule && inAlertImages && localName.equalsIgnoreCase("android")) {
//			((IAlertModule)module).setAlertImageUrl(currentValue);
//		} else if (localName.equalsIgnoreCase("name")) {
//			if(searchScope != null) {
//				searchScope.name = currentValue;
//			} else {
//				module.setName(currentValue);
//			}
//		} else if (localName.equalsIgnoreCase("version")) {
//			module.addVersion(currentValue);
//		} else if (localName.equalsIgnoreCase("helpdeskEmail")) {
//			configuration.setHelpdeskEmail(currentValue);
//		} else if (localName.equalsIgnoreCase("helpdeskPhone")) {
//			configuration.setHelpdeskPhone(currentValue);
//		} else if (localName.equalsIgnoreCase("helpdeskWebsite")) {
//			configuration.setHelpdeskWebsite(currentValue);
//		} else if (localName.equalsIgnoreCase("address")) {
//			configuration.setAddress(currentValue);
//		} else if (localName.equalsIgnoreCase("url")) {
//			if (module instanceof Notifications) {
//				((Notifications) module).setUrl(currentValue);
//			} else if (module instanceof ImportantNumbers) {
//				((ImportantNumbers) module).setUrl(currentValue);
//			} else if (module instanceof Maps) {
//				((Maps) module).setUrl(currentValue);
//			} else if (module instanceof Grades) {
//				((Grades) module).setUrl(currentValue);
//			} else if (module instanceof WebApplication) {
//				((WebApplication) module).setUrl(currentValue);
//			}
//		} else if (localName.equalsIgnoreCase("searchUrl") && module instanceof Directory && searchScope != null) {
//			searchScope.searchUrl = currentValue;
//		} else if (localName.equalsIgnoreCase("profileUrl") && module instanceof Directory && searchScope != null) {
//			searchScope.profileUrl = currentValue;
//		} else if (localName.equalsIgnoreCase("urls")) {
//			inUrls = false;
//		} else if (inUrls) {
//			if (module instanceof News) {
//				if (localName.equalsIgnoreCase("public")) {
//					((News) module).setPublicUrl(currentValue);
//				} else if (localName.equalsIgnoreCase("authenticated")) {
//					((News) module).setAuthenticatedUrl(currentValue);
//				}
//			} else if (module instanceof Events) {
//				if (localName.equalsIgnoreCase("public")) {
//					((Events) module).setPublicUrl(currentValue);
//				} else if (localName.equalsIgnoreCase("authenticated")) {
//					((Events) module).setAuthenticatedUrl(currentValue);
//				}
//			}
//		} else if (localName.equalsIgnoreCase("short")) {
//			configuration.setShortInterval(Integer.parseInt(currentValue));
//		} else if (localName.equalsIgnoreCase("long")) {
//			configuration.setLongInterval(Integer.parseInt(currentValue));
//		} else if (localName.equalsIgnoreCase("notification")) {
//			configuration.setNotificationInterval(Integer.parseInt(currentValue));
//		} else if (localName.equalsIgnoreCase("gradesNotification")) {
//			configuration.setGradesNotificationInterval(Integer.parseInt(currentValue));
//		} else if (localName.equalsIgnoreCase("visible")) {
//			module.setEnabled(Boolean.parseBoolean(currentValue));
//		} else if (localName.equals("notifications")) {
//			configuration.addModule(module);
//		} else if (localName.equals("importantnumbers")) {
//			configuration.addModule(module);
//		} else if (localName.equals("news")) {
//			configuration.addModule(module);
//		} else if (localName.equals("events")) {
//			configuration.addModule(module);
//		} else if (localName.equals("maps")) {
//			try
//		    {
//		        // check if Google Maps is supported on given device
//		        Class.forName("com.google.android.maps.MapActivity");
//
//		        configuration.addModule(module);
//		    }
//		    catch (ClassNotFoundException e)
//		    {
//		        Log.w(EllucianApplication.TAG, e.getLocalizedMessage());
//		    }
//			
//		} else if (localName.equals("directory")) {
//			configuration.addModule(module);
//		} else if (localName.equals("courses")) {
//			configuration.addModule(module);
//		} else if (localName.equals("grades")) {
//			configuration.addModule(module);
//		} else if (localName.equals("webapp")) {
//			configuration.addModule(module);
//		} else if (localName.equals("profileUrl") && module instanceof Directory) {
//			((Directory) module).setProfileUrl(currentValue);
//		} else if (localName.equalsIgnoreCase("scope")  && module instanceof Directory) {
//			((Directory) module).addSearchScope(searchScope.name, searchScope.searchUrl, searchScope.profileUrl);
//			searchScope = null;
//		} else if (localName.equals("coursesUrl") && module instanceof Courses) {
//			((Courses) module).setCoursesUrl(currentValue);
//		} else if (localName.equals("assignmentsUrl") && module instanceof Courses) {
//			((Courses) module).setAssignmentsUrl(currentValue);
//		} else if (localName.equals("rosterUrl") && module instanceof Courses) {
//			((Courses) module).setRosterUrl(currentValue);
//		} else if (localName.equals("rosterProfileUrl") && module instanceof Courses) {
//			((Courses) module).setRosterProfileUrl(currentValue);
//		} else if (localName.equals("announcementsUrl") && module instanceof Courses) {
//			((Courses) module).setAnnouncementsUrl(currentValue);
//		} else if (localName.equals("eventsUrl") && module instanceof Courses) {
//			((Courses) module).setEventsUrl(currentValue);
//		}
//	}
//
//	private void trimCurrentValue() {
//		if(currentValue == null) return;
//		currentValue = currentValue.trim();
//		if(currentValue.length() == 0) currentValue = null;
//	}
//
//	public Configuration getConfiguration() {
//		return configuration;
//	}
//
//	@Override
//	public void startElement(String uri, String localName, String qName,
//			Attributes attributes) throws SAXException {
//		currentElement = true;
//		if (localName.equals("primary")) {
//			color = configuration.getPrimaryColor();
//		} else if (localName.equals("secondary")) {
//			color = configuration.getSecondaryColor();
//		} else if (localName.equals("accent")) {
//			color = configuration.getAccentColor();
//		} else if (localName.equals("authentication")) {
//			configuration.setAuthenticationType(attributes.getValue("type"));
//		} else if (localName.equals("images")) {
//			inImages = true;
//		} else if (localName.equals("alertImages")) {
//			inAlertImages = true;
//		} else if (localName.equals("notifications")) {
//			module = new Notifications();
//		} else if (localName.equals("importantnumbers")) {
//			module = new ImportantNumbers();
//		} else if (localName.equals("news")) {
//			module = new News();
//		} else if (localName.equals("events")) {
//			module = new Events();
//		} else if (localName.equals("maps")) {
//			module = new Maps();
//		} else if (localName.equals("directory")) {
//			module = new Directory();
//		} else if (localName.equals("courses")) {
//			module = new Courses();
//		} else if (localName.equals("grades")) {
//			module = new Grades();
//		} else if (localName.equals("webapp")) {
//			module = new WebApplication();
//		} else if (localName.equals("urls")) {
//			inUrls = true;
//		} else if (localName.equals("scope")) {
//			searchScope = new SearchScope();
//		} else if (localName.equals("helpdeskWebsite")) {
//			configuration.setHelpdeskWebsiteLabel(attributes.getValue("label"));
//		} else if (localName.equals("helpdeskEmail")) {
//			configuration.setHelpdeskEmailLabel(attributes.getValue("label"));
//		} else if (localName.equals("helpdeskPhone")) {
//			configuration.setHelpdeskPhoneLabel(attributes.getValue("label"));
//		}
//		currentValue = "";
//	}
//
//}