package com.example.com.gvccracing.android.nookhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author p97z
 * Screen used to configure wifi networks for nook simple touch only
 */
public class WiFiSettings extends Activity {
	
	private String connectedSSID;
	private WifiManager wfm;
	private boolean wifiOn = false;
	private Button wifiOnOffButton;
	private Button wifiScanButton;
	private List<NetInfo> wifiNetworks = new ArrayList<NetInfo>();
	private WiFiAdapter adapter;
	private ListView wifiListView;

	/**
	 * @author p97z
	 * Used to store network information
	 */
	static class NetInfo {
		static int unknownLevel = -5000;
		String SSID;
		String extra;
		int level;
		int netId;
		boolean inrange;
		boolean configured;

		NetInfo(String s, int id, boolean in, boolean conf) {
			SSID = s;
			extra = "";
			level = unknownLevel;
			netId = id;
			inrange = in;
			configured = conf;
		}

		NetInfo(String s, boolean in, boolean conf) {
			SSID = s;
			extra = "";
			level = unknownLevel;
			netId = 0;
			inrange = in;
			configured = conf;
		}

		NetInfo(String s, String e, int id, boolean in, boolean conf) {
			SSID = s;
			extra = e;
			level = unknownLevel;
			netId = id;
			inrange = in;
			configured = conf;
		}

		NetInfo(String s, String e, boolean in, boolean conf) {
			SSID = s;
			extra = e;
			level = unknownLevel;
			netId = 0;
			inrange = in;
			configured = conf;
		}
	}

	public class NetInfoComparator implements java.util.Comparator<NetInfo> {
		public int compare(NetInfo o1, NetInfo o2) {
			if (connectedSSID != null && connectedSSID.equals(o1.SSID)) {
				return -1;
			}
			if (connectedSSID != null && connectedSSID.equals(o2.SSID)) {
				return 1;
			}
			if (o1.inrange && !o2.inrange)
				return -1;
			if (!o1.inrange && o2.inrange)
				return 1;
			if (o1.level < o2.level)
				return 1;
			if (o1.level > o2.level)
				return -1;
			return o1.SSID.compareToIgnoreCase(o2.SSID);
		}
	}

	static class ViewHolder {
		TextView tv1;
		TextView tv2;
		TextView tv3;
		ImageView iv;
	}
	
	class WiFiAdapter extends BaseAdapter {
		final Context cntx;

		WiFiAdapter(Context context) {
			cntx = context;
		}

		public int getCount() {
			return wifiNetworks.size();
		}

		public Object getItem(int position) {
			return wifiNetworks.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getApplicationContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.wifisettings_item, null);
				holder = new ViewHolder();
				holder.tv1 = (TextView) v.findViewById(R.id.wf_ssid);
				holder.tv2 = (TextView) v.findViewById(R.id.wf_capabilities);
				holder.tv3 = (TextView) v.findViewById(R.id.wf_other);
				holder.iv = (ImageView) v.findViewById(R.id.wf_icon);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();
			TextView tv1 = holder.tv1;
			TextView tv2 = holder.tv2;
			TextView tv3 = holder.tv3;
			ImageView iv = holder.iv;
			final WifiInfo winfo = wfm.getConnectionInfo();
			final NetInfo item = wifiNetworks.get(position);
			if (item != null) {
				if (item.inrange && item.configured) {
					tv1.setBackgroundColor(getResources().getColor(
							R.color.wifi_settings_selected_item_bg));
					tv1.setTextColor(getResources().getColor(
							R.color.wifi_settings_selected_item_fg));
					tv2.setBackgroundColor(getResources().getColor(
							R.color.wifi_settings_selected_item_bg));
					tv2.setTextColor(getResources().getColor(
							R.color.wifi_settings_selected_item_fg));
					tv3.setBackgroundColor(getResources().getColor(
							R.color.wifi_settings_selected_item_bg));
					tv3.setTextColor(getResources().getColor(
							R.color.wifi_settings_selected_item_fg));
					iv.setImageDrawable(getResources().getDrawable(
							R.drawable.file_ok));
				} else {
					tv1.setBackgroundColor(getResources().getColor(
							R.color.wifi_settings_not_highlighted_bg));
					tv1.setTextColor(getResources().getColor(
							R.color.wifi_settings_not_highlighted_fg));
					tv2.setBackgroundColor(getResources().getColor(
							R.color.wifi_settings_not_highlighted_bg));
					tv2.setTextColor(getResources().getColor(
							R.color.wifi_settings_not_highlighted_fg));
					tv3.setBackgroundColor(getResources().getColor(
							R.color.wifi_settings_not_highlighted_bg));
					tv3.setTextColor(getResources().getColor(
							R.color.wifi_settings_not_highlighted_fg));
					iv.setImageDrawable(getResources().getDrawable(
							R.drawable.file_notok));
				}

				if (item.SSID.equals(winfo.getSSID())) {
					SpannableString s1 = new SpannableString(item.SSID);
					s1.setSpan(Typeface.BOLD, 0, item.SSID.length(), 0);
					tv1.setText(s1);
					if (item.extra.equals(""))
						tv2.setText("");
					else {
						SpannableString s2 = new SpannableString(item.extra);
						s2.setSpan(Typeface.BOLD, 0, item.extra.length(), 0);
						tv2.setText(s2);
					}
					int ipAddress = winfo.getIpAddress();
					// "Connected, IP: %d.%d.%d.%d"
					String s = String.format(
							getResources().getString(
									R.string.wifi_settings_connected)
									+ " %d.%d.%d.%d", (ipAddress & 0xff),
							(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
							(ipAddress >> 24 & 0xff));
					int sl1 = s.length();
					// ", Level: "
					s += ", "
							+ getResources().getString(
									R.string.wifi_settings_level) + " "
							+ item.level + "dBm " + signalStrengthToString(item.level);
					SpannableString s3 = new SpannableString(s);
					s3.setSpan(Typeface.BOLD, 0, sl1, 0);
					tv3.setText(s3);
				} else {
					SpannableString s1 = new SpannableString(item.SSID);
					s1.setSpan(Typeface.BOLD, 0, item.SSID.length(), 0);
					tv1.setText(s1);
					tv2.setText(item.extra);
					String s;
					if (item.inrange)
						// "Level: "
						s = getResources()
								.getString(R.string.wifi_settings_level)
								+ " "
								+ item.level
								+ "dBm "
								+ signalStrengthToString(item.level);
					else
						// "Not in range"
						s = getResources().getString(
								R.string.wifi_settings_not_in_range);
					if (!item.configured)
						// ", not configured"
						s += ", "
								+ getResources().getString(
										R.string.wifi_settings_not_configured);
					tv3.setText(s);
				}
			}
			return v;
		}
	}

