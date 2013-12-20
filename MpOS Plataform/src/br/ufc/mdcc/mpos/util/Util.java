package br.ufc.mdcc.mpos.util;

import java.util.regex.Pattern;

/**
 * @author Philipp
 */
public final class Util {
	
	private static Pattern patternIpAddress;
	
	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	static {
		patternIpAddress = Pattern.compile(IPADDRESS_PATTERN);
	}
	
	private Util() {
		
	}
	
	/**
	 * Validate ip address with regular expression
	 * 
	 * @param ip
	 * @return true valid ip address, false invalid ip address
	 */
	public static boolean validateIpAddress(final String ip) {
		return patternIpAddress.matcher(ip).matches();
	}
	
	/**
	 * Esse metodo reconhece um padrão dentro de um array de bytes
	 * 
	 * @param source
	 *            array que gostaria de buscar o padrao
	 * @param target
	 *            array alvo que gostaria detectar o padrao neste array
	 * @return verdade se achou ou falso se não achou
	 */
	public static boolean containsArrays(byte source[], byte target[]) {
		return indexOfArrays(source, target, 0, target.length) > -1;
	}
	
	private static int indexOfArrays(byte target[], byte source[], int sourceOffset, int sourceCount) {
		int targetOffset = 0, targetCount = target.length;
		int fromIndex = 0;
		
		if (fromIndex >= targetCount) {
			return (sourceCount == 0 ? targetCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (sourceCount == 0) {
			return fromIndex;
		}
		
		byte first = source[sourceOffset];
		int max = targetOffset + (targetCount - sourceCount);
		
		for (int i = targetOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (target[i] != first) {
				while (++i <= max && target[i] != first)
					;
			}
			
			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + sourceCount - 1;
				for (int k = sourceOffset + 1; j < end && target[j] == source[k]; j++, k++)
					;
				
				if (j == end) {
					/* Found whole string. */
					return i - targetOffset;
				}
			}
		}
		
		return -1;
	}
}
