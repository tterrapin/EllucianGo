// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;
import com.ellucian.mobile.android.client.registration.Section;

public class VariableCreditsConfirmDialogFragment extends EllucianDialogFragment {

	private RegistrationActivity registrationActivity;
	private List<String> creditsList;
	
	public static VariableCreditsConfirmDialogFragment newInstance(Section section) {
		VariableCreditsConfirmDialogFragment fragment = new VariableCreditsConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putParcelable(RegistrationActivity.SECTION, section);
		fragment.setArguments(args);
		
		return fragment;
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			registrationActivity = (RegistrationActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Attached Activity must of type: RegistrationActivity");
        }
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Bundle args = getArguments();
		final Section section = args.getParcelable(RegistrationActivity.SECTION);
		creditsList = new ArrayList<String>();

		float increase = 0;
		if (section.variableCreditIncrement == 0) {
			increase = (float) 1.0;
		} else {
			increase = section.variableCreditIncrement;
		}
		for (float i = (float)section.minimumCredits; i <= section.maximumCredits; i = (float)(i + increase)) {
			creditsList.add("" + i);
		}
		
		final String[] creditsArray = creditsList.toArray(new String[creditsList.size()]);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		
		builder.setTitle(R.string.registration_dialog_variable_credits_message)
			.setItems(creditsArray, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String credits = creditsList.get(which);
					registrationActivity.onVariableCreditsConfirmOkClicked(section.termId, section.sectionId, credits);
					
				}
			});

		return builder.create();
	}

}
