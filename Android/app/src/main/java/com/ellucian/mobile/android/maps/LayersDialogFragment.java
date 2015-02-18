package com.ellucian.mobile.android.maps;

import com.ellucian.elluciango.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class LayersDialogFragment extends DialogFragment implements
		DialogInterface.OnClickListener {

	private LayersDialogFragmentListener listener;
	private int selected = -1;

	public static LayersDialogFragment newInstance(int type) {
		LayersDialogFragment frag = new LayersDialogFragment();
		Bundle args = new Bundle();
		args.putInt("type", type);
		frag.setArguments(args);
		return frag;
	}

	public interface LayersDialogFragmentListener {
		public static final int MAP_TYPE_HYBRID = 3;
		public static final int MAP_TYPE_NORMAL = 0;
		public static final int MAP_TYPE_SATELLITE = 1;
		public static final int MAP_TYPE_TERRAIN = 2;
		
		public void setLayer(int layer);
	}

	public void setLayersDialogFragmentListener(
			LayersDialogFragmentListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int type = getArguments().getInt("type");

		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.layers))
				.setSingleChoiceItems(R.array.map_types, type,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								selected = which;

							}
						}).setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.cancel, null).create();
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		listener.setLayer(selected);
		
	}
}