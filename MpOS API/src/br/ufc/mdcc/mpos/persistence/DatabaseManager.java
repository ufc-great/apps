package br.ufc.mdcc.mpos.persistence;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import br.ufc.mdcc.mpos.R;

final class DatabaseManager extends SQLiteOpenHelper {
	
	private Context context;
	
	private static final int DATABASE_VERSION = 4;
	
	private final ArrayList<String> tabelas = new ArrayList<String>(2);
	
	public DatabaseManager(Context con) {
		super(con, con.getString(R.string.database_name), null, DATABASE_VERSION);
		this.context = con;
		
		loadingTables();
	}

	//quando o DAO der getWritableDatabase() vai ser chamado esse metodo
	@Override
	public void onCreate(SQLiteDatabase db) {
		//constroi a tabela dentro do sistema
		for(String tabela:tabelas)
			db.execSQL(tabela);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion!=newVersion){
			db.execSQL(context.getString(R.string.drop_table_netprofile));
			db.execSQL(context.getString(R.string.drop_table_user));
		    onCreate(db);
		}
	}

	private void loadingTables(){
		tabelas.add(context.getString(R.string.create_table_netprofile));
		tabelas.add(context.getString(R.string.create_table_user));
	}
}

