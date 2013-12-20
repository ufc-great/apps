package br.ufc.mdcc.benchimage;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author Philipp
 */
public final class Main {
	
	public static String packagePath = "/br/ufc/mdcc/benchimage/res/";
	
	static {
		PropertyConfigurator.configure(Main.class.getResourceAsStream(packagePath + "log4j.properties"));
		System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	public static void main(String... args) throws Exception {
		
		System.out.println("Iniciou o PicFilterService\n");
		
		PhotoTcpServer pts = new PhotoTcpServer();
		new Thread(pts, pts.getService().toString()).start();
		
	}
}
