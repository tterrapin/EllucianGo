// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.client.registration;

import com.ellucian.mobile.android.client.ResponseObject;

public class EligibilityResponse implements ResponseObject<EligibilityResponse> {
	public boolean eligible;
	public Message[] messages;
	public EligibleTerm[] terms;
	
}
