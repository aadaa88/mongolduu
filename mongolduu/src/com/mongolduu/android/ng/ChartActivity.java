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
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.cyrilmottier.android.greendroid.R;
import com.makeramen.segmented.SegmentedRadioGroup;
import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.HttpConnector;
import com.mongolduu.android.ng.misc.SongInfoListAdapter;
import com.mongolduu.android.ng.misc.TextAndProgressBarUtils;
import com.mongolduu.android.ng.misc.Utils;

public class ChartActivity extends AbstractSearchAndChartActivity {
	private class FetchChartListTask extends AsyncTask<Void, Void, List<SongInfo>> {
		private int chartTypeButtonId;
		
		public FetchChartListTask(int chartTypeButtonId) {
			this.chartTypeButtonId = chartTypeButtonId;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			((SongInfoListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).clear();
			listview.invalidateViews();
			TextAndProgressBarUtils.configureTextAndProgressBar(listview, null, false, true);
			TextAndProgressBarUtils.showTextAndProgressBar(listview);
		}

		@Override
		protected List<SongInfo> doInBackground(Void... params) {
			String chartType = null;
			switch (chartTypeButtonId) {
			case R.id.chart_type_today:
				chartType = IMongolduuConstants.CHART_TYPE_TODAY;
				break;
			case R.id.chart_type_week:
				chartType = IMongolduuConstants.CHART_TYPE_WEEK;
				break;
			case R.id.chart_type_alltime:
				chartType = IMongolduuConstants.CHART_TYPE_ALLTIME;
				break;
			case R.id.chart_type_new:
				chartType = IMongolduuConstants.CHART_TYPE_NEW;
				break;
			default:
				chartType = IMongolduuConstants.CHART_TYPE_TODAY;
				break;
			}
			return HttpConnector.fetchChartList(chartType);
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
					TextAndProgressBarUtils.hideTextAndProgressBar(listview);
				} else {
					TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.network_problem), false, false);
				}
			}

			if (refreshItem != null) {
				refreshItem.setLoading(false);
			}
			currentTask = null;
			super.onPostExecute(result);
		}
	}
	
	private LoaderActionBarItem refreshItem = null;
	private SegmentedRadioGroup radiogroup;
	
	private FetchChartListTask currentTask;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.chart);
        setTitle(R.string.chart_activity_title);
        
        refreshItem = (LoaderActionBarItem) addActionBarItem(Type.Refresh, R.id.action_bar_refresh);
        addActionBarItem(Type.Search, R.id.action_bar_search);
        
        listview = (ListView) findViewById(R.id.list);
        initializeListViewListener();
        
        textandprogressbar = getLayoutInflater().inflate(R.layout.text_and_progressbar, listview, false);
        listview.addFooterView(textandprogressbar, null, false);
        TextAndProgressBarUtils.initializeTextAndProgressBar(listview, textandprogressbar);
        
        radiogroup = (SegmentedRadioGroup) findViewById(R.id.chart_type_radiogroup);
		radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				refreshChartList();
			}
		});
		
		listview.setAdapter(new SongInfoListAdapter(this, new LinkedList<SongInfo>(), true));
		
		refreshChartList();
	}
    
    public void onResume() {
    	try {
			Utils.checkIfSongSavedOnDevice(getHelper(), ((SongInfoListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()), Utils.isExternalStorageMounted());
			listview.invalidateViews();
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
    	super.onResume();
    }
    
    private void refreshChartList() {
    	if (currentTask != null) {
    		currentTask.cancel(true);
    	}
    	currentTask = new FetchChartListTask(radiogroup.getCheckedRadioButtonId());
    	currentTask.execute();
    }

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		case R.id.action_bar_search:
			startActivity(Utils.createSearchIntent(this));
			return true;
		case R.id.action_bar_refresh:
			refreshChartList();
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean retValue = super.onCreateOptionsMenu(menu);
		menu.add(0, CONTEXT_MENU_REFRESH, 0, R.string.context_menu_refresh);
		return retValue;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_REFRESH:
			//refreshItem.setLoading(true);
			refreshChartList();
			return true;
		case CONTEXT_MENU_SEARCH:
			startActivity(Utils.createSearchIntent(this));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}