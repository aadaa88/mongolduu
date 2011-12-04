/*
 * Copyright (C) 2011 Erdene-Ochir Tuguldur (https://github.com/tugstugi)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongolduu.android.ng;

import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cyrilmottier.android.greendroid.R;
import com.google.android.c2dm.C2DMessaging;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.misc.Utils;

public class MainActivity extends OrmLiteGDActivity<DatabaseHelper> {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.main);

		((Button) findViewById(R.id.home_btn_news)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Utils.createNewsIntent(MainActivity.this));
			}
		});
		((Button) findViewById(R.id.home_btn_search)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Utils.createSearchIntent(MainActivity.this));
			}
		});
		((Button) findViewById(R.id.home_btn_chart)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Utils.createChartIntent(MainActivity.this));
			}
		});
		((Button) findViewById(R.id.home_btn_playlist)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Utils.createPlaylistIntent(MainActivity.this));
			}
		});
		((Button) findViewById(R.id.home_btn_settings)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Utils.createSettingsIntent(MainActivity.this));
			}
		});
		((Button) findViewById(R.id.home_btn_info)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(Utils.createInfoIntent(MainActivity.this));
			}
		});

		addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(new ActionBarDrawable(this, R.drawable.ic_action_bar_info)), R.id.action_bar_view_info);

		startService(Utils.createMediaPlayerServiceIntent(this));
		
		String registrationID = C2DMessaging.getRegistrationId(this);
		if (registrationID == null || registrationID.length() == 0) {
			C2DMessaging.register(this, IMongolduuConstants.NOTIFICATION_SERVICE_MAIL);
		}
		
		Utils.registerDevice(getApplicationContext(), false);
	}

	@Override
	public void onDestroy() {
		// stopService(Utils.createMediaPlayerServiceIntent(this));
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			stopService(Utils.createMediaPlayerServiceIntent(this));
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		case R.id.action_bar_view_info:
			startActivity(Utils.createInfoIntent(this));
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}
}