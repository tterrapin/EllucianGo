/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.directory;

import com.ellucian.mobile.android.client.ResponseObject;

public class DirectoryResponse implements ResponseObject<DirectoryResponse>{
	//public boolean resultTruncated;
	//public boolean containsSecureData;
	public Entry[] entries;
}
