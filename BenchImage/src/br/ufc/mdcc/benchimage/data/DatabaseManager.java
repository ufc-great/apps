package br.ufc.mdcc.benchimage.data;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Philipp
 */
public final class DatabaseManager extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 4;
	
	private final ArrayList<String> tabelas = new ArrayList<String>(1);
	
	public DatabaseManager(Context con) {
		super(con, "app.db", null, DATABASE_VERSION);
		
		tabelas.add("CREATE TABLE performance (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, photo_name TEXT NOT NULL, filter_name TEXT NOT NULL, local TEXT NOT NULL, photo_size TEXT NOT NULL, " +
				"execution_cpu_time INTEGER NOT NULL, download_time INTEGER NOT NULL, upload_time INTEGER NOT NULL, total_time INTEGER NOT NULL, " +
				"download_tx TEXT NOT NULL, upload_tx TEXT NOT NULL, date DATETIME NOT NULL) ");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(String tabela:tabelas)
			db.execSQL(tabela);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion!=newVersion){
			db.execSQL("DROP TABLE IF EXISTS performance");
		    onCreate(db);
		}
	}
}