/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.configurationlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ellucian.mobile.android.client.ResponseObject;

public class ConfigurationListResponse implements ResponseObject<ConfigurationListResponse>{
	private Institution[] institutions;
	public Version versions;
	public Analytics analytics;
	
	public ArrayList<Configuration> getConfigurationList(ArrayList<String> institutionNames) {
		List<Institution> institutionList = new ArrayList<Institution>();
		ArrayList<Configuration> configurationList = new ArrayList<Configuration>();		
		
		if (institutionNames != null && institutionNames.size() != 0) {
			for (int i = 0; i < institutions.length; i++) {
				if (institutionNames.contains(institutions[i].name)) {
					institutionList.add(institutions[i]);
				}
			}
		} else {			
			institutionList = Arrays.asList(institutions);
		}
		
		
		for (int i = 0; i < institutionList.size(); i++) {
			Institution institution = institutions[i];
			for (int n = 0; n < institution.configurations.length; n++) {
				configurationList.add(institution.configurations[n]);
			}
		}
		Collections.sort(configurationList);
		if (!configurationList.isEmpty()) {
			return configurationList;
		} else {
			return null;
		}
	}
}
