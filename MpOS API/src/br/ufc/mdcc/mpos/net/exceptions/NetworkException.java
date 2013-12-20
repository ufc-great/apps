package br.ufc.mdcc.mpos.net.exceptions;

public class NetworkException extends Exception {

	private static final long serialVersionUID = 3896983009102697980L;

	public NetworkException(String msg){
		super(msg);
	}
	
	public NetworkException() {
		this("Any network error");
	}
	
}
