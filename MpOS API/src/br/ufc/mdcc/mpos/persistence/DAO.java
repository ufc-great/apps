package br.ufc.mdcc.mpos.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DAO {
	
	private final SQLiteOpenHelper databaseManager;
	
	protected SQLiteDatabase database;
	
	/**
	 * Vai instanciar o databaseManager do MpOS framework.
	 * 
	 * @param con
	 */
	public DAO(Context con) {
		databaseManager = new DatabaseManager(con);
	}
	
	/**
	 * Vai instanciar um databaseManager externo ao framework.
	 * 
	 * @param databaseManager
	 */
	public DAO(SQLiteOpenHelper databaseManager){
		this.databaseManager = databaseManager;
	}
	
	public void openDatabase(){
		database = databaseManager.getWritableDatabase();
	}
	
	public void closeDatabase(){
		database.close();
	}
}
