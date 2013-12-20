package br.ufc.mdcc.mpos.persistence;

import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import br.ufc.mdcc.mpos.R;

/**
 * Pode ser usado durante a operação de offloading
 * 
 * @author hack
 */
public final class MobileDAO extends DAO {

	private final String TABLE_NAME;

	// F_ID = FIELD ID
	private final String F_ID = "id";

	public MobileDAO(Context con) {
		super(con);

		TABLE_NAME = con.getString(R.string.name_table_user);
	}

	// apenas procura se existe
	private String getMobileId() {

		openDatabase();

		Cursor cursor = database.query(TABLE_NAME, new String[] { F_ID }, null, null, null, null, F_ID);

		String mobileId = null;

		if (cursor != null && cursor.moveToFirst()) {
			do {
				mobileId = cursor.getString(0);
			} while (cursor.moveToNext());
		}

		closeDatabase();

		return mobileId;
	}

	// gera e devolve o mobileId
	private String generatingMobileId() {

		openDatabase();

		String uuid = UUID.randomUUID().toString();

		ContentValues campos = new ContentValues();
		campos.put(F_ID, uuid);

		// insere no banco
		database.insert(TABLE_NAME, null, campos);

		closeDatabase();

		return uuid;
	}

	public String checkMobileId() {
		String mobileId = getMobileId();
		if (mobileId == null || mobileId.equals(""))
			mobileId = generatingMobileId();

		return mobileId;
	}
}
