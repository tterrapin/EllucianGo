package com.ellucian.mobile.android.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.elluciango.R;

public class CategoryDialogFragment extends EllucianDialogFragment {
	CategoryDialogListener listener;
	private String[] startingFilteredCategories;

	public interface CategoryDialogListener {
		public static final String FILTERED_CATEGORIES = "filtered_categories";
		public static final String CATEGORY_DIALOG = "category_dialog";
		public String[] getAllCategories();
		public String[] getFilteredCategories();
		public void updateFilteredCategories(String[] filteredCategories);
	}
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			listener = (CategoryDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement CategoryDialogListenter");
		}
	}
		
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d("CategoryDialogFragment.onCreateDialog", "Creating dialog");
		final String[] currentCategories = listener.getAllCategories();
		final String[] currentFiltered = listener.getFilteredCategories();
		
		final ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
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
               });

        // Create the AlertDialog object and return it
        return builder.create();
	}

}
