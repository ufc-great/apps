package br.ufc.mdcc.mpos.persistence;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import br.ufc.mdcc.mpos.R;
import br.ufc.mdcc.mpos.net.model.Network;

public final class ProfileNetworkDAO extends DAO {

	private final String pattern = "dd-MM-yyyy HH:mm:ss";
	private final DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
	
	private final String TABLE_NAME;
	
	// FILEDS
	// private final String F_ID = "id";
	private final String F_DATE = "date";
	private final String F_LOSS = "loss";
	private final String F_JITTER = "jitter";
	private final String F_UDP = "udp";
	private final String F_TCP = "tcp";
	private final String F_DOWN = "down";
	private final String F_UP = "up";
	private final String F_TYPE = "type";

	public ProfileNetworkDAO(Context con) {
		super(con);
		
		TABLE_NAME = con.getString(R.string.name_table_netprofile);
	}

	/**
	 * Adiciona os resultados obtidos no PingTask
	 * 
	 * @param results - Objeto com os resultados do PingTask
	 */
	public void add(Network results) {

		openDatabase();
		
		ContentValues cv = new ContentValues();

		cv.put(F_DATE, dateFormat.format(results.getDate()));
		cv.put(F_LOSS, results.getLossPacket());
		cv.put(F_JITTER, results.getJitter());
		cv.put(F_UDP, Network.arrayToString(results.getResultPingUdp()));
		cv.put(F_TCP, Network.arrayToString(results.getResultPingTcp()));
		cv.put(F_DOWN, results.getBandwidthDownload());
		cv.put(F_UP, results.getBandwidthUpload());
		cv.put(F_TYPE, results.getType());

		database.insert(TABLE_NAME, null, cv);
		
		closeDatabase();
	}

	/**
	 * Consulta os ultimos 15 resultados do profile network
	 * 
	 * @return lista dos 15 ultimos resultados.
	 * @throws JSONException
	 * @throws ParseException 
	 */
	public ArrayList<Network> getLastResults() throws JSONException, ParseException {

		openDatabase();
		
		Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
				+ " ORDER BY id DESC LIMIT 15", null);

		ArrayList<Network> lista = new ArrayList<Network>(15);

		// obtem todos os indices das colunas da tabela
		int idx_loss = cursor.getColumnIndex(F_LOSS);
		int idx_jitter = cursor.getColumnIndex(F_JITTER);
		int idx_udp = cursor.getColumnIndex(F_UDP);
		int idx_tcp = cursor.getColumnIndex(F_TCP);
		int idx_down = cursor.getColumnIndex(F_DOWN);
		int idx_up = cursor.getColumnIndex(F_UP);
		int idx_type = cursor.getColumnIndex(F_TYPE);
		int idx_date = cursor.getColumnIndex(F_DATE);

		if (cursor !=null && cursor.moveToFirst()) {
			do {
				Network network = new Network();
				network.setJitter(cursor.getInt(idx_jitter));
				network.setLossPacket(cursor.getInt(idx_loss));
				network.setBandwidthDownload(cursor.getString(idx_down));
				network.setBandwidthUpload(cursor.getString(idx_up));
				network.setResultPingTcp(Network.StringToLongArray(cursor.getString(idx_tcp)));
				network.setResultPingUdp(Network.StringToLongArray(cursor.getString(idx_udp)));
				network.setType(cursor.getString(idx_type));
				network.setDate(simpleDateFormat.parse(cursor.getString(idx_date)));
				
				lista.add(network);
			}while (cursor.moveToNext());
		}
		
		cursor.close();
		closeDatabase();
		
		return lista;
	}
	
	public void clean(){
		openDatabase();
		
		database.delete(TABLE_NAME, null, null);
		
		closeDatabase();
	}
}

