package br.ufc.mdcc.mpos.util.device;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import br.ufc.mdcc.mpos.MposActivity;
import br.ufc.mdcc.mpos.persistence.MobileDAO;
import br.ufc.mdcc.mpos.util.LocationListenerAdapter;
import br.ufc.mdcc.mpos.util.LocationTracker;


public final class DeviceController {

	private Context context;
	private Device device;
    private LocationTracker tracker;
    
    private String appName;
	
	private DeviceController(){
	}
	
	private static class ServiceSingleton {
		public static final DeviceController INSTANCE = new DeviceController();
	}
	
	public static DeviceController getInstance() {
		return ServiceSingleton.INSTANCE;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public String getAppName() {
		return appName;
	}
	
	public String getNetworkConnectedType(){
		NetworkInfo info = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		
		if(info != null){
			int type = info.getType();
			int subtype = info.getSubtype();
			
			if(type == ConnectivityManager.TYPE_WIFI){
				return "WiFi";
			}else if(type == ConnectivityManager.TYPE_MOBILE){
				switch (subtype) {
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return "CDMA";
				case TelephonyManager.NETWORK_TYPE_IDEN:
					return "iDen";
				case TelephonyManager.NETWORK_TYPE_EHRPD:
					return "eHRPD";
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return "EDGE";
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					return "1xRTT";
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return "GPRS";
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return "UMTS";
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
					return "EVDO";
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return "HSPA";
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return "HSDPA";
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					return "HSPA+";
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					return "HSUPA";
				case TelephonyManager.NETWORK_TYPE_LTE:
					return "LTE";
				default:
					return "Unknown";		
				}
			}
		}
		return "Offline";
	}
	
	public Device getDevice() {
		return device;
	}
	
	public void collectDeviceConfig(){
		device = new Device();
		device.setMobileId(new MobileDAO(context).checkMobileId());
		device.setCarrier(((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName());
		device.setDeviceName(Build.MANUFACTURER +" "+ Build.MODEL);
	}
	
	public void collectLocation(){
		tracker = new LocationTracker(context);
		tracker.setLocationListener(new LocationListenerAdapter(){
			public void onLocationChanged(Location location) {
				//200m
				if(location.getAccuracy() < 250.0f){
					device.setLatitude(Double.toString(location.getLatitude()));
					device.setLongitude(Double.toString(location.getLongitude()));
					
					//collect completed
					tracker.removeLocationListener();
					
					Log.d(MposActivity.class.getName(), "Location collect completed");
					Log.d(MposActivity.class.getName(), "lat: "+location.getLatitude());
					Log.d(MposActivity.class.getName(), "lon: "+location.getLongitude());
				}
			}
		});
	}
	
	public void removeLocationListener(){
		if(tracker != null){
			tracker.removeLocationListener();
		}
	}
}
