/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.configurationlist;


public class Configuration implements Comparable<Configuration> {
	public String id;
	public String name;
	public String configurationUrl;
	public String[] keywords;

	@Override
    public int compareTo(Configuration o) {
        Configuration f = (Configuration)o;
        return name.compareToIgnoreCase(f.name);
    }
}
