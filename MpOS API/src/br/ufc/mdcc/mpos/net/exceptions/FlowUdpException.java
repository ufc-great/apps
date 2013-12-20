package br.ufc.mdcc.mpos.net.exceptions;

import java.io.IOException;

public final class FlowUdpException extends IOException {

	private static final long serialVersionUID = 927363741853751935L;

	public FlowUdpException(String msg) {
		super(msg);
	}
	
	public FlowUdpException() {
		this("Thread Interrupted send data flow");
	}	
}
