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

import greendroid.widget.ActionBarItem;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.SongInfoListAdapter;
import com.mongolduu.android.ng.misc.TextAndProgressBarUtils;
import com.mongolduu.android.ng.misc.Utils;

public class PickSongActivity extends OrmLiteGDActivity<DatabaseHelper> {

	private EditText searchText;
	private SongInfoListAdapter listadapter;
	private ListView listview;
	private View textandprogressbar = null;

	private TextWatcher filterTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (listadapter != null) {
				listadapter.getFilter().filter(s);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.pick_song);
		setTitle(R.string.pick_song_activity_title);

		searchText = (EditText) findViewById(R.id.search_text);
		searchText.addTextChangedListener(filterTextWatcher);
		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);

					return true;
				}
				return false;
			}
		});

		listview = (ListView) findViewById(R.id.list);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SongInfo songinfo = (SongInfo) parent.getAdapter().getItem(position);
				if (songinfo != null) {
					Intent intent = new Intent();
					intent.putExtra(IMongolduuConstants.SONG_ID_EXTRA_ID, songinfo.id);
					if (PickSongActivity.this.getParent() == null) {
						PickSongActivity.this.setResult(RESULT_OK, intent);
					} else {
						PickSongActivity.this.getParent().setResult(RESULT_OK, intent);
					}
					PickSongActivity.this.finish();
				}
			}
		});
		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

			}
		});

		textandprogressbar = getLayoutInflater().inflate(R.layout.text_and_progressbar, listview, false);
		listview.addFooterView(textandprogressbar, null, false);
		TextAndProgressBarUtils.initializeTextAndProgressBar(listview, textandprogressbar);
		TextAndProgressBarUtils.hideTextAndProgressBar(listview);

		List<SongInfo> songinfos = new LinkedList<SongInfo>();
		try {
			songinfos = getHelper().getSongInfoDao().queryForAll();
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}

		listadapter = new SongInfoListAdapter(this, songinfos, false);
		listview.setAdapter(listadapter);

		if (((SongInfoListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).isEmpty()) {
			TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.pick_song_empty), false, false);
			TextAndProgressBarUtils.showTextAndProgressBar(listview);
		}
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}

	private static final int CONTEXT_MENU_HOME = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_HOME:
			startActivity(Utils.createHomeIntent(this));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}