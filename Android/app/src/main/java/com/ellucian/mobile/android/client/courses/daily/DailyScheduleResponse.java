/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.daily;

import com.ellucian.mobile.android.client.ResponseObject;

import java.io.Serializable;

public class DailyScheduleResponse implements ResponseObject<DailyScheduleResponse>, Serializable {
	public Day[] coursesDays;
	
}
