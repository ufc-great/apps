package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;

import android.util.Log;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.util.TaskResult;

public class ProfileNetworkDefault extends ProfileNetworkTask{
	
	protected ProfileNetworkDefault(TaskResult<Network> result, String ip) throws MissedEventException {
		super(ip, result, ProfileNetworkDefault.class);
		
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(LOG, "ProfileDefault Started");
	}

	@Override
	protected Network doInBackground(Void... params) {
		
		Network results = new Network();
		
		try {
			Network temp = pingService(Protocol.TCP);			
			results.setResultPingTcp(temp.getResultPingTcp());
			publishProgress(33);
			
			temp = pingService(Protocol.UDP);
			long resultUdp[] = temp.getResultPingUdp();
			results.setResultPingUdp(resultUdp);
			publishProgress(66);
			
			//conta os pacotes perdidos UDP
			int count = 0;
			for(long udpPing:resultUdp){
				if(udpPing == 0){
					count++;
				}
			}
			
			results.setLossPacket(count);
			publishProgress(100);
			
			Log.d(LOG, "ProfileDefault Finished");
			
			return results;
		} catch (InterruptedException e) {
			Log.e(LOG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(LOG, e.getMessage(), e);
		} catch (MissedEventException e) {
			Log.e(LOG, e.getMessage(), e);
		}
		
		publishProgress(100);
		return null;
	}
}
