package br.ufc.mdcc.mpos.net.model;

/**
 * Diz o nome, ip e a porta do cloudlet. 
 * 
 * Essa porta utiliza o protocolo TCP, que server para meio para o cliente perguntar sobre 
 * um determinado serviço de offloading em execução no Cloudlet. 
 * 
 * @author hack
 *
 */
public final class Cloudlet {

	private String ip,name;
	private int port;
	
	public Cloudlet(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public Cloudlet(String ip, int port, String name) {
		this(ip, port);
		this.name = name;
	}

	public final String getIp() {
		return ip;
	}

	public final void setIp(String ip) {
		this.ip = ip;
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

	public final void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("nome: ").append(name).append("\n");
		sb.append("ip: ").append(ip).append(":").append(port).append("\n");
		
		return sb.toString();
	}
}
