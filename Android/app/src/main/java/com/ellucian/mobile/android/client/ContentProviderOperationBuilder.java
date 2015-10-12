/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

public abstract class ContentProviderOperationBuilder<E> {
	private final Context context;
	protected ContentProviderOperationBuilder(Context context) {
		this.context = context;
	}
	
	public abstract ArrayList<ContentProviderOperation> buildOperations(E model);
	
}
