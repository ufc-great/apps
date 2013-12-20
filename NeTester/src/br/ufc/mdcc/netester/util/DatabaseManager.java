package br.ufc.mdcc.netester.util;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class DatabaseManager extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 1;
	
	private final ArrayList<String> tabelas = new ArrayList<String>(1);
	
	public DatabaseManager(Context con) {
		super(con, "huntersignal.data", null, DATABASE_VERSION);
		
		loadingTables();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//constroi a tabela dentro do sistema
		for(String tabela:tabelas)
			db.execSQL(tabela);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion!=newVersion){
			db.execSQL("DROP TABLE IF EXISTS history");
		    onCreate(db);
		}
	}

	private void loadingTables(){
		tabelas.add("CREATE TABLE history (network TEXT NOT NULL, packetCount TEXT NOT NULL, date DATETIME NOT NULL)");
	}
}