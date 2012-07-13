package com.example.com.gvccracing.android.nookhome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class Home extends Activity {
	final String TAG = "Home";
	MainActivity app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//app = ((MainActivity) getApplicationContext());
		//app.RestartIntent = PendingIntent.getActivity(this, 0, getIntent(),
		//		getIntent().getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);

		Intent intent = new Intent(Home.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}
}