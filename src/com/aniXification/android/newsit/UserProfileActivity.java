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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UserProfileActivity extends Activity implements OnClickListener {

	Button tv_postCount, tv_PostLikeCount, tv_postSpamCount;
	Button btn_ChangeProfile, tv_editProfile, btn_submit;
	private static final String URL_COUNT_ALL = Constants.URL_COUNT_ALL_BY_ID;
	EditText name, pass, email, mobile;

	int USER_ID = 1; // this should not be static!! :D get from user
						// preference!!!

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		tv_postCount = (Button) findViewById(R.id.tvPostsCount);
		tv_PostLikeCount = (Button) findViewById(R.id.tvPostLikesCount);
		tv_postSpamCount = (Button) findViewById(R.id.tvPostSpamsCount);
		tv_editProfile = (Button) findViewById(R.id.btn_editProfile);
		tv_editProfile.setOnClickListener(this);

		btn_ChangeProfile = (Button) findViewById(R.id.btn_editProfile);
		btn_ChangeProfile.setOnClickListener(this);

		btn_submit = (Button) findViewById(R.id.btnSubmit);
		btn_submit.setOnClickListener(this);

		name = (EditText) findViewById(R.id.etname);
		name.setEnabled(false);
		pass = (EditText) findViewById(R.id.etpassword);
		pass.setEnabled(false);
		email = (EditText) findViewById(R.id.etemail);
		email.setEnabled(false);
		mobile = (EditText) findViewById(R.id.etmobile);
		mobile.setEnabled(false);

		retriveUserProfile();

	}

	// retrieve the login response: success or failure
	public void retriveUserProfile() {
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "Loading data...");

		String newUrl = URL_COUNT_ALL + "?id="
				+ (getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE)).getString(Constants.PREF_USER_ID, "NULL");
		System.out.println("the new URL to get POST details:"
				+ (getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE)).getString(Constants.PREF_USER_ID, "NULL"));

		wst.execute(new String[] { newUrl });

	}

	// handle the account info response : success or failure
	public void handleResponse(String response) {
		// System.out.println("the response we get from json : " +
		// response.toString());

		if (response != null & response.equalsIgnoreCase("UPDATE OK")) {
			System.out.println("the response we get from json Update completed: " + response.toString());
		} else {
			System.out.println("the response we get from json Update completed: " + response.toString());
			try {
				JSONObject jso = new JSONObject(response);

				tv_PostLikeCount.setText("  " + jso.getString("LIKES"));
				tv_postCount.setText("  " + jso.getString("POSTS"));
				tv_postSpamCount.setText("  " + jso.getString("SPAM"));
				name.setText(jso.getString("NAME"));
				mobile.setText(jso.getString("MOBILE"));
				pass.setText(jso.getString("PASSWORD"));
				email.setText(jso.getString("EMAIL"));

			} catch (JSONException e) {
				System.out.println("The board details could not be fetched!");
				e.printStackTrace();
			}
		}

		// System.out.println("THe success/failure response from server:: " +
		// response);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSubmit:
			// fill the fields from the shared preferences!!!!

			System.out.println("submit button clicked!!");

			WebServiceTask wst1 = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");

			wst1.addNameValuePair("id",
					(getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE)).getString(Constants.PREF_USER_ID, "NULL"));
			wst1.addNameValuePair("email", email.getText().toString());
			wst1.addNameValuePair("password", pass.getText().toString());
			wst1.addNameValuePair("name", name.getText().toString());
			wst1.addNameValuePair("mobile", mobile.getText().toString());

			System.out.println("email:: " + email.getText().toString() + " password::" + pass.getText().toString() + " name "
					+ name.getText().toString() + " mobile::" + mobile.getText().toString());

			System.out.println("URL to update:: " + Constants.URL_UPDATE_PROFILE);

			String newUrl = Constants.URL_UPDATE_PROFILE + "?ID="
					+ (getSharedPreferences(Constants.PREF_INFO, Context.MODE_PRIVATE)).getString(Constants.PREF_USER_ID, "NULL");

			// the passed String is the URL we will POST to
			Log.d(UserProfileActivity.class.getName(), newUrl);
			wst1.execute(new String[] { newUrl });

		case R.id.btn_editProfile:
			name.setEnabled(true);
			pass.setEnabled(true);
			email.setEnabled(true);
			mobile.setEnabled(true);
			btn_submit.setEnabled(true);

		default:
			break;
		}

	}

	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;

		private static final String TAG = "LoginActivity";
		private static final int CONN_TIMEOUT = 10000;
		private static final int SOCKET_TIMEOUT = 10000;

		public int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Validating...";

		private final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		private ProgressDialog pDlg = null;

		public WebServiceTask(int taskType, Context mContext, String processMessage) {

			this.taskType = taskType;
			this.mContext = mContext;
			this.processMessage = processMessage;
			Log.d(UserProfileActivity.class.getName(), taskType + "");
		}

		public void addNameValuePair(String name, String value) {
			params.add(new BasicNameValuePair(name, value));
		}

		@SuppressWarnings("unused")
		@Override
		protected String doInBackground(String... urls) {

			String url = urls[0];
			String result = "";

			HttpResponse response = doResponse(url);

			System.out.println("the response from the POST::" + response.toString());

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
			HttpClient httpclient = new DefaultHttpClient(getHttpParams());
			HttpResponse response = null;

			try {
				switch (taskType) {

				case POST_TASK:
					Log.d(UserProfileActivity.class.getName(), "POST TASK");
					HttpPost httppost = new HttpPost(url);
					// Add parameters
					httppost.setEntity(new UrlEncodedFormEntity(params));
					response = httpclient.execute(httppost);

					System.out.println("the response in doResponse::" + response);

					break;

				case GET_TASK:
					Log.d(UserProfileActivity.class.getName(), "GET TASK");
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
