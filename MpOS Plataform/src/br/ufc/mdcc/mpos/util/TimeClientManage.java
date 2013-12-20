package br.ufc.mdcc.mpos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

/**
 * @author Philipp
 */
public final class TimeClientManage {
	
	private Logger logger;
	private Semaphore mutex;
	private Map<String, List<Long>> timeClients;
	
	private TimeClientManage() {
		mutex = new Semaphore(1);
		timeClients = new TreeMap<String, List<Long>>();
		
		logger = Logger.getLogger(TimeClientManage.class);
	}
	
	private static class SingletonHolder {
		public static final TimeClientManage instance = new TimeClientManage();
	}
	
	public static TimeClientManage getInstance() {
		return SingletonHolder.instance;
	}
	
	public void addTime(final String ip) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				long time = System.currentTimeMillis();
				
				try {
					mutex.acquire();
					
					if (timeClients.containsKey(ip)) {
						timeClients.get(ip).add(time);
					} else {
						List<Long> arrives = new ArrayList<Long>(15);
						arrives.add(time);
						
						timeClients.put(ip, arrives);
					}
					
				} catch (InterruptedException e) {
					logger.error("Algum Bug com que semaforo dessa classe!");
				} finally {
					mutex.release();
				}
			}
		}).start();
	}
	
	/**
	 * Um cliente TCP vai pegar os resultados para enviar para o mobile
	 * 
	 * @param ip
	 * @return
	 */
	public List<Long> getTimeResults(String ip) {
		try {
			mutex.acquire();
			
			if (timeClients.containsKey(ip)) {
				return timeClients.remove(ip);
			}
			
		} catch (InterruptedException e) {
			logger.error("Algum Bug com que semaforo dessa classe!");
		} finally {
			mutex.release();
		}
		
		return null;
	}
}
