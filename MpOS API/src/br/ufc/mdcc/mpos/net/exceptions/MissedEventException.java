package br.ufc.mdcc.mpos.net.exceptions;

public final class MissedEventException extends Exception {

	private static final long serialVersionUID = -1580074066152767986L;

	public MissedEventException(String msg){
		super(msg);
	}
	
	public MissedEventException() {
		this("Need define a ReceiveDataEvent for receive data from socket");
	}
	
}
