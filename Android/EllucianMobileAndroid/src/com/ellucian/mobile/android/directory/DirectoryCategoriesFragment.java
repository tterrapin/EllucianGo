package com.ellucian.mobile.android.directory;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesProperties;
import com.ellucian.mobile.android.util.Extra;

public class DirectoryCategoriesFragment extends EllucianListFragment {
	
	public static final String DIRECTORY_TYPE_ALL = "All";
	public static final String DIRECTORY_TYPE_STUDENT = "Student";
	public static final String DIRECTORY_TYPE_FACULTY = "Faculty";
	protected static final int DIRECTORY_ALL_POSITION = 0;
	protected static final int DIRECTORY_STUDENT_POSITION = 1;
	protected static final int DIRECTORY_FACULTY_POSITION = 2;
	public static final String DIRECTORY_STUDENT_VISIBLE = "directoryStudentVisible";
	public static final String DIRECTORY_FACULTY_VISIBLE = "directoryFacultyVisible";
	
	private String[] categories;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Activity activity = getActivity();
		String moduleId = ((EllucianActivity)activity).moduleId;
		
		boolean studentDirectoryVisible = false;
		boolean facultyDirectoryVisible = false;
		
		String selection =  Modules.MODULES_ID + " = ? AND (" 
				+ ModulesProperties.MODULE_PROPERTIES_NAME + " = ? OR " + ModulesProperties.MODULE_PROPERTIES_NAME + " = ?)";
		String[] selectionArgs = new String[] { moduleId, DIRECTORY_FACULTY_VISIBLE, DIRECTORY_STUDENT_VISIBLE };
		
		Cursor cursor = activity.getContentResolver().query(ModulesProperties.CONTENT_URI, 
																 null, 
																 selection, 
																 selectionArgs, 
																 ModulesProperties.MODULE_PROPERTIES_NAME + " ASC");
		
		if (cursor.moveToFirst()) {
			String facultyDirectoryString = cursor.getString(cursor.getColumnIndex(ModulesProperties.MODULE_PROPERTIES_VALUE));
			facultyDirectoryVisible = Boolean.parseBoolean(facultyDirectoryString);
			
			if (cursor.moveToNext()) {
				String studentDirectoryString= cursor.getString(cursor.getColumnIndex(ModulesProperties.MODULE_PROPERTIES_VALUE));
				studentDirectoryVisible = Boolean.parseBoolean(studentDirectoryString);
			}
		}
		cursor.close();
		
		// Test if there is certain directory type missing if so call goToDirectory on the one that isnt.
		if (!studentDirectoryVisible && !facultyDirectoryVisible) {
			goToDirectory(DIRECTORY_TYPE_ALL);
			activity.finish();
		} else if (studentDirectoryVisible && !facultyDirectoryVisible) {
			goToDirectory(DIRECTORY_TYPE_STUDENT);
			activity.finish();
		} else if (facultyDirectoryVisible && !studentDirectoryVisible) {
			goToDirectory(DIRECTORY_TYPE_FACULTY);
			activity.finish();
		}
		
		Resources r = getResources();
		
		categories = new String[] { r.getString(R.string.directory_type_all), 
									r.getString(R.string.directory_type_student), 
									r.getString(R.string.directory_type_faculty)};

		setListAdapter(new ArrayAdapter<String>(getActivity(),
												android.R.layout.simple_list_item_1,
												categories));
	}

	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		String directoryType = null;
		if (position == DIRECTORY_ALL_POSITION) {
			directoryType = DIRECTORY_TYPE_ALL;
		} else if (position == DIRECTORY_STUDENT_POSITION) {
			directoryType = DIRECTORY_TYPE_STUDENT;		
		} else if (position == DIRECTORY_FACULTY_POSITION) {
			directoryType = DIRECTORY_TYPE_FACULTY;	
		}
		
		goToDirectory(directoryType);
     
    }
	
	private void goToDirectory(String directoryType) {
		Intent intent = new Intent(getActivity(), DirectoryListActivity.class);
		intent.putExtras(getActivity().getIntent().getExtras());
		intent.putExtra(Extra.DIRECTORY_TYPE, directoryType);
		startActivity(intent);
	}


	@Override
	public void onStart() {
		super.onStart();
		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Select directory type", null, getEllucianActivity().moduleName);
	}
	
	

}
