package br.ufc.mdcc.mpos.net.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.JmDNSImpl;

import br.ufc.mdcc.mpos.net.AbstractServer;

/**
 * TODO: Falta apenas encapsular a parte do Android que ler o serviço no celular
 * e ele conseguir automaticamente obter o ip e porta dos serviços fixos!
 * 
 * @author Philipp
 */
public final class DiscoveryCloudlet extends AbstractServer {
	
	private JmDNSImpl jmdnsService;
	
	private Service services[];
	
	public DiscoveryCloudlet(Service... essencialServices) {
		super("srvDiscoveryCloudlet", DiscoveryCloudlet.class);
		
		this.services = essencialServices;
		startName = "Descoberta de Serviço Iniciado";
	}
	
	@Override
	public void run() {
		
		try {
			jmdnsService = (JmDNSImpl) JmDNS.create();
			
			logger.info(jmdnsService);
		} catch (IOException e) {
			logger.info("Error durante a criação do JmDNS", e);
		}
		
		logger.info(startName);
		
		// registra os serviços fixos do sistema
		for (Service srv : services)
			registerService(srv);
		
		// watcherServices();
	}
	
	/**
	 * Esse metodo trata da logica de registrar e desregistrar os serviços em
	 * execução no MpOS, no entanto nesse caso foi usado um gerador de serviços
	 * dinamico na memoria.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void watcherServices() {
		
		DiscoveryServiceDAOMemory serviceDao = new DiscoveryServiceDAOMemory();
		
		System.out.println("Serviços registrados: " + jmdnsService.getServices());
		
		while (true) {
			
			try {
				// 10s espaço de tempo para verificar os serviços
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.info("A thread na espera foi interrupda de alguma forma", e);
			}
			
			serviceDao.acquireMutex();
			List<Service> tempActive = serviceDao.listActiveServices();
			List<Service> tempDesactive = serviceDao.listDesactiveServices();
			serviceDao.releaseMutex();
			
			System.out.println("Serviços para ativar: " + tempActive.size());
			System.out.println("Serviços para desativar: " + tempDesactive.size());
			
			System.out.println("Serviços cadastrados: " + ((JmDNSImpl) jmdnsService).getServices().values().size());
			
			// cadastra os serviços recém ativos no cloudlet
			for (Service act : tempActive)
				registerService(act);
			
			// retirar do jmdns os serviços que foram desativados no cloudlet
			for (Service desact : tempDesactive) {
				
				// transforma os objetos
				ServiceInfo si = createServiceInfo(desact);
				
				// transforma os valores do mapa em lista
				Collection<ServiceInfo> lstRegistered = ((JmDNSImpl) jmdnsService).getServices().values();
				
				for (ServiceInfo srvIn : lstRegistered) {
					// apenas uma porta por serviço...
					if (srvIn.getPort() == si.getPort()) {
						unregisterService(srvIn);
						break;
					}
				}
			}
			
			// atualiza apenas o status do dss no banco de dados
			serviceDao.acquireMutex();
			serviceDao.updateDssStatus(tempActive);
			serviceDao.updateDssStatus(tempDesactive);
			serviceDao.releaseMutex();
			
			// System.out.println("Serviços registrados: "+jmdnsService.getServices());
			
		}
	}
	
	/**
	 * Quando um serviço é desligado no cloudlet ele deve ser desregistrado no
	 * DSS
	 * 
	 * @param service
	 */
	private void unregisterService(ServiceInfo si) {
		// System.out.println(si);
		jmdnsService.unregisterService(si);
	}
	
	/**
	 * Quando um serviço sobe no cloudlet ele deve ser registrado no DSS
	 * 
	 * @param service
	 */
	private void registerService(Service service) {
		
		ServiceInfo si = createServiceInfo(service);
		
		try {
			jmdnsService.registerService(si);
		} catch (IOException e) {
			logger.info("Error ao cadastrar um serviço no JmDNS", e);
		}
	}
	
	private ServiceInfo createServiceInfo(Service srv) {
		return ServiceInfo.create(srv.getName(), "Cloudlet: " + srv.getName(),
				srv.getPort(), srv.getName());
	}
}
