/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.numbers;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.Numbers;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersCategories;

public class NumbersBuilder extends ContentProviderOperationBuilder<NumbersResponse> {
	private final String module;
	
	public NumbersBuilder(Context context, String module) {
		super(context);
		this.module = module;
	}

	@Override
	public ArrayList<ContentProviderOperation> buildOperations(NumbersResponse model) {
		
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		final List<String> categories = new ArrayList<String>();
		
		batch.add(ContentProviderOperation.newDelete(Numbers.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		batch.add(ContentProviderOperation.newDelete(NumbersCategories.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		
		
		for (ImportantNumber number : model.numbers) {
			if (!categories.contains(number.category)) {
				categories.add(number.category);
			}
			
			batch.add(ContentProviderOperation
					.newInsert(Numbers.CONTENT_URI)
					.withValue(NumbersCategories.NUMBERS_CATEGORY_NAME, number.category)
					.withValue(Modules.MODULES_ID, module)
					.withValue(Numbers.NUMBERS_EMAIL, number.email)
					.withValue(Numbers.NUMBERS_NAME, number.name)
					.withValue(Numbers.NUMBERS_PHONE, number.phone)
					.withValue(Numbers.NUMBERS_ADDRESS, number.address != null ? number.address.replace("\\n", "\n") : null)
					.withValue(Numbers.NUMBERS_LATITUDE, number.latitude)
					.withValue(Numbers.NUMBERS_LONGITUDE, number.longitude)
					.withValue(Numbers.NUMBERS_BUILDING_ID, number.buildingId) 
					.withValue(Numbers.NUMBERS_CAMPUS_ID, number.campusId)
                    .withValue(Numbers.NUMBERS_EXTENSION, number.extension)
					.build());
		}	
		
		for (String category : categories ) {
			batch.add(ContentProviderOperation
					.newInsert(NumbersCategories.CONTENT_URI)
					.withValue(NumbersCategories.NUMBERS_CATEGORY_NAME, category)
					.withValue(Modules.MODULES_ID, module)
					.build());
		}
		
		return batch;
		
	}
	
}
