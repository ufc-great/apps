package br.ufc.mdcc.mpos.net.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;
import br.ufc.mdcc.mpos.net.model.Cloudlet;
import br.ufc.mdcc.mpos.net.profile.ProfileController;

public final class MulticastDnsService implements Runnable{

	private final String LOG = MulticastDnsService.class.getName();
	
	private WifiManager wifiMan = null;
	private MulticastLock mLock = null;

	private JmDNS jmdnsService = null;

	private String cloudletService;

	public MulticastDnsService(Activity act, String cloudletService) {
		this.cloudletService = cloudletService;

		multicastWifiContext(act);
		
		Log.i(LOG,"Started Cloudlet Discovery");
	}

	private void multicastWifiContext(Context act) {
		// pega o contexto do wifi!
		wifiMan = (WifiManager) act.getSystemService(Context.WIFI_SERVICE);

		if (wifiMan != null) {
			mLock = wifiMan.createMulticastLock("descServLock");
			mLock.setReferenceCounted(true);
			mLock.acquire();
		} else {
			throw new NullPointerException("Não conseguiu pegar o Wifi Manager");
		}
	}

	@Override
	public void run() {

		if (cloudletService != null) {

			ArrayList<Cloudlet> endpointServices = new ArrayList<Cloudlet>(3);
			
			instanceJmDNS();

			ServiceInfo[] discoveryServices = jmdnsService.list(ServiceInfo.create(cloudletService, "", 0, "").getType());
			
			//pode haver mais de um cloudlet numa mesma rede!
			for (ServiceInfo si : discoveryServices) {
				
				int porta = si.getPort();
				String endereco = si.getHostAddresses()[0];
				
				endpointServices.add(new Cloudlet(endereco, porta, cloudletService));
			}

			Log.i(LOG, "Quantidade de cloudlets: " + endpointServices.size());

			if(!endpointServices.isEmpty()){
				if(endpointServices.size()==1){
					ProfileController.getInstance().setCloudlet(endpointServices.get(0)); 
				}else{
					//escolhe um dos cloudlets disponivel na rede
					ProfileController.getInstance().setCloudlet(endpointServices.get(new Random().nextInt(endpointServices.size())));
				}
			}
		}

		releaseWifiLock();

		Log.i(LOG,"Finished Cloudlet Discovery");
	}
	
	private void instanceJmDNS() {
		try {
			jmdnsService = JmDNS.create();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void releaseWifiLock() {
		if (mLock != null && mLock.isHeld()) {
			mLock.release();
			mLock = null;
		}
	}
}
