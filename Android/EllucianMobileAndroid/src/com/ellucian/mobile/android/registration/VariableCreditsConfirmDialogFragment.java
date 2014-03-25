// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;
import com.ellucian.mobile.android.client.registration.Section;

public class VariableCreditsConfirmDialogFragment extends EllucianDialogFragment {
	
	private RegistrationActivity registrationActivity;
	
	public static VariableCreditsConfirmDialogFragment newInstance(Section section, int position) {
		VariableCreditsConfirmDialogFragment fragment = new VariableCreditsConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putParcelable(RegistrationActivity.SECTION, section);
		args.putInt("position", position);
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Bundle args = getArguments();
		final Section section = args.getParcelable(RegistrationActivity.SECTION);
		final int position = args.getInt("position");
		
		// All float point math is x100 to a whole number to avoid float point math issues
		
		final int intIncrement; 
		if (section.variableCreditIncrement != 0) {
			intIncrement =  (int) (100 * section.variableCreditIncrement);
		} else {
			intIncrement = 1; // represents a float point minimum increase of .01
		}
		final int minCredits = (int) (100 * section.minimumCredits);
		final int maxCredits = (int) (100 * section.maximumCredits);
		
		// Resetting float to only 2 decimal, will truncate the rest
		final float floatIncrement = (float) intIncrement / 100; 
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

		View layout = registrationActivity.getLayoutInflater().inflate(R.layout.decimal_number_input_dialog_layout, null);
		final TextView title = (TextView) layout.findViewById(R.id.title);
		final EditText input = (EditText) layout.findViewById(R.id.input);
		final Button okButton = (Button) layout.findViewById(R.id.ok_button);
		final Button cancelButton = (Button) layout.findViewById(R.id.cancel_button);

		title.setText(getString(R.string.registration_dialog_variable_credits_message) 
				+ (float)section.minimumCredits + "-" + (float)section.maximumCredits);

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String value = input.getText().toString().trim();

				Toast rangeToast = Toast.makeText(registrationActivity, 
						R.string.registration_dialog_variable_credits_range_error, Toast.LENGTH_SHORT);
				rangeToast.setGravity(Gravity.CENTER, 0, 0);

				if (TextUtils.isEmpty(value)) {				
					rangeToast.show(); 
				} else {
					float floatValue = Float.parseFloat(value);
					int intValue = (int) (100 * floatValue);
					// Resetting float to only 2 decimal, will truncate the rest
					floatValue = (float) intValue / 100;

					if (intValue >= minCredits && intValue <= maxCredits) {

						if ((intValue - minCredits) % intIncrement == 0) {
							registrationActivity.onVariableCreditsConfirmOkClicked(section.termId, section.sectionId, floatValue);
							VariableCreditsConfirmDialogFragment.this.dismiss();
						} else {
							Toast incrementToast = Toast.makeText(registrationActivity, floatIncrement + " " 
									+ getString(R.string.registration_dialog_variable_credits_increment_error),
									Toast.LENGTH_SHORT);
							incrementToast.setGravity(Gravity.CENTER, 0, 0);
							incrementToast.show();
						}

					} else {
						rangeToast.show();
					}
				}
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				registrationActivity.onVariableCreditsConfirmCancelClicked(position);
				VariableCreditsConfirmDialogFragment.this.dismiss();
			}
		});

		builder.setView(layout);

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);

		return dialog;
	}

	@Override
	public void onDestroyView() {
		// Trick to keep dialog open on rotate
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}

}

