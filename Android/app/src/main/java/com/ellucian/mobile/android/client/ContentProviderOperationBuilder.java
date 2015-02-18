package com.ellucian.mobile.android.client;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

public abstract class ContentProviderOperationBuilder<E> {
	Context context;
	public ContentProviderOperationBuilder(Context context) {
		this.context = context;
	}
	
	public abstract ArrayList<ContentProviderOperation> buildOperations(E model);
	
}
