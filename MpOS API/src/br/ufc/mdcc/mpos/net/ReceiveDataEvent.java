package br.ufc.mdcc.mpos.net;

/**
 * 
 * Tratar um evento recebido via rede, atraves de interfaces, promove o desaclopamento desses componentes de rede com a aplicação
 * 
 * @version 1.3v
 * 
 * @author philipp
 *
 */
public interface ReceiveDataEvent {
	/**
	 * 
	 * @param data - recebe sempre da rede um array de bytes
	 */
	public void receive(byte data[],int offset, int read);
}
