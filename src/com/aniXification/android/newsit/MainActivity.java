package com.aniXification.android.newsit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private Uri mImageCaptureUri;
	private ImageView mImageView;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	int category = 0;
	String[] s = { "Cat0", "Cat1", "Cat2", "Cat3", "Cat4", "Cat5" };

	InputStream is;
	String path = ""; // get this from image location!!!!
	String filename = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.cancle)).setOnClickListener(this);
		((Button) findViewById(R.id.send)).setOnClickListener(this);
		((ImageView) findViewById(R.id.iv_photo)).setOnClickListener(this);

		final String[] items = new String[] { "Take from camera", "Select from gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		List<String> planets = Arrays.asList(s);
		ArrayAdapter<String> adapterCat = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);

		adapterCat = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, planets);
		adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.category);
		spinner.setAdapter(adapterCat);
		spinner.setSelection(category);

		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) { // pick from
																	// camera
				if (item == 0) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "Img4_News_"
							+ String.valueOf(System.currentTimeMillis()) + ".jpg"));

					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

					try {
						intent.putExtra("return-data", true);

						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} else { // pick from file
					Intent intent = new Intent();

					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);

					startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
				}
			}
		});

		final AlertDialog dialog = builder.create();

		Button button = (Button) findViewById(R.id.btn_crop);
		mImageView = (ImageView) findViewById(R.id.iv_photo);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case PICK_FROM_CAMERA:
			doCrop();

			break;

		case PICK_FROM_FILE:
			mImageCaptureUri = data.getData();

			doCrop();

			break;

		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();

			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				mImageView.setImageBitmap(photo);
			}

			File f = new File(mImageCaptureUri.getPath());

			if (f.exists()) {
				f.delete();

				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				try {
					FileOutputStream out = new FileOutputStream(f);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			break;

		}
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

			return;
		} else {
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 150);
			intent.putExtra("outputY", 150);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
					}
				});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null, null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.cancle) {
			Toast.makeText(getBaseContext(), "Cancle This Post", Toast.LENGTH_LONG).show();
			finish();

		} else if (v.getId() == R.id.send) {
			Log.d("Image Path : ", mImageCaptureUri.getPath());
			uploadImage(mImageCaptureUri);

			// externalStorage.getAbsolutePath()
			// uploader.uploadImage("/sdcard/aaa", "flashCropped.png");
			// Log.d("Image Path : ", mImageCaptureUri.getPath());
			Toast.makeText(getBaseContext(), "Send This Post", Toast.LENGTH_LONG).show();
		} else if (v.getId() == R.id.iv_photo) {
			Intent i = new Intent(this, Preview.class);
			Log.d("URI", mImageCaptureUri.getPath());
			i.putExtra("path", mImageCaptureUri.getPath());
			startActivity(i);
		}

	}

	// upload Image
	public void uploadImage(Uri uri) {

		WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Sending Article...");
		Bitmap bitmapOrg = BitmapFactory.decodeFile(uri.getPath());

		// Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),
		// R.drawable.jpeg);

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90, bao);
		byte[] ba = bao.toByteArray();
		String ba1 = com.aniXification.android.newsit.Base64.encodeBytes(ba);

		wst.addNameValuePair("head", ((EditText) findViewById(R.id.postHead)).getText().toString());
		wst.addNameValuePair("body", ((EditText) findViewById(R.id.postBody)).getText().toString());
		wst.addNameValuePair("image", ba1);
		wst.addNameValuePair("category", ((Spinner) findViewById(R.id.category)).getSelectedItemPosition() + "");
		wst.addNameValuePair("userid", "99999");

		// the passed String is the URL we will POST to
		wst.execute(new String[] { Constants.URL_UOLOAD_IMAGE });
	}

	// handle the account info response : success or failure
	public void handleResponse(String response) {
		try {
			if (response != null) {
				// Intent homeIntent = new Intent();
				// // homeIntent.putExtra("successMessage", "Welcome home.");
				// homeIntent.setClass(MainActivity.this, HomeActivity.class);
				//
				System.out.println("uploaded!!" + response);
				// startActivity(homeIntent);
			} else {
				System.out.println("failed.");
			}

		} catch (Exception e) {
			System.out.println("Error:" + e.toString());
		}

	}

	public void createAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Successfull!");

		// set dialog message
		alertDialogBuilder.setMessage("Send Next Article?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						MainActivity.this.finish();
						Intent i = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(i);
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;

		private static final String TAG = "Starterctivity";
		private static final int CONN_TIMEOUT = 10000;
		private static final int SOCKET_TIMEOUT = 10000;

		private int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Uploading ...";

		private final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		private ProgressDialog pDlg = null;

		public WebServiceTask(int taskType, Context mContext, String processMessage) {

			this.taskType = taskType;
			this.mContext = mContext;
			this.processMessage = processMessage;
		}

		public void addNameValuePair(String name, String value) {

			params.add(new BasicNameValuePair(name, value));
		}

		@Override
		protected String doInBackground(String... urls) {

			String url = urls[0];
			String result = "";

			HttpResponse response = doResponse(url);

			// json aayo
			if (response == null) {
				System.out.println("do in backgroud response is null");
				return result;
			} else {
				try {
					// null pointer exception throwing in here for invalid user
					result = inputStreamToString(response.getEntity().getContent());
					System.out.println("the RESULT in doInBackground thread is : " + result);

					StatusLine statusLine = response.getStatusLine();
					if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
						System.out.println("status code ok. doInBackground!!");
					} else {
						System.out.println("status code not OK!!");
						return null;
					}

				} catch (IllegalStateException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}

			}

			return result;
		}

		private HttpResponse doResponse(String url) {

			// Use our connection and data timeouts as parameters for our
			// DefaultHttpClient
			HttpClient httpclient = new DefaultHttpClient(getHttpParams());

			HttpResponse response = null;

			try {
				switch (taskType) {

				case POST_TASK:
					HttpPost httppost = new HttpPost(url);
					// Add parameters
					httppost.setEntity(new UrlEncodedFormEntity(params));
					response = httpclient.execute(httppost);

					break;

				case GET_TASK:
					HttpGet httpget = new HttpGet(url);
					response = httpclient.execute(httpget);
					break;
				}
			} catch (Exception e) {

				Log.e(TAG, e.getLocalizedMessage(), e);

			}

			return response;
		}

		private String inputStreamToString(InputStream is) {

			String line = "";
			StringBuilder total = new StringBuilder();

			// Wrap a BufferedReader around the InputStream
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			try {
				// Read response until the end
				while ((line = rd.readLine()) != null) {
					total.append(line);
				}
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}

			// Return full string
			return total.toString();
		}

		// hide keyboard while Loading the response
		/*
		 * private void hideKeyboard() { InputMethodManager inputManager =
		 * (InputMethodManager)
		 * ImageUploader.this.getSystemService(Context.INPUT_METHOD_SERVICE);
		 * inputManager
		 * .hideSoftInputFromWindow(ImageUploader.this.getCurrentFocus
		 * ().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS); }
		 */

		@SuppressWarnings("deprecation")
		private void showProgressDialog() {

			pDlg = new ProgressDialog(mContext);
			pDlg.setMessage(processMessage);
			pDlg.setProgressDrawable(mContext.getWallpaper());
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.show();
		}

		@Override
		protected void onPreExecute() {
			// hideKeyboard();
			showProgressDialog();
		}

		@Override
		protected void onPostExecute(String response) {

			handleResponse(response);
			pDlg.dismiss();
			createAlert();
		}

		// Establish connection and socket (data retrieval) timeouts
		private HttpParams getHttpParams() {
			HttpParams htpp = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
			HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

			return htpp;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.logout:
			finish();
			SharedPreferences prefNLS = getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE);
			((prefNLS.edit()).putString(Constants.PREF_USER_ID, "NULL")).commit();
			return true;

		}
		return false;
	}

}