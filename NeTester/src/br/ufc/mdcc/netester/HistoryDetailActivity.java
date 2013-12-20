package br.ufc.mdcc.netester;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.ufc.mdcc.mpos.net.model.Network;
import br.ufc.mdcc.mpos.persistence.ProfileNetworkDAO;
import br.ufc.mdcc.netester.util.ParcelableNetwork;

/**
 * Essa classe suporta aquela funcionalidade de swiper!
 * 
 * @author hack
 */
public final class HistoryDetailActivity extends FragmentActivity {
	private ArrayList<Network> history = null;

	HistoryItemAdapter historyItemAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_detail);

		Intent intent = getIntent();
		int position = intent.getIntExtra("pos", 0);

		try {
			history = new ProfileNetworkDAO(getBaseContext()).getLastResults();
		} catch (JSONException e) {
		} catch (ParseException e) {
		}

		// Show the Up button in the action bar.
		setupActionBar();

		// putOnScreen(network);
		historyItemAdapter = new HistoryItemAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.history_viewpager);
		mViewPager.setAdapter(historyItemAdapter);
		mViewPager.setCurrentItem(position);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.history_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				break;

		}
		return true;
	}

	public class HistoryItemAdapter extends FragmentPagerAdapter {

		public HistoryItemAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			// System.out.println(position);

			Fragment fragment = new DetailFragment();
			Bundle args = new Bundle();
			args.putParcelable("history", new ParcelableNetwork(history.get(position)));
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return history.size();
		}
	}

	public static class DetailFragment extends Fragment {
		private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);

		public DetailFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ParcelableNetwork parcelableNetwork = getArguments().getParcelable("history");
			Network network = parcelableNetwork.getNetwork();
			network.generatingPingTCPStats();
			network.generatingPingUDPStats();

			View rootView = inflater.inflate(R.layout.fragment_history_swiper, container, false);

			TextView tv = (TextView) rootView.findViewById(R.id.hist_ping_min_tcp);
			tv.setText(network.getPingMinTCP() + " ms");

			((TextView) rootView.findViewById(R.id.hist_ping_med_tcp)).setText(network.getPingMedTCP() + " ms");
			((TextView) rootView.findViewById(R.id.hist_ping_max_tcp)).setText(network.getPingMaxTCP() + " ms");
			((TextView) rootView.findViewById(R.id.hist_ping_min_udp)).setText(network.getPingMinUDP() + " ms");
			((TextView) rootView.findViewById(R.id.hist_ping_med_udp)).setText(network.getPingMedUDP() + " ms");
			((TextView) rootView.findViewById(R.id.hist_ping_max_udp)).setText(network.getPingMaxUDP() + " ms");
			((TextView) rootView.findViewById(R.id.hist_jitter)).setText(network.getJitter() + " ms");
			if (network.getLossPacket() == 0) {
				((TextView) rootView.findViewById(R.id.hist_perda_pacote)).setText(getString(R.string.status_nenhuma_perda_pacote));
			} else {
				((TextView) rootView.findViewById(R.id.hist_perda_pacote)).setText(String.valueOf(network.getLossPacket()) + "/14");
			}
			((TextView) rootView.findViewById(R.id.hist_date)).setText(dateFormat.format(network.getDate()));

			tv = (TextView) rootView.findViewById(R.id.hist_type);
			tv.setText(network.getType());

			formatBandwidthTextView(rootView, R.id.hist_banda_download, Double.parseDouble(network.getBandwidthDownload()));
			formatBandwidthTextView(rootView, R.id.hist_banda_upload, Double.parseDouble(network.getBandwidthUpload()));

			return rootView;
		}

		private void formatBandwidthTextView(View root, int id, double bandwidth) {
			if (bandwidth > 9) {
				((TextView) root.findViewById(id)).setText(String.format("%.2f", bandwidth) + " MBit/s");
			} else {
				((TextView) root.findViewById(id)).setText(String.format("%.3f", bandwidth) + " MBit/s");
			}
		}
	}
}
