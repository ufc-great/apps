package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import br.ufc.mdcc.mpos.net.AbstractServer;

/**
 * @author Philipp
 */
public final class PingUdpServer extends AbstractServer {
	
	private DatagramSocket requestClientSocket;
	private byte tempBuffer[] = new byte[1024 * 4];
	
	// esse pacote é reusado várias vezes...
	private DatagramPacket pacote = null;
	
	public PingUdpServer(int port) {
		super(port, "srvPingUdp", PingUdpServer.class);
		
		startName = "Servidor Echo UDP Iniciado";
		pacote = new DatagramPacket(tempBuffer, tempBuffer.length);
	}
	
	public PingUdpServer() {
		this(30002);
	}
	
	@Override
	public void run() {
		
		logger.info(startName);
		
		try {
			requestClientSocket = new DatagramSocket(getPort());
			
			// não dispara uma thread para cada cliente como acontece no TCP...
			// =(
			while (true) {
				
				// limpa pacote para receber o dado
				pacoteClean();
				
				// echo...
				requestClientSocket.receive(pacote);// recebe o dado do socket
				requestClientSocket.send(pacote);
			}
			
		} catch (IOException e) {
			logger.info("Error de Conexão", e);
		}
	}
	
	private void pacoteClean() {
		// limpa o buffer
		Arrays.fill(tempBuffer, (byte) 0);
		
		// limpa os atributos do pacote
		pacote.setAddress(null);
		pacote.setData(tempBuffer);
		pacote.setPort(0);
	}
}
