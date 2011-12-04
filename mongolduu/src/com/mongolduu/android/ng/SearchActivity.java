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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.HttpConnector;
import com.mongolduu.android.ng.misc.SongInfoListAdapter;
import com.mongolduu.android.ng.misc.TextAndProgressBarUtils;
import com.mongolduu.android.ng.misc.Utils;

public class SearchActivity extends AbstractSearchAndChartActivity {
	
	private class FetchSearchResultTask extends AsyncTask<String, Void, List<SongInfo>> {
		private boolean newSearch;
		
		public FetchSearchResultTask(boolean newSearch) {
			this.newSearch = newSearch;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (newSearch) {
				((SongInfoListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).clear();
				listview.invalidateViews();
			}
			TextAndProgressBarUtils.configureTextAndProgressBar(listview, null, false, true);
			TextAndProgressBarUtils.showTextAndProgressBar(listview);
		}

		@Override
		protected List<SongInfo> doInBackground(String... params) {
			String searchString = params[0];
			return HttpConnector.searchArtistOrSong(songId, searchString, newSearch ? 0 : ((SongInfoListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).getCount(), IMongolduuConstants.SEARCH_SIZE);
		}

		@Override
		protected void onPostExecute(List<SongInfo> result) {
			if (!isCancelled() && listview != null) {
				if (result != null) {
					try {
						Utils.checkIfSongSavedOnDevice(getHelper(), result, Utils.isExternalStorageMounted());
					} catch (SQLException e) {
						Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
					}
					((SongInfoListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).addSongs(result);
					listview.invalidateViews();
		
					if (songId == -1) {
						if (newSearch) {
							if (result.size() == 0) {
								TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.search_nothing), false, false);
							} else if (result.size() < IMongolduuConstants.SEARCH_SIZE) {
								TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.search_nomore), false, false);
							} else {
								TextAndProgressBarUtils.configureTextAndProgressBar(listview, String.format(getString(R.string.search_more), IMongolduuConstants.SEARCH_SIZE), true, false);
							}
						} else {
							if (result.size() < IMongolduuConstants.SEARCH_SIZE) {
								TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.search_nomore), false, false);
							} else {
								TextAndProgressBarUtils.configureTextAndProgressBar(listview, String.format(getString(R.string.search_more), IMongolduuConstants.SEARCH_SIZE), true, false);
							}
						}
					} else {
						TextAndProgressBarUtils.configureTextAndProgressBar(listview, "", false, false);
					}
				} else {
					TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.network_problem), false, false);
				}
			}

			currentTask = null;
			songId = -1;
			super.onPostExecute(result);
		}
	}
	
	private long songId = -1;
	private FetchSearchResultTask currentTask;
	private EditText searchText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.search);
        setTitle(R.string.search_activity_title);
        
        addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(new ActionBarDrawable(this, R.drawable.ic_action_bar_info)), R.id.action_bar_view_info);
        
        searchText = (EditText) findViewById(R.id.search_text);
		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);

					searchArtistOrSong(true);
					return true;
				}
				return false;
			}
		});
		
		listview = (ListView) findViewById(R.id.list);
		initializeListViewListener();
		
		textandprogressbar = getLayoutInflater().inflate(R.layout.text_and_progressbar, listview, false);
		listview.addFooterView(textandprogressbar, null, false);
		TextAndProgressBarUtils.initializeTextAndProgressBar(listview, textandprogressbar);
		TextAndProgressBarUtils.hideTextAndProgressBar(listview);
		
		listview.setAdapter(new SongInfoListAdapter(this, new ArrayList<SongInfo>(), true));
		
		showSongInfo();
	}
    
    @Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		
		showSongInfo();
	}
    
    private void showSongInfo() {
    	Bundle extras = getIntent().getExtras();
		if (extras != null) {
			songId = extras.getLong(IMongolduuConstants.SONG_ID_EXTRA_ID, -1);
		}
		if (songId != -1) {
			searchArtistOrSong(true);
		}
    }
    
    private synchronized void searchArtistOrSong(boolean firstSearch) {
		if (currentTask == null) {
			currentTask = new FetchSearchResultTask(firstSearch);
			currentTask.execute(searchText.getText().toString());
		}
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
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
		 if (keyCode == KeyEvent.KEYCODE_SEARCH) {
				searchArtistOrSong(true);
		 }
		 return super.onKeyUp(keyCode, event);
		
    }
	
	public void onTextAndProgressBarClick(View view) {
		searchArtistOrSong(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_SEARCH:
			searchArtistOrSong(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}