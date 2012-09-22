package com.aniXification.android.newsit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class ImageUploader {

	InputStream is;
	String path = ""; // get this from image location!!!!
	String filename = "";
	Context context;

	public ImageUploader(Context ctx) {
		context = ctx;
	}

	public void uploadImage(Uri uri) {

		WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, context, "Uploading image...");

		// Bitmap bitmapOrg = BitmapFactory.decodeFile(uri.getPath());

		Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmapOrg.compress(Bitmap.CompressFormat.PNG, 90, bao);
		byte[] ba = bao.toByteArray();
		String ba1 = Base64.encodeBytes(ba);

		wst.addNameValuePair("image", ba1);

		System.out.println("URL to upload image:: " + Constants.URL_UOLOAD_IMAGE);

		// the passed String is the URL we will POST to
		wst.execute(new String[] { Constants.URL_UOLOAD_IMAGE });
	}

	private Resources getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	// handle the account info response : success or failure
	public void handleResponse(String response) {
		try {
			if (response != null) {
				// Intent homeIntent = new Intent();
				// // homeIntent.putExtra("successMessage", "Welcome home.");
				// homeIntent.setClass(ImageUploader.this, HomeActivity.class);
				//
				System.out.println("uploaded!!");
				// startActivity(homeIntent);

			} else {
				System.out.println("failed.");
			}

		} catch (Exception e) {
			System.out.println("Error:" + e.toString());
		}

	}

	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;

		private static final String TAG = "Starterctivity";
		private static final int CONN_TIMEOUT = 10000;
		private static final int SOCKET_TIMEOUT = 10000;

		private int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Uploading in...";

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
		}

		// Establish connection and socket (data retrieval) timeouts
		private HttpParams getHttpParams() {
			HttpParams htpp = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
			HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

			return htpp;
		}

	}

}
