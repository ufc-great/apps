package br.ufc.mdcc.mpos.net.service;

import br.ufc.mdcc.mpos.net.model.Cloudlet;

/**
 * Retorna a lista de endpoints de serviços requisitados pela tarefa de descoberta de serviço
 * 
 * @author hack
 *
 */
interface CloudletDetectable {

	void address(Cloudlet cloudlet);
	
}
