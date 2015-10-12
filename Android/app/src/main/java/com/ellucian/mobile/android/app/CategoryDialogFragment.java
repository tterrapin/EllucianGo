/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.elluciango.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryDialogFragment extends EllucianDialogFragment {
	private CategoryDialogListener listener;
	private String[] startingFilteredCategories;

	public interface CategoryDialogListener {
		String FILTERED_CATEGORIES = "filtered_categories";
		String CATEGORY_DIALOG = "category_dialog";
		String[] getAllCategories();
		String[] getFilteredCategories();
		void updateFilteredCategories(String[] filteredCategories);
	}
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			listener = (CategoryDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement CategoryDialogListener");
		}
	}
		
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d("CategoryDialogFragment.onCreateDialog", "Creating dialog");
		final String[] currentCategories = listener.getAllCategories();
		final String[] currentFiltered = listener.getFilteredCategories();
		
		final ArrayList<Integer> selectedPositions = new ArrayList<>();
		boolean[] checkedItems = new boolean[currentCategories.length];

		if (currentFiltered != null) {
			
			startingFilteredCategories = new String[currentFiltered.length];
			System.arraycopy(currentFiltered, 0, startingFilteredCategories, 0, currentFiltered.length);
			
			List<String> filteredList = Arrays.asList(currentFiltered);
			for (int i = 0; i < currentCategories.length; i++) {				
				if (filteredList.contains(currentCategories[i])) {
					checkedItems[i] = false;
				} else {
					checkedItems[i] = true;
					selectedPositions.add(i);
				}
			}
		} else {
			for (int i = 0; i < currentCategories.length; i++) {
				checkedItems[i] = true;
				selectedPositions.add(i);
			}
		}
		
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.dialog_select_categories)       		
        		.setMultiChoiceItems(currentCategories, checkedItems,
                      new DialogInterface.OnMultiChoiceClickListener() {
			               @Override
			               public void onClick(DialogInterface dialog, int which,
			                       boolean isChecked) {
			            	   
			                   if (isChecked) {
			                	   selectedPositions.add(which);
			                   } else if (selectedPositions.contains(which)) {		                	   
			                	   selectedPositions.remove(Integer.valueOf(which));
			                   }			                   
			               }
        		})		        
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
	                	ArrayList<String> filteredCategoriesList = new ArrayList<String>();
	               		for (int i = 0; i < currentCategories.length; i++) {
	               			if (!selectedPositions.contains(i)) {
	               				filteredCategoriesList.add(currentCategories[i]);
	               			}
	               		}
	               		String[] filteredArray = null;
	               		if (filteredCategoriesList.size() > 0) {
	               			filteredArray = filteredCategoriesList.toArray(new String[filteredCategoriesList.size()]);		
	               		} 
	               		
						if ((filteredArray == null && startingFilteredCategories != null)
								|| (filteredArray != null && startingFilteredCategories == null)
								|| (filteredArray != null && !Arrays.equals(filteredArray, startingFilteredCategories))) {
							CategoryDialogFragment.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
									GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Filter changed", null, getEllucianActivity().moduleName);
						}
	               		listener.updateFilteredCategories(filteredArray);
                   }
               })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
	}

}
