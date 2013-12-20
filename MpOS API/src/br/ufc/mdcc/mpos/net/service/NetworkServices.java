package br.ufc.mdcc.mpos.net.service;

public enum NetworkServices {
	PERSIST_RESULTS(30000), PING_TCP(30001), PING_UDP(30002), 
	BANDWIDTH(30005), JITTER(30010,30011);

	private int port;
	private int portSec;

	private NetworkServices(int port) {
		this(port, 0);
	}
	
	private NetworkServices(int port, int portSec) {
		this.port = port;
		this.portSec = portSec;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getPortSec() {
		return portSec;
	}
}
