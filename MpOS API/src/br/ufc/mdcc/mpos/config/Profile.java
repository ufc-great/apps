package br.ufc.mdcc.mpos.config;

/**
 * None = sem profile
 * Light = faz analise apenas do RTT (TCP).
 * Default = faz analise do RTT (TCP, UDP) e perda de pacotes (UDP)
 * Full = faz analise do RTT (TCP, UDP), perda de pacotes (UDP), jitter (UDP) e largura de banda
 * 
 * @author hack
 *
 */
public enum Profile { 
	NONE, LIGHT, DEFAULT, FULL;
}
