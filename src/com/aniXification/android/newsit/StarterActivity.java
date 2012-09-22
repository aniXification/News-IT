package com.aniXification.android.newsit;

import java.io.BufferedReader;
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
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StarterActivity extends Activity implements OnClickListener {

	private EditText et_email, et_password;
	private Button btn_login;
	private String email, password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starter);

		et_email = (EditText) findViewById(R.id.etEmail);
		et_password = (EditText) findViewById(R.id.etPassword);

		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(this);

	}

	// handle the account info response : success or failure
	public void handleResponse(String response) {
		try {

			if (response != null) {

				Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_LONG);
				JSONObject jo = new JSONObject(response);
				System.out.println("login successful!! " + response);

				SharedPreferences prefNLS = getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE);

				((prefNLS.edit()).putString(Constants.PREF_USER_ID, (new JSONObject(response)).getString("ID"))).commit();
				((prefNLS.edit()).putString(Constants.PREF_USER_NAME, (new JSONObject(response)).getString("USERNAME"))).commit();

				Log.d("Respose ID ", prefNLS.getString(Constants.PREF_USER_ID, "NULL"));
				Log.d("Respose NAME ", prefNLS.getString(Constants.PREF_USER_NAME, "NULL"));
				finish();
				Intent i = new Intent(this, TabSchedule.class);
				startActivity(i);

			} else {
				System.out.println("failed.");
			}

		} catch (Exception e) {
			System.out.println("Error:" + e.toString());
		}

	}

	// LOGIN click event
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:

			email = et_email.getText().toString();
			password = et_password.getText().toString();

			if (email.length() == 0 || password.length() == 0) {
				Toast.makeText(this, "Email or Password field is empty.", Toast.LENGTH_LONG).show();
			} else {
				WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Loggin In ....");

				wst.addNameValuePair("email", email);
				wst.addNameValuePair("pwd", password);

				System.out.println("URL to login:: " + Constants.URL_LOGIN);

				// the passed String is the URL we will POST to
				wst.execute(new String[] { Constants.URL_LOGIN });
			}

			break;

		default:
			break;
		}

	}

	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;

		private static final String TAG = "Starterctivity";
		private static final int CONN_TIMEOUT = 3000;
		private static final int SOCKET_TIMEOUT = 5000;

		private int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Logging in...";

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
						System.out.println("status code ok");
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
		private void hideKeyboard() {
			InputMethodManager inputManager = (InputMethodManager) StarterActivity.this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(StarterActivity.this.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}

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
			hideKeyboard();
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
