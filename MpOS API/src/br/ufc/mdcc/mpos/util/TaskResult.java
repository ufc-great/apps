package br.ufc.mdcc.mpos.util;

public interface TaskResult<T> {

	/**
	 * Manda o resultado do objeto de volta para quem implementa essa Interface
	 * em geral deve ser implementado por uma Activity
	 * 
	 * @param obj - objeto de resposta de algum processamento em background
	 */
	void completedTask(T obj);
	
	/**
	 * Se a Activity tiver uma barra de progresso será usada por esse metodo..
	 * @param completed
	 */
	void taskOnGoing(int completed);
	
	/**
	 * Se a Activity tiver uma barra de progresso e uma mensagem de status
	 * @param completed
	 * @param statusText
	 */
	void taskOnGoing(int completed,String statusText);
	
}
