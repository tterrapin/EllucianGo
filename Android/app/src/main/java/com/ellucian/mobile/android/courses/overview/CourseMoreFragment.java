package com.ellucian.mobile.android.courses.overview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.courses.announcements.CourseAnnouncementsActivity;
import com.ellucian.mobile.android.courses.assignments.CourseAssignmentsActivity;
import com.ellucian.mobile.android.courses.events.CourseEventsActivity;

public class CourseMoreFragment extends EllucianListFragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        String[] courseMoreOptions = new String[] { 
        		getString(R.string.course_assignments), 
				getString(R.string.course_announcements), 
				getString(R.string.course_events)};

        setListAdapter(new ArrayAdapter<String>(getActivity(),
							android.R.layout.simple_list_item_1,
							courseMoreOptions));
		
        return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.putExtras(getActivity().getIntent().getExtras());
		switch (position) {
		case 0: intent.setClass(getActivity(), CourseAssignmentsActivity.class); break;
		case 1: intent.setClass(getActivity(), CourseAnnouncementsActivity.class); break;
		case 2: intent.setClass(getActivity(), CourseEventsActivity.class); break;
		}
		startActivity(intent);
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Show \"More\" list", getEllucianActivity().moduleName);
	}

	
}
