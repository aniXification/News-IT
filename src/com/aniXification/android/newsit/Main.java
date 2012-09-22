package com.aniXification.android.newsit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Main extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainpage);

		Intent mainIntent = new Intent().setClass(Main.this, SplashScreen.class);
		startActivity(mainIntent);
	}

}
