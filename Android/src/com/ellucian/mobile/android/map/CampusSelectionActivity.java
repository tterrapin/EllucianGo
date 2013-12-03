package com.ellucian.mobile.android.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ellucian.mobile.android.R;

public class CampusSelectionActivity extends Activity {
	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final CharSequence[] items = getIntent().getCharSequenceArrayExtra(
				"campuses");

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getResources().getString(R.string.chooseCampus))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Intent intent = new Intent();
						intent.putExtra("position", item);
						setResult(RESULT_OK, intent);
						finish();

					}
				});
		builder.setOnCancelListener(new OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				finish();

			}
		});
		builder.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						|| keyCode == KeyEvent.KEYCODE_SEARCH) {
					dialog.dismiss();
					finish();
					return true;
				}
				return false;
			}
		});
		dialog = builder.create();
		dialog.show();
	}


	
	
}
