package br.ufc.mdcc.mpos.net.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * TODO: Inicialmente simula na memoria (MOCK), mas na frente vai ser em cima do
 * sqlite...
 * 
 * @author Philipp
 */
public final class DiscoveryServiceDAOMemory {
	
	// mock dos serviços
	private String servicos[] = { "procImg", "procVideo", "chess", "procVoz" };
	private Random random = new Random();
	
	private final List<Service> listServices = new ArrayList<Service>(50);
	
	private final Semaphore mutex = new Semaphore(1);
	
	public DiscoveryServiceDAOMemory() {
		// MOCK
		new Thread(new SimulateDynamicServices()).start();
	}
	
	/**
	 * Lista todos os serviços que estão ativos no sistema, mas não estão
	 * cadastrados no DSS. OBS: Esse metodo não é safe, precisando ser protegido
	 * em caso de acesso concorrente.
	 * 
	 * @return retorna a lista de serviços
	 */
	public List<Service> listActiveServices() {
		
		List<Service> temp = new ArrayList<Service>();
		for (Service srv : listServices)
			if (srv.isOperacaoCloudlet() && !srv.isDss())
				temp.add(srv);
		
		return temp;
	}
	
	/**
	 * Lista todos os serviços que estão desativados no sistema e que estão
	 * cadastrados no DSS.
	 * 
	 * @return retorna a lista de serviços
	 */
	public List<Service> listDesactiveServices() {
		
		List<Service> temp = new ArrayList<Service>();
		for (Service srv : listServices)
			if (!srv.isOperacaoCloudlet() && srv.isDss())
				temp.add(srv);
		
		return temp;
	}
	
	/**
	 * Atualiza o campo relativo ao Dss, invertendo o valor passado e
	 * persistindo no banco de dados.
	 * 
	 * @param services
	 */
	public void updateDssStatus(List<Service> services) {
		
		for (Service srv : services) {
			int index = listServices.indexOf(srv);
			
			if (index > -1) {
				Service updateService = listServices.get(index);
				updateService.setDss(!updateService.isDss());// inverte a
																// situação!
			}
		}
	}
	
	// por causa do MOCK
	void acquireMutex() {
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// por causa do MOCK
	void releaseMutex() {
		mutex.release();
	}
	
	// por causa do MOCK
	private class SimulateDynamicServices implements Runnable {
		
		@Override
		public void run() {
			
			while (true) {
				Service srv = new Service(servicos[random.nextInt(4)],
						random.nextInt(60000));
				
				acquireMutex();
				
				srv.setOperacaoCloudlet(true);
				
				if (!listServices.isEmpty()) {
					
					// se gerar um serviço com mesma porta já cadastrada no
					// sistema muda a opCloudlet
					boolean uniquePort = true;
					int index = 0;
					for (Service temp : listServices) {
						if (temp.getPort() == srv.getPort()) {
							uniquePort = false;
							break;
						}
						index++;
					}
					
					if (!uniquePort) {
						System.out.println("Descartado, escolha outra porta para " + srv);
						if (srv.isOperacaoCloudlet()) {
							srv.setOperacaoCloudlet(false);
							listServices.set(index, srv);
						}
					} else
						listServices.add(srv);
					
				} else
					listServices.add(srv);
				
				// atualizar um serviço cadastrado no cloudlet
				if (random.nextBoolean()) {
					int count = random.nextInt(listServices.size());
					
					for (int i = 0; i < count; i++) {
						Service tmp = listServices.get(random.nextInt(listServices.size()));// pega
																							// dois
																							// objetos
																							// randomicamente...
						if (tmp.isDss() && tmp.isOperacaoCloudlet())
							tmp.setOperacaoCloudlet(false);
					}
				}
				
				// System.out.println("Cloudlet Services: "+listServices);
				
				releaseMutex();
				
				// pausa de até 7s entre um serviço e outro...
				try {
					Thread.sleep(random.nextInt(7000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
