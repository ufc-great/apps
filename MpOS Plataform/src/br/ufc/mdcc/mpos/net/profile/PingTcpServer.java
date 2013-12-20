package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import br.ufc.mdcc.mpos.net.TcpServer;

/**
 * @author Philipp
 */
public final class PingTcpServer extends TcpServer {
	
	/**
	 * Porta padrão do serviço é 30001
	 */
	public PingTcpServer() {
		this(30001);
	}
	
	public PingTcpServer(int port) {
		super(port, "srvPingTcp", PingTcpServer.class);
		startName = "Servidor Echo TCP Iniciado";
	}
	
	/**
	 * Como no servidor TCP cada thread invoca este metodo, então todas as
	 * variaveis devem ser locais desse metodo ou pode ser de instancia mais
	 * usando algum mecanismo de lock, que diminuiria a concorrencia do
	 * servidor!
	 */
	@Override
	public void clientRequest(Socket connection) throws IOException {
		
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();
		
		byte tempBuffer[] = new byte[1024 * 4];
		
		int read = 0;
		while ((read = input.read(tempBuffer)) != -1) {
			
			String data = new String(tempBuffer, 0, read);
			
			if (data.contains("pigado")) {
				// envia para o cliente remoto
				output.write(data.getBytes(), 0, data.length());
				
				// evitar "connection reset"
				output.flush();
			}
			
			// limpa o buffer
			Arrays.fill(tempBuffer, (byte) 0);
		}
		
		close(input);
		close(output);
	}
}
