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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserProfileEdit extends Activity implements OnClickListener{

	private EditText et_email, et_password, et_name, et_mobile;
	private Button btn_editProfile;
	private String email, password, mobile, name;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);
        
        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.etPassword);
        et_name = (EditText) findViewById(R.id.et_name);
        et_mobile = (EditText) findViewById(R.id.etMobile);
        
        btn_editProfile = (Button) findViewById(R.id.btn_editProfile);
        btn_editProfile.setOnClickListener(this);
        
        
    }
    
 // handle the account info response : success or failure
  		public void handleResponse(String response) {
  			try {
  				 				
  				if(response!= null){
  					Toast.makeText(this, "Profile updated Successfully.", Toast.LENGTH_LONG).show();
  					System.out.println("update successful!!");
  					
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
 		private String processMessage = "Logging in...";

 		private final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

 		private ProgressDialog pDlg = null;

 		public WebServiceTask(int taskType, Context mContext,String processMessage) {

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

 			//json aayo
 			if (response == null) {
 				System.out.println("do in backgroud response is null");
 				return result;
 			} else {
 				try {
 					//null pointer exception throwing in here for invalid user
 					result = inputStreamToString(response.getEntity().getContent());
 					System.out.println("the RESULT in doInBackground thread is : " + result);
 					
 					StatusLine statusLine = response.getStatusLine();
 					if(statusLine.getStatusCode() == HttpStatus.SC_OK){
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
 			InputMethodManager inputManager = (InputMethodManager) UserProfileEdit.this.getSystemService(Context.INPUT_METHOD_SERVICE);
 			inputManager.hideSoftInputFromWindow(UserProfileEdit.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
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

     @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_editProfile:
			
			System.out.println("button clicked!!");
			
			String id = 2 + "";
			
			//fill the fields from the shared preferences!!!!
			name = et_name.getText().toString();
			email = et_email.getText().toString();
			password = et_password.getText().toString();
			mobile = et_mobile.getText().toString();
			
			Toast.makeText(this, "email:" + email + " Password:" + password + "email:: " + email + "moibile:: " + mobile, Toast.LENGTH_LONG).show();
			
			WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");
			
			wst.addNameValuePair("id", id);
			wst.addNameValuePair("email", email);
			wst.addNameValuePair("password", password);
			wst.addNameValuePair("name", name);
			wst.addNameValuePair("mobile", mobile);

			System.out.println("URL to update:: " + Constants.URL_UPDATE_PROFILE);
			
			String newUrl = Constants.URL_UPDATE_PROFILE + "?id=" + id;
			
			// the passed String is the URL we will POST to
			wst.execute(new String[] { newUrl });
			
			
			
			break;

		default:
			break;
		}
		
	}


    
}
