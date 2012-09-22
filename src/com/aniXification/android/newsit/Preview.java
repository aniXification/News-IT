package com.aniXification.android.newsit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class Preview extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview_img);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String filename = extras.getString("path");
		FileInputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = new FileInputStream(new File(filename));
			bis = new BufferedInputStream(is);
			Bitmap bitmap = BitmapFactory.decodeStream(bis);
			ImageView image = (ImageView) findViewById(R.id.preview);
			image.setImageBitmap(bitmap);
		} catch (Exception e) {
			// Try to recover
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}
	}

}
