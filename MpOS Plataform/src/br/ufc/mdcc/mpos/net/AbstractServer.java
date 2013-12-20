package br.ufc.mdcc.mpos.net;

import java.io.Closeable;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.ufc.mdcc.mpos.net.discovery.Service;

/**
 * É uma thread que precisa ser inicializada com a logica do servidor relacionada com um protocolo
 * para realizar alguma tarefa
 * @author hack
 *
 */
public abstract class AbstractServer implements Runnable{

	protected Logger logger;
	protected String startName;
	
	private int port;
	
	private Service service = null;
	
	public AbstractServer(Class<?> cls) {
		this(-1, "srvGeneric", cls);
	}
	
	public AbstractServer(String serviceName,Class<?> cls) {
		this(-1,serviceName,cls);
	}
	
	public AbstractServer(int port, String serviceName,Class<?> cls) {
		logger = Logger.getLogger(cls);
		this.port=port;
		
		service = new Service(serviceName,port);
	}
	
	public void close(Closeable stream){
		if(stream != null){
			try{
				stream.close();
			}catch(IOException e){
				logger.info("Algum erro ao fechar um stream", e);
			}
		}
	}

	public final Service getService() {
		return service;
	}

	public final int getPort() {
		return port;
	}
}
