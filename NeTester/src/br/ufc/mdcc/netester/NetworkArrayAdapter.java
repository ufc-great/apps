package br.ufc.mdcc.netester;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import br.ufc.mdcc.mpos.net.model.Network;

public final class NetworkArrayAdapter extends ArrayAdapter<Network> {

	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM\nHH:mm", Locale.US);
	
	public NetworkArrayAdapter(Context context, List<Network> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		
		ItemView iv = null;
		
		if(convertView == null){
			
			convertView = inflater.inflate(R.layout.item_history, parent, false);
			iv = new ItemView();
			iv.type = (TextView) convertView.findViewById(R.id.tv_hist_type);
			iv.date = (TextView) convertView.findViewById(R.id.tv_hist_date);
			iv.down = (TextView) convertView.findViewById(R.id.tv_hist_down);
			iv.up = (TextView) convertView.findViewById(R.id.tv_hist_up);
			iv.ping = (TextView) convertView.findViewById(R.id.tv_hist_ping);
			
			convertView.setTag(iv);
			
		}else{
			iv = (ItemView) convertView.getTag();
		}
		
		Network network = getItem(position);
		network.generatingPingTCPStats();
		
		iv.type.setText(network.getType());
		
		iv.ping.setText(String.valueOf(network.getPingMedTCP()));
		iv.date.setText(dateFormat.format(network.getDate())); 
		iv.down.setText(bandFormat(Double.parseDouble(network.getBandwidthDownload())));
		iv.up.setText(bandFormat(Double.parseDouble(network.getBandwidthUpload())));
		
		return convertView;
	}
	
	private String bandFormat(double band){
		return String.format(Locale.US,"%2.2f", band);
	}
	
	private class ItemView{
		TextView ping, down, up, date, type;
	}
	
}
