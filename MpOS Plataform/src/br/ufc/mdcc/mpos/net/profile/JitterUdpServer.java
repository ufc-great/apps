package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import br.ufc.mdcc.mpos.net.AbstractServer;
import br.ufc.mdcc.mpos.util.TimeClientManage;

/**
 * Esse servidor é dedicado para receber os pacotes dos clientes, o envio dos
 * resultados é via outro servidor, por outra porta usando o procolo tcp. Isso é
 * para diminuir o overhead dessas outras operações e atrapalhar no
 * processamento do jitter
 * 
 * @author Philipp
 */
public final class JitterUdpServer extends AbstractServer {
	
	private DatagramSocket requestClientSocket;
	
	public JitterUdpServer(int port) {
		super(port, "srvJitterUdp", JitterUdpServer.class);
		
		startName = "Servidor Jitter UDP Iniciado";
	}
	
	public JitterUdpServer() {
		this(30010);
	}
	
	@Override
	public void run() {
		
		logger.info(startName);
		try {
			requestClientSocket = new DatagramSocket(getPort());
			
			byte tempBuffer[] = new byte[1024 * 4];
			
			while (true) {
				
				// limpa pacote para receber o dado
				DatagramPacket pacote = new DatagramPacket(tempBuffer, tempBuffer.length);
				requestClientSocket.receive(pacote);
				TimeClientManage.getInstance().addTime(pacote.getAddress().toString());
			}
		} catch (IOException e) {
			logger.info("Error de Conexão", e);
		}
	}
}
