package com.aniXification.android.newsit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsDetailsActivity extends Activity implements OnClickListener {

	private WebView wvDescriptions;
	TextView tvUsername, tvPublishedDate, tvCategory, tvTitle, tvDescription;
	ImageView imageView;
	
	EditText etComment;
	Button btnLike, btnShare, btnComment;

	Bitmap bitmap;
	private static final String URL_POST_DETAILS = Constants.URL_POST_DETAILS_BY_ID;

	String id = "";
	String descriptions = "";
	String username = "";
	String publishedDate = "";
	String category = "";
	String title = "";

	private static String POST_ID = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_details);

		tvUsername = (TextView) findViewById(R.id.tvUsername);
		tvPublishedDate = (TextView) findViewById(R.id.tvPostedDate);
		tvCategory = (TextView) findViewById(R.id.tvPostCategory);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvDescription = (TextView) findViewById(R.id.tv_Descriptions);
		
		etComment = (EditText) findViewById(R.id.et_comment);
		btnLike = (Button) findViewById(R.id.btn_like);
		btnShare = (Button) findViewById(R.id.btn_share);
		btnComment = (Button) findViewById(R.id.btn_comment);
		
		btnComment.setOnClickListener(this);
		btnLike.setOnClickListener(this);
		btnShare.setOnClickListener(this);
		
		imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setOnClickListener(this);

		// Get the ID from the previous Activity
		Bundle bundle = getIntent().getExtras();
		id = bundle.get("postId").toString();

		POST_ID = id;

		System.out.println("the POSTID selected is ::" + POST_ID);

		retrievePostDetails();

	}

	// retrieve the login response: success or failure
	public void retrievePostDetails() {
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...");

		String newUrl = URL_POST_DETAILS + "?ID=" + POST_ID;
		System.out.println("the new URL to get POST details:" + newUrl);

		wst.execute(new String[] { newUrl });

	}

	// handle the account info response : success or failure
	public void handleResponse(String response) throws MalformedURLException, IOException {
		System.out.println("the response we get from json : " + response.toString());

		try {

			// JSONObject jObject = new JSONObject(response);

			JSONArray jArray = new JSONArray(response);

			System.out.println("json array::" + jArray.toString());

			for (int i = 0; i < jArray.length(); i++) {

				JSONObject jso = (JSONObject) jArray.get(i);

				username = "USERNAME STATIC";
				publishedDate = jso.getString("POSTED_DATE");
				category = jso.getString("POST_CATEGORY");
				title = jso.getString("title");
				descriptions = jso.getString("DESCRIPTIONS");
				String imageName = jso.getString("IMAGE_ID");

				System.out.println("username:: " + username + " publish Date:: " + publishedDate + " category:: " + category
						+ " title:: " + title + " Descriptions::" + descriptions);

				// now the set the data to the UI fields!
				tvUsername.setText("Username:: " + username);
				tvPublishedDate.setText("Posted Date:: " + publishedDate);
				tvCategory.setText("Category:: " + category);
				tvTitle.setText("Post Title:: " + title);
				tvDescription.setText("Post Descriptions:: " + descriptions);

				String imageUrl = Constants.URL_IMAGE + imageName;
				// now set the image to the image VIEW
				// Bitmap bmp= BitmapFactory.decodeFile(imageUrl);

				bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
				imageView.setImageBitmap(bitmap);

				// imageView.setImageDrawable(drawable_from_url(Constants.URL_IMAGE,
				// imageName));
				// imageView.setImageDrawable(getResources().getDrawable(R.drawable.icon));

			}

		} catch (JSONException e) {
			System.out.println("The board details could not be fetched!");
			e.printStackTrace();
		}

		// System.out.println("THe success/failure response from server:: " +
		// response);
	}

	Drawable drawable_from_url(String url, String src_name) throws java.net.MalformedURLException, java.io.IOException {
		return Drawable.createFromStream(((java.io.InputStream) new java.net.URL(url).getContent()), src_name);
	}

	// retrieve the login response: success or failure
	public void retrieveSampleData() {
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...");
		wst.execute(new String[] { URL_POST_DETAILS });

	}

	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;

		private static final String TAG = "LoginActivity";
		private static final int CONN_TIMEOUT = 10000;
		private static final int SOCKET_TIMEOUT = 10000;

		private int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Validating...";

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

			if (response == null) {
				System.out.println("do in backgroud response is null");
				return result;
			} else {
				try {
					// null pointer exception throwing in here for invalid user
					result = inputStreamToString(response.getEntity().getContent());
					System.out.println("the RESULT in doInBackground thread is : " + result);
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
			try {
				handleResponse(response);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	case R.id.btn_comment:
		System.out.println("comment clicked");
		Toast.makeText(this, "commented", Toast.LENGTH_LONG).show();
		break;
		
	case R.id.btn_like:
		System.out.println("like clicked");
		Toast.makeText(this, "liked", Toast.LENGTH_LONG).show();
		break;
		
	case R.id.btn_share:
		
		Toast.makeText(this, "share", Toast.LENGTH_LONG).show();
		//create the send intent
		Intent shareIntent =  new Intent(android.content.Intent.ACTION_SEND);

		//set the type
		shareIntent.setType("text/plain");

		//add a subject
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,  "Title:" + title);

		//build the body of the message to be shared
		String shareMessage = "Descriptions:" +descriptions;

		//add the message
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);

		//start the chooser for sharing
		startActivity(Intent.createChooser(shareIntent, "Insert share chooser title here"));
		break;

	default:
		break;
	}
		

	}

}
