package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.util.TimeClientManage;

/**
 * @author Philipp
 */
public final class JitterRetrieveTcpServer extends TcpServer {
	
	/**
	 * Porta padrão do serviço é 30000
	 */
	public JitterRetrieveTcpServer() {
		this(30011);
	}
	
	public JitterRetrieveTcpServer(int port) {
		super(port, "srvJitterTcp", JitterRetrieveTcpServer.class);
		startName = "Servidor de Resultados do Jitter TCP Iniciado";
	}
	
	@Override
	public void clientRequest(Socket connection) throws IOException {
		
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();
		
		byte tempBuffer[] = new byte[1024 * 4];
		String remoteIp = connection.getInetAddress().toString();
		
		int read = 0;
		while ((read = input.read(tempBuffer)) != -1) {
			
			String data = new String(tempBuffer, 0, read);
			
			if (data.contains("get")) {
				
				List<Long> times = TimeClientManage.getInstance().getTimeResults(remoteIp);
				
				if (times != null) {
					byte resp[] = calculeJitter(times);
					output.write(resp, 0, resp.length);
				} else {
					String resp = "bug";
					output.write(resp.getBytes(), 0, resp.length());
				}
				
				// evitar "connection reset"
				output.flush();
				break;
			}
			
			// limpa o buffer
			Arrays.fill(tempBuffer, (byte) 0);
		}
		
		close(output);
		close(input);
	}
	
	// manda todos os resultados
	// manda o minimo, medio e maximo
	private byte[] calculeJitter(List<Long> times) {
		
		// System.out.println("Tempos: "+times);
		
		List<Long> timeInterval = new ArrayList<Long>(times.size() - 1);
		
		long timeReference = times.get(0);
		int tam = times.size();
		
		for (int i = 1; i < tam; i++) {
			timeInterval.add(times.get(i) - timeReference);
			timeReference = times.get(i);
		}
		
		// System.out.println("Intervalo: "+timeInterval);
		
		List<Long> jitterInterval = new ArrayList<Long>(timeInterval.size() - 1);
		timeReference = timeInterval.get(0);
		tam = timeInterval.size();
		
		long jitterTotal = 0L;
		for (int i = 1; i < tam; i++) {
			long jitter = timeInterval.get(i) - timeReference;
			
			jitter = (jitter < 0) ? jitter *= (-1) : jitter;
			
			jitterTotal += jitter;
			jitterInterval.add(jitter);
			timeReference = timeInterval.get(i);
		}
		
		// System.out.println("Jitter: "+jitterInterval);
		
		// TODO: Depois será decido como será enviado esse jitter, por enquanto
		// apenas a média.
		// Collections.sort(jitterInterval);
		// long jitterMin = Collections.min(jitterInterval);
		// long jitterMax = Collections.max(jitterInterval);
		long jitterMed = jitterTotal / (long) jitterInterval.size();
		
		// compoe o resultado para ser retornado
		StringBuilder result = new StringBuilder();
		// result.append(jitterInterval.toString()).append(";");
		// result.append(jitterMin).append(";");
		result.append(jitterMed);
		// result.append(jitterMax).append(";");
		
		return result.toString().getBytes();
	}
}
