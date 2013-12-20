package br.ufc.mdcc.mpos;

import java.lang.annotation.Annotation;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Process;
import br.ufc.mdcc.mpos.config.Application;
import br.ufc.mdcc.mpos.net.profile.ProfileController;
import br.ufc.mdcc.mpos.util.device.DeviceController;

/**
 * Essa classe inicializa os serviços do framework MpOS.
 * 
 * @author hack
 * 
 */
public abstract class MposActivity extends Activity {
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		configureControllers();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		DeviceController.getInstance().removeLocationListener();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Process.killProcess(Process.myPid());
	}
	
	protected boolean isOnline() {	
	
		boolean connectedWifi = false, connectedMobile = false;
		
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
	    NetworkInfo netInfo[] = connectivityManager.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if ("wifi".equalsIgnoreCase(ni.getTypeName()) && ni.isConnected())
	        	connectedWifi = true;
	        if ("mobile".equalsIgnoreCase(ni.getTypeName()) && ni.isConnected())
	            connectedMobile = true;
	    }
	    return connectedWifi || connectedMobile;
	}
	
	private void configureControllers(){
		ProfileController.getInstance().setActivity(this);
		
		DeviceController.getInstance().setContext(getApplicationContext());
		DeviceController.getInstance().setAppName(getString(getApplicationInfo().labelRes));
		
		Class<?> entryClass = this.getClass();
		if(entryClass != null){
			Annotation[] annotations = entryClass.getAnnotations();
			for(Annotation anno:annotations){
				if(anno instanceof Application){
					Application app = entryClass.getAnnotation(Application.class);
					ProfileController.getInstance().setProfile(app.profile());
					
					if(app.deviceDetails()){
						DeviceController.getInstance().collectDeviceConfig();
					}
					
					if(app.locationProfile()){
						DeviceController.getInstance().collectLocation();
					}
					
					//check se tem conexão e precisa da discoberta de serviço
					if(app.discoveryService() && isOnline()){
						ProfileController.getInstance().discoveryCloudlet();
					}
				}
			}
		}
	}
}
