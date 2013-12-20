package br.ufc.mdcc.mpos.net;


/**
 * Fabrica de clientes do protocolo TCP e UDP
 * 
 * @version 1.5v
 * @author philipp
 *
 */
public final class FactoryCliente {

	private FactoryCliente(){
		
	}
	
	public static ClientAbstract getInstance(Protocol protocolo) {
		switch (protocolo) {
		
		case TCP:
			return new ClientTcp();
		case UDP:
			return new ClientUdp();
		}
		
		return null;
	}
}
