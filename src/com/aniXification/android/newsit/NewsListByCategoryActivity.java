package com.aniXification.android.newsit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class NewsListByCategoryActivity extends ListActivity {

	// Flag for current page
	//int current_page = 1;
	int catId = 1;

	//Button btnLoadMore;
	private final String url = Constants.URL_POST_LIST_BY_ID;

	ArrayList<HashMap<String, String>> myList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listplaceholder);

		myList = new ArrayList<HashMap<String, String>>();

		// LoadMore button
		//btnLoadMore = new Button(this);
		//btnLoadMore.setText("Load More");

		retrieveNewsPosts();
	}
	
	// retrieve News Post lists
	public void retrieveNewsPosts() {
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...");

		String new_url = url + "?cat=" + catId;

		System.out.println("the URL to call::" + new_url);

		wst.execute(new String[] { new_url });

	}

	private class WebServiceTask extends AsyncTask<String, Integer, ArrayList<HashMap<String, String>>> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;
		private static final String TAG = "ListActivity";
		private static final int CONN_TIMEOUT = 10000;
		private static final int SOCKET_TIMEOUT = 10000;

		private int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Validating...";
		private ProgressDialog pDlg = null;

		public WebServiceTask(int taskType, Context mContext, String processMessage) {
			this.taskType = taskType;
			this.mContext = mContext;
			this.processMessage = processMessage;
		}

		private final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		// myList = new ArrayList<HashMap<String, String>>();

		public void addNameValuePair(String title, String value) {
			params.add(new BasicNameValuePair(title, value));
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... urls) {

			String url = urls[0];
			String result = "";

			HttpResponse response = doResponse(url);
			if (response == null) {
				System.out.println("do in backgroud response is null");
			} else {
				try {
					result = inputStreamToString(response.getEntity().getContent());
					System.out.println("the RESULT in doInBackground thread is : " + result);

					if (result == null) {
						// hide the button
						// btnLoadMore.setEnabled(false);

					} else {

						try {

							JSONArray json = new JSONArray(result);

							for (int i = 0; i < json.length(); i++) {
								JSONObject c = json.getJSONObject(i);

								// JSON DATA...

								String id = c.getString("ID");
								String title = c.getString("title");
								String imageName = c.getString("IMAGE_ID");
								
								String imageUrl = Constants.URL_IMAGE +imageName;
								
								//set the image to the image!!!

								HashMap<String, String> map = new HashMap<String, String>();

								map.put("id", id);
								map.put("title", title);
								//map.put("imageURL", imageUrl);

								myList.add(map);
							}
						} catch (JSONException e) {
							Log.e("log_tag", "Error parsing data " + e.toString());
						}
					}
				} catch (IllegalStateException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}

			}
			// System.out.println("the list size returned from doInBackground::"
			// +myList.size());
			return myList;

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
		protected void onPostExecute(final ArrayList<HashMap<String, String>> myList) {
			pDlg.dismiss();
			final ListView lv = getListView();

			try {
				ListAdapter adapter = new SimpleAdapter(NewsListByCategoryActivity.this, myList, R.layout.listitem, new String[] {
						"id", "title" }, new int[] { R.id.item_title, R.id.item_subtitle });
				
				

				// get listview current position - used to maintain scroll
				// position
				int currentPosition = lv.getFirstVisiblePosition();

				lv.setSelectionFromTop(currentPosition + 1, 0);

				// Adding Load More button to lisview at bottom
				//lv.addFooterView(btnLoadMore);

				setListAdapter(adapter);

				/**
				 * Listening to Load More button click event
				 * */
/*				btnLoadMore.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {

						// increment current page
						//current_page += 1;

						//retrieveNewsPosts();

						// Starting a new async task
						// retrieve the list
						// retrieveBoardsList();
						
						 * if(collegeBundle == null){ retrieveBoardsList();
						 * //flag here too!!!!! } else
						 * if(collegeBundle.getString("boardId") != null){
						 * retrieveBoardsListWithBoardId(); } else
						 * if(collegeBundle.getString("courseId") != null){
						 * retrieveBoardsListWithCollegeId(); }
						 
					}
				});*/

			} catch (Exception e) {
				e.printStackTrace();

				Intent homeActivityIntent = new Intent();
				startActivity(homeActivityIntent);

				Toast.makeText(NewsListByCategoryActivity.this, "The list cannot be populated. Please try again!!",
						Toast.LENGTH_LONG).show();
				System.out.println("The list cannot be populated.");
			}

			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					@SuppressWarnings("unchecked")
					HashMap<String, String> o = (HashMap<String, String>) lv.getItemAtPosition(position);

					// get the list id
					Integer pk = Integer.parseInt(o.get("id"));
					String pkName = o.get("title");

					System.out.println("pk::" + pk);
					System.out.println("pk Name ::" + pkName);

					Intent postDetailsInetnt = new Intent();
					postDetailsInetnt.setClass(NewsListByCategoryActivity.this, NewsDetailsActivity.class);
					postDetailsInetnt.putExtra("postId", pk);
					// Toast.makeText(BoardsActivity.this, "the board counts ::"
					// + mylist.size(), Toast.LENGTH_LONG).show();
					Toast.makeText(NewsListByCategoryActivity.this, "selected id:" + pk, Toast.LENGTH_LONG).show();
					startActivity(postDetailsInetnt);
				}
			});
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
