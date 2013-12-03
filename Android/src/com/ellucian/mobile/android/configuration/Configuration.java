package com.ellucian.mobile.android.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONObject;

import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.auth.LoginUtil;

public class Configuration {
	public class Color {
		private int blue;
		private int green;
		private int red;

		public Color () {
			
		}
		public Color (int red, int green, int blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		public int getBlue() {
			return blue;
		}

		public int getGreen() {
			return green;
		}

		public int getRed() {
			return red;
		}

		public void setBlue(int blue) {
			this.blue = blue;
		}

		public void setBlue(String blue) {
			this.blue = Integer.parseInt(blue);
		}

		public void setGreen(int green) {
			this.green = green;
		}

		public void setGreen(String green) {
			this.green = Integer.parseInt(green);
		}

		public void setRed(int red) {
			this.red = red;
		}

		public void setRed(String red) {
			this.red = Integer.parseInt(red);
		}
	}

	private  Color accent = new Color();
	private String address;
	@SuppressWarnings("unused")
	private String authenticationType;
	private String authenticationUrl;
	private String helpdeskEmail;
	private String helpdeskEmailLabel;
	private String helpdeskPhone;
	private String helpdeskPhoneLabel;
	private String helpdeskWebsite;
	private String helpdeskWebsiteLabel;
	private String logoUrl;
	private long longInterval = 1440 * 1000 * 60;
	private final ArrayList<AbstractModule> modules = new ArrayList<AbstractModule>();
	private long notificationInterval = 1440 * 1000 * 60;
	private long gradesNotificationInterval = 1440 * 1000 * 60;
	private  Color primary = new Color();
	private  Color secondary = new Color();
	private long shortInterval = 60 * 1000 * 60;

	public void addModule(AbstractModule module) {
		if (module.hasSupportedVersion() && module.isEnabled()) {
			modules.add(module);
		}
		Collections.sort(modules, new Comparator<AbstractModule>(){
			  public int compare(AbstractModule s1, AbstractModule s2) {
			    return Integer.valueOf(s1.getIndex()).compareTo(s2.getIndex());
			  }
			});	}
	

	public Color getAccentColor() {
		return accent;
	}

	public String getAddress() {
		return address;
	}

	public String getAuthenticationUrl() {
		return authenticationUrl;
	}

	public String getHelpdeskEmail() {
		return helpdeskEmail;
	}

	public String getHelpdeskEmailLabel() {
		return helpdeskEmailLabel;
	}

	public String getHelpdeskPhone() {
		return helpdeskPhone;
	}

	public String getHelpdeskPhoneLabel() {
		return helpdeskPhoneLabel;
	}

	public String getHelpdeskWebsite() {
		return helpdeskWebsite;
	}

	public String getHelpdeskWebsiteLabel() {
		return helpdeskWebsiteLabel;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public long getLongInterval() {
		return longInterval;
	}

	public ArrayList<AbstractModule> getModules() {
		return modules;
	}

	public long getNotificationInterval() {
		return notificationInterval;
	}
	
	public long getGradesNotificationInterval() {
		return gradesNotificationInterval;
	}

	public Color getPrimaryColor() {
		return primary;
	}

	public Color getSecondaryColor() {
		return secondary;
	}

	public long getShortInterval() {
		return shortInterval;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAuthenticationType(String authenticationType) {
		this.authenticationType = authenticationType;
	}

	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}

	public void setHelpdeskEmail(String helpdeskEmail) {
		this.helpdeskEmail = helpdeskEmail;
	}

	public void setHelpdeskEmailLabel(String value) {
		helpdeskEmailLabel = value;
	}

	public void setHelpdeskPhone(String helpdeskPhone) {
		this.helpdeskPhone = helpdeskPhone;
	}

	public void setHelpdeskPhoneLabel(String value) {
		helpdeskPhoneLabel = value;
	}

	public void setHelpdeskWebsite(String helpdeskWebsite) {
		this.helpdeskWebsite = helpdeskWebsite;
	}

	public void setHelpdeskWebsiteLabel(String value) {
		helpdeskWebsiteLabel = value;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public void setLongInterval(long minutes) {
		this.longInterval = minutes * 60 * 1000;
	}

	public void setNotificationInterval(long minutes) {
		this.notificationInterval = minutes * 60 * 1000;
	}

	public void setShortInterval(long minutes) {
		this.shortInterval = minutes * 60 * 1000;
	}
	
	public void setGradesNotificationInterval(long minutes) {
		this.gradesNotificationInterval = minutes * 60 * 1000;
	}

	public static Configuration parseConfiguration(String content) {
		Configuration configuration = new Configuration();
		try {
			JSONObject jsonConfiguration = new JSONObject(content);
			
			JSONObject layout = jsonConfiguration.getJSONObject("layout");
			JSONObject color = layout.getJSONObject("colors");
			
			//TODO
			configuration.primary = configuration.new Color (100,100,100);
			configuration.secondary = configuration.new Color (40,40,40);
			configuration.accent = configuration.new Color (0,0,0);
			
			JSONObject mApps = jsonConfiguration.getJSONObject("mapp");

			Iterator iter = mApps.keys();
		    while(iter.hasNext()){
		        String key = (String)iter.next();
		        JSONObject value = mApps.getJSONObject(key);
		        
		        String localName = value.getString("type");
		        
		        AbstractModule module = null;
				//if (localName.equals("notifications")) {
				//	module = new Notifications();
				//} else 
					if (localName.equals("importantnumbers")) {
					module = new ImportantNumbers();
				} else if (localName.equals("feed")) {
					module = new News();
				} else if (localName.equals("events")) {
					module = new Events();
				} else if (localName.equals("maps")) {
					module = new Maps();
				} else if (localName.equals("directory")) {
					module = new Directory();
				} else if (localName.equals("courses")) {
					module = new Courses();
				//} else if (localName.equals("grades")) {
				//	module = new Grades();
				} else if (localName.equals("webapp")) {
					module = new WebApplication();
				} else {
					continue;
				}
				module.setName(value.getString("name"));
				module.setImageUrl(value.getString("icon"));
				if(value.has("showGuest")) {
					module.setShowForGuest(Boolean.getBoolean(value.getString("showGuest")));
				}
				if(value.has("roles")) {
					String[] roles = value.getString("roles").split(",");
					for(String role : roles) {
						module.addRole(role);
					}
				}
				module.setEnabled(true);
				module.setIndex(Integer.parseInt(value.getString("order")));
				configuration.addModule(module );
			}
			return configuration;
		} catch (Exception e) {
			Log.e(EllucianApplication.TAG, e.getLocalizedMessage());
		}

		return null;
	}
}
