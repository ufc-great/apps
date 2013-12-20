package br.ufc.mdcc.benchimage;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import br.ufc.mdcc.benchimage.data.Config;
import br.ufc.mdcc.benchimage.data.Result;
import br.ufc.mdcc.benchimage.task.BitmapProcessLocalTask;
import br.ufc.mdcc.benchimage.task.BitmapProcessRemoteTask;
import br.ufc.mdcc.benchimage.util.OnItemSelectedListenerAdapter;
import br.ufc.mdcc.benchimage.util.WidgetUtil;
import br.ufc.mdcc.mpos.util.ExportDatabase;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * @author Philipp
 */

public final class MainActivity extends Activity {

	private WidgetUtil wu;

	private Config config;

	private final String LOG = MainActivity.class.getSimpleName();
	private SharedPreferences preferences;

	private String photoName;
	private String outputDir;
	private long vmSize = 0L;

	private String cloudletIp = "192.168.42.133";
	private String cloudIp = "200.129.39.120";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wu = new WidgetUtil(this);
		config = new Config();

		configureSpinner();
		configureButton();
		configureStatusView();

		preferencesLoading();
		createDirOutput();

		processImage();

		Log.i(LOG, "Iniciou PicFilter");
	}

	private void preferencesLoading() {
		preferences = getSharedPreferences("picfilter", Context.MODE_PRIVATE);
		cloudletIp = preferences.getString("cloudletIp", "192.168.42.133");
	}

	private void processImage() {

		TaskResultAdapter<Result> taskResultAdapter = new TaskResultAdapter<Result>() {
			@Override
			public void completedTask(Result obj) {
				if (obj != null) {
					ImageView imageView = (ImageView) findViewById(R.id.imageView);
					imageView.setImageBitmap(obj.getBitmap());

					TextView tv_tamanho = (TextView) findViewById(R.id.tv_tamanho);
					tv_tamanho.setText("Tamanho/Foto: " + config.getSize() + "/" + photoName);

					TextView tv_execucao = (TextView) findViewById(R.id.tv_execucao);
					if (obj.getTotalTime() != 0) {
						double segundos = obj.getTotalTime() / 1000.0;
						tv_execucao.setText("Tempo de\nExecução: " + String.format("%.3f", segundos) + "s");
					} else {
						tv_execucao.setText("Tempo de\nExecução: 0s");
					}

					if (obj.getConfig().getFilter().equals("Benchmark")) {
						double segundos = obj.getTotalTime() / 1000.0;
						tv_execucao.setText("Tempo de\nExecução: " + String.format("%.3f", segundos) + "s");
					}

					buttonStatusChange(R.id.buttonExecute, true, "Inicia");
				} else {
					TextView tv_status = (TextView) findViewById(R.id.tv_status);
					tv_status.setText("Status: Algum Error na transmissão!");
					buttonStatusChange(R.id.buttonExecute, true, "Inicia");
				}
			}

			@Override
			public void taskOnGoing(int completed, String statusText) {
				TextView tv_status = (TextView) findViewById(R.id.tv_status);
				tv_status.setText("Status: " + statusText);
			}
		};

		TextView tv_execucao = (TextView) findViewById(R.id.tv_execucao);
		tv_execucao.setText("Tempo de\nExecução: 0s");

		TextView tv_tamanho = (TextView) findViewById(R.id.tv_tamanho);
		tv_tamanho.setText("Tamanho/Foto: " + config.getSize() + "/" + photoName);

		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageBitmap(null);

		// obtem as config from spinner
		getConfigFromSpinner();

		System.gc();

		if (config.getFilter().equals("Cartoonizer") && vmSize <= 64 && (config.getSize().equals("8MP") || config.getSize().equals("6MP") || config.getSize().equals("4MP"))) {

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Celular limitado!");
			alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			alertDialogBuilder.setNegativeButton(R.string.button_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			alertDialogBuilder.setMessage("Celular não suporta o Cartoonizer, minimo recomendo é 128MB de VMSize");
			// cria e mostra
			alertDialogBuilder.create().show();

			buttonStatusChange(R.id.buttonExecute, true, "Inicia");
			TextView tv_status = (TextView) findViewById(R.id.tv_status);
			tv_status.setText("Status: Requisição anterior não suporta Filtro!");

		} else {
			if (config.getLocal().equals("Local")) {
				executeTask(new BitmapProcessLocalTask(taskResultAdapter, outputDir, getApplicationContext(), config));
			} else {
				if (config.getLocal().equals("Cloudlet")) {
					// cloudlet
					executeTask(new BitmapProcessRemoteTask(taskResultAdapter, outputDir, getApplicationContext(), config, cloudletIp));
				} else {
					// cloud on Internet
					executeTask(new BitmapProcessRemoteTask(taskResultAdapter, outputDir, getApplicationContext(), config, cloudIp));
				}

			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeTask(AsyncTask<Void, String, Result> bitmapTask) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			bitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			bitmapTask.execute();
		}
	}

	private void createDirOutput() {

		File storage = Environment.getExternalStorageDirectory();
		outputDir = storage.getAbsolutePath() + File.separator + "PicFilter_Temp";
		File dir = new File(outputDir);

		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Process.killProcess(Process.myPid());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		switch (item.getItemId()) {

			case R.id.menu_action_ip_cloudlet:

				// instancia um EditText
				final EditText editText = new EditText(this);
				editText.setText(cloudletIp);
				editText.setRawInputType(Configuration.KEYBOARD_12KEY);

				alertDialogBuilder.setTitle("Cloudlet IP");
				alertDialogBuilder.setMessage("Digite o IP do Cloudlet");
				alertDialogBuilder.setView(editText);
				alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						cloudletIp = editText.getText().toString();

						Editor edit = preferences.edit();
						edit.putString("cloudletIp", cloudletIp);
						edit.apply();
					}
				});
				alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
				alertDialogBuilder.create().show();

				break;

			case R.id.menu_action_export:
				alertDialogBuilder.setTitle("Exportação de Banco de dados");
				alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
				alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// invoca o classe que exporta banco de dados...
						new ExportDatabase(MainActivity.this, "app.db", "benchimage_backup.db").execute();
					}
				});
				alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				alertDialogBuilder.setMessage("Deseja exportar banco de dados?");
				// cria e mostra
				alertDialogBuilder.create().show();
				break;

		}

		return true;
	}

	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.alert_exit_title);
		alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialogBuilder.setMessage(R.string.alert_exit_message);
		// cria e mostra
		alertDialogBuilder.create().show();
	}

	private void configureButton() {
		wu.setListeningButton(R.id.buttonExecute, new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonStatusChange(R.id.buttonExecute, false, "Processando");
				processImage();
			}
		});
	}

	private void buttonStatusChange(int id, boolean state, String text) {
		Button but = (Button) findViewById(id);
		but.setEnabled(state);
		but.setText(text);
	}

	private void configureSpinner() {

		Spinner spinnerImage = (Spinner) findViewById(R.id.spin_image);
		Spinner spinnerFilter = (Spinner) findViewById(R.id.spin_filter);
		Spinner spinnerSize = (Spinner) findViewById(R.id.spin_size);
		Spinner spinnerLocal = (Spinner) findViewById(R.id.spin_local);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_img, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerImage.setAdapter(adapter);
		spinnerImage.setSelection(2);

		adapter = ArrayAdapter.createFromResource(this, R.array.spinner_filter, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerFilter.setAdapter(adapter);

		adapter = ArrayAdapter.createFromResource(this, R.array.spinner_local, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerLocal.setAdapter(adapter);

		adapter = ArrayAdapter.createFromResource(this, R.array.spinner_size, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSize.setAdapter(adapter);
		spinnerSize.setSelection(6);

		spinnerImage.setOnItemSelectedListener(new OnItemSelectedListenerAdapter() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				photoName = (String) parent.getItemAtPosition(pos);
				config.setImage(photoToFileName(photoName));
			}
		});

		spinnerFilter.setOnItemSelectedListener(new OnItemSelectedListenerAdapter() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				config.setFilter((String) parent.getItemAtPosition(pos));
			}
		});

		spinnerSize.setOnItemSelectedListener(new OnItemSelectedListenerAdapter() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				config.setSize((String) parent.getItemAtPosition(pos));
			}
		});

		spinnerLocal.setOnItemSelectedListener(new OnItemSelectedListenerAdapter() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				config.setLocal((String) parent.getItemAtPosition(pos));
			}
		});

		getConfigFromSpinner();
	}

	private void getConfigFromSpinner() {

		Spinner spinnerImage = (Spinner) findViewById(R.id.spin_image);
		Spinner spinnerFilter = (Spinner) findViewById(R.id.spin_filter);
		Spinner spinnerSize = (Spinner) findViewById(R.id.spin_size);
		Spinner spinnerLocal = (Spinner) findViewById(R.id.spin_local);

		photoName = (String) spinnerImage.getSelectedItem();
		config.setImage(photoToFileName(photoName));
		config.setFilter((String) spinnerFilter.getSelectedItem());
		config.setSize((String) spinnerSize.getSelectedItem());
		config.setLocal((String) spinnerLocal.getSelectedItem());

	}

	private void configureStatusView() {
		TextView tv_vmsize = (TextView) findViewById(R.id.tv_vmsize);
		vmSize = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		tv_vmsize.setText("VMSize " + vmSize + "MB");

		if (vmSize < 128) {
			tv_vmsize.setTextColor(Color.RED);
		} else if (vmSize == 128) {
			tv_vmsize.setTextColor(Color.YELLOW);
		} else {
			tv_vmsize.setTextColor(Color.GREEN);
		}

		TextView tv_execucao = (TextView) findViewById(R.id.tv_execucao);
		tv_execucao.setText("Tempo de\nExecução: 0s");

		TextView tv_tamanho = (TextView) findViewById(R.id.tv_tamanho);
		tv_tamanho.setText("Tamanho/Foto: " + config.getSize() + "/" + photoName);

		TextView tv_status = (TextView) findViewById(R.id.tv_status);
		tv_status.setText("Status: Sem Atividade");

	}

	private String photoToFileName(String name) {

		if (name.equals("FAB Show")) {
			return "img1.jpg";
		} else if (name.equals("Cidade")) {
			return "img4.jpg";
		} else if (name.equals("SkyLine")) {
			return "img5.jpg";
		}

		return null;
	}
}
