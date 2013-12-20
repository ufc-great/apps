package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;

import android.util.Log;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.util.TaskResult;

public final class ProfileNetworkLight extends ProfileNetworkTask{
	
	protected ProfileNetworkLight(TaskResult<Network> result, String ip) throws MissedEventException {
		super(ip,result, ProfileNetworkLight.class);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(LOG, "ProfileLight Started");
	}
	
	@Override
	protected Network doInBackground(Void... params) {
		
		Network results = new Network();
		
		try {
			Network temp = pingService(Protocol.TCP);			
			results.setResultPingTcp(temp.getResultPingTcp());
			
			publishProgress(100);
			
			Log.d(LOG, "ProfileLight Finished");
			
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
