// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;
import com.ellucian.mobile.android.registration.RegistrationActivity.TermInfoHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PinConfirmDialogFragment extends EllucianDialogFragment {
	
	private RegistrationActivity registrationActivity;
	public List<TermInfoHolder> termsThatNeedPins;
	public String action;
	
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
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

		View mainLayout = registrationActivity.getLayoutInflater().inflate(R.layout.registration_pin_input_dialog_layout, null);
		final TextView title = (TextView) mainLayout.findViewById(R.id.title);		
		final Button okButton = (Button) mainLayout.findViewById(R.id.ok_button);
		final Button cancelButton = (Button) mainLayout.findViewById(R.id.cancel_button);
		final LinearLayout rowsContainer = (LinearLayout) mainLayout.findViewById(R.id.pin_rows_layout);
		final List<TextView> labelViews = new ArrayList<TextView>();
		final List<EditText> inputViews = new ArrayList<EditText>();
		
		
		title.setText(R.string.registration_dialog_pin_message);
		
		for (TermInfoHolder holder : termsThatNeedPins) {
			View rowLayout = registrationActivity.getLayoutInflater().inflate(R.layout.label_edit_text_row, null);
			TextView label = (TextView) rowLayout.findViewById(R.id.label);		
			EditText input = (EditText) rowLayout.findViewById(R.id.input);
			input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // Use label_format so the colon appears on the left for RTL languages.
            String labelText = getString(R.string.label_format, holder.termName);
            label.setText(labelText);
			labelViews.add(label);
			inputViews.add(input);
			
			rowsContainer.addView(rowLayout);
		}
		
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HashMap<String, String> pinMap = new HashMap<String, String>();
				
				
				for (int i = 0; i < inputViews.size(); i ++) {
					EditText input = inputViews.get(i);
					String value = input.getText().toString().trim();
					
					if (TextUtils.isEmpty(value)) {
						Toast emptyFieldToast = Toast.makeText(registrationActivity, 
								R.string.dialog_field_empty, Toast.LENGTH_SHORT);
						emptyFieldToast.setGravity(Gravity.CENTER, 0, 0);
						emptyFieldToast.show();
						return;
					}
					
					pinMap.put(termsThatNeedPins.get(i).termId, value);					
				}
				
				registrationActivity.onPinConfirmOkClicked(pinMap, action);
				PinConfirmDialogFragment.this.dismiss();
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PinConfirmDialogFragment.this.dismiss();
			}
		});

		builder.setView(mainLayout);

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