	/**
	 * converts the signal strength to a neat looking [*****] signal strength meter
	 * @param level
	 * @return
	 */
	private String signalStrengthToString(int level) {
		if (level >= -56)
			return "[\u25A0\u25A0\u25A0\u25A0\u25A0]";
		if (level >= -63)
			return "[\u25A0\u25A0\u25A0\u25A0\u25A1]";
		if (level >= -70)
			return "[\u25A0\u25A0\u25A0\u25A1\u25A1]";
		if (level >= -77)
			return "[\u25A0\u25A0\u25A1\u25A1\u25A1]";
		if (level >= -84)
			return "[\u25A0\u25A1\u25A1\u25A1\u25A1]";
		return "[\u25A1\u25A1\u25A1\u25A1\u25A1]";
	}

	/**
	 * reads the results from the network scan... the scan will find any new wifi devices in range
	 * @param w
	 * @return
	 */
	private List<NetInfo> readScanResults(WifiManager w) {
		List<NetInfo> rc = new ArrayList<NetInfo>();
		List<ScanResult> rc1 = w.getScanResults();
		List<WifiConfiguration> rc2 = w.getConfiguredNetworks();

		connectedSSID = w.getConnectionInfo().getSSID();

		if (rc1 == null) {
			// No scan results - just copy configured networks to returned value
			for (WifiConfiguration wc : rc2)
				rc.add(new NetInfo(wc.SSID, wc.networkId, false, true));
			Collections.sort(rc, new NetInfoComparator());
			return rc;
		}

		for (ScanResult s : rc1) {
			boolean alreadyHere = false;
			for (NetInfo s1 : rc)
				if (s1.SSID.equals(s.SSID)) {
					alreadyHere = true;
					s1.level = s.level;
					break;
				}
			if (!alreadyHere) {
				boolean in = false;
				for (WifiConfiguration wc : rc2) {
					String ssid = wc.SSID;
					if (ssid.startsWith("\"") && ssid.endsWith("\""))
						ssid = ssid.substring(1, ssid.length() - 1);
					if (ssid.equals(s.SSID)) {
						rc.add(new NetInfo(ssid, s.capabilities, wc.networkId,
								true, true));
						rc.get(rc.size() - 1).level = s.level;
						in = true;
						break;
					}
				}
				if (!in)
					// In range but not configured
					rc.add(new NetInfo(s.SSID, s.capabilities, true, false));
				rc.get(rc.size() - 1).level = s.level;
			}
		}

		// Add configured but not active networks
		for (WifiConfiguration wc : rc2) {
			String ssid = wc.SSID;
			if (ssid.startsWith("\"") && ssid.endsWith("\""))
				ssid = ssid.substring(1, ssid.length() - 1);
			boolean alreadyHere = false;
			for (NetInfo s : rc)
				if (s.SSID.equals(ssid)) {
					alreadyHere = true;
					break;
				}
			if (!alreadyHere)
				rc.add(new NetInfo(ssid, false, true));
		}
		Collections.sort(rc, new NetInfoComparator());
		return rc;
	}

