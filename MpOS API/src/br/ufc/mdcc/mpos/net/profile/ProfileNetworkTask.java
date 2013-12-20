package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import android.os.AsyncTask;
import android.util.Log;
import br.ufc.mdcc.mpos.net.ClientAbstract;
import br.ufc.mdcc.mpos.net.FactoryCliente;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.ReceiveDataEvent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.net.model.NetworkResult;
import br.ufc.mdcc.mpos.net.service.NetworkServices;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.device.Device;
import br.ufc.mdcc.mpos.util.device.DeviceController;

public abstract class ProfileNetworkTask extends AsyncTask<Void, Integer, Network> {

	private TaskResult<Network> taskResult;
	protected String LOG;
	
	protected String ip;
	private int portTcp, portUdp;
	private int portPersistenceRemote;

	private long tempoInicio;
	private final int NUM_PING = 7; // quantidade de vezes que irá fazer o ping
	private final long coletaPing[] = new long[NUM_PING];
	
	/**
	 * 
	 * @param sendDataToView
	 *            - Os dados do resultado da tarefa é enviado para activity
	 *            atraves de uma interface
	 * @param ip
	 *            - IP do serviço que pode ser local ou remoto
	 * @throws MissedEventException
	 */
	protected ProfileNetworkTask(String ip, TaskResult<Network> result, Class<?> cls) throws MissedEventException {
		if (result == null) {
			throw new MissedEventException("Need to set a TaskResult<Network> for get results from end task");
		}
		
		this.LOG = cls.getName();
		this.taskResult = result;
		this.ip = ip;
		
		portTcp = NetworkServices.PING_TCP.getPort();
		portUdp = NetworkServices.PING_UDP.getPort();
		portPersistenceRemote = NetworkServices.PERSIST_RESULTS.getPort();
	}

	protected void onPreExecute() {
		taskResult.taskOnGoing(0);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		taskResult.taskOnGoing(values[0]);
	}
	
	@Override
	protected void onPostExecute(Network result) {
		//depois da execução vai ser primeiro enviado pela rede, para depois ser persistido localmente...
		if(result != null){
			Device device = DeviceController.getInstance().getDevice();
			
			if(device != null){
				NetworkResult networkResult = new NetworkResult(device, result);
				sentRemotable(networkResult.toString().getBytes());
			}else{
				sentRemotable(result.toString().getBytes());
			}
		}
		taskResult.completedTask(result);
	}

	// tarefa de ping
	protected Network pingService(Protocol prot) throws InterruptedException, IOException, MissedEventException {

		Network results = new Network();
		
		// tem que setar as portas pelo menos para um dos protocolos
		if ((portTcp == 0 && Protocol.TCP == prot)
				|| (portUdp == 0 && Protocol.UDP == prot)) {
			throw new IOException("Need to set a port service for requested Protocol ");
		}

		// criar uma conexao
		ClientAbstract cliente = FactoryCliente.getInstance(prot);

		// seta o evento
		cliente.setReceiveDataEvent(new ReceiveDataEvent() {
			int count = 0;// dentro da classe interna

			@Override
			public void receive(byte[] data, int offset, int read) {
				long vlr = (System.currentTimeMillis() - tempoInicio);
				coletaPing[count++] = vlr;
			}
		});

		if (prot == Protocol.TCP){
			cliente.connect(portTcp, ip);
		}else{
			cliente.connect(portUdp, ip);
		}

		for (int i = 0; i < NUM_PING; i++) {
			tempoInicio = System.currentTimeMillis();
			cliente.send(("pigado" + i).getBytes());

			// bota 400ms para retorno
			// por causa do UDP que não tem controle de fluxo
			Thread.sleep(400);
		}

		// bota no objeto de resposta
		if (prot == Protocol.TCP) {
			results.setResultPingTcp(Arrays.copyOf(coletaPing, coletaPing.length));
		} else {
			results.setResultPingUdp(Arrays.copyOf(coletaPing, coletaPing.length));
		}

		cliente.close();// fecha a conexao
		
		return results;
	}

	/**
	 * Manda persistir remotamente os resultados da rede
	 * 
	 * @param result - Resultado do teste
	 */
	private void sentRemotable(final byte json[]) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {	
				final Semaphore mutex = new Semaphore(0);
				
				ClientAbstract cliente = FactoryCliente.getInstance(Protocol.TCP);
				cliente.setReceiveDataEvent(new ReceiveDataEvent() {
					@Override
					public void receive(byte[] data, int offset, int read) {
						Log.d(LOG, "Enviado com sucesso");
						mutex.release();
					}
				});
				
				try {
					cliente.connect(portPersistenceRemote, ip);
					cliente.send(json);
					
					mutex.acquire();
					cliente.close();
				} catch (IOException e) {
					Log.e(LOG, "Error on transmition to remote data");
				} catch (MissedEventException e) {
					Log.e(LOG, "Need to set a listening interface");
				} catch (InterruptedException e) {
					Log.e(LOG, "Error on thread interrupted");
				}
			}
		}).start();
	}
}