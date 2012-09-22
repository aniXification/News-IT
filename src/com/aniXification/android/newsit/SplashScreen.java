package com.aniXification.android.newsit;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SplashScreen extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		final int splashTime = 1000;

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				finish();
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, splashTime);

		SharedPreferences prefNLS = getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE);
		Log.d(Main.class.getName(), prefNLS.getString(Constants.PREF_USER_ID, "NA"));

		Editor prefNLSEditor = prefNLS.edit();

		if (prefNLS.getString(Constants.PREF_USER_ID, "NULL").equalsIgnoreCase("NULL")) {
			Intent i = new Intent(this, StarterActivity.class);
			startActivity(i);
		} else {
			Toast.makeText(
					this,
					"Wel Come : "
							+ getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE).getString(Constants.PREF_USER_NAME,
									"NULL"), Toast.LENGTH_LONG).show();

			Intent i = new Intent(this, TabSchedule.class);
			startActivity(i);
		}

	}
}
