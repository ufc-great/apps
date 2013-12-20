package br.ufc.mdcc.mpos.net.discovery;

import java.net.PortUnreachableException;

/**
 * @author Philipp
 */
public final class Service implements Comparable<Service> {
	
	private int id;
	private String name;
	private int port;// só pode existir uma porta por serviço
	
	// o serviço está em operação no cloudlet
	private boolean operacaoCloudlet;
	
	// o serviço está cadastrado no dss (DiscoveryServiceServer)
	private boolean dss;
	
	public Service(int port) {
		this("srvCloudlet", port);
	}
	
	public Service(String name, int port) {
		this.name = name;
		this.port = port;
		
		operacaoCloudlet = false;
		dss = false;
	}
	
	public final String getName() {
		return name;
	}
	
	public final void setName(String name) {
		this.name = name;
	}
	
	public final int getPort() {
		return port;
	}
	
	public final void setPort(int port) throws PortUnreachableException {
		if (1024 < port && port < 65536)
			this.port = port;
		else
			throw new PortUnreachableException("Porta fora dos limites de utilização");
	}
	
	public final boolean isOperacaoCloudlet() {
		return operacaoCloudlet;
	}
	
	public final void setOperacaoCloudlet(boolean ativo) {
		this.operacaoCloudlet = ativo;
	}
	
	public final boolean isDss() {
		return dss;
	}
	
	public final void setDss(boolean dss) {
		this.dss = dss;
	}
	
	public final int getId() {
		return id;
	}
	
	public final void setId(int id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Service))
			return false;
		
		Service srv = (Service) obj;
		
		return srv.getPort() == port && srv.getName().equals(name);
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(name).append(":").append(port).
				append("|").
				append("dss:").append(dss).
				append("|").
				append("opCloudlet:").append(operacaoCloudlet);
		
		return sb.toString();
	}
	
	@Override
	public int compareTo(Service o) {
		
		int strCmp = o.name.compareTo(name);
		
		if (strCmp == 0) {
			if (o.port == port)
				return 0;
			else if (o.port > port)
				return -1;
			else
				return 1;
			
		} else
			return strCmp;
	}
	
}
