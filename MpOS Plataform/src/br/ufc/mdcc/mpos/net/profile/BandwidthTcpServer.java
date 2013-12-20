package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.util.Util;

/**
 * @author Philipp
 */
public final class BandwidthTcpServer extends TcpServer {
	
	/**
	 * Porta padrão do serviço é 30005
	 */
	public BandwidthTcpServer() {
		this(30005);
	}
	
	public BandwidthTcpServer(int port) {
		super(port, "srvBandwidthTcp", BandwidthTcpServer.class);
		startName = "Servidor de Resultados do Bandwidth TCP Iniciado";
	}
	
	@Override
	public void clientRequest(Socket connection) throws IOException {
		
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();
		
		// String remoteIp = connection.getInetAddress().toString();
		
		byte tempBuffer[] = new byte[1024 * 4];
		
		// dados que são produzidos por cada thread para fazer a simulação de um
		// binario
		byte fixedData[] = new byte[1024 * 32];
		new Random().nextBytes(fixedData);
		
		byte down[] = "down".getBytes();
		byte end_up[] = "end_up".getBytes();
		
		long endUploadTime = 0L, initTime = 0L, skipTime = 0L;
		boolean firstTime = true;
		
		long countBlock = 0L;
		int read;
		while ((read = input.read(tempBuffer)) != -1) {
			
			if (Util.containsArrays(tempBuffer, down)) {
				
				// System.out.println("[DEBUG] Iniciou down:"+remoteIp);
				
				// vai enviando blocos de 32k até estourar os 7 segundos da
				// transferencia
				long timeExit = System.currentTimeMillis() + 7000;
				while (System.currentTimeMillis() < timeExit) {
					output.write(fixedData);
				}
				output.write("end_down".getBytes());
				output.flush();
				
				logger.info(">> Terminou operacao de download");
				
				// System.out.println("[DEBUG] Finalizou down: "+remoteIp);
			} else if (Util.containsArrays(tempBuffer, end_up)) {
				
				// apenas para debug
				// StringBuilder debug = new StringBuilder();
				// long finalTime = System.currentTimeMillis() - initTime;
				// debug.append("[DEBUG]").append("\n");
				// debug.append("Tempo passado para finalizar o upload: "+finalTime).append("\n");
				// debug.append("Size: "+countBlock).append("\n");
				// debug.append("Banda: "+((double) countBlock / (double)
				// finalTime)).append("\n");
				// logger.info(debug);
				
				// protocolo de termino do upload
				//bytes * 8bits / 7s + 1E+6 = X Mbit/s
				double bandwidth = ((double) (countBlock * 8) / ((double) (7.0 * 1E+6)));
				
				
				// envia o resultado para o celular!
				output.write(("end_session:" + String.valueOf(bandwidth)).getBytes());
				output.flush();
				
				logger.info(">> Terminou operacao de upload");
				
			} else {
				long current = System.currentTimeMillis();
				if (firstTime) {
					firstTime = false;
					initTime = System.currentTimeMillis();
					skipTime = initTime + 2000;// byte ignorados
					endUploadTime = skipTime + 7000;
				} else if (skipTime < current && current < endUploadTime) {
					// vai descatar os primeiro 2s e capturar
					// apenas os dados enviados pelo usuario durante 7s
					// e descatar o resto...
					countBlock += (long) read;
				}
				
				// metodo antigo sem a logica acima...
				// countBlock += (long)read;
			}
			// limpa o buffer
			Arrays.fill(tempBuffer, (byte) 0);
		}
		close(input);
		close(output);
	}
}