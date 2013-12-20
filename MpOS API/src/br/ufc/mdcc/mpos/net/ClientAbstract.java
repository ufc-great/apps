package br.ufc.mdcc.mpos.net;

import java.io.IOException;

import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * 
 * @version 1.5v
 * 
 * @author philipp
 *
 */
public abstract class ClientAbstract{
	
	protected final int BUFFER = 4*1024;
	protected ReceiveDataEvent event;
	protected Thread thread;
	protected String threadName;
	
	public abstract void connect(int porta, String ip) throws IOException, MissedEventException;
	public abstract void close() throws IOException;
	
	abstract void receive() throws IOException, ClassNotFoundException;
	public abstract void send(byte data[]) throws IOException;
	
	public ClientAbstract() {
	}
	
	protected void startThreadListening() throws IOException{
		//manda finalizar as tarefas
		if(thread!=null)
			close();
		
		thread = new Thread(){
			@Override
			public void run() {
				try {
					int connectionRetry = 0;
					while(connectionRetry < 7){
						receive();
						connectionRetry++;
					}
				} catch (Exception e) {
					//quando o servidor ou cliente fechar o socket
				}
			}
		};
		
		thread.setName(threadName);
		thread.start();
	}
	
	public void setReceiveDataEvent(ReceiveDataEvent event){
		if(event==null)
			throw new NullPointerException("É preciso ter um evento de receber dados");
		
		this.event=event;
	}
}
