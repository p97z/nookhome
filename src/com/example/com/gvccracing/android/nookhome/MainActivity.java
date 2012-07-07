package com.example.com.gvccracing.android.nookhome;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author p97z
 *
 */
public class MainActivity extends Activity {

	private TextView wifiText;
	private TextView batteryText;
	// get the battery change events
	private boolean batteryLevelRegistered = false;	
	private boolean powerReceiverRegistered = false;
	private boolean wifiReceiverRegistered = false;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // full screen application hard coded
        setFullScreen(this);

        setContentView(R.layout.activity_main);
        
//        boolean homeKey;
        // were we called from our home button?
//        final Intent data = getIntent();
 //       if (data.getExtras() == null) {
  //      	homeKey = false;
//		} else {
	//		homeKey = data.getBooleanExtra("home", false);
//		}
        
        setupAllAppsButton();
        // set the text on the button
		setupBatteryButton();
	
		registerForEvents();
		
		drawButtons();
    }

	@Override
	public void onDestroy() {

		unregisterForEvents();
		super.onDestroy();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /*
     * Registers for intents like, power, wifi and battery level
     */
    private void registerForEvents()
    {
		if (!powerReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_POWER_CONNECTED);
			filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			registerReceiver(this.PowerChangeReceiver, new IntentFilter(filter));
			powerReceiverRegistered = true;
		}

		if (!wifiReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(this.WiFiChangeReceiver, new IntentFilter(filter));
			wifiReceiverRegistered = true;
		}
		
		if (!batteryLevelRegistered) {
			IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			registerReceiver(this.BatteryChangeReceiver, new IntentFilter(batteryLevelFilter));
			batteryLevelRegistered = true;
		}
    }
    
    /*
     * unregister for power, wifi and battery intents
     */
    private void unregisterForEvents()
    {
    	if (powerReceiverRegistered)
    	{
    		unregisterReceiver(this.PowerChangeReceiver);
    		powerReceiverRegistered = false;
    	}
    	if (wifiReceiverRegistered)
    	{
        	unregisterReceiver(this.WiFiChangeReceiver);
        	wifiReceiverRegistered = false;    		
    	}
		if (batteryLevelRegistered)
		{
			unregisterReceiver(this.BatteryChangeReceiver);
			batteryLevelRegistered = false;
		}
    }
    
	/* 
	 * sets up the battery layout as a button... 
	 */
    private void setupBatteryButton()
    {
    	final LinearLayout batteryLayout = (LinearLayout) findViewById(R.id.bat_layout);
		if (batteryLayout != null)
		{
			class BatterySimpleOnGestureListener extends SimpleOnGestureListener 
			{
				/* 
				 * The user touched the battery button so show the wifi dialog
				 */
				@Override public boolean onSingleTapConfirmed(MotionEvent e)
				{
					// show the wifisettings activity
					Intent i = new Intent(MainActivity.this, WiFiSettings.class);
					startActivity(i);
					return true;
				}
				
				/* 
				 * ignore double click
				 */		
				@Override public boolean onDoubleTap(MotionEvent e) {	return true; }
		
				/* 
				 * Long press ask the user if they want to toggle
				 */
				@Override public void onLongPress(MotionEvent e) 
				{
					AskToggleWiFi();
				}
			}

			final GestureDetector batteryGestureDetector = new GestureDetector(new BatterySimpleOnGestureListener());
			batteryLayout.setOnTouchListener(new View.OnTouchListener() 
			{
				public boolean onTouch(View v, MotionEvent event) 
				{
					batteryGestureDetector.onTouchEvent(event);
					return false;
				}
			});
		}
		
		batteryText = (TextView) findViewById(R.id.bat_level);
		wifiText = (TextView) findViewById(R.id.bat_title);		
    }
    
    
    /**
     * Ask the user if they want to toggle the WiFi state
     */
    private void AskToggleWiFi()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(
				R.string.main_wifi_toggle_title));
		builder.setMessage(getResources().getString(
				R.string.main_wifi_toggle_message));
		
		builder.setPositiveButton(
				getResources().getString(R.string.main_yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,	int which) {
						toggleWiFi();
					}
				});
		// "NO"
		builder.setNegativeButton(
				getResources().getString(R.string.main_no),
				null);

		builder.show();

    }
    
	/**
	 * Turns wifi on or off
	 */
	private void toggleWiFi() 
	{
		WifiManager wifiManager;
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			// "WiFi is Off"
			Toast.makeText(
					MainActivity.this,
					getResources().getString(
							R.string.main_wifi_off),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(false);
		} else {
			// "WiFi is ON"
			Toast.makeText(
					MainActivity.this,
					getResources().getString(
							R.string.main_wifi_on),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(true);
		}
		//drawButtons();
	}
    
    private void setupAllAppsButton()
    {
		final ImageButton allAppsButton = ((ImageButton) findViewById(R.id.all_apps_btn));
		// create a class to listen to button clicks
		class AllAppsSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				/*
				Intent intent = new Intent(MainActivity.this,
						AllApplications.class);
				intent.putExtra("list", "app_all");
				// "All applications"
				intent.putExtra(
						"title",
						getResources().getString(
								R.string.jv_all_a));
				startActivity(intent);*/
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (allAppsButton.hasWindowFocus()) {

				}
			}
		};
		// listen to the gestures on the all apps button
		AllAppsSimpleOnGestureListener allappsGestListener = new AllAppsSimpleOnGestureListener();
		final GestureDetector allAppsGestureDetector = new GestureDetector(allappsGestListener);
		allAppsButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				allAppsGestureDetector.onTouchEvent(event);
				return false;
			}
		});
    }
    
    
	/**
	 * @param a main Activity class
	 */
	public static void setFullScreen(Activity a) 
	{
		if ( a.requestWindowFeature(Window.FEATURE_NO_TITLE) )
		{
			a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
	
	/*
	 * draws the button text
	 */
	private void drawButtons()
	{
		// Wifi status
		WifiManager wfm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiText != null) 
		{
			if (wfm.isWifiEnabled()) 
			{
				String nowConnected = wfm.getConnectionInfo().getSSID();
				if (nowConnected != null && !nowConnected.equals("")) {
					wifiText.setText(nowConnected);
				} else {
					wifiText.setText(getResources().getString(
							R.string.main_wifi_on));
				}
				wifiText.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.wifi_on), null,
						null, null);
			} else {
				// "WiFi is off"
				wifiText.setText(getResources().getString(
						R.string.main_wifi_off));
				wifiText.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.wifi_off), null,
						null, null);
			}
		}
	}

	/*
	 * puts the battery level text inside the battery button
	 */
	private void drawBatteryLevel(Context context, Intent intent)
	{
		int rawlevel = intent.getIntExtra(
				BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(
				BatteryManager.EXTRA_SCALE, -1);
		int plugged = intent.getIntExtra(
				BatteryManager.EXTRA_PLUGGED, -1);
		int level = -1;
		if (rawlevel >= 0 && scale > 0) {
			level = (rawlevel * 100) / scale;
		}
		
		if (batteryText != null) 
		{
			String add_text = "";
			if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
				add_text = " AC";
			} else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
				add_text = " USB";
			}
			batteryText.setText(level + "%" + add_text);

			if (level < 25)
				batteryText
						.setCompoundDrawablesWithIntrinsicBounds(
								getResources().getDrawable(
										R.drawable.bat1), null,
								null, null);
			else if (level < 50)
				batteryText
						.setCompoundDrawablesWithIntrinsicBounds(
								getResources().getDrawable(
										R.drawable.bat2), null,
								null, null);
			else if (level < 75)
				batteryText
						.setCompoundDrawablesWithIntrinsicBounds(
								getResources().getDrawable(
										R.drawable.bat3), null,
								null, null);
			else
				batteryText
						.setCompoundDrawablesWithIntrinsicBounds(
								getResources().getDrawable(
										R.drawable.bat4), null,
								null, null);
		}
	}
	
	private BroadcastReceiver PowerChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			drawButtons();
		}
	};

	private BroadcastReceiver WiFiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			drawButtons();
		}
	};

	private BroadcastReceiver BatteryChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			drawBatteryLevel(context,intent);			
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		drawButtons();
	}
}
