package br.ufc.mdcc.mpos.persistence;

public final class FachadeDao {
	
	/**
	 * Usando desse jeito a inicialização da instancia acontece de forma segura,
	 * pois a JVM garante a inicialização serial de uma classe estática quando
	 * ela é chamada! Após instancia o metodo fica apenas "only-read" não
	 * precisando de sychronized no getInstance e nem volatile no atributo de
	 * instance
	 * 
	 * @author Philipp
	 */
	private static class SingletonHolder {
		public static final FachadeDao instance = new FachadeDao();
	}
	
	private NetProfileDao netProfileDao;
	
	private FachadeDao() {
		netProfileDao = new NetProfileDao();
	}
	
	public static FachadeDao getInstance() {
		return SingletonHolder.instance;
	}
	
	public NetProfileDao getNetProfileDao() {
		return netProfileDao;
	}
	
}
