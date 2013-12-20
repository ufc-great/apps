package br.ufc.mdcc.mpos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public final class ExportDatabase extends AsyncTask<Void, Void, Void> {

	private Context context;
	private ProgressDialog progressDialog;

	private String internalDatabase;
	private String exportDatabaseName;

	public ExportDatabase(Context context, String databaseName, String exportDatabaseName) {
		this.context = context;
		this.internalDatabase = "/data/" + context.getPackageName() + "/databases/" + databaseName;
		this.exportDatabaseName = exportDatabaseName;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(context, "", "Exportando dados...", true);
		progressDialog.show();
	}

	@Override
	protected Void doInBackground(Void... params) {

		FileInputStream fisSrc = null;
		FileOutputStream fisDst = null;

		FileChannel fcSrc = null;
		FileChannel fcDst = null;

		try {
			File externalStorage = Environment.getExternalStorageDirectory();
			File internalStorage = Environment.getDataDirectory();

			if (externalStorage.canWrite()) {

				File currentDB = new File(internalStorage, internalDatabase);
				File backupDB = new File(externalStorage, exportDatabaseName);// persist on device root

				if (currentDB.exists()) {

					// open stream
					fisSrc = new FileInputStream(currentDB);
					fisDst = new FileOutputStream(backupDB);

					// obtem os canais
					fcSrc = fisSrc.getChannel();
					fcDst = fisDst.getChannel();

					// transfere de forma otimizado...
					fcDst.transferFrom(fcSrc, 0, fcSrc.size());
				}
			}
		} catch (IOException e) {
			Log.w("Error no processamento do I/O", e);
		} finally {
			try {
				if (fcSrc != null) {
					fcSrc.close();
				}
				if (fcDst != null) {
					fcDst.close();
				}
				if (fisSrc != null) {
					fisSrc.close();
				}
				if (fisDst != null) {
					fisDst.close();
				}
			} catch (IOException e) {
				Log.w("Error ao tentar fechar os arquivos", e);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		// fecha o dialog e avisa que terminou...
		progressDialog.dismiss();
		Toast.makeText(context, "Finalizado a Exportação dos dados", Toast.LENGTH_LONG).show();
	}
}
