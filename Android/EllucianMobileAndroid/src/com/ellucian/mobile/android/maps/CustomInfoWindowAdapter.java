package com.ellucian.mobile.android.maps;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements InfoWindowAdapter {
	LayoutInflater inflater=null;

	CustomInfoWindowAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return(null);
	}

	@Override
	public View getInfoContents(Marker marker) {
		View infoWindow = inflater.inflate(R.layout.info_window, null);

		TextView tv = (TextView)infoWindow.findViewById(R.id.title);
		tv.setText(marker.getTitle());
		
		tv = (TextView)infoWindow.findViewById(R.id.snippet);
		tv.setText(marker.getSnippet());

		return(infoWindow);
	}
}
