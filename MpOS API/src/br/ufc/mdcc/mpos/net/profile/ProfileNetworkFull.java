package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import android.util.Log;
import br.ufc.mdcc.mpos.net.ClientAbstract;
import br.ufc.mdcc.mpos.net.FactoryCliente;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.ReceiveDataEvent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.net.service.NetworkServices;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.Util;

/**
 * @author Philipp
 */
public class ProfileNetworkFull extends ProfileNetworkTask {

	private int portJitter, portJitterResult, portBandwidth;

	private byte data[] = new byte[32 * 1024];

	private Network results;
	private boolean bandwidthDone = false;

	public ProfileNetworkFull(TaskResult<Network> result, String ip) throws MissedEventException {
		super(ip, result, ProfileNetworkFull.class);

		portJitter = NetworkServices.JITTER.getPort();
		portJitterResult = NetworkServices.JITTER.getPortSec();
		portBandwidth = NetworkServices.BANDWIDTH.getPort();

		// randomize os dados que serão enviados
		new Random().nextBytes(data);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(LOG, "ProfileFull Started");
	}

	@Override
	protected Network doInBackground(Void... params) {

		results = new Network();

		try {
			Log.i(LOG, "ping tcp");
			Network temp = pingService(Protocol.TCP);
			results.setResultPingTcp(temp.getResultPingTcp());
			publishProgress(15);

			Log.i(LOG, "ping udp");
			temp = pingService(Protocol.UDP);
			results.setResultPingUdp(temp.getResultPingUdp());
			publishProgress(30);

			Log.i(LOG, "loss packet udp");
			// conta os pacotes perdidos UDP
			int count = 0;
			for (long udpPing : temp.getResultPingUdp()) {
				if (udpPing == 0) {
					count++;
				}
			}
			for (long tcpPing : results.getResultPingTcp()) {
				if (tcpPing == 0) {
					count++;
				}
			}
			results.setLossPacket(count);
			publishProgress(35);

			Log.i(LOG, "jitter calculation");
			jitterCalculation();
			retrieveJitterResult();
			publishProgress(55);

			Log.i(LOG, "bandwidth calculation");
			boolean finish = calculeBandwidth();
			publishProgress(100);

			// foi parado por um timer
			if (!finish)
				return null;

			Log.d(LOG, "ProfileFull Finished");

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

	/**
	 * Definição: RFC 4689 - defines jitter as “the absolute value of the
	 * difference between the Forwarding Delay of two consecutive received
	 * packets belonging to the same stream”. The jitter is important in
	 * real-time communications when the variation between delays can cause a
	 * negative impact to the server quality, such voice over IP services.
	 * Referencia: http://tools.ietf.org/html/rfc4689#section-3.2.5 Em resumo o
	 * jitter calcula os intervalos tempos entre o intervalo de tempo (corrente)
	 * e intervalo de tempo (anterior) e deve ser enviado num fluxo de taxa
	 * constante. #formula no servidor Intervalo de tempo (It) = Tempo_chegada -
	 * Tempo_anterior Jitter = it_atual - it_anterior (voce pode pegar a média,
	 * maximo e minimo) Sobre os resultados: Um jitter de 15ms é regular, abaixo
	 * de 5ms é excelente e acima de 15ms é ruim para o padrão VoIP. Seja esse
	 * site: http://www.onsip.com/tools/voip-test
	 * 
	 * @throws MissedEventException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void jitterCalculation() throws IOException, MissedEventException, InterruptedException {

		ClientAbstract cliente = FactoryCliente.getInstance(Protocol.UDP);
		cliente.setReceiveDataEvent(new ReceiveDataEvent() {
			@Override
			public void receive(byte[] data, int offset, int read) {
				Log.d(LOG, "Jitter Finalizado");
			}
		});

		cliente.connect(portJitter, ip);

		for (int i = 0; i < 21; i++) {
			cliente.send(("jitter").getBytes());

			// bota 250ms para retorno
			// por causa do UDP que não tem controle de fluxo
			Thread.sleep(250);
		}

		cliente.close();

	}

	private void retrieveJitterResult() throws IOException, MissedEventException, InterruptedException {

		// espera uns 1.5s
		Thread.sleep(1500);
		final Semaphore mutex = new Semaphore(0);

		ClientAbstract cliente = FactoryCliente.getInstance(Protocol.TCP);
		cliente.setReceiveDataEvent(new ReceiveDataEvent() {
			@Override
			public void receive(byte[] data, int offset, int read) {
				Log.d(LOG, "Retrieve data from server for Jitter calcule");

				results.setJitter(Integer.parseInt(new String(data, offset, read)));
				// System.out.println(results.getJitter());

				mutex.release();
			}
		});

		cliente.connect(portJitterResult, ip);
		cliente.send("get".getBytes());

		mutex.acquire();
		cliente.close();
	}

	// TODO: tranformar KB/s para kbps (kilobit per second)
	private boolean calculeBandwidth() throws IOException, MissedEventException, InterruptedException {

		final Semaphore mutex = new Semaphore(0);

		ClientAbstract cliente = FactoryCliente.getInstance(Protocol.TCP);
		cliente.setReceiveDataEvent(new ReceiveDataEvent() {

			private long countBytes = 0L;

			private byte end_down[] = "end_down".getBytes();
			private byte end_session[] = "end_session".getBytes();

			@Override
			public void receive(byte[] data, int offset, int read) {
				countBytes += (long) read;

				if (Util.containsArrays(data, end_down)) {

					// System.out.println("Bytes: "+countBytes);

					// bytes * 8bits / 7s + 1E+6 = X Mbits
					double bandwidth = ((double) (countBytes * 8L) / (double) (7.0 * 1E+6));
					results.setBandwidthDownload(String.valueOf(bandwidth));
					countBytes = 0L;
					mutex.release();
				} else if (Util.containsArrays(data, end_session)) {

					bandwidthDone = true;
					String dataBlock = new String(data, offset, read);
					String res[] = dataBlock.split(":");
					results.setBandwidthUpload(res[1]);

					mutex.release();
				}
			}
		});

		// timer for finish!
		Timer timeout = new Timer("Timeout Bandwidth");
		timeout.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!bandwidthDone) {
					// para garantir que não vai travar nenhum semaphoro!
					mutex.release();
					mutex.release();
					Log.i(LOG, "Bandwidth Timeout...");
				}
			}
		}, 120000);// 120s de timeout

		Log.i(LOG, "bandwidth (download)");
		cliente.connect(portBandwidth, ip);
		cliente.send("down".getBytes());
		mutex.acquire();
		publishProgress(75);

		Log.i(LOG, "bandwidth (upload)");
		cliente.send("up".getBytes());

		// faz upload! - 11s
		long timeExit = System.currentTimeMillis() + 11000;
		while (System.currentTimeMillis() < timeExit) {
			cliente.send(data);
		}
		cliente.send("end_up".getBytes());

		Log.i(LOG, "bandwidth (ended upload)");
		mutex.acquire();
		cliente.close();

		// cancela o timer
		timeout.cancel();

		return bandwidthDone;
	}
}
