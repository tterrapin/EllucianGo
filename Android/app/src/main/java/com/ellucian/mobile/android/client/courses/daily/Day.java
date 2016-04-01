/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.daily;


import java.io.Serializable;

public class Day implements Serializable {
	public String date;
	public Meeting[] coursesMeetings;
}