	/**
	 * update the wifi button and the list view with the wifi information
	 */
	private void updateWiFi() {
		wifiOnOffButton.setEnabled(true);
		if (wifiOn) {
			// "Turn WiFi off"
			wifiOnOffButton.setText(getResources().getString(
					R.string.wifi_settings_turning_wifi_off));
			wifiOnOffButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(R.drawable.wifi_off), getResources()
					.getDrawable(R.drawable.wifi), getResources()
					.getDrawable(R.drawable.wifi_off), null);
			wifiOnOffButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					wfm.setWifiEnabled(false);
					// "Turning WiFi off"
					wifiOnOffButton.setText(getResources().getString(
							R.string.wifi_settings_turning_wifi_off));
					wifiOnOffButton.setEnabled(false);
				}
			});
			wifiNetworks = readScanResults(wfm);
			adapter.notifyDataSetChanged();
			wifiScanButton.setEnabled(true);
		} else {
			// "Turn WiFi on"
			wifiOnOffButton.setText(getResources().getString(
					R.string.wifi_settings_turning_wifi_on));
			wifiOnOffButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(R.drawable.wifi_on), getResources()
					.getDrawable(R.drawable.wifi), getResources()
					.getDrawable(R.drawable.wifi_on), null);
			
			wifiOnOffButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					wfm.setWifiEnabled(true);
					// "Turning WiFi on"
					wifiOnOffButton.setText(getResources().getString(
							R.string.wifi_settings_turning_wifi_on));
					wifiOnOffButton.setEnabled(false);
				}
			});
			wifiNetworks.clear();
			adapter.notifyDataSetChanged();
			wifiScanButton.setEnabled(false);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MainActivity.setFullScreen(this);
		setContentView(R.layout.wifisettings_layout);

		// initialize wifi
		// Wifi
		wfm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiOn = wfm.isWifiEnabled();
		wifiNetworks = readScanResults(wfm);

		((TextView) findViewById(R.id.wifisettings_title)).setText(getResources()
				.getString(R.string.wifi_settings_title));
		// finish this activity when the user clicks the back button at the top
		((ImageButton) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		// Wifi info
		wifiListView = (ListView) findViewById(R.id.wifi_settings_list_view);
		adapter = new WiFiAdapter(this);
		wifiListView.setAdapter(adapter);
		wifiListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			public void onScrollStateChanged(AbsListView view,
					int scrollState) {
			}
		});

		wifiScanButton = (Button) findViewById(R.id.wifi_scan_btn);
		wifiScanButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				wfm.startScan();
				wifiScanButton.setEnabled(false);
			}
		});

		wifiOnOffButton = (Button) findViewById(R.id.wifi_onoff_btn);
		updateWiFi();

		setupOtherButtons();
	}

	/**
	 * listen for button clicks here, like power, lock and reboot
	 */
	private void setupOtherButtons() {
		// launch the stock wifi settings for nook only
		((Button) findViewById(R.id.wifi_setup_btn)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_MAIN, null);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					// NOOK ST only!
					final ComponentName cn = new ComponentName(
							"com.android.settings",
							"com.android.settings.wifi.Settings_Wifi_Settings");
					intent.setComponent(cn);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
			}
		});

		// Lock button
		final Activity parent = this;
		Button lockBtn = (Button) findViewById(R.id.lock_btn);
		lockBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (PowerFunctions.actionLock(parent)) {
					parent.finish();
				}
			}
		});

		// Reboot button
		((Button) findViewById(R.id.reboot_btn)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PowerFunctions.actionReboot(parent);
			}
		});

		// Power Off button
		((Button) findViewById(R.id.poweroff_btn)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PowerFunctions.actionPowerOff(parent);
			}
		});		
	}

	/**
	 * Start listening for events like wifi and scan results
	 */
	private void registerForEvents()
	{
		// Listen for WiFi status changed
		IntentFilter wifiFilter = new IntentFilter();
		wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		wifiFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		wifiFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		wifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		registerReceiver(WiFiChangeReceiver, wifiFilter);
		
		// scan results are available
		IntentFilter scanFilter = new IntentFilter();
		scanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(ScanResultsReceiver, scanFilter);
	}
	
	
	/**
	 * Start listening for any events
	 */
	private void unregisterForEvents()
	{
		unregisterReceiver(WiFiChangeReceiver);
		unregisterReceiver(ScanResultsReceiver);
	}
	
	/**
	 * Used to list for wifi changes
	 */
	private BroadcastReceiver WiFiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			wifiOn = wfm.isWifiEnabled();
			wifiNetworks = readScanResults(wfm);
			wifiScanButton.setEnabled(true);
			adapter.notifyDataSetChanged();
			updateWiFi();
		}
	};
	
	/**
	 * Listen for when the scan button is complete
	 */
	private BroadcastReceiver ScanResultsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			wifiNetworks = readScanResults(wfm);
			wifiScanButton.setEnabled(true);
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterForEvents();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerForEvents();
	}
}