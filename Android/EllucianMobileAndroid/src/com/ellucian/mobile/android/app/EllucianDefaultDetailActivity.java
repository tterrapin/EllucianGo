package com.ellucian.mobile.android.app;

import android.content.res.Configuration;
import android.os.Bundle;

import com.ellucian.elluciango.R;

/**
 * This Activity is the container of the detail fragment if show in single pane (portrait) mode.
 * 
 * @author Jared Higley
 *
 */
public class EllucianDefaultDetailActivity extends EllucianActivity {
	private static final String TAG = EllucianDefaultDetailActivity.class.getSimpleName();
	
	
	/** A subclass can override this method to change the support for different screen sizes and modes */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_detail);
		
		
		// Only for large screens in landscape
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && 
				(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= 
					        Configuration.SCREENLAYOUT_SIZE_LARGE) {
	
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}
		
		
		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			EllucianDefaultDetailFragment details = getDetailFragment(getIntent().getExtras(), -1);

			getFragmentManager().beginTransaction().add(R.id.detail_container, details).commit();
		}
	}
	
	/** Override to return a subclass of EllucianDefaultDetailFragment created by EllucianDefaultDetailFragment.newInstance
     *  See EllucianDefaultDetailFragment.newInstance for more information
     *  If you are only making changes to the class name, override getDetailFragmentClass() instead
     */
	public EllucianDefaultDetailFragment getDetailFragment(Bundle args, int index) {
		
		return EllucianDefaultDetailFragment.newInstance(this, 
				getDetailFragmentClass().getName(), args, index);
				
	}
	
	/** Override to return the class of an EllucianDefaultDetailFragment subclass */
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return EllucianDefaultDetailFragment.class;	
	}
	

}
