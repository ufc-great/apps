package br.ufc.mdcc.benchimage.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Philipp
 */
public abstract class DAO {
	
	protected SQLiteDatabase database;
	private final DatabaseManager databaseManager;
	
	/**
	 * Ao instanciar sempre vai abrir o recurso do database, sem a necessidade
	 * do metodo open!
	 * 
	 * @param con
	 */
	public DAO(Context con) {
		databaseManager = new DatabaseManager(con);
	}
	
	public void openDatabase(){
		database = databaseManager.getWritableDatabase();
	}
	
	public void closeDatabase(){
		database.close();
	}
	
}
