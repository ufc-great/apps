package br.ufc.mdcc.netester.hs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import br.ufc.mdcc.mpos.persistence.DAO;
import br.ufc.mdcc.netester.util.DatabaseManager;

/**
 * Pode ser usado durante a operação de offloading
 * @author hack
 *
 */
public final class HunterSignalDAO extends DAO{

	private final String pattern = "dd-MM-yyyy HH:mm:ss";
	private final DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
	
	public HunterSignalDAO(Context con) {
		super(new DatabaseManager(con));
	}

	public void add(String network, String packetCount, Date date){
		
		openDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put("network", network);
		cv.put("packetCount", packetCount);
		cv.put("date", dateFormat.format(date));
		
		database.insert("history", null, cv);
		
		closeDatabase();
		
	}
}
