package br.ufc.mdcc.mpos.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import br.ufc.mdcc.mpos.net.exceptions.FlowUdpException;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * 
 * 
 * @version 1.55v
 * 
 * @author philipp
 * 
 */
public final class ClientUdp extends ClientAbstract {

	private DatagramSocket socket;
	private InetAddress endereco;

	// payload do UDP pode ser no maximo de 64k, botei um pouco menos
	private final byte payload[] = new byte[BUFFER];

	private DatagramPacket pacoteEnvio = null, pacoteRecebimento = null;

	ClientUdp() {
		super.threadName = "Thread_Cliente_UDP";

		pacoteEnvio = new DatagramPacket(new byte[BUFFER], BUFFER);
		pacoteRecebimento = new DatagramPacket(new byte[BUFFER], BUFFER);
	}
	
	@Override
	public void connect(int porta, String ip) throws IOException, MissedEventException {
		endereco = InetAddress.getByName(ip);

		socket = new DatagramSocket();
		socket.connect(endereco, porta);
		
		//jah define o endereco para aonde os dados serao enviados...
		pacoteEnvio.setSocketAddress(socket.getRemoteSocketAddress());
		
		if(event==null)
			throw new MissedEventException();
		
		startThreadListening();
	}
	
	void receive() throws IOException, ClassNotFoundException {
		//fica esperando aqui até receber os dados do servidor remoto
		socket.receive(pacoteRecebimento);
		
		event.receive(pacoteRecebimento.getData(),pacoteRecebimento.getOffset(),pacoteRecebimento.getLength());
		clean(pacoteRecebimento);
	}
	
	public void send(byte data[]) throws IOException{
		InputStream bis = new ByteArrayInputStream(data);
		
		//quebra o dados no tamanho do payload e envia para o cliente
		int tamanhoRestante=0;
		while ((tamanhoRestante = bis.read(payload)) > 0){
			//constroi o pacote
			pacoteEnvio.setData(payload, 0, tamanhoRestante);
			
			//envia o pacote para o end:porta definidos
			socket.send(pacoteEnvio);
			clean(pacoteEnvio);
			
			//se nao botar um timer vai atropelar o envio
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new FlowUdpException();
			}
		}
	}
	
	public void close() throws IOException{
		socket.close();
		thread.interrupt();
	}
	
	private void clean(DatagramPacket pacote){
		//clean o payload do pacote
		pacote.setData(new byte[BUFFER]);
	}
}
