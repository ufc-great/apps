package br.ufc.mdcc.mpos;

import org.apache.log4j.PropertyConfigurator;

import br.ufc.mdcc.mpos.net.AbstractServer;
import br.ufc.mdcc.mpos.net.discovery.DiscoveryCloudlet;
import br.ufc.mdcc.mpos.net.discovery.Service;
import br.ufc.mdcc.mpos.net.profile.BandwidthTcpServer;
import br.ufc.mdcc.mpos.net.profile.JitterRetrieveTcpServer;
import br.ufc.mdcc.mpos.net.profile.JitterUdpServer;
import br.ufc.mdcc.mpos.net.profile.PersistenceTcpServer;
import br.ufc.mdcc.mpos.net.profile.PingTcpServer;
import br.ufc.mdcc.mpos.net.profile.PingUdpServer;

/**
 * Classe de inicio dos serviços OBS: É bom ao iniciar o jar forçar o uso de
 * ipv4. java -Djava.net.preferIPv4Stack=true -jar MyJar.jar
 * 
 * @author Philipp
 */
public final class StartService {
	
	static {
		PropertyConfigurator.configure(StartService.class.getResourceAsStream("/br/ufc/mdcc/mpos/util/log4j.properties"));
		System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	/**
	 * Vai receber via argumentos se tb dispara a descoberta de serviço local...
	 * 
	 * @param args
	 */
	public static void main(String ...args) {
		
		AbstractServer startServices[] = { new PingTcpServer(), new PingUdpServer(), new PersistenceTcpServer(),
				new JitterRetrieveTcpServer(), new JitterUdpServer(), new BandwidthTcpServer() };
		
		for (AbstractServer as : startServices) {
			new Thread(as, as.getService().toString()).start();
		}
		
		// inicializa a descoberta do cloudlet atraves do JmDNS em paralelo...
		if (args.length == 1 && args[0].equals("disc")) {
			DiscoveryCloudlet dss = new DiscoveryCloudlet(new Service(40000));
			new Thread(dss, dss.getService().toString()).start();
		}
		
		System.out.println("Inicialização dos Serviços Terminada!");		
	}
}