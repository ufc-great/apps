package br.ufc.mdcc.mpos.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public abstract class TcpServer extends AbstractServer{
	private ServerSocket requestClientSocket;
	
	protected TcpServer(int port, String serverName, Class<?> cls) {
		super(port,serverName,cls);
	}
	
	@Override
	public void run() {
		logger.info(startName);
		
		try{
			//abre o socket na porta desejada
			requestClientSocket = new ServerSocket(getPort());

			while (true) {
				
				final Socket connection = requestClientSocket.accept();
				
				if (connection != null){
					//processa cada cliente em paralelo
					new Thread(){
						@Override
						public void run() {
							
							try{
								logger.info("Esse cliente:"+connection.getInetAddress().toString()+" conectou no servidor");
								clientRequest(connection);
								
							}catch(SocketTimeoutException e){
								logger.info("Estouro do timeout de um socket", e);
							}catch(SocketException e){
								logger.info("Error ao processar I/O de um socket", e);
							}catch(IOException e){
								logger.info("Error generico de I/O", e);
							}finally{
								try{
									connection.close();
								}catch(IOException e){}
							}
						}
					}.start();
				}
			}
		}catch(IOException e){
			logger.info("Error de Conexão", e);
		}finally{
			try{
				requestClientSocket.close();
			}catch(IOException e){}
		}
	}
	
	/**
	 * Processa as requisições de cada cliente do socket tcp, atraves de uma thread!
	 * 
	 * @param socket
	 */
	public abstract void clientRequest(Socket connection) throws IOException;

}
