package br.ufc.mdcc.netester.hs;

import java.util.Date;
import java.util.Map;
import java.util.Timer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import br.ufc.mdcc.mpos.util.ExportDatabase;
import br.ufc.mdcc.netester.R;

public final class HunterSignalActivity extends Activity {

	// in ms
	private int frequency = 1000;
	private int indexTime = 0;
	private boolean started = false;

	private String items[] = new String[] { "1s", "5s", "10s" };

	private Timer networkTimer = null;

	private HunterSignalDAO historyDAO;
	private NetworkStateTask networkStateTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hunter_signal);
		setupActionBar();

		historyDAO = new HunterSignalDAO(getApplicationContext());

		configureButton();
	}

	private void configureButton() {

		final Button button = (Button) findViewById(R.id.buttonStart);

		button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!started) {
					started = true;
					button.setText(R.string.button_stop);

					networkStateTask = new NetworkStateTask(HunterSignalActivity.this, items[indexTime]);

					networkTimer = new Timer("CaptureNetworkState");
					networkTimer.schedule(networkStateTask, new Date(), frequency);
				} else {
					started = false;
					button.setText(R.string.button_start);

					if (networkTimer != null) {
						final Map<String, Integer> networks = networkStateTask.getNetworks();
						final int total = networkStateTask.getTotal();

						if (frequency == 1000) {
							showOnDebugView("Status: Serviço desligado (1s) e resultados salvos!\n");
						} else if (frequency == 5000) {
							showOnDebugView("Status: Serviço desligado (5s) e resultados salvos!\n");
						} else {
							showOnDebugView("Status: Serviço desligado (10s) e resultados salvos!\n");
						}

						networkTimer.cancel();

						// persistence async...
						new Thread() {
							public void run() {
								Date date = new Date();
								for (String key : networks.keySet()) {
									int value = networks.get(key);
									historyDAO.add(key, String.format("%.3f", ((double) value / (double) total) * 100.0), date);
								}
							}
						}.start();

						networkTimer = null;
						networkStateTask = null;
					}
				}
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.hunter_signal, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		
		switch (item.getItemId()) {

			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				break;

			case R.id.menu_action_export:

				// se tiver iniciado a captura manda parar!
				if (started) {

					Button button = (Button) findViewById(R.id.buttonStart);
					Toast.makeText(this, "Captura Paralizada", Toast.LENGTH_LONG).show();

					started = false;
					button.setText(R.string.button_start);

					if (networkTimer != null) {
						networkTimer.cancel();
						networkTimer = null;
					}
				}

				alertDialogBuilder.setTitle("Exportação de Banco de dados");
				alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
				alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// invoca o classe que exporta banco de dados...
						new ExportDatabase(HunterSignalActivity.this, "huntersignal.data", "huntersignal_backup.db").execute();
					}
				});
				alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				alertDialogBuilder.setMessage("Deseja exportar banco de dados?");
				alertDialogBuilder.create().show();
				break;

			case R.id.menu_action_frequency:
				alertDialogBuilder.setTitle(R.string.alert_freq_title);
				alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
				alertDialogBuilder.setItems(items, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						indexTime = which;

						switch (which) {
							case 0:
								frequency = 1000;
								showOnDebugView("Status: Serviço desligado (1s)\n");
								break;

							case 1:
								frequency = 5000;
								showOnDebugView("Status: Serviço desligado (5s)\n");
								break;

							default:
								frequency = 10000;
								showOnDebugView("Status: Serviço desligado (10s)\n");
								break;
						}
					}
				});

				alertDialogBuilder.create().show();
				break;

		}
		return true;
	}

	private void showOnDebugView(final String debug) {
		final TextView debugView = (TextView) findViewById(R.id.debugView);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.hunterLinearLayout);
		linearLayout.post(new Runnable() {
			@Override
			public void run() {
				debugView.setText(debug);
			}
		});
	}
}