package br.ufc.mdcc.mpos.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Philipp
 */
public final class Device {
	
	private String mobileId;
	private String deviceName;
	private String carrier;
	private String longitude;
	private String latitude;
	
	public final String getMobileId() {
		return mobileId;
	}
	
	public final void setMobileId(String mobileId) {
		this.mobileId = mobileId;
	}
	
	public final String getDeviceName() {
		return deviceName;
	}
	
	public final void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}
	
	public String getCarrier() {
		return carrier;
	}
	
	public final String getLongitude() {
		return longitude;
	}
	
	public final void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public final String getLatitude() {
		return latitude;
	}
	
	public final void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject object = new JSONObject();
		
		object.put("mobileId", mobileId);
		object.put("deviceName", deviceName);
		object.put("carrier", carrier);
		object.put("lat", latitude);
		object.put("lon", longitude);
		
		return object;
	}
	
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return null;
		}
	}
}
