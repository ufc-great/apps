package br.ufc.mdcc.benchimage.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;

/**
 * @author Philipp
 */
public final class PerformanceDAO extends DAO {

	private final String pattern = "dd-MM-yyyy HH:mm:ss";
	private final DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
	//private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
	
	private final String TABLE_NAME;
	
	// FILEDS
	// private final String F_ID = "id";
	private final String F_PHOTO_NAME = "photo_name";
	private final String F_FILTER_NAME = "filter_name";
	private final String F_LOCAL = "local";
	private final String F_SIZE = "photo_size";
	private final String F_EXECUTION_CPU_TIME = "execution_cpu_time";
	private final String F_UPLOAD_TIME = "upload_time";
	private final String F_DOWNLOAD_TIME = "download_time";
	private final String F_FINAL_TIME = "total_time";
	private final String F_DOWN_TX = "download_tx";
	private final String F_UP_TX = "upload_tx";
	private final String F_DATE = "date";

	public PerformanceDAO(Context con) {
		super(con);
		TABLE_NAME = "performance";
	}

	/**
	 * Adiciona os resultados obtidos no PingTask
	 * 
	 * @param results - Objeto com os resultados do PingTask
	 */
	public void add(Result results) {

		openDatabase();
		
		ContentValues cv = new ContentValues();

		cv.put(F_PHOTO_NAME, results.getConfig().getImage());
		cv.put(F_FILTER_NAME, results.getConfig().getFilter());
		cv.put(F_LOCAL, results.getConfig().getLocal());
		cv.put(F_SIZE, results.getConfig().getSize());
		cv.put(F_EXECUTION_CPU_TIME, results.getExecutionCPUTime());
		cv.put(F_UPLOAD_TIME, results.getUploadTime());
		cv.put(F_DOWNLOAD_TIME, results.getDonwloadTime());
		cv.put(F_FINAL_TIME, results.getTotalTime());
		cv.put(F_DOWN_TX, results.getDownloadTx());
		cv.put(F_UP_TX, results.getUploadTx());
		cv.put(F_DATE, dateFormat.format(results.getDate()));

		database.insert(TABLE_NAME, null, cv);
		
		closeDatabase();
	}

	/**
	 * Consulta os ultimos 50 resultados do profile network
	 * 
	 * @return lista dos 50 ultimos resultados.
	 * @throws JSONException
	 * @throws ParseException 
	 */
//	public ArrayList<Result> getLastResults() throws JSONException, ParseException {
//
//		openDatabase();
//		
//		Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
//				+ " ORDER BY id DESC LIMIT 50", null);
//
//		ArrayList<Result> lista = new ArrayList<Result>(50);
//
//		// obtem todos os indices das colunas da tabela
//		int idx_filter_name = cursor.getColumnIndex(F_FILTER_NAME);
//		int idx_local = cursor.getColumnIndex(F_LOCAL);
//		int idx_size = cursor.getColumnIndex(F_SIZE);
//		int idx_time = cursor.getColumnIndex(F_EXECUTION_CPU_TIME);
//		int idx_date = cursor.getColumnIndex(F_DATE);
//		int idx_photo_name = cursor.getColumnIndex(F_PHOTO_NAME);
//
//		if (cursor !=null && cursor.moveToFirst()) {
//			do {
//				Result results = new Result();
//				results.setDate(simpleDateFormat.parse(cursor.getString(idx_date)));
//				results.setExecutionCPUTime(cursor.getInt(idx_time));
//				results.getConfig().setFilter(cursor.getString(idx_filter_name));
//				results.getConfig().setImage(cursor.getString(idx_photo_name));
//				results.getConfig().setLocal(cursor.getString(idx_local));
//				results.getConfig().setSize(cursor.getString(idx_size));
//				
//				lista.add(results);
//			}while (cursor.moveToNext());
//		}
//		
//		cursor.close();
//		closeDatabase();
//		
//		return lista;
//	}
	
	public void clean(){
		openDatabase();
		
		database.delete(TABLE_NAME, null, null);
		
		closeDatabase();
	}
}

