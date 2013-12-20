package br.ufc.mdcc.netester;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import br.ufc.mdcc.mpos.MposActivity;
import br.ufc.mdcc.mpos.config.Application;
import br.ufc.mdcc.mpos.config.Profile;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.net.profile.ProfileController;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import br.ufc.mdcc.netester.hs.HunterSignalActivity;
import br.ufc.mdcc.netester.util.WidgetUtil;

/**
 * @author philipp
 */

@Application(profile = Profile.FULL, deviceDetails = true, locationProfile = true)
public final class MainActivity extends MposActivity {

	private final WidgetUtil widgetUtil = new WidgetUtil(this);

	private ProgressBar progressBar;

	private boolean remote;
	private boolean localSessionDisable = true;
	private boolean working = false;

	private String cloudIp = "200.129.39.120";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);

		configureButton();
		checkNetworkGPS();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
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

		if (working) {
			alertDialogBuilder.setMessage(R.string.alert_exit_message_testing);
		} else {
			alertDialogBuilder.setMessage(R.string.alert_exit_message);
		}
		// cria e mostra
		alertDialogBuilder.create().show();
	}

	// acao de um item no menu
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_act_princ_huntersignal:
				Intent hunterSignal = new Intent(getBaseContext(), HunterSignalActivity.class);
				hunterSignal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(hunterSignal);
				break;

			case R.id.menu_act_princ_history:
				Intent history = new Intent(getBaseContext(), HistoryActivity.class);
				history.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(history);
				break;
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void configureButton() {

		// enquanto os serviços padrões não estiverem inicializados
		Button button = (Button) findViewById(R.id.button_local);
		button.setEnabled(false);
		button.setVisibility(View.GONE);

		// muda o estado do botão de conexão local
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new ChangeLocalButtonStatusTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new ChangeLocalButtonStatusTask().execute();
		}

		widgetUtil.setListeningButton(R.id.button_internet, new Button.OnClickListener() {
			public void onClick(View v) {
				if (isOnline()) {
					beginTestScreen();
					
					try {
						//start network analysis...
						ProfileController.getInstance().networkAnalysis(new TaskResultAdapter<Network>() {
							@Override
							public void completedTask(Network obj) {
								completedResults(obj);
							}

							@Override
							public void taskOnGoing(int completed) {
								progressBar.setProgress(completed);
								feedbackProgress(completed);
							}
						}, cloudIp);// amazon
					} catch (NetworkException e) {
						cleanScreen();
					} catch (MissedEventException e) {
						cleanScreen();
					}

					remote = true;
				} else {
					// mostra um alerta!
					connectionAlertDialog();
				}
			}
		});

		widgetUtil.setListeningButton(R.id.button_local, new Button.OnClickListener() {
			public void onClick(View v) {
				beginTestScreen();

				try {
					ProfileController.getInstance().networkAnalysis(new TaskResultAdapter<Network>() {
						@Override
						public void completedTask(Network obj) {
							completedResults(obj);
						}

						@Override
						public void taskOnGoing(int completed) {
							progressBar.setProgress(completed);
							feedbackProgress(completed);
						}
					});
				} catch (NetworkException e) {
					cleanScreen();
				} catch (MissedEventException e) {
					cleanScreen();
				}

				remote = false;
			}
		});
	}

	private void completedResults(Network result) {
		if (result != null) {
			result.generatingPingTCPStats();
			result.generatingPingUDPStats();

			// seta o resultado na tela
			putOnScreen(result.getBandwidthDownload(), result.getBandwidthUpload(), result.getLossPacket(), result.getPingMinTCP(), result.getPingMedTCP(), result.getPingMaxTCP(), result.getPingMinUDP(), result.getPingMedUDP(), result.getPingMaxUDP(), result.getJitter());

			// trata a mensagem de status
			if (remote) {
				TextView status = (TextView) findViewById(R.id.mensagem_status);
				status.setText(R.string.status_message_teste_internet);
				status.setTextColor(Color.GREEN);
			} else {
				TextView status = (TextView) findViewById(R.id.mensagem_status);
				status.setText(R.string.status_message_teste_local);
				status.setTextColor(Color.GREEN);
			}
		} else {
			TextView status = (TextView) findViewById(R.id.mensagem_status);
			status.setText(R.string.status_message_offline);
			status.setTextColor(Color.RED);
		}
	}

	private void beginTestScreen(){
		// OBS: não precisa verificar se está online, pois se o botão da
		// descoberta de serviço foi habilitado!
		
		TextView status = (TextView) findViewById(R.id.mensagem_status);
		status.setText(R.string.status_message_executing_test);
		status.setTextColor(Color.YELLOW);

		// trava o botao
		Button button = (Button) findViewById(R.id.button_local);
		button.setEnabled(false);
		button = (Button) findViewById(R.id.button_internet);
		button.setEnabled(false);

		cleanScreen();
	}
	
	private void cleanScreen() {
		feedbackProgress(0);

		widgetUtil.stringToTextView(R.id.ping_min_tcp, null);
		widgetUtil.stringToTextView(R.id.ping_med_tcp, null);
		widgetUtil.stringToTextView(R.id.ping_max_tcp, null);
		widgetUtil.stringToTextView(R.id.ping_min_udp, null);
		widgetUtil.stringToTextView(R.id.ping_med_udp, null);
		widgetUtil.stringToTextView(R.id.ping_max_udp, null);
		widgetUtil.stringToTextView(R.id.jitter, null);
		widgetUtil.stringToTextView(R.id.perda_pacote, null);
		widgetUtil.stringToTextView(R.id.banda_download, null);
		widgetUtil.stringToTextView(R.id.banda_upload, null);
	}

	private void putOnScreen(String down, String up, int perda, int... args) {
		int cont = 0;
		widgetUtil.stringToTextView(R.id.ping_min_tcp, args[cont++] + " ms");
		widgetUtil.stringToTextView(R.id.ping_med_tcp, args[cont++] + " ms");
		widgetUtil.stringToTextView(R.id.ping_max_tcp, args[cont++] + " ms");
		widgetUtil.stringToTextView(R.id.ping_min_udp, args[cont++] + " ms");
		widgetUtil.stringToTextView(R.id.ping_med_udp, args[cont++] + " ms");
		widgetUtil.stringToTextView(R.id.ping_max_udp, args[cont++] + " ms");
		widgetUtil.stringToTextView(R.id.jitter, args[cont++] + " ms");

		if (perda == 0) {
			widgetUtil.stringToTextView(R.id.perda_pacote, getString(R.string.status_nenhuma_perda_pacote));
		} else {
			widgetUtil.stringToTextView(R.id.perda_pacote, String.valueOf(perda) + "/14");
		}

		formatBandwidthTextView(R.id.banda_download, Double.parseDouble(down));
		formatBandwidthTextView(R.id.banda_upload, Double.parseDouble(up));
	}

	private void formatBandwidthTextView(int id, double bandwidth) {
		if (bandwidth > 9) {
			widgetUtil.stringToTextView(id, String.format("%.2f", bandwidth) + " MBit/s");
		} else {
			widgetUtil.stringToTextView(id, String.format("%.3f", bandwidth) + " MBit/s");
		}
	}

	private void feedbackProgress(int completed) {
		switch (completed) {

			case 0:
				progressBar.setProgress(completed);
				progressBar.setVisibility(View.VISIBLE);
				working = true;
				break;

			case 35:
				Toast.makeText(getBaseContext(), R.string.status_feedback_35, Toast.LENGTH_LONG).show();
				break;

			case 55:
				Toast.makeText(getBaseContext(), R.string.status_feedback_55, Toast.LENGTH_LONG).show();
				break;

			case 75:
				Toast.makeText(getBaseContext(), R.string.status_feedback_75, Toast.LENGTH_LONG).show();
				break;

			case 100:
				Toast.makeText(getBaseContext(), R.string.status_feedback_100, Toast.LENGTH_LONG).show();

				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout_buttons);
				linearLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisibility(View.GONE);

						// libera o botao
						Button button = null;
						if (!localSessionDisable) {
							button = (Button) findViewById(R.id.button_local);
							button.setEnabled(true);
						}

						button = (Button) findViewById(R.id.button_internet);
						button.setEnabled(true);

					}
				}, 3500);

				working = false;

				break;
		}
	}

	private void checkNetworkGPS() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!enable) {
			showSettingsAlert();
		}
	}

	private void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		alertDialog.setTitle(R.string.alert_gps_title);
		alertDialog.setMessage(R.string.alert_gps_message);
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
		alertDialog.setPositiveButton(R.string.button_configuration, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		alertDialog.setNegativeButton(R.string.button_late, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	private void connectionAlertDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setTitle(R.string.alert_connection_title);
		alertDialogBuilder.setMessage(R.string.alert_connection_message);
		alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		// cria e mostra
		alertDialogBuilder.create().show();
	}

	/**
	 * Essa classe interna irá verificar até 45s, se foi descoberto algum
	 * serviço padrão localmente na rede onde está esse smartphone
	 * 
	 * @author hack
	 */
	private class ChangeLocalButtonStatusTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		public Boolean doInBackground(Void... params) {
			int count = 0;

			while (count < 60) {

				if (ProfileController.getInstance().cloudletReady()) {
					return true;
				}
				// taxa de atualização 0,75s aprox.
				try {
					Thread.sleep(750);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count++;
			}

			return false;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				// se achou serviço locais o botao pode ser ativado e desativado
				localSessionDisable = false;
				Toast.makeText(MainActivity.this, R.string.toast_discovery_service, Toast.LENGTH_LONG).show();

				Button button = (Button) findViewById(R.id.button_local);
				button.setEnabled(result);
				button.setVisibility(View.VISIBLE);
			}
		}
	}
}
