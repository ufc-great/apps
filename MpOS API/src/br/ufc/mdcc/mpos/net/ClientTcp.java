package br.ufc.mdcc.mpos.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * 
 * @version 1.55v
 * 
 * @author philipp
 */

public final class ClientTcp extends ClientAbstract  {
	
	private Socket conex;
	
	ClientTcp() {
		super.threadName = "Thread_Cliente_TCP";
	}
	
	/**
	 * O padrao desse metodo inicia uma thread de autolistening
	 */
	@Override
	public void connect(int porta, String ip) throws IOException, MissedEventException {
		
		conex = new Socket(ip,porta);
		conex.setSoTimeout(30000);
		
		if(event==null)
			throw new MissedEventException();
		
		startThreadListening();
	}

	void receive() throws IOException, ClassNotFoundException {
		byte tempBuffer[] = new byte[BUFFER];//4k de buffer
		
	    InputStream is = conex.getInputStream();
	    
	    int read = 0;
	    byte zero = (byte)0;
	    while ((read = is.read(tempBuffer)) != -1){
	    	event.receive(tempBuffer,0,read);
	    	Arrays.fill(tempBuffer, zero);
	    }
	}
	
	public void send(byte data[]) throws IOException{
		conex.getOutputStream().write(data,0,data.length);
		conex.getOutputStream().flush();
	}
	
	public void close() throws IOException{
		conex.close();
		thread.interrupt();
	}
}