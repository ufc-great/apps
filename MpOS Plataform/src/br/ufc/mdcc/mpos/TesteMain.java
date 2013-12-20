package br.ufc.mdcc.mpos;

public final class TesteMain {

	
	/**
	 * 
	 * Protegendo o banco de dados de acessos multiplos
	 * 
	 * @param args
	 * @throws InterruptedException 
	 
	public static void main(String[] args) throws InterruptedException {
		List<Thread> lst = new ArrayList<Thread>(50);
		final Random random = new Random();
		
		for(int i=0;i<50;i++){
			lst.add(new Thread(new Runnable() {
				@Override
				public void run() {
					
					String dateStr = String.valueOf(random.nextInt(1000));
					String appName = String.valueOf(random.nextInt(1000));
					String tcp = String.valueOf(random.nextInt(200));
					String udp = String.valueOf(random.nextInt(200));
					String mobileId = "adas";
					
					//FachadeDao.getInstance().getNetProfileDao().adicionar(dateStr, appName, tcp, udp,mobileId);
					
				}
			}));
		}
		
		for(Thread th:lst)
			th.start();
		
		for(Thread th:lst)
			th.join();
		
		System.out.println("Terminou");

	}
	*/
}
