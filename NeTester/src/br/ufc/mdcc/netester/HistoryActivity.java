package br.ufc.mdcc.netester;

import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.persistence.ProfileNetworkDAO;

public final class HistoryActivity extends Activity {

	private ProfileNetworkDAO dao;
	private ArrayList<Network> array;
	private NetworkArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_history);

		// Show the Up button in the action bar.
		setupActionBar();

		try {
			dao = new ProfileNetworkDAO(getBaseContext());
			array = dao.getLastResults();
			adapter = new NetworkArrayAdapter(getBaseContext(), array);
			generatingListView();
		} catch (JSONException e) {
		} catch (ParseException e) {
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				break;

			case R.id.menu_act_hist_clean:
				cleanAlertDialog();
				break;

		}
		return true;
	}

	private void generatingListView() {

		ListView listView = (ListView) findViewById(R.id.hist_list);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				Intent historyDetails = new Intent(HistoryActivity.this.getBaseContext(), HistoryDetailActivity.class);
				historyDetails.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// historyDetails.putExtra("network", new
				// ParcelableNetwork(array.get(position)));
				historyDetails.putExtra("pos", position);
				startActivity(historyDetails);
			}
		});
	}

	private void cleanAlertDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setTitle(R.string.alert_history_title);
		alertDialogBuilder.setMessage(R.string.alert_history_message);
		alertDialogBuilder.setIcon(android.R.drawable.ic_delete);
		alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dao.clean();
				adapter.clear();
				adapter.notifyDataSetChanged();
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		// cria e mostra
		alertDialogBuilder.create().show();
	}
}
