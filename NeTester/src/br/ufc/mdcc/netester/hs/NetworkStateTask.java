package br.ufc.mdcc.netester.hs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.ufc.mdcc.mpos.util.device.DeviceController;
import br.ufc.mdcc.netester.R;

public final class NetworkStateTask extends TimerTask {

	private Activity activiy;
	private String interval;
	
	private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
	
	private final Map<String, Integer> networks = new HashMap<String, Integer>(5);
	
	private int total = 0;
	
	public NetworkStateTask(Activity activiy, String interval) {
		this.activiy = activiy;
		this.interval = interval;
	}

	@Override
	public void run() {
		
		//collect data
		String network = DeviceController.getInstance().getNetworkConnectedType();
		String dateStr = dateFormat.format(new Date(System.currentTimeMillis()));
		
		countNetwork(network);
		
		//status message
		StringBuilder statusMessage = new StringBuilder();
		statusMessage.append("Status: Serviço ligado, intervalo ").append("(").append(interval).append(")\n");
		statusMessage.append("Tempo: ").append(dateStr).append("\n\n");
		statusMessage.append("Rede: ").append("\n");
		for(String key:networks.keySet()){
			int value = networks.get(key);
			statusMessage.append("  -").append(key).append(": ").append(value).append(" (").append(String.format("%.3f", ((double) value/ (double) total) * 100.0)).append(" %)\n");
		}
		
		showOnDebugView(statusMessage);
	}
	
	public Map<String, Integer> getNetworks() {
		return networks;
	}
	
	public int getTotal() {
		return total;
	}
	
	private void countNetwork(String network){
		Integer count = networks.get(network);
		if(count != null){
			count++;
			networks.put(network, count);
			total++;
		}else{
			networks.put(network, 1);
			total++;
		}
	}
	
	private void showOnDebugView(final StringBuilder debug){
		final TextView debugView = (TextView) activiy.findViewById(R.id.debugView);
		LinearLayout linearLayout = (LinearLayout) activiy.findViewById(R.id.hunterLinearLayout);
		linearLayout.post(new Runnable() {
			@Override
			public void run() {
				debugView.setText(debug.toString());
			}
		});
	}
}
