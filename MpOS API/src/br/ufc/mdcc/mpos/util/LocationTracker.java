package br.ufc.mdcc.mpos.util;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public final class LocationTracker {

	private LocationManager locationManager;
	private LocationListener locationListener;
	
	public LocationTracker(Context context) {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void setLocationListener(LocationListener locationListener){
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);
		this.locationListener = locationListener;
	}
	
	public void removeLocationListener(){
		locationManager.removeUpdates(locationListener);
	}
}
