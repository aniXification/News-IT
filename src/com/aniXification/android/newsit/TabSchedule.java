package com.aniXification.android.newsit;

import java.security.acl.Group;

import android.app.TabActivity;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost.OnTabChangeListener;

public class TabSchedule extends TabActivity {
	final int GROUPS_COUNTS = 7;
	private SoundPool soundPool;
	private int soundID;
	boolean loaded = false;
	public static Group grps[];
	public static String currentTab = "One";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabwidget);
		Intent intent;

		grps = new Group[GROUPS_COUNTS];
		int tab_schedule[] = { R.drawable.tab_schedule_news, R.drawable.tab_schedule_list, R.drawable.tab_schedule_post,
				R.drawable.tab_schedule_profile };
		String groupName[] = { "", "", "", "" };

		int id = 0;
		intent = new Intent().setClass(this, NewsList.class);
		intent.putExtra("ActivityID", groupName[id]);
		getTabHost().addTab(
				getTabHost().newTabSpec(groupName[id] + "")
						.setIndicator(groupName[id], getResources().getDrawable(tab_schedule[id])).setContent(intent));

		id = 1;
		intent = new Intent().setClass(this, NewsListByCategoryActivity.class);
		intent.putExtra("ActivityID", groupName[id]);
		getTabHost().addTab(
				getTabHost().newTabSpec(groupName[id] + "")
						.setIndicator(groupName[id], getResources().getDrawable(tab_schedule[id])).setContent(intent));

		id = 2;
		intent = new Intent().setClass(this, MainActivity.class);
		intent.putExtra("ActivityID", groupName[id]);
		getTabHost().addTab(
				getTabHost().newTabSpec(groupName[id] + "")
						.setIndicator(groupName[id], getResources().getDrawable(tab_schedule[id])).setContent(intent));

		id = 3;
		intent = new Intent().setClass(this, UserProfileActivity.class);
		intent.putExtra("ActivityID", groupName[id]);
		getTabHost().addTab(
				getTabHost().newTabSpec(groupName[id] + "")
						.setIndicator(groupName[id], getResources().getDrawable(tab_schedule[id])).setContent(intent));

		getTabHost().setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				Log.d(TabSchedule.class.getName(), "Tab Changed : " + tabId);
				currentTab = tabId;
			}
		});

	}

}
