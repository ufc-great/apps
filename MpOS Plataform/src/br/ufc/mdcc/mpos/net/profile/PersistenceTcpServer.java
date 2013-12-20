package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.mdcc.mpos.model.Device;
import br.ufc.mdcc.mpos.model.Network;
import br.ufc.mdcc.mpos.model.NetworkResult;
import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.persistence.FachadeDao;

/**
 * @author Philipp
 */
public final class PersistenceTcpServer extends TcpServer {
	
	/**
	 * Porta padrão do serviço é 30000
	 */
	public PersistenceTcpServer() {
		this(30000);
	}
	
	public PersistenceTcpServer(int port) {
		super(port, "srvPersistenceTcp", PersistenceTcpServer.class);
		startName = "Servidor Persistencia TCP Iniciado";
	}
	
	@Override
	public void clientRequest(Socket connection) throws IOException {
		
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();
		
		byte tempBuffer[] = new byte[1024 * 4];
		
		while (input.read(tempBuffer) != -1) {
			
			String data = new String(tempBuffer);
			
			if (data != null && data.contains("date")) {
				
				persistence(data, connection.getInetAddress().toString());
				
				String resp = "ok";
				output.write(resp.getBytes(), 0, resp.length());
				
				// evitar "connection reset"
				output.flush();
			}
			
			// limpa o buffer
			Arrays.fill(tempBuffer, (byte) 0);
		}
		
		close(input);
		close(output);
	}
	
	private void persistence(String data, String ip) {
		
		Device device = null;
		Network network = null;
		
		try {
			JSONObject resp = new JSONObject(data);
			device = new Device();
			device.setMobileId((String) resp.get("mobileId"));
			device.setDeviceName((String) resp.get("deviceName"));
			device.setCarrier((String) resp.getString("carrier"));
			device.setLatitude((String) resp.getString("lat"));
			device.setLongitude((String) resp.getString("lon"));
			
			network = getJSONToNetwork(data);
			
		} catch (JSONException e) {
			
			// caso não passe informações sobre o device
			try {
				
				network = getJSONToNetwork(data);
				
			} catch (JSONException ex) {
				logger.info("Algum erro de parser do JSON", ex);
			}
		} finally {
			if (network != null) {
				asyncPersistence(ip, device, network);
			}
		}
	}
	
	private Network getJSONToNetwork(String data) throws JSONException {
		JSONObject resp = new JSONObject(data);
		Network network = new Network();
		network.setDate(new Date((Long) resp.get("date")));
		network.setResultPingTcp(Network.StringToLongArray(((JSONArray) resp.get("tcp")).toString()));
		network.setResultPingUdp(Network.StringToLongArray(((JSONArray) resp.get("udp")).toString()));
		network.setLossPacket((Integer) resp.get("loss"));
		network.setJitter((Integer) resp.get("jitter"));
		network.setBandwidthDownload((String) resp.get("down"));
		network.setBandwidthUpload((String) resp.get("up"));
		network.setType((String) resp.getString("net"));
		network.setAppName((String) resp.get("appName"));
		
		return network;
	}
	
	private void asyncPersistence(String ip, Device device, Network network) {
		final NetworkResult networkResult = new NetworkResult(device, network);
		networkResult.setIp(ip);
		
		// o esquema para persistir os dados entra num pool de threads
		// protegidas por um semaforo mutex...
		new Thread() {
			public void run() {
				FachadeDao.getInstance().getNetProfileDao().adicionar(networkResult);
			}
		}.start();
		
		// System.out.println(networkResult);
	}
}
