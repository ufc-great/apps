package br.ufc.mdcc.mpos.net.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.mdcc.mpos.util.device.Device;

/**
 * Usado para montar os dados para serem enviados remotamente.
 * 
 * @author hack
 *
 */
public final class NetworkResult {

	//device details
	private Device device;
	private Network network;

	public NetworkResult(Device device, Network network) {
		this.device = device;
		this.network = network;
	}
	
	public Device getDevice() {
		return device;
	}
	
	public Network getNetwork() {
		return network;
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject object = new JSONObject();
		
		object.put("mobileId", device.getMobileId());
		object.put("deviceName", device.getDeviceName());
		object.put("carrier", device.getCarrier());
		object.put("lat", device.getLatitude());
		object.put("lon", device.getLongitude());
		
		object.put("date", network.getDate().getTime());
		object.put("tcp", new JSONArray(Network.arrayToString(network.getResultPingTcp())));
		object.put("udp", new JSONArray(Network.arrayToString(network.getResultPingUdp())));
		object.put("loss", network.getLossPacket());
		object.put("jitter", network.getJitter());
		object.put("down", network.getBandwidthDownload());
		object.put("up", network.getBandwidthUpload());
		object.put("net", network.getType());
		object.put("appName", network.getAppName());
		
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
